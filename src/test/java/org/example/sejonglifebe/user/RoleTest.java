package org.example.sejonglifebe.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoleTest {

    @Nested
    @DisplayName("includes 메서드 테스트")
    class IncludesTest {

        @Test
        @DisplayName("ADMIN은 USER 권한을 포함한다")
        void admin_includes_user() {
            assertThat(Role.ADMIN.includes(Role.USER)).isTrue();
        }

        @Test
        @DisplayName("ADMIN은 ADMIN 권한을 포함한다")
        void admin_includes_admin() {
            assertThat(Role.ADMIN.includes(Role.ADMIN)).isTrue();
        }

        @Test
        @DisplayName("USER는 USER 권한을 포함한다")
        void user_includes_user() {
            assertThat(Role.USER.includes(Role.USER)).isTrue();
        }

        @Test
        @DisplayName("USER는 ADMIN 권한을 포함하지 않는다")
        void user_not_includes_admin() {
            assertThat(Role.USER.includes(Role.ADMIN)).isFalse();
        }
    }

    @Nested
    @DisplayName("fromString 메서드 테스트")
    class FromStringTest {

        @Test
        @DisplayName("null이면 기본값 USER를 반환한다")
        void fromString_null_returns_user() {
            assertThat(Role.fromString(null)).isEqualTo(Role.USER);
        }

        @Test
        @DisplayName("USER 문자열이면 USER를 반환한다")
        void fromString_user_returns_user() {
            assertThat(Role.fromString("USER")).isEqualTo(Role.USER);
        }

        @Test
        @DisplayName("ADMIN 문자열이면 ADMIN을 반환한다")
        void fromString_admin_returns_admin() {
            assertThat(Role.fromString("ADMIN")).isEqualTo(Role.ADMIN);
        }

        @Test
        @DisplayName("유효하지 않은 문자열이면 예외를 던진다")
        void fromString_invalid_throws_exception() {
            assertThatThrownBy(() -> Role.fromString("INVALID"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
