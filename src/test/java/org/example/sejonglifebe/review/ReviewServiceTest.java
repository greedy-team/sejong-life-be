package org.example.sejonglifebe.review;

import org.example.sejonglifebe.auth.AuthUser;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.example.sejonglifebe.place.PlaceRepository;
import org.example.sejonglifebe.place.entity.Place;
import org.example.sejonglifebe.review.dto.RatingCount;
import org.example.sejonglifebe.review.dto.ReviewRequest;
import org.example.sejonglifebe.review.dto.ReviewSummaryResponse;
import org.example.sejonglifebe.tag.Tag;
import org.example.sejonglifebe.tag.TagRepository;
import org.example.sejonglifebe.user.User;
import org.example.sejonglifebe.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private PlaceRepository placeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewLikeRepository reviewLikeRepository;

    @InjectMocks
    private ReviewService reviewService;

    @Nested
    @DisplayName("리뷰 조회 테스트")
    class GetReviewTest {

        @Test
        @DisplayName("존재하지 않는 장소이면 예외를 던진다")
        void getReviews_placeNotFound() {
            // given
            given(placeRepository.findById(1L)).willReturn(Optional.empty());

            // then
            assertThatThrownBy(() -> reviewService.getReviewsByPlaceId(1L, null))
                    .isInstanceOf(SejongLifeException.class)
                    .hasMessageContaining(ErrorCode.PLACE_NOT_FOUND.getErrorMessage());
        }

        @Test
        @DisplayName("로그인하지 않은 경우 liked를 false로 반환된다")
        void getReviews_noAuthUser() {
            // given
            Place place = Place.builder()
                    .name("맛집")
                    .address("주소")
                    .build();
            User user = User.builder()
                    .studentId("21011111")
                    .nickname("닉네임")
                    .build();

            Review review = Review.createReview(place, user, 5, "맛있어요");
            ReflectionTestUtils.setField(review, "id", 1L);
            ReflectionTestUtils.setField(review, "createdAt", LocalDateTime.now());

            given(placeRepository.findById(1L)).willReturn(Optional.of(place));
            given(reviewRepository.findByPlace(place)).willReturn(List.of(review));

            // when
            var result = reviewService.getReviewsByPlaceId(1L, null);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).liked()).isFalse();
        }

        @Test
        @DisplayName("로그인한 사용자가 좋아요한 리뷰는 liked를 true로 반환된다")
        void getReviews_withAuthUser() {
            // given
            AuthUser authUser = new AuthUser("21011111");
            Place place = Place.builder()
                    .name("맛집")
                    .address("주소")
                    .build();
            User user = User.builder()
                    .studentId("21011111")
                    .nickname("닉네임")
                    .build();

            Review review = Review.createReview(place, user, 5, "맛있어요");
            ReflectionTestUtils.setField(review, "id", 1L);
            ReflectionTestUtils.setField(review, "createdAt", LocalDateTime.now());

            given(placeRepository.findById(1L)).willReturn(Optional.of(place));
            given(reviewRepository.findByPlace(place)).willReturn(List.of(review));
            given(reviewLikeRepository.findByUserStudentId("21011111"))
                    .willReturn(List.of(ReviewLike.createReviewLike(review, user)));

            // when
            var result = reviewService.getReviewsByPlaceId(1L, authUser);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).liked()).isTrue();
        }
    }

    @Nested
    @DisplayName("리뷰 요약 조회 테스트")
    class getReviewSummaryTest {

        @Test
        @DisplayName("장소가 없으면 PLACE_NOT_FOUND 예외를 던진다")
        void getSummary_placeNotFound() {
            // given
            given(placeRepository.findById(1L)).willReturn(Optional.empty());

            // then
            assertThatThrownBy(() -> reviewService.getReviewSummaryByPlaceId(1L))
                    .isInstanceOf(SejongLifeException.class)
                    .hasMessage(ErrorCode.PLACE_NOT_FOUND.getErrorMessage());
        }

        @Test
        @DisplayName("리뷰 0건이면 count=0, averageRate=null, 분포는 전부 0으로 반환한다")
        void getSummary_zeroReviews() {
            // given
            Place place = Place.builder()
                    .name("맛집")
                    .address("주소")
                    .mainImageUrl("url")
                    .build();

            given(placeRepository.findById(1L)).willReturn(Optional.of(place));
            given(reviewRepository.countByPlace(place)).willReturn(0L);
            given(reviewRepository.averageRatingByPlace(place)).willReturn(null);
            given(reviewRepository.findRatingCountsByPlace(place)).willReturn(List.of());

            // when
            ReviewSummaryResponse result = reviewService.getReviewSummaryByPlaceId(1L);

            // then
            assertThat(result.reviewCount()).isEqualTo(0L);
            assertThat(result.averageRate()).isNull();
            for (int i = 1; i <= 5; i++) {
                assertThat(result.ratingDistribution().get(String.valueOf(i))).isEqualTo(0L);
            }
        }

        @Test
        @DisplayName("리뷰 다수면 누락 구간 0으로 채우고 평균, 분포를 계산한다")
        void getSummary_fillMissingBuckets() {
            // given
            Place place = Place.builder()
                    .name("맛집")
                    .address("주소")
                    .mainImageUrl("url")
                    .build();

            given(placeRepository.findById(1L)).willReturn(Optional.of(place));
            given(reviewRepository.countByPlace(place)).willReturn(3L);
            given(reviewRepository.averageRatingByPlace(place)).willReturn(4.0);
            given(reviewRepository.findRatingCountsByPlace(place))
                    .willReturn(List.of(
                            new RatingCount(5, 2L),
                            new RatingCount(3, 1L)
                    ));

            // when
            ReviewSummaryResponse res = reviewService.getReviewSummaryByPlaceId(1L);

            // then
            assertThat(res.reviewCount()).isEqualTo(3L);
            assertThat(res.averageRate()).isEqualTo(4.0);
            assertThat(res.ratingDistribution().get("1")).isEqualTo(0L);
            assertThat(res.ratingDistribution().get("2")).isEqualTo(0L);
            assertThat(res.ratingDistribution().get("3")).isEqualTo(1L);
            assertThat(res.ratingDistribution().get("4")).isEqualTo(0L);
            assertThat(res.ratingDistribution().get("5")).isEqualTo(2L);
        }
    }

    @Nested
    @DisplayName("리뷰 생성 테스트")
    class CreateReviewTest {

        @Test
        @DisplayName("정상적으로 리뷰를 생성한다")
        void createReview_success() {
            // given
            Long placeId = 1L;
            AuthUser authUser = new AuthUser("21011111");
            User user = User.builder()
                    .studentId("21011111")
                    .nickname("닉네임")
                    .build();
            Place place = Place.builder()
                    .name("맛집")
                    .address("주소")
                    .mapLinks(null)
                    .build();
            Tag tag = new Tag("가성비");
            ReviewRequest request = new ReviewRequest(5, "맛있어요", List.of(1L));

            given(userRepository.findByStudentId("21011111")).willReturn(Optional.of(user));
            given(placeRepository.findById(placeId)).willReturn(Optional.of(place));
            given(tagRepository.findByIdIn(request.tagIds())).willReturn(List.of(tag));

            // when
            reviewService.createReview(placeId, request, authUser, null);

            // then
            ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
            verify(reviewRepository).save(captor.capture());

            Review saved = captor.getValue();
            assertThat(saved.getContent()).isEqualTo("맛있어요");
            assertThat(saved.getUser().getStudentId()).isEqualTo("21011111");
            assertThat(saved.getPlace().getName()).isEqualTo("맛집");
            assertThat(saved.getReviewTags().get(0).getTag().getName()).isEqualTo("가성비");
        }

        @Test
        @DisplayName("존재하지 않는 장소이면 예외를 던진다")
        void createReview_placeNotFound() {
            // given
            Long placeId = 1L;
            AuthUser authUser = new AuthUser("21011111");
            ReviewRequest request = new ReviewRequest(5, "맛있어요", List.of());

            given(userRepository.findByStudentId("21011111"))
                    .willReturn(Optional.of(User.builder()
                            .studentId("21011111")
                            .nickname("닉네임")
                            .build()));
            given(placeRepository.findById(placeId)).willReturn(Optional.empty());

            // then
            assertThatThrownBy(() -> reviewService.createReview(placeId, request, authUser, null))
                    .isInstanceOf(SejongLifeException.class)
                    .hasMessage(ErrorCode.PLACE_NOT_FOUND.getErrorMessage());
        }
    }

    @Nested
    @DisplayName("좋아요 테스트")
    class CreateLikeTest {

        @Test
        @DisplayName("이미 좋아요가 있으면 예외를 던진다")
        void createLike_duplicate() {
            // given
            Long reviewId = 1L;
            AuthUser authUser = new AuthUser("21011111");
            User user = User.builder().studentId("21011111").nickname("닉네임").build();
            Review review = Review.builder().build();
            ReflectionTestUtils.setField(review, "id", reviewId);

            given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
            given(userRepository.findByStudentId("21011111")).willReturn(Optional.of(user));
            given(reviewLikeRepository.existsByReviewAndUser(review, user)).willReturn(true);

            // then
            assertThatThrownBy(() -> reviewService.createLike(reviewId, authUser))
                    .isInstanceOf(SejongLifeException.class)
                    .hasMessage(ErrorCode.DUPLICATE_LIKE.getErrorMessage());
        }
    }

    @Nested
    @DisplayName("좋아요 취소 테스트")
    class DeleteLikeTest {

        @Test
        @DisplayName("존재하지 않는 좋아요는 삭제할 수 없다")
        void deleteLike_notFound() {
            // given
            Long reviewId = 1L;
            AuthUser authUser = new AuthUser("21011111");

            given(reviewRepository.findById(reviewId))
                    .willReturn(Optional.of(Review.builder().build()));
            given(reviewLikeRepository.findByReviewIdAndUserStudentId(reviewId, "21011111"))
                    .willReturn(Optional.empty());

            // then
            assertThatThrownBy(() -> reviewService.deleteLike(reviewId, authUser))
                    .isInstanceOf(SejongLifeException.class)
                    .hasMessage(ErrorCode.REVIEW_LIKE_NOT_FOUND.getErrorMessage());
        }
    }
}
