package org.example.sejonglifebe.place;

import org.example.sejonglifebe.auth.AuthInterceptor;
import org.example.sejonglifebe.auth.AuthUser;
import org.example.sejonglifebe.auth.AuthUserArgumentResolver;
import org.example.sejonglifebe.category.Category;
import org.example.sejonglifebe.category.CategoryRepository;
import org.example.sejonglifebe.common.jwt.JwtTokenExtractor;
import org.example.sejonglifebe.common.jwt.JwtTokenProvider;
import org.example.sejonglifebe.common.storage.ImageStorage;
import org.example.sejonglifebe.exception.GlobalExceptionHandler;
import org.example.sejonglifebe.external.MapLinksService;
import org.example.sejonglifebe.place.entity.Place;
import org.example.sejonglifebe.place.favorite.FavoritePlaceService;
import org.example.sejonglifebe.place.view.PlaceViewService;
import org.example.sejonglifebe.tag.Tag;
import org.example.sejonglifebe.tag.TagRepository;
import org.example.sejonglifebe.user.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 실제 Controller, Service, 인증 인터셉터를 사용하며 저장소와 JWT 검증만 대체한다.
class PlaceUpdateApiTest {

    private static final String PLACE_JSON = """
            {"placeName":"수정된 장소","address":"수정된 주소","latitude":37.55,"longitude":127.07,
             "categoryIds":[1],"tagIds":[10],"mapLinks":{"naverMap":"","kakaoMap":"","googleMap":""},
             "isPartnership":false,"partnershipContent":""}
            """;
    private MockMvc mockMvc;
    private Place place;
    private ImageStorage imageStorage;
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        TransactionSynchronizationManager.initSynchronization();
        PlaceRepository repository = mock(PlaceRepository.class);
        CategoryRepository categories = mock(CategoryRepository.class);
        TagRepository tags = mock(TagRepository.class);
        imageStorage = mock(ImageStorage.class);
        jwtTokenProvider = mock(JwtTokenProvider.class);
        given(jwtTokenProvider.validateAndGetAuthUser("test-token"))
                .willReturn(new AuthUser("21011111", Role.ADMIN));
        place = Place.builder().name("기존 장소").address("기존 주소").build();
        ReflectionTestUtils.setField(place, "id", 1L);
        given(repository.findByIdForUpdate(1L)).willReturn(Optional.of(place));
        given(categories.findAllById(List.of(1L))).willReturn(List.of(new Category("식당")));
        given(tags.findAllById(List.of(10L))).willReturn(List.of(new Tag("맛집")));
        PlaceService service = new PlaceService(repository, tags, categories, imageStorage, mock(PlaceViewService.class));
        JwtTokenExtractor extractor = new JwtTokenExtractor();
        mockMvc = MockMvcBuilders.standaloneSetup(new PlaceController(service,
                        mock(FavoritePlaceService.class), mock(MapLinksService.class)))
                .setControllerAdvice(new GlobalExceptionHandler())
                .addInterceptors(new AuthInterceptor(extractor, jwtTokenProvider))
                .setCustomArgumentResolvers(new AuthUserArgumentResolver(extractor, jwtTokenProvider))
                .build();
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.clearSynchronization();
    }

    @Test
    @DisplayName("multipart 요청으로 장소 정보를 수정하고 썸네일을 추가한다")
    void multipartPut_updatesPlaceAndAddsThumbnail() throws Exception {
        // given
        given(imageStorage.uploadImage(anyString(), any())).willReturn("new.webp");

        // when & then
        mockMvc.perform(request(PLACE_JSON)
                        .file(new MockMultipartFile("thumbnail", "photo.png", "image/png", new byte[]{1}))
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk());
        assertThat(place.getName()).isEqualTo("수정된 장소");
        assertThat(place.getThumbnailImage()).isEqualTo("new.webp");
    }

    @Test
    @DisplayName("빈 파일과 삭제 요청을 보내면 관리자 썸네일을 삭제한다")
    void multipartPut_deletesThumbnail() throws Exception {
        // given
        place.addImage("old.webp", true);

        // when & then
        mockMvc.perform(request(PLACE_JSON)
                        .file(new MockMultipartFile("thumbnail", new byte[0]))
                        .param("deleteThumbnail", "true")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk());
        assertThat(place.getThumbnailImage()).isNull();
    }

    @Test
    @DisplayName("파일과 삭제 요청을 함께 보내면 400을 반환한다")
    void multipartPut_conflictingImageActionsAreBadRequest() throws Exception {
        // when & then
        mockMvc.perform(request(PLACE_JSON).file(new MockMultipartFile("thumbnail", new byte[]{1}))
                        .param("deleteThumbnail", "true").header("Authorization", "Bearer test-token"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(imageStorage);
    }

    @Test
    @DisplayName("일반 사용자는 403을 반환하고 미인증 요청은 401을 반환한다")
    void multipartPut_requiresAdmin() throws Exception {
        // given
        given(jwtTokenProvider.validateAndGetAuthUser("test-token"))
                .willReturn(new AuthUser("21011111", Role.USER));

        // when & then
        mockMvc.perform(request(PLACE_JSON).header("Authorization", "Bearer test-token"))
                .andExpect(status().isForbidden());
        mockMvc.perform(request(PLACE_JSON)).andExpect(status().isUnauthorized());
        verifyNoInteractions(imageStorage);
    }

    @Test
    @DisplayName("장소명이나 좌표가 유효하지 않으면 400을 반환한다")
    void multipartPut_rejectsInvalidPlaceFields() throws Exception {
        // when & then
        mockMvc.perform(request(PLACE_JSON.replace("수정된 장소", ""))
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(request(PLACE_JSON.replace("37.55", "100"))
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isBadRequest());
        assertThat(place.getName()).isEqualTo("기존 장소");
    }

    @Test
    @DisplayName("필수 place 파트가 없으면 400을 반환한다")
    void multipartPut_missingPlacePartIsBadRequest() throws Exception {
        // when & then
        mockMvc.perform(multipart(HttpMethod.PUT, "/api/places/1")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(imageStorage);
    }

    @Test
    @DisplayName("place 파트의 Content-Type이 지원되지 않으면 415를 반환한다")
    void multipartPut_unsupportedPartContentTypeIs415() throws Exception {
        // when & then
        mockMvc.perform(multipart(HttpMethod.PUT, "/api/places/1")
                        .file(new MockMultipartFile("place", "", "text/plain", PLACE_JSON.getBytes(StandardCharsets.UTF_8)))
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isUnsupportedMediaType());
        verifyNoInteractions(imageStorage);
    }

    @Test
    @DisplayName("JSON PUT 요청은 415를 반환한다")
    void jsonPut_isUnsupportedMediaType() throws Exception {
        // when & then
        mockMvc.perform(put("/api/places/1").contentType("application/json").content(PLACE_JSON)
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isUnsupportedMediaType());
        verifyNoInteractions(imageStorage);
    }

    @Test
    @DisplayName("JSON 형식이 잘못되면 400을 반환한다")
    void multipartPut_malformedJsonIsBadRequest() throws Exception {
        // when & then
        mockMvc.perform(request("{broken").header("Authorization", "Bearer test-token"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(imageStorage);
    }

    private MockMultipartHttpServletRequestBuilder request(String json) {
        return multipart(HttpMethod.PUT, "/api/places/1")
                .file(new MockMultipartFile("place", "", "application/json", json.getBytes(StandardCharsets.UTF_8)));
    }
}
