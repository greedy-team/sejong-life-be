package org.example.sejonglifebe.tag.dto;

import org.example.sejonglifebe.tag.Tag;

public record TagResponse(
        Long tagId,
        String tagName
) {

    public static TagResponse from(Tag tag) {
        return new TagResponse(tag.getId(), tag.getName());
    }
}
