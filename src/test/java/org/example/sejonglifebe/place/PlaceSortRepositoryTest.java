package org.example.sejonglifebe.place;

import org.example.sejonglifebe.place.dto.PlaceQueryResult;
import org.example.sejonglifebe.place.dto.PlaceSortType;
import org.example.sejonglifebe.place.entity.Place;
import org.example.sejonglifebe.review.Review;
import org.example.sejonglifebe.review.ReviewRepository;
import org.example.sejonglifebe.user.User;
import org.example.sejonglifebe.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PlaceSortRepositoryTest {

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    private final Pageable pageable = PageRequest.of(0, 10);

    private Place placeA; // 리뷰 3개, 평균 별점 5, 조회수 1
    private Place placeB; // 리뷰 2개, 평균 별점 3, 조회수 10
    private Place placeC; // 리뷰 0개, 평균 별점 없음, 조회수 5

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();
        placeRepository.deleteAll();
        userRepository.deleteAll();

        User user = userRepository.save(
                User.builder().studentId("21010001").nickname("테스터").build()
        );

        placeA = placeRepository.save(newPlace("장소A"));
        placeB = placeRepository.save(newPlace("장소B"));
        placeC = placeRepository.save(newPlace("장소C"));

        // 리뷰 개수 & 별점 세팅
        saveReview(placeA, user, 5);
        saveReview(placeA, user, 5);
        saveReview(placeA, user, 5); // A: 3개, 평균 5
        saveReview(placeB, user, 4);
        saveReview(placeB, user, 2); // B: 2개, 평균 3
        // C: 리뷰 없음

        // 조회수 세팅
        placeRepository.increaseViewCount(placeB.getId()); // B: 10
        for (int i = 0; i < 9; i++) {
            placeRepository.increaseViewCount(placeB.getId());
        }
        for (int i = 0; i < 5; i++) {
            placeRepository.increaseViewCount(placeC.getId()); // C: 5
        }
        placeRepository.increaseViewCount(placeA.getId()); // A: 1
    }

    private Place newPlace(String name) {
        return Place.createPlace(name, "주소", null, null, null, false, "");
    }

    private void saveReview(Place place, User user, int rating) {
        Review review = Review.createReview(place, user, rating, "리뷰");
        reviewRepository.save(review);
    }

    private List<String> names(Page<PlaceQueryResult> page) {
        return page.getContent().stream()
                .map(result -> result.place().getName())
                .toList();
    }

    @Nested
    @DisplayName("정렬 기준별 조회")
    class SortByType {

        @Test
        @DisplayName("REVIEW_COUNT: 리뷰 많은 순으로 정렬한다")
        void sortByReviewCount() {
            Page<PlaceQueryResult> result = placeRepository.getPlacesByConditions(
                    null, List.of(), null, false, PlaceSortType.REVIEW_COUNT, null, null, pageable);

            // A(3) > B(2) > C(0)
            assertThat(names(result)).containsExactly("장소A", "장소B", "장소C");
        }

        @Test
        @DisplayName("RATING: 평균 별점 높은 순으로 정렬하고, 리뷰 없는 장소는 맨 뒤로 보낸다")
        void sortByRating() {
            Page<PlaceQueryResult> result = placeRepository.getPlacesByConditions(
                    null, List.of(), null, false, PlaceSortType.RATING, null, null, pageable);

            // A(5) > B(3) > C(리뷰 없음 → 맨 뒤)
            assertThat(names(result)).containsExactly("장소A", "장소B", "장소C");
        }

        @Test
        @DisplayName("VIEW_COUNT: 조회수 많은 순으로 정렬한다")
        void sortByViewCount() {
            Page<PlaceQueryResult> result = placeRepository.getPlacesByConditions(
                    null, List.of(), null, false, PlaceSortType.VIEW_COUNT, null, null, pageable);

            // B(10) > C(5) > A(1)
            assertThat(names(result)).containsExactly("장소B", "장소C", "장소A");
        }

        @Test
        @DisplayName("sort가 null이면 기본값(REVIEW_COUNT)으로 정렬한다")
        void sortByNull_usesReviewCountDefault() {
            Page<PlaceQueryResult> result = placeRepository.getPlacesByConditions(
                    null, List.of(), null, false, null, null, null, pageable);

            // 기본값 = REVIEW_COUNT → A(3) > B(2) > C(0)
            assertThat(names(result)).containsExactly("장소A", "장소B", "장소C");
        }
    }
}
