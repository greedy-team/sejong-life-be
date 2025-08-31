package org.example.sejonglifebe.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "아이디는 필수 입력 값입니다.")
    private String sejongPortalId;

    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    private String sejongPortalPw;
}
