package me.mortaldev.jbeaster.modules.playerdata;

import me.mortaldev.crudapi.CRUD;
import me.mortaldev.crudapi.CRUDManager;
import me.mortaldev.jbeaster.Main;
import me.mortaldev.jbeaster.utils.TextUtil;
import me.mortaldev.jbeaster.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class PlayerDataManager extends CRUDManager<PlayerData> {

  private static class Singleton {
    private static final PlayerDataManager INSTANCE = new PlayerDataManager();
  }

  public static PlayerDataManager getInstance() {
    return Singleton.INSTANCE;
  }

  private PlayerDataManager() {}

  @Override
  public CRUD<PlayerData> getCRUD() {
    return PlayerDataCRUD.getInstance();
  }

  @Override
  public void log(String string) {
    Main.log(string);
  }

  public void givePlayerReward(Player player, ItemStack reward) {
    givePlayerReward(player.getUniqueId(), reward);
  }
  public void givePlayerReward(UUID uuid, ItemStack reward) {
    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
    PlayerData playerData = getByID(uuid.toString()).orElse(new PlayerData(uuid.toString()));
    if (!offlinePlayer.isOnline()) { // Player is offline
      playerData.addRewardOverflow(reward);
    } else { // Player is online
      Player player = offlinePlayer.getPlayer();
      if (player == null) { // But somehow null
        playerData.addRewardOverflow(reward);
      } else if (!Utils.canInventoryHold(player.getInventory(), reward)) { // Inventory is full
        player.sendMessage(
            TextUtil.format("&cYour inventory was full! Reward added to '/easterRewards'."));
        playerData.addRewardOverflow(reward);
      } else { // Has space for item
        player.getInventory().addItem(reward);
      }
    }
    update(playerData, true);
  }
}
