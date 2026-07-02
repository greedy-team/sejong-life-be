package org.example.sejonglifebe.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@Scope("prototype")
@RequiredArgsConstructor
public class PortalClient {

    private static final String LOGIN_URL = "https://portal.sejong.ac.kr/jsp/login/login_action.jsp";
    private static final String RETURN_URL = "http://classic.sejong.ac.kr/_custom/sejong/sso/sso-return.jsp?returnUrl=https%3A%2F%2Fclassic.sejong.ac.kr%2Fclassic%2Findex.do";
    private static final String HOST = "portal.sejong.ac.kr";
    private static final String REFERER = "https://portal.sejong.ac.kr";
    private static final String COOKIE = "chknos=false";
    private static final String SSO_URL = "http://classic.sejong.ac.kr/_custom/sejong/sso/sso-return.jsp?returnUrl=https://classic.sejong.ac.kr/classic/index.do";
    private static final String HTML_URL = "https://classic.sejong.ac.kr/classic/reading/status.do";

    private static final int RETRY_COUNT = 3;

    private final OkHttpClient client;

    public void login(String portalId, String portalPassword) {
        String loginUrl = LOGIN_URL;

        RequestBody formBody = new FormBody.Builder()
                .add("mainLogin", "Y")
                .add("rtUrl", RETURN_URL)
                .add("id", portalId)
                .add("password", portalPassword)
                .build();

        Request request = new Request.Builder()
                .url(loginUrl)
                .post(formBody)
                .header("Host", HOST)
                .header("Referer", REFERER)
                .header("Cookie", COOKIE)
                .build();

        try (Response response = executeWithRetry(request)) {
            if (!response.isSuccessful()) { // 포털 로그인 실패
                throw new SejongLifeException(ErrorCode.PORTAL_LOGIN_FAILED);
            }
        }

        // 로그인 이후 SSO 요청 (세션 연결 유지 목적)
        String ssoUrl = SSO_URL;
        Request ssoRequest = new Request.Builder().url(ssoUrl).get().build();

        try (Response ssoResponse = client.newCall(ssoRequest).execute()) {
            if (!ssoResponse.isSuccessful()) { // SSO 요청 실패
                log.error("SSO 요청 실패 - code: {}, message: {}", ssoResponse.code(), ssoResponse.message());
                throw new SejongLifeException(ErrorCode.PORTAL_CONNECTION_ERROR);
            }
        } catch (IOException e) {
            log.error("SSO IOException", e);
            throw new SejongLifeException(ErrorCode.PORTAL_CONNECTION_ERROR, e);
        }
    }

    public String fetchHtml() {
        String url = HTML_URL;
        Request request = new Request.Builder().url(url).get().build();

        try (Response response = client.newCall(request).execute()) {
            if (response.body() == null || response.code() != 200) { //고전독서 인증현황 페이지 조회 실패
                log.error("고전독서 페이지 조회 실패 - code: {}, message: {}, body: {}",
                        response.code(), response.message(), response.body());
                throw new SejongLifeException(ErrorCode.PORTAL_CONNECTION_ERROR);
            }
            return response.body().string();
        } catch (IOException e) {
            log.error("fetchHtml IOException", e);
            throw new SejongLifeException(ErrorCode.PORTAL_CONNECTION_ERROR, e);
        }
    }

    private Response executeWithRetry(Request request) {
        Response lastResponse = null;

        for (int tryCount = 1; tryCount <= RETRY_COUNT; tryCount++) {
            try {
                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    return response;
                }

                // 실패한 응답은 리소스 해제
                lastResponse = response;
                response.close();

                log.error("[PortalLogin] HTTP 오류 발생 -> 재시도 {}/{}, 상태 코드: {}",
                        tryCount, RETRY_COUNT, response.code());

            } catch (IOException e) {
                log.error("[PortalLogin] 네트워크 오류 발생 -> 재시도 {}/{}",
                        tryCount, RETRY_COUNT, e);
            }

            // 마지막 시도가 아니면 대기 (Exponential backoff)
            if (tryCount < RETRY_COUNT) {
                try {
                    long waitTime = (long) Math.pow(2, tryCount - 1) * 1000; // 1초, 2초, 4초
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("재시도 대기 중 인터럽트 발생", e);
                    throw new SejongLifeException(ErrorCode.PORTAL_CONNECTION_ERROR);
                }
            }
        }

        log.error("모든 재시도 실패 - 최종 응답: {}", lastResponse);
        throw new SejongLifeException(ErrorCode.PORTAL_CONNECTION_ERROR);
    }
}
