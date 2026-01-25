package org.example.sejonglifebe.auth;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
}
