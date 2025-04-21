package me.mortaldev.jbeaster.listeners;

import me.mortaldev.jbeaster.modules.miningegg.MiningEgg;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDropItemEvent;

public class MiningBunnyEvent implements Listener {

  @EventHandler
  public void onEntityDrop(EntityDropItemEvent event) {
    MiningEgg instance = MiningEgg.getInstance();
    if (!instance.hasOwner(event.getEntity())) {
      return;
    }
    event.setCancelled(true);
  }

  @EventHandler
  public void onEntityDamage(EntityDamageEvent event) {
    if (event.getEntityType() != EntityType.RABBIT) {
      return;
    }
    MiningEgg instance = MiningEgg.getInstance();
    if (!instance.hasOwner(event.getEntity())) {
      return;
    }
    if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onEntityDeath(EntityDeathEvent event) {
    if (event.getEntityType() != EntityType.RABBIT) {
      return;
    }
    MiningEgg instance = MiningEgg.getInstance();
    if (!instance.hasOwner(event.getEntity())) {
      return;
    }
    event.setDroppedExp(0);
    event.getDrops().clear();
    instance.giveReward(event.getEntity());
  }
}
