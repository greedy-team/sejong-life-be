package org.example.sejonglifebe.review.mypage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.sejonglifebe.auth.AuthUser;
import org.example.sejonglifebe.category.Category;
import org.example.sejonglifebe.category.CategoryRepository;
import org.example.sejonglifebe.common.jwt.JwtTokenProvider;
import org.example.sejonglifebe.place.PlaceRepository;
import org.example.sejonglifebe.place.entity.MapLinks;
import org.example.sejonglifebe.place.entity.Place;
import org.example.sejonglifebe.review.Review;
import org.example.sejonglifebe.review.ReviewRepository;
import org.example.sejonglifebe.s3.S3Service;
import org.example.sejonglifebe.tag.Tag;
import org.example.sejonglifebe.tag.TagRepository;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class MyPageReviewControllerTest {

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
    private S3Service s3Service;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        given(jwtTokenProvider.validateAndGetAuthUser(anyString()))
                .willReturn(new AuthUser("21011111", Role.USER));
    }

    @Test
    @DisplayName("마이페이지 리뷰 목록이 정상적으로 조회된다")
    void getMyPageReviews_success() throws Exception {
        // given
        User user = userRepository.save(User.builder().studentId("21011111").nickname("닉네임").build());

        Category category = categoryRepository.save(new Category("식당"));
        Tag tag1 = tagRepository.save(new Tag("맛집"));
        Tag tag2 = tagRepository.save(new Tag("가성비"));

        Place place1 = createPlaceFixture("또래끼리", "세종대 후문", "url1", category);
        Place place2 = createPlaceFixture("스타벅스", "세종대 정문", "url2", category);
        placeRepository.save(place1);
        placeRepository.save(place2);

        Review review1 = createReview(place1, user, "맛있어요", 5, List.of("image1.jpg"), List.of(tag1));
        Review review2 = createReview(place2, user, "커피가 좋아요", 4, List.of(), List.of(tag2));
        reviewRepository.save(review1);
        reviewRepository.save(review2);

        // when & then
        mockMvc.perform(get("/api/mypage/reviews")
                        .header("Authorization", "Bearer test-token")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("마이페이지 리뷰 목록 조회 성공"))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].content").value("커피가 좋아요"))
                .andExpect(jsonPath("$.data[0].isAuthor").value(true))
                .andExpect(jsonPath("$.data[0].place.placeId").value(place2.getId()))
                .andExpect(jsonPath("$.data[0].place.placeName").value("스타벅스"))
                .andExpect(jsonPath("$.data[1].content").value("맛있어요"))
                .andExpect(jsonPath("$.data[1].isAuthor").value(true))
                .andExpect(jsonPath("$.data[1].place.placeId").value(place1.getId()))
                .andExpect(jsonPath("$.data[1].place.placeName").value("또래끼리"))
                .andDo(print());
    }

    @Test
    @DisplayName("마이페이지 리뷰 목록 조회 시 작성한 리뷰가 없으면 빈 배열을 반환한다")
    void getMyPageReviews_emptyList() throws Exception {
        // given
        userRepository.save(User.builder().studentId("21011111").nickname("닉네임").build());

        // when & then
        mockMvc.perform(get("/api/mypage/reviews")
                        .header("Authorization", "Bearer test-token")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("마이페이지 리뷰 목록 조회 성공"))
                .andExpect(jsonPath("$.data", hasSize(0)))
                .andDo(print());
    }

    @Test
    @DisplayName("마이페이지 리뷰 삭제가 정상적으로 처리된다")
    void deleteMyPageReview_success() throws Exception {
        // given
        User user = userRepository.save(User.builder().studentId("21011111").nickname("닉네임").build());

        Category category = categoryRepository.save(new Category("식당"));
        Place place = createPlaceFixture("또래끼리", "세종대 후문", "url", category);
        placeRepository.save(place);

        Review review = createReview(place, user, "맛있어요", 5, List.of(), List.of());
        reviewRepository.save(review);

        // when & then
        mockMvc.perform(delete("/api/mypage/reviews/{reviewId}", review.getId())
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("마이페이지 리뷰 삭제 성공"))
                .andDo(print());

        // then
        assertThat(reviewRepository.findById(review.getId())).isEmpty();
    }

    @Test
    @DisplayName("마이페이지 리뷰 삭제 시 작성자가 아니면 예외를 반환한다")
    void deleteMyPageReview_permissionDenied() throws Exception {
        // given
        User author = userRepository.save(User.builder().studentId("22222222").nickname("작성자").build());
        userRepository.save(User.builder().studentId("21011111").nickname("요청자").build());

        Category category = categoryRepository.save(new Category("식당"));
        Place place = createPlaceFixture("또래끼리", "세종대 후문", "url", category);
        placeRepository.save(place);

        Review review = createReview(place, author, "맛있어요", 5, List.of(), List.of());
        reviewRepository.save(review);

        // when & then
        mockMvc.perform(delete("/api/mypage/reviews/{reviewId}", review.getId())
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("PERMISSION_DENIED"))
                .andDo(print());

        // then
        assertThat(reviewRepository.findById(review.getId())).isPresent();
    }

    @Test
    @DisplayName("마이페이지 리뷰 삭제 시 존재하지 않는 리뷰면 예외를 반환한다")
    void deleteMyPageReview_reviewNotFound() throws Exception {
        // given
        userRepository.save(User.builder().studentId("21011111").nickname("닉네임").build());
        Long nonExistentReviewId = 999L;

        // when & then
        mockMvc.perform(delete("/api/mypage/reviews/{reviewId}", nonExistentReviewId)
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("REVIEW_NOT_FOUND"))
                .andDo(print());
    }

    private Place createPlaceFixture(String name, String address, String url, Category category) {
        Place place = Place.builder()
                .name(name)
                .address(address)
                .mainImageUrl(url)
                .mapLinks(new MapLinks("a", "b", "c"))
                .build();
        place.addCategory(category);
        return place;
    }

    private Review createReview(Place place, User user, String content, int rating, List<String> images, List<Tag> tags) {
        Review review = Review.builder()
                .place(place)
                .user(user)
                .content(content)
                .rating(rating)
                .build();
        ReflectionTestUtils.setField(review, "createdAt", LocalDateTime.now());
        images.forEach(review::addImage);
        tags.forEach(review::addTag);
        return review;
    }
}
