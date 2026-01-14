package org.example.sejonglifebe.place;

import java.util.List;

import org.example.sejonglifebe.category.Category;
import org.example.sejonglifebe.place.entity.Place;
import org.example.sejonglifebe.tag.Tag;
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
            "LEFT JOIN p.reviews r " +
            "WHERE pc.category = :category AND pt.tag IN :tags " +
            "GROUP BY p.id " +
            "HAVING COUNT(DISTINCT pt.tag) = :tagCount " +
            "ORDER BY COUNT(DISTINCT r) DESC")
    List<Place> findPlacesByTagsAndCategoryContainingAllTags(
            @Param("category") Category category,
            @Param("tags") List<Tag> tags,
            @Param("tagCount") Long tagCount
    );

    @Query("SELECT p FROM Place p " +
            "JOIN p.placeTags pt " +
            "LEFT JOIN p.reviews r " +
            "WHERE pt.tag IN :tags " +
            "GROUP BY p.id " +
            "HAVING COUNT(DISTINCT pt.tag) = :tagCount " +
            "ORDER BY COUNT(DISTINCT r) DESC")
    List<Place> findByTags(
            @Param("tags") List<Tag> tags,
            @Param("tagCount") Long tagCount
    );

    @Query("SELECT p FROM Place p " +
            "JOIN p.placeCategories pc " +
            "LEFT JOIN p.reviews r " +
            "WHERE pc.category = :category " +
            "GROUP BY p.id " +
            "ORDER BY COUNT(DISTINCT r) DESC")
    List<Place> findByCategory(@Param("category") Category category);

    @Query("SELECT p FROM Place p " +
            "LEFT JOIN p.reviews r " +
            "GROUP BY p.id " +
            "ORDER BY COUNT(r) DESC")
    List<Place> findAllOrderByReviewCountDesc();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Place p
            SET p.viewCount = p.viewCount + 1,
            p.weeklyViewCount = p.weeklyViewCount + 1
            WHERE p.id = :id
           """)
    void increaseViewCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Place p SET p.weeklyViewCount = 0")
    void resetAllWeeklyViewCounts();

    List<Place> findTop10ByOrderByWeeklyViewCountDesc();
}
