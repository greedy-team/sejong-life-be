package org.example.sejonglifebe.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

public record PageResponse(
        @Schema(example = "0") int page,
        @Schema(example = "10") int size,
        @Schema(example = "50") long totalElements,
        @Schema(example = "5") int totalPages,
        @Schema(example = "true") boolean hasNext
) {
    public static PageResponse from(Page<?> page) {
        return new PageResponse(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }
}
