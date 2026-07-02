package org.example.sejonglifebe.common.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.example.sejonglifebe.auth.AuthUser;
import org.example.sejonglifebe.user.Role;
import org.example.sejonglifebe.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private static final String SECRET_KEY = "8QTch7k82vFYqvLt13SJYQtlVyCfSoY2GIWMdqFH3zA=";
    private static final long EXPIRATION_TIME = 3600000L;
    private SecretKey key;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(jwtTokenProvider, "expirationTime", EXPIRATION_TIME);
        ReflectionTestUtils.setField(jwtTokenProvider, "signUpTokenExpirationTime", 600000L);
        jwtTokenProvider.init();

        key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    @Nested
    @DisplayName("토큰 생성 테스트")
    class CreateTokenTest {

        @Test
        @DisplayName("토큰 생성 시 role claim이 포함된다")
        void createToken_containsRoleClaim() {
            // given
            User user = User.builder()
                    .studentId("21011111")
                    .nickname("테스터")
                    .role(Role.ADMIN)
                    .build();

            // when
            String token = jwtTokenProvider.createToken(user);

            // then
            String role = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("role", String.class);

            assertThat(role).isEqualTo("ADMIN");
        }

        @Test
        @DisplayName("USER 역할로 토큰 생성 시 role이 USER로 설정된다")
        void createToken_withUserRole() {
            // given
            User user = User.builder()
                    .studentId("21011111")
                    .nickname("테스터")
                    .role(Role.USER)
                    .build();

            // when
            String token = jwtTokenProvider.createToken(user);

            // then
            String role = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("role", String.class);

            assertThat(role).isEqualTo("USER");
        }
    }

    @Nested
    @DisplayName("토큰 검증 및 파싱 테스트")
    class ValidateAndGetAuthUserTest {

        @Test
        @DisplayName("ADMIN role이 포함된 토큰을 파싱하면 ADMIN AuthUser를 반환한다")
        void validateAndGetAuthUser_withAdminRole() {
            // given
            User user = User.builder()
                    .studentId("21011111")
                    .nickname("관리자")
                    .role(Role.ADMIN)
                    .build();
            String token = jwtTokenProvider.createToken(user);

            // when
            AuthUser authUser = jwtTokenProvider.validateAndGetAuthUser(token);

            // then
            assertThat(authUser.studentId()).isEqualTo("21011111");
            assertThat(authUser.role()).isEqualTo(Role.ADMIN);
        }

        @Test
        @DisplayName("USER role이 포함된 토큰을 파싱하면 USER AuthUser를 반환한다")
        void validateAndGetAuthUser_withUserRole() {
            // given
            User user = User.builder()
                    .studentId("21011111")
                    .nickname("일반유저")
                    .role(Role.USER)
                    .build();
            String token = jwtTokenProvider.createToken(user);

            // when
            AuthUser authUser = jwtTokenProvider.validateAndGetAuthUser(token);

            // then
            assertThat(authUser.studentId()).isEqualTo("21011111");
            assertThat(authUser.role()).isEqualTo(Role.USER);
        }

        @Test
        @DisplayName("role claim이 없는 토큰을 파싱하면 기본값 USER를 반환한다")
        void validateAndGetAuthUser_withoutRoleClaim_returnsDefaultUser() {
            // given: role claim 없이 토큰 생성
            String tokenWithoutRole = Jwts.builder()
                    .subject("21011111")
                    .claim("nickname", "테스터")
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                    .signWith(key)
                    .compact();

            // when
            AuthUser authUser = jwtTokenProvider.validateAndGetAuthUser(tokenWithoutRole);

            // then
            assertThat(authUser.studentId()).isEqualTo("21011111");
            assertThat(authUser.role()).isEqualTo(Role.USER);
        }

        @Test
        @DisplayName("미팅 최종 토큰(type=meeting)으로 sejonglife 인증 시 INVALID_TOKEN 예외가 발생한다")
        void validateAndGetAuthUser_withMeetingToken_throwsInvalidToken() {
            // given
            String meetingToken = jwtTokenProvider.createMeetingToken("kakao_123456789");

            // when & then
            assertThatThrownBy(() -> jwtTokenProvider.validateAndGetAuthUser(meetingToken))
                    .isInstanceOf(SejongLifeException.class)
                    .hasMessage(ErrorCode.INVALID_TOKEN.getErrorMessage());
        }

        @Test
        @DisplayName("미팅 회원가입 토큰(type=meeting_signup)으로 sejonglife 인증 시 INVALID_TOKEN 예외가 발생한다")
        void validateAndGetAuthUser_withMeetingSignUpToken_throwsInvalidToken() {
            // given
            String meetingSignUpToken = jwtTokenProvider.createMeetingSignUpToken("kakao_123456789");

            // when & then
            assertThatThrownBy(() -> jwtTokenProvider.validateAndGetAuthUser(meetingSignUpToken))
                    .isInstanceOf(SejongLifeException.class)
                    .hasMessage(ErrorCode.INVALID_TOKEN.getErrorMessage());
        }
    }
}
