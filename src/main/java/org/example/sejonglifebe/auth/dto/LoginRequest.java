package org.example.sejonglifebe.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "로그인 요청")
public class LoginRequest {

    @Schema(description = "세종 포털 아이디", example = "20231234")
    @NotBlank(message = "아이디는 필수 입력 값입니다.")
    private String sejongPortalId;

    @Schema(description = "세종 포털 비밀번호", example = "p@ssw0rd!")
    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    private String sejongPortalPw;
}
