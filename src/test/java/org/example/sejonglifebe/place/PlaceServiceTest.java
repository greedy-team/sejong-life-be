package org.example.sejonglifebe.place;

import org.example.sejonglifebe.category.Category;
import org.example.sejonglifebe.category.CategoryRepository;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.example.sejonglifebe.place.dto.PlaceRequest;
import org.example.sejonglifebe.place.entity.Place;
import org.example.sejonglifebe.tag.Tag;
import org.example.sejonglifebe.tag.TagRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;
import java.util.Optional;

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

    @InjectMocks
    private PlaceService placeService;

    @Nested
    @DisplayName("카테고리,태그 기반 장소 조회 테스트")
    class GetPlacesFilteredTest {

        @Test
        @DisplayName("존재하지 않는 태그 이름이 포함되면 TAG_NOT_FOUND 예외를 던진다")
        void getPlaces_tagNotFound() {
            // given
            PlaceRequest request = new PlaceRequest(List.of("존재X"), "전체");

            given(tagRepository.findByNameIn(anyList()))
                    .willReturn(List.of());

            // when/then
            assertThatThrownBy(() -> placeService.getPlacesFilteredByCategoryAndTags(request))
                    .isInstanceOf(SejongLifeException.class)
                    .hasMessage(ErrorCode.TAG_NOT_FOUND.getErrorMessage());

            verify(tagRepository).findByNameIn(List.of("존재X"));
        }

        @Test
        @DisplayName("카테고리 = 전체 && 태그 없음 → findAll() 호출한다")
        void getPlaces_allCategory_noTags() {
            // given
            PlaceRequest request = new PlaceRequest(List.of(), "전체");

            given(tagRepository.findByNameIn(List.of())).willReturn(List.of());

            // when
            placeService.getPlacesFilteredByCategoryAndTags(request);

            // then
            verify(placeRepository).findAllOrderByReviewCountDesc();
        }

        @Test
        @DisplayName("카테고리 = 전체 && 태그 있음 → findByTags() 호출한다")
        void getPlaces_allCategory_withTags() {
            // given
            Tag tag = new Tag("가성비");
            PlaceRequest request = new PlaceRequest(List.of("가성비"), "전체");

            given(tagRepository.findByNameIn(request.tags())).willReturn(List.of(tag));

            // when
            placeService.getPlacesFilteredByCategoryAndTags(request);

            // then
            verify(placeRepository).findByTags(List.of(tag), (long) List.of(tag).size());
        }

        @Test
        @DisplayName("카테고리 존재하지 않으면 CATEGORY_NOT_FOUND 예외를 던진다")
        void getPlaces_categoryNotFound() {
            // given
            PlaceRequest request = new PlaceRequest(List.of(), "맛집");

            given(tagRepository.findByNameIn(List.of())).willReturn(List.of());
            given(categoryRepository.findByName("맛집")).willReturn(Optional.empty());

            // then
            assertThatThrownBy(() -> placeService.getPlacesFilteredByCategoryAndTags(request))
                    .isInstanceOf(SejongLifeException.class)
                    .hasMessage(ErrorCode.CATEGORY_NOT_FOUND.getErrorMessage());
        }

        @Test
        @DisplayName("카테고리 + 태그 없음 → findByCategory() 호출한다")
        void getPlaces_selectedCategory_noTags() {
            // given
            Category category = new Category("맛집");
            PlaceRequest request = new PlaceRequest(List.of(), "맛집");

            given(tagRepository.findByNameIn(List.of())).willReturn(List.of());
            given(categoryRepository.findByName("맛집")).willReturn(Optional.of(category));

            // when
            placeService.getPlacesFilteredByCategoryAndTags(request);

            // then
            verify(placeRepository).findByCategory(category);
        }

        @Test
        @DisplayName("카테고리 + 태그 있음 → findPlacesByTagsAndCategory() 호출한다")
        void getPlaces_selectedCategory_withTags() {
            // given
            Category category = new Category("맛집");
            Tag tag = new Tag("가성비");
            PlaceRequest request = new PlaceRequest(List.of("가성비"), "맛집");

            given(tagRepository.findByNameIn(request.tags())).willReturn(List.of(tag));
            given(categoryRepository.findByName("맛집")).willReturn(Optional.of(category));

            // when
            placeService.getPlacesFilteredByCategoryAndTags(request);

            // then
            verify(placeRepository).findPlacesByTagsAndCategoryContainingAllTags(category, List.of(tag), (long) List.of(tag).size());
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
            assertThatThrownBy(() -> placeService.getPlaceDetail(1L, request, response))
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
            placeService.getPlaceDetail(1L, request, response);

            // then
            verify(placeRepository).findById(1L);
        }
    }
}
