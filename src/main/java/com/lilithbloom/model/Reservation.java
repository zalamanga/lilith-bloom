package com.lilithbloom.model;

import java.io.Serializable;
import javax.persistence.*;

/** Reservation domain retained from LilithBloom, originally credited to aulia maulidia. */
@Entity
@Table(name = "reservations")
public class Reservation implements Serializable {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "first_name", nullable = false, length = 80)
  private String firstName;

  @Column(name = "last_name", nullable = false, length = 80)
  private String lastName;

  @Column(nullable = false, length = 24)
  private String phone;

  @Column(nullable = false, length = 254)
  private String email;

  @Column(nullable = false, length = 80)
  private String workshop;

  @Version private long version;

  public Long getId() {
    return id;
  }

  public long getVersion() {
    return version;
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

  public String getFullName() {
    return firstName + " " + lastName;
  }
}
