package org.example.sejonglifebe.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Schema(description = "사용자 응답")
public class UserResponse {

    @Schema(description = "학번", example = "20231234") private String studentId;
    @Schema(description = "닉네임", example = "세종대왕") private String nickname;

    @Schema(description = "가입일시", example = "2025-08-24T12:34:56")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime createdAt;

    public static UserResponse fromEntity(org.example.sejonglifebe.user.User user) {
        return new UserResponse(
                user.getStudentId(),
                user.getNickname(),
                user.getCreatedAt()
        );
    }
}
