package org.example.sejonglifebe.external.client;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.external.config.ExternalApiProperties;
import org.example.sejonglifebe.external.dto.KakaoSearchResponse;
import org.example.sejonglifebe.external.dto.PlaceSearchResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class KakaoPlacesClient {

    private final WebClient kakaoWebClient;
    private final ExternalApiProperties props;

    public List<PlaceSearchResponse> search(String rawPlaceName) {
        KakaoSearchResponse response = kakaoWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v2/local/search/keyword.json")
                        .queryParam("query", rawPlaceName)
                        .queryParam("x", 127.0742595815513)
                        .queryParam("y", 37.550638892935346)
                        .queryParam("radius", 2000)
                        .queryParam("sort", "distance")
                        .queryParam("size", 10)
                        .build())
                .header("Authorization", "KakaoAK " + props.kakao().restApiKey())
                .retrieve()
                .bodyToMono(KakaoSearchResponse.class)
                .block();

        if (response == null || response.documents() == null)
            return List.of();

        return response.documents().stream()
                .map(d -> new PlaceSearchResponse(
                        d.id(),
                        d.place_name(),
                        d.road_address_name(),
                        parseDouble(d.y()),
                        parseDouble(d.x())
                ))
                .toList();
    }

    private Double parseDouble(String v) {
        if (v == null || v.isBlank()) return null;
        try {
            return Double.parseDouble(v);
        }
        catch (NumberFormatException e) {
            return null;
        }
    }
}
