package com.lilithbloom;

import com.lilithbloom.model.*;
import java.util.*;
import java.util.function.Function;
import org.hibernate.*;

public class Store {
  private final SessionFactory factory;

  public Store() {
    this(Database.factory());
  }

  public Store(SessionFactory factory) {
    this.factory = factory;
  }

  private <T> T transaction(Function<Session, T> work) {
    try (Session session = factory.openSession()) {
      Transaction transaction = session.beginTransaction();
      try {
        T result = work.apply(session);
        transaction.commit();
        return result;
      } catch (RuntimeException e) {
        if (transaction.isActive()) {
          try {
            transaction.rollback();
          } catch (RuntimeException rollbackFailure) {
            if (rollbackFailure != e) e.addSuppressed(rollbackFailure);
          }
        }
        throw e;
      }
    } catch (RuntimeException failure) {
      if (Database.connectionFailure(failure)) Database.invalidate(factory, failure);
      throw failure;
    }
  }

  public static String email(String value) {
    String normalized = value == null ? "" : value.strip().toLowerCase(Locale.ROOT);
    if (normalized.length() > 254 || !normalized.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))
      throw new IllegalArgumentException("Masukkan alamat email yang valid.");
    return normalized;
  }

  public Account register(String name, String email, String password) {
    String cleanName = text(name, 80, "Nama wajib diisi, maksimal 80 karakter.");
    String cleanEmail = email(email);
    if (password == null || password.length() < 12 || password.length() > 128)
      throw new IllegalArgumentException("Password harus 12 sampai 128 karakter.");
    String hash = Passwords.hash(password);
    return transaction(
        s -> {
          if (s.createQuery("from Account where email=:email", Account.class)
                  .setParameter("email", cleanEmail)
                  .uniqueResult()
              != null)
            throw new IllegalArgumentException(
                "Email sudah terdaftar. Gunakan email lain atau masuk.");
          Account account = new Account();
          account.setName(cleanName);
          account.setEmail(cleanEmail);
          account.setPasswordHash(hash);
          s.persist(account);
          return account;
        });
  }

  public Account authenticate(String email, String password) {
    String cleanEmail = email(email);
    Account account =
        transaction(
            s ->
                s.createQuery("from Account where email=:email", Account.class)
                    .setParameter("email", cleanEmail)
                    .uniqueResult());
    return account != null
            && password != null
            && password.length() <= 128
            && Passwords.verify(password, account.getPasswordHash())
        ? account
        : null;
  }

  public List<Reservation> reservations(String query) {
    String q = query == null ? "" : query.strip().toLowerCase(Locale.ROOT);
    if (q.length() > 120) throw new IllegalArgumentException("Pencarian maksimal 120 karakter.");
    String term = "%" + q.replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
    return transaction(
        s ->
            s.createQuery(
                    "from Reservation where lower(firstName) like :q escape '!' or lower(lastName) like :q escape '!' or lower(email) like :q escape '!' or lower(workshop) like :q escape '!' order by id desc",
                    Reservation.class)
                .setParameter("q", term)
                .getResultList());
  }

  public Reservation get(long id) {
    return transaction(s -> s.get(Reservation.class, id));
  }

  public Reservation save(
      Long id,
      long version,
      String firstName,
      String lastName,
      String phone,
      String email,
      String workshop) {
    String first = text(firstName, 80, "Nama depan wajib diisi, maksimal 80 karakter.");
    String last = text(lastName, 80, "Nama belakang wajib diisi, maksimal 80 karakter.");
    String cleanEmail = email(email);
    String cleanPhone = phone == null ? "" : phone.strip();
    if (cleanPhone.length() < 7
        || cleanPhone.length() > 24
        || !cleanPhone.matches("\\+?[0-9 ()-]+")
        || cleanPhone.replaceAll("\\D", "").length() < 7)
      throw new IllegalArgumentException(
          "Nomor telepon harus memuat 7 sampai 24 karakter berupa angka, spasi, +, -, atau kurung.");
    if (!Workshops.OPTIONS.contains(workshop))
      throw new IllegalArgumentException("Pilih salah satu kelas demo yang tersedia.");
    return transaction(
        s -> {
          Reservation item = id == null ? new Reservation() : s.get(Reservation.class, id);
          if (item == null || (id != null && item.getVersion() != version))
            throw new IllegalArgumentException(
                "Reservasi berubah atau sudah dihapus. Muat ulang daftar.");
          item.setFirstName(first);
          item.setLastName(last);
          item.setPhone(cleanPhone);
          item.setEmail(cleanEmail);
          item.setWorkshop(workshop);
          if (id == null) s.persist(item);
          return item;
        });
  }

  public void delete(long id, long version) {
    transaction(
        s -> {
          Reservation item = s.get(Reservation.class, id);
          if (item == null || item.getVersion() != version)
            throw new IllegalArgumentException(
                "Reservasi berubah atau sudah dihapus. Muat ulang daftar.");
          s.delete(item);
          return null;
        });
  }

  private static String text(String value, int max, String error) {
    String clean = value == null ? "" : value.strip();
    if (clean.isEmpty() || clean.length() > max) throw new IllegalArgumentException(error);
    return clean;
  }
}
