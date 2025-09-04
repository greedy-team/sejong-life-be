package org.example.sejonglifebe.tag;

import org.example.sejonglifebe.category.CategoryRepository;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.example.sejonglifebe.tag.dto.TagResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private TagService tagService;

    @Nested
    @DisplayName("전체 태그 목록 조회 테스트")
    class GetAllTagsTest {

        @Test
        @DisplayName("태그가 존재하면 정상적으로 DTO로 변환되어 반환된다.")
        void getAllTags_success() {
            //given
            Tag tag1 = Tag.builder().id(1L).name("집밥").build();
            Tag tag2 = Tag.builder().id(2L).name("가성비").build();
            given(tagRepository.findAll()).willReturn(List.of(tag1, tag2));

            //when
            List<TagResponse> result = tagService.getTags();

            //then
            assertThat(result)
                    .hasSize(2)
                    .extracting("tagId", "tagName")
                    .containsExactlyInAnyOrder(
                            tuple(1L, "집밥"),
                            tuple(2L, "가성비")
                    );
            verify(tagRepository, times(1)).findAll();
            verifyNoMoreInteractions(tagRepository, categoryRepository);
        }

        @Test
        @DisplayName("태그가 존재하지 않으면 빈 리스트 반환한다.")
        void getAllTags_emptyResult() {
            //given
            given(tagRepository.findAll()).willReturn(List.of());

            //when
            List<TagResponse> result = tagService.getTags();

            //then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("특정 카테고리 태그 목록 조회 테스트")
    class GetTagsByCategoryIdTest {

        @Test
        @DisplayName("categoryId가 null이면 전체 목록을 반환한다.")
        void nullCategoryId_returnsAll() {
            // given
            Tag tag1 = Tag.builder().id(1L).name("집밥").build();
            Tag tag2 = Tag.builder().id(2L).name("가성비").build();
            given(tagRepository.findAll()).willReturn(List.of(tag1, tag2));

            // when
            List<TagResponse> result = tagService.getTagsByCategoryId(null);

            // then
            assertThat(result)
                    .extracting("tagId", "tagName")
                    .containsExactlyInAnyOrder(tuple(1L, "집밥"), tuple(2L, "가성비"));
            verify(categoryRepository, never()).existsById(anyLong());
            verify(tagRepository).findAll();
            verify(tagRepository, never()).findAllByCategoryId(anyLong());
        }

        @Test
        @DisplayName("존재하지 않는 카테고리면 예외를 던진다.")
        void notFoundCategory_throws() {
            // given
            given(categoryRepository.existsById(1L)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> tagService.getTagsByCategoryId(1L))
                    .isInstanceOf(SejongLifeException.class)
                    .hasMessage(ErrorCode.CATEGORY_NOT_FOUND.getErrorMessage());
        }

        @Test
        @DisplayName("카테고리가 존재하면 해당 카테고리의 태그 목록을 반환한다.")
        void foundCategory_returnsList() {
            // given
            Tag tag1 = Tag.builder().id(1L).name("집밥").build();
            Tag tag2 = Tag.builder().id(2L).name("가성비").build();
            given(categoryRepository.existsById(10L)).willReturn(true);
            given(tagRepository.findAllByCategoryId(10L)).willReturn(List.of(tag1, tag2));

            // when
            List<TagResponse> result = tagService.getTagsByCategoryId(10L);

            // then
            assertThat(result)
                    .hasSize(2)
                    .extracting("tagId", "tagName")
                    .containsExactlyInAnyOrder(tuple(1L, "집밥"), tuple(2L, "가성비"));
            verify(categoryRepository).existsById(10L);
            verify(tagRepository).findAllByCategoryId(10L);
            verify(tagRepository, never()).findAll();
        }

        @Test
        @DisplayName("카테고리가 존재하지만 해당 태그가 없으면 빈 리스트 반환한다.")
        void foundCategory_emptyList() {
            // given
            given(categoryRepository.existsById(10L)).willReturn(true);
            given(tagRepository.findAllByCategoryId(10L)).willReturn(List.of());

            // when
            List<TagResponse> result = tagService.getTagsByCategoryId(10L);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("추천 태그 목록 조회 테스트")
    class GetFrequentlyUsedTagsTest {

        @Test
        @DisplayName("존재하지 않는 카테고리면 예외를 던진다,")
        void notFoundCategory_throws() {
            // given
            given(categoryRepository.existsById(7L)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> tagService.getFrequentlyUsedTagsByCategoryId(7L))
                    .isInstanceOf(SejongLifeException.class)
                    .hasMessage(ErrorCode.CATEGORY_NOT_FOUND.getErrorMessage());
        }

        @Test
        @DisplayName("인기 태그를 먼저, 나머지는 뒤에 중복 없이 반환한다.")
        void popularFirst_thenAll() {
            // given
            Tag tag1 = Tag.builder().id(1L).name("가성비").build();
            Tag tag2 = Tag.builder().id(2L).name("집밥").build();
            Tag tag3 = Tag.builder().id(3L).name("분위기").build();
            Tag tag4 = Tag.builder().id(4L).name("혼밥").build();
            given(categoryRepository.existsById(1L)).willReturn(true);
            // 인기: [집밥, 가성비]
            given(tagRepository.findFrequentlyUsedTagsByCategoryId(1L))
                    .willReturn(List.of(tag2, tag1));
            // 전체: [가성비, 집밥, 분위기, 혼밥]
            given(tagRepository.findAll())
                    .willReturn(List.of(tag1, tag2, tag3, tag4));

            // when
            List<TagResponse> result = tagService.getFrequentlyUsedTagsByCategoryId(1L);

            // then
            assertThat(result)
                    .extracting("tagId", "tagName")
                    .containsExactly(
                            tuple(2L, "집밥"),
                            tuple(1L, "가성비"),
                            tuple(3L, "분위기"),
                            tuple(4L, "혼밥")
                    );
            verify(tagRepository).findFrequentlyUsedTagsByCategoryId(1L);
            verify(tagRepository).findAll();
        }

        @Test
        @DisplayName("인기 태그가 비어있으면 전체 목록만 반환한다")
        void noPopular_returnsAll() {
            // given
            Tag tag1 = Tag.builder().id(1L).name("가성비").build();
            Tag tag2 = Tag.builder().id(2L).name("집밥").build();
            given(categoryRepository.existsById(1L)).willReturn(true);
            given(tagRepository.findFrequentlyUsedTagsByCategoryId(1L)).willReturn(List.of());
            given(tagRepository.findAll()).willReturn(List.of(tag1, tag2));

            // when
            List<TagResponse> result = tagService.getFrequentlyUsedTagsByCategoryId(1L);

            // then
            assertThat(result)
                    .extracting("tagId", "tagName")
                    .containsExactly(tuple(1L, "가성비"), tuple(2L, "집밥"));
        }

        @Test
        @DisplayName("전체가 비어있고 인기만 있으면 인기만 반환한다")
        void onlyPopular_returnsPopular() {
            // given
            Tag tag1 = Tag.builder().id(1L).name("가성비").build();
            given(categoryRepository.existsById(2L)).willReturn(true);
            given(tagRepository.findFrequentlyUsedTagsByCategoryId(2L)).willReturn(List.of(tag1));
            given(tagRepository.findAll()).willReturn(List.of());

            // when
            List<TagResponse> result = tagService.getFrequentlyUsedTagsByCategoryId(2L);

            // then
            assertThat(result)
                    .extracting("tagId", "tagName")
                    .containsExactly(tuple(1L, "가성비"));
        }
    }
}
