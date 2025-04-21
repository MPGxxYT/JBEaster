package me.mortaldev.jbeaster.listeners.bunnyrace;

import me.mortaldev.jbeaster.configs.MainConfig;
import me.mortaldev.jbeaster.modules.bunnyrace.BunnyRaceDataCRUD;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

public class BunnyDenyEvent implements Listener {

  @EventHandler
  public void onEntityEvent(EntityPotionEffectEvent event) {
    if (event.getEntity().getType() != EntityType.RABBIT) {
      return;
    }
    if (event.getEntity().getWorld() != MainConfig.getInstance().getEggHuntWorld()) {
      return;
    }
    event.setCancelled(true);
  }

  @EventHandler
  public void onEntityEvent(PlayerLeashEntityEvent event) {
    if (event.getEntity().getType() != EntityType.RABBIT) {
      return;
    }
    if (event.getEntity().getWorld() != MainConfig.getInstance().getEggHuntWorld()) {
      return;
    }
    event.setCancelled(true);
  }

  @EventHandler
  public void onEntityEvent(ProjectileHitEvent event) {
    if (event.getEntity().getType() == EntityType.FISHING_HOOK) {
      if (event.getEntity().getWorld() != MainConfig.getInstance().getEggHuntWorld()) {
        return;
      }
      Entity hitEntity = event.getHitEntity();
      if (hitEntity == null) {
        return;
      }
      if (hitEntity.getType() == EntityType.RABBIT) {
        event.getEntity().remove();
        event.setCancelled(true);
      }
    } else if (event.getEntity().getType() == EntityType.ENDER_PEARL) {
      if (event.getEntity().getWorld() != MainConfig.getInstance().getEggHuntWorld()) {
        return;
      }
      Location location = BunnyRaceDataCRUD.getInstance().get().getInformationDisplayLocation();
      if (event.getEntity().getLocation().distance(location) <= 15) {
        event.getEntity().remove();
        event.setCancelled(true);
      }
    }
  }
}
