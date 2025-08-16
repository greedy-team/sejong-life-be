package org.example.sejonglifebe.place;

import org.example.sejonglifebe.place.entity.MapLinks;
import org.example.sejonglifebe.place.entity.Place;
import org.example.sejonglifebe.tag.Tag;
import org.example.sejonglifebe.tag.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class PlaceControllerTest {

    private static final Long NON_EXISTENT_ID = 999L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private TagRepository tagRepository;

    private Place testPlace;

    @BeforeEach
    void setUp() {
        Tag tag1 = new Tag("맛집");
        Tag tag2 = new Tag("가성비");
        tagRepository.save(tag1);
        tagRepository.save(tag2);

        Place place = Place.builder()
                .name("테스트 장소")
                .address("테스트 주소 123")
                .mapLinks(new MapLinks("naver.com", "kakao.com", "google.com"))
                .build();

        place.addTag(tag1);
        place.addImage("image1.jpg", true);
        place.addImage("image2.jpg", false);

        testPlace = placeRepository.save(place);
    }

    @Test
    @DisplayName("장소 상세 조회 성공 테스트")
    void getPlaceDetail_success() throws Exception {
        mockMvc.perform(get("/api/places/" + testPlace.getId())
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk()) // 1. HTTP 상태 코드가 200 OK 인지 확인
                .andExpect(jsonPath("$.message").value("장소 상세 정보 조회 성공")) // 2. 응답 메시지 확인
                .andExpect(jsonPath("$.data.id").value(testPlace.getId())) // 3. 응답 데이터의 ID 확인
                .andExpect(jsonPath("$.data.name").value("테스트 장소"))
                .andExpect(jsonPath("$.data.imageUrls.length()").value(2)) // 4. 이미지 URL 개수 확인
                .andExpect(jsonPath("$.data.tags[0]").value("맛집")) // 5. 태그 정보 확인
                .andDo(print()); // 요청/응답 전체 내용 콘솔에 출력
    }

    @Test
    @DisplayName("존재하지 않는 장소 ID 조회 실패 테스트")
    void getPlaceDetail_fail() throws Exception {
        mockMvc.perform(get("/api/places/" + NON_EXISTENT_ID)
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isNotFound()) // 1. HTTP 상태 코드가 404 Not Found 인지 확인
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND_PLACE")) // 2. 커스텀 에러 코드 확인
                .andExpect(jsonPath("$.message").exists()) // 3. 에러 메시지가 존재하는지 확인
                .andDo(print());
    }
}
