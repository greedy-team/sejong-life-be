package org.example.sejonglifebe.review;

import java.util.List;
import org.example.sejonglifebe.place.entity.Place;
import org.example.sejonglifebe.review.dto.RatingCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByPlace(Place place);

    Long countByPlace(Place place);

    @Query("SELECT r.rating as rating, COUNT(r) as count FROM Review r WHERE r.place = :place GROUP BY r.rating")
    List<RatingCount> findRatingCountsByPlace(@Param("place") Place place);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.place = :place")
    Double averageRatingByPlace(@Param("place") Place place);
}
