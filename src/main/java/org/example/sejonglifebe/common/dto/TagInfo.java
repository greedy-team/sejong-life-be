package org.example.sejonglifebe.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "태그 정보")
public record TagInfo(
        @Schema(description = "태그 ID", example = "10") Long tagId,
        @Schema(description = "태그명", example = "혼밥하기 좋은") String tagName
) {

}
