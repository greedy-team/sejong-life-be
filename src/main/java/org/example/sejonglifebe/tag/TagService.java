package org.example.sejonglifebe.tag;

import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.category.CategoryRepository;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.example.sejonglifebe.tag.dto.TagResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    @Transactional(readOnly = true)
    public List<TagResponse> getTagsByCategoryId(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new SejongLifeException(ErrorCode.NOT_FOUND_CATEGORY);
        }

        return tagRepository.findAllByCategoryId(categoryId).stream()
                .map(TagResponse::from)
                .toList();
    }
}
