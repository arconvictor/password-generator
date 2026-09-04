package de.victorarcon.pwdgenerator.boundary;

import de.victorarcon.pwdgenerator.control.Try;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import java.util.List;

/**
 * Reports the outcome of a password-rotation run to the log: how many identifiers were
 * received, how many were updated successfully, and the error details for the rest.
 *
 * @param report the per-user results of the update attempt
 * @param identifiers the full list of identifiers that were requested
 */
public record StatisticsLogger(List<Try<String>> report, List<String> identifiers) {

    private static final Logger LOG = LogManager.getLogger(StatisticsLogger.class);

    /**
     * Logs a summary of the password update process.
     *
     * This method analyzes the update report and outputs:
     * - Total number of identifiers received
     * - Number of successful updates
     * - Number of errors
     * - List of successfully updated identifiers
     * - Detailed error messages for failed updates
     *
     * Logging is performed using Log4j at INFO and ERROR levels.
     * Useful for auditing and debugging the outcome of batch password operations.
     */
    public void log() {
        var successfulUpdates = report.stream()
                .filter(Try::isSuccess)
                .map(Try::getResult)
                .toList();

        var successfulUpdatesCount = successfulUpdates.size();
        var updatedIdentifiers = Strings.join(successfulUpdates, ',');

        var errorsCount = report.stream()
                .filter(update -> !update.isSuccess())
                .count();

        var errorMessages = report.stream()
                .filter(stringTry -> !stringTry.isSuccess())
                .map(Try::getError)
                .map(Throwable::toString)
                .toList();

        LOG.info("Received {} identifiers. Of those, {} were successfully updated, while {} resulted in an error", identifiers.size(), successfulUpdatesCount, errorsCount);
        LOG.error("Successful updates: [{}]", updatedIdentifiers);
        LOG.error("Error details: {}", errorMessages);
    }

}
