package org.example.sejonglifebe.place;

import jakarta.persistence.EntityManager;
import org.example.sejonglifebe.category.Category;
import org.example.sejonglifebe.common.config.QueryDslConfig;
import org.example.sejonglifebe.common.storage.TransactionalImageStorage;
import org.example.sejonglifebe.place.dto.PlaceUpdateRequest;
import org.example.sejonglifebe.place.entity.MapLinks;
import org.example.sejonglifebe.place.entity.Place;
import org.example.sejonglifebe.place.view.PlaceViewService;
import org.example.sejonglifebe.review.Review;
import org.example.sejonglifebe.s3.S3Service;
import org.example.sejonglifebe.tag.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@DataJpaTest(showSql = false, properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import({PlaceService.class, QueryDslConfig.class, TransactionalImageStorage.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PlaceThumbnailConcurrencyTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlaceService placeService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @MockitoBean(name = "s3Service")
    private S3Service imageStorage;

    @MockitoBean
    private PlaceViewService placeViewService;

    @ParameterizedTest(name = "기존 썸네일={0}, 첫 요청 삭제={1}, 두 번째 요청 삭제={2}")
    @CsvSource({"false,false,false", "true,false,false", "true,false,true", "true,true,false"})
    @DisplayName("동시에 수정해도 썸네일은 최대 하나만 유지하고 리뷰 사진은 보존한다")
    void concurrentUpdates_preserveSingleThumbnailAndReview(
            boolean hasThumbnail,
            boolean firstDelete,
            boolean secondDelete
    ) throws Exception {
        // given
        Category firstCategory = new Category("식당");
        Category secondCategory = new Category("카페");
        Tag firstTag = new Tag("맛집");
        Tag secondTag = new Tag("분위기");
        entityManager.persist(firstCategory);
        entityManager.persist(secondCategory);
        entityManager.persist(firstTag);
        entityManager.persist(secondTag);
        Place place = Place.createPlace("장소", "주소", null, null, new MapLinks("", "", ""), false, null);
        if (hasThumbnail) {
            place.addImage("old.webp", true);
            place.addCategory(firstCategory);
            place.addTag(firstTag);
        }
        entityManager.persist(place);
        Review review = Review.builder().place(place).rating(5).content("리뷰").build();
        ReflectionTestUtils.setField(review, "createdAt", LocalDateTime.now());
        review.addImage("review.webp");
        entityManager.persist(review);
        entityManager.flush();
        Long placeId = place.getId();
        PlaceUpdateRequest firstRequest = request(firstCategory.getId(), firstTag.getId());
        PlaceUpdateRequest secondRequest = request(secondCategory.getId(), secondTag.getId());
        TestTransaction.flagForCommit();
        TestTransaction.end();
        given(imageStorage.uploadImage(anyString(), any()))
                .willAnswer(invocation -> ((MockMultipartFile) invocation.getArgument(1)).getOriginalFilename());

        // when
        CountDownLatch firstUpdated = new CountDownLatch(1);
        CountDownLatch releaseFirstCommit = new CountDownLatch(1);
        CountDownLatch secondStarted = new CountDownLatch(1);
        CountDownLatch secondUpdated = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<?> first = executor.submit(() ->
                    new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
                        update(placeId, firstRequest, "first.webp", firstDelete);
                        firstUpdated.countDown();
                        await(releaseFirstCommit);
                    }));
            assertThat(firstUpdated.await(10, TimeUnit.SECONDS)).isTrue();
            Future<?> second = executor.submit(() ->
                    new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
                        secondStarted.countDown();
                        update(placeId, secondRequest, "second.webp", secondDelete);
                        secondUpdated.countDown();
                    }));
            assertThat(secondStarted.await(10, TimeUnit.SECONDS)).isTrue();
            boolean secondPassedBeforeFirstCommit = secondUpdated.await(500, TimeUnit.MILLISECONDS);
            releaseFirstCommit.countDown();
            first.get(10, TimeUnit.SECONDS);
            second.get(10, TimeUnit.SECONDS);
            // then
            assertThat(secondPassedBeforeFirstCommit)
                    .as("첫 트랜잭션 커밋 전에 동일 장소 수정이 진행되면 안 된다")
                    .isFalse();

            new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
                Place saved = entityManager.find(Place.class, placeId);
                assertThat(saved.getPlaceImages().stream()
                        .filter(image -> Boolean.TRUE.equals(image.getIsThumbnail()))
                        .count())
                        .isEqualTo(secondDelete ? 0 : 1);
                assertThat(saved.getThumbnailImage()).isEqualTo(secondDelete ? "review.webp" : "second.webp");
                assertThat(saved.getPlaceImages().stream().filter(image -> image.getReview() != null))
                        .extracting(image -> image.getUrl()).containsExactly("review.webp");
            });
        } finally {
            releaseFirstCommit.countDown();
            executor.shutdownNow();
            assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
        }
    }

    private PlaceUpdateRequest request(Long categoryId, Long tagId) {
        return new PlaceUpdateRequest("장소", "주소", null, null, List.of(categoryId), List.of(tagId),
                new MapLinks("", "", ""), false, "");
    }

    private void update(Long placeId, PlaceUpdateRequest request, String filename, boolean delete) {
        placeService.updatePlace(placeId, request,
                delete ? null : new MockMultipartFile("thumbnail", filename, "image/png", new byte[]{1}), delete);
    }

    private void await(CountDownLatch latch) {
        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("동시성 테스트 동기화 시간 초과");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception);
        }
    }
}
