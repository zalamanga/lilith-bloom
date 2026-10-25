package com.lilithbloom.web;

import com.lilithbloom.Store;
import javax.faces.bean.*;

@ManagedBean(name = "registration")
@RequestScoped
public class RegistrationBean {
  private String name, email, password, confirmation;

  public String register() {
    try {
      if (password == null || !password.equals(confirmation)) {
        AuthBean.error("Konfirmasi password harus sama.");
        return null;
      }
      new Store().register(name, email, password);
      return "login?faces-redirect=true&registered=1";
    } catch (IllegalArgumentException e) {
      AuthBean.error(e.getMessage());
      return null;
    } catch (RuntimeException e) {
      AuthBean.error(
          "Akun belum dapat dibuat. Periksa database lalu coba lagi; email mungkin sudah terdaftar.");
      return null;
    } finally {
      password = null;
      confirmation = null;
    }
  }

  public String getName() {
    return name;
  }

  public void setName(String value) {
    name = value;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String value) {
    email = value;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String value) {
    password = value;
  }

  public String getConfirmation() {
    return confirmation;
  }

  public void setConfirmation(String value) {
    confirmation = value;
  }
}
