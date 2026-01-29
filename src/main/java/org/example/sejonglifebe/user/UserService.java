package org.example.sejonglifebe.user;

import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.auth.AuthUser;
import org.example.sejonglifebe.auth.PortalStudentInfo;
import org.example.sejonglifebe.common.jwt.JwtTokenProvider;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.example.sejonglifebe.place.PlaceRepository;
import org.example.sejonglifebe.place.favorite.FavoritePlaceRepository;
import org.example.sejonglifebe.place.favorite.FavoritePlaceService;
import org.example.sejonglifebe.review.Review;
import org.example.sejonglifebe.review.ReviewLikeRepository;
import org.example.sejonglifebe.review.ReviewRepository;
import org.example.sejonglifebe.s3.S3Service;
import org.example.sejonglifebe.user.dto.MyPageResponse;
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
    private final FavoritePlaceRepository favoritePlaceRepository;
    private final S3Service s3Service;

    @Transactional(readOnly = true)
    public Optional<User> findUserByStudentId(String studentId) {
        return userRepository.findByStudentId(studentId);
    }

    @Transactional
    public String createUser(SignUpRequest requestDto, PortalStudentInfo portalStudentInfo) {
        if (userRepository.existsByNickname(requestDto.getNickname())) {
            throw new SejongLifeException(ErrorCode.DUPLICATE_NICKNAME);
        }

        User newUser = User.builder()
                .studentId(portalStudentInfo.getStudentId())
                .nickname(requestDto.getNickname())
                .name(portalStudentInfo.getName())
                .department(portalStudentInfo.getDepartment())
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

    @Transactional
    public void updateStudentProfileIfChanged(Long userId, String name, String department) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new SejongLifeException(ErrorCode.USER_NOT_FOUND));

        boolean changed = false;

        if (name != null && !name.isBlank() && !name.equals(user.getName())) {
            changed = true;
        }
        if (department != null && !department.isBlank() && !department.equals(user.getDepartment())) {
            changed = true;
        }

        if (changed) {
            user.updateStudentProfile(name, department);
        }
    }

    @Transactional
    public MyPageResponse getMyPageInfo(AuthUser authUser) {
        User user = userRepository.findByStudentId(authUser.studentId())
                .orElseThrow(() -> new SejongLifeException(ErrorCode.USER_NOT_FOUND));
        String studentId = user.getStudentId().substring(0,2);
        String name = user.getName();
        String nickname = user.getNickname();
        String department = user.getDepartment();
        long favoriteCount = favoritePlaceRepository.countByUserStudentId(studentId);
        long reviewCount = reviewRepository.countByUserStudentId(studentId);
        return new MyPageResponse(
                name,
                nickname,
                studentId,
                department,
                favoriteCount,
                reviewCount
        );
    }

}
