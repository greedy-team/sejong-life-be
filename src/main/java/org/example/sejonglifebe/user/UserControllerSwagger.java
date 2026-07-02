package org.example.sejonglifebe.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.sejonglifebe.auth.AuthUser;
import org.example.sejonglifebe.common.dto.CommonResponse;
import org.example.sejonglifebe.user.dto.MyPageResponse;
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

    @Operation(summary = "회원 탈퇴", description = "회원 탈퇴 시 작성한 리뷰와 좋아요가 모두 삭제됩니다")
    ResponseEntity<CommonResponse<Void>> deleteUser(AuthUser authUser);

    @Operation(summary = "회원 마이페이지 정보 조회", description = "회원 마이페이지 정보를 불러옵니다")
    public ResponseEntity<CommonResponse<MyPageResponse>> getMyPageInfo(AuthUser authUser);
}
