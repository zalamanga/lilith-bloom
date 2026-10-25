package com.lilithbloom.web;

import com.lilithbloom.Store;
import com.lilithbloom.model.Reservation;
import javax.faces.bean.*;
import javax.faces.context.FacesContext;

@ManagedBean(name = "booking")
@RequestScoped
public class ReservationBean {
  private String firstName, lastName, phone, email, workshop;

  public String reserve() {
    try {
      Reservation saved = new Store().save(null, 0, firstName, lastName, phone, email, workshop);
      FacesContext.getCurrentInstance()
          .getExternalContext()
          .getFlash()
          .put("reservationId", saved.getId());
      return "confirmation?faces-redirect=true";
    } catch (IllegalArgumentException e) {
      AuthBean.error(e.getMessage());
      return null;
    } catch (RuntimeException e) {
      AuthBean.error(
          "Penyimpanan reservasi belum dapat dikonfirmasi. Periksa daftar melalui pengelola sebelum mengirim ulang.");
      return null;
    }
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String value) {
    firstName = value;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String value) {
    lastName = value;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String value) {
    phone = value;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String value) {
    email = value;
  }

  public String getWorkshop() {
    return workshop;
  }

  public void setWorkshop(String value) {
    workshop = value;
  }
}
