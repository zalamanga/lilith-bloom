package com.lilithbloom;

import java.sql.SQLException;
import org.hibernate.exception.JDBCConnectionException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ConnectionFailureTest {
  @Test
  void recognizesBrokenConnectionWithoutTreatingValidationAsAnOutage() {
    assertTrue(
        Database.connectionFailure(
            new IllegalStateException(new SQLException("offline", "08S01"))));
    assertTrue(
        Database.connectionFailure(
            new JDBCConnectionException("offline", new SQLException("closed", "08003"))));
    assertFalse(Database.connectionFailure(new SQLException("duplicate", "23000")));
    assertFalse(Database.connectionFailure(new IllegalArgumentException("validation")));
  }
}
