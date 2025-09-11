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
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
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

        RequestBody formBody = new FormBody.Builder()
                .add("mainLogin", "Y")
                .add("rtUrl", RETURN_URL)
                .add("id", portalId)
                .add("password", portalPassword)
                .build();

        Request request = new Request.Builder()
                .url(LOGIN_URL)
                .post(formBody)
                .header("Host", HOST)
                .header("Referer", REFERER)
                .header("Cookie", COOKIE)
                .build();

        try (Response response = executeWithRetry(request)) {
            if (!response.isSuccessful()) {
                throw new SejongLifeException(ErrorCode.PORTAL_INVALID_CREDENTIALS);
            }
        }

        // 로그인 이후 SSO 요청 (세션 연결 유지 목적)
        Request ssoRequest = new Request.Builder().url(SSO_URL).get().build();
        try (Response ssoResponse = client.newCall(ssoRequest).execute()) {
            if (!ssoResponse.isSuccessful()) { // SSO 요청 실패
                throw new SejongLifeException(ErrorCode.PORTAL_SSO_FAILED);
            }
        } catch (IOException e) {
            throw new SejongLifeException(ErrorCode.PORTAL_NETWORK_ERROR, e);
        }
    }

    public String fetchHtml() {
        String url = HTML_URL;
        Request request = new Request.Builder().url(url).get().build();

        try (Response response = client.newCall(request).execute()) {
            if (response.body() == null || response.code() != 200) { //고전독서 인증현황 페이지 조회 실패
                throw new SejongLifeException(ErrorCode.PORTAL_NETWORK_ERROR);
            }
            return response.body().string();
        } catch (IOException e) {
            throw new SejongLifeException(ErrorCode.PORTAL_NETWORK_ERROR, e);
        }
    }

    private Response executeWithRetry(Request request) {
        int tryCount = 0;
        while (tryCount < RETRY_COUNT) {
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    return response;
                }
            } catch (IOException e) {
                tryCount++;
                log.warn("[PortalLogin] 네트워크 오류 발생 -> 재시도... ({}회), 오류: {}", tryCount, e.getMessage());
            }
        }
        throw new SejongLifeException(ErrorCode.PORTAL_NETWORK_ERROR);
    }
}
