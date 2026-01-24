package org.example.sejonglifebe.user;

import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.auth.AuthUser;
import org.example.sejonglifebe.common.jwt.JwtTokenProvider;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.example.sejonglifebe.review.Review;
import org.example.sejonglifebe.review.ReviewLikeRepository;
import org.example.sejonglifebe.review.ReviewRepository;
import org.example.sejonglifebe.s3.S3Service;
import org.example.sejonglifebe.user.dto.SignUpRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final S3Service s3Service;

    @Transactional(readOnly = true)
    public Optional<User> findUserByStudentId(String studentId) {
        return userRepository.findByStudentId(studentId);
    }

    @Transactional
    public String createUser(SignUpRequest requestDto) {
        if (userRepository.existsByNickname(requestDto.getNickname())) {
            throw new SejongLifeException(ErrorCode.DUPLICATE_NICKNAME);
        }

        User newUser = User.builder()
                .studentId(requestDto.getStudentId())
                .nickname(requestDto.getNickname())
                .build();

        User savedUser = userRepository.save(newUser);

        return jwtTokenProvider.createToken(savedUser);
    }

    @Transactional
    public void deleteUser(AuthUser authUser) {
        User user = userRepository.findByStudentId(authUser.studentId())
                .orElseThrow(() -> new SejongLifeException(ErrorCode.USER_NOT_FOUND));

        List<Review> reviews = reviewRepository.findAllByUserOrderByCreatedAtDesc(user);

        for (Review review : reviews) {
            s3Service.deleteImages(review.getPlaceImages());
            review.getPlace().removeReview(review);
            reviewRepository.delete(review);
        }

        reviewLikeRepository.deleteAllByUser(user);
        userRepository.delete(user);
    }
}
