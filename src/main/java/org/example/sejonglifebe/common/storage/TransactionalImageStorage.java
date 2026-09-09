package org.example.sejonglifebe.common.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Primary
@Service
public class TransactionalImageStorage implements ImageStorage {

    private final ImageStorage delegate;

    public TransactionalImageStorage(@Qualifier("s3Service") ImageStorage delegate) {
        this.delegate = delegate;
    }

    @Override
    public String uploadImage(String keyPrefix, MultipartFile image) {
        String uploadedUrl = delegate.uploadImage(keyPrefix, image);
        if (isTransactionActive()) {
            scheduleDeletion(List.of(uploadedUrl), TransactionSynchronization.STATUS_ROLLED_BACK);
        }
        return uploadedUrl;
    }

    @Override
    public void deleteImages(List<String> imageUrls) {
        if (imageUrls.isEmpty()) {
            return;
        }
        if (isTransactionActive()) {
            scheduleDeletion(List.copyOf(imageUrls), TransactionSynchronization.STATUS_COMMITTED);
        } else {
            delegate.deleteImages(imageUrls);
        }
    }

    private boolean isTransactionActive() {
        return TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isSynchronizationActive();
    }

    private void scheduleDeletion(List<String> urls, int targetStatus) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != targetStatus) {
                    return;
                }
                try {
                    delegate.deleteImages(urls);
                } catch (RuntimeException exception) {
                    log.error("이미지 파일 정리 실패: urls={}", urls, exception);
                }
            }
        });
    }
}
