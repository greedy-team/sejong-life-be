package org.example.sejonglifebe.user;

import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.auth.AuthUser;
import org.example.sejonglifebe.auth.PortalStudentInfo;
import org.example.sejonglifebe.auth.TokenIssuer;
import org.example.sejonglifebe.auth.dto.LoginResponse;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.example.sejonglifebe.place.favorite.FavoritePlaceRepository;
import org.example.sejonglifebe.review.Review;
import org.example.sejonglifebe.review.ReviewLikeRepository;
import org.example.sejonglifebe.review.ReviewRepository;
import org.example.sejonglifebe.common.storage.ImageStorage;
import org.example.sejonglifebe.place.entity.PlaceImage;
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
    private final TokenIssuer tokenIssuer;
    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final FavoritePlaceRepository favoritePlaceRepository;
    private final ImageStorage imageStorage;

    @Transactional(readOnly = true)
    public Optional<User> findUserByStudentId(String studentId) {
        return userRepository.findByStudentId(studentId);
    }

    @Transactional
    public LoginResponse createUser(SignUpRequest requestDto, PortalStudentInfo portalStudentInfo) {
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

        return tokenIssuer.issue(savedUser);
    }

    @Transactional
    public void deleteUser(AuthUser authUser) {
        User user = userRepository.findByStudentId(authUser.studentId())
                .orElseThrow(() -> new SejongLifeException(ErrorCode.USER_NOT_FOUND));

        List<Review> reviews = reviewRepository.findAllByUserOrderByCreatedAtDesc(user);

        for (Review review : reviews) {
            imageStorage.deleteImages(review.getPlaceImages().stream().map(PlaceImage::getUrl).toList());
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

    @Transactional(readOnly = true)
    public MyPageResponse getMyPageInfo(AuthUser authUser) {
        User user = userRepository.findByStudentId(authUser.studentId())
                .orElseThrow(() -> new SejongLifeException(ErrorCode.USER_NOT_FOUND));
        String studentId = user.getStudentId().substring(0,2);
        String name = user.getName();
        String nickname = user.getNickname();
        String department = user.getDepartment();
        long favoriteCount = favoritePlaceRepository.countByUserStudentId(user.getStudentId());
        long reviewCount = reviewRepository.countByUserStudentId(user.getStudentId());
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
