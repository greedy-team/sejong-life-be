package org.example.sejonglifebe.place.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.example.sejonglifebe.place.entity.MapLinks;

public record PlaceUpdateRequest(
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
        String partnershipContent)
{ }
