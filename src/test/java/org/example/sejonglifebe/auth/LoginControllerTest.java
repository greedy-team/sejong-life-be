package org.example.sejonglifebe.auth;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import org.example.sejonglifebe.auth.dto.LoginResponse;
import org.example.sejonglifebe.common.jwt.JwtTokenProvider;
import org.example.sejonglifebe.user.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private LoginService loginService;

    @Test
    @DisplayName("관리자 권한 확인 성공 - ADMIN이면 200 OK 반환")
    void checkAdmin_success_whenAdmin() throws Exception {
        // given
        given(jwtTokenProvider.validateAndGetAuthUser(anyString()))
                .willReturn(new AuthUser("21011111", Role.ADMIN));

        // when & then
        mockMvc.perform(get("/api/auth/admin")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("관리자 권한 확인 성공"))
                .andDo(print());
    }

    @Test
    @DisplayName("관리자 권한 확인 실패 - USER면 403 Forbidden 반환")
    void checkAdmin_fail_whenUserRole() throws Exception {
        // given
        given(jwtTokenProvider.validateAndGetAuthUser(anyString()))
                .willReturn(new AuthUser("21011111", Role.USER));

        // when & then
        mockMvc.perform(get("/api/auth/admin")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Test
    @DisplayName("관리자 권한 확인 실패 - 토큰이 없으면 401 Unauthorized 반환")
    void checkAdmin_fail_whenNoToken() throws Exception {
        mockMvc.perform(get("/api/auth/admin")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("재발급 성공 - refresh token 쿠키가 있으면 새 access/refresh token을 발급하고 쿠키를 갱신한다")
    void reissue_success_whenRefreshTokenCookiePresent() throws Exception {
        // given
        given(loginService.reissue("old-refresh"))
                .willReturn(LoginResponse.loginSuccessWithRefreshToken("new-access", "new-refresh"));

        // when & then
        mockMvc.perform(post("/api/auth/reissue")
                        .cookie(new Cookie("refreshToken", "old-refresh")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("new-access"))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("refreshToken=new-refresh")))
                .andDo(print());
    }

    @Test
    @DisplayName("재발급 실패 - refresh token 쿠키가 없으면 401 Unauthorized 반환")
    void reissue_fail_whenNoRefreshTokenCookie() throws Exception {
        mockMvc.perform(post("/api/auth/reissue"))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("로그아웃 성공 - 인증된 사용자면 refresh token을 삭제하고 쿠키를 만료시킨다")
    void logout_success_whenAuthenticated() throws Exception {
        // given
        given(jwtTokenProvider.validateAndGetAuthUser(anyString()))
                .willReturn(new AuthUser("21011111", Role.USER));

        // when & then
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("로그아웃 성공"))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("Max-Age=0")))
                .andDo(print());
    }

    @Test
    @DisplayName("로그아웃 실패 - 토큰이 없으면 401 Unauthorized 반환")
    void logout_fail_whenNoToken() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }
}
