package org.example.sejonglifebe.tag;

import org.example.sejonglifebe.category.Category;
import org.example.sejonglifebe.category.CategoryRepository;
import org.example.sejonglifebe.place.MapLinks;
import org.example.sejonglifebe.place.Place;
import org.example.sejonglifebe.place.PlaceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class TagControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    TagRepository tagRepository;
    @Autowired
    PlaceRepository placeRepository;

    @Test
    @DisplayName("카테고리별 태그를 조회 후 반환한다")
    void 카테고리별_태그를_조회_후_반환한다() throws Exception {
        Category category = categoryRepository.save(new Category("식당"));
        Tag tag1 = tagRepository.save(new Tag("가성비"));
        Tag tag2 = tagRepository.save(new Tag("집밥"));

        Place place = Place.builder()
                .name("또래끼리")
                .address("세종대 후문")
                .mapLinks(new MapLinks("네이버1", "카카오1", "구글1"))
                .build();

        place.addTag(tag1);
        place.addTag(tag2);
        place.addCategory(category);
        placeRepository.save(place);

        mockMvc.perform(get("/api/tags")
                        .param("categoryId", String.valueOf(category.getId()))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("카테고리별 태그 목록 조회 성공"))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[*].tagName",
                        containsInAnyOrder("가성비", "집밥")))
                .andExpect(jsonPath("$.data[*].tagId",
                        everyItem(notNullValue())));
    }

    @Test
    @DisplayName("카테고리에 해당하는 태그가 없으면 빈 배열을 반환한다")
    void 카테고리에_해당하는_태그가_없으면_빈_배열을_반환한다() throws Exception {
        Category emptyCategory = categoryRepository.save(new Category("편의점"));

        mockMvc.perform(get("/api/tags")
                        .param("categoryId", String.valueOf(emptyCategory.getId()))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("카테고리별 태그 목록 조회 성공"))
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    @DisplayName("존재하지 않는 카테고리이면 예외를 던진다")
    void 존재하지_않는_카테고리이면_예외를_던진다() throws Exception {
        mockMvc.perform(get("/api/tags")
                        .param("categoryId", "999999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND_CATEGORY"))
                .andExpect(jsonPath("$.message", containsString("존재하지 않는 카테고리입니다.")));
    }

    @Test
    @DisplayName("카테고리 파라미터가 누락되면 예외를 던진다")
    void 카테고리_파라미터가_누락되면_예외를_던진다() throws Exception {
        mockMvc.perform(get("/api/tags").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("MISSING_REQUIRED_PARAMETER"))
                .andExpect(jsonPath("$.message", containsString("필수 파라미터가 누락되었습니다.")));


    }

    @Test
    @DisplayName("카테고리 파라미터 타입이 맞지 않으면 예외를 던진다")
    void 카테고리ID_타입이_맞지_않으면_예외를_던진다() throws Exception {
        mockMvc.perform(get("/api/tags")
                        .param("categoryId", "abc")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("TYPE_MISMATCH"))
                .andExpect(jsonPath("$.message", containsString("파라미터 형식이 올바르지 않습니다.")));
    }
}
