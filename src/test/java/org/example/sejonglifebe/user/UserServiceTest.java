package org.example.sejonglifebe.user;

import org.example.sejonglifebe.auth.AuthUser;
import org.example.sejonglifebe.auth.PortalStudentInfo;
import org.example.sejonglifebe.common.jwt.JwtTokenProvider;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.example.sejonglifebe.place.entity.Place;
import org.example.sejonglifebe.review.Review;
import org.example.sejonglifebe.review.ReviewLikeRepository;
import org.example.sejonglifebe.review.ReviewRepository;
import org.example.sejonglifebe.s3.S3Service;
import org.example.sejonglifebe.user.dto.SignUpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    JwtTokenProvider jwtTokenProvider;

    @Mock
    ReviewRepository reviewRepository;

    @Mock
    ReviewLikeRepository reviewLikeRepository;

    @Mock
    S3Service s3Service;

    @InjectMocks
    UserService userService;

    @Nested
    @DisplayName("유저 생성 테스트")
    class CreateUserTest {

        @Test
        @DisplayName("중복된 닉네임이면 예외를 던진다")
        void givenDuplicateNickname_whenCreateUser_thenThrowException() {
            // given
            SignUpRequest request = SignUpRequest.builder()
                    .studentId("21111111")
                    .name("홍길동")
                    .nickname("존재하는 닉네임")
                    .build();

            PortalStudentInfo portalInfo = PortalStudentInfo.builder()
                    .studentId("21111111")
                    .name("홍길동")
                    .department("컴퓨터공학과")
                    .build();

            given(userRepository.existsByNickname("존재하는 닉네임")).willReturn(true);

            // then
            assertThatThrownBy(() -> userService.createUser(request, portalInfo))
                    .isInstanceOf(SejongLifeException.class)
                    .hasMessage(ErrorCode.DUPLICATE_NICKNAME.getErrorMessage());

            verify(userRepository, never()).save(any());
            verify(jwtTokenProvider, never()).createToken(any());
        }

        @Test
        @DisplayName("유저가 정상적으로 생성된다")
        void createUser_success() {
            // given
            SignUpRequest request = SignUpRequest.builder()
                    .studentId("21111111")
                    .name("홍길동")
                    .nickname("새로 생성된 닉네임")
                    .build();

            PortalStudentInfo portalInfo = PortalStudentInfo.builder()
                    .studentId("21111111")
                    .name("홍길동")
                    .department("컴퓨터공학과")
                    .build();

            User savedUser = User.builder()
                    .id(1L)
                    .studentId("21111111")
                    .name("홍길동")
                    .department("컴퓨터공학과")
                    .nickname("새로 생성된 닉네임")
                    .build();

            given(userRepository.existsByNickname("새로 생성된 닉네임")).willReturn(false);
            given(userRepository.save(any(User.class))).willReturn(savedUser);
            given(jwtTokenProvider.createToken(savedUser)).willReturn("jwt-token");

            // when
            String token = userService.createUser(request, portalInfo);

            // then
            assertThat(token).isEqualTo("jwt-token");

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User toSave = userCaptor.getValue();

            // 저장은 portalInfo 기준이 가장 안전함(서비스도 그렇게 구현하길 추천)
            assertThat(toSave.getStudentId()).isEqualTo("21111111");
            assertThat(toSave.getName()).isEqualTo("홍길동");
            assertThat(toSave.getDepartment()).isEqualTo("컴퓨터공학과");
            assertThat(toSave.getNickname()).isEqualTo("새로 생성된 닉네임");

            verify(jwtTokenProvider).createToken(savedUser);
        }
    }

    @Nested
    @DisplayName("회원 탈퇴 테스트")
    class DeleteUserTest {

        private AuthUser authUser;
        private User user;
        private Place place;
        private Review review;

        @BeforeEach
        void setUp() {
            authUser = new AuthUser("21011111", Role.USER);
            user = User.builder()
                    .studentId("21011111")
                    .nickname("테스트유저")
                    .build();
            ReflectionTestUtils.setField(user, "id", 1L);

            place = Place.builder()
                    .name("테스트장소")
                    .address("주소")
                    .build();
            ReflectionTestUtils.setField(place, "id", 1L);

            review = Review.createReview(place, user, 5, "테스트 리뷰");
            ReflectionTestUtils.setField(review, "id", 101L);
            review.addImage("s3_image_url_1");
        }

        @Test
        @DisplayName("회원 탈퇴가 정상적으로 처리된다")
        void deleteUser_success() {
            // given
            given(userRepository.findByStudentId("21011111")).willReturn(Optional.of(user));
            given(reviewRepository.findAllByUserOrderByCreatedAtDesc(user)).willReturn(List.of(review));

            // when
            userService.deleteUser(authUser);

            // then
            verify(s3Service, times(1)).deleteImages(anyList());
            verify(reviewRepository, times(1)).delete(review);
            verify(reviewLikeRepository, times(1)).deleteAllByUser(user);
            verify(userRepository, times(1)).delete(user);
        }

        @Test
        @DisplayName("존재하지 않는 사용자이면 예외를 던진다")
        void deleteUser_userNotFound() {
            // given
            given(userRepository.findByStudentId("99999999")).willReturn(Optional.empty());
            AuthUser nonExistentUser = new AuthUser("99999999", Role.USER);

            // then
            assertThatThrownBy(() -> userService.deleteUser(nonExistentUser))
                    .isInstanceOf(SejongLifeException.class)
                    .hasMessage(ErrorCode.USER_NOT_FOUND.getErrorMessage());

            verify(reviewRepository, never()).findAllByUserOrderByCreatedAtDesc(any());
            verify(userRepository, never()).delete(any());
        }

        @Test
        @DisplayName("리뷰가 없는 사용자도 탈퇴가 가능하다")
        void deleteUser_noReviews() {
            // given
            given(userRepository.findByStudentId("21011111")).willReturn(Optional.of(user));
            given(reviewRepository.findAllByUserOrderByCreatedAtDesc(user)).willReturn(List.of());

            // when
            userService.deleteUser(authUser);

            // then
            verify(s3Service, never()).deleteImages(anyList());
            verify(reviewRepository, never()).delete(any());
            verify(reviewLikeRepository, times(1)).deleteAllByUser(user);
            verify(userRepository, times(1)).delete(user);
        }
    }
}
