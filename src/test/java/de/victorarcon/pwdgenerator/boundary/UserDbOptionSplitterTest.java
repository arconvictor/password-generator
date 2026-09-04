package de.victorarcon.pwdgenerator.boundary;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertWith;
import static org.junit.jupiter.params.provider.Arguments.arguments;

final class UserDbOptionSplitterTest {

    @ParameterizedTest
    @MethodSource(value = "userDbOptions")

    void can_split_userDbOptions(String token, String expectedUsername, String expectedDbName, Long expectedPort, String expectedSid, String expectedSchema) {
        var testSubject = new UserDbOptionSplitter(token);

        assertWith(testSubject, userDbOptionSplitter -> {
            assertThat(userDbOptionSplitter.getUsername()).isEqualTo(expectedUsername);
            assertThat(userDbOptionSplitter.getDatabaseName()).isEqualTo(expectedDbName);
            assertThat(userDbOptionSplitter.getPortNumber()).isEqualTo(expectedPort);
            assertThat(userDbOptionSplitter.getSid()).isEqualTo(expectedSid);
            assertThat(userDbOptionSplitter.getSchema()).isEqualTo(expectedSchema);
        });
    }

    public static Stream<Arguments> userDbOptions() {
        return Stream.of(
                arguments("CHGPW_TEST@db01.example.com:1521:DB01", "CHGPW_TEST", "db01.example.com", 1521L, "DB01", "db01"),
                arguments("jdoe@db02.example.com:1521:DB02", "jdoe", "db02.example.com", 1521L, "DB02", "db02"),
                arguments("asmith@db03.example.com:1522:DB03", "asmith", "db03.example.com", 1522L, "DB03", "db03"),
                arguments("bwayne@db04.example.com:1525:DB04", "bwayne", "db04.example.com", 1525L, "DB04", "db04"),
                arguments("_@a.b-c.d:1:E", "_", "a.b-c.d", 1L, "E", "e"),
                arguments("user@dbname:1521:XE", "user", "dbname", 1521L, "XE", "xe")
        );
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {"u", "@", "@::", "..@::", "u@", "u:", "u@d", "u@d:", "u@d:x", "u@d:x:", "u@d:x:9", "u@d:x:s9", "u.@d.:x:s9", "u.@d.a:x:s9", "@@:a.b-c.d:1:E"})
    void cannot_split_userDbOptions(String input) {
        assertThatThrownBy(() -> new UserDbOptionSplitter(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid input string " + input);
    }

    @ParameterizedTest
    @NullSource
    void cannot_split_null(String input) {
        assertThatThrownBy(() -> new UserDbOptionSplitter(input))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("userDb must not be null");
    }
}