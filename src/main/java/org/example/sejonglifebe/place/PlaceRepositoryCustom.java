package org.example.sejonglifebe.place;

import org.example.sejonglifebe.place.dto.PlaceQueryResult;
import org.example.sejonglifebe.place.dto.PlaceSearchQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PlaceRepositoryCustom {

    Page<PlaceQueryResult> getPlacesByConditions(
            PlaceSearchQuery query,
            Pageable pageable
    );
}
