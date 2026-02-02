package org.example.sejonglifebe.place.favorite;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import jakarta.persistence.EntityManager;
import org.example.sejonglifebe.auth.AuthUser;
import org.example.sejonglifebe.common.jwt.JwtTokenProvider;
import org.example.sejonglifebe.place.PlaceRepository;
import org.example.sejonglifebe.place.entity.Place;
import org.example.sejonglifebe.s3.S3Service;
import org.example.sejonglifebe.user.Role;
import org.example.sejonglifebe.user.User;
import org.example.sejonglifebe.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class FavoritePlaceControllerTest {

    private static final String STUDENT_ID = "21011111";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FavoritePlaceRepository favoritePlaceRepository;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager em;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private S3Service s3Service;

    private Place place1;
    private Place place2;

    @BeforeEach
    void setUp() {
        given(jwtTokenProvider.validateAndGetAuthUser(anyString()))
                .willReturn(new AuthUser(STUDENT_ID, Role.USER));

        favoritePlaceRepository.deleteAll();
        placeRepository.deleteAll();
        userRepository.deleteAll();

        userRepository.save(User.builder()
                .studentId(STUDENT_ID)
                .nickname("테스트유저")
                .build());

        place1 = Place.builder().name("장소1").address("주소1").mainImageUrl("url1").build();
        place2 = Place.builder().name("장소2").address("주소2").mainImageUrl("url2").build();
        placeRepository.saveAll(List.of(place1, place2));
    }

    @Test
    @DisplayName("즐겨찾기 추가 성공")
    void addFavorite_success() throws Exception {
        mockMvc.perform(post("/api/places/{placeId}/favorite", place1.getId())
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("즐겨찾기 추가 성공"))
                .andDo(print());

        assertThat(favoritePlaceRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("즐겨찾기 추가 실패 - 이미 즐겨찾기한 장소")
    void addFavorite_duplicate_fail() throws Exception {
        mockMvc.perform(post("/api/places/{placeId}/favorite", place1.getId())
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/places/{placeId}/favorite", place1.getId())
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("ALREADY_FAVORITE_PLACE"))
                .andExpect(jsonPath("$.message").value("이미 즐겨찾기로 등록된 장소입니다."))
                .andDo(print());

        em.clear();
        assertThat(favoritePlaceRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("내 즐겨찾기 목록 조회 성공 - 즐겨찾기한 장소들이 반환된다")
    void getMyFavorites_success() throws Exception {
        // given
        mockMvc.perform(post("/api/places/{placeId}/favorite", place1.getId())
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/places/{placeId}/favorite", place2.getId())
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk());

        // when & then
        mockMvc.perform(get("/api/places/favorites/me")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("즐겨찾기 목록 조회 성공"))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].placeId").exists())
                .andExpect(jsonPath("$.data[0].placeName").exists())
                .andExpect(jsonPath("$.data[1].placeId").exists())
                .andExpect(jsonPath("$.data[1].placeName").exists())
                .andDo(print());
    }

    @Test
    @DisplayName("즐겨찾기 삭제 성공 - 삭제 후 목록에서 제외된다 (멱등 삭제)")
    void removeFavorite_success_idempotent() throws Exception {
        // given
        mockMvc.perform(post("/api/places/{placeId}/favorite", place1.getId())
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk());

        assertThat(favoritePlaceRepository.count()).isEqualTo(1);

        // when
        mockMvc.perform(delete("/api/places/{placeId}/favorite", place1.getId())
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("즐겨찾기 삭제 성공"))
                .andDo(print());

        // then
        assertThat(favoritePlaceRepository.count()).isEqualTo(0);

        mockMvc.perform(delete("/api/places/{placeId}/favorite", place1.getId())
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("즐겨찾기 삭제 성공"))
                .andDo(print());
    }

    @Test
    @DisplayName("즐겨찾기 목록 조회 - 즐겨찾기가 없으면 빈 배열을 반환한다")
    void getMyFavorites_empty() throws Exception {
        mockMvc.perform(get("/api/places/favorites/me")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("즐겨찾기 목록 조회 성공"))
                .andExpect(jsonPath("$.data.length()").value(0))
                .andDo(print());
    }

    @Test
    @DisplayName("즐겨찾기 추가 실패 - 존재하지 않는 장소면 PLACE_NOT_FOUND를 반환한다")
    void addFavorite_placeNotFound_fail() throws Exception {
        Long nonExistentPlaceId = 999L;

        mockMvc.perform(post("/api/places/{placeId}/favorite", nonExistentPlaceId)
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PLACE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").exists())
                .andDo(print());
    }

    @Test
    @DisplayName("즐겨찾기 개수 조회 - 즐겨찾기 없으면 0을 반환한다")
    void getMyFavoriteCount_empty() throws Exception {
        mockMvc.perform(get("/api/places/favorite/count")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("즐겨찾기 개수 조회 성공"))
                .andExpect(jsonPath("$.data").value(0))
                .andDo(print());
    }

    @Test
    @DisplayName("즐겨찾기 개수 조회 - 즐겨찾기 2개면 2를 반환한다")
    void getMyFavoriteCount_success() throws Exception {
        mockMvc.perform(post("/api/places/{placeId}/favorite", place1.getId())
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/places/{placeId}/favorite", place2.getId())
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk());

        em.flush();
        em.clear();

        mockMvc.perform(get("/api/places/favorite/count")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("즐겨찾기 개수 조회 성공"))
                .andExpect(jsonPath("$.data").value(2))
                .andDo(print());

        em.clear();
        assertThat(favoritePlaceRepository.countByUserStudentId(STUDENT_ID)).isEqualTo(2L);
    }

    @Test
    @DisplayName("즐겨찾기 추가 후 장소 목록 조회 시 isFavorite=true")
    void addFavorite_thenGetPlaceList_isFavoriteIsTrue() throws Exception {
        // given
        mockMvc.perform(post("/api/places/{placeId}/favorite", place1.getId())
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk());

        em.clear();

        // when & then
        mockMvc.perform(get("/api/places")
                        .param("category", "전체")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.placeId==" + place1.getId() + ")].isFavorite").value(true))
                .andDo(print());
    }

    @Test
    @DisplayName("즐겨찾기 추가 후 장소 상세 조회 시 isFavorite=true")
    void addFavorite_thenGetPlaceDetail_isFavoriteIsTrue() throws Exception {
        // given
        mockMvc.perform(post("/api/places/{placeId}/favorite", place1.getId())
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk());

        em.clear();

        // when & then
        mockMvc.perform(get("/api/places/{placeId}", place1.getId())
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isFavorite").value(true))
                .andDo(print());
    }

    @Test
    @DisplayName("즐겨찾기 삭제 후 장소 상세 조회 시 isFavorite=false")
    void removeFavorite_thenGetPlaceDetail_isFavoriteIsFalse() throws Exception {
        // given
        mockMvc.perform(post("/api/places/{placeId}/favorite", place1.getId())
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk());

        em.flush();

        mockMvc.perform(delete("/api/places/{placeId}/favorite", place1.getId())
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk());

        em.flush();
        em.clear();

        // when & then
        mockMvc.perform(get("/api/places/{placeId}", place1.getId())
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isFavorite").value(false))
                .andDo(print());
    }
}
