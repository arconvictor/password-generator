package de.victorarcon.pwdgenerator.boundary;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Resolves the final list of user identifiers to process, from either a direct list
 * (CLI {@code -u}) or a text file (CLI {@code -f}).
 */
final class IdentifiersReader {

    private static final Logger LOG = LogManager.getLogger(IdentifiersReader.class);

    private final List<String> usernames;

    private final Path path;

    IdentifiersReader(List<String> usernames, Path path) {
        this.usernames = usernames;
        this.path = path;
    }

    /**
     * Returns the list of users to process.
     *
     * If a list of users is provided directly, it is trimmed and blank entries are removed.
     * Otherwise, if a file path is provided, identifiers are read from the file as-is
     * (one per line, no trimming or blank-line filtering — see {@link #readIdentifiersFromFile}).
     * If both a list and a path are provided, the list takes priority and the file is ignored.
     *
     * <p><b>Note:</b> neither path uppercases identifiers, and the two input sources are
     * normalized differently (trimmed vs. raw) — a username with stray whitespace in a
     * file will not be cleaned up the way a CLI-provided one is.
     *
     * @return the list of users
     */
    List<String> read() {
        if (usernames != null) {
            return usernames.stream()
                    .map(String::trim)
                    .filter(id -> !id.isEmpty())
                    .toList();
        } else if (path != null) {
            return readIdentifiersFromFile(path);
        }

        return List.of();
    }

    /**
     * Reads user identifiers from a file located at the given path.
     *
     * Each line in the file is treated as a separate identifier, exactly as written
     * (no trimming or blank-line filtering is applied here).
     * If the file cannot be read due to an I/O error, the method logs the error and returns an empty list.
     *
     * @param path the path to the file containing user identifiers
     * @return a list of identifiers read from the file, or an empty list if reading fails
     */
    public List<String> readIdentifiersFromFile(Path path) {
        try {
            return Files.readAllLines(path)
                    .stream()
                    .toList();

        } catch (IOException e) {
            LOG.error(e);

            return List.of();
        }
    }

}
