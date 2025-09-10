package org.example.sejonglifebe.place;

import java.util.List;

import org.example.sejonglifebe.category.Category;
import org.example.sejonglifebe.place.entity.Place;
import org.example.sejonglifebe.tag.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {

    @Query("SELECT p FROM Place p " +
            "JOIN p.placeCategories pc " +
            "JOIN p.placeTags pt " +
            "WHERE pc.category = :category AND pt.tag IN :tags " +
            "GROUP BY p.id " +
            "ORDER BY COUNT(DISTINCT pt.tag) DESC")
    List<Place> findPlacesByTagsAndCategory(
            @Param("category") Category category,
            @Param("tags") List<Tag> tags

    );

    @Query("SELECT p FROM Place p " +
            "JOIN p.placeTags pt " +
            "WHERE pt.tag IN :tags " +
            "GROUP BY p.id " +
            "ORDER BY COUNT(DISTINCT pt.tag) DESC")
    List<Place> findByTags(
            @Param("tags") List<Tag> tags
    );

    @Query("SELECT p FROM Place p JOIN p.placeCategories pc WHERE pc.category = :category")
    List<Place> findByCategory(@Param("category") Category category);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Place p SET p.viewCount = p.viewCount + 1, p.weeklyViewCount = p.weeklyViewCount + 1 WHERE p.id = :id")
    void increaseViewCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Place p SET p.weeklyViewCount = 0")
    void resetAllWeeklyViewCounts();

    @Query("SELECT DISTINCT p FROM Place p " +
            "LEFT JOIN FETCH p.reviews " +
            "LEFT JOIN FETCH p.placeCategories pc LEFT JOIN FETCH pc.category " +
            "LEFT JOIN FETCH p.placeTags pt LEFT JOIN FETCH pt.tag " +
            "ORDER BY p.weeklyViewCount DESC")
    List<Place> findHotPlaces();

}
