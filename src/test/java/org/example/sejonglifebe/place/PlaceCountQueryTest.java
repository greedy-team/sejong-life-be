package org.example.sejonglifebe.place;

import org.example.sejonglifebe.category.Category;
import org.example.sejonglifebe.category.CategoryRepository;
import org.example.sejonglifebe.place.dto.PlaceQueryResult;
import org.example.sejonglifebe.place.dto.PlaceSearchQuery;
import org.example.sejonglifebe.place.entity.Place;
import org.example.sejonglifebe.tag.Tag;
import org.example.sejonglifebe.tag.TagRepository;
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
class PlaceCountQueryTest {

    @Autowired private PlaceRepository placeRepository;
    @Autowired private TagRepository tagRepository;
    @Autowired private CategoryRepository categoryRepository;

    private Tag tagA;
    private Tag tagB;

    @BeforeEach
    void setUp() {
        placeRepository.deleteAll();
        tagRepository.deleteAll();
        categoryRepository.deleteAll();

        tagA = tagRepository.save(new Tag("가성비"));
        tagB = tagRepository.save(new Tag("분위기"));

        // A+B 둘 다 가진 장소 2개 (필터 A,B 시 매칭돼야 함)
        savePlace("둘다1", tagA, tagB);
        savePlace("둘다2", tagA, tagB);
        // A만 가진 장소 3개 (필터 A,B 시 제외돼야 함)
        savePlace("A만1", tagA);
        savePlace("A만2", tagA);
        savePlace("A만3", tagA);
    }

    private void savePlace(String name, Tag... tags) {
        Place place = Place.createPlace(name, "주소", null, null, null, false, "");
        for (Tag tag : tags) {
            place.addTag(tag);
        }
        placeRepository.save(place);
    }

    @Test
    @DisplayName("태그 여러 개(AND)로 필터 시 totalElements가 전체 일치 장소 수와 일치한다")
    void totalElements_matchesAllTagFilteredCount() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<PlaceQueryResult> result = placeRepository.getPlacesByConditions(
                PlaceSearchQuery.builder().tags(List.of(tagA, tagB)).build(), pageable);

        // A+B 둘 다 가진 장소는 2개뿐. A만 가진 3개는 제외돼야 함.
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(1);
    }
}
