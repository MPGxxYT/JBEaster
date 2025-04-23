package me.mortaldev.jbeaster.modules.dropparty;

import com.destroystokyo.paper.ParticleBuilder;
import me.mortaldev.jbeaster.Main;
import me.mortaldev.jbeaster.modules.egghunt.EggHuntDataCRUD;
import me.mortaldev.jbeaster.modules.playerdata.PlayerDataManager;
import me.mortaldev.jbeaster.utils.ItemStackHelper;
import me.mortaldev.jbeaster.utils.NBTAPI;
import me.mortaldev.jbeaster.utils.TextUtil;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DropPartyController {

  private static class Singleton {
    private static final DropPartyController INSTANCE = new DropPartyController();
  }

  public static DropPartyController getInstance() {
    return Singleton.INSTANCE;
  }

  private final HashSet<Entity> entities = new HashSet<>();
  private final HashMap<String, Integer> schedules = new HashMap<>();

  private DropPartyController() {}

  public boolean playerHasActiveParty(Player player) {
    return schedules.containsKey("party." + player.getUniqueId());
  }

  public void killEntities() {
    for (Entity entity : entities) {
      entity.remove();
    }
    entities.clear();
  }

  public void killSchedules() {
    for (String key : schedules.keySet()) {
      Bukkit.getScheduler().cancelTask(schedules.get(key));
    }
    schedules.clear();
  }

  public List<ItemStack> getDrops() {
    ItemStack moneyPickup =
        ItemStackHelper.builder(Material.GOLD_NUGGET).name("&6&l10k Pickup").build();
    ItemStack currencyItemStack =
        EggHuntDataCRUD.getInstance().get().getCurrencyItemStack().clone();
    currencyItemStack.setAmount(1);
    NBTAPI.addNBT(moneyPickup, "moneyPickup", "true");
    List<ItemStack> drops = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      drops.add(moneyPickup.clone());
      drops.add(currencyItemStack.clone());
    }
    return drops;
  }

  public void animate(Location location, Player player) {
    location.toCenterLocation();
    World world = location.getWorld();
    if (world == null) {
      player.getInventory().addItem(DropPartyCRUD.getInstance().get().getItemStack());
      player.sendMessage(TextUtil.format("&eFailed to start drop party."));
      return;
    }
    Bukkit.getServer()
        .sendMessage(TextUtil.format("&e&lEaster Drop&f Party &eis opening at spawn!"));
    Entity entity = world.spawnEntity(location, EntityType.ARMOR_STAND);
    entities.add(entity);
    entity.setCustomNameVisible(false);
    entity.setInvulnerable(true);
    entity.setVelocity(new Vector(0, 1, 0));
    new DelayedRunnable(() -> entity.setGravity(false), 5L, true);
    if (entity instanceof ArmorStand armorStand) {
      armorStand.setVisible(false);
      armorStand.setItem(EquipmentSlot.HEAD, DropPartyCRUD.getInstance().get().getItemStack());
    }
    int particleCount = 200;
    Location stormLocation = entity.getLocation().clone().add(0, 5, 0);
    AtomicInteger stormStarter = new AtomicInteger(0);
    AtomicInteger thunderInterval = new AtomicInteger(5);
    AtomicInteger dropsInterval = new AtomicInteger(0);
    DelayedRunnable thunder =
        new DelayedRunnable(
            () -> {
              new ParticleBuilder(Particle.CLOUD)
                  .count(particleCount)
                  .offset(4, 0.5, 4)
                  .location(stormLocation)
                  .extra(0)
                  .spawn();
              new ParticleBuilder(Particle.ELECTRIC_SPARK)
                  .count(particleCount)
                  .offset(4, 5, 4)
                  .location(stormLocation)
                  .extra(0)
                  .spawn();
              new DelayedRunnable(entity::remove, 1L, true);
            },
            1L);
    List<ItemStack> drops = getDrops();
    schedules.put(
        "party." + player.getUniqueId(),
        Bukkit.getScheduler()
            .scheduleSyncRepeatingTask(
                Main.getInstance(),
                () -> {
                  int stormStarterNum = stormStarter.getAndIncrement();
                  if (stormStarterNum < 4) {
                    int i = (int) (particleCount * (stormStarterNum * 0.25));
                    new ParticleBuilder(Particle.CAMPFIRE_SIGNAL_SMOKE)
                        .count(i)
                        .offset(stormStarterNum, 0.25, stormStarterNum)
                        .location(stormLocation)
                        .extra(0)
                        .spawn();
                    new ParticleBuilder(Particle.CAMPFIRE_COSY_SMOKE)
                        .count(i)
                        .offset(stormStarterNum, 0.5, stormStarterNum)
                        .location(stormLocation)
                        .extra(0)
                        .spawn();
                    new ParticleBuilder(Particle.SMOKE_NORMAL)
                        .count(i)
                        .offset(stormStarterNum, 0.25, stormStarterNum)
                        .location(stormLocation)
                        .extra(0)
                        .spawn();
                  } else if (stormStarterNum % thunderInterval.get() == 0) {
                    if (thunderInterval.get() == 5) {
                      thunderInterval.set(8);
                    } else {
                      thunderInterval.set(13);
                    }
                    thunder.run();
                  }
                  if (stormStarterNum > 4 && stormStarterNum % 10 == 0) {
                    new ParticleBuilder(Particle.CAMPFIRE_SIGNAL_SMOKE)
                        .count(100)
                        .offset(4, 0.25, 4)
                        .location(stormLocation)
                        .extra(0)
                        .spawn();
                    new ParticleBuilder(Particle.CAMPFIRE_COSY_SMOKE)
                        .count(100)
                        .offset(4, 0.5, 4)
                        .location(stormLocation)
                        .extra(0)
                        .spawn();
                    new ParticleBuilder(Particle.SMOKE_NORMAL)
                        .count(100)
                        .offset(4, 0.25, 4)
                        .location(stormLocation)
                        .extra(0)
                        .spawn();
                  }
                  if (stormStarterNum % 4 == 0) {
                    int dropsIntervalAndIncrement = dropsInterval.getAndIncrement();
                    if (dropsIntervalAndIncrement < drops.size()) {
                      ItemStack itemStack = drops.get(dropsIntervalAndIncrement);
                      dropItemStack(stormLocation, itemStack);
                    }
                  }
                  if (stormStarterNum >= 80) {
                    new DelayedRunnable(() -> displayUniqueReward(stormLocation, player), 5L, true);
                    Bukkit.getScheduler()
                        .cancelTask(schedules.remove("party." + player.getUniqueId()));
                  }
                },
                8L,
                5L));
  }

  private void dropItemStack(Location location, ItemStack itemStack) {
    Random random = new Random();
    int offsetX = random.nextInt(17) - 8;
    int offsetZ = random.nextInt(17) - 8;
    Location offsetLocation = location.clone().add(offsetX, 0, offsetZ);
    NBTAPI.addNBT(itemStack, "removeThisUUID", UUID.randomUUID().toString());
    location.getWorld().dropItem(offsetLocation, itemStack);
  }

  private void displayUniqueReward(Location location, Player player) {
    World world = location.getWorld();
    ItemStack winStack = ItemStackHelper.deserialize(DropPartyCRUD.getInstance().get().getChanceMap().roll());
    if (world == null) {
      player.sendMessage(TextUtil.format("&cInvalid world for displaying reward!"));
      return;
    }
    Entity entity = world.spawnEntity(location, EntityType.ARMOR_STAND);
    entities.add(entity);
    entity.setCustomNameVisible(false);
    entity.setInvulnerable(true);
    entity.setVelocity(new Vector(0, -0.6, 0));
    new DelayedRunnable(() -> entity.setGravity(false), 5L, true);
    if (entity instanceof ArmorStand armorStand) {
      armorStand.setVisible(false);
      armorStand.setItem(EquipmentSlot.HEAD, winStack);
      armorStand.customName(winStack.getItemMeta().displayName());
      armorStand.setCustomNameVisible(true);
    }
    new DelayedRunnable(
        () -> {
          entity.remove();
          PlayerDataManager.getInstance().givePlayerReward(player.getUniqueId(), winStack);
          if (player.isOnline()) {
            String snbt = ItemStackHelper.toSNBT(winStack, true);
            player.sendMessage(
                TextUtil.format(
                    "&eYou have received a ##&f&o[unique reward]##itm:" + snbt + "##", true));
          }
        },
        100L,
        true);
  }
}
