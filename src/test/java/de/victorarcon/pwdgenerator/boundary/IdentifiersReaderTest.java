package de.victorarcon.pwdgenerator.boundary;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class IdentifiersReaderTest {

    @ParameterizedTest
    @MethodSource(value = "data")
    void read_users_from_list(List<String> users, Path path) {
        var identifiers = new IdentifiersReader(users, path)
                .read();

        assertThat(identifiers).containsExactly("a", "b", "c");
    }

    public static Stream<Arguments> data() {
        return Stream.of(
                arguments(List.of("a", "b", "c"), null),
                arguments(List.of("a", "b", "c"), Path.of("src/test/java/de/victorarcon/pwdgenerator/boundary/List-KennungendDB.txt"))
        );
    }

    @Test
    void read_users_from_file() {
        var identifiers = new IdentifiersReader(null, Path.of("src/test/java/de/victorarcon/pwdgenerator/boundary/List-KennungendDB.txt"))
                .read();

        assertThat(identifiers).containsExactly("dummy1", "dummy2");
    }

    @Test
    void list_is_empty() {
        var identifiers = new IdentifiersReader(null, null)
                .read();

        assertThat(identifiers).isEmpty();
    }
}