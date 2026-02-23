package org.example.sejonglifebe.place;

import org.example.sejonglifebe.category.Category;
import org.example.sejonglifebe.place.dto.PlaceQueryResult;
import org.example.sejonglifebe.tag.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PlaceRepositoryCustom {

    Page<PlaceQueryResult> getPlacesByConditions(
            Category category,
            List<Tag> tags,
            String keyword,
            Pageable pageable
    );
}
