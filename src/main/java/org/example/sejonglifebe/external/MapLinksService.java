package org.example.sejonglifebe.external;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.external.client.GooglePlacesClient;
import org.example.sejonglifebe.external.client.KakaoPlacesClient;
import org.example.sejonglifebe.external.client.NaverPlacesClient;
import org.example.sejonglifebe.external.dto.MapLinksRequest;
import org.example.sejonglifebe.external.dto.MapLinksResponse;
import org.example.sejonglifebe.external.dto.PlaceSearchResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class MapLinksService {

    private final NaverPlacesClient naverClient;
    private final GooglePlacesClient googleClient;
    private final KakaoPlacesClient kakaoClient;

    public MapLinksResponse buildUrl(MapLinksRequest request) {

        String kakaoUrl = "https://map.kakao.com/link/map/" + request.kakaoId();

        String naverQuery = "세종대학교 " + request.name();

        String naverCanonical = naverClient
                .findCanonicalName(naverQuery)
                .orElse(request.name());

        String encodedNaver = UriUtils.encode(naverCanonical, StandardCharsets.UTF_8);
        String naverUrl = "https://map.naver.com/v5/search/세종대학교 " + encodedNaver;

        String googleUrl = googleClient
                .findPlaceId(request.name())
                .map(id -> "https://www.google.com/maps/place/?q=place_id:" + id)
                .orElseGet(() -> {
                    String encoded = UriUtils.encode(request.name(), StandardCharsets.UTF_8);
                    return "https://www.google.com/maps/search/?api=1&query=" + encoded;
                });

        return new MapLinksResponse(kakaoUrl, naverUrl, googleUrl);
    }

    public List<PlaceSearchResponse> search(String query) {
        return kakaoClient.search(query);
    }
}
