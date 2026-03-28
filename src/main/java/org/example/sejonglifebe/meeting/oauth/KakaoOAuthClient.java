package org.example.sejonglifebe.meeting.oauth;

import lombok.extern.slf4j.Slf4j;
import org.example.sejonglifebe.meeting.dto.KakaoTokenResponse;
import org.example.sejonglifebe.meeting.dto.KakaoUserInfo;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class KakaoOAuthClient {

    private final WebClient webClient;
    private final KakaoOAuthProperties properties;

    public KakaoOAuthClient(@Qualifier("kakaoWebClient") WebClient webClient, KakaoOAuthProperties properties) {
        this.webClient = webClient;
        this.properties = properties;
    }

    /**
     * 카카오 인가 코드를 액세스 토큰으로 교환
     */
    public KakaoTokenResponse getAccessToken(String code) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", properties.getClientId());
        formData.add("client_secret", properties.getClientSecret());
        formData.add("redirect_uri", properties.getRedirectUri());
        formData.add("code", code);

        try {
            return webClient.post()
                    .uri(properties.getTokenUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(formData)
                    .retrieve()
                    .bodyToMono(KakaoTokenResponse.class)
                    .block();
        } catch (Exception e) {
            log.error("카카오 토큰 발급 실패", e);
            throw new SejongLifeException(ErrorCode.KAKAO_TOKEN_REQUEST_FAILED);
        }
    }

    /**
     * 카카오 액세스 토큰으로 사용자 정보 조회
     */
    public KakaoUserInfo getUserInfo(String accessToken) {
        try {
            return webClient.get()
                    .uri(properties.getUserInfoUri())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(KakaoUserInfo.class)
                    .block();
        } catch (Exception e) {
            log.error("카카오 사용자 정보 조회 실패", e);
            throw new SejongLifeException(ErrorCode.KAKAO_USER_INFO_REQUEST_FAILED);
        }
    }
}
