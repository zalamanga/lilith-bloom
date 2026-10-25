package com.lilithbloom;

import java.security.*;
import java.util.*;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class Passwords {
  private static final int ITERATIONS = 600_000;

  private Passwords() {}

  public static String hash(String password) {
    byte[] salt = new byte[16];
    new SecureRandom().nextBytes(salt);
    return "pbkdf2-sha256$"
        + ITERATIONS
        + "$"
        + Base64.getEncoder().encodeToString(salt)
        + "$"
        + Base64.getEncoder().encodeToString(derive(password, salt, ITERATIONS));
  }

  public static boolean verify(String password, String encoded) {
    try {
      String[] parts = encoded.split("\\$");
      if (parts.length != 4 || !parts[0].equals("pbkdf2-sha256")) return false;
      int rounds = Integer.parseInt(parts[1]);
      if (rounds < 100_000 || rounds > 1_000_000) return false;
      byte[] salt = Base64.getDecoder().decode(parts[2]);
      byte[] expected = Base64.getDecoder().decode(parts[3]);
      return salt.length == 16
          && expected.length == 32
          && MessageDigest.isEqual(expected, derive(password, salt, rounds));
    } catch (RuntimeException e) {
      return false;
    }
  }

  private static byte[] derive(String password, byte[] salt, int rounds) {
    PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, rounds, 256);
    try {
      return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("Password hashing is unavailable.", e);
    } finally {
      spec.clearPassword();
    }
  }
}
