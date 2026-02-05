package org.example.sejonglifebe.review;

import org.example.sejonglifebe.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {

    boolean existsByReviewAndUser(Review review, User user);

    Optional<ReviewLike> findByReviewIdAndUserStudentId(Long reviewId, String studentId);

    List<ReviewLike> findByUserStudentId(String studentId);

    void deleteAllByUser(User user);

    @Query("""
            SELECT rl.review.id
            FROM ReviewLike rl
            WHERE rl.user = :user AND rl.review IN :reviews
            """)
    Set<Long> findLikedReviewIds(@Param("user") User user, @Param("reviews") List<Review> reviews);
}
