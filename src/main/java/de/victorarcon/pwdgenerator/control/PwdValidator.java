package de.victorarcon.pwdgenerator.control;

/**
 * Validates password strings against a set of security and formatting rules.
 *
 * A password is considered valid if it:
 * - Contains at least one uppercase letter
 * - Contains at least one lowercase letter
 * - Contains at least one digit
 * - Contains at least one allowed symbol
 * - Does not contain any forbidden characters (e.g. &, ", ´, \, @, /, <, >, ;, ^, |)
 *
 * This validator is used to enforce password policies before storing credentials in Vault or the database.
 */
public final class PwdValidator {

    /**
     * Validates the given password string against all required rules.
     *
     * @param pwdInput the password string to validate
     * @return {@code true} if the password meets all criteria; {@code false} otherwise
     */
    public boolean isValid(String pwdInput) {
        return pwdContainsUpperCase(pwdInput) && pwdContainsLowerCase(pwdInput) && pwdContainsDigit(pwdInput) && pwdContainsSymbol(pwdInput) && containsNoForbiddenCharacters(pwdInput);
    }

    /**
     * Checks if the password contains at least one uppercase letter.
     *
     * @param pwdInput the password string
     * @return {@code true} if at least one uppercase character is present
     */
    private boolean pwdContainsUpperCase(String pwdInput) {
        return pwdInput.chars()
                .mapToObj(character -> (char) character)
                .anyMatch(Character::isUpperCase);
    }

    /**
     * Checks if the password contains at least one lowercase letter.
     *
     * @param pwdInput the password string
     * @return {@code true} if at least one lowercase character is present
     */
    private boolean pwdContainsLowerCase(String pwdInput) {
        return pwdInput.chars()
                .mapToObj(character -> (char) character)
                .anyMatch(Character::isLowerCase);
    }

    /**
     * Checks if the password contains at least one numeric digit.
     *
     * @param pwdInput the password string
     * @return {@code true} if at least one digit is present
     */
    private boolean pwdContainsDigit(String pwdInput) {
        return pwdInput.chars()
                .mapToObj(character -> (char) character)
                .anyMatch(Character::isDigit);
    }

    /**
     * Checks if the password contains at least one allowed symbol.
     *
     * Allowed symbols include: #, $, %, (, ), *, +, ,, -, ., /, :, =, ?, [, ], _, {, }, ~
     *
     * @param pwdInput the password string
     * @return {@code true} if at least one allowed symbol is present
     */
    private boolean pwdContainsSymbol(String pwdInput) {
        return pwdInput.chars()
                .anyMatch(characterAsInt -> characterAsInt >= 35 && characterAsInt <= 37 || characterAsInt >= 40 && characterAsInt <= 46 || characterAsInt == 58 || characterAsInt == 61 || characterAsInt == 63 || characterAsInt == 91 || characterAsInt == 93 || characterAsInt == 95 || characterAsInt == 123 || characterAsInt == 125 || characterAsInt == 126);
    }

    /**
     * Checks that the password does NOT contain any forbidden character — i.e. this
     * returns {@code true} when the password is clean, not when a forbidden character is found.
     *
     * Forbidden characters include: &amp;, ", ´, \, @, /, ', &lt;, &gt;, ;, ^, |
     *
     * @param pwdInput the password string
     * @return {@code true} if none of the forbidden characters are present
     */
    private boolean containsNoForbiddenCharacters(String pwdInput) {
        return pwdInput.matches("^[^&\"´\\\\@/'<>;^|]+$");
    }
}
