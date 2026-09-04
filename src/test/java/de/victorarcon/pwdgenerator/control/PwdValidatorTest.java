package de.victorarcon.pwdgenerator.control;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;

class PwdValidatorTest {

    private static PwdValidator testSubject;

    @BeforeAll
    static void beforeAll() {
        testSubject = new PwdValidator();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Hanse2024Hello\"Word", "Hanse2020Admin", "HanseAdmin!§Top&", "hanseadmin2024(+*", "Hanse2024&HelloWord", "Hanse2024\"HelloWord", "Hanse2024\\HelloWord", "Hanse2024/HelloWord", "Hanse2024'HelloWord", "Hanse2024@HelloWord", "Hanse2024<HelloWord", "Hanse2024>HelloWord", "Hanse2024´HelloWord", "Hanse2024|HelloWord", "Hanse2024^HelloWord", "Hanse2024;HelloWord"})
        //@ValueSource(strings = {"HansexxXX1985)("})
    void passwordValidateWithSymbolsNotAllowed(String password) {
        var actualResult = testSubject.isValid(password);

        assertFalse(actualResult);
    }
}
