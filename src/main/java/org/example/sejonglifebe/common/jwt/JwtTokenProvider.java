package org.example.sejonglifebe.common.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.example.sejonglifebe.auth.dto.LoginUser;
import jakarta.annotation.PostConstruct;
import org.example.sejonglifebe.auth.dto.PortalStudentInfo;
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
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public String createSignUpToken(PortalStudentInfo portalInfo) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + signUpTokenExpirationTime);

        return Jwts.builder()
                .subject(portalInfo.studentId())
                .claim("name", portalInfo.studentName())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public PortalStudentInfo validateAndGetPortalInfo(String token) {
        Claims claims = getClaims(token);
        return new PortalStudentInfo(
                claims.getSubject(),
                claims.get("name", String.class)
        );
    }

    public LoginUser validateAndGetAuthUser(String token) {
        Claims claims = getClaims(token);
        return new LoginUser(claims.getSubject());
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
