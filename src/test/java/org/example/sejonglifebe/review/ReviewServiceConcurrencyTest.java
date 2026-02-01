package org.example.sejonglifebe.review;

import org.example.sejonglifebe.auth.AuthUser;
import org.example.sejonglifebe.category.CategoryRepository;
import org.example.sejonglifebe.place.PlaceRepository;
import org.example.sejonglifebe.place.entity.Place;
import org.example.sejonglifebe.review.dto.ReviewRequest;
import org.example.sejonglifebe.tag.Tag;
import org.example.sejonglifebe.tag.TagRepository;
import org.example.sejonglifebe.user.Role;
import org.example.sejonglifebe.user.User;
import org.example.sejonglifebe.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ReviewServiceConcurrencyTest {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    private Place testPlace;
    private Tag testTag;
    private List<User> testUsers;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();
        placeRepository.deleteAll();
        userRepository.deleteAll();
        tagRepository.deleteAll();

        testPlace = placeRepository.save(
                Place.createPlace("테스트 맛집", "서울시 테스트구", null, false, null)
        );

        testTag = tagRepository.save(new Tag("가성비"));

        testUsers = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            User user = userRepository.save(
                    User.builder()
                            .studentId("2101000" + i)
                            .nickname("테스터" + i)
                            .build()
            );
            testUsers.add(user);
        }
    }

    @Test
    @DisplayName("동시에 같은 장소에 리뷰를 작성해도 재시도로 모두 성공한다")
    void concurrentReviewCreation_shouldSucceedWithRetry() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    AuthUser authUser = new AuthUser(testUsers.get(index).getStudentId(), Role.USER);
                    ReviewRequest request = new ReviewRequest(5, "맛있어요" + index, List.of(testTag.getId()));

                    reviewService.createReview(testPlace.getId(), request, authUser, null);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    System.out.println("실패 [" + index + "]: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        System.out.println("===== 결과 =====");
        System.out.println("성공: " + successCount.get());
        System.out.println("실패: " + failCount.get());

        assertThat(successCount.get()).isEqualTo(threadCount);
        assertThat(failCount.get()).isZero();
    }

}
