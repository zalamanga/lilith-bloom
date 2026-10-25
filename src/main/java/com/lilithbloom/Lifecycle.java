package com.lilithbloom;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;
import java.sql.*;
import java.util.Enumeration;
import javax.servlet.*;
import javax.servlet.annotation.WebListener;

@WebListener
public class Lifecycle implements ServletContextListener {
  @Override
  public void contextDestroyed(ServletContextEvent event) {
    try {
      Database.close();
    } finally {
      AbandonedConnectionCleanupThread.checkedShutdown();
      ClassLoader ownLoader = Lifecycle.class.getClassLoader();
      Enumeration<Driver> drivers = DriverManager.getDrivers();
      while (drivers.hasMoreElements()) {
        Driver driver = drivers.nextElement();
        if (driver.getClass().getClassLoader() == ownLoader) {
          try {
            DriverManager.deregisterDriver(driver);
          } catch (SQLException failure) {
            event.getServletContext().log("JDBC driver cleanup failed.", failure);
          }
        }
      }
    }
  }
}
