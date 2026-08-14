package org.example.sejonglifebe.place.dto;

public enum PlaceSortType {
    REVIEW_COUNT,
    RATING,
    VIEW_COUNT;

    public static PlaceSortType orDefault(PlaceSortType sort) {
        return sort == null ? REVIEW_COUNT : sort;
    }
}
