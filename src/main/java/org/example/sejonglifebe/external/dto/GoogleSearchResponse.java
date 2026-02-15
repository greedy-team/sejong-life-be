package org.example.sejonglifebe.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GoogleSearchResponse(
        List<Place> places
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Place(
            String id,                 // placeId
            DisplayName displayName    // canonical name
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DisplayName(
            String text
    ) {}
}
