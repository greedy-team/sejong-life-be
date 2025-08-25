package org.example.sejonglifebe.review;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.auth.AuthUser;
import org.example.sejonglifebe.common.dto.ApiResponse;
import org.example.sejonglifebe.review.dto.ReviewRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping(value = "/api/places/{placeId}/reviews", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> createReview(@PathVariable(name = "placeId") Long placeId,
                                                          @Valid @RequestPart("review") ReviewRequest reviewRequest,
                                                          @RequestPart(value = "images", required = false) List<MultipartFile> images,
                                                          AuthUser authUser) {
        reviewService.createReview(placeId, reviewRequest, authUser);
        return ApiResponse.of(HttpStatus.CREATED, "리뷰 작성 성공", null);
    }
}
