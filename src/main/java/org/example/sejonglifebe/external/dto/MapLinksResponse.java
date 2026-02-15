package org.example.sejonglifebe.external.dto;

public record MapLinksResponse(
        String kakaoUrl,
        String naverUrl,
        String googleUrl
) {}
