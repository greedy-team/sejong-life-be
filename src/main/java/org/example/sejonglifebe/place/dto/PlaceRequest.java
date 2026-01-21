package org.example.sejonglifebe.place.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.example.sejonglifebe.common.dto.CategoryInfo;
import org.example.sejonglifebe.common.dto.TagInfo;
import org.example.sejonglifebe.place.entity.MapLinks;

@Schema(description = "장소 추가 요청")
public record PlaceRequest(

        @Schema(description = "장소명", example = "세종 카페 101")
        @NotBlank(message = "장소명은 필수 항목입니다.")
        String placeName,

        @Schema(description = "주소", example = "서울특별시 강동구 천호대로 123")
        @NotBlank(message = "주소는 필수 항목입니다.")
        String address,

        @Schema(description = "카테고리 목록")
        @NotEmpty(message = "카테고리는 최소 1개 이상 선택해야 합니다.")
        @Valid
        List<CategoryInfo> categories,

        @Schema(description = "태그 목록")
        @NotEmpty(message = "태그는 최소 1개 이상 선택해야 합니다.")
        @Valid
        List<TagInfo> tags,

        @Schema(description = "지도 링크(카카오/네이버/구글 등)")
        @NotNull(message = "지도 링크는 필수 항목입니다.")
        @Valid
        MapLinks mapLinks,

        @Schema(description = "제휴 여부")
        boolean isPartnership,

        @Schema(description = "제휴 내용")
        @NotNull(message = "제휴 장소인 경우 제휴 내용은 필수입니다.")
        String partnershipContent
) {
}
