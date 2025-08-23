package org.example.sejonglifebe.review;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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

        Review review1 = createReview(place, "맛있어요", 5.0, List.of("url1", "url2"), List.of(tag1, tag2));
        Review review2 = createReview(place, "별로에요", 2.0, List.of("url3"), List.of(tag2));

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

    private Review createReview(Place place, String content, Double rating, List<String> images, List<Tag> tags) {
        Review review = Review.builder()
                .place(place)
                .content(content)
                .rating(rating)
                .build();

        images.forEach(image -> review.addImage(place,image));
        tags.forEach(review::addTag);
        return review;
    }

    @Test
    @DisplayName("api 조회")
    public void apiTest() throws Exception
    {
        mockMvc.perform(get("/api/places/1/reviews").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

}
