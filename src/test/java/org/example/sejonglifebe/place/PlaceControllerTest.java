package org.example.sejonglifebe.place;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
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
public class PlaceControllerTest {

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
    private Place place1, place2, place3, place4, place5, place6; // 테스트에서 사용하기 위해 필드로 선언

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

        place1 = createPlace("식당1", "주소1", category1, List.of(tag1), new MapLinks("naver.com", "kakao.com", "google.com"), mainImg("image1.jpg"), img("image2.jpg"));
        place2 = createPlace("식당2", "주소2", category1, List.of(tag2), new MapLinks("n2.com", "k2.com", "g2.com"));
        place3 = createPlace("식당3", "주소3", category1, List.of(tag1, tag2), null);
        place4 = createPlace("카페1", "주소4", category2, List.of(tag3), new MapLinks("n4.com", "k4.com", "g4.com"), mainImg("image4.jpg"));
        place5 = createPlace("카페2", "주소5", category2, List.of(tag4), null);
        place6 = createPlace("카페3", "주소6", category2, List.of(tag3, tag4), null, mainImg("image6.jpg"));
        placeRepository.saveAll(List.of(place1, place2, place3, place4, place5, place6));

        detailPlace = place1;
    }

    private record Img(String url, boolean main) {}

    private static Img img(String url) {
        return new Img(url, false);
    }

    private static Img mainImg(String url) {
        return new Img(url, true);
    }

    private Place createPlace(String name, String address, Category category, List<Tag> tags, MapLinks mapLinks, Img... images) {
        Place place = Place.builder()
                .name(name)
                .address(address)
                .mapLinks(mapLinks)
                .build();
        place.addCategory(category);
        tags.forEach(place::addTag);
        for (Img i : images) {
            place.addImage(i.url(), i.main());
        }
        return place;
    }

    @Test
    @DisplayName("카테고리가 전체이고 선택된 태그가 없으면 모든 장소가 조회된다.")
    public void search_noCategory_noTags() throws Exception {
        mockMvc.perform(get("/api/places")
                        .param("category", "전체")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(6))
                .andExpect(jsonPath("$.data[0].viewCount").value(0))
                .andExpect(jsonPath("$.data[0].reviewCount").value(0))
                .andDo(print());
    }

    @Test
    @DisplayName("카테고리 전체이고 선택된 태그가 맛집과 가성비이면 태그를 둘 다 가진 장소 1개가 조회된다.")
    public void search_noCategory_withTags() throws Exception {
        mockMvc.perform(get("/api/places")
                        .param("category", "전체")
                        .param("tags", "맛집")
                        .param("tags", "가성비")
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))

                // 정렬 순서 검증
                .andExpect(jsonPath("$.data[1].placeName").value("식당3")) // 두 번째 결과는 '식당1'
                .andExpect(jsonPath("$.data[1].viewCount").value(0))
                .andExpect(jsonPath("$.data[1].reviewCount").value(0));
    }

    @Test
    @DisplayName("카테고리 식당이고 선택된 태그가 없으면 식당 3개가 조회된다.")
    public void search_categoryRestaurant_noTags() throws Exception {
        mockMvc.perform(get("/api/places")
                        .param("category", "식당")
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].placeName").value("식당1"))
                .andExpect(jsonPath("$.data[0].viewCount").value(0))
                .andExpect(jsonPath("$.data[0].reviewCount").value(0))
                .andExpect(jsonPath("$.data[1].placeName").value("식당2"))
                .andExpect(jsonPath("$.data[1].viewCount").value(0))
                .andExpect(jsonPath("$.data[1].reviewCount").value(0))
                .andExpect(jsonPath("$.data[2].placeName").value("식당3"))
                .andExpect(jsonPath("$.data[2].viewCount").value(0))
                .andExpect(jsonPath("$.data[2].reviewCount").value(0));
    }

    @Test
    @DisplayName("카테고리가 카페이고 선택된 태그가 분위기 좋은 이면 태그를 가진 카페 2개가 조회된다.")
    public void search_categoryCafe_withTags() throws Exception {
        mockMvc.perform(get("/api/places")
                        .param("category", "카페")
                        .param("tags", "분위기 좋은")
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].placeName").value("카페1"))
                .andExpect(jsonPath("$.data[0].viewCount").value(0))
                .andExpect(jsonPath("$.data[0].reviewCount").value(0))
                .andExpect(jsonPath("$.data[1].placeName").value("카페3"))
                .andExpect(jsonPath("$.data[1].viewCount").value(0))
                .andExpect(jsonPath("$.data[1].reviewCount").value(0));
    }

    @Test
    @DisplayName("잘못된 카테고리로 검색하면 예외를 반환한다")
    void search_wrongCategory_fail() throws Exception {
        mockMvc.perform(get("/api/places")
                        .param("category", "병원")
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("CATEGORY_NOT_FOUND"))
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

                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("TAG_NOT_FOUND"))
                .andExpect(jsonPath("$.message", containsString("존재하지 않는 태그입니다.")));
    }

    @Test
    @DisplayName("장소 상세 조회 성공 테스트")
    void getPlaceDetail_success() throws Exception {
        mockMvc.perform(get("/api/places/" + detailPlace.getId())
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value(detailPlace.getName()))
                .andExpect(jsonPath("$.data.categories.length()").value(1))
                .andExpect(jsonPath("$.data.categories[0].categoryName").value("식당"))
                .andExpect(jsonPath("$.data.tags[*].tagName", containsInAnyOrder("맛집")))
                .andExpect(jsonPath("$.data.mapLinks.naverMap").value("naver.com"))
                .andExpect(jsonPath("$.data.mapLinks.kakaoMap").value("kakao.com"))
                .andExpect(jsonPath("$.data.mapLinks.googleMap").value("google.com"))
                .andExpect(jsonPath("$.data.images.length()").value(2))
                .andExpect(jsonPath("$.data.images[0].url").value("image1.jpg"))
                .andExpect(jsonPath("$.data.images[1].url").value("image2.jpg"))
                .andDo(print());
    }


    @Test
    @DisplayName("존재하지 않는 장소 ID 조회 실패 테스트")
    void getPlaceDetail_fail() throws Exception {
        mockMvc.perform(get("/api/places/" + NON_EXISTENT_ID)
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PLACE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").exists())
                .andDo(print());
    }

    @Test
    @DisplayName("장소 상세 조회 시 쿠키가 없으면 전체/주간 조회수가 1 증가하고 쿠키를 발급한다")
    void getPlaceDetail_noCookie_increaseViewCount() throws Exception {
        Long placeId = detailPlace.getId();

        mockMvc.perform(get("/api/places/" + placeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.viewCount").value(1))
                .andExpect(cookie().exists("placeView"))
                .andExpect(cookie().value("placeView", "[" + placeId + "]"));

        Place updatedPlace = placeRepository.findById(placeId).get();
        assertThat(updatedPlace.getViewCount()).isEqualTo(1);
        assertThat(updatedPlace.getWeeklyViewCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("장소 상세 조회 시 동일한 장소 ID 쿠키가 있으면 조회수가 증가하지 않는다")
    void getPlaceDetail_withSamePlaceCookie_doesNotIncreaseViewCount() throws Exception {
        Long placeId = detailPlace.getId();
        Cookie placeViewCookie = new Cookie("placeView", "[" + placeId + "]");

        mockMvc.perform(get("/api/places/" + placeId)
                        .cookie(placeViewCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.viewCount").value(0));

        Place notUpdatedPlace = placeRepository.findById(placeId).get();
        assertThat(notUpdatedPlace.getViewCount()).isEqualTo(0);
        assertThat(notUpdatedPlace.getWeeklyViewCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("장소 상세 조회 시 다른 장소 ID 쿠키가 있으면 조회수가 1 증가하고 쿠키를 갱신한다")
    void getPlaceDetail_withAnotherPlaceCookie_increaseViewCount() throws Exception {
        Long placeId = detailPlace.getId();
        Long anotherPlaceId = 99L;
        Cookie placeViewCookie = new Cookie("placeView", "[" + anotherPlaceId + "]");

        mockMvc.perform(get("/api/places/" + placeId)
                        .cookie(placeViewCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.viewCount").value(1))
                .andExpect(cookie().exists("placeView"))
                .andExpect(cookie().value("placeView", "[" + anotherPlaceId + "]_[" + placeId + "]"));

        Place updatedPlace = placeRepository.findById(placeId).get();
        assertThat(updatedPlace.getViewCount()).isEqualTo(1);
        assertThat(updatedPlace.getWeeklyViewCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("주간 핫플레이스 조회 시 weeklyViewCount가 높은 순으로 정렬되어 반환된다")
    void getHotPlaces_success() throws Exception {
        // given: 테스트를 위해 특정 장소들의 weeklyViewCount 값을 임의로 설정
        // (엔티티에 protected setter나 테스트용 메서드가 있다고 가정)
        detailPlace.setWeeklyViewCount(100L); // 식당1
        place2.setWeeklyViewCount(50L);      // 식당2
        place6.setWeeklyViewCount(200L);     // 카페3
        placeRepository.saveAll(List.of(detailPlace, place2, place6));

        // when & then
        mockMvc.perform(get("/api/places/hot") // 핫플레이스 조회 API 엔드포인트
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("핫플레이스 조회 성공"))
                .andExpect(jsonPath("$.data.length()").value(6))
                .andExpect(jsonPath("$.data[0].placeName").value("카페3"))
                .andExpect(jsonPath("$.data[1].placeName").value("식당1"))
                .andExpect(jsonPath("$.data[2].placeName").value("식당2"))
                .andDo(print());
    }
}
