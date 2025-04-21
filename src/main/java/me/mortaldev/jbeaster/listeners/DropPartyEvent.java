package me.mortaldev.jbeaster.listeners;

import me.mortaldev.jbeaster.Main;
import me.mortaldev.jbeaster.configs.MainConfig;
import me.mortaldev.jbeaster.modules.dropparty.DropPartyCRUD;
import me.mortaldev.jbeaster.modules.dropparty.DropPartyController;
import me.mortaldev.jbeaster.modules.dropparty.DropPartyData;
import me.mortaldev.jbeaster.utils.NBTAPI;
import me.mortaldev.jbeaster.utils.TextUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class DropPartyEvent implements Listener {

  @EventHandler
  public void onPlace(BlockPlaceEvent event) {
    ItemStack itemStack = DropPartyCRUD.getInstance().get().getItemStack();
    if (itemStack == null) {
      return;
    }
    if (!event.getItemInHand().isSimilar(itemStack)) {
      return;
    }
    if (DropPartyController.getInstance().playerHasActiveParty(event.getPlayer())) {
      event.getPlayer().sendMessage(TextUtil.format("&cYou can only do one drop party at a time."));
      event.setCancelled(true);
      return;
    }
    if (!DropPartyCRUD.getInstance().get().hasPlaceableLocation(event.getBlockAgainst().getLocation())) {
      event.getPlayer().sendMessage(TextUtil.format("&cYou cannot place eggs here. Try using at a designated location."));
      event.setCancelled(true);
      return;
    }
    PlayerInventory inventory = event.getPlayer().getInventory();
    ItemStack item = inventory.getItem(event.getHand());
    event.setCancelled(true);
    if (item.getAmount() == 1) {
        inventory.setItem(event.getHand(), new ItemStack(Material.AIR));
    } else {
      item.setAmount(item.getAmount() - 1);
      inventory.setItem(event.getHand(), item);
    }
    DropPartyController.getInstance().animate(event.getBlockAgainst().getLocation(), event.getPlayer());
  }

  @EventHandler
  public void onDropPartyEvent(PlayerAttemptPickupItemEvent event) {
    if (event.getItem().getLocation().getWorld() != MainConfig.getInstance().getEggHuntWorld()) {
      return;
    }
    ItemStack itemStack = event.getItem().getItemStack();
    if (NBTAPI.hasNBT(itemStack, "removeThisUUID")) {
      NBTAPI.removeNBT(itemStack, "removeThisUUID");
    } else {
      return;
    }
    if (itemStack.getType() == Material.GOLD_NUGGET) {
      if (NBTAPI.hasNBT(itemStack, "moneyPickup")) {
        Player player = event.getPlayer();
        Main.getEconomy().depositPlayer(player, 10000);
        player.sendMessage(TextUtil.format("&eYou have received &f$10,000!"));
        event.setCancelled(true);
        event.getItem().remove();
      }
    }
  }
}
