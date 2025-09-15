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
            "WHERE pc.category = :category AND pt.tag IN :tags " + // 1️⃣ 카테고리와 태그로 1차 필터링
            "GROUP BY p.id " +
            "HAVING COUNT(DISTINCT pt.tag) = :tagCount") // 2️⃣ 모든 태그를 가졌는지 최종 확인
    List<Place> findPlacesByTagsAndCategoryContainingAllTags(
            @Param("category") Category category,
            @Param("tags") List<Tag> tags,
            @Param("tagCount") Long tagCount // 🎯 태그 개수 파라미터 추가
    );

    @Query("SELECT p FROM Place p " +
            "JOIN p.placeTags pt " +
            "WHERE pt.tag IN :tags " +
            "GROUP BY p.id " +
            "HAVING COUNT(DISTINCT pt.tag) = :tagCount")
    List<Place> findByTags(
            @Param("tags") List<Tag> tags,
            @Param("tagCount") Long tagCount
    );

    @Query("SELECT p FROM Place p JOIN p.placeCategories pc WHERE pc.category = :category")
    List<Place> findByCategory(@Param("category") Category category);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Place p SET p.viewCount = p.viewCount + 1, p.weeklyViewCount = p.weeklyViewCount + 1 WHERE p.id = :id")
    void increaseViewCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Place p SET p.weeklyViewCount = 0")
    void resetAllWeeklyViewCounts();

    List<Place> findTop10ByOrderByWeeklyViewCountDesc();

}
