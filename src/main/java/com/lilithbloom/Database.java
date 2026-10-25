package com.lilithbloom;

import java.sql.SQLException;
import java.util.Properties;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.exception.JDBCConnectionException;
import com.lilithbloom.model.Account;
import com.lilithbloom.model.Reservation;

public final class Database {
  private static SessionFactory factory;

  private Database() {}

  public static synchronized SessionFactory factory() {
    if (factory == null) {
      Properties p = new Properties();
      p.setProperty("hibernate.connection.url", required("DB_URL"));
      p.setProperty("hibernate.connection.username", required("DB_USER"));
      p.setProperty("hibernate.connection.password", required("DB_PASSWORD"));
      if (!p.getProperty("hibernate.connection.url").startsWith("jdbc:mysql://")) {
        throw new IllegalStateException("DB_URL must use MySQL.");
      }
      p.setProperty("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver");
      p.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
      p.setProperty("hibernate.hbm2ddl.auto", "validate");
      p.setProperty("hibernate.connection.pool_size", "5");
      p.setProperty("hibernate.connection.connectTimeout", "5000");
      p.setProperty("hibernate.connection.socketTimeout", "5000");
      factory = build(p);
    }
    return factory;
  }

  public static SessionFactory build(Properties properties) {
    return new Configuration()
        .addProperties(properties)
        .addAnnotatedClass(Account.class)
        .addAnnotatedClass(Reservation.class)
        .buildSessionFactory();
  }

  private static String required(String name) {
    String value = System.getenv(name);
    if (value == null || value.isBlank())
      throw new IllegalStateException("Database environment is incomplete.");
    return value;
  }

  public static synchronized void close() {
    if (factory != null) {
      SessionFactory closing = factory;
      factory = null;
      closing.close();
    }
  }

  static boolean connectionFailure(Throwable failure) {
    Throwable current = failure;
    for (int depth = 0; current != null && depth < 16; depth++, current = current.getCause()) {
      if (current instanceof JDBCConnectionException) return true;
      if (current instanceof SQLException sql
          && sql.getSQLState() != null
          && sql.getSQLState().startsWith("08")) return true;
    }
    return false;
  }

  static synchronized void invalidate(SessionFactory failed, RuntimeException originalFailure) {
    if (factory != failed) return;
    factory = null;
    try {
      failed.close();
    } catch (RuntimeException cleanupFailure) {
      if (cleanupFailure != originalFailure) originalFailure.addSuppressed(cleanupFailure);
    }
  }
}
