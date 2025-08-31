package org.example.sejonglifebe.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "별점-개수 집계")
public record RatingCount(
        @Schema(description = "별점", example = "5") int rating,
        @Schema(description = "개수", example = "12") Long count
) {

}
