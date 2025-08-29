package org.example.sejonglifebe.review;

import org.example.sejonglifebe.place.entity.Place;
import org.example.sejonglifebe.tag.Tag;
import java.util.List;
import org.example.sejonglifebe.review.dto.RatingCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    List<Review> findByPlace(Place place);

    Long countByPlace(Place place);

    @Query("SELECT r.rating as rating, COUNT(r) as count FROM Review r WHERE r.place = :place GROUP BY r.rating")
    List<RatingCount> findRatingCountsByPlace(@Param("place") Place place);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.place = :place")
    Double averageRatingByPlace(@Param("place") Place place);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Review r SET r.likeCount = r.likeCount + 1 WHERE r.id = :reviewId")
    void incrementLikeCount(@Param("reviewId") Long reviewId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Review r SET r.likeCount = r.likeCount - 1 WHERE r.id = :reviewId AND r.likeCount > 0")
    int decrementLikeCount(@Param("reviewId") Long reviewId);
}
