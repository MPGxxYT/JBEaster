package me.mortaldev.jbeaster.menus;

import me.mortaldev.jbeaster.modules.egghunt.EggHuntData;
import me.mortaldev.jbeaster.modules.egghunt.EggHuntDataCRUD;
import me.mortaldev.jbeaster.utils.TextUtil;
import me.mortaldev.menuapi.InventoryGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Objects;

public class EggHuntRewardsMenu extends InventoryGUI {

  @Override
  protected Inventory createInventory() {
    return Bukkit.createInventory(null, 9 * 3, TextUtil.format("Egg Hunt Rewards"));
  }

  public EggHuntRewardsMenu() {
    allowBottomInventoryClick(true);
    allowTopInventoryClick(true);
  }

  @Override
  public void decorate(Player player) {
    int i = 0;
    for (ItemStack winReward : EggHuntDataCRUD.getInstance().get().getWinRewards()) {
      getInventory().setItem(i, winReward);
      i++;
    }
    super.decorate(player);
  }

  @Override
  public void onClose(InventoryCloseEvent event) {
    Inventory inventory = Bukkit.createInventory(null, 9 * 3);
    for (ItemStack itemStack : getInventory().getStorageContents()) {
      if (itemStack == null || itemStack.getType() == Material.AIR) {
        continue;
      }
      inventory.addItem(itemStack);
    }
    EggHuntData eggHuntData = EggHuntDataCRUD.getInstance().get();
    if (!inventory.isEmpty() || eggHuntData.getWinRewards() != Arrays.stream(inventory.getStorageContents()).filter(Objects::nonNull).toList()) {
      eggHuntData.setWinRewards(Arrays.stream(inventory.getStorageContents()).filter(Objects::nonNull).toList());
    }
    super.onClose(event);
  }
}
