package org.example.sejonglifebe.auth;

import org.example.sejonglifebe.auth.dto.LoginResponse;
import org.example.sejonglifebe.common.jwt.JwtTokenProvider;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.example.sejonglifebe.user.User;
import org.example.sejonglifebe.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    UserService userService;

    @Mock
    JwtTokenProvider jwtTokenProvider;

    @Mock
    RefreshTokenService refreshTokenService;

    @Mock
    TokenIssuer tokenIssuer;

    @InjectMocks
    LoginService loginService;

    @Test
    @DisplayName("refresh token이 Redis에 저장된 값과 일치하면 access/refresh token을 재발급한다")
    void reissue_success_whenRefreshTokenMatches() {
        // given
        User user = User.builder().studentId("21011111").build();
        LoginResponse reissued = LoginResponse.loginSuccessWithRefreshToken("new-access", "new-refresh");

        given(jwtTokenProvider.validateRefreshToken("old-refresh")).willReturn("21011111");
        given(refreshTokenService.matches("21011111", "old-refresh")).willReturn(true);
        given(userService.findUserByStudentId("21011111")).willReturn(Optional.of(user));
        given(tokenIssuer.issue(user)).willReturn(reissued);

        // when
        LoginResponse response = loginService.reissue("old-refresh");

        // then
        assertThat(response.getAccessToken()).isEqualTo("new-access");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh");
        verify(refreshTokenService, never()).delete(any());
    }

    @Test
    @DisplayName("Redis에 저장된 값과 다른(이미 회전되어 폐기된) refresh token이면 세션을 무효화하고 예외를 던진다")
    void reissue_fail_whenRefreshTokenReused() {
        // given
        given(jwtTokenProvider.validateRefreshToken("stale-refresh")).willReturn("21011111");
        given(refreshTokenService.matches("21011111", "stale-refresh")).willReturn(false);

        // when & then
        assertThatThrownBy(() -> loginService.reissue("stale-refresh"))
                .isInstanceOf(SejongLifeException.class)
                .hasMessage(ErrorCode.INVALID_TOKEN.getErrorMessage());

        verify(refreshTokenService).delete("21011111");
        verify(tokenIssuer, never()).issue(any());
    }

    @Test
    @DisplayName("logout은 해당 studentId의 refresh token을 Redis에서 삭제한다")
    void logout_deletesRefreshToken() {
        // when
        loginService.logout("21011111");

        // then
        verify(refreshTokenService).delete("21011111");
    }
}
