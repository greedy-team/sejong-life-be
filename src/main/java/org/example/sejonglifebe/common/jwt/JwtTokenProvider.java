package org.example.sejonglifebe.common.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.example.sejonglifebe.auth.AuthUser;
import jakarta.annotation.PostConstruct;
import org.example.sejonglifebe.auth.PortalStudentInfo;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.example.sejonglifebe.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expirationTime;

    @Value("${jwt.signup-expiration}")
    private long signUpTokenExpirationTime;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .subject(user.getStudentId())
                .claim("nickname", user.getNickname()) // 닉네임 포함 여부 논의 필요
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    // 회원가입용 임시 토큰
    public String createSignUpToken(PortalStudentInfo portalInfo) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + signUpTokenExpirationTime);

        return Jwts.builder()
                .subject(portalInfo.getStudentId())
                .claim("name", portalInfo.getName())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public PortalStudentInfo validateAndGetPortalInfo(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return PortalStudentInfo.builder()
                    .studentId(claims.getSubject())
                    .name(claims.get("name", String.class))
                    .build();

        } catch (ExpiredJwtException e) {
            throw new SejongLifeException(ErrorCode.EXPIRED_TOKEN);

        } catch (MalformedJwtException e) {
            throw new SejongLifeException(ErrorCode.MALFORMED_TOKEN);

        } catch (SignatureException e) {
            throw new SejongLifeException(ErrorCode.INVALID_TOKEN);

        } catch (Exception e) {
            throw new SejongLifeException(ErrorCode.INVALID_TOKEN);
        }
    }

    public AuthUser validateAndGetAuthUser(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return new AuthUser(claims.getSubject());

        } catch (ExpiredJwtException e) {
            throw new SejongLifeException(ErrorCode.EXPIRED_TOKEN);

        } catch (MalformedJwtException e) {
            throw new SejongLifeException(ErrorCode.MALFORMED_TOKEN);

        } catch (SignatureException e) {
            throw new SejongLifeException(ErrorCode.INVALID_TOKEN);

        } catch (Exception e) {
            throw new SejongLifeException(ErrorCode.INVALID_TOKEN);
        }
    }
}
