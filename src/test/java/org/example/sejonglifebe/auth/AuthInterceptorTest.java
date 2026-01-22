package org.example.sejonglifebe.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.sejonglifebe.common.jwt.JwtTokenExtractor;
import org.example.sejonglifebe.common.jwt.JwtTokenProvider;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.example.sejonglifebe.user.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.web.method.HandlerMethod;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class AuthInterceptorTest {

    @Mock
    private JwtTokenExtractor jwtTokenExtractor;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private AuthInterceptor authInterceptor;

    private static final String AUTH_HEADER = "Bearer test-token";
    private static final String TOKEN = "test-token";

    @Nested
    @DisplayName("@LoginRequired(role=USER) 테스트")
    class LoginRequiredDefaultTest {

        @Test
        @DisplayName("USER 사용자가 접근하면 성공한다")
        void user_access_success() throws Exception {
            // given
            HandlerMethod handlerMethod = mockHandlerMethodWithRole(Role.USER);
            AuthUser authUser = new AuthUser("21011111", Role.USER);

            given(request.getHeader(HttpHeaders.AUTHORIZATION)).willReturn(AUTH_HEADER);
            given(jwtTokenExtractor.extractToken(AUTH_HEADER)).willReturn(TOKEN);
            given(jwtTokenProvider.validateAndGetAuthUser(TOKEN)).willReturn(authUser);

            // when
            boolean result = authInterceptor.preHandle(request, response, handlerMethod);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("ADMIN 사용자가 접근하면 성공한다")
        void admin_access_to_user_api_success() throws Exception {
            // given
            HandlerMethod handlerMethod = mockHandlerMethodWithRole(Role.USER);
            AuthUser authUser = new AuthUser("21011111", Role.ADMIN);

            given(request.getHeader(HttpHeaders.AUTHORIZATION)).willReturn(AUTH_HEADER);
            given(jwtTokenExtractor.extractToken(AUTH_HEADER)).willReturn(TOKEN);
            given(jwtTokenProvider.validateAndGetAuthUser(TOKEN)).willReturn(authUser);

            // when
            boolean result = authInterceptor.preHandle(request, response, handlerMethod);

            // then
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("@LoginRequired(role=ADMIN) 테스트")
    class LoginRequiredAdminTest {

        @Test
        @DisplayName("ADMIN 사용자가 접근하면 성공한다")
        void admin_access_success() throws Exception {
            // given
            HandlerMethod handlerMethod = mockHandlerMethodWithRole(Role.ADMIN);
            AuthUser authUser = new AuthUser("21011111", Role.ADMIN);

            given(request.getHeader(HttpHeaders.AUTHORIZATION)).willReturn(AUTH_HEADER);
            given(jwtTokenExtractor.extractToken(AUTH_HEADER)).willReturn(TOKEN);
            given(jwtTokenProvider.validateAndGetAuthUser(TOKEN)).willReturn(authUser);

            // when
            boolean result = authInterceptor.preHandle(request, response, handlerMethod);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("USER 사용자가 접근하면 ACCESS_DENIED 예외가 발생한다")
        void user_access_to_admin_api_throws_exception() throws Exception {
            // given
            HandlerMethod handlerMethod = mockHandlerMethodWithRole(Role.ADMIN);
            AuthUser authUser = new AuthUser("21011111", Role.USER);

            given(request.getHeader(HttpHeaders.AUTHORIZATION)).willReturn(AUTH_HEADER);
            given(jwtTokenExtractor.extractToken(AUTH_HEADER)).willReturn(TOKEN);
            given(jwtTokenProvider.validateAndGetAuthUser(TOKEN)).willReturn(authUser);

            // when & then
            assertThatThrownBy(() -> authInterceptor.preHandle(request, response, handlerMethod))
                    .isInstanceOf(SejongLifeException.class)
                    .hasMessage(ErrorCode.ACCESS_DENIED.getErrorMessage());
        }
    }

    @Nested
    @DisplayName("@LoginRequired 없는 API 테스트")
    class NoLoginRequiredTest {

        @Test
        @DisplayName("@LoginRequired가 없으면 인증 없이 통과한다")
        void no_login_required_passes() throws Exception {
            // given
            HandlerMethod handlerMethod = mock(HandlerMethod.class);
            given(handlerMethod.getMethodAnnotation(LoginRequired.class)).willReturn(null);

            // when
            boolean result = authInterceptor.preHandle(request, response, handlerMethod);

            // then
            assertThat(result).isTrue();
        }
    }

    private HandlerMethod mockHandlerMethodWithRole(Role role) {
        HandlerMethod handlerMethod = mock(HandlerMethod.class);
        LoginRequired loginRequired = mock(LoginRequired.class);
        given(handlerMethod.getMethodAnnotation(LoginRequired.class)).willReturn(loginRequired);
        given(loginRequired.role()).willReturn(role);
        return handlerMethod;
    }
}
