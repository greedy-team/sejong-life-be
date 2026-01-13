package org.example.sejonglifebe.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.sejonglifebe.common.dto.CommonResponse;
import org.example.sejonglifebe.user.dto.SignUpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "User", description = "회원")
public interface UserControllerSwagger {

    @Operation(summary = "회원가입")
    ResponseEntity<CommonResponse<String>> signup(
            @RequestHeader("Authorization") String signUpToken,
            @Valid @RequestBody SignUpRequest request);
}
