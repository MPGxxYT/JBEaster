package me.mortaldev.jbeaster.commands.eastercommand;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import java.util.ArrayList;
import java.util.List;
import me.mortaldev.jbeaster.modules.playerdata.PlayerData;
import me.mortaldev.jbeaster.modules.playerdata.PlayerDataManager;
import me.mortaldev.jbeaster.utils.TextUtil;
import me.mortaldev.jbeaster.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@CommandAlias("easterrewards|er")
public class EasterRewardsCommand extends BaseCommand {

  @Default
  public void giveRewards(Player player) {
    PlayerData playerData =
        PlayerDataManager.getInstance()
            .getByID(player.getUniqueId().toString())
            .orElse(PlayerData.create(player.getUniqueId().toString()));
    List<ItemStack> rewardOverflow = playerData.getRewardOverflow();
    if (rewardOverflow == null || rewardOverflow.isEmpty()) {
      player.sendMessage(TextUtil.format("&cYou have no rewards to claim."));
      return;
    }
    for (ItemStack itemStack : new ArrayList<>(rewardOverflow)) {
      if (!Utils.canInventoryHold(player.getInventory(), itemStack)) {
        player.sendMessage(
            TextUtil.format("&cYour inventory is full! Make some space for your rewards."));
        return;
      } else {
        player.getInventory().addItem(itemStack);
        playerData.removeRewardOverflow(itemStack);
        PlayerDataManager.getInstance().update(playerData, true);
      }
    }
    player.sendMessage(TextUtil.format("&eYou claimed your rewards!"));
  }
}
