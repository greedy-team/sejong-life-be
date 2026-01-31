package org.example.sejonglifebe.place;


import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sejonglifebe.auth.AuthUser;
import org.example.sejonglifebe.category.Category;
import org.example.sejonglifebe.category.CategoryRepository;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.example.sejonglifebe.place.dto.PlaceDetailResponse;
import org.example.sejonglifebe.place.dto.PlaceRequest;
import org.example.sejonglifebe.place.dto.PlaceResponse;
import org.example.sejonglifebe.place.dto.PlaceSearchConditions;
import org.example.sejonglifebe.place.entity.Place;
import org.example.sejonglifebe.place.view.PlaceViewService;
import org.example.sejonglifebe.place.entity.PlaceImage;
import org.example.sejonglifebe.review.Review;
import org.example.sejonglifebe.s3.S3Service;
import org.example.sejonglifebe.tag.Tag;
import org.example.sejonglifebe.tag.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final TagRepository tagRepository;
    private final CategoryRepository categoryRepository;
    private final S3Service s3Service;
    private final PlaceViewService placeViewService;


    public List<PlaceResponse> getPlaceByConditions(PlaceSearchConditions conditions) {
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

        return placeRepository.getPlacesByConditions(category, tags, conditions.keyword())
                .stream()
                .map(PlaceResponse::from)
                .toList();
    }

    @Transactional
    public void createPlace(PlaceRequest request, MultipartFile thumbnail, AuthUser authUser) {

        Place place = Place.createPlace(
                request.placeName(),
                request.address(),
                request.mapLinks(),
                request.isPartnership(),
                request.partnershipContent()
        );

        placeRepository.save(place);

        if (thumbnail != null && !thumbnail.isEmpty()) {
            String uploadedUrl = s3Service.uploadImage(place.getId(), thumbnail);
            place.addImage(uploadedUrl, true);
        }

        attachCategoriesToPlace(place, request);
        attachTagsToPlace(place, request);
    }

    @Transactional
    public void deletePlace(Long placeId, AuthUser authUser) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new SejongLifeException(ErrorCode.PLACE_NOT_FOUND));

        List<PlaceImage> images = new ArrayList<>(place.getPlaceImages());
        s3Service.deleteImages(images);

        place.getPlaceImages().clear();

        List<Review> reviews = new ArrayList<>(place.getReviews());
        for (Review review : reviews) {
            review.getUser().removeReview(review);
        }
        place.getReviews().clear();

        placeRepository.delete(place);
    }

    @Transactional
    public List<PlaceResponse> getWeeklyHotPlaces() {
        List<Place> hotPlaces = placeRepository.findTop10ByOrderByWeeklyViewCountDesc();
        return hotPlaces.stream()
                .map(PlaceResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public PlaceDetailResponse getPlaceDetail(Long placeId, AuthUser authUser, HttpServletRequest request) {

        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new SejongLifeException(ErrorCode.PLACE_NOT_FOUND));

        PlaceDetailResponse response = PlaceDetailResponse.from(place);

        placeViewService.increaseViewCount(placeId, authUser, request);

        return response;
    }
    private void attachCategoriesToPlace(Place place, PlaceRequest request){

            List<Category> categories = categoryRepository.findAllById(request.categoryIds());
            if (categories.size() != request.categoryIds().size()) {
                throw new SejongLifeException(ErrorCode.CATEGORY_NOT_FOUND);
            }

            categories.forEach(place::addCategory);
        }

    private void attachTagsToPlace(Place place, PlaceRequest request) {

        List<Tag> tags = tagRepository.findAllById(request.tagIds());
        if (tags.size() != request.tagIds().size()) {
            throw new SejongLifeException(ErrorCode.TAG_NOT_FOUND);
        }

        tags.forEach(place::addTag);
    }

}
