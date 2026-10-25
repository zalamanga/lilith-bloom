package com.lilithbloom;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class Workshops {
  private Workshops() {}

  public static final List<String> OPTIONS =
      List.of(
          "HALLOWEEN SERIES - BOUQUET (October)",
          "HALLOWEEN SERIES - BASKET (October)",
          "SPRING BOUQUET (November)",
          "HANDTIED FLOWER (November)",
          "CHRISTMAS SERIES - BOUQUET (December)",
          "CHRISTMAS SERIES - VASE (December)",
          "CHRISTMAS SERIES - BASKET (December)");

  public static List<Schedule> archive() {
    List<Schedule> rows = new ArrayList<>();
    try (InputStream stream = Workshops.class.getResourceAsStream("/schedule-2023.tsv")) {
      if (stream == null) throw new IllegalStateException("Source schedule missing");
      try (BufferedReader reader =
          new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
        reader
            .lines()
            .forEach(
                line -> {
                  String[] values = line.split("\t", -1);
                  if (values.length != 4)
                    throw new IllegalStateException("Source schedule malformed");
                  rows.add(new Schedule(values[0], values[1], values[2], values[3]));
                });
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return List.copyOf(rows);
  }

  public static final class Schedule {
    private final String month, day, time, title;

    public Schedule(String month, String day, String time, String title) {
      this.month = month;
      this.day = day;
      this.time = time;
      this.title = title;
    }

    public String getMonth() {
      return month;
    }

    public String getDay() {
      return day;
    }

    public String getTime() {
      return time;
    }

    public String getTitle() {
      return title;
    }
  }
}
