package org.example.sejonglifebe.external.client;

import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.external.config.ExternalApiProperties;
import org.example.sejonglifebe.external.dto.GoogleSearchResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GooglePlacesClient {

    private final WebClient googleWebClient;
    private final ExternalApiProperties props;

    private static final double SCHOOL_LATITUDE = 37.550638892935346;
    private static final double SCHOOL_LONGITUDE = 127.0742595815513;
    private static final int RADIUS_METERS = 1500;

    public Optional<String> findPlaceId(String name) {

        String body =
        """
        {
          "textQuery": "%s",
          "maxResultCount": 1,
          "locationBias": {
            "circle": {
              "center": { "latitude": %f, "longitude": %f },
              "radius": %d
            }
          }
        }
        """.formatted(escapeJson(name), SCHOOL_LATITUDE, SCHOOL_LONGITUDE, RADIUS_METERS);

        GoogleSearchResponse res = googleWebClient.post()
                .uri("/v1/places:searchText")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Goog-Api-Key", props.google().apiKey())
                .header("X-Goog-FieldMask", "places.id")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(GoogleSearchResponse.class)
                .block();

        if (res == null || res.places() == null || res.places().isEmpty()) {
            return Optional.empty();
        }

        String placeId = res.places().get(0).id();
        if (placeId == null || placeId.isBlank()) return Optional.empty();

        return Optional.of(placeId);
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
