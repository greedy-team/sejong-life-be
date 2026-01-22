package org.example.sejonglifebe.place.view;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.example.sejonglifebe.exception.ErrorCode;
import org.example.sejonglifebe.exception.SejongLifeException;

public class ViewerKeyGenerator {

    private ViewerKeyGenerator() {}

    public static String extractClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if(realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    public static String ipUaHash(String ip, String ua) {
        if (ua == null) ua = "";
        return sha256Hex(ip + "|" + ua);
    }

    public static String ipUaHash(HttpServletRequest request) {
        String ip = extractClientIp(request);
        String ua = request.getHeader("User-Agent");
        if (ua == null) ua = "";
        return sha256Hex(ip + "|" + ua);
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length*2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new SejongLifeException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

}
