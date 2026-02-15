package org.example.sejonglifebe.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoSearchResponse(
        List<Document> documents
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Document(
            String id,               // placeId
            String place_name,       // canonical name
            String road_address_name,
            String address_name,
            String x,                // lng
            String y                 // lat
    ) {}
}
