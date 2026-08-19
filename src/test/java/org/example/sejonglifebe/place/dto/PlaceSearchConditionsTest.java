package org.example.sejonglifebe.place.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PlaceSearchConditionsTest {

    @Test
    @DisplayName("DISTANCE 정렬인데 좌표가 없으면 isLocationValidForDistanceSort가 false다")
    void distanceSort_withoutCoordinates_isInvalid() {
        PlaceSearchConditions conditions =
                new PlaceSearchConditions(List.of(), "전체", null, false, PlaceSortType.DISTANCE, null, null);

        assertThat(conditions.isLocationValidForDistanceSort()).isFalse();
    }

    @Test
    @DisplayName("DISTANCE 정렬이고 좌표가 있으면 isLocationValidForDistanceSort가 true다")
    void distanceSort_withCoordinates_isValid() {
        PlaceSearchConditions conditions =
                new PlaceSearchConditions(List.of(), "전체", null, false, PlaceSortType.DISTANCE, 37.55, 127.07);

        assertThat(conditions.isLocationValidForDistanceSort()).isTrue();
    }

    @Test
    @DisplayName("DISTANCE가 아니면 좌표가 없어도 isLocationValidForDistanceSort가 true다")
    void nonDistanceSort_withoutCoordinates_isValid() {
        PlaceSearchConditions conditions =
                new PlaceSearchConditions(List.of(), "전체", null, false, PlaceSortType.REVIEW_COUNT, null, null);

        assertThat(conditions.isLocationValidForDistanceSort()).isTrue();
    }

    @Test
    @DisplayName("sortType이 null이면 기본값 REVIEW_COUNT로 치환된다")
    void sortType_null_defaultsToReviewCount() {
        PlaceSearchConditions conditions =
                new PlaceSearchConditions(List.of(), "전체", null, false, null, null, null);

        assertThat(conditions.sortType()).isEqualTo(PlaceSortType.REVIEW_COUNT);
    }
}
