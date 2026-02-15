package org.example.sejonglifebe.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoSearchResponse(
        List<Document> documents
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Document(
            String id,
            String place_name,
            String road_address_name,
            String address_name,
            String x,
            String y
    ) {}
}
