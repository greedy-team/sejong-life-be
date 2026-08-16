package org.example.sejonglifebe.place.dto;

import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

class PlaceSearchConditionsTest {

    @Test
    @DisplayName("DISTANCE 정렬인데 좌표(lat/lng)가 없으면 예외를 던진다")
    void distanceSort_withoutCoordinates_throws() {
        assertThatThrownBy(() ->
                new PlaceSearchConditions(List.of(), "전체", null, false, PlaceSortType.DISTANCE, null, null))
                .isInstanceOf(SejongLifeException.class)
                .hasMessage(ErrorCode.DISTANCE_SORT_REQUIRES_LOCATION.getErrorMessage());
    }

    @Test
    @DisplayName("DISTANCE 정렬이고 좌표가 있으면 정상 생성된다")
    void distanceSort_withCoordinates_ok() {
        assertThatCode(() ->
                new PlaceSearchConditions(List.of(), "전체", null, false, PlaceSortType.DISTANCE, 37.55, 127.07))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("DISTANCE가 아니면 좌표가 없어도 정상 생성된다")
    void nonDistanceSort_withoutCoordinates_ok() {
        assertThatCode(() ->
                new PlaceSearchConditions(List.of(), "전체", null, false, PlaceSortType.REVIEW_COUNT, null, null))
                .doesNotThrowAnyException();
    }
}
