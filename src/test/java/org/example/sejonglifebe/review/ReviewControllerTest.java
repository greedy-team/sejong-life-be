package org.example.sejonglifebe.review;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.sejonglifebe.auth.AuthUser;
import org.example.sejonglifebe.category.Category;
import org.example.sejonglifebe.category.CategoryRepository;
import org.example.sejonglifebe.common.jwt.JwtTokenProvider;
import org.example.sejonglifebe.place.PlaceRepository;
import org.example.sejonglifebe.place.entity.MapLinks;
import org.example.sejonglifebe.place.entity.Place;
import org.example.sejonglifebe.review.dto.ReviewRequest;
import org.example.sejonglifebe.tag.Tag;
import org.example.sejonglifebe.tag.TagRepository;
import org.example.sejonglifebe.user.User;
import org.example.sejonglifebe.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private static final Long NON_EXISTENT_ID = 999L;

    @BeforeEach
    void setUp() {
        given(jwtTokenProvider.validateAndGetAuthUser(anyString()))
                .willReturn(new AuthUser("21011111"));
    }

    @Test
    @DisplayName("리뷰가 정상적으로 생성된다")
    void createReview_success() throws Exception {
        // given
        User user = User.builder().studentId("21011111").nickname("닉네임").build();
        userRepository.save(user);

        Category category = categoryRepository.save(new Category("식당"));
        Tag tag1 = tagRepository.save(new Tag("가성비"));
        Tag tag2 = tagRepository.save(new Tag("집밥"));

        Place place = createPlaceFixture("또래끼리", "세종대 후문", "url", category, List.of());
        placeRepository.save(place);

        ReviewRequest request = new ReviewRequest(3, "맛있음", List.of(tag1.getId(), tag2.getId()));
        MockMultipartFile reviewPart = new MockMultipartFile("review", "", "application/json",
                objectMapper.writeValueAsBytes(request));

        // when & then
        mockMvc.perform(multipart("/api/places/{placeId}/reviews", place.getId())
                        .file(reviewPart)
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("리뷰 작성 성공"));
    }

    @Test
    @DisplayName("리뷰에서 5번 이상 언급된 태그는 장소 태그에 추가된다.")
    void addFrequentlyUsedTagToPlace_whenTagMentionedFiveTimes() throws Exception {
        // given
        User user = User.builder().studentId("21011111").nickname("닉네임").build();
        userRepository.save(user);

        Category category = categoryRepository.save(new Category("식당"));
        Tag tag = tagRepository.save(new Tag("가성비"));

        Place place = createPlaceFixture("또래끼리", "세종대 후문", "url", category, List.of());
        placeRepository.save(place);

        ReviewRequest request = new ReviewRequest(5, "맛있음", List.of(tag.getId()));
        MockMultipartFile reviewPart = new MockMultipartFile("review", "", "application/json",
                objectMapper.writeValueAsBytes(request));

        // when: 동일한 태그가 포함된 리뷰 5개 작성
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(multipart("/api/places/{placeId}/reviews", place.getId())
                            .file(reviewPart)
                            .header("Authorization", "Bearer test-token"))
                    .andExpect(status().isCreated());
        }

        // then: placeTags 에 tag 가 추가되었는지 검증
        Place savedPlace = placeRepository.findById(place.getId()).orElseThrow();
        assertThat(savedPlace.getPlaceTags()).hasSize(1).extracting(pt -> pt.getTag().getName()).contains("가성비");
    }

    @Test
    @DisplayName("장소에 대한 리뷰 목록을 성공적으로 조회한다")
    void getReviews_success() throws Exception {
        // given: 4. 조회 테스트에 필요한 데이터 생성 로직을 테스트 메서드 내부로 이동
        Place place = setupReviewListData();

        // when & then
        mockMvc.perform(get("/api/places/{placeId}/reviews", place.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("리뷰 목록 조회 성공"))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].content").value("맛있어요"))
                .andExpect(jsonPath("$.data[0].userName").value("닉네임"))
                .andExpect(jsonPath("$.data[0].studentId").value("21011111"))
                .andExpect(jsonPath("$.data[0].tags[*].tagName", containsInAnyOrder("맛집", "가성비")))
                .andExpect(jsonPath("$.data[1].content").value("별로에요"))
                .andExpect(jsonPath("$.data[1].tags[*].tagName", containsInAnyOrder("가성비")))
                .andExpect(jsonPath("$.data[1].userName").value("닉네임2"))
                .andExpect(jsonPath("$.data[1].studentId").value("21011112"))
                .andDo(print());
    }

    @Test
    @DisplayName("존재하지 않는 장소의 리뷰 목록 조회 시 예외를 반환한다")
    void getReviews_placeNotFound_fail() throws Exception {
        mockMvc.perform(get("/api/places/{placeId}/reviews", NON_EXISTENT_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PLACE_NOT_FOUND"));
    }

    @Test
    @DisplayName("장소에 대한 리뷰 요약 정보를 성공적으로 조회한다")
    void getReviewSummary_success() throws Exception {
        // given
        Place place = setupReviewListData();

        // when & then
        mockMvc.perform(get("/api/places/{placeId}/reviews/summary", place.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("리뷰 요약 정보 조회 성공"))
                .andExpect(jsonPath("$.data.reviewCount").value(2))
                .andExpect(jsonPath("$.data.averageRate").value(3.5));
    }

    @Test
    @DisplayName("존재하지 않는 장소의 리뷰 요약 정보 조회 시 예외를 반환한다")
    void getReviewSummary_placeNotFound_fail() throws Exception {
        mockMvc.perform(get("/api/places/{placeId}/reviews/summary", NON_EXISTENT_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PLACE_NOT_FOUND"));
    }

    @Test
    @DisplayName("리뷰 좋아요 생성, 삭제 성공")
    void likeAndUnlikeReview_success() throws Exception {
        // given
        User user = userRepository.save(User.builder().studentId("21011111").nickname("닉네임").build());
        Place place = createPlaceFixture("맛집", "주소", "url", categoryRepository.save(new Category("식당")), List.of());
        placeRepository.save(place);
        Review review = createReview(place, user, "맛있어요", 5, List.of(), List.of());
        reviewRepository.save(review);

        // when
        mockMvc.perform(post("/api/places/{placeId}/reviews/{reviewId}/likes", place.getId(), review.getId())
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("리뷰 좋아요 성공"));

        // then
        Review savedReview = reviewRepository.findById(review.getId()).orElseThrow();
        assertThat(savedReview.getReviewLikes()).hasSize(1);
        assertThat(savedReview.getLikeCount()).isEqualTo(1);

        // when
        mockMvc.perform(delete("/api/places/{placeId}/reviews/{reviewId}/likes", place.getId(), review.getId())
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("리뷰 좋아요 취소 성공"));

        // then
        savedReview = reviewRepository.findById(review.getId()).orElseThrow();
        assertThat(savedReview.getReviewLikes()).isEmpty();
        assertThat(savedReview.getLikeCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("리뷰 목록 조회시 해당 사용자가 좋아요 누른 리뷰는 liked=true 로 표시된다")
    void getReviews_marksLikedTrue_whenUserLiked() throws Exception {
        // given
        User user = userRepository.save(User.builder()
                .studentId("21011111").nickname("로그인유저").build());

        Category category = categoryRepository.save(new Category("식당"));
        Place place = createPlaceFixture("한식집", "세종대", "url", category, List.of());
        placeRepository.save(place);

        User writer = userRepository.save(User.builder()
                .studentId("21011112").nickname("작성자").build());
        Review review = createReview(place, writer, "좋아요눌림", 5, List.of(), List.of());
        reviewRepository.save(review);

        // when
        mockMvc.perform(post("/api/places/{placeId}/reviews/{reviewId}/likes", place.getId(), review.getId())
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk());

        // then: 목록 조회시 liked=true 로 표시
        mockMvc.perform(get("/api/places/{placeId}/reviews", place.getId())
                        .header("Authorization", "Bearer test-token")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("리뷰 목록 조회 성공"))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].reviewId").value(review.getId()))
                .andExpect(jsonPath("$.data[0].liked").value(true))
                .andExpect(jsonPath("$.data[0].likeCount").value(1));
    }

    @Test
    @DisplayName("리뷰 목록 조회시 해당 사용자가 좋아요 누르지 않은 리뷰는 liked=false 로 표시된다")
    void getReviews_marksLikedFalse_whenUserNotLiked() throws Exception {
        // given
        User user = userRepository.save(User.builder()
                .studentId("21011111").nickname("닉네임").build());

        Category category = categoryRepository.save(new Category("식당"));
        Place place = createPlaceFixture("분식집", "세종대", "url", category, List.of());
        placeRepository.save(place);

        User writer = userRepository.save(User.builder()
                .studentId("21011112").nickname("작성자").build());
        Review review = createReview(place, writer, "좋아요안눌림", 4, List.of(), List.of());
        reviewRepository.save(review);

        // then
        mockMvc.perform(get("/api/places/{placeId}/reviews", place.getId())
                        .header("Authorization", "Bearer test-token")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("리뷰 목록 조회 성공"))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].reviewId").value(review.getId()))
                .andExpect(jsonPath("$.data[0].liked").value(false))
                .andExpect(jsonPath("$.data[0].likeCount").value(0));
    }

    private Place setupReviewListData() {

        User user1 = userRepository.save(User.builder().studentId("21011111").nickname("닉네임").build());
        User user2 = userRepository.save(User.builder().studentId("21011112").nickname("닉네임2").build());

        Category category = categoryRepository.save(new Category("식당"));

        Tag tag1 = tagRepository.save(new Tag("맛집"));
        Tag tag2 = tagRepository.save(new Tag("가성비"));

        Place place = createPlaceFixture("식당2", "주소2", "url2", category, List.of(tag1, tag2));
        placeRepository.save(place);

        Review review1 = createReview(place, user1, "맛있어요", 5, List.of("url1", "url2"), List.of(tag1, tag2));
        Review review2 = createReview(place, user2, "별로에요", 2, List.of("url3"), List.of(tag2));
        reviewRepository.saveAll(List.of(review1, review2));

        return place;
    }

    private Place createPlaceFixture(String name, String address, String url, Category category, List<Tag> tags) {
        Place place = Place.builder().name(name).address(address).mainImageUrl(url)
                .mapLinks(new MapLinks("a", "b", "c")).build();
        place.addCategory(category);
        tags.forEach(place::addTag);
        return place;
    }

    private Review createReview(Place place, User user, String content, int rating, List<String> images,
                                List<Tag> tags) {
        Review review = Review.builder().place(place).user(user).content(content).rating(rating).build();
        images.forEach(review::addImage);
        tags.forEach(review::addTag);
        return review;
    }
}
