package me.mortaldev.jbeaster.commands.eastercommand;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import me.mortaldev.jbeaster.modules.playerdata.PlayerData;
import me.mortaldev.jbeaster.modules.playerdata.PlayerDataManager;
import me.mortaldev.jbeaster.utils.ItemStackHelper;
import me.mortaldev.jbeaster.utils.TextUtil;
import me.mortaldev.jbeaster.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@CommandAlias("easterrewards|er")
public class EasterRewardsCommand extends BaseCommand {

  @Default
  public void giveRewards(Player player) {
    PlayerData playerData =
        PlayerDataManager.getInstance()
            .getByID(player.getUniqueId().toString())
            .orElse(new PlayerData(player.getUniqueId().toString()));
    List<String> rewardOverlowStrings = playerData.getRewardOverlowStrings();
    if (rewardOverlowStrings == null || rewardOverlowStrings.isEmpty()) {
      player.sendMessage(TextUtil.format("&cYou have no rewards to claim."));
      return;
    }
    boolean update = false;
    List<String> strings = new ArrayList<>(rewardOverlowStrings);
    for (String str : strings) {
      ItemStack itemStack = ItemStackHelper.deserialize(str);
      if (!Utils.canInventoryHold(player.getInventory(), itemStack)) {
        player.sendMessage(
            TextUtil.format("&cYour inventory is full! Make some space for your rewards."));
        return;
      } else {
        player.getInventory().addItem(itemStack);
        playerData.removeRewardOverflowString(str);
        update = true;
      }
    }
    if (update) {
      PlayerDataManager.getInstance().update(playerData, true);
    }
    player.sendMessage(TextUtil.format("&eYou claimed your rewards!"));
  }
}
