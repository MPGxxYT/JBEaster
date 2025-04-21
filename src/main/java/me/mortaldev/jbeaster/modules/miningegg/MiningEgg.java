package me.mortaldev.jbeaster.modules.miningegg;

import com.destroystokyo.paper.ParticleBuilder;
import me.mortaldev.jbeaster.Main;
import me.mortaldev.jbeaster.configs.MainConfig;
import me.mortaldev.jbeaster.modules.ActiveDate;
import me.mortaldev.jbeaster.modules.bunnyrace.BunnyRaceController;
import me.mortaldev.jbeaster.modules.egghunt.EggHuntDataCRUD;
import me.mortaldev.jbeaster.modules.playerdata.PlayerDataManager;
import me.mortaldev.jbeaster.utils.ChanceMap;
import me.mortaldev.jbeaster.utils.TextUtil;
import me.mortaldev.jbeaster.utils.Utils;
import org.bukkit.*;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public class MiningEgg {

  private static class Singleton {
    private static final MiningEgg INSTANCE = new MiningEgg();
  }

  public static MiningEgg getInstance() {
    return Singleton.INSTANCE;
  }

  private MiningEgg() {}

  private final HashMap<Entity, OwnerRarity> entityOwners = new HashMap<>();

  public void giveReward(Entity entity) {
    ItemStack currencyItemStack =
        EggHuntDataCRUD.getInstance().get().getCurrencyItemStack().clone();
    OwnerRarity ownerRarity = entityOwners.get(entity);
    ChanceRollRarity rarity = ownerRarity.rarity();
    switch (rarity) {
      case COMMON -> currencyItemStack.setAmount(MainConfig.getInstance().getCommonReward());
      case RARE -> currencyItemStack.setAmount(MainConfig.getInstance().getRareReward());
      case LEGENDARY -> currencyItemStack.setAmount(MainConfig.getInstance().getLegendaryReward());
    }
    UUID uuid = ownerRarity.uuid();
    PlayerDataManager.getInstance().givePlayerReward(uuid, currencyItemStack);
    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
    if (offlinePlayer.isOnline()) {
      offlinePlayer.getPlayer().sendMessage(
              TextUtil.format("&eYou received &f" + currencyItemStack.getAmount() + "&e eggs!"));
    }
  }

  public HashMap<Entity, OwnerRarity> getEntityOwners() {
    return entityOwners;
  }

  public boolean hasOwner(Entity entity) {
    return entityOwners.containsKey(entity);
  }

  public void setOwner(Entity entity, OwnerRarity ownerRarity) {
    entityOwners.put(entity, ownerRarity);
  }

  public void removeOwner(Entity entity) {
    entityOwners.remove(entity);
  }

  public void rollAt(Player player, Location location) {
    if (!MainConfig.getInstance().getMiningEggEnabled()) {
      Main.debugLog("mining egg not enabled");
      return;
    }
    if (!ActiveDate.getInstance().isEventActive()) {
      Main.debugLog("event not active");
      return;
    }
    ChanceMap<Boolean> spawnMap = new ChanceMap<>();
    spawnMap.put(true, MainConfig.getInstance().getChanceToRoll(), false);
    spawnMap.put(false, 100 - MainConfig.getInstance().getChanceToRoll(), false);
    Main.debugLog("spawnMap = " + spawnMap.getTable());
    boolean b = spawnMap.roll();
    if (!b) {
      Main.debugLog("no spawn");
      return;
    }
    ChanceMap<ChanceRollRarity> rarityChanceMap = new ChanceMap<>();
    rarityChanceMap.put(ChanceRollRarity.COMMON, MainConfig.getInstance().getCommonChance(), false);
    rarityChanceMap.put(ChanceRollRarity.RARE, MainConfig.getInstance().getRareChance(), false);
    rarityChanceMap.put(
        ChanceRollRarity.LEGENDARY, MainConfig.getInstance().getLegendaryChance(), true);
    ChanceRollRarity roll = rarityChanceMap.roll();
    switch (roll) {
      case COMMON -> {
        Entity entity =
            spawnBunny(
                location,
                MainConfig.getInstance().getCommonHealth(),
                ChanceRollRarity.COMMON.getId());
        setOwner(entity, new OwnerRarity(player.getUniqueId(), ChanceRollRarity.COMMON));
      }
      case RARE -> {
        Entity entity =
            spawnBunny(
                location, MainConfig.getInstance().getRareHealth(), ChanceRollRarity.RARE.getId());
        setOwner(entity, new OwnerRarity(player.getUniqueId(), ChanceRollRarity.RARE));
      }
      case LEGENDARY -> {
        Entity entity =
            spawnBunny(
                location,
                MainConfig.getInstance().getLegendaryHealth(),
                ChanceRollRarity.LEGENDARY.getId());
        setOwner(entity, new OwnerRarity(player.getUniqueId(), ChanceRollRarity.LEGENDARY));
      }
    }
  }

  private Location checkLocation(Location location) {
    if (location.getBlock().getType() == Material.AIR) {
      return location;
    }
    for (Block block : Utils.cubicAround(location, 1)) {
      if (block.getType() == Material.AIR) {
        for (int i = 1; i <= 3; i++) {
          if (block.getRelative(0, i, 0).getType() != Material.AIR) {
            return block.getRelative(0, i + 1, 0).getLocation();
          }
        }
        for (int i = -1; i >= -3; i--) {
          if (block.getRelative(0, i, 0).getType() != Material.AIR) {
            return block.getRelative(0, i + 1, 0).getLocation();
          }
        }
      }
    }
    return location;
  }

  private Entity spawnBunny(Location location, int health, String name) {
    World world = location.getWorld();
    Entity entity = world.spawnEntity(checkLocation(location), EntityType.RABBIT);
    entity.setInvulnerable(true);
    BunnyRaceController.getInstance().delayedRunnable(() -> entity.setInvulnerable(false), 10L);
    entity.customName(TextUtil.format(name));
    entity.setCustomNameVisible(true);
    if (health > 0) {
      if (entity instanceof Attributable attributable) {
        AttributeInstance attribute = attributable.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attribute == null) {
          attributable.registerAttribute(Attribute.GENERIC_MAX_HEALTH);
          attribute = attributable.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        }
        attribute.setBaseValue(health);
      }
      if (entity instanceof Damageable damageable) {
        damageable.setHealth(health);
      }
    }
    bunnyTimer(entity);
    return entity;
  }

  private void bunnyTimer(Entity entity) {
    Bukkit.getScheduler()
        .scheduleSyncDelayedTask(
            Main.getInstance(),
            () -> {
              if (entity.isDead()) {
                return;
              }
              Location location = entity.getLocation();
              new ParticleBuilder(Particle.CLOUD).extra(0.25).location(location).count(10).spawn();
              entity.remove();
              removeOwner(entity);
            },
            1200);
  }
}
