package org.example.sejonglifebe.user;

import org.example.sejonglifebe.auth.PortalStudentInfo;
import org.example.sejonglifebe.common.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("회원가입이 정상적으로 된다")
    void shouldSignupSuccessfully_whenRequestIsValid() throws Exception {
        //given
        given(jwtTokenProvider.validateAndGetPortalInfo(anyString()))
                .willReturn(PortalStudentInfo.builder().studentId("21011111").name("새로운 사용자").build());

        mockMvc.perform(post("/api/users/signup")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentId": "21011111",
                                  "name": "새로운 사용자",
                                  "nickname": "닉네임"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("회원가입 및 로그인 성공"));
    }

    @Test
    @DisplayName("사용자의 정보가 일치하지 않으면 예외를 던진다")
    void shouldThrowException_whenUserInfoDoesNotMatchToken() throws Exception {
        //given
        given(jwtTokenProvider.validateAndGetPortalInfo(anyString()))
                .willReturn(PortalStudentInfo.builder().studentId("99999999").name("일치하지 않는 사용자").build());

        mockMvc.perform(post("/api/users/signup")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentId": "21011111",
                                  "name": "새로운 사용자",
                                  "nickname": "닉네임"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("인증에 실패했습니다."));
    }

    @Test
    @DisplayName("유효하지 않은 토큰이면 예외를 던진다")
    void shouldThrowException_whenAuthorizationHeaderIsInvalid() throws Exception {
        //given
        given(jwtTokenProvider.validateAndGetPortalInfo(anyString()))
                .willReturn(PortalStudentInfo.builder().studentId("21011111").name("새로운 사용자").build());

        mockMvc.perform(post("/api/users/signup")
                        .header("Authorization", "유효하지 않은 토큰")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentId": "21011111",
                                  "name": "새로운 사용자",
                                  "nickname": "닉네임"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("유효하지 않은 Authorization 헤더입니다."));
    }
}
