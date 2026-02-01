package org.example.sejonglifebe.place;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.sejonglifebe.place.view.ViewerKeyGenerator.ipUaHash;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.sejonglifebe.auth.AuthUser;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.user.Role;
import org.example.sejonglifebe.common.jwt.JwtTokenProvider;
import org.example.sejonglifebe.place.dto.PlaceRequest;
import org.example.sejonglifebe.place.entity.MapLinks;
import org.example.sejonglifebe.place.view.PlaceViewLog;
import org.example.sejonglifebe.place.view.PlaceViewLogRepository;
import org.springframework.http.HttpHeaders;
import org.example.sejonglifebe.s3.S3Service;
import org.example.sejonglifebe.user.UserRepository;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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
import org.springframework.web.multipart.MultipartFile;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class PlaceControllerTest {

    private static final Long NON_EXISTENT_ID = 999L;
    private static final String TEST_IP = "203.0.113.10";
    private static final String TEST_UA = "Mozilla/5.0 (JUnit Test UA)";
    private static final String VIEWER_TYPE_IPUA = "IPUA";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private S3Service s3Service;

    @Autowired
    private PlaceViewLogRepository placeViewLogRepository;

    private Place detailPlace;
    private Place place1, place2, place3, place4, place5, place6; // 테스트에서 사용하기 위해 필드로 선언

    @BeforeEach
    void setUp() {

        placeRepository.deleteAll();
        tagRepository.deleteAll();
        categoryRepository.deleteAll();
        placeViewLogRepository.deleteAll();

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
                .andExpect(jsonPath("$.data[0].placeName").value("식당3")) // 두 번째 결과는 '식당1'
                .andExpect(jsonPath("$.data[0].viewCount").value(0))
                .andExpect(jsonPath("$.data[0].reviewCount").value(0));
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
    @DisplayName("키워드만 입력하면 장소명에 키워드가 포함된 장소가 조회된다")
    void search_withKeywordOnly() throws Exception {
        mockMvc.perform(get("/api/places")
                        .param("category", "전체")
                        .param("keyword", "식당")
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].placeName").value("식당1"))
                .andExpect(jsonPath("$.data[1].placeName").value("식당2"))
                .andExpect(jsonPath("$.data[2].placeName").value("식당3"))
                .andDo(print());
    }

    @Test
    @DisplayName("카테고리 + 키워드로 검색하면 해당 카테고리에서 키워드가 포함된 장소가 조회된다")
    void search_withCategoryAndKeyword() throws Exception {
        mockMvc.perform(get("/api/places")
                        .param("category", "카페")
                        .param("keyword", "카페1")
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].placeName").value("카페1"))
                .andDo(print());
    }

    @Test
    @DisplayName("태그 + 키워드로 검색하면 해당 태그를 가지고 키워드가 포함된 장소가 조회된다")
    void search_withTagAndKeyword() throws Exception {
        mockMvc.perform(get("/api/places")
                        .param("category", "전체")
                        .param("tags", "맛집")
                        .param("keyword", "식당1")
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].placeName").value("식당1"))
                .andDo(print());
    }

    @Test
    @DisplayName("카테고리 + 태그 + 키워드로 검색하면 모든 조건을 만족하는 장소가 조회된다")
    void search_withAllConditions() throws Exception {
        mockMvc.perform(get("/api/places")
                        .param("category", "카페")
                        .param("tags", "분위기 좋은")
                        .param("keyword", "카페3")
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].placeName").value("카페3"))
                .andDo(print());
    }

    @Test
    @DisplayName("존재하지 않는 키워드로 검색하면 빈 배열을 반환한다")
    void search_withNonExistentKeyword() throws Exception {
        mockMvc.perform(get("/api/places")
                        .param("category", "전체")
                        .param("keyword", "존재하지않는장소")
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0))
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

    /*
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
    @DisplayName("장소 상세 조회 시 view log가 없으면 전체/주간 조회수가 1 증가하고 view log를 저장한다")
    void getPlaceDetail_noViewLog_increaseViewCount() throws Exception {
        Long placeId = detailPlace.getId();

        mockMvc.perform(get("/api/places/" + placeId)
                        .header("X-Forwarded-For", TEST_IP)
                        .header(HttpHeaders.USER_AGENT, TEST_UA)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.viewCount").value(1));

        Place updatedPlace = placeRepository.findById(placeId).get();
        assertThat(updatedPlace.getViewCount()).isEqualTo(1);
        assertThat(updatedPlace.getWeeklyViewCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("장소 상세 조회 시 동일 viewer의 최근(6시간 이내) view log가 있으면 조회수가 증가하지 않는다")
    void getPlaceDetail_withRecentViewLog_doesNotIncreaseViewCount() throws Exception {
        Long placeId = detailPlace.getId();

        String viewerKey = ipUaHash(TEST_IP, TEST_UA);
        placeViewLogRepository.save(new PlaceViewLog(
                placeId, VIEWER_TYPE_IPUA, viewerKey, LocalDateTime.now()
        ));

        mockMvc.perform(get("/api/places/" + placeId)
                        .header("X-Forwarded-For", TEST_IP)
                        .header(HttpHeaders.USER_AGENT, TEST_UA)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.viewCount").value(0))
                .andExpect(cookie().doesNotExist("placeView"));

        Place notUpdatedPlace = placeRepository.findById(placeId).get();
        assertThat(notUpdatedPlace.getViewCount()).isEqualTo(0);
        assertThat(notUpdatedPlace.getWeeklyViewCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("장소 상세 조회 시 동일 viewer가 '다른 장소'만 최근에 봤더라도 현재 장소는 조회수가 1 증가하고 view log를 저장한다")
    void getPlaceDetail_withAnotherPlaceViewLog_increaseViewCount() throws Exception {
        Long placeId = detailPlace.getId();
        Long anotherPlaceId = 99L;

        String viewerKey = ipUaHash(TEST_IP, TEST_UA);
        placeViewLogRepository.save(new PlaceViewLog(
                anotherPlaceId, VIEWER_TYPE_IPUA, viewerKey, LocalDateTime.now()
        ));

        mockMvc.perform(get("/api/places/" + placeId)
                        .header("X-Forwarded-For", TEST_IP)
                        .header(HttpHeaders.USER_AGENT, TEST_UA)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.viewCount").value(1))
                .andExpect(cookie().doesNotExist("placeView"));

        Place updatedPlace = placeRepository.findById(placeId).get();
        assertThat(updatedPlace.getViewCount()).isEqualTo(1);
        assertThat(updatedPlace.getWeeklyViewCount()).isEqualTo(1);
    }
     */

    @Test
    @DisplayName("주간 핫플레이스 조회 시 weeklyViewCount가 높은 순으로 정렬되어 반환된다")
    void getHotPlaces_success() throws Exception {
        // given: 테스트를 위해 특정 장소들의 weeklyViewCount 값을 임의로 설정
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

    @Test
    @DisplayName("장소가 정상적으로 생성된다 - 썸네일 없음")
    void createPlace_success_withoutThumbnail() throws Exception {
        // given: setUp()에서 category/tag 이미 저장됨 -> 중복 save 금지
        Category category = categoryRepository.findByName("식당")
                .orElseThrow(() -> new IllegalStateException("식당 카테고리가 없습니다."));
        Tag tag1 = tagRepository.findByName("맛집")
                .orElseThrow(() -> new IllegalStateException("맛집 태그가 없습니다."));
        Tag tag2 = tagRepository.findByName("가성비")
                .orElseThrow(() -> new IllegalStateException("가성비 태그가 없습니다."));
        given(jwtTokenProvider.validateAndGetAuthUser(anyString()))
                .willReturn(new AuthUser("21011111", Role.ADMIN));

        PlaceRequest request = new PlaceRequest(
                "새로운 장소",
                "새로운 주소",
                List.of(category.getId()), // CategoryInfo 리스트 괄호 닫기
                List.of(tag1.getId(), tag2.getId()),
                new MapLinks("https://naver.com/place", "", ""),
                false,
                ""
        );

        MockMultipartFile placePart = new MockMultipartFile(
                "place",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(request)
        );

        long beforeCount = placeRepository.count();

        // when & then
        mockMvc.perform(multipart("/api/places")
                        .file(placePart)
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("장소 추가 성공"))
                .andDo(print());

        // then: DB 저장 검증
        assertThat(placeRepository.count()).isEqualTo(beforeCount + 1);

        Place savedPlace = placeRepository.findAll().stream()
                .filter(place -> place.getName().equals("새로운 장소"))
                .findFirst()
                .orElseThrow();

        assertThat(savedPlace.getAddress()).isEqualTo("새로운 주소");
        assertThat(savedPlace.getPlaceImages()).isEmpty(); // 썸네일 없음
        assertThat(savedPlace.getPlaceCategories()).hasSize(1);
        assertThat(savedPlace.getPlaceTags()).hasSize(2);
    }

    @Test
    @DisplayName("장소가 정상적으로 생성된다 - 썸네일 포함")
    void createPlace_success_withThumbnail() throws Exception {
        // given
        Category category = categoryRepository.findByName("카페")
                .orElseThrow(() -> new IllegalStateException("카페 카테고리가 없습니다."));
        Tag tag = tagRepository.findByName("분위기 좋은")
                .orElseThrow(() -> new IllegalStateException("분위기 좋은 태그가 없습니다."));
        given(jwtTokenProvider.validateAndGetAuthUser(anyString()))
                .willReturn(new AuthUser("21011111", Role.ADMIN));

        PlaceRequest request = new PlaceRequest(
                "썸네일 장소",
                "썸네일 주소",
                List.of(category.getId()),
                List.of(tag.getId()),
                new MapLinks("", "https://kakao.com/place", ""),
                false,
                ""
        );

        MockMultipartFile placePart = new MockMultipartFile(
                "place",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(request)
        );

        MockMultipartFile thumbnailPart = new MockMultipartFile(
                "thumbnail",
                "thumb.webp",
                "image/webp",
                "fake-image".getBytes()
        );

        given(s3Service.uploadImage(anyLong(), any(MultipartFile.class)))
                .willReturn("https://mock-s3/thumb.webp");

        long beforeCount = placeRepository.count();

        // when & then
        mockMvc.perform(multipart("/api/places")
                        .file(placePart)
                        .file(thumbnailPart)
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("장소 추가 성공"))
                .andDo(print());

        // then
        assertThat(placeRepository.count()).isEqualTo(beforeCount + 1);

        Place savedPlace = placeRepository.findAll().stream()
                .filter(place -> place.getName().equals("썸네일 장소"))
                .findFirst()
                .orElseThrow();

        assertThat(savedPlace.getPlaceImages()).hasSize(1);
        assertThat(savedPlace.getPlaceImages().get(0).getUrl()).isEqualTo("https://mock-s3/thumb.webp");
        assertThat(savedPlace.getPlaceImages().get(0).getIsThumbnail()).isTrue();
    }

    @Test
    @DisplayName("장소 생성 권한 없음 - USER면 ACCESS_DENIED 반환")
    void createPlace_fail_accessDenied_whenUserRole() throws Exception {
        // given
        Category category = categoryRepository.findByName("식당")
                .orElseThrow(() -> new IllegalStateException("식당 카테고리가 없습니다."));
        Tag tag1 = tagRepository.findByName("맛집")
                .orElseThrow(() -> new IllegalStateException("맛집 태그가 없습니다."));
        Tag tag2 = tagRepository.findByName("가성비")
                .orElseThrow(() -> new IllegalStateException("가성비 태그가 없습니다."));

        given(jwtTokenProvider.validateAndGetAuthUser(anyString()))
                .willReturn(new AuthUser("21011111", Role.USER)); // ✅ USER

        PlaceRequest request = new PlaceRequest(
                "권한없는 장소",
                "권한없는 주소",
                List.of(category.getId()),
                List.of(tag1.getId(), tag2.getId()),
                new MapLinks("https://naver.com/place", "", ""),
                false,
                ""
        );

        MockMultipartFile placePart = new MockMultipartFile(
                "place",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(request)
        );

        long beforeCount = placeRepository.count();

        // when & then
        mockMvc.perform(multipart("/api/places")
                        .file(placePart)
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isForbidden())
                .andDo(print());

        // then: DB 저장 안 됨
        assertThat(placeRepository.count()).isEqualTo(beforeCount);
    }

    @Test
    @DisplayName("장소가 정상적으로 삭제된다")
    void deletePlace_success() throws Exception {
        // given
        Long placeId = detailPlace.getId();
        long beforeCount = placeRepository.count();
        given(jwtTokenProvider.validateAndGetAuthUser(anyString()))
                .willReturn(new AuthUser("21011111", Role.ADMIN));

        // when & then
        mockMvc.perform(delete("/api/places/{placeId}", placeId)
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("장소 삭제 성공"))
                .andDo(print());

        // then: DB 반영 확인
        assertThat(placeRepository.count()).isEqualTo(beforeCount - 1);
        assertThat(placeRepository.findById(placeId)).isEmpty();
    }

    @Test
    @DisplayName("장소 삭제 권한 없음 - USER면 ACCESS_DENIED 반환")
    void deletePlace_fail_accessDenied_whenUserRole() throws Exception {
        // given
        Long placeId = detailPlace.getId();
        long beforeCount = placeRepository.count();

        given(jwtTokenProvider.validateAndGetAuthUser(anyString()))
                .willReturn(new AuthUser("21011111", Role.USER));

        // when & then
        mockMvc.perform(delete("/api/places/{placeId}", placeId)
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isForbidden())
                .andDo(print());

        // then: DB 삭제 안 됨
        assertThat(placeRepository.count()).isEqualTo(beforeCount);
        assertThat(placeRepository.findById(placeId)).isPresent();
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
