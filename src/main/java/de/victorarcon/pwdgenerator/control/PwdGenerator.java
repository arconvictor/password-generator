package de.victorarcon.pwdgenerator.control;

import org.apache.commons.text.RandomStringGenerator;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

/**
 * Generates secure, policy-compliant passwords for a list of user identifiers.
 *
 * This class uses {@link SecureRandom} and Apache Commons Text's {@link RandomStringGenerator}
 * to produce cryptographically strong passwords. Each password is validated using a {@link PwdValidator}
 * to ensure it meets all required security constraints (uppercase, lowercase, digit, symbol, etc.).
 *
 * Typical usage involves calling {@link #generateIdPassMap(List)} to generate a map of usernames to passwords.
 */
public final class PwdGenerator {

    private final PwdValidator validator;

    /**
     * Constructs a {@code PwdGenerator} with the given password validator.
     *
     * @param validator the validator used to enforce password policy rules
     */
    public PwdGenerator(PwdValidator validator) {
        this.validator = validator;
    }

    /**
     * Generates a map of user identifiers to secure, validated passwords.
     *
     * Each identifier is mapped to a unique password that satisfies the policy defined by {@link PwdValidator}.
     *
     * @param identifierslist the list of user identifiers
     * @return a map where each key is a user identifier and the value is a generated password
     */
    public Map<String, String> generateIdPassMap(List<String> identifierslist) {
        return identifierslist.stream()
                .collect(toMap(Function.identity(), pwdForIdentifier -> generate()));
    }

    /**
     * Generates a single secure password that satisfies all validation rules.
     *
     * The password is built using printable ASCII characters in the range {@code '!' (33)} to {@code '~' (126)}.
     * Generation continues in a loop until a valid password is produced.
     *
     * @return a validated, secure password string
     */
    private String generate() {
        var secureR = new SecureRandom();
        var generator = new RandomStringGenerator.Builder()
                .withinRange('!', '~')
                .usingRandom(secureR::nextInt)
                .build();

        var password = "";
        do {
            password = generator.generate(20);
        } while (!validator.isValid(password));

        return password;
    }
}
