package com.project.foodwastagereductionapp;

import com.project.foodwastagereductionapp.data.PasswordHasher;
import org.junit.Test;
import static org.junit.Assert.*;

public class PasswordHasherTest {
    @Test public void matchesOnlyTheExactPasswordIncludingSpacesAndUnicode() {
        String password = "  비밀번호 café  ";
        String hash = PasswordHasher.hash(password);
        assertTrue(PasswordHasher.verify(password, hash));
        assertFalse(PasswordHasher.verify(password.trim(), hash));
        assertFalse(PasswordHasher.verify("wrong", hash));
        assertFalse(PasswordHasher.verify("", hash));
        assertFalse(hash.contains(password));
    }
    @Test public void generatesIndependentRandomSalts() {
        String first = PasswordHasher.hash("Coursework123");
        String second = PasswordHasher.hash("Coursework123");
        assertNotEquals(first, second);
        assertTrue(PasswordHasher.verify("Coursework123", first));
        assertTrue(PasswordHasher.verify("Coursework123", second));
    }
    @Test public void rejectsMalformedStoredCredentials() {
        for (String hash : new String[]{"", "plainpassword", "pbkdf2_sha1$0$00$00",
                "pbkdf2_sha1$120000$nothex$00", "pbkdf2_sha1$999999999$00$00"}) {
            assertFalse(PasswordHasher.verify("password", hash));
        }
        assertFalse(PasswordHasher.verify(null, "anything"));
        assertFalse(PasswordHasher.verify("password", null));
    }
}
