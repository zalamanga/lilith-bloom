package com.lilithbloom.web;

import com.lilithbloom.Store;
import com.lilithbloom.model.Reservation;
import java.io.Serializable;
import java.util.*;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.*;
import javax.faces.context.FacesContext;

@ManagedBean(name = "reservations")
@ViewScoped
public class ReservationsBean implements Serializable {
  private List<Reservation> rows = new ArrayList<>();
  private Long id, pendingId;
  private long version, pendingVersion;
  private String firstName, lastName, phone, email, workshop, query = "", pendingName;
  private boolean loadError;

  @PostConstruct
  public void init() {
    query =
        FacesContext.getCurrentInstance()
            .getExternalContext()
            .getRequestParameterMap()
            .getOrDefault("q", "");
    reload();
  }

  public void reload() {
    try {
      rows = new Store().reservations(query);
      loadError = false;
    } catch (IllegalArgumentException e) {
      rows = new ArrayList<>();
      loadError = true;
      AuthBean.error(e.getMessage());
    } catch (RuntimeException e) {
      rows = new ArrayList<>();
      loadError = true;
      AuthBean.error("Daftar reservasi gagal dimuat. Periksa database lalu pilih Muat ulang.");
    }
  }

  public void save() {
    if (id == null) {
      AuthBean.error("Pilih reservasi yang ingin diedit.");
      return;
    }
    try {
      new Store().save(id, version, firstName, lastName, phone, email, workshop);
      clear();
      reload();
      info("Perubahan reservasi disimpan.");
    } catch (IllegalArgumentException e) {
      AuthBean.error(e.getMessage());
    } catch (RuntimeException e) {
      AuthBean.error("Perubahan belum tersimpan. Muat ulang daftar lalu coba lagi.");
    }
  }

  public void edit(Long selectedId) {
    try {
      Reservation item = new Store().get(selectedId);
      if (item == null)
        throw new IllegalArgumentException("Reservasi sudah dihapus. Muat ulang daftar.");
      id = item.getId();
      version = item.getVersion();
      firstName = item.getFirstName();
      lastName = item.getLastName();
      phone = item.getPhone();
      email = item.getEmail();
      workshop = item.getWorkshop();
      cancelDelete();
      info("Reservasi dipilih. Ubah isian lalu simpan.");
    } catch (IllegalArgumentException e) {
      AuthBean.error(e.getMessage());
      reload();
    } catch (RuntimeException e) {
      AuthBean.error("Reservasi belum dapat dibuka. Periksa database lalu coba lagi.");
    }
  }

  public void askDelete(Long selectedId) {
    try {
      Reservation item = new Store().get(selectedId);
      if (item == null)
        throw new IllegalArgumentException("Reservasi sudah dihapus. Muat ulang daftar.");
      pendingId = item.getId();
      pendingVersion = item.getVersion();
      pendingName = item.getFullName();
    } catch (IllegalArgumentException e) {
      AuthBean.error(e.getMessage());
      reload();
    } catch (RuntimeException e) {
      AuthBean.error("Konfirmasi hapus belum dapat dimuat. Coba lagi.");
    }
  }

  public void delete() {
    if (pendingId == null) return;
    try {
      new Store().delete(pendingId, pendingVersion);
      if (pendingId.equals(id)) clear();
      cancelDelete();
      reload();
      info("Reservasi dihapus.");
    } catch (IllegalArgumentException e) {
      AuthBean.error(e.getMessage());
      cancelDelete();
      reload();
    } catch (RuntimeException e) {
      AuthBean.error("Reservasi belum terhapus. Periksa database lalu coba lagi.");
    }
  }

  public void clear() {
    id = null;
    firstName = null;
    lastName = null;
    phone = null;
    email = null;
    workshop = null;
  }

  public void cancelDelete() {
    pendingId = null;
    pendingName = null;
  }

  private void info(String message) {
    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(message));
  }

  public List<Reservation> getRows() {
    return rows;
  }

  public Long getId() {
    return id;
  }

  public boolean isEditing() {
    return id != null;
  }

  public boolean isLoadError() {
    return loadError;
  }

  public Long getPendingId() {
    return pendingId;
  }

  public String getPendingName() {
    return pendingName;
  }

  public String getQuery() {
    return query;
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
