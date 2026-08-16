package org.example.sejonglifebe.place.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlaceSortTypeTest {

    @Test
    @DisplayName("orDefault: null이면 REVIEW_COUNT를 반환한다")
    void orDefault_null_returnsReviewCount() {
        assertThat(PlaceSortType.orDefault(null)).isEqualTo(PlaceSortType.REVIEW_COUNT);
    }

    @Test
    @DisplayName("orDefault: null이 아니면 그대로 반환한다")
    void orDefault_notNull_returnsSame() {
        assertThat(PlaceSortType.orDefault(PlaceSortType.RATING)).isEqualTo(PlaceSortType.RATING);
        assertThat(PlaceSortType.orDefault(PlaceSortType.VIEW_COUNT)).isEqualTo(PlaceSortType.VIEW_COUNT);
    }
}
