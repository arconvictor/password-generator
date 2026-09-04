package de.victorarcon.pwdgenerator.boundary;

import de.victorarcon.pwdgenerator.control.DbSecretException;
import de.victorarcon.pwdgenerator.control.Failure;
import de.victorarcon.pwdgenerator.control.Success;
import de.victorarcon.pwdgenerator.entity.DataSource;
import de.victorarcon.pwdgenerator.entity.OracleDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;


final class UserReportTest extends VaultAndDbBootstrap {

    private static DataSource dataSource;

    @BeforeAll
    static void startContainers() {
        dataSource = new OracleDataSource(DB_HOST_ADDRESS, "testUser", "password".toCharArray());
    }

    @Test
    void can_find_existing_user() {
        var testSubject = new UserReport(dataSource, List.of("dummy1", "not_existing"));

        assertThat(testSubject.getExistingUsers()).containsExactly(new Success<>("dummy1"));
        assertThat(testSubject.getNonExistingUsers()).containsExactly(new Failure<>(DbSecretException.forSecret("not_existing", new SQLException("not_existing could not be found in the DB"))));
    }

    @ParameterizedTest
    @MethodSource(value = "users")
    void can_find_all_users(List<String> users) {
        var expectedReport = users.stream()
                .map(Success::new)
                .toList();

        var testSubject = new UserReport(dataSource, users);

        assertThat(testSubject.getExistingUsers()).isEqualTo(expectedReport);
        assertThat(testSubject.getNonExistingUsers()).isEmpty();
    }

    public static Stream<Arguments> users() {
        return Stream.of(
                arguments(List.of("DUMMY1", "DUMMY2", "DUMMY3")),
                arguments(List.of("dummy1", "DUMMY2", "DUMMY3")),
                arguments(List.of("DUMMY1", "dummy2", "DUMMY3")),
                arguments(List.of("dummy1", "dummy2", "DUMMY3")),
                arguments(List.of("DUMMY1", "DUMMY2", "dummy3")),
                arguments(List.of("dummy1", "DUMMY2", "dummy3")),
                arguments(List.of("DUMMY1", "dummy2", "dummy3")),
                arguments(List.of("dummy1", "dummy2", "dummy3"))
        );
    }

    @Test
    void cannot_find_any_user() {
        var testSubject = new UserReport(dataSource, List.of("not_existing_1", "not_existing_2"));

        assertThat(testSubject.getExistingUsers()).isEmpty();
        assertThat(testSubject.getNonExistingUsers()).containsExactly(
                new Failure<>(DbSecretException.forSecret("not_existing_1", new SQLException("not_existing_1 could not be found in the DB"))),
                new Failure<>(DbSecretException.forSecret("not_existing_2", new SQLException("not_existing_2 could not be found in the DB")))
        );
    }
}