package org.example.sejonglifebe.place;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.auth.AuthUser;
import org.example.sejonglifebe.category.Category;
import org.example.sejonglifebe.category.CategoryRepository;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.example.sejonglifebe.place.dto.PlaceDetailResponse;
import org.example.sejonglifebe.place.dto.PlaceRequest;
import org.example.sejonglifebe.place.dto.PlaceResponse;
import org.example.sejonglifebe.place.entity.Place;
import org.example.sejonglifebe.place.view.PlaceViewLog;
import org.example.sejonglifebe.place.view.PlaceViewLogRepository;
import org.example.sejonglifebe.place.view.Viewer;
import org.example.sejonglifebe.place.view.ViewerKeyGenerator;
import org.example.sejonglifebe.tag.Tag;
import org.example.sejonglifebe.tag.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final TagRepository tagRepository;
    private final CategoryRepository categoryRepository;
    private final PlaceViewLogRepository placeViewLogRepository;
    private static final Duration VIEW_TIME_TO_LIVE = Duration.ofHours(6);

    public List<PlaceResponse> getPlacesFilteredByCategoryAndTags(PlaceRequest placeRequest) {
        List<String> tagNames = placeRequest.tags();
        String categoryName = placeRequest.category();

        if (tagNames == null) {
            tagNames = Collections.emptyList();
        }
        List<Tag> tags = tagRepository.findByNameIn(tagNames);
        if (tags.size() != tagNames.size()) {
            throw new SejongLifeException(ErrorCode.TAG_NOT_FOUND);
        }

        List<Place> places = new ArrayList<>();
        if (categoryName.equals("전체"))
            places = getPlacesByAllCategory(tags);

        Category category;
        if (!categoryName.equals("전체")) {
            category = categoryRepository
                    .findByName(categoryName)
                    .orElseThrow(() -> new SejongLifeException(ErrorCode.CATEGORY_NOT_FOUND));

            places = getPlacesBySelectedCategory(category, tags);
        }
        return places.stream().map(PlaceResponse::from).toList();
    }

    private List<Place> getPlacesByAllCategory(List<Tag> tags) {
        if (tags.isEmpty()) {
            return placeRepository.findAllOrderByReviewCountDesc();
        }
        return placeRepository.findByTags(tags, (long) tags.size());
    }

    private List<Place> getPlacesBySelectedCategory(Category category, List<Tag> tags) {
        if (tags.isEmpty()) {
            return placeRepository.findByCategory(category);
        }
        return placeRepository.findPlacesByTagsAndCategoryContainingAllTags(category, tags, (long)tags.size());
    }

    @Transactional
    public List<PlaceResponse> getWeeklyHotPlaces() {
        List<Place> hotPlaces = placeRepository.findTop10ByOrderByWeeklyViewCountDesc();
        return hotPlaces.stream()
                .map(PlaceResponse::from).toList();
    }

    @Transactional
    public PlaceDetailResponse getPlaceDetail(Long placeId, AuthUser authUser, HttpServletRequest request) {
        increaseViewCount(placeId, authUser, request);

        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new SejongLifeException(ErrorCode.PLACE_NOT_FOUND));

        return PlaceDetailResponse.from(place);
    }

    private void increaseViewCount(Long placeId, AuthUser authUser, HttpServletRequest request) {
        Viewer viewer = identifyViewer(authUser, request);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireBefore = now.minus(VIEW_TIME_TO_LIVE);

        Optional<PlaceViewLog> existingViewLog =
                placeViewLogRepository.findForUpdate(placeId, viewer.type(), viewer.key());

        if (existingViewLog.isEmpty()) {
            placeViewLogRepository.save(new PlaceViewLog(placeId, viewer.type(), viewer.key(), now));
            placeRepository.increaseViewCount(placeId);
            return;
        }

        PlaceViewLog viewLog = existingViewLog.get();
        if (!viewLog.getLastViewedAt().isAfter(expireBefore)) {
            viewLog.updateLastViewedAt(now);
            placeRepository.increaseViewCount(placeId);
        }
    }

    private Viewer identifyViewer(AuthUser authUser, HttpServletRequest request) {
        if (authUser != null && authUser.studentId() != null && !authUser.studentId().isBlank()) {
            return Viewer.user(authUser.studentId());
        }
        return Viewer.ipua(ViewerKeyGenerator.ipUaHash(request));
    }
}
