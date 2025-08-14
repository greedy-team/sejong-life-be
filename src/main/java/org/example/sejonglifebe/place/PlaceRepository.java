package org.example.sejonglifebe.place;

import java.util.List;
import org.example.sejonglifebe.category.Category;
import org.example.sejonglifebe.place.entity.Place;
import org.example.sejonglifebe.tag.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface PlaceRepository extends JpaRepository<Place, Long> {

    @Query("SELECT p FROM Place p " +
            "JOIN p.placeTags pt " +
            "JOIN p.placeCategories pc " +
            "WHERE pt.tag IN :tags AND pc.category IN :categories " +
            "GROUP BY p.id " +
            "HAVING COUNT(DISTINCT pt.tag) = :tagCount AND COUNT(DISTINCT pc.category) = :categoryCount")
    List<Place> findPlacesByAllTagsAndCategories(
            @Param("tags") List<Tag> tags,
            @Param("categories") List<Category> categories,
            @Param("tagCount") Long tagCount,
            @Param("categoryCount") Long categoryCount
    );

}
