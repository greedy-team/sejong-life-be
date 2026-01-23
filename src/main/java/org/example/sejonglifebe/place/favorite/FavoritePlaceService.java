package org.example.sejonglifebe.place.favorite;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.example.sejonglifebe.place.PlaceRepository;
import org.example.sejonglifebe.place.dto.PlaceResponse;
import org.example.sejonglifebe.place.entity.Place;
import org.example.sejonglifebe.user.User;
import org.example.sejonglifebe.user.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FavoritePlaceService {

    private final FavoritePlaceRepository favoritePlaceRepository;
    private final UserRepository userRepository;
    private final PlaceRepository placeRepository;

    @Transactional(readOnly = true)
    public List<PlaceResponse> getMyFavorites(String studentId) {
        return favoritePlaceRepository.findFavoritePlacesByStudentId(studentId).stream()
                .map(PlaceResponse::from)
                .toList();
    }

    public void addFavorite(String studentId, Long placeId) {
        User user = userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new SejongLifeException(ErrorCode.USER_NOT_FOUND));

        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new SejongLifeException(ErrorCode.PLACE_NOT_FOUND));

        try {
            favoritePlaceRepository.save(FavoritePlace.of(user, place));
        } catch (DataIntegrityViolationException e) {
            throw new SejongLifeException(ErrorCode.ALREADY_FAVORITE_PLACE);
        }
    }

    public void removeFavorite(String studentId, Long placeId) {
        favoritePlaceRepository.deleteByUserStudentIdAndPlaceId(studentId, placeId);
    }

    @Transactional(readOnly = true)
    public long getMyFavoriteCount(String studentId) {
        return favoritePlaceRepository.countByUserStudentId(studentId);
    }
}
