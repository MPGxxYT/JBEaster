package me.mortaldev.jbeaster.modules.bunnyrace;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.api.holograms.HologramPage;
import me.gamercoder215.mobchip.EntityBrain;
import me.gamercoder215.mobchip.ai.controller.EntityController;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;
import me.mortaldev.jbeaster.Main;
import me.mortaldev.jbeaster.configs.MainConfig;
import me.mortaldev.jbeaster.modules.ActiveDate;
import me.mortaldev.jbeaster.modules.egghunt.EggHuntDataCRUD;
import me.mortaldev.jbeaster.modules.playerdata.PlayerDataManager;
import me.mortaldev.jbeaster.records.Pair;
import me.mortaldev.jbeaster.utils.TextUtil;
import me.mortaldev.jbeaster.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class BunnyRaceController {

  public static final EntityType ENTITY_TYPE = EntityType.RABBIT;

  private static class Singleton {
    private static final BunnyRaceController INSTANCE = new BunnyRaceController();
  }

  public static BunnyRaceController getInstance() {
    return Singleton.INSTANCE;
  }

  private BunnyRaceController() {}

  private final HashMap<Mob, Integer> bunnyRacers = new HashMap<>();
  private final HashMap<String, Integer> scheduledTasks = new HashMap<>();
  private final HashMap<UUID, PlayerBet> playerBets = new HashMap<>();
  private final List<Integer> winners = new ArrayList<>() {};
  private final HashMap<String, ScheduledExecutorService> executorServices = new HashMap<>();
  private boolean raceActive = false;
  private boolean betsOpen = false;

  public void resetTimers() {
    for (Map.Entry<String, ScheduledExecutorService> entry : executorServices.entrySet()) {
      entry.getValue().shutdownNow();
    }
  }

  public boolean isEnabled() {
    return MainConfig.getInstance().getBunnyRaceEnabled();
  }

  public void initTimer() {
    resetTimers();
    if (isRaceActive()) {
      return;
    }
    Bukkit.getServer()
        .sendMessage(
            TextUtil.format(
                "&f[&e&lBUNNY RACE&f] Next race in "
                    + MainConfig.getInstance().getRaceFrequency()
                    + " minutes!"));
    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    AtomicInteger atomicCount = new AtomicInteger(0);
    executorServices.put("initTimer", executor);
    executor.scheduleAtFixedRate(
        () -> {
          HologramPage bunnyBetInfo = DHAPI.getHologram("bunnyBetInfo").getPage(0);
          int count = atomicCount.getAndIncrement();
          if (count == MainConfig.getInstance().getRaceFrequency() - 1) {
            Bukkit.getServer()
                .sendMessage(
                    TextUtil.format("&f[&e&lBUNNY RACE&f] Next Race in less than a minute!"));
            bunnyBetInfo.setLine(1, "&6&lNext Race in&f: 1 min");
          } else if (count >= MainConfig.getInstance().getRaceFrequency()) {
            bunnyBetInfo.setLine(1, "&6&lRace Active!");
            betPhase();
            executorServices.remove("initTimer");
            executor.shutdown();
          } else {
            bunnyBetInfo.setLine(1, "&6&lNext Race in&f: " + (MainConfig.getInstance().getRaceFrequency() - count) + " mins");
          }
        },
        0,
        1,
        TimeUnit.MINUTES);
  }

  public boolean isBetsOpen() {
    return betsOpen;
  }

  public void setBetsOpen(boolean betsOpen) {
    this.betsOpen = betsOpen;
  }

  public boolean isRaceActive() {
    return raceActive;
  }

  public void setRaceActive(boolean raceActive) {
    this.raceActive = raceActive;
  }

  public String formatNumber(double number) {
    return formatNumber(number, true);
  }

  public String formatNumber(double number, boolean useCommas) {
    if (number == (long) number) {
      if (useCommas) {
        return String.format("%,d", (long) number);
      } else {
        return String.format("%d", (long) number);
      }
    } else {
      String pattern = useCommas ? "#,##0.00" : "#0.00";
      DecimalFormat decimalFormat = new DecimalFormat(pattern);
      return decimalFormat.format(number);
    }
  }

  public void resetController() {
    BunnyRaceController.getInstance().clearBunnies();
    BunnyRaceController.getInstance().clearHolograms();
    for (Map.Entry<String, Integer> entry : scheduledTasks.entrySet()) {
      Bukkit.getScheduler().cancelTask(entry.getValue());
    }
    scheduledTasks.clear();
    for (Map.Entry<String, ScheduledExecutorService> entry : executorServices.entrySet()) {
      entry.getValue().shutdownNow();
    }
    executorServices.clear();
    refundAllPlayers(false);
    winners.clear();
    setRaceActive(false);
    Bukkit.getScheduler()
        .scheduleSyncDelayedTask(
            Main.getInstance(),
            () -> {
              BunnyRaceController.getInstance().initStadium();
            },
            20);
  }

  private void betPhase() {
    setBetsOpen(true);
    showBettingHolograms();
    Bukkit.getServer()
        .sendMessage(
            TextUtil.format("&f[&e&lBUNNY RACE&f] Bets are now open! Go to /spawn to bet!"));
    AtomicInteger atomicCount = new AtomicInteger(0);
    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    int bettingTime = MainConfig.getInstance().getBettingTime();
    int interval = (int) Math.floor((double) bettingTime / 3);
    World eggHuntWorld = MainConfig.getInstance().getEggHuntWorld();
    executorServices.put("betPhase", executor);
    executor.scheduleAtFixedRate(
        () -> {
          int count = atomicCount.getAndIncrement();
          int secondsLeft = bettingTime - count;
          if (secondsLeft <= 3 && secondsLeft > 0) {
            eggHuntWorld.sendMessage(
                TextUtil.format("&f[&e&lBUNNY RACE&f] Bets closing in " + secondsLeft + "..."));
          } else if (secondsLeft % interval == 0 && secondsLeft != 0) {
            eggHuntWorld.sendMessage(
                TextUtil.format(
                    "&f[&e&lBUNNY RACE&f] Bets are closing in " + secondsLeft + " seconds!"));
          }
          if (secondsLeft == 0) {
            Bukkit.getScheduler().runTask(Main.getInstance(), this::startRace);
          } else if (secondsLeft <= 0) {
            setBetsOpen(false);
            executorServices.remove("betPhase");
            executor.shutdown();
          }
        },
        0,
        1,
        TimeUnit.SECONDS);
  }

  private void cashInBets() {
    for (Map.Entry<UUID, PlayerBet> entry : playerBets.entrySet()) {
      OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(entry.getKey());
      double amount = entry.getValue().getAmount();
      if (!(Main.getEconomy().getBalance(offlinePlayer) >= amount)) {
        playerBets.remove(entry.getKey());
        if (offlinePlayer.isOnline()) {
          Player player = offlinePlayer.getPlayer();
          player.sendMessage(TextUtil.format("&cYour bet was cancelled due to lack of funds."));
        }
        continue;
      }
      Main.getEconomy().withdrawPlayer(offlinePlayer, amount);
      entry.getValue().cashIn();
    }
  }

  public void playerBet(UUID uuid, double amount, int bunnyID) {
    PlayerBet playerBet = new PlayerBet(bunnyID, amount);
    playerBets.put(uuid, playerBet);
  }

  public PlayerBet getPlayerBet(UUID uuid) {
    if (!playerBets.containsKey(uuid)) {
      return null;
    }
    return playerBets.get(uuid);
  }

  public boolean playerHasBet(UUID uuid) {
    return playerBets.containsKey(uuid);
  }

  public void refundAllPlayers(boolean onDisable) {
    BunnyRaceData bunnyRaceData = BunnyRaceDataCRUD.getInstance().get();
    if (playerBets.isEmpty()) {
      HashMap<UUID, Double> refunds = bunnyRaceData.getRefunds();
      if (!refunds.isEmpty()) {
        for (Map.Entry<UUID, Double> entry : refunds.entrySet()) {
          OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(entry.getKey());
          Main.getEconomy().depositPlayer(offlinePlayer, entry.getValue());
          if (offlinePlayer.isOnline()) {
            offlinePlayer
                    .getPlayer()
                    .sendMessage(TextUtil.format("&cYou were refunded $" + formatNumber(entry.getValue())));
          }
          bunnyRaceData.removeRefund(entry.getKey());
        }
      }
    }

    Iterator<Map.Entry<UUID, PlayerBet>> iterator = playerBets.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry<UUID, PlayerBet> entry = iterator.next();
      if (!entry.getValue().isCashedIn()) {
        continue;
      }
      if (onDisable) {
        double refundAmount = getRefundAmount(entry.getKey(), 1);
        bunnyRaceData.addRefund(entry.getKey(), refundAmount);
      } else {
        double refund = getRefundAmount(entry.getKey(), 1);
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(entry.getKey());
        Main.getEconomy().depositPlayer(offlinePlayer, refund);
        if (offlinePlayer.isOnline()) {
          offlinePlayer
                  .getPlayer()
                  .sendMessage(TextUtil.format("&cYou were refunded $" + formatNumber(refund)));
        }
      }
      iterator.remove();
    }
  }

  public double refundPlayer(UUID uuid, double percentToRefund) {
    return refundPlayer(uuid, percentToRefund, true);
  }

  public double refundPlayer(UUID uuid, double percentToRefund, boolean withMessage) {
    double refund = getRefundAmount(uuid, percentToRefund);
    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
    Main.getEconomy().depositPlayer(offlinePlayer, refund);
    if (withMessage) {
      if (offlinePlayer.isOnline()) {
        offlinePlayer
            .getPlayer()
            .sendMessage(TextUtil.format("&cYou were refunded $" + formatNumber(refund)));
      }
    }
    return refund;
  }

  public double getRefundAmount(UUID uuid, double percentToRefund) {
    if (!playerBets.containsKey(uuid)) {
      return 0;
    }
    PlayerBet playerBet = playerBets.get(uuid);
    double amount = playerBet.getAmount();
    return Math.floor(amount * percentToRefund);
  }

  public void payoutOnlinePlayer(Player player, ItemStack currencyItemStack) {
    PlayerDataManager.getInstance().givePlayerReward(player.getUniqueId(), currencyItemStack);
  }

  private void payoutBets() {
    ItemStack payoutItemStack = EggHuntDataCRUD.getInstance().get().getCurrencyItemStack().clone();
    for (Map.Entry<UUID, PlayerBet> entry : playerBets.entrySet()) {
      int placement = winners.indexOf(entry.getValue().getBunnyID()) + 1;
      int eggPayout = entry.getValue().eggPayout();
      payoutItemStack.setAmount(eggPayout);
      OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(entry.getKey());
      switch (placement) {
        case 1 -> {
          if (!offlinePlayer.isOnline()) {
            PlayerDataManager.getInstance().givePlayerReward(entry.getKey(), payoutItemStack);
          } else {
            Player player = offlinePlayer.getPlayer();
            if (player == null) {
              PlayerDataManager.getInstance().givePlayerReward(entry.getKey(), payoutItemStack);
              break;
            }
            payoutOnlinePlayer(player, payoutItemStack);
            player.sendMessage(
                TextUtil.format(
                    "&eYour bunny won &f#"
                        + placement
                        + " &ewith a payout of &f"
                        + eggPayout
                        + "&e eggs!"));
          }
        }
        case 2 -> {
          double refund = refundPlayer(entry.getKey(), (double) 1 / 2, false);
          if (offlinePlayer.isOnline()) {
            Player player = offlinePlayer.getPlayer();
            player.sendMessage(
                TextUtil.format(
                    "&eYour bunny won &f#"
                        + placement
                        + " &ewith a payout of &f$"
                        + formatNumber(refund)));
          }
        }
        case 3 -> {
          double refund = refundPlayer(entry.getKey(), (double) 1 / 3, false);
          if (offlinePlayer.isOnline()) {
            Player player = offlinePlayer.getPlayer();
            player.sendMessage(
                TextUtil.format(
                    "&eYour bunny won &f#"
                        + placement
                        + " &ewith a payout of &f$"
                        + formatNumber(refund)));
          }
        }
        case 4 -> {
          if (offlinePlayer.isOnline()) {
            Player player = offlinePlayer.getPlayer();
            player.sendMessage(
                TextUtil.format("&eYour bunny won &f#" + placement + " &ewith a payout of &f$0"));
          }
        }
      }
    }
    playerBets.clear();
  }

  public void initStadium() {
    if (!isEnabled()) {
      return;
    }
    if (!ActiveDate.getInstance().isEventActive()) {
      return;
    }
    homeBunny();
    initHolograms();
    initTimer();
    //    betPhase();
  }

  private void initHolograms() {
    BunnyRaceData bunnyRaceData = BunnyRaceDataCRUD.getInstance().get();
    DHAPI.createHologram(
        "bunnyBetInfo",
        bunnyRaceData.getInformationDisplayLocation().clone().add(0, 5, 0),
        bunnyRaceData.getInformationDisplay()).getPage(0).setLine(1, "&6&lNext Race in&f: "+ MainConfig.getInstance().getRaceFrequency() + " min");
  }

  private void hideHolograms() {
    for (Map.Entry<Integer, Location> entry :
        BunnyRaceDataCRUD.getInstance().get().getBunnyHomes().entrySet()) {
      Hologram hologram = DHAPI.getHologram("bunnyBet_" + entry.getKey());
      if (hologram != null) {
        hologram.delete();
      }
    }
  }

  private void showBettingHolograms() {
    BunnyRaceData bunnyRaceData = BunnyRaceDataCRUD.getInstance().get();
    for (Map.Entry<Integer, Location> entry : bunnyRaceData.getBunnyHomes().entrySet()) {
      List<String> hologramLines =
          new ArrayList<>() {
            {
              add("&eBet on me!");
              add("&7[CLICK HERE]");
              // add(bunnyRaceData.getBunnyName(entry.getKey()));
            }
          };
      DHAPI.createHologram(
          "bunnyBet_" + entry.getKey(), entry.getValue().clone().add(0, 2, 0), hologramLines);
    }
  }

  public void clearHolograms() {
    for (Map.Entry<Integer, Location> entry :
        BunnyRaceDataCRUD.getInstance().get().getBunnyHomes().entrySet()) {
      Hologram hologram = DHAPI.getHologram("bunnyBet_" + entry.getKey());
      if (hologram != null) {
        hologram.delete();
      }
    }
    Hologram hologram = DHAPI.getHologram("bunnyBetInfo");
    if (hologram != null) {
      hologram.delete();
    }
  }

  private void homeBunny() {
    for (Map.Entry<Integer, Location> entry :
        BunnyRaceDataCRUD.getInstance().get().getBunnyHomes().entrySet()) {
      spawnBunny(entry.getKey(), entry.getValue());
    }
  }

  public void startRace() {
    setRaceActive(true);
    hideHolograms();
    cashInBets();
    BunnyRaceData bunnyRaceData = BunnyRaceDataCRUD.getInstance().get();
    World world = BunnyRaceData.getDefaultPoint().getWorld();
    for (Map.Entry<Mob, Integer> entry : bunnyRacers.entrySet()) {
      Location first = bunnyRaceData.getPoints(entry.getValue()).first();
      world = first.getWorld();
      EntityController controller = BukkitBrain.getBrain(entry.getKey()).getController();
      controller.moveTo(first, 1);
    }
    Bukkit.getServer()
        .sendMessage(
            TextUtil.format("&f[&e&lBUNNY RACE&f] Bets are now closed! Race is starting!"));
    AtomicInteger atomicCount = new AtomicInteger(0);
    World finalWorld = world;
    scheduledTasks.put(
        "countdown",
        Bukkit.getScheduler()
            .scheduleSyncRepeatingTask(
                Main.getInstance(),
                () -> {
                  int count = atomicCount.getAndIncrement();
                  switch (count) {
                    case 1 -> {
                      finalWorld.sendMessage(TextUtil.format("&6On Your Marks!"));
                      finalWorld.showTitle(
                          Title.title(TextUtil.format("&6On Your Marks!"), TextUtil.format("")));
                    }
                    case 2 -> {
                      finalWorld.sendMessage(TextUtil.format("&6Get Set!"));
                      finalWorld.showTitle(
                          Title.title(TextUtil.format("&6Get Set!"), TextUtil.format("")));
                    }
                    case 3 -> {
                      finalWorld.sendMessage(TextUtil.format("&6GOOO!"));
                      finalWorld.showTitle(
                          Title.title(TextUtil.format("&6GOOO!"), TextUtil.format("")));
                      raceBunnies();
                      Bukkit.getScheduler().cancelTask(scheduledTasks.remove("countdown"));
                    }
                  }
                },
                20,
                20));
  }

  public void endRace() {
    clearBunnies();
    scheduledTasks.forEach((key, value) -> Bukkit.getScheduler().cancelTask(value));
    scheduledTasks.clear();
    World world = BunnyRaceDataCRUD.getInstance().get().getPoints(1).first().getWorld();
    world.sendMessage(TextUtil.format(""));
    world.sendMessage(TextUtil.format("&eBunny Race has ended!"));
    world.sendMessage(TextUtil.format(""));
    world.sendMessage(TextUtil.format("&e&lPlacements:"));
    for (int i = 0; i < winners.size(); i++) {
      Component bunnyName =
          TextUtil.format(
              "&f" + BunnyRaceDataCRUD.getInstance().get().getBunnyName(winners.get(i)));
      world.sendMessage(TextUtil.format("&6#&l" + (i + 1) + ": &f").append(bunnyName));
    }
    world.sendMessage(TextUtil.format(""));
    payoutBets();
    winners.clear();
    delayedRunnable(
        () -> {
          setRaceActive(false);
          homeBunny();
          initTimer();
        },
        60L);
  }

  private void spawnBunny(int id, Location location) {
    Entity entity = location.getWorld().spawnEntity(location, ENTITY_TYPE);
    Mob bunny = (Mob) entity;
    bunnyRacers.put(bunny, id);
    bunny.setCustomNameVisible(true);
    Component bunnyName =
        TextUtil.format("&f" + BunnyRaceDataCRUD.getInstance().get().getBunnyName(id));
    bunny.customName(bunnyName);
    bunny.setInvulnerable(true);
    EntityBrain brain = BukkitBrain.getBrain(bunny);
    brain.getGoalAI().clear();
    brain.getTargetAI().clear();
  }

  private void addWinner(int id) {
    winners.add(id);
  }

  public void clearBunnies() {
    for (Map.Entry<Mob, Integer> entry : bunnyRacers.entrySet()) {
      entry.getKey().remove();
    }
    bunnyRacers.clear();
  }

  private void raceBunnies() {
    AtomicInteger winnerCount = new AtomicInteger(0);
    Pair<Location, Location> finishLine = BunnyRaceDataCRUD.getInstance().get().getFinishLine();
    bunnyRacers.forEach(
        (bunny, id) -> {
          Location finish = BunnyRaceDataCRUD.getInstance().get().getPoints(id).second();
          EntityBrain brain = BukkitBrain.getBrain(bunny);
          EntityController controller = brain.getController();
          AtomicInteger speedUpdateCount = new AtomicInteger(0);
          scheduledTasks.put(
              id + " bunny",
              Bukkit.getScheduler()
                  .scheduleSyncRepeatingTask(
                      Main.getInstance(),
                      () -> {
                        checkAndMovePlayer(bunny);
                        if (speedUpdateCount.incrementAndGet() >= 2) {
                          speedUpdateCount.set(0);
                          double v = generateSpeed();
                          controller.moveTo(finish, v);
                        }
                        if (bunny.isDead()
                            || Utils.locationIsWithin(
                                bunny.getLocation(), finishLine.first(), finishLine.second())) {
                          controller.moveTo(finish, 1.5);
                          winnerCount.getAndIncrement();
                          addWinner(id);
                          if (winnerCount.get() >= 4) {
                            delayedRunnable(this::endRace, 60L);
                          }
                          Bukkit.getScheduler().cancelTask(scheduledTasks.remove(id + " bunny"));
                        }
                      },
                      10,
                      10));
        });
  }

  private void checkAndMovePlayer(Mob bunny) {
    Location location = BunnyRaceDataCRUD.getInstance().get().getInformationDisplayLocation();
    bunny.getLocation().getNearbyPlayers(1).forEach(player -> player.teleport(location));
  }

  public void delayedRunnable(Runnable runnable, Long delay) {
    Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), runnable, delay);
  }

  private double generateSpeed() {
    double min = 0.5;
    double max = 2;
    return min + (Math.random() * (max - min));
  }
}
