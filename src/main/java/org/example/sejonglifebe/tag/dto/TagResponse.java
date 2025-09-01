package org.example.sejonglifebe.tag.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.sejonglifebe.tag.Tag;

@Schema(description = "태그 정보")
public record TagResponse(
        @Schema(description = "태그 ID", example = "10") Long tagId,
        @Schema(description = "태그명", example = "혼밥하기 좋은")String tagName
) {

    public static TagResponse from(Tag tag) {
        return new TagResponse(tag.getId(), tag.getName());
    }
}
