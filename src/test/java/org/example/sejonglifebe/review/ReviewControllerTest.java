package org.example.sejonglifebe.review;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.example.sejonglifebe.category.Category;
import org.example.sejonglifebe.category.CategoryRepository;
import org.example.sejonglifebe.place.PlaceRepository;
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

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class ReviewControllerTest {

    private static final Long NON_EXISTENT_ID = 999L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @BeforeEach
    void setUp() {
        placeRepository.deleteAll();
        reviewRepository.deleteAll();
        tagRepository.deleteAll();
        categoryRepository.deleteAll();

        Category category = new Category("식당");
        categoryRepository.saveAll(List.of(category));

        Tag tag1 = new Tag("맛집");
        Tag tag2 = new Tag("가성비");
        tagRepository.saveAll(List.of(tag1, tag2));

        Place place = createPlace("식당2", "주소2", "url2", category, List.of(tag1, tag2));
        placeRepository.save(place);

        Review review1 = createReview(place, "맛있어요", 5L, List.of("url1", "url2"), List.of(tag1, tag2));
        Review review2 = createReview(place, "별로에요", 2L, List.of("url3"), List.of(tag2));

        reviewRepository.saveAll(List.of(review1, review2));
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

    private Review createReview(Place place, String content, Long rating, List<String> images, List<Tag> tags) {
        Review review = Review.builder()
                .place(place)
                .content(content)
                .rating(rating)
                .build();

        images.forEach(image -> review.addImage(place, image));
        tags.forEach(review::addTag);
        return review;
    }

    @Test
    @DisplayName("장소에 대한 리뷰 목록을 성공적으로 조회한다")
    void getReviews_success() throws Exception {
        Place place = placeRepository.findAll().get(0);

        mockMvc.perform(get("/api/places/{placeId}/reviews", place.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("리뷰 목록 조회 성공"))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].reviewId").value(notNullValue()))
                .andExpect(jsonPath("$.data[0].content").value("맛있어요"))
                .andExpect(jsonPath("$.data[0].rating").value(5))
                .andExpect(jsonPath("$.data[0].likeCount").value(0))
                .andExpect(jsonPath("$.data[0].createdAt").exists())
                .andExpect(jsonPath("$.data[0].images", hasSize(2)))
                .andExpect(jsonPath("$.data[0].tags[*].tagName", containsInAnyOrder("맛집", "가성비")))
                .andExpect(jsonPath("$.data[1].reviewId").value(notNullValue()))
                .andExpect(jsonPath("$.data[1].content").value("별로에요"))
                .andExpect(jsonPath("$.data[1].rating").value(2))
                .andExpect(jsonPath("$.data[1].images", hasSize(1)))
                .andExpect(jsonPath("$.data[1].tags[*].tagName", containsInAnyOrder("가성비")))
                .andDo(print());
    }

    @Test
    @DisplayName("존재하지 않는 장소의 리뷰 목록 조회 시 예외를 반환한다")
    void getReviews_placeNotFound_fail() throws Exception {
        mockMvc.perform(get("/api/places/{placeId}/reviews", NON_EXISTENT_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PLACE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 장소입니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("장소에 대한 리뷰 요약 정보를 성공적으로 조회한다")
    void getReviewSummary_success() throws Exception {
        Place place = placeRepository.findAll().get(0);

        mockMvc.perform(get("/api/places/{placeId}/reviews/summary", place.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("리뷰 요약 정보 조회 성공"))
                .andExpect(jsonPath("$.data.reviewCount").value(2))
                .andExpect(jsonPath("$.data.averageRate").value(3.5))
                .andExpect(jsonPath("$.data.ratingDistribution").isMap())
                .andExpect(jsonPath("$.data.ratingDistribution['5']").value(1))
                .andExpect(jsonPath("$.data.ratingDistribution['2']").value(1))
                .andExpect(jsonPath("$.data.ratingDistribution['1']").value(0))
                .andDo(print());
    }

    @Test
    @DisplayName("존재하지 않는 장소의 리뷰 요약 정보 조회 시 예외를 반환한다")
    void getReviewSummary_placeNotFound_fail() throws Exception {
        mockMvc.perform(get("/api/places/{placeId}/reviews/summary", NON_EXISTENT_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PLACE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 장소입니다."))
                .andDo(print());
    }
}

