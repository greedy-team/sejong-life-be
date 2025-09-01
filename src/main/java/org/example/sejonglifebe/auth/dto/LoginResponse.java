package org.example.sejonglifebe.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.example.sejonglifebe.auth.PortalStudentInfo;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "로그인 응답")
public class LoginResponse {

    @Schema(description = "신규 회원 여부", example = "false")
    private final boolean isNewUser;

    @Schema(description = "기존 회원용 접근 토큰(JWT)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private final String accessToken;

    @Schema(description = "신규 회원용 가입 토큰(임시)", example = "eyJhbGciOiJIUzI1NiJ9...")
    private final String signUpToken;

    @Schema(description = "신규 회원 기본 정보")
    private final UserInfo userInfo;

    @Getter
    @Builder
    @Schema(description = "사용자 기본 정보")
    public static class UserInfo {
        @Schema(description = "학번", example = "20231234")
        private String studentId;

        @Schema(description = "이름", example = "홍길동")
        private String name;

        public static UserInfo from(PortalStudentInfo portalInfo) {
            return UserInfo.builder()
                    .studentId(portalInfo.getStudentId())
                    .name(portalInfo.getName())
                    .build();
        }
    }

    /**
     * 기존 회원일 때 : 로그인 성공, access token 발급
     */
    public static LoginResponse loginSuccess(String accessToken) {
        return LoginResponse.builder()
                .isNewUser(false)
                .accessToken(accessToken)
                .build();
    }

    /**
     * 신규 회원일 때 : 회원가입 필요, sign-up token 발급, 포털에서 가져온 기본 정보 제공
     */
    public static LoginResponse signUpRequired(String signUpToken, PortalStudentInfo portalInfo) {
        return LoginResponse.builder()
                .isNewUser(true)
                .signUpToken(signUpToken)
                .userInfo(UserInfo.from(portalInfo))
                .build();
    }
}
