package org.example.sejonglifebe.category;

import org.junit.jupiter.api.BeforeEach;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class CategoryControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();
        categoryRepository.save(new Category("식당"));
        categoryRepository.save(new Category("카페"));
    }

    @Test
    @DisplayName("전체 카테고리를 조회 후 반환한다")
    void 전체_카테고리를_조회_후_반환한다() throws Exception {
        mockMvc.perform(get("/api/categories").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("전체 카테고리 목록 조회 성공"))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[*].categoryName",
                        containsInAnyOrder("식당", "카페")))
                .andExpect(jsonPath("$.data[*].categoryId",
                        everyItem(notNullValue())));
    }

    @Test
    @DisplayName("카테고리가 없으면 빈 배열을 반환한다")
    void 카테고리가_없으면_빈_배열을_반환한다() throws Exception {
        categoryRepository.deleteAll();

        mockMvc.perform(get("/api/categories").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("전체 카테고리 목록 조회 성공"))
                .andExpect(jsonPath("$.data", hasSize(0)));
    }
}
