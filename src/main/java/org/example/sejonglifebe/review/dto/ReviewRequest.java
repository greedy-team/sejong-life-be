package org.example.sejonglifebe.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record ReviewRequest(

        @Min(value = 1, message = "별점은 1 이상이어야 합니다.")
        @Max(value = 5, message = "별점은 5 이하여야 합니다.")
        int rating,

        @NotBlank(message = "리뷰 내용은 필수 항목입니다.")
        String content,
        List<Long> tagIds
) {

}
