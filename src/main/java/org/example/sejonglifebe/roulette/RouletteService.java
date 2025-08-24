package org.example.sejonglifebe.roulette;

import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.category.Category;
import org.example.sejonglifebe.category.CategoryRepository;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.example.sejonglifebe.place.PlaceRepository;
import org.example.sejonglifebe.place.entity.Place;
import org.example.sejonglifebe.roulette.dto.RouletteResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RouletteService {
    private final PlaceRepository placeRepository;
    private final CategoryRepository categoryRepository;

    public RouletteResponse getRoulettePlaces(String categoryName) {
        List<Place> places;

        if ("전체".equals(categoryName)) {
            places = placeRepository.findAll();
        } else {
            Category category = categoryRepository.findByName(categoryName)
                    .orElseThrow(() -> new SejongLifeException(ErrorCode.CATEGORY_NAME_NOT_FOUND));
            places = placeRepository.findByCategory(category);
        }

        return RouletteResponse.from(places);
    }
}
