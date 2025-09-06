package org.example.sejonglifebe.user;

import org.example.sejonglifebe.common.jwt.JwtTokenProvider;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.example.sejonglifebe.user.dto.SignUpRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    UserService userService;

    @Nested
    @DisplayName("유저 생성 테스트")
    class createUserTest {

        @Test
        @DisplayName("중복된 닉네임이면 예외를 던진다")
        void givenDuplicateNickname_whenCreateUser_thenThrowException() {
            // given
            SignUpRequest request = SignUpRequest.builder()
                    .studentId("21111111")
                    .nickname("존재하는 닉네임")
                    .build();
            given(userRepository.existsByNickname("존재하는 닉네임")).willReturn(true);

            //then
            assertThatThrownBy(() -> userService.createUser(request))
                    .isInstanceOf(SejongLifeException.class)
                    .hasMessage(ErrorCode.DUPLICATE_NICKNAME.getErrorMessage());
        }

        @Test
        @DisplayName("유저가 정상적으로 생성된다")
        void gcreateUser_success() {
            // given
            SignUpRequest request = SignUpRequest.builder()
                    .studentId("21111111")
                    .nickname("새로 생성된 닉네임")
                    .build();
            User savedUser = User.builder()
                    .id(1L)
                    .studentId("21111111")
                    .nickname("새로 생성된 닉네임")
                    .build();

            given(userRepository.existsByNickname("새로 생성된 닉네임")).willReturn(false);
            given(userRepository.save(any(User.class))).willReturn(savedUser);
            given(jwtTokenProvider.createToken(savedUser)).willReturn("jwt-token");

            //when
            String token = userService.createUser(request);

            //then
            assertThat(token).isEqualTo("jwt-token");
            verify(userRepository).save(any(User.class));
            verify(jwtTokenProvider).createToken(savedUser);
        }
    }
}
