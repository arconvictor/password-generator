package de.victorarcon;

import de.victorarcon.pwdgenerator.boundary.CmdPasswordGen;
import picocli.CommandLine;

/**
 * CLI entry point. Delegates all argument parsing and execution to {@link CmdPasswordGen}
 * via Picocli, and exits the JVM with whatever exit code the command returns.
 */
public class Main {

    public static void main(String[] args) {
        var exitCode = new CommandLine(new CmdPasswordGen()).execute(args);

        System.exit(exitCode);
    }
}
