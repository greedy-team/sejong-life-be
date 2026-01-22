package org.example.sejonglifebe.place;

import org.example.sejonglifebe.auth.AuthUser;
import org.example.sejonglifebe.user.Role;
import org.example.sejonglifebe.category.Category;
import org.example.sejonglifebe.category.CategoryRepository;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.example.sejonglifebe.place.dto.PlaceRequest;
import org.example.sejonglifebe.place.dto.PlaceResponse;
import org.example.sejonglifebe.place.dto.PlaceSearchConditions;
import org.example.sejonglifebe.place.entity.Place;
import org.example.sejonglifebe.place.view.PlaceViewLogRepository;
import org.example.sejonglifebe.s3.S3Service;
import org.example.sejonglifebe.tag.Tag;
import org.example.sejonglifebe.tag.TagRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;
import java.util.Optional;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PlaceServiceTest {

    @Mock
    private PlaceRepository placeRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private PlaceViewLogRepository placeViewLogRepository;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private PlaceService placeService;

    @Nested
    @DisplayName("카테고리,태그 기반 장소 조회 테스트")
    class GetPlacesFilteredTest {

        @Test
        @DisplayName("존재하지 않는 태그 이름이 포함되면 TAG_NOT_FOUND 예외를 던진다")
        void getPlaces_tagNotFound() {
            // given
            PlaceSearchConditions conditions = new PlaceSearchConditions(List.of("존재X"), "전체", null);

            given(tagRepository.findByNameIn(anyList()))
                    .willReturn(List.of());

            // when/then
            assertThatThrownBy(() -> placeService.getPlaceByConditions(conditions))
                    .isInstanceOf(SejongLifeException.class)
                    .hasMessage(ErrorCode.TAG_NOT_FOUND.getErrorMessage());

            verify(tagRepository).findByNameIn(List.of("존재X"));
        }

        @Test
        @DisplayName("카테고리 = 전체 && 태그 없음 → 모든 장소를 조회한다")
        void getPlaces_allCategory_noTags() {
            // given
            PlaceSearchConditions conditions = new PlaceSearchConditions(List.of(), "전체", null);
            Place place1 = Place.builder().name("장소1").build();
            Place place2 = Place.builder().name("장소2").build();

            given(tagRepository.findByNameIn(List.of())).willReturn(List.of());
            given(placeRepository.getPlacesByConditions(null, List.of(), null))
                    .willReturn(List.of(place1, place2));

            // when
            List<PlaceResponse> result = placeService.getPlaceByConditions(conditions);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).placeName()).isEqualTo("장소1");
            assertThat(result.get(1).placeName()).isEqualTo("장소2");
        }

        @Test
        @DisplayName("카테고리 = 전체 && 태그 있음 → 해당 태그를 가진 장소를 조회한다")
        void getPlaces_allCategory_withTags() {
            // given
            Tag tag = new Tag("가성비");
            PlaceSearchConditions conditions = new PlaceSearchConditions(List.of("가성비"), "전체", null);
            Place place1 = Place.builder().name("가성비 장소").build();

            given(tagRepository.findByNameIn(conditions.tags())).willReturn(List.of(tag));
            given(placeRepository.getPlacesByConditions(null, List.of(tag), null))
                    .willReturn(List.of(place1));

            // when
            List<PlaceResponse> result = placeService.getPlaceByConditions(conditions);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).placeName()).isEqualTo("가성비 장소");
        }

        @Test
        @DisplayName("카테고리 존재하지 않으면 CATEGORY_NOT_FOUND 예외를 던진다")
        void getPlaces_categoryNotFound() {
            // given
            PlaceSearchConditions conditions = new PlaceSearchConditions(List.of(), "맛집", null);

            given(tagRepository.findByNameIn(List.of())).willReturn(List.of());
            given(categoryRepository.findByName("맛집")).willReturn(Optional.empty());

            // then
            assertThatThrownBy(() -> placeService.getPlaceByConditions(conditions))
                    .isInstanceOf(SejongLifeException.class)
                    .hasMessage(ErrorCode.CATEGORY_NOT_FOUND.getErrorMessage());
        }

        @Test
        @DisplayName("카테고리 + 태그 없음 → 해당 카테고리의 장소를 조회한다")
        void getPlaces_selectedCategory_noTags() {
            // given
            Category category = new Category("맛집");
            PlaceSearchConditions conditions = new PlaceSearchConditions(List.of(), "맛집", null);
            Place place1 = Place.builder().name("맛집1").build();
            Place place2 = Place.builder().name("맛집2").build();

            given(tagRepository.findByNameIn(List.of())).willReturn(List.of());
            given(categoryRepository.findByName("맛집")).willReturn(Optional.of(category));
            given(placeRepository.getPlacesByConditions(category, List.of(), null))
                    .willReturn(List.of(place1, place2));

            // when
            List<PlaceResponse> result = placeService.getPlaceByConditions(conditions);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).placeName()).isEqualTo("맛집1");
            assertThat(result.get(1).placeName()).isEqualTo("맛집2");
        }

        @Test
        @DisplayName("카테고리 + 태그 있음 → 해당 카테고리와 태그를 모두 만족하는 장소를 조회한다")
        void getPlaces_selectedCategory_withTags() {
            // given
            Category category = new Category("맛집");
            Tag tag = new Tag("가성비");
            PlaceSearchConditions conditions = new PlaceSearchConditions(List.of("가성비"), "맛집", null);
            Place place1 = Place.builder().name("가성비 맛집").build();

            given(tagRepository.findByNameIn(conditions.tags())).willReturn(List.of(tag));
            given(categoryRepository.findByName("맛집")).willReturn(Optional.of(category));
            given(placeRepository.getPlacesByConditions(category, List.of(tag), null))
                    .willReturn(List.of(place1));

            // when
            List<PlaceResponse> result = placeService.getPlaceByConditions(conditions);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).placeName()).isEqualTo("가성비 맛집");
        }

        @Test
        @DisplayName("키워드만 입력 → 장소명에 키워드가 포함된 장소를 조회한다")
        void getPlaces_withKeywordOnly() {
            // given
            PlaceSearchConditions conditions = new PlaceSearchConditions(List.of(), "전체", "카페");
            Place place1 = Place.builder().name("스타벅스 카페").build();
            Place place2 = Place.builder().name("투썸 카페").build();

            given(tagRepository.findByNameIn(List.of())).willReturn(List.of());
            given(placeRepository.getPlacesByConditions(null, List.of(), "카페"))
                    .willReturn(List.of(place1, place2));

            // when
            List<PlaceResponse> result = placeService.getPlaceByConditions(conditions);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).placeName()).contains("카페");
            assertThat(result.get(1).placeName()).contains("카페");
        }

        @Test
        @DisplayName("카테고리 + 키워드 → 해당 카테고리에서 키워드가 포함된 장소를 조회한다")
        void getPlaces_withCategoryAndKeyword() {
            // given
            Category category = new Category("맛집");
            PlaceSearchConditions conditions = new PlaceSearchConditions(List.of(), "맛집", "치킨");
            Place place1 = Place.builder().name("BHC 치킨").build();

            given(tagRepository.findByNameIn(List.of())).willReturn(List.of());
            given(categoryRepository.findByName("맛집")).willReturn(Optional.of(category));
            given(placeRepository.getPlacesByConditions(category, List.of(), "치킨"))
                    .willReturn(List.of(place1));

            // when
            List<PlaceResponse> result = placeService.getPlaceByConditions(conditions);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).placeName()).isEqualTo("BHC 치킨");
        }

        @Test
        @DisplayName("태그 + 키워드 → 해당 태그를 가지고 키워드가 포함된 장소를 조회한다")
        void getPlaces_withTagAndKeyword() {
            // given
            Tag tag = new Tag("가성비");
            PlaceSearchConditions conditions = new PlaceSearchConditions(List.of("가성비"), "전체", "피자");
            Place place1 = Place.builder().name("가성비 피자").build();

            given(tagRepository.findByNameIn(conditions.tags())).willReturn(List.of(tag));
            given(placeRepository.getPlacesByConditions(null, List.of(tag), "피자"))
                    .willReturn(List.of(place1));

            // when
            List<PlaceResponse> result = placeService.getPlaceByConditions(conditions);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).placeName()).contains("피자");
        }

        @Test
        @DisplayName("카테고리 + 태그 + 키워드 → 모든 조건을 만족하는 장소를 조회한다")
        void getPlaces_withAllConditions() {
            // given
            Category category = new Category("맛집");
            Tag tag = new Tag("가성비");
            PlaceSearchConditions conditions = new PlaceSearchConditions(List.of("가성비"), "맛집", "치킨");
            Place place1 = Place.builder().name("가성비 치킨집").build();

            given(tagRepository.findByNameIn(conditions.tags())).willReturn(List.of(tag));
            given(categoryRepository.findByName("맛집")).willReturn(Optional.of(category));
            given(placeRepository.getPlacesByConditions(category, List.of(tag), "치킨"))
                    .willReturn(List.of(place1));

            // when
            List<PlaceResponse> result = placeService.getPlaceByConditions(conditions);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).placeName()).isEqualTo("가성비 치킨집");
        }
    }

    @Nested
    @DisplayName("장소 상세 조회 테스트")
    class GetPlaceDetailTest {

        @Test
        @DisplayName("존재하지 않는 placeId면 PLACE_NOT_FOUND 예외를 던진다")
        void getPlaceDetail_notFound() {
            // given
            given(placeRepository.findById(1L)).willReturn(Optional.empty());
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();

            // then
            assertThatThrownBy(() -> placeService.getPlaceDetail(1L, new AuthUser("20000000", Role.USER), request))
                    .isInstanceOf(SejongLifeException.class)
                    .hasMessage(ErrorCode.PLACE_NOT_FOUND.getErrorMessage());
        }

        @Test
        @DisplayName("존재하는 placeId면 PlaceDetailResponse 반환한다")
        void getPlaceDetail_success() {
            // given
            Place place = Place.builder().name("맛집").address("주소").mainImageUrl("url").build();
            given(placeRepository.findById(1L)).willReturn(Optional.of(place));
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();

            // when
            placeService.getPlaceDetail(1L, new AuthUser("20000000", Role.USER), request);

            // then
            verify(placeRepository).findById(1L);
        }
    }

    @Nested
    @DisplayName("장소 생성")
    class CreatePlaceTest {

        @Test
        @DisplayName("성공: 로그인된 사용자가 장소를 생성한다")
        void createPlace_success() {
            // given
            AuthUser authUser = new AuthUser("21011111", Role.USER);

            Category category = new Category("식당");
            ReflectionTestUtils.setField(category, "id", 1L);

            Tag tag = new Tag("맛집");
            ReflectionTestUtils.setField(tag, "id", 10L);

            PlaceRequest request = new PlaceRequest(
                    "장소명",
                    "주소",
                    List.of(1L),
                    List.of(10L),
                    null,
                    false,
                    ""
            );

            given(categoryRepository.findAllById(List.of(1L)))
                    .willReturn(List.of(category));
            given(tagRepository.findAllById(List.of(10L)))
                    .willReturn(List.of(tag));

            // when
            placeService.createPlace(request, null, authUser);

            // then
            ArgumentCaptor<Place> captor = ArgumentCaptor.forClass(Place.class);
            verify(placeRepository).save(captor.capture());

            Place saved = captor.getValue();
            assertThat(saved.getName()).isEqualTo("장소명");
            assertThat(saved.getPlaceCategories()).hasSize(1);
            assertThat(saved.getPlaceTags()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("장소 삭제")
    class DeletePlaceTest {

        @Test
        @DisplayName("성공: 로그인된 사용자가 장소를 삭제한다")
        void deletePlace_success() {
            // given
            AuthUser authUser = new AuthUser("21011111", Role.USER);

            Place place = Place.builder()
                    .name("장소")
                    .build();
            ReflectionTestUtils.setField(place, "id", 1L);

            given(placeRepository.findById(1L))
                    .willReturn(Optional.of(place));

            // when
            placeService.deletePlace(1L, authUser);

            // then
            verify(placeRepository).delete(place);
        }
    }
}
