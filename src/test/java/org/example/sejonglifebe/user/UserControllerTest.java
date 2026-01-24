package org.example.sejonglifebe.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.sejonglifebe.auth.AuthUser;
import org.example.sejonglifebe.auth.PortalStudentInfo;
import org.example.sejonglifebe.category.Category;
import org.example.sejonglifebe.category.CategoryRepository;
import org.example.sejonglifebe.common.jwt.JwtTokenProvider;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.place.PlaceRepository;
import org.example.sejonglifebe.place.entity.MapLinks;
import org.example.sejonglifebe.place.entity.Place;
import org.example.sejonglifebe.review.Review;
import org.example.sejonglifebe.review.ReviewRepository;
import org.example.sejonglifebe.s3.S3Service;
import org.example.sejonglifebe.user.dto.SignUpRequest;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private S3Service s3Service;

    @BeforeEach
    void setUp() {
        given(jwtTokenProvider.validateAndGetAuthUser(anyString()))
                .willReturn(new AuthUser("21011111", Role.USER));
    }

    @Test
    @DisplayName("회원가입이 정상적으로 된다")
    void shouldSignupSuccessfully_whenRequestIsValid() throws Exception {
        //given
        given(jwtTokenProvider.validateAndGetPortalInfo(anyString()))
                .willReturn(PortalStudentInfo.builder().studentId("21011111").name("새로운 사용자").build());
        SignUpRequest request = SignUpRequest.builder()
                .studentId("21011111")
                .name("새로운 사용자")
                .nickname("닉네임")
                .build();

        //then
        mockMvc.perform(post("/api/users/signup")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("회원가입 및 로그인 성공"));
    }

    @Test
    @DisplayName("사용자의 정보가 일치하지 않으면 예외를 던진다")
    void shouldThrowException_whenUserInfoDoesNotMatchToken() throws Exception {
        //given
        given(jwtTokenProvider.validateAndGetPortalInfo(anyString()))
                .willReturn(PortalStudentInfo.builder().studentId("99999999").name("일치하지 않는 사용자").build());
        SignUpRequest request = SignUpRequest.builder()
                .studentId("21011111")
                .name("새로운 사용자")
                .nickname("닉네임")
                .build();

        //then
        mockMvc.perform(post("/api/users/signup")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("유효하지 않은 토큰입니다."));
    }

    @Test
    @DisplayName("유효하지 않은 토큰이면 예외를 던진다")
    void shouldThrowException_whenAuthorizationHeaderIsInvalid() throws Exception {
        //given
        SignUpRequest request = SignUpRequest.builder()
                .studentId("21011111")
                .name("새로운 사용자")
                .nickname("닉네임")
                .build();

        //then
        mockMvc.perform(post("/api/users/signup")
                        .header("Authorization", "유효하지 않은 토큰")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("유효하지 않은 Authorization 헤더입니다."));
    }

    @Test
    @DisplayName("사용자가 잘못된 형식의 닉네임을 입력하면 예외를 반환한다")
    void shouldThrowException_whenNicknameIsInvalid() throws Exception {
        //given
        SignUpRequest request = SignUpRequest.builder()
                .studentId("21011111")
                .name("새로운 사용자")
                .nickname("잘못된닉네임!@#")
                .build();

        //then
        mockMvc.perform(post("/api/users/signup")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("닉네임은 한글, 영문, 숫자만 사용 가능합니다."));
    }

    @Test
    @DisplayName("1자 이하의 닉네임을 입력하면 예외를 던진다")
    void shouldThrowException_whenNicknameIsTooShort() throws Exception {
        //given
        SignUpRequest request = SignUpRequest.builder()
                .studentId("21011111")
                .name("새로운 사용자")
                .nickname("a")
                .build();

        //tehn
        mockMvc.perform(post("/api/users/signup")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("닉네임은 2자 이상 10자 이하로 입력해주세요."));
    }

    @Test
    @DisplayName("11자 이상의 닉네임을 입력하면 예외를 던진다")
    void shouldThrowException_whenNicknameIsTooLong() throws Exception {
        //given
        SignUpRequest request = SignUpRequest.builder()
                .studentId("21011111")
                .name("새로운 사용자")
                .nickname("길이가11자인닉네임임")
                .build();

        //tehn
        mockMvc.perform(post("/api/users/signup")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("닉네임은 2자 이상 10자 이하로 입력해주세요."));
    }

    @Test
    @DisplayName("헤더가 누락되면 예외를 던진다")
    void shouldThrowException_whenAuthorizationHeaderIsMissing() throws Exception {
        //given
        SignUpRequest request = SignUpRequest.builder()
                .studentId("21011111")
                .name("새로운 사용자")
                .nickname("닉네임")
                .build();

        //tehn
        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_AUTH_HEADER.getErrorMessage()));
    }

    @Test
    @DisplayName("회원 탈퇴가 정상적으로 처리된다")
    void deleteUser_success() throws Exception {
        // given
        User user = userRepository.save(User.builder()
                .studentId("21011111")
                .nickname("탈퇴할사용자")
                .build());

        Category category = categoryRepository.save(new Category("식당"));
        Place place = createPlaceFixture("테스트장소", "주소", "url", category);
        placeRepository.save(place);

        Review review = createReview(place, user, "테스트 리뷰", 5);
        reviewRepository.save(review);

        // when & then
        mockMvc.perform(delete("/api/users")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원 탈퇴 성공"))
                .andDo(print());

        // then
        assertThat(userRepository.findById(user.getId())).isEmpty();
        assertThat(reviewRepository.findById(review.getId())).isEmpty();
    }

    @Test
    @DisplayName("리뷰가 없는 사용자도 탈퇴가 가능하다")
    void deleteUser_noReviews() throws Exception {
        // given
        User user = userRepository.save(User.builder()
                .studentId("21011111")
                .nickname("리뷰없는사용자")
                .build());

        // when & then
        mockMvc.perform(delete("/api/users")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원 탈퇴 성공"))
                .andDo(print());

        // then
        assertThat(userRepository.findById(user.getId())).isEmpty();
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

    private Review createReview(Place place, User user, String content, int rating) {
        Review review = Review.builder()
                .place(place)
                .user(user)
                .content(content)
                .rating(rating)
                .build();
        ReflectionTestUtils.setField(review, "createdAt", LocalDateTime.now());
        return review;
    }
}
