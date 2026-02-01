package org.example.sejonglifebe.place.favorite;

import java.util.List;
import org.example.sejonglifebe.place.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface FavoritePlaceRepository extends JpaRepository<FavoritePlace, Long> {

    @Query("""
            SELECT DISTINCT p
            FROM FavoritePlace fp
            JOIN fp.place p
            WHERE fp.user.studentId = :studentId
            ORDER BY p.id DESC
           """)
    List<Place> findFavoritePlacesByStudentId(@Param("studentId") String studentId);

    long deleteByUserStudentIdAndPlaceId(String studentId, Long placeId);

    long countByUserStudentId(String studentId);

}
