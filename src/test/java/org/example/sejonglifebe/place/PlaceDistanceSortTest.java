package org.example.sejonglifebe.place;

import org.example.sejonglifebe.place.dto.PlaceQueryResult;
import org.example.sejonglifebe.place.dto.PlaceSearchQuery;
import org.example.sejonglifebe.place.dto.PlaceSortType;
import org.example.sejonglifebe.place.entity.Place;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PlaceDistanceSortTest {

    @Autowired
    private PlaceRepository placeRepository;

    // 기준점: 세종대학교 정문 부근
    private static final double USER_LATITUDE = 37.550838;
    private static final double USER_LONGITUDE = 127.074430;

    @BeforeEach
    void setUp() {
        placeRepository.deleteAll();
    }

    @Test
    @DisplayName("DISTANCE 정렬은 사용자 위치에서 가까운 순으로 장소를 반환한다")
    void sortByDistance_nearestFirst() {
        // 기준점에서: 가까움 → 중간 → 멈 순으로 좌표 배치
        Place near = savePlace("가까운곳", 37.550900, 127.074500);   // ~수십 m
        Place mid = savePlace("중간곳", 37.556000, 127.080000);      // ~수백 m
        Place far = savePlace("먼곳", 37.600000, 127.150000);        // ~수 km
        savePlace("좌표없는곳", null, null);

        Pageable pageable = PageRequest.of(0, 10);
        Page<PlaceQueryResult> result = placeRepository.getPlacesByConditions(
                PlaceSearchQuery.builder()
                        .tags(List.of())
                        .sort(PlaceSortType.DISTANCE)
                        .latitude(USER_LATITUDE)
                        .longitude(USER_LONGITUDE)
                        .build(),
                pageable);

        assertThat(result.getContent())
                .extracting(r -> r.place().getName())
                .containsExactly("가까운곳", "중간곳", "먼곳", "좌표없는곳");
    }

    private Place savePlace(String name, Double latitude, Double longitude) {
        Place place = Place.createPlace(name, "주소", latitude, longitude, null, false, "");
        return placeRepository.save(place);
    }
}
