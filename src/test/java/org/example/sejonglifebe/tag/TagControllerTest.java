package org.example.sejonglifebe.tag;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class TagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TagRepository tagRepository;

    @BeforeEach
    void setUp() {
        tagRepository.deleteAllInBatch();

        Tag tag1 = new Tag("맛집");
        Tag tag2 = new Tag("분위기 좋은");
        tagRepository.saveAll(List.of(tag1, tag2));
    }

    @Test
    @DisplayName("전체 태그 목록 조회 성공 테스트")
    void getAllTags_success() throws Exception {
        mockMvc.perform(get("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk()) // 1. HTTP 상태 코드 확인
                .andExpect(jsonPath("$.message").value("전체 태그 목록 조회 성공")) // 2. 응답 메시지 확인
                .andExpect(jsonPath("$.data").isArray()) // 3. data 필드 배열인지 확인
                .andExpect(jsonPath("$.data[0].tagName").value("맛집")) // 4. 첫 번째 태그이름 확인
                .andExpect(jsonPath("$.data[1].tagName").value("분위기 좋은")) // 5. 두 번째 태그이름 확인
                .andDo(print()); // 요청/응답 전체 내용 콘솔에 출력
    }
}
