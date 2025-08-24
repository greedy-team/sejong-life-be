package org.example.sejonglifebe.tag;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    @Query("""
                 SELECT DISTINCT t
                 FROM Tag t
                 JOIN t.placeTags pt
                 JOIN pt.place p
                 JOIN p.placeCategories pc
                 WHERE pc.category.id = :categoryId
            """)
    List<Tag> findAllByCategoryId(@Param("categoryId") Long categoryId);

    List<Tag> findByNameIn(List<String> names);

}
