package me.mortaldev.jbeaster;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import co.aikar.commands.PaperCommandManager;
import me.mortaldev.jbeaster.commands.eastercommand.EasterRewardsCommand;
import me.mortaldev.jbeaster.commands.eastercommand.JBEasterCommand;
import me.mortaldev.jbeaster.configs.MainConfig;
import me.mortaldev.jbeaster.listeners.DropPartyEvent;
import me.mortaldev.jbeaster.listeners.bunnyrace.BunnyDenyEvent;
import me.mortaldev.jbeaster.listeners.bunnyrace.BunnyRaceBetEvent;
import me.mortaldev.jbeaster.listeners.MiningBunnyEvent;
import me.mortaldev.jbeaster.listeners.egghunt.EditingClickEvent;
import me.mortaldev.jbeaster.listeners.egghunt.EggHuntClickEvent;
import me.mortaldev.jbeaster.modules.ActiveDate;
import me.mortaldev.jbeaster.modules.bunnyrace.BunnyRaceController;
import me.mortaldev.jbeaster.modules.dropparty.DropPartyCRUD;
import me.mortaldev.jbeaster.modules.dropparty.DropPartyController;
import me.mortaldev.jbeaster.modules.egghunt.EggHuntDataCRUD;
import me.mortaldev.jbeaster.modules.miningegg.MiningEgg;
import me.mortaldev.jbeaster.modules.miningegg.OwnerRarity;
import me.mortaldev.jbeaster.modules.playerdata.PlayerDataManager;
import me.mortaldev.menuapi.GUIListener;
import me.mortaldev.menuapi.GUIManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.Bukkit;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;

public final class Main extends JavaPlugin {

  private static final String LABEL = "JBEaster";
  private static Main instance;
  private static boolean debug = false;
  private static Economy economy = null;
  private static final HashSet<String> dependencies =
      new HashSet<>() {
        {
          add("Skript");
          add("Vault");
          add("DecentHolograms");
        }
      };

  public static Main getInstance() {
    return instance;
  }

  public static String getLabel() {
    return LABEL;
  }

  public static boolean toggleDebug() {
    debug = !debug;
    return debug;
  }

  public static boolean isDebug() {
    return debug;
  }

  public static void debugLog(String message) {
    if (debug) {
      log(message);
    }
  }

  private boolean setupEconomy() {
    RegisteredServiceProvider<Economy> registration =
        getServer().getServicesManager().getRegistration(Economy.class);
    if (registration == null) {
      return false;
    }
    economy = registration.getProvider();
    return economy != null;
  }

  public static Economy getEconomy() {
    return economy;
  }

  public static void log(String message) {
    Bukkit.getLogger().info("[" + Main.getLabel() + "] " + message);
  }

  @Override
  public void onEnable() {
    instance = this;
    PaperCommandManager commandManager = new PaperCommandManager(this);

    // DATA FOLDER

    if (!getDataFolder().exists()) getDataFolder().mkdir();

    // DEPENDENCIES

    boolean disable = false;
    for (String plugin : dependencies) {
      if (Bukkit.getPluginManager().getPlugin(plugin) == null) {
        getLogger().warning("Could not find " + plugin + "! This plugin is required.");
        disable = true;
      }
    }
    if (disable) {
      Bukkit.getPluginManager().disablePlugin(this);
      return;
    }
    setupEconomy();

    // CONFIGS
    MainConfig.getInstance().load();

    // Managers (Loading data)
    //    GangManager.loadGangDataList();
    EggHuntDataCRUD.getInstance().load();
    PlayerDataManager.getInstance().load();
    DropPartyCRUD.getInstance().load();

    // GUI Manager
    GUIListener guiListener = new GUIListener(GUIManager.getInstance());
    Bukkit.getPluginManager().registerEvents(guiListener, this);

    // Events

    getServer().getPluginManager().registerEvents(new EditingClickEvent(), this);
    getServer().getPluginManager().registerEvents(new EggHuntClickEvent(), this);
    getServer().getPluginManager().registerEvents(new BunnyRaceBetEvent(), this);
    getServer().getPluginManager().registerEvents(new MiningBunnyEvent(), this);
    getServer().getPluginManager().registerEvents(new BunnyDenyEvent(), this);
    getServer().getPluginManager().registerEvents(new DropPartyEvent(), this);

    // COMMANDS

    commandManager.registerCommand(new JBEasterCommand());
    commandManager.registerCommand(new EasterRewardsCommand());

    SkriptAddon addon = Skript.registerAddon(this);
    try {
      addon.loadClasses("me.mortaldev.jbeaster.register", "effects");
    } catch (IOException e) {
      e.printStackTrace();
    }

    getLogger().info(LABEL + " Enabled");
    ActiveDate.getInstance().beginClock();
    Bukkit.getScheduler()
        .runTask(
            this,
            () -> {
              if (ActiveDate.getInstance().isEventActive()) {
                BunnyRaceController.getInstance().initStadium();
              }
              BunnyRaceController.getInstance().refundAllPlayers(false);
            });
  }

  @Override
  public void onDisable() {
    for (Map.Entry<Entity, OwnerRarity> entry :
        MiningEgg.getInstance().getEntityOwners().entrySet()) {
      entry.getKey().remove();
    }
    DropPartyController.getInstance().killSchedules();
    DropPartyController.getInstance().killEntities();
    BunnyRaceController.getInstance().resetTimers();
    BunnyRaceController.getInstance().refundAllPlayers(true);
    BunnyRaceController.getInstance().clearBunnies();
    BunnyRaceController.getInstance().clearHolograms();
    getLogger().info(LABEL + " Disabled");
  }
}
