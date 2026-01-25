package org.example.sejonglifebe.review;

import org.example.sejonglifebe.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {

    boolean existsByReviewAndUser(Review review, User user);

    Optional<ReviewLike> findByReviewIdAndUserStudentId(Long reviewId, String studentId);

    List<ReviewLike> findByUserStudentId(String studentId);

    void deleteAllByUser(User user);
}
