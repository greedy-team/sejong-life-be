package org.example.sejonglifebe.place;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.auth.AuthUser;
import org.example.sejonglifebe.category.Category;
import org.example.sejonglifebe.category.CategoryRepository;
import org.example.sejonglifebe.common.storage.ImageStorage;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.example.sejonglifebe.place.dto.PlaceDetailResponse;
import org.example.sejonglifebe.place.dto.PlaceRequest;
import org.example.sejonglifebe.place.dto.PlaceResponse;
import org.example.sejonglifebe.place.dto.PlaceSearchConditions;
import org.example.sejonglifebe.place.dto.PlaceSearchQuery;
import org.example.sejonglifebe.place.dto.PlaceUpdateRequest;
import org.example.sejonglifebe.place.entity.Place;
import org.example.sejonglifebe.place.entity.PlaceImage;
import org.example.sejonglifebe.place.view.PlaceViewService;
import org.example.sejonglifebe.place.view.Viewer;
import org.example.sejonglifebe.place.view.ViewerKeyGenerator;
import org.example.sejonglifebe.review.Review;
import org.example.sejonglifebe.tag.Tag;
import org.example.sejonglifebe.tag.TagRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final TagRepository tagRepository;
    private final CategoryRepository categoryRepository;
    private final ImageStorage imageStorage;
    private final PlaceViewService placeViewService;

    @Transactional(readOnly = true)
    public Page<PlaceResponse> getPlaceByConditions(PlaceSearchConditions conditions, Pageable pageable) {
        List<String> tagNames = conditions.tags();
        String categoryName = conditions.category();

        Category category = null;

        if (tagNames == null) {
            tagNames = Collections.emptyList();
        }

        List<Tag> tags = tagRepository.findByNameIn(tagNames);
        if (tags.size() != tagNames.size()) {
            throw new SejongLifeException(ErrorCode.TAG_NOT_FOUND);
        }

        if (!categoryName.equals("전체")) {
            category = categoryRepository
                    .findByName(categoryName)
                    .orElseThrow(() -> new SejongLifeException(ErrorCode.CATEGORY_NOT_FOUND));
        }

        PlaceSearchQuery query = PlaceSearchQuery.builder()
                .category(category)
                .tags(tags)
                .keyword(conditions.keyword())
                .partnershipOnly(conditions.partnershipOnly())
                .sort(conditions.sortType())
                .latitude(conditions.latitude())
                .longitude(conditions.longitude())
                .build();

        return placeRepository.getPlacesByConditions(query, pageable)
                .map(PlaceResponse::from);
    }

    @Transactional
    public void createPlace(PlaceRequest request, MultipartFile thumbnail, AuthUser authUser) {

        Place place = Place.createPlace(
                request.placeName(),
                request.address(),
                request.latitude(),
                request.longitude(),
                request.mapLinks(),
                request.isPartnership(),
                request.partnershipContent()
        );

        placeRepository.save(place);

        if (thumbnail != null && !thumbnail.isEmpty()) {
            String uploadedUrl = imageStorage.uploadImage(String.valueOf(place.getId()), thumbnail);
            place.addImage(uploadedUrl, true);
        }

        attachCategoriesToPlace(place, request);
        attachTagsToPlace(place, request);
    }

    @Transactional
    public void updatePlace(
            Long placeId,
            PlaceUpdateRequest request,
            MultipartFile thumbnail,
            boolean deleteThumbnail
    ) {
        validateThumbnailUpdate(thumbnail, deleteThumbnail);
        validateCoordinates(request.latitude(), request.longitude());
        Place place = placeRepository.findByIdForUpdate(placeId)
                .orElseThrow(() -> new SejongLifeException(ErrorCode.PLACE_NOT_FOUND));

        List<Category> categories = findCategoriesOrThrow(request.categoryIds());
        List<Tag> tags = findTagsOrThrow(request.tagIds());

        place.update(
                request.placeName(),
                request.address(),
                request.latitude(),
                request.longitude(),
                request.mapLinks(),
                request.isPartnership(),
                request.partnershipContent(),
                categories,
                tags
        );
        updateThumbnail(place, thumbnail, deleteThumbnail);
    }

    @Transactional
    public void deletePlace(Long placeId, AuthUser authUser) {
        Place place = placeRepository.findByIdForUpdate(placeId)
                .orElseThrow(() -> new SejongLifeException(ErrorCode.PLACE_NOT_FOUND));

        imageStorage.deleteImages(PlaceImage.toUrls(place.getPlaceImages()));

        place.getPlaceImages().clear();

        List<Review> reviews = new ArrayList<>(place.getReviews());
        for (Review review : reviews) {
            review.getUser().removeReview(review);
        }
        place.getReviews().clear();

        placeRepository.delete(place);
    }

    @Transactional(readOnly = true)
    public List<PlaceResponse> getWeeklyHotPlaces() {
        List<Place> hotPlaces = placeRepository.findTop10ByOrderByWeeklyViewCountDesc();
        return hotPlaces.stream()
                .map(PlaceResponse::from).toList();
    }

    @Transactional
    public PlaceDetailResponse getPlaceDetail(Long placeId, AuthUser authUser, HttpServletRequest request) {
        if (!placeRepository.existsById(placeId)) {
            throw new SejongLifeException(ErrorCode.PLACE_NOT_FOUND);
        }

        increaseViewCount(placeId, authUser, request);
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new SejongLifeException(ErrorCode.PLACE_NOT_FOUND));

        return PlaceDetailResponse.from(place);
    }

    private void increaseViewCount(Long placeId, AuthUser authUser, HttpServletRequest request) {
        Viewer viewer = identifyViewer(authUser, request);

        boolean first = placeViewService.recordFirstView(placeId, viewer);
        if (!first) {
            return;
        }
        placeRepository.increaseViewCount(placeId);
    }

    private void attachCategoriesToPlace(Place place, PlaceRequest request) {
        findCategoriesOrThrow(request.categoryIds()).forEach(place::addCategory);
    }

    private void attachTagsToPlace(Place place, PlaceRequest request) {
        findTagsOrThrow(request.tagIds()).forEach(place::addTag);
    }

    private List<Category> findCategoriesOrThrow(List<Long> categoryIds) {
        List<Category> categories = categoryRepository.findAllById(categoryIds);
        if (categories.size() != categoryIds.size()) {
            throw new SejongLifeException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        return categories;
    }

    private List<Tag> findTagsOrThrow(List<Long> tagIds) {
        List<Tag> tags = tagRepository.findAllById(tagIds);
        if (tags.size() != tagIds.size()) {
            throw new SejongLifeException(ErrorCode.TAG_NOT_FOUND);
        }
        return tags;
    }

    private void validateThumbnailUpdate(MultipartFile thumbnail, boolean deleteThumbnail) {
        if (thumbnail == null) {
            return;
        }
        if (thumbnail.isEmpty()) {
            throw new SejongLifeException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (deleteThumbnail) {
            throw new SejongLifeException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private void validateCoordinates(Double latitude, Double longitude) {
        boolean hasLatitude = latitude != null;
        boolean hasLongitude = longitude != null;
        if (hasLatitude != hasLongitude) {
            throw new SejongLifeException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private void updateThumbnail(Place place, MultipartFile thumbnail, boolean deleteThumbnail) {
        if (deleteThumbnail) {
            imageStorage.deleteImages(place.removeThumbnail());
            return;
        }
        if (thumbnail != null) {
            String uploadedUrl = imageStorage.uploadImage(String.valueOf(place.getId()), thumbnail);
            imageStorage.deleteImages(place.replaceThumbnail(uploadedUrl));
        }
    }

    private Viewer identifyViewer(AuthUser authUser, HttpServletRequest request) {
        if (authUser != null && StringUtils.hasText(authUser.studentId())) {
            return Viewer.user(authUser.studentId());
        }
        return Viewer.ipua(ViewerKeyGenerator.ipUaHash(request));
    }

}
