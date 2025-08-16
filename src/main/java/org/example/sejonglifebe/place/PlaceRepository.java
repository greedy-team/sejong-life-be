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
            "JOIN p.placeCategories pc " +
            "LEFT JOIN p.placeTags pt " +
            "WHERE pc.category = :category " +
            "AND (:tags IS EMPTY OR pt.tag IN :tags) " +
            "GROUP BY p.id " +
            "ORDER BY COUNT(DISTINCT pt.tag) DESC")
    List<Place> findPlacesByTagsAndCategory(
            @Param("category") Category category,
            @Param("tags") List<Tag> tags

    );

    @Query("SELECT p FROM Place p " +
            "LEFT JOIN p.placeTags pt " +
            "WHERE (:tags IS EMPTY OR pt.tag IN :tags) " +
            "GROUP BY p.id " +
            "ORDER BY COUNT(DISTINCT pt.tag) DESC")
    List<Place> findByTags(
            @Param("tags") List<Tag> tags
    );

}
