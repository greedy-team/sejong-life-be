package org.example.sejonglifebe.review.dto;

import java.util.List;

public record ReviewDataResponse(
    ReviewSummaryResponse summary,
    List<ReviewResponse> reviews
) {}




