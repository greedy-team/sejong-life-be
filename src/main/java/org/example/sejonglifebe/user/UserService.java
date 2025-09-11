package org.example.sejonglifebe.user;

import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.auth.dto.PortalStudentInfo;
import org.example.sejonglifebe.common.jwt.JwtTokenProvider;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.example.sejonglifebe.user.dto.SignUpRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional(readOnly = true)
    public Optional<User> findUserByStudentId(String studentId) {
        return userRepository.findByStudentId(studentId);
    }

    @Transactional
    public User createUser(String signUpToken, SignUpRequest request) {
        PortalStudentInfo studentInfo = jwtTokenProvider.validateAndGetPortalInfo(signUpToken);

        if (!studentInfo.studentId().equals(request.getStudentId()) ||
                !studentInfo.studentName().equals(request.getName())) {
            throw new SejongLifeException(ErrorCode.USER_INFO_MISMATCH);
        }

        User newUser = User.builder()
                .studentId(request.getStudentId())
                .nickname(request.getNickname())
                .build();

        return userRepository.save(newUser);
    }
}
