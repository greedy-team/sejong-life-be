package org.example.sejonglifebe.review.dto;

import java.util.Map;

public record ReviewSummaryResponse(
        Long reviewCount,
        Double averageRate,
        Map<String, Long> ratingDistribution
) {}
