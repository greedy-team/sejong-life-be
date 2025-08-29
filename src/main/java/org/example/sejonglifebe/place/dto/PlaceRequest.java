package org.example.sejonglifebe.place.dto;

import java.util.List;

public record PlaceRequest(
        List<String> tags,
        String category
) {

}
