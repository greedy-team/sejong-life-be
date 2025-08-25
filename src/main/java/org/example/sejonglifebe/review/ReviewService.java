package org.example.sejonglifebe.review;

import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.auth.AuthUser;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.example.sejonglifebe.place.PlaceRepository;
import org.example.sejonglifebe.place.entity.Place;
import org.example.sejonglifebe.review.dto.ReviewRequest;
import org.example.sejonglifebe.tag.Tag;
import org.example.sejonglifebe.tag.TagRepository;
import org.example.sejonglifebe.user.User;
import org.example.sejonglifebe.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private static final int TAG_THRESHOLD = 5;

    private final ReviewRepository reviewRepository;
    private final PlaceRepository placeRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;

    @Transactional
    public void createReview(Long placeId, ReviewRequest reviewRequest, AuthUser authUser) {

        User user = userRepository.findByStudentId(authUser.studentId()).orElseThrow(() -> new SejongLifeException(ErrorCode.USER_NOT_FOUND));
        Place place = placeRepository.findById(placeId).orElseThrow(() -> new SejongLifeException(ErrorCode.PLACE_NOT_FOUND));
        List<Tag> tags = tagRepository.findByIdIn(reviewRequest.tagIds());
        if (tags.size() != reviewRequest.tagIds().size()) {
            throw new SejongLifeException(ErrorCode.TAG_NOT_FOUND);
        }

        Review review = Review.createReview(place, user, reviewRequest.rating(), reviewRequest.content());
        if (!tags.isEmpty()) {
            tags.forEach(review::addTag);
        }

        reviewRepository.save(review);
        checkAndAddTagToPlace(tags, place);
    }

    private void checkAndAddTagToPlace(List<Tag> tags, Place place) {
        for (Tag tag : tags) {
            long count = reviewRepository.countByPlaceAndTag(place, tag);
            if (count >= TAG_THRESHOLD) {
                place.addTag(tag);
            }
        }
    }
}
