package com.lilithbloom;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordsTest {
  @Test
  void saltsDifferAndPasswordsVerify() {
    String first = Passwords.hash("Synthetic-password-2026");
    String second = Passwords.hash("Synthetic-password-2026");
    assertNotEquals(first, second);
    assertTrue(Passwords.verify("Synthetic-password-2026", first));
    assertFalse(Passwords.verify("wrong", first));
    assertFalse(first.contains("Synthetic-password"));
  }

  @Test
  void malformedHashesAreRejected() {
    for (String value :
        new String[] {
          "", "plaintext", "pbkdf2-sha256$1$x$x", "pbkdf2-sha256$600000$invalid$invalid"
        }) assertFalse(Passwords.verify("password", value));
  }

  @Test
  void csrfRequiresAnExactNonNullToken() {
    assertTrue(SecurityFilter.validToken("test-token", "test-token"));
    assertFalse(SecurityFilter.validToken("test-token", "different"));
    assertFalse(SecurityFilter.validToken(null, null));
    assertFalse(SecurityFilter.validToken("test-token", null));
  }
}
