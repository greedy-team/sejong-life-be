package org.example.sejonglifebe.place;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.example.sejonglifebe.place.entity.MapLinks;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import org.example.sejonglifebe.category.Category;
import org.example.sejonglifebe.category.CategoryRepository;
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

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class PlaceControllerTest
{
    private static final Long NON_EXISTENT_ID = 999L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TagRepository tagRepository;

    private Place detailPlace;

    @BeforeEach
    void setUp() {
        placeRepository.deleteAll();
        tagRepository.deleteAll();
        categoryRepository.deleteAll();

        Category category1 = new Category("식당");
        Category category2 = new Category("카페");
        categoryRepository.saveAll(List.of(category1, category2));

        Tag tag1 = new Tag("맛집");
        Tag tag2 = new Tag("가성비");
        Tag tag3 = new Tag("분위기 좋은");
        Tag tag4 = new Tag("콘센트 있는");
        tagRepository.saveAll(List.of(tag1, tag2, tag3, tag4));

        Place place1 = createPlaceInDetail("식당1", "주소1", "url1", category1, List.of(tag1));
        detailPlace = placeRepository.save(place1);

        Place place2 = createPlace("식당2", "주소2", "url2", category1, List.of(tag2));
        Place place3 = createPlace("식당3", "주소3", "url3", category1, List.of(tag1, tag2));
        Place place4 = createPlace("카페1", "주소4", "url4", category2, List.of(tag3));
        Place place5 = createPlace("카페2", "주소5", "url5", category2, List.of(tag4));
        Place place6 = createPlace("카페3", "주소6", "url6", category2, List.of(tag3, tag4));

        placeRepository.saveAll(List.of(place2, place3, place4, place5, place6));
    }

    private Place createPlaceInDetail(String name, String address, String url, Category category, List<Tag> tags) {
        Place place = Place.builder()
                .name(name)
                .address(address)
                .mapLinks(new MapLinks("naver.com", "kakao.com", "google.com"))
                .mainImageUrl(url)
                .build();
        place.addImage("image1.jpg", true);
        place.addImage("image2.jpg", false);
        place.addCategory(category);
        tags.forEach(place::addTag);

        return place;
    }

    private Place createPlace(String name, String address, String url, Category category, List<Tag> tags) {
        Place place = Place.builder()
                .name(name)
                .address(address)
                .mainImageUrl(url)
                .build();

        place.addCategory(category);
        tags.forEach(place::addTag);

        return place;
    }

    @Test
    @DisplayName("카테고리가 전체이고 선택된 태그가 없으면 모든 장소가 조회된다.")
    public void search_noCategory_noTags() throws Exception {
        mockMvc.perform(get("/api/places")
                        .param("category", "전체")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(6)) // 응답 데이터의 개수가 6개인지 확인
                .andDo(print());
    }

    @Test
    @DisplayName("카테고리 전체이고 선택된 태그가 맛집과 가성비이면 태그를 하나라도 가진 장소 3개가 조회된다.")
    public void search_noCategory_withTags() throws Exception {
        // when
        mockMvc.perform(get("/api/places")
                        .param("category", "전체")   // @RequestParam("categories") String category
                        .param("tags", "맛집")       // @RequestParam("tags") List<String> tags 의 첫 번째 요소
                        .param("tags", "가성비")      // @RequestParam("tags") List<String> tags 의 두 번째 요소
                        .contentType(MediaType.APPLICATION_JSON))

                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3)) // 전체 개수 검증

                // 정렬 순서 검증
                .andExpect(jsonPath("$.data[0].placeName").value("식당3")) // 첫 번째 결과는 태그 2개인 '식당3'
                .andExpect(jsonPath("$.data[1].placeName").value("식당1")) // 두 번째 결과는 '식당1'
                .andExpect(jsonPath("$.data[2].placeName").value("식당2")); // 세 번째 결과는 '식당2'
    }

    @Test
    @DisplayName("카테고리 식당이고 선택된 태그가 없으면 식당 3개가 조회된다.")
    public void search_categoryRestaurant_noTags() throws Exception {
        // when
        mockMvc.perform(get("/api/places")
                        .param("category", "식당")
                        .contentType(MediaType.APPLICATION_JSON))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].placeName").value("식당1"))
                .andExpect(jsonPath("$.data[1].placeName").value("식당2"))
                .andExpect(jsonPath("$.data[2].placeName").value("식당3"));
    }

    @Test
    @DisplayName("카테고리가 카페이고 선택된 태그가 분위기 좋은 이면 태그를 가진 카페 2개가 조회된다.")
    public void search_categoryCafe_withTags() throws Exception {
        // when
        mockMvc.perform(get("/api/places")
                        .param("category", "카페")
                        .param("tags", "분위기 좋은")
                        .contentType(MediaType.APPLICATION_JSON))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].placeName").value("카페1"))
                .andExpect(jsonPath("$.data[1].placeName").value("카페3"));
    }

    @Test
    @DisplayName("잘못된 카테고리로 검색하면 예외를 반환한다")
    void search_wrongCategory_fail() throws Exception {
        mockMvc.perform(get("/api/places")
                        .param("category", "병원")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_CATEGORY"))
                .andExpect(jsonPath("$.message", containsString("존재하지 않는 카테고리입니다.")));
    }

    @Test
    @DisplayName("잘못된 태그로 검색하면 예외를 반환한다")
    void search_wrongTags_fail() throws Exception {
        mockMvc.perform(get("/api/places")
                        .param("category", "전체")
                        .param("tags", "맛집")
                        .param("tags", "진상부리기 좋은")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_TAG"))
                .andExpect(jsonPath("$.message", containsString("존재하지 않는 태그입니다.")));
    }

    @Test
    @DisplayName("장소 상세 조회 성공 테스트")
    void getPlaceDetail_success() throws Exception {
        mockMvc.perform(get("/api/places/" + detailPlace.getId())
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk()) // 1. HTTP 상태 코드가 200 OK 인지 확인
                .andExpect(jsonPath("$.message").value("장소 상세 정보 조회 성공")) // 2. 응답 메시지 확인
                .andExpect(jsonPath("$.data.id").value(detailPlace.getId())) // 3. 응답 데이터의 ID 확인
                .andExpect(jsonPath("$.data.name").value("식당1"))
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
