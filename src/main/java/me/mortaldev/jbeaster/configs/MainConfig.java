package me.mortaldev.jbeaster.configs;

import me.mortaldev.AbstractConfig;
import me.mortaldev.ConfigValue;
import me.mortaldev.jbeaster.Main;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public class MainConfig extends AbstractConfig {

  private ConfigValue<String> collectedEggMessage =
      new ConfigValue<>(
          "collectedEggMessage", "&6You have collected egg &f%foundEggNumber%&6/&f%maxEggs%!");
  private ConfigValue<String> alreadyCollectedEggMessage =
      new ConfigValue<>("alreadyCollectedEggMessage", "&cYou already found this egg!");
  private ConfigValue<String> allEggsCollectedMessage =
      new ConfigValue<>(
          "allEggsCollectedMessage",
          "&6You have collected all &f%maxEggs%&6 eggs! &lYou have earned a reward!");
  private ConfigValue<Boolean> eggHuntEnabled = new ConfigValue<>("eggHuntEnabled", false);
  private ConfigValue<Boolean> bunnyRaceEnabled = new ConfigValue<>("bunnyRaceEnabled", false);
  private ConfigValue<Boolean> miningEggEnabled = new ConfigValue<>("miningEggEnabled", false);
  private ConfigValue<String> eggHuntWorld = new ConfigValue<>("eggHuntWorld", "spawn");
  private ConfigValue<Integer> raceFrequency = new ConfigValue<>("raceFrequency", 15);
  private ConfigValue<Integer> bettingTime = new ConfigValue<>("bettingTime", 45);
  private ConfigValue<Double> chanceToRoll = new ConfigValue<>("chanceToRoll", 0.75d);
  private ConfigValue<Double> commonChance = new ConfigValue<>("commonChance", 60d);
  private ConfigValue<Double> rareChance = new ConfigValue<>("rareChance", 30d);
  private ConfigValue<Double> legendaryChance = new ConfigValue<>("legendaryChance", 10d);
  private ConfigValue<Integer> commonHealth = new ConfigValue<>("commonHealth", 0);
  private ConfigValue<Integer> rareHealth = new ConfigValue<>("rareHealth", 20);
  private ConfigValue<Integer> legendaryHealth = new ConfigValue<>("legendaryHealth", 40);
  private ConfigValue<Integer> commonReward = new ConfigValue<>("commonReward", 1);
  private ConfigValue<Integer> rareReward = new ConfigValue<>("rareReward", 2);
  private ConfigValue<Integer> legendaryReward = new ConfigValue<>("legendaryReward", 3);

  private static class Singleton {
    private static final MainConfig INSTANCE = new MainConfig();
  }

  public static MainConfig getInstance() {
    return Singleton.INSTANCE;
  }

  private MainConfig() {}

  @Override
  public void log(String message) {
    Main.log(message);
  }

  @Override
  public String getName() {
    return "config";
  }

  @Override
  public JavaPlugin getMain() {
    return Main.getInstance();
  }

  @Override
  public void loadData() {
    eggHuntWorld = getConfigValue(eggHuntWorld);
    raceFrequency = getConfigValue(raceFrequency);
    bettingTime = getConfigValue(bettingTime);
    loadToggles();
    loadMessages();
    loadMiningEggData();
  }

  private void loadToggles() {
    eggHuntEnabled = getConfigValue(eggHuntEnabled);
    bunnyRaceEnabled = getConfigValue(bunnyRaceEnabled);
    miningEggEnabled = getConfigValue(miningEggEnabled);
  }

  private void loadMessages() {
    collectedEggMessage = getConfigValue(collectedEggMessage);
    alreadyCollectedEggMessage = getConfigValue(alreadyCollectedEggMessage);
    allEggsCollectedMessage = getConfigValue(allEggsCollectedMessage);
  }

  private void loadMiningEggData(){
    chanceToRoll = getConfigValue(chanceToRoll);
    commonChance = getConfigValue(commonChance);
    rareChance = getConfigValue(rareChance);
    legendaryChance = getConfigValue(legendaryChance);
    commonHealth = getConfigValue(commonHealth);
    rareHealth = getConfigValue(rareHealth);
    legendaryHealth = getConfigValue(legendaryHealth);
    commonReward = getConfigValue(commonReward);
    rareReward = getConfigValue(rareReward);
    legendaryReward = getConfigValue(legendaryReward);
  }

  public double getChanceToRoll() {
    return chanceToRoll.getValue();
  }

  public double getCommonChance() {
    return commonChance.getValue();
  }

  public double getRareChance() {
    return rareChance.getValue();
  }

  public double getLegendaryChance() {
    return legendaryChance.getValue();
  }

  public int getCommonHealth() {
    return commonHealth.getValue();
  }

  public int getRareHealth() {
    return rareHealth.getValue();
  }

  public int getLegendaryHealth() {
    return legendaryHealth.getValue();
  }

  public int getCommonReward() {
    return commonReward.getValue();
  }

  public int getRareReward() {
    return rareReward.getValue();
  }

  public int getLegendaryReward() {
    return legendaryReward.getValue();
  }

  public Boolean getBunnyRaceEnabled() {
    return bunnyRaceEnabled.getValue();
  }

  public Boolean getEggHuntEnabled() {
    return eggHuntEnabled.getValue();
  }

  public Boolean getMiningEggEnabled() {
    return miningEggEnabled.getValue();
  }

  public String getAlreadyCollectedEggMessage() {
    return alreadyCollectedEggMessage.getValue();
  }

  public String getCollectedEggMessage() {
    return collectedEggMessage.getValue();
  }

  public String getAllEggsCollectedMessage() {
    return allEggsCollectedMessage.getValue();
  }

  public World getEggHuntWorld() {
    World world = Bukkit.getWorld(eggHuntWorld.getValue());
    if (world == null) {
      return Bukkit.getWorlds().get(0);
    }
    return world;
  }

  public int getRaceFrequency() {
    return raceFrequency.getValue();
  }

  public int getBettingTime() {
    return bettingTime.getValue();
  }
}
