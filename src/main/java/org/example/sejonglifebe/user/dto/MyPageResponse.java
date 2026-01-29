package org.example.sejonglifebe.user.dto;

public record MyPageResponse(
    String name,
    String nickname,
    String studentId,
    String department,
    long favoriteCount,
    long reviewCount
) { }
