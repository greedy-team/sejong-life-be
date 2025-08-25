package org.example.sejonglifebe.user;

import lombok.RequiredArgsConstructor;
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
}
