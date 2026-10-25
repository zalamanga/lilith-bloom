package com.lilithbloom.web;

import com.lilithbloom.*;
import java.io.Serializable;
import javax.faces.application.FacesMessage;
import javax.faces.bean.*;
import javax.faces.context.FacesContext;
import javax.servlet.http.*;
import com.lilithbloom.model.Account;

@ManagedBean(name = "auth")
@SessionScoped
public class AuthBean implements Serializable {
  private String email, password, name;
  private int failures;
  private long retryAfter;

  public String login() {
    FacesContext context = FacesContext.getCurrentInstance();
    try {
      if (System.currentTimeMillis() < retryAfter) {
        error("Terlalu banyak percobaan. Tunggu 30 detik lalu coba lagi.");
        return null;
      }
      Account employee = new Store().authenticate(email, password);
      if (employee == null) {
        if (++failures >= 5) {
          retryAfter = System.currentTimeMillis() + 30_000;
          failures = 0;
        }
        error("Email atau password tidak cocok.");
        return null;
      }
      HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
      request.changeSessionId();
      HttpSession session = request.getSession();
      session.removeAttribute("csrf");
      SecurityFilter.token(session);
      session.setAttribute("accountId", employee.getId());
      name = employee.getName();
      failures = 0;
      retryAfter = 0;
      return "reservations?faces-redirect=true";
    } catch (IllegalArgumentException e) {
      error(e.getMessage());
      return null;
    } catch (RuntimeException e) {
      error("Login belum dapat diproses. Periksa koneksi database lalu coba lagi.");
      return null;
    } finally {
      password = null;
    }
  }

  static void error(String message) {
    FacesContext.getCurrentInstance()
        .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getName() {
    return name;
  }
}
