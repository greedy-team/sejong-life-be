package org.example.sejonglifebe.meeting.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.sejonglifebe.meeting.entity.FaceType;
import org.example.sejonglifebe.meeting.entity.Gender;

public record MeetingSignUpRequest(
        @NotNull(message = "성별은 필수입니다.")
        Gender gender,

        @NotNull(message = "얼굴상은 필수입니다.")
        FaceType faceType,

        @NotNull(message = "출생년도는 필수입니다.")
        Integer birthYear,

        @NotBlank(message = "취미는 필수입니다.")
        @Size(max = 20, message = "취미는 20자 이내로 입력해주세요.")
        String hobby,

        @NotBlank(message = "하고싶은 데이트는 필수입니다.")
        @Size(max = 30, message = "원하는 데이트 스타일은 30자 이내로 입력해주세요.")
        String dateStyle,

        @NotBlank(message = "연락처는 필수입니다.")
        String contact
) {
}
