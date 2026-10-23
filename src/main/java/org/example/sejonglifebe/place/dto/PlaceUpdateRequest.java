package org.example.sejonglifebe.place.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.example.sejonglifebe.place.entity.MapLinks;

import java.util.List;

public record PlaceUpdateRequest(
        @Schema(description = "장소명", example = "세종대학교")
        @NotBlank(message = "장소명은 필수 항목입니다.")
        String placeName,

        @Schema(description = "주소", example = "서울특별시 광진구 능동로 209")
        @NotBlank(message = "주소는 필수 항목입니다.")
        String address,

        @Schema(description = "위도. 경도와 함께 전달하며 좌표가 없는 장소는 둘 다 null", example = "37.5506")
        @DecimalMin("-90")
        @DecimalMax("90")
        Double latitude,

        @Schema(description = "경도. 위도와 함께 전달하며 좌표가 없는 장소는 둘 다 null", example = "127.0742")
        @DecimalMin("-180")
        @DecimalMax("180")
        Double longitude,

        @Schema(description = "카테고리 id 목록", example = "[1]")
        @NotEmpty(message = "카테고리는 최소 1개 이상 선택해야 합니다.")
        @Valid
        List<Long> categoryIds,

        @Schema(description = "태그 id 목록", example = "[1,2,3]")
        @NotEmpty(message = "태그는 최소 1개 이상 선택해야 합니다.")
        @Valid
        List<Long> tagIds,

        @Schema(description = "지도 링크(카카오/네이버/구글 등)",
                example =
                        """
                        {
                            "naverMap": "",
                            "kakaoMap": "",
                            "googleMap": ""
                        }
                        """)
        @NotNull(message = "지도 링크는 필수 항목입니다.")
        @Valid
        MapLinks mapLinks,

        @Schema(description = "제휴 여부", example = "true")
        boolean isPartnership,

        @Schema(description = "제휴 내용", example = "재학생 5% 할인")
        @NotNull(message = "제휴 장소인 경우 제휴 내용은 필수입니다.")
        String partnershipContent
) {
}
