package org.example.sejonglifebe.place;

import org.example.sejonglifebe.auth.AuthUser;
import org.example.sejonglifebe.place.view.PlaceViewService;
import org.example.sejonglifebe.place.dto.PlaceUpdateRequest;
import org.example.sejonglifebe.place.entity.MapLinks;
import org.example.sejonglifebe.place.entity.PlaceCategory;
import org.example.sejonglifebe.place.entity.PlaceTag;
import org.example.sejonglifebe.user.Role;
import org.example.sejonglifebe.category.Category;
import org.example.sejonglifebe.category.CategoryRepository;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.example.sejonglifebe.place.dto.PlaceQueryResult;
import org.example.sejonglifebe.place.dto.PlaceRequest;
import org.example.sejonglifebe.place.dto.PlaceResponse;
import org.example.sejonglifebe.place.dto.PlaceSearchConditions;
import org.example.sejonglifebe.place.entity.Place;
import org.example.sejonglifebe.s3.S3Service;
import org.example.sejonglifebe.tag.Tag;
import org.example.sejonglifebe.tag.TagRepository;
import org.example.sejonglifebe.user.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.never;
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
    private PlaceViewService placeViewService;

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
            PlaceSearchConditions conditions = new PlaceSearchConditions(List.of("존재X"), "전체", null, false);
            Pageable pageable = PageRequest.of(0, 10);

            given(tagRepository.findByNameIn(anyList()))
                    .willReturn(List.of());

            // when/then
            assertThatThrownBy(() -> placeService.getPlaceByConditions(conditions, pageable))
                    .isInstanceOf(SejongLifeException.class)
                    .hasMessage(ErrorCode.TAG_NOT_FOUND.getErrorMessage());

            verify(tagRepository).findByNameIn(List.of("존재X"));
        }

        @Test
        @DisplayName("카테고리 = 전체 && 태그 없음 → 모든 장소를 조회한다")
        void getPlaces_allCategory_noTags() {
            // given
            PlaceSearchConditions conditions = new PlaceSearchConditions(List.of(), "전체", null, false);
            Pageable pageable = PageRequest.of(0, 10);
            Place place1 = Place.builder().name("장소1").build();
            Place place2 = Place.builder().name("장소2").build();
            Page<PlaceQueryResult> pageResult = new PageImpl<>(List.of(
                    new PlaceQueryResult(place1, 0L),
                    new PlaceQueryResult(place2, 0L)
            ));

            given(tagRepository.findByNameIn(List.of())).willReturn(List.of());
            given(placeRepository.getPlacesByConditions(null, List.of(), null, false, pageable))
                    .willReturn(pageResult);

            // when
            Page<PlaceResponse> result = placeService.getPlaceByConditions(conditions, pageable);

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).placeName()).isEqualTo("장소1");
            assertThat(result.getContent().get(1).placeName()).isEqualTo("장소2");
        }

        @Test
        @DisplayName("카테고리 = 전체 && 태그 있음 → 해당 태그를 가진 장소를 조회한다")
        void getPlaces_allCategory_withTags() {
            // given
            Tag tag = new Tag("가성비");
            PlaceSearchConditions conditions = new PlaceSearchConditions(List.of("가성비"), "전체", null, false);
            Pageable pageable = PageRequest.of(0, 10);
            Place place1 = Place.builder().name("가성비 장소").build();
            Page<PlaceQueryResult> pageResult = new PageImpl<>(List.of(new PlaceQueryResult(place1, 0L)));

            given(tagRepository.findByNameIn(conditions.tags())).willReturn(List.of(tag));
            given(placeRepository.getPlacesByConditions(null, List.of(tag), null, false, pageable))
                    .willReturn(pageResult);

            // when
            Page<PlaceResponse> result = placeService.getPlaceByConditions(conditions, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).placeName()).isEqualTo("가성비 장소");
        }

        @Test
        @DisplayName("카테고리 존재하지 않으면 CATEGORY_NOT_FOUND 예외를 던진다")
        void getPlaces_categoryNotFound() {
            // given
            PlaceSearchConditions conditions = new PlaceSearchConditions(List.of(), "맛집", null, false);
            Pageable pageable = PageRequest.of(0, 10);

            given(tagRepository.findByNameIn(List.of())).willReturn(List.of());
            given(categoryRepository.findByName("맛집")).willReturn(Optional.empty());

            // then
            assertThatThrownBy(() -> placeService.getPlaceByConditions(conditions, pageable))
                    .isInstanceOf(SejongLifeException.class)
                    .hasMessage(ErrorCode.CATEGORY_NOT_FOUND.getErrorMessage());
        }

        @Test
        @DisplayName("카테고리 + 태그 없음 → 해당 카테고리의 장소를 조회한다")
        void getPlaces_selectedCategory_noTags() {
            // given
            Category category = new Category("맛집");
            PlaceSearchConditions conditions = new PlaceSearchConditions(List.of(), "맛집", null, false);
            Pageable pageable = PageRequest.of(0, 10);
            Place place1 = Place.builder().name("맛집1").build();
            Place place2 = Place.builder().name("맛집2").build();
            Page<PlaceQueryResult> pageResult = new PageImpl<>(List.of(
                    new PlaceQueryResult(place1, 0L),
                    new PlaceQueryResult(place2, 0L)
            ));

            given(tagRepository.findByNameIn(List.of())).willReturn(List.of());
            given(categoryRepository.findByName("맛집")).willReturn(Optional.of(category));
            given(placeRepository.getPlacesByConditions(category, List.of(), null, false, pageable))
                    .willReturn(pageResult);

            // when
            Page<PlaceResponse> result = placeService.getPlaceByConditions(conditions, pageable);

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).placeName()).isEqualTo("맛집1");
            assertThat(result.getContent().get(1).placeName()).isEqualTo("맛집2");
        }

        @Test
        @DisplayName("카테고리 + 태그 있음 → 해당 카테고리와 태그를 모두 만족하는 장소를 조회한다")
        void getPlaces_selectedCategory_withTags() {
            // given
            Category category = new Category("맛집");
            Tag tag = new Tag("가성비");
            PlaceSearchConditions conditions = new PlaceSearchConditions(List.of("가성비"), "맛집", null, false);
            Pageable pageable = PageRequest.of(0, 10);
            Place place1 = Place.builder().name("가성비 맛집").build();
            Page<PlaceQueryResult> pageResult = new PageImpl<>(List.of(new PlaceQueryResult(place1, 0L)));

            given(tagRepository.findByNameIn(conditions.tags())).willReturn(List.of(tag));
            given(categoryRepository.findByName("맛집")).willReturn(Optional.of(category));
            given(placeRepository.getPlacesByConditions(category, List.of(tag), null, false, pageable))
                    .willReturn(pageResult);

            // when
            Page<PlaceResponse> result = placeService.getPlaceByConditions(conditions, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).placeName()).isEqualTo("가성비 맛집");
        }

        @Test
        @DisplayName("키워드만 입력 → 장소명에 키워드가 포함된 장소를 조회한다")
        void getPlaces_withKeywordOnly() {
            // given
            PlaceSearchConditions conditions = new PlaceSearchConditions(List.of(), "전체", "카페", false);
            Pageable pageable = PageRequest.of(0, 10);
            Place place1 = Place.builder().name("스타벅스 카페").build();
            Place place2 = Place.builder().name("투썸 카페").build();
            Page<PlaceQueryResult> pageResult = new PageImpl<>(List.of(
                    new PlaceQueryResult(place1, 0L),
                    new PlaceQueryResult(place2, 0L)
            ));

            given(tagRepository.findByNameIn(List.of())).willReturn(List.of());
            given(placeRepository.getPlacesByConditions(null, List.of(), "카페", false, pageable))
                    .willReturn(pageResult);

            // when
            Page<PlaceResponse> result = placeService.getPlaceByConditions(conditions, pageable);

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).placeName()).contains("카페");
            assertThat(result.getContent().get(1).placeName()).contains("카페");
        }

        @Test
        @DisplayName("카테고리 + 키워드 → 해당 카테고리에서 키워드가 포함된 장소를 조회한다")
        void getPlaces_withCategoryAndKeyword() {
            // given
            Category category = new Category("맛집");
            PlaceSearchConditions conditions = new PlaceSearchConditions(List.of(), "맛집", "치킨", false);
            Pageable pageable = PageRequest.of(0, 10);
            Place place1 = Place.builder().name("BHC 치킨").build();
            Page<PlaceQueryResult> pageResult = new PageImpl<>(List.of(new PlaceQueryResult(place1, 0L)));

            given(tagRepository.findByNameIn(List.of())).willReturn(List.of());
            given(categoryRepository.findByName("맛집")).willReturn(Optional.of(category));
            given(placeRepository.getPlacesByConditions(category, List.of(), "치킨", false, pageable))
                    .willReturn(pageResult);

            // when
            Page<PlaceResponse> result = placeService.getPlaceByConditions(conditions, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).placeName()).isEqualTo("BHC 치킨");
        }

        @Test
        @DisplayName("태그 + 키워드 → 해당 태그를 가지고 키워드가 포함된 장소를 조회한다")
        void getPlaces_withTagAndKeyword() {
            // given
            Tag tag = new Tag("가성비");
            PlaceSearchConditions conditions = new PlaceSearchConditions(List.of("가성비"), "전체", "피자", false);
            Pageable pageable = PageRequest.of(0, 10);
            Place place1 = Place.builder().name("가성비 피자").build();
            Page<PlaceQueryResult> pageResult = new PageImpl<>(List.of(new PlaceQueryResult(place1, 0L)));

            given(tagRepository.findByNameIn(conditions.tags())).willReturn(List.of(tag));
            given(placeRepository.getPlacesByConditions(null, List.of(tag), "피자", false, pageable))
                    .willReturn(pageResult);

            // when
            Page<PlaceResponse> result = placeService.getPlaceByConditions(conditions, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).placeName()).contains("피자");
        }

        @Test
        @DisplayName("카테고리 + 태그 + 키워드 → 모든 조건을 만족하는 장소를 조회한다")
        void getPlaces_withAllConditions() {
            // given
            Category category = new Category("맛집");
            Tag tag = new Tag("가성비");
            PlaceSearchConditions conditions = new PlaceSearchConditions(List.of("가성비"), "맛집", "치킨", false);
            Pageable pageable = PageRequest.of(0, 10);
            Place place1 = Place.builder().name("가성비 치킨집").build();
            Page<PlaceQueryResult> pageResult = new PageImpl<>(List.of(new PlaceQueryResult(place1, 0L)));

            given(tagRepository.findByNameIn(conditions.tags())).willReturn(List.of(tag));
            given(categoryRepository.findByName("맛집")).willReturn(Optional.of(category));
            given(placeRepository.getPlacesByConditions(category, List.of(tag), "치킨", false, pageable))
                    .willReturn(pageResult);

            // when
            Page<PlaceResponse> result = placeService.getPlaceByConditions(conditions, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).placeName()).isEqualTo("가성비 치킨집");
        }
    }

    @Nested
    @DisplayName("장소 상세 조회 테스트")
    class GetPlaceDetailTest {

        @Test
        @DisplayName("존재하지 않는 placeId면 PLACE_NOT_FOUND 예외를 던진다")
        void getPlaceDetail_notFound() {
            given(placeRepository.existsById(1L)).willReturn(false);

            MockHttpServletRequest request = new MockHttpServletRequest();

            assertThatThrownBy(() -> placeService.getPlaceDetail(1L, new AuthUser("20000000", Role.USER), request))
                    .isInstanceOf(SejongLifeException.class)
                    .hasMessage(ErrorCode.PLACE_NOT_FOUND.getErrorMessage());

            verify(placeRepository).existsById(1L);
            verify(placeRepository, never()).findById(anyLong());
        }

        @Test
        @DisplayName("존재하는 placeId면 PlaceDetailResponse 반환한다")
        void getPlaceDetail_success() {
            Place place = Place.builder().name("맛집").address("주소").mainImageUrl("url").build();

            given(placeRepository.existsById(1L)).willReturn(true);
            given(placeRepository.findById(1L)).willReturn(Optional.of(place));
            given(placeViewService.recordFirstView(anyLong(), any())).willReturn(false); // 조회수 증가 안 타게

            MockHttpServletRequest request = new MockHttpServletRequest();

            placeService.getPlaceDetail(1L, new AuthUser("20000000", Role.USER), request);

            verify(placeRepository).existsById(1L);
            verify(placeRepository, atLeastOnce()).findById(1L);
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
                    127.0,
                    70.0,
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
    @DisplayName("장소 수정")
    class UpdatePlaceTest {

        @Test
        @DisplayName("성공: ADMIN이 장소의 mapLinks/partnership/categoryIds/tagIds를 수정한다")
        void updatePlace_success() {
            // given
            AuthUser admin = new AuthUser("21011111", Role.ADMIN);

            Place place = Place.builder()
                    .name("원래이름")
                    .address("원래주소")
                    .mapLinks(new MapLinks("https://old-n.com", "https://old-k.com", "https://old-g.com"))
                    .build();
            ReflectionTestUtils.setField(place, "id", 1L);

            Category oldCategory = new Category("식당");
            ReflectionTestUtils.setField(oldCategory, "id", 1L);
            Tag oldTag = new Tag("맛집");
            ReflectionTestUtils.setField(oldTag, "id", 10L);

            PlaceCategory.createPlaceCategory(place, oldCategory);
            PlaceTag.createPlaceTag(place, oldTag);

            Category newCategory = new Category("카페");
            ReflectionTestUtils.setField(newCategory, "id", 2L);
            Tag newTag1 = new Tag("분위기 좋은");
            ReflectionTestUtils.setField(newTag1, "id", 11L);
            Tag newTag2 = new Tag("콘센트 있는");
            ReflectionTestUtils.setField(newTag2, "id", 12L);

            PlaceUpdateRequest request = new PlaceUpdateRequest(
                    List.of(2L),
                    List.of(11L, 12L),
                    new MapLinks("https://n-updated.com", "https://k-updated.com", "https://g-updated.com"),
                    true,
                    "재학생 10% 할인"
            );

            given(placeRepository.findById(1L)).willReturn(Optional.of(place));
            given(categoryRepository.findAllById(List.of(2L))).willReturn(List.of(newCategory));
            given(tagRepository.findAllById(List.of(11L, 12L))).willReturn(List.of(newTag1, newTag2));

            // when
            placeService.updatePlace(1L, request, admin);

            // then
            assertThat(place.getMapLinks().getNaverMap()).isEqualTo("https://n-updated.com");
            assertThat(place.getMapLinks().getKakaoMap()).isEqualTo("https://k-updated.com");
            assertThat(place.getMapLinks().getGoogleMap()).isEqualTo("https://g-updated.com");

            assertThat(place.isPartnership()).isTrue();
            assertThat(place.getPartnershipContent()).isEqualTo("재학생 10% 할인");

            assertThat(place.getPlaceCategories()).hasSize(1);
            assertThat(place.getPlaceCategories().get(0).getCategory().getName()).isEqualTo("카페");

            assertThat(place.getPlaceTags()).hasSize(2);
            assertThat(place.getPlaceTags())
                    .extracting(pt -> pt.getTag().getName())
                    .containsExactlyInAnyOrder("분위기 좋은", "콘센트 있는");

            verify(placeRepository).findById(1L);
            verify(categoryRepository).findAllById(List.of(2L));
            verify(tagRepository).findAllById(List.of(11L, 12L));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 placeId면 PLACE_NOT_FOUND 예외를 던진다")
        void updatePlace_fail_placeNotFound() {
            // given
            AuthUser admin = new AuthUser("21011111", Role.ADMIN);

            PlaceUpdateRequest request = new PlaceUpdateRequest(
                    List.of(1L),
                    List.of(10L),
                    new MapLinks("https://naver.com/place", "https://kakao.com/place", "https://google.com/maps"),
                    false,
                    ""
            );

            given(placeRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> placeService.updatePlace(999L, request, admin))
                    .isInstanceOf(SejongLifeException.class)
                    .hasMessage(ErrorCode.PLACE_NOT_FOUND.getErrorMessage());

            verify(placeRepository).findById(999L);
            verify(categoryRepository, org.mockito.Mockito.never()).findAllById(org.mockito.ArgumentMatchers.anyList());
            verify(tagRepository, org.mockito.Mockito.never()).findAllById(org.mockito.ArgumentMatchers.anyList());
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
