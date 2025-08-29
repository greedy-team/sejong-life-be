package org.example.sejonglifebe.review;

import org.example.sejonglifebe.auth.AuthUser;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.example.sejonglifebe.place.PlaceRepository;
import org.example.sejonglifebe.place.entity.Place;
import org.example.sejonglifebe.review.dto.ReviewRequest;
import org.example.sejonglifebe.tag.Tag;
import org.example.sejonglifebe.tag.TagRepository;
import org.example.sejonglifebe.user.User;
import org.example.sejonglifebe.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

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
    @DisplayName("리뷰 생성 테스트")
    class CreateReviewTest {

        @Test
        @DisplayName("정상적으로 리뷰를 생성한다")
        void createReview_success() {
            // given
            Long placeId = 1L;
            AuthUser authUser = new AuthUser("21011111");
            User user = User.builder().studentId("21011111").nickname("닉네임").build();
            Place place = Place.builder().name("맛집").address("주소").mapLinks(null).mainImageUrl("url").build();
            Tag tag = new Tag("가성비");
            ReviewRequest request = new ReviewRequest(5, "맛있어요", List.of(1L));

            given(userRepository.findByStudentId("21011111")).willReturn(Optional.of(user));
            given(placeRepository.findById(placeId)).willReturn(Optional.of(place));
            given(tagRepository.findByIdIn(request.tagIds())).willReturn(List.of(tag));

            // when
            reviewService.createReview(placeId, request, authUser);

            // then
            verify(reviewRepository).save(Mockito.<Review>any());
        }

        @Test
        @DisplayName("존재하지 않는 장소이면 예외를 던진다")
        void createReview_placeNotFound() {
            // given
            Long placeId = 1L;
            AuthUser authUser = new AuthUser("21011111");
            ReviewRequest request = new ReviewRequest(5, "맛있어요", List.of());

            given(userRepository.findByStudentId("21011111"))
                    .willReturn(Optional.of(User.builder().studentId("21011111").nickname("닉네임").build()));
            given(placeRepository.findById(placeId)).willReturn(Optional.empty());

            // then
            assertThatThrownBy(() -> reviewService.createReview(placeId, request, authUser))
                    .isInstanceOf(SejongLifeException.class)
                    .hasMessageContaining(ErrorCode.PLACE_NOT_FOUND.getErrorMessage());
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
                    .hasMessageContaining(ErrorCode.DUPLICATE_LIKE.getErrorMessage());
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
                    .hasMessageContaining(ErrorCode.REVIEW_LIKE_NOT_FOUND.getErrorMessage());
        }
    }
}
