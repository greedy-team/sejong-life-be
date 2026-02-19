package org.example.sejonglifebe.place.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.sejonglifebe.common.dto.CategoryInfo;
import org.example.sejonglifebe.common.dto.TagInfo;
import org.example.sejonglifebe.place.entity.Place;

@Schema(description = "장소 목록 아이템")
public record PlaceResponse(
        @Schema(description = "장소 ID", example = "101") Long placeId,
        @Schema(description = "장소명", example = "세종 카페 101") String placeName,
        @Schema(description = "썸네일 이미지 URL", example = "https://.../thumb.jpg") String mainImageUrl,
        @Schema(description = "조회수", example = "12345") Long viewCount,
        @Schema(description = "리뷰 수", example = "12") Long reviewCount,
        @Schema(description = "카테고리 목록") List<CategoryInfo> categories,
        @Schema(description = "태그 목록") List<TagInfo> tags,
        @Schema(description = "제휴 여부") boolean isPartnership,
        @Schema(description = "제휴 내용") String partnershipContent
) {

    public static PlaceResponse from(Place place) {
        return new PlaceResponse(
                place.getId(),
                place.getName(),
                place.getThumbnailImage(),
                place.getViewCount(),
                (long) place.getReviews().size(),
                place.getPlaceCategories().stream()
                        .map(pc -> new CategoryInfo(pc.getCategory().getId(), pc.getCategory().getName()))
                        .toList(),
                place.getPlaceTags().stream()
                        .map(pt -> new TagInfo(pt.getTag().getId(), pt.getTag().getName()))
                        .toList(),
                place.isPartnership(),
                place.getPartnershipContent()
        );
    }

    public static PlaceResponse from(PlaceQueryResult result) {
        Place place = result.place();
        return new PlaceResponse(
                place.getId(),
                place.getName(),
                place.getThumbnailImage(),
                place.getViewCount(),
                result.reviewCount(),
                place.getPlaceCategories().stream()
                        .map(pc -> new CategoryInfo(pc.getCategory().getId(), pc.getCategory().getName()))
                        .toList(),
                place.getPlaceTags().stream()
                        .map(pt -> new TagInfo(pt.getTag().getId(), pt.getTag().getName()))
                        .toList(),
                place.isPartnership(),
                place.getPartnershipContent()
        );
    }
}
