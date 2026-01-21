package org.example.sejonglifebe.place;

import java.util.List;

import java.util.Optional;
import org.example.sejonglifebe.category.Category;
import org.example.sejonglifebe.place.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long>, PlaceRepositoryCustom {

    @Query("""
            SELECT p FROM Place p
            JOIN p.placeCategories pc
            LEFT JOIN p.reviews r
            WHERE pc.category = :category
            GROUP BY p.id
            ORDER BY COUNT(DISTINCT r) DESC
           """)
    List<Place> findByCategory(@Param("category") Category category);

    @Query("""
            SELECT DISTINCT p FROM Place p
            LEFT JOIN FETCH p.placeImages
            LEFT JOIN FETCH p.reviews
            WHERE p.id = :placeId
           """)
    Optional<Place> findByIdWithImagesAndReviews(@Param("placeId") Long placeId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Place p SET p.viewCount = p.viewCount + 1, p.weeklyViewCount = p.weeklyViewCount + 1 WHERE p.id = :id")
    void increaseViewCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Place p SET p.weeklyViewCount = 0")
    void resetAllWeeklyViewCounts();

    List<Place> findTop10ByOrderByWeeklyViewCountDesc();
}
