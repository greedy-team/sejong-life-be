package org.example.sejonglifebe.auth.dto;

public record LoginResult(
        boolean isNewUser,
        String token,
        PortalStudentInfo studentInfo
) {

    // 기존 회원
    public static LoginResult forExistingUser(String accessToken) {
        return new LoginResult(false, accessToken, null);
    }

    // 신규 회원
    public static LoginResult forNewUser(String signUpToken, PortalStudentInfo studentInfo) {
        return new LoginResult(true, signUpToken, studentInfo);
    }
}
