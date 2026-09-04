package de.victorarcon.pwdgenerator.control;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PwdGeneratorTest {

    @Test
    void generatePasswords() {
        var testSubject = new PwdGenerator(new PwdValidator());

        var identifiers = List.of("USER1", "USER2");

        var actualresult = testSubject.generateIdPassMap(identifiers);

        assertEquals(2, actualresult.size());
    }
}