package me.mortaldev.jbeaster.modules;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ActiveDate {

  private static class Singleton {
    private static final ActiveDate INSTANCE = new ActiveDate();
  }

  public static ActiveDate getInstance() {
    return Singleton.INSTANCE;
  }

  private ActiveDate() {}

  private final LocalDateTime startDate = LocalDateTime.of(2025, 4, 20, 0, 0); // Sunday
  private final LocalDateTime endDate = LocalDateTime.of(2025, 4, 24, 0, 0); // Wednesday Morning
  private boolean isActive = false;

  /**
   * Returns whether the Easter event is currently active.
   *
   * @return {@code true} if the Easter event is currently active, {@code false} otherwise.
   */
  public boolean isEventActive() {
    return isActive;
  }

  private boolean isWithinDates() {
    ZoneId timeZone = ZoneId.of("America/New_York");
    LocalDateTime now = LocalDateTime.now(timeZone);
    return (now.isAfter(startDate) && now.isBefore(endDate));
  }

  /**
   * Schedules a task to run every minute that updates the value of {@link #isActive} to {@code
   * true} if the current date is within the range of the Easter event, and {@code false} otherwise.
   */
  public void beginClock() {
    isActive = isWithinDates();
    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    scheduler.scheduleAtFixedRate(() -> isActive = isWithinDates(), 0, 1, TimeUnit.MINUTES);
  }
}
