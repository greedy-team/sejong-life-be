package org.example.sejonglifebe.review;

import org.example.sejonglifebe.place.entity.Place;
import org.example.sejonglifebe.tag.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("""
                SELECT COUNT(r)
                FROM Review r
                JOIN r.reviewTags rt
                JOIN rt.tag t
                WHERE r.place = :place AND t = :tag
            """)
    long countByPlaceAndTag(@Param("place") Place place, @Param("tag") Tag tag);
}
