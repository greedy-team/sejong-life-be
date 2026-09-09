package org.example.sejonglifebe.common.storage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.transaction.support.TransactionSynchronization.STATUS_COMMITTED;
import static org.springframework.transaction.support.TransactionSynchronization.STATUS_ROLLED_BACK;
import static org.springframework.transaction.support.TransactionSynchronization.STATUS_UNKNOWN;

@ExtendWith(MockitoExtension.class)
class TransactionalImageStorageTest {

    @Mock
    private ImageStorage delegate;

    private ImageStorage storage;
    private final MockMultipartFile file = new MockMultipartFile("thumbnail", new byte[]{1});

    @BeforeEach
    void setUp() {
        storage = new TransactionalImageStorage(delegate);
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.clear();
    }

    @Test
    @DisplayName("삭제 요청의 URL 목록을 보존하고 커밋 이후 파일을 삭제한다")
    void delete_waitsUntilCommitAndCopiesUrls() {
        // given
        List<String> urls = new ArrayList<>(List.of("old.webp"));

        // when
        storage.deleteImages(urls);
        urls.clear();

        // then
        verify(delegate, never()).deleteImages(anyList());
        complete(STATUS_COMMITTED);
        verify(delegate).deleteImages(List.of("old.webp"));
    }

    @Test
    @DisplayName("롤백하면 기존 파일을 삭제하지 않는다")
    void delete_preservesFilesOnRollback() {
        // when
        storage.deleteImages(List.of("old.webp", "review.webp"));
        complete(STATUS_ROLLED_BACK);

        // then
        verify(delegate, never()).deleteImages(anyList());
    }

    @Test
    @DisplayName("업로드 후 롤백하면 신규 파일을 정리한다")
    void upload_registersRollbackCleanupBeforeReturning() {
        // given
        given(delegate.uploadImage("1", file)).willReturn("new.webp");

        // when & then
        assertThat(storage.uploadImage("1", file)).isEqualTo("new.webp");
        verify(delegate, never()).deleteImages(anyList());
        complete(STATUS_ROLLED_BACK);
        verify(delegate).deleteImages(List.of("new.webp"));
    }

    @Test
    @DisplayName("커밋하면 신규 업로드 파일을 유지한다")
    void upload_keepsNewFileOnCommit() {
        // given
        given(delegate.uploadImage("1", file)).willReturn("new.webp");

        // when
        storage.uploadImage("1", file);
        complete(STATUS_COMMITTED);

        // then
        verify(delegate, never()).deleteImages(anyList());
    }

    @Test
    @DisplayName("후속 업로드 실패로 롤백하면 먼저 업로드한 파일을 정리한다")
    void laterUploadFailure_cleansEarlierUploadOnRollback() {
        // given
        RuntimeException failure = new IllegalStateException("두 번째 업로드 실패");
        given(delegate.uploadImage("1", file)).willReturn("first.webp").willThrow(failure);

        // when
        storage.uploadImage("1", file);

        // then
        assertThatThrownBy(() -> storage.uploadImage("1", file)).isSameAs(failure);
        complete(STATUS_ROLLED_BACK);
        verify(delegate).deleteImages(List.of("first.webp"));
    }

    @Test
    @DisplayName("트랜잭션 상태를 알 수 없으면 파일을 삭제하지 않는다")
    void unknownTransactionStatus_doesNotDeleteEitherFile() {
        // given
        given(delegate.uploadImage("1", file)).willReturn("new.webp");

        // when
        storage.uploadImage("1", file);
        storage.deleteImages(List.of("old.webp"));
        complete(STATUS_UNKNOWN);

        // then
        verify(delegate, never()).deleteImages(anyList());
    }

    @Test
    @DisplayName("파일 정리 실패는 완료된 트랜잭션에 영향을 주지 않는다")
    void cleanupFailure_doesNotChangeCompletedTransaction() {
        // given
        storage.deleteImages(List.of("old.webp"));
        doThrow(new IllegalStateException("S3 unavailable")).when(delegate).deleteImages(List.of("old.webp"));

        // when & then
        assertThatCode(() -> complete(STATUS_COMMITTED)).doesNotThrowAnyException();
        verify(delegate).deleteImages(List.of("old.webp"));
    }

    @Test
    @DisplayName("트랜잭션 밖에서는 즉시 삭제하고 실패를 전파한다")
    void outsideTransaction_deletesImmediatelyAndPropagatesFailure() {
        // given
        TransactionSynchronizationManager.clear();
        RuntimeException failure = new IllegalStateException("S3 unavailable");
        doThrow(failure).when(delegate).deleteImages(List.of("old.webp"));

        // when & then
        assertThatThrownBy(() -> storage.deleteImages(List.of("old.webp"))).isSameAs(failure);
    }

    @Test
    @DisplayName("삭제할 URL이 없으면 저장소를 호출하지 않는다")
    void emptyDelete_doesNotCallStorage() {
        // when
        storage.deleteImages(List.of());
        complete(STATUS_COMMITTED);

        // then
        verifyNoInteractions(delegate);
    }

    private void complete(int status) {
        TransactionSynchronizationManager.getSynchronizations()
                .forEach(synchronization -> synchronization.afterCompletion(status));
    }
}
