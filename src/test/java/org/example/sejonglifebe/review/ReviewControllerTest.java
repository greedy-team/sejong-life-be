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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        given(jwtTokenProvider.validateAndGetAuthUser(anyString()))
                .willReturn(new AuthUser("21011111"));

    }

    @Test
    @DisplayName("리뷰가 정상적으로 생성된다")
    void createReview_success() throws Exception {

        // given
        User user = User.builder()
                .studentId("21011111")
                .name("이름")
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
        placeRepository.save(place);

        ReviewRequest request = new ReviewRequest(
                3,
                "맛있음",
                List.of(tag1.getId(), tag2.getId())
        );

        MockMultipartFile reviewPart = new MockMultipartFile(
                "review",
                "",
                "application/json",
                new ObjectMapper().writeValueAsBytes(request)
        );

        MockMultipartFile imagePart = new MockMultipartFile(
                "images",
                "file1.jpg",
                "image/jpeg",
                "test".getBytes()
        );

        // when & then
        mockMvc.perform(multipart("/api/places/{placeId}/reviews", place.getId())
                        .file(reviewPart)
                        .file(imagePart)
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("리뷰 작성 성공"));
    }

    @Test
    @DisplayName("리뷰에서 5번 이상 언급된 태그는 장소 태그에 추가된다.")
    void addFrequentlyUsedTagToPlace_whenTagMentionedFiveTimes() throws Exception {

        // given
        User user = User.builder()
                .studentId("21011111")
                .name("이름")
                .nickname("닉네임")
                .build();
        userRepository.save(user);

        Category category = categoryRepository.save(new Category("식당"));
        Tag tag = tagRepository.save(new Tag("가성비"));

        Place place = Place.builder()
                .name("또래끼리")
                .address("세종대 후문")
                .mapLinks(new MapLinks("네이버", "카카오", "구글"))
                .build();
        place.addCategory(category);
        placeRepository.save(place);

        ReviewRequest request = new ReviewRequest(
                5,
                "맛있음",
                List.of(tag.getId())
        );

        MockMultipartFile reviewPart = new MockMultipartFile(
                "review", "", "application/json",
                new ObjectMapper().writeValueAsBytes(request)
        );

        // when: 동일한 태그가 포함된 리뷰 5개 작성
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(multipart("/api/places/{placeId}/reviews", place.getId())
                            .file(reviewPart)
                            .header("Authorization", "Bearer test-token"))
                    .andExpect(status().isCreated());
        }

        // then: placeTags 에 tag 가 추가되었는지 검증
        Place savedPlace = placeRepository.findById(place.getId())
                .orElseThrow();

        assertThat(savedPlace.getPlaceTags())
                .hasSize(1)
                .extracting(pt -> pt.getTag().getName())
                .contains("가성비");
    }
}
