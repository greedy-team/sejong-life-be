package org.example.sejonglifebe.place.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.sejonglifebe.common.dto.CategoryInfo;
import org.example.sejonglifebe.common.dto.TagInfo;
import org.example.sejonglifebe.place.entity.MapLinks;
import org.example.sejonglifebe.place.entity.Place;

@Schema(description = "장소 상세 정보")
public record PlaceDetailResponse(
        @Schema(description = "장소 ID", example = "101") Long id,
        @Schema(description = "장소명", example = "보난자") String name,
        @Schema(description = "카테고리 목록") List<CategoryInfo> categories,
        @Schema(description = "이미지 정보 목록") List<PlaceImageInfo> images,
        @Schema(description = "태그 목록") List<TagInfo> tags,
        @Schema(description = "조회수", example = "12345") Long viewCount,
        @Schema(description = "지도 링크(카카오/네이버/구글 등)") MapLinks mapLinks,
        @Schema(description = "제휴 여부") boolean isPartnership,
        @Schema(description = "제휴 내용") String partnershipContent
) {

    public static PlaceDetailResponse from(Place place) {
        return new PlaceDetailResponse(
                place.getId(),
                place.getName(),
                place.getPlaceCategories().stream()
                        .map(pc -> new CategoryInfo(pc.getCategory().getId(), pc.getCategory().getName()))
                        .toList(),
                place.getPlaceImages().stream()
                        .map(PlaceImageInfo::from)
                        .toList(),
                place.getPlaceTags().stream()
                        .map(pt -> new TagInfo(pt.getTag().getId(), pt.getTag().getName()))
                        .toList(),
                place.getViewCount(),
                place.getMapLinks(),
                place.isPartnership(),
                place.getPartnershipContent()
        );
    }
}
