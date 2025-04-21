package me.mortaldev.jbeaster.modules.dropparty;

import me.mortaldev.jbeaster.Main;
import org.bukkit.Bukkit;

public class DelayedRunnable {

  Runnable runnable;
  long delay;

  public DelayedRunnable(Runnable runnable, long delay) {
    this.runnable = runnable;
    this.delay = delay;
  }

  public DelayedRunnable(Runnable runnable, long delay, boolean runNow) {
    this.runnable = runnable;
    this.delay = delay;
    if (runNow) {
      run();
    }
  }

  public void run() {
    Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), runnable, delay);
  }
}
