package org.example.sejonglifebe.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "로그인 응답")
public class LoginResponse {

    @Schema(description = "신규 회원 여부", example = "false")
    private final boolean isNewUser;

    @Schema(description = "신규 회원 기본 정보")
    private final UserInfo userInfo;

    @Builder
    @Schema(description = "사용자 기본 정보")
    public static class UserInfo {
        @Schema(description = "학번", example = "20231234")
        private String studentId;

        @Schema(description = "이름", example = "홍길동")
        private String name;

        public static UserInfo from(PortalStudentInfo portalInfo) {
            return new UserInfo(portalInfo.studentId(), portalInfo.studentName());
        }
    }

    /**
     * 기존 회원일 때 : 로그인 성공
     */
    public static LoginResponse loginSuccess() {
        return LoginResponse.builder()
                .isNewUser(false)
                .build();
    }

    /**
     * 신규 회원일 때 : 회원가입 필요, 포털에서 가져온 기본 정보 제공
     */
    public static LoginResponse signUpRequired(PortalStudentInfo portalInfo) {
        return LoginResponse.builder()
                .isNewUser(true)
                .userInfo(UserInfo.from(portalInfo))
                .build();
    }
}
