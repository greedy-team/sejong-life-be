package org.example.sejonglifebe.meeting.auth;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.sejonglifebe.common.jwt.JwtTokenExtractor;
import org.example.sejonglifebe.common.jwt.JwtTokenProvider;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;
import org.example.sejonglifebe.meeting.dto.MeetingAuthUser;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class MeetingAuthUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final JwtTokenExtractor jwtTokenExtractor;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getGenericParameterType().equals(MeetingAuthUser.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null) {
            throw new SejongLifeException(ErrorCode.INVALID_AUTH_HEADER);
        }

        String token = jwtTokenExtractor.extractToken(authHeader);
        if (token.isBlank()) {
            throw new SejongLifeException(ErrorCode.INVALID_AUTH_HEADER);
        }

        String kakaoId = jwtTokenProvider.validateMeetingToken(token);
        return new MeetingAuthUser(kakaoId);
    }
}
