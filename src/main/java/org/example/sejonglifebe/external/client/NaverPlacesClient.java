package org.example.sejonglifebe.external.client;

import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.external.config.ExternalApiProperties;
import org.example.sejonglifebe.external.dto.NaverSearchResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class NaverPlacesClient {

    private final WebClient naverWebClient;
    private final ExternalApiProperties props;

    public Optional<String> findCanonicalName(String rawPlaceName) {

        String query = "세종대학교 " + rawPlaceName;

        NaverSearchResponse response = naverWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/search/local.json")
                        .queryParam("query", query)
                        .queryParam("display", 5)
                        .build())
                .header("X-Naver-Client-Id", props.naver().clientId())
                .header("X-Naver-Client-Secret", props.naver().clientSecret())
                .retrieve()
                .bodyToMono(NaverSearchResponse.class)
                .block();

        if (response == null || response.items() == null || response.items().isEmpty()) {
            return Optional.empty();
        }

        String title = response.items().get(0).title();

        String clean = title.replaceAll("<[^>]*>", "");

        return Optional.of(clean);
    }
}
