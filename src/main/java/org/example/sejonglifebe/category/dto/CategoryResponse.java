package org.example.sejonglifebe.category.dto;

import org.example.sejonglifebe.category.Category;

public record CategoryResponse(
        Long categoryId,
        String categoryName
) {

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getId(), category.getName());
    }
}
