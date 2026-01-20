package org.example.sejonglifebe.review.admin;

import org.example.sejonglifebe.auth.AuthUser;
import org.example.sejonglifebe.category.Category;
import org.example.sejonglifebe.category.CategoryRepository;
import org.example.sejonglifebe.common.jwt.JwtTokenProvider;
import org.example.sejonglifebe.place.PlaceRepository;
import org.example.sejonglifebe.place.entity.MapLinks;
import org.example.sejonglifebe.place.entity.Place;
import org.example.sejonglifebe.review.Review;
import org.example.sejonglifebe.review.ReviewRepository;
import org.example.sejonglifebe.user.Role;
import org.example.sejonglifebe.user.User;
import org.example.sejonglifebe.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class AdminReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Nested
    @DisplayName("관리자 리뷰 목록 조회 API")
    class GetAdminReviewsTest {

        @Test
        @DisplayName("ADMIN 사용자가 조회하면 성공한다")
        void admin_access_success() throws Exception {
            // given
            User adminUser = User.builder()
                    .studentId("21011111")
                    .nickname("관리자")
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(adminUser);

            given(jwtTokenProvider.validateAndGetAuthUser(anyString()))
                    .willReturn(new AuthUser("21011111", Role.ADMIN));

            // 테스트용 리뷰 데이터 생성
            Category category = categoryRepository.save(new Category("식당"));
            Place place = Place.builder()
                    .name("맛집")
                    .address("주소")
                    .mainImageUrl("url")
                    .mapLinks(new MapLinks("a", "b", "c"))
                    .build();
            place.addCategory(category);
            placeRepository.save(place);

            Review review = Review.builder()
                    .place(place)
                    .user(adminUser)
                    .content("맛있어요")
                    .rating(5)
                    .build();
            reviewRepository.save(review);

            // when & then
            mockMvc.perform(get("/api/admin/reviews")
                            .header("Authorization", "Bearer admin-token")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("리뷰 로그 목록 조회 성공"))
                    .andDo(print());
        }

        @Test
        @DisplayName("USER 사용자가 조회하면 403 Forbidden을 반환한다")
        void user_access_forbidden() throws Exception {
            // given
            User normalUser = User.builder()
                    .studentId("21011111")
                    .nickname("일반유저")
                    .role(Role.USER)
                    .build();
            userRepository.save(normalUser);

            given(jwtTokenProvider.validateAndGetAuthUser(anyString()))
                    .willReturn(new AuthUser("21011111", Role.USER));

            // when & then
            mockMvc.perform(get("/api/admin/reviews")
                            .header("Authorization", "Bearer user-token")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"))
                    .andDo(print());
        }

        @Test
        @DisplayName("비로그인 사용자가 조회하면 401 Unauthorized를 반환한다")
        void guest_access_unauthorized() throws Exception {
            // when & then
            mockMvc.perform(get("/api/admin/reviews")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized())
                    .andDo(print());
        }
    }
}
