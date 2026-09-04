package de.victorarcon.pwdgenerator.boundary;

import de.victorarcon.pwdgenerator.control.Try;
import de.victorarcon.pwdgenerator.entity.DataSource;
import de.victorarcon.pwdgenerator.entity.UserDataRepository;

import java.util.List;

/**
 * Splits a list of usernames into those that exist in the database and those that don't,
 * by querying {@link UserDataRepository} once at construction time.
 *
 * Used by {@link PasswordUpdater} to only attempt password rotation for users that are
 * actually present in the database, while still reporting the ones that weren't found.
 */
public class UserReport {

    private final List<Try<String>> existingUsers;

    private final List<Try<String>> nonExistingUsers;

    /**
     * Constructs a {@code UserReport} by querying the database for the given identifiers.
     *
     * @param dataSource the data source used to access the database
     * @param identifiers the list of user identifiers to check
     */
    public UserReport(DataSource dataSource, List<String> identifiers) {
        var userDataRepository = new UserDataRepository(dataSource);
        var usersReport = userDataRepository.buildUsersReport(identifiers);

        existingUsers = getExistingUsers(usersReport);
        nonExistingUsers = getNonExistingUsers(usersReport);
    }

    private List<Try<String>> getExistingUsers(List<Try<String>> usersReport) {
        return usersReport
                .stream()
                .filter(Try::isSuccess)
                .toList();
    }

    private List<Try<String>> getNonExistingUsers(List<Try<String>> usersReport) {
        return usersReport
                .stream()
                .filter(stringTry -> !stringTry.isSuccess())
                .toList();
    }

    List<Try<String>> getExistingUsers() {
        return existingUsers;
    }

    List<Try<String>> getNonExistingUsers() {
        return nonExistingUsers;
    }
}
