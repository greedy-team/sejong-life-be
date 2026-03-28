package org.example.sejonglifebe.meeting.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KakaoTokenResponse {

    private String access_token;
    private String token_type;
    private Integer expires_in;
    private String refresh_token;
    private Integer refresh_token_expires_in;
}
