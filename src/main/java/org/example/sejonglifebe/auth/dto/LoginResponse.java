package org.example.sejonglifebe.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.example.sejonglifebe.auth.PortalStudentInfo;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {

    private final boolean isNewUser;
    private final String accessToken; // 기존 회원용 JWT
    private final String signUpToken; // 신규 회원용 임시 토큰
    private final UserInfo userInfo; // 신규 회원용 기본 정보

    @Getter
    @Builder
    public static class UserInfo {
        private String studentId;
        private String name;

        public static UserInfo from(PortalStudentInfo portalInfo) {
            return UserInfo.builder()
                    .studentId(portalInfo.getStudentId())
                    .name(portalInfo.getName())
                    .build();
        }
    }

    // 기존 회원일 때 : 로그인 성공
    public static LoginResponse loginSuccess(String accessToken) {
        return LoginResponse.builder()
                .isNewUser(false)
                .accessToken(accessToken)
                .build();
    }

    // 신규 회원일 때 : 회원가입 필요
    public static LoginResponse signUpRequired(String signUpToken, PortalStudentInfo portalInfo) {
        return LoginResponse.builder()
                .isNewUser(true)
                .signUpToken(signUpToken)
                .userInfo(UserInfo.from(portalInfo))
                .build();
    }
}
