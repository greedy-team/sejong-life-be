package org.example.sejonglifebe.place;

import org.example.sejonglifebe.category.Category;
import org.example.sejonglifebe.place.entity.Place;
import org.example.sejonglifebe.tag.Tag;

import java.util.List;

public interface PlaceRepositoryCustom {

    List<Place> getPlacesByConditions(
            Category category,
            List<Tag> tags,
            String keyword
    );
}
