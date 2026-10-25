package com.lilithbloom;

import com.lilithbloom.model.*;
import java.util.*;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class StoreTest {
  private SessionFactory factory;
  private Store store;

  @BeforeEach
  void isolatedDatabase() {
    Properties p = new Properties();
    p.setProperty(
        "hibernate.connection.url",
        "jdbc:h2:mem:lilith_" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
    p.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
    p.setProperty("hibernate.connection.username", "sa");
    p.setProperty("hibernate.connection.password", "");
    p.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
    p.setProperty("hibernate.hbm2ddl.auto", "create-drop");
    factory = Database.build(p);
    store = new Store(factory);
  }

  @AfterEach
  void close() {
    if (factory != null) factory.close();
  }

  @Test
  void registrationAndLoginUseNormalizedEmailAndHash() {
    Account account = store.register("Demo QA", " QA@example.test ", "Synthetic-password-2026");
    assertNotNull(account.getId());
    assertEquals("qa@example.test", account.getEmail());
    assertNotEquals("Synthetic-password-2026", account.getPasswordHash());
    assertEquals(
        account.getId(), store.authenticate("QA@EXAMPLE.TEST", "Synthetic-password-2026").getId());
    assertNull(store.authenticate("qa@example.test", "wrong"));
    assertNull(store.authenticate("absent@example.test", "wrong"));
    assertThrows(
        IllegalArgumentException.class,
        () -> store.register("Demo", "qa@example.test", "Synthetic-password-2026"));
  }

  @Test
  void invalidAccountDoesNotPersist() {
    assertThrows(
        IllegalArgumentException.class,
        () -> store.register("", "qa@example.test", "Synthetic-password-2026"));
    assertThrows(
        IllegalArgumentException.class,
        () -> store.register("Demo", "not-email", "Synthetic-password-2026"));
    assertThrows(
        IllegalArgumentException.class, () -> store.register("Demo", "qa@example.test", "short"));
    assertNull(store.authenticate("qa@example.test", "short"));
  }

  @Test
  void reservationRoundTripAndLiteralSearch() {
    assertTrue(store.reservations("").isEmpty());
    Reservation item =
        store.save(
            null,
            0,
            "Demo 100%_",
            "QA",
            "+62 811 0000",
            "QA@EXAMPLE.TEST",
            Workshops.OPTIONS.get(0));
    assertNotNull(item.getId());
    assertEquals("qa@example.test", store.get(item.getId()).getEmail());
    assertEquals(1, store.reservations("%").size());
    assertEquals(1, store.reservations("_").size());
    assertEquals(1, store.reservations("halloween").size());
    assertTrue(store.reservations("' or 1=1 --").isEmpty());
    store.save(
        item.getId(),
        item.getVersion(),
        "Edited",
        "QA",
        "0811000000",
        "edited@example.test",
        Workshops.OPTIONS.get(1));
    Reservation edited = store.get(item.getId());
    assertEquals("Edited QA", edited.getFullName());
    assertEquals(Workshops.OPTIONS.get(1), edited.getWorkshop());
    store.delete(edited.getId(), edited.getVersion());
    assertNull(store.get(item.getId()));
    assertTrue(store.reservations("").isEmpty());
  }

  @Test
  void invalidReservationDoesNotAlterExistingData() {
    Reservation item =
        store.save(
            null, 0, "Demo", "QA", "0811000000", "qa@example.test", Workshops.OPTIONS.get(0));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            store.save(
                item.getId(),
                item.getVersion(),
                "",
                "QA",
                "0811000000",
                "qa@example.test",
                Workshops.OPTIONS.get(0)));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            store.save(
                item.getId(),
                item.getVersion(),
                "Demo",
                "QA",
                "not-phone",
                "qa@example.test",
                Workshops.OPTIONS.get(0)));
    assertThrows(
        IllegalArgumentException.class,
        () -> store.save(null, 0, "Demo", "QA", "0811000000", "invalid", Workshops.OPTIONS.get(0)));
    assertThrows(
        IllegalArgumentException.class,
        () -> store.save(null, 0, "Demo", "QA", "0811000000", "qa@example.test", "injected-class"));
    assertEquals("Demo", store.get(item.getId()).getFirstName());
    assertEquals(1, store.reservations("").size());
  }

  @Test
  void staleUpdateAndDeleteAreRejected() {
    Reservation first =
        store.save(
            null, 0, "Demo", "QA", "0811000000", "qa@example.test", Workshops.OPTIONS.get(0));
    store.save(
        first.getId(),
        first.getVersion(),
        "Newer",
        "QA",
        "0811000000",
        "qa@example.test",
        Workshops.OPTIONS.get(0));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            store.save(
                first.getId(),
                first.getVersion(),
                "Stale",
                "QA",
                "0811000000",
                "qa@example.test",
                Workshops.OPTIONS.get(0)));
    assertThrows(
        IllegalArgumentException.class, () -> store.delete(first.getId(), first.getVersion()));
    assertEquals("Newer", store.get(first.getId()).getFirstName());
  }

  @Test
  void sourceArchiveContainsFortyAgendasAndTwelveMonths() {
    assertEquals(40, Workshops.archive().size());
    assertEquals(
        12, Workshops.archive().stream().map(Workshops.Schedule::getMonth).distinct().count());
    assertEquals(7, Workshops.OPTIONS.size());
  }
}
