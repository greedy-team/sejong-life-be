package org.example.sejonglifebe.tag;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.sejonglifebe.category.Category;
import org.example.sejonglifebe.category.CategoryRepository;
import org.example.sejonglifebe.place.PlaceRepository;
import org.example.sejonglifebe.place.entity.MapLinks;
import org.example.sejonglifebe.place.entity.Place;
import org.example.sejonglifebe.review.Review;
import org.example.sejonglifebe.user.User;
import org.example.sejonglifebe.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class TagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    PlaceRepository placeRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    UserRepository userRepository;

    @PersistenceContext
    EntityManager em;

    @Test
    @DisplayName("전체 태그 목록 조회 성공 테스트")
    void getAllTags_success() throws Exception {

        Tag tag1 = new Tag("맛집");
        Tag tag2 = new Tag("분위기 좋은");
        tagRepository.saveAll(List.of(tag1, tag2));
        em.flush();

        mockMvc.perform(get("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk()) // 1. HTTP 상태 코드 확인
                .andExpect(jsonPath("$.message").value("전체 태그 목록 조회 성공")) // 2. 응답 메시지 확인
                .andExpect(jsonPath("$.data").isArray()) // 3. data 필드 배열인지 확인
                .andExpect(jsonPath("$.data[*].tagName", containsInAnyOrder("맛집", "분위기 좋은")));// 4. 첫 번째 태그이름 확인
    }

    @Test
    @DisplayName("카테고리별 태그를 조회 후 반환한다")
    void getTagsByCategory_success() throws Exception {
        Category category = categoryRepository.save(new Category("식당"));
        Tag tag1 = tagRepository.save(new Tag("가성비"));
        Tag tag2 = tagRepository.save(new Tag("집밥"));

        Place place = Place.builder()
                .name("또래끼리")
                .address("세종대 후문")
                .mapLinks(new MapLinks("네이버1", "카카오1", "구글1"))
                .build();

        place.addTag(tag1);
        place.addTag(tag2);
        place.addCategory(category);
        placeRepository.save(place);

        mockMvc.perform(get("/api/tags")
                        .param("categoryId", String.valueOf(category.getId()))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("전체 태그 목록 조회 성공"))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[*].tagName",
                        containsInAnyOrder("가성비", "집밥")))
                .andExpect(jsonPath("$.data[*].tagId",
                        everyItem(notNullValue())));
    }

    @Test
    @DisplayName("카테고리에 해당하는 태그가 없으면 빈 배열을 반환한다")
    void getTagsByCategory_emptyResult() throws Exception {
        Category emptyCategory = categoryRepository.save(new Category("편의점"));

        mockMvc.perform(get("/api/tags")
                        .param("categoryId", String.valueOf(emptyCategory.getId()))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("전체 태그 목록 조회 성공"))
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    @DisplayName("존재하지 않는 카테고리이면 예외를 던진다")
    void getTagsByCategory_notFoundCategory_throwsException() throws Exception {
        mockMvc.perform(get("/api/tags")
                        .param("categoryId", "999999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("CATEGORY_NOT_FOUND"))
                .andExpect(jsonPath("$.message", containsString("존재하지 않는 카테고리입니다.")));
    }

    @Test
    @DisplayName("카테고리 파라미터 타입이 맞지 않으면 예외를 던진다")
    void getTagsByCategory_invalidCategoryIdType_throwsException() throws Exception {
        mockMvc.perform(get("/api/tags")
                        .param("categoryId", "abc")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("TYPE_MISMATCH"))
                .andExpect(jsonPath("$.message", containsString("파라미터 형식이 올바르지 않습니다.")));
    }

    @Test
    @DisplayName("카테고리에서 자주 쓰인 태그를 우선적으로 반환한다")
    void getFrequentlyUsedTagsByCategory_prioritized() throws Exception {
        // given
        User user = User.builder()
                .studentId("21011111")
                .nickname("닉네임")
                .build();
        userRepository.save(user);

        Category category = categoryRepository.save(new Category("식당"));
        Tag tag1 = tagRepository.save(new Tag("가성비"));
        Tag tag2 = tagRepository.save(new Tag("집밥"));

        Place place = Place.builder()
                .name("또래끼리")
                .address("세종대 후문")
                .mapLinks(new MapLinks("네이버", "카카오", "구글"))
                .build();
        place.addCategory(category);
        place.addTag(tag1);
        place.addTag(tag2);
        placeRepository.save(place);

        Review review1 = Review.createReview(place, user,5, "좋아요");
        Review review2 = Review.createReview(place, user,4, "괜찮음");
        Review review3 = Review.createReview(place, user,3, "보통");
        review1.addTag(tag1);
        review2.addTag(tag1);
        review3.addTag(tag2);

        em.persist(review1);
        em.persist(review2);
        em.persist(review3);
        em.flush();
        em.clear();

        // when & then
        mockMvc.perform(get("/api/tags/recommended")
                        .param("categoryId", String.valueOf(category.getId()))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("추천 태그 목록 조회 성공"))
                // 태그 개수는 2개
                .andExpect(jsonPath("$.data", hasSize(2)))
                // 첫 번째 태그가 tag1("가성비")인지 확인 (더 많이 쓰임)
                .andExpect(jsonPath("$.data[0].tagName").value("가성비"))
                // 두 번째 태그가 tag2("집밥")인지 확인
                .andExpect(jsonPath("$.data[1].tagName").value("집밥"));
    }
}
