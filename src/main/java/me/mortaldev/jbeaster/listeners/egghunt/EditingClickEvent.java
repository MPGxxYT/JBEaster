package me.mortaldev.jbeaster.listeners.egghunt;

import me.mortaldev.jbeaster.Main;
import me.mortaldev.jbeaster.modules.egghunt.EggHuntData;
import me.mortaldev.jbeaster.modules.egghunt.EggHuntDataCRUD;
import me.mortaldev.jbeaster.utils.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class EditingClickEvent implements Listener {

  private static final Set<UUID> editingPlayers = new HashSet<>();
  public static final int DELAY_IN_TICKS = 10;
  private final Set<UUID> delayedPlayers = new HashSet<>();

  private void doDelay(UUID uuid) {
    delayedPlayers.add(uuid);
    Bukkit.getScheduler()
        .scheduleSyncDelayedTask(
            Main.getInstance(), () -> delayedPlayers.remove(uuid), DELAY_IN_TICKS);
  }

  public static boolean isEditingPlayer(UUID uuid) {
    return editingPlayers.contains(uuid);
  }

  public static void addEditingPlayer(UUID uuid) {
    editingPlayers.add(uuid);
  }

  public static void removeEditingPlayer(UUID uuid) {
    editingPlayers.remove(uuid);
  }

  @EventHandler
  public void onClick(PlayerInteractEvent event) {
    Player player = event.getPlayer();
    if (event.isBlockInHand()) {
      return;
    }
    if (isEditingPlayer(player.getUniqueId())) {
      if (delayedPlayers.contains(player.getUniqueId())) {
        return;
      }
      doDelay(player.getUniqueId());
      EggHuntData eggHuntData = EggHuntDataCRUD.getInstance().get();
      Block clickedBlock = event.getClickedBlock();
      if (clickedBlock == null) {
        return;
      }
      Location location = clickedBlock.getLocation();
      if (eggHuntData.hasLocation(location)) {
        eggHuntData.removeLocation(location);
        player.sendMessage(
            TextUtil.format(
                "&eRemoved egg at &7"
                    + "&7 "
                    + location.getBlockX()
                    + "&e,&7 "
                    + location.getBlockY()
                    + "&e,&7 "
                    + location.getBlockZ()
                    + "&e,&7 "
                    + location.getWorld().getName()
                    + "&e."));
      } else {
        eggHuntData.addLocation(location);
        player.sendMessage(
            TextUtil.format(
                "&eAdded egg at &7"
                    + "&7 "
                    + location.getBlockX()
                    + "&e,&7 "
                    + location.getBlockY()
                    + "&e,&7 "
                    + location.getBlockZ()
                    + "&e,&7 "
                    + location.getWorld().getName()
                    + "&e."));
      }
    }
  }
}
