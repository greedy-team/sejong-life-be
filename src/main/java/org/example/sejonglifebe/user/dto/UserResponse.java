package org.example.sejonglifebe.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UserResponse {

    private String studentId;
    private String nickname;
    private final LocalDateTime createdAt;

    public static UserResponse fromEntity(org.example.sejonglifebe.user.User user) {
        return new UserResponse(
                user.getStudentId(),
                user.getNickname(),
                user.getCreatedAt()
        );
    }
}
