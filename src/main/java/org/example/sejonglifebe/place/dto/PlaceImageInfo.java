package org.example.sejonglifebe.place.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.sejonglifebe.place.entity.PlaceImage;

@Schema(description = "장소 이미지 정보")
public record PlaceImageInfo(
        @Schema(description = "이미지 ID", example = "2")
        Long imageId,

        @Schema(description = "이미지 URL", example = "https://.../image.jpg")
        String url
) {

    public static PlaceImageInfo from(PlaceImage placeImage) {
        return new PlaceImageInfo(placeImage.getId(), placeImage.getUrl());
    }
}
