package org.example.sejonglifebe.place.favorite;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.example.sejonglifebe.place.PlaceRepository;
import org.example.sejonglifebe.place.dto.PlaceResponse;
import org.example.sejonglifebe.place.entity.Place;
import org.example.sejonglifebe.user.User;
import org.example.sejonglifebe.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class FavoritePlaceServiceTest {

    @Mock
    private FavoritePlaceRepository favoritePlaceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PlaceRepository placeRepository;

    @InjectMocks
    private FavoritePlaceService favoritePlaceService;

    @Nested
    @DisplayName("즐겨찾기 목록 조회")
    class GetMyFavoritesTest {

        @Test
        @DisplayName("성공: studentId로 즐겨찾기한 장소 목록을 조회하고 PlaceResponse로 변환한다")
        void getMyFavorites_success() {
            // given
            String studentId = "21011111";
            Place place1 = Place.builder().name("장소1").address("주소1").mainImageUrl("url1").build();
            Place place2 = Place.builder().name("장소2").address("주소2").mainImageUrl("url2").build();

            given(favoritePlaceRepository.findFavoritePlacesByStudentId(studentId))
                    .willReturn(List.of(place1, place2));

            // when
            List<PlaceResponse> result = favoritePlaceService.getMyFavorites(studentId);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).placeName()).isEqualTo("장소1");
            assertThat(result.get(1).placeName()).isEqualTo("장소2");
            verify(favoritePlaceRepository).findFavoritePlacesByStudentId(studentId);
        }
    }

    @Nested
    @DisplayName("즐겨찾기 추가")
    class AddFavoriteTest {

        @Test
        @DisplayName("실패: studentId에 해당하는 유저가 없으면 USER_NOT_FOUND 예외를 던진다")
        void addFavorite_userNotFound() {
            // given
            String studentId = "21011111";
            Long placeId = 1L;

            given(userRepository.findByStudentId(studentId)).willReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> favoritePlaceService.addFavorite(studentId, placeId))
                    .isInstanceOf(SejongLifeException.class)
                    .hasMessage(ErrorCode.USER_NOT_FOUND.getErrorMessage());

            verify(userRepository).findByStudentId(studentId);
        }

        @Test
        @DisplayName("실패: placeId에 해당하는 장소가 없으면 PLACE_NOT_FOUND 예외를 던진다")
        void addFavorite_placeNotFound() {
            // given
            String studentId = "21011111";
            Long placeId = 1L;

            User user = User.builder()
                    .studentId(studentId)
                    .nickname("닉네임")
                    .build();

            given(userRepository.findByStudentId(studentId)).willReturn(Optional.of(user));
            given(placeRepository.findById(placeId)).willReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> favoritePlaceService.addFavorite(studentId, placeId))
                    .isInstanceOf(SejongLifeException.class)
                    .hasMessage(ErrorCode.PLACE_NOT_FOUND.getErrorMessage());

            verify(userRepository).findByStudentId(studentId);
            verify(placeRepository).findById(placeId);
        }

        @Test
        @DisplayName("성공: 유저/장소가 존재하면 FavoritePlace를 저장한다")
        void addFavorite_success() {
            // given
            String studentId = "21011111";
            Long placeId = 1L;

            User user = User.builder()
                    .studentId(studentId)
                    .nickname("닉네임")
                    .build();

            Place place = Place.builder()
                    .name("장소")
                    .address("주소")
                    .mainImageUrl("url")
                    .build();

            given(userRepository.findByStudentId(studentId)).willReturn(Optional.of(user));
            given(placeRepository.findById(placeId)).willReturn(Optional.of(place));

            // when
            favoritePlaceService.addFavorite(studentId, placeId);

            // then
            verify(favoritePlaceRepository).save(any(FavoritePlace.class));
        }

        @Test
        @DisplayName("실패: 이미 즐겨찾기한 장소면 DataIntegrityViolationException을 잡아 ALREADY_FAVORITE_PLACE 예외를 던진다")
        void addFavorite_alreadyFavorite() {
            // given
            String studentId = "21011111";
            Long placeId = 1L;

            User user = User.builder()
                    .studentId(studentId)
                    .nickname("닉네임")
                    .build();

            Place place = Place.builder()
                    .name("장소")
                    .address("주소")
                    .mainImageUrl("url")
                    .build();

            given(userRepository.findByStudentId(studentId)).willReturn(Optional.of(user));
            given(placeRepository.findById(placeId)).willReturn(Optional.of(place));
            given(favoritePlaceRepository.save(any(FavoritePlace.class)))
                    .willThrow(new DataIntegrityViolationException("unique constraint"));

            // when/then
            assertThatThrownBy(() -> favoritePlaceService.addFavorite(studentId, placeId))
                    .isInstanceOf(SejongLifeException.class)
                    .hasMessage(ErrorCode.ALREADY_FAVORITE_PLACE.getErrorMessage());
        }
    }

    @Nested
    @DisplayName("즐겨찾기 삭제")
    class RemoveFavoriteTest {

        @Test
        @DisplayName("성공: 즐겨찾기 삭제를 호출한다")
        void removeFavorite_success() {
            // given
            String studentId = "21011111";
            Long placeId = 1L;

            // when
            favoritePlaceService.removeFavorite(studentId, placeId);

            // then
            verify(favoritePlaceRepository).deleteByUserStudentIdAndPlaceId(studentId, placeId);
        }
    }

    @Test
    @DisplayName("내 즐겨찾기 개수 조회 성공")
    void getMyFavoriteCount_success() {
        // given
        String studentId = "21011111";
        given(favoritePlaceRepository.countByUserStudentId(studentId)).willReturn(3L);

        // when
        long count = favoritePlaceService.getMyFavoriteCount(studentId);

        // then
        assertThat(count).isEqualTo(3L);
        verify(favoritePlaceRepository).countByUserStudentId(studentId);
    }
}
