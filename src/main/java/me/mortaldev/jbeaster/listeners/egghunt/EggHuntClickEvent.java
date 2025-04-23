package me.mortaldev.jbeaster.listeners.egghunt;

import me.mortaldev.jbeaster.Main;
import me.mortaldev.jbeaster.configs.MainConfig;
import me.mortaldev.jbeaster.modules.ActiveDate;
import me.mortaldev.jbeaster.modules.egghunt.EggHuntData;
import me.mortaldev.jbeaster.modules.egghunt.EggHuntDataCRUD;
import me.mortaldev.jbeaster.modules.playerdata.PlayerData;
import me.mortaldev.jbeaster.modules.playerdata.PlayerDataManager;
import me.mortaldev.jbeaster.utils.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class EggHuntClickEvent implements Listener {

  private static HashSet<UUID> interactDelay = new HashSet<>();

  private String parse(String string, PlayerData playerData) {
    HashMap<String, String> placeholders =
        new HashMap<>() {
          {
            put("%foundEggNumber%", playerData.getCollectedEggs().size() + 1 + "");
            put("%maxEggs%", EggHuntDataCRUD.getInstance().get().getLocations().size() + "");
          }
        };
    for (Map.Entry<String, String> entry : placeholders.entrySet()) {
      if (string.contains(entry.getKey())) {
        string = string.replaceAll(entry.getKey(), entry.getValue());
      }
    }
    return string;
  }

  private void addDelay(UUID uuid) {
    interactDelay.add(uuid);
    Bukkit.getScheduler()
        .scheduleSyncDelayedTask(Main.getInstance(), () -> interactDelay.remove(uuid), 5);
  }

  private boolean hasDelay(UUID uuid) {
    return interactDelay.contains(uuid);
  }

  @EventHandler
  public void onClick(PlayerInteractEvent event) {
    if (!MainConfig.getInstance().getEggHuntEnabled()) {
      return;
    }
    Player player = event.getPlayer();
    if (hasDelay(player.getUniqueId())) {
      Main.debugLog(player.getName() + " has delay");
      return;
    }
    if (!ActiveDate.getInstance().isEventActive()) {
      Main.debugLog("Event not active");
      return;
    }
    if (player.getWorld() != MainConfig.getInstance().getEggHuntWorld()) {
      Main.debugLog("Not in egg hunt world");
      return;
    }
    if (event.isBlockInHand()) {
      Main.debugLog("block in hand");
      return;
    }
    if (event.getClickedBlock() == null) {
      Main.debugLog("block null");
      return;
    }
    if (EditingClickEvent.isEditingPlayer(player.getUniqueId())) {
      return;
    }
    EggHuntData eggHuntData = EggHuntDataCRUD.getInstance().get();
    if (!eggHuntData.hasLocation(event.getClickedBlock().getLocation())) {
      Main.debugLog("not a real location");
      return;
    }
    PlayerData playerData =
        PlayerDataManager.getInstance()
            .getByID(player.getUniqueId().toString())
            .orElse(PlayerData.create(player.getUniqueId().toString()));
    PlayerDataManager.getInstance().update(playerData, true);
    Location location = event.getClickedBlock().getLocation();
    addDelay(player.getUniqueId());
    if (playerData.hasCollectedEgg(location)) {
      String alreadyCollectedEggMessage = MainConfig.getInstance().getAlreadyCollectedEggMessage();
      alreadyCollectedEggMessage = parse(alreadyCollectedEggMessage, playerData);
      player.sendMessage(TextUtil.format(alreadyCollectedEggMessage));
      return;
    }
    String collectedEggMessage = MainConfig.getInstance().getCollectedEggMessage();
    collectedEggMessage = parse(collectedEggMessage, playerData);
    if ((playerData.getCollectedEggs().size() + 1) % 2 == 0) {
      List<ItemStack> rewards = Collections.singletonList(eggHuntData.getCurrencyItemStack());
      giveRewards(player, rewards);
    }
    playerData.addCollectedEgg(location);
    PlayerDataManager.getInstance().update(playerData, true);
    player.sendMessage(TextUtil.format(collectedEggMessage));
    if (playerData.getCollectedEggs().size() == eggHuntData.getLocations().size()) {
      String allEggsCollectedMessage = MainConfig.getInstance().getAllEggsCollectedMessage();
      allEggsCollectedMessage = parse(allEggsCollectedMessage, playerData);
      player.sendMessage(TextUtil.format(allEggsCollectedMessage));
      giveRewards(player, eggHuntData.getWinRewards());
    }
  }

  private void giveRewards(Player player, List<ItemStack> rewards) {
    player.sendMessage(TextUtil.format("&6&lYou earned 1 egg reward!"));
    for (ItemStack winReward : rewards) {
      PlayerDataManager.getInstance().givePlayerReward(player.getUniqueId(), winReward);
    }
  }
}
