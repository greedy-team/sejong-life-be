package org.example.sejonglifebe.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.sejonglifebe.auth.PortalStudentInfo;
import org.example.sejonglifebe.common.jwt.JwtTokenProvider;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.user.dto.SignUpRequest;
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

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("회원가입이 정상적으로 된다")
    void shouldSignupSuccessfully_whenRequestIsValid() throws Exception {
        //given
        given(jwtTokenProvider.validateAndGetPortalInfo(anyString()))
                .willReturn(PortalStudentInfo.builder().studentId("21011111").name("새로운 사용자").build());
        SignUpRequest request = SignUpRequest.builder()
                .studentId("21011111")
                .name("새로운 사용자")
                .nickname("닉네임")
                .build();

        //then
        mockMvc.perform(post("/api/users/signup")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("회원가입 및 로그인 성공"));
    }

    @Test
    @DisplayName("사용자의 정보가 일치하지 않으면 예외를 던진다")
    void shouldThrowException_whenUserInfoDoesNotMatchToken() throws Exception {
        //given
        given(jwtTokenProvider.validateAndGetPortalInfo(anyString()))
                .willReturn(PortalStudentInfo.builder().studentId("99999999").name("일치하지 않는 사용자").build());
        SignUpRequest request = SignUpRequest.builder()
                .studentId("21011111")
                .name("새로운 사용자")
                .nickname("닉네임")
                .build();

        //then
        mockMvc.perform(post("/api/users/signup")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("유효하지 않은 토큰입니다."));
    }

    @Test
    @DisplayName("유효하지 않은 토큰이면 예외를 던진다")
    void shouldThrowException_whenAuthorizationHeaderIsInvalid() throws Exception {
        //given
        SignUpRequest request = SignUpRequest.builder()
                .studentId("21011111")
                .name("새로운 사용자")
                .nickname("닉네임")
                .build();

        //then
        mockMvc.perform(post("/api/users/signup")
                        .header("Authorization", "유효하지 않은 토큰")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("유효하지 않은 Authorization 헤더입니다."));
    }

    @Test
    @DisplayName("사용자가 잘못된 형식의 닉네임을 입력하면 예외를 반환한다")
    void shouldThrowException_whenNicknameIsInvalid() throws Exception {
        //given
        SignUpRequest request = SignUpRequest.builder()
                .studentId("21011111")
                .name("새로운 사용자")
                .nickname("잘못된닉네임!@#")
                .build();

        //then
        mockMvc.perform(post("/api/users/signup")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("닉네임은 한글, 영문, 숫자만 사용 가능합니다."));
    }

    @Test
    @DisplayName("1자 이하의 닉네임을 입력하면 예외를 던진다")
    void shouldThrowException_whenNicknameIsTooShort() throws Exception {
        //given
        SignUpRequest request = SignUpRequest.builder()
                .studentId("21011111")
                .name("새로운 사용자")
                .nickname("a")
                .build();

        //tehn
        mockMvc.perform(post("/api/users/signup")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("닉네임은 2자 이상 10자 이하로 입력해주세요."));
    }

    @Test
    @DisplayName("11자 이상의 닉네임을 입력하면 예외를 던진다")
    void shouldThrowException_whenNicknameIsTooLong() throws Exception {
        //given
        SignUpRequest request = SignUpRequest.builder()
                .studentId("21011111")
                .name("새로운 사용자")
                .nickname("길이가11자인닉네임임")
                .build();

        //tehn
        mockMvc.perform(post("/api/users/signup")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("닉네임은 2자 이상 10자 이하로 입력해주세요."));
    }

    @Test
    @DisplayName("헤더가 누락되면 예외를 던진다")
    void shouldThrowException_whenAuthorizationHeaderIsMissing() throws Exception {
        //given
        SignUpRequest request = SignUpRequest.builder()
                .studentId("21011111")
                .name("새로운 사용자")
                .nickname("닉네임")
                .build();

        //tehn
        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_AUTH_HEADER.getErrorMessage()));
    }
}
