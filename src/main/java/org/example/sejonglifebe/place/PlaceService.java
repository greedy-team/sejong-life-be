package org.example.sejonglifebe.place;

import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.exception.PlaceNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlaceService {

    private final PlaceRepository placeRepository;

    /**
     * 장소 상세 조회
     */
    public PlaceDetailResponse getPlaceDetail(Long placeId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new PlaceNotFoundException("해당하는 장소 ID를 찾을 수 없습니다. id=" + placeId));

        return PlaceDetailResponse.from(place);
    }
}
