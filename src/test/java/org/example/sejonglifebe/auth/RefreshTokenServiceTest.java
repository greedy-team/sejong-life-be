package org.example.sejonglifebe.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    StringRedisTemplate redisTemplate;

    @Mock
    ValueOperations<String, String> valueOperations;

    @InjectMocks
    RefreshTokenService refreshTokenService;

    @Test
    @DisplayName("save는 refresh:{studentId} 키로 TTL과 함께 저장한다")
    void save_storesWithKeyPrefixAndTtl() {
        // given
        ReflectionTestUtils.setField(refreshTokenService, "refreshExpirationTime", 1_000_000L);
        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        // when
        refreshTokenService.save("21011111", "refresh-token-value");

        // then
        verify(valueOperations).set("refresh:21011111", "refresh-token-value", Duration.ofMillis(1_000_000L));
    }

    @Test
    @DisplayName("저장된 토큰과 일치하면 matches는 true를 반환한다")
    void matches_returnsTrue_whenStoredValueEquals() {
        // given
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("refresh:21011111")).willReturn("stored-token");

        // when & then
        assertThat(refreshTokenService.matches("21011111", "stored-token")).isTrue();
    }

    @Test
    @DisplayName("저장된 토큰과 다르면 matches는 false를 반환한다")
    void matches_returnsFalse_whenStoredValueDiffers() {
        // given
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("refresh:21011111")).willReturn("stored-token");

        // when & then
        assertThat(refreshTokenService.matches("21011111", "other-token")).isFalse();
    }

    @Test
    @DisplayName("저장된 토큰이 없으면 matches는 false를 반환한다")
    void matches_returnsFalse_whenNoStoredValue() {
        // given
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(anyString())).willReturn(null);

        // when & then
        assertThat(refreshTokenService.matches("21011111", "any-token")).isFalse();
    }

    @Test
    @DisplayName("delete는 refresh:{studentId} 키를 삭제한다")
    void delete_removesKey() {
        // when
        refreshTokenService.delete("21011111");

        // then
        verify(redisTemplate).delete("refresh:21011111");
    }
}
