package me.mortaldev.jbeaster.commands.eastercommand;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.*;
import me.mortaldev.jbeaster.Main;
import me.mortaldev.jbeaster.configs.MainConfig;
import me.mortaldev.jbeaster.listeners.egghunt.EditingClickEvent;
import me.mortaldev.jbeaster.menus.EggHuntRewardsMenu;
import me.mortaldev.jbeaster.menus.DropPartyLoottableMenu;
import me.mortaldev.jbeaster.modules.bunnyrace.BunnyRaceController;
import me.mortaldev.jbeaster.modules.bunnyrace.BunnyRaceDataCRUD;
import me.mortaldev.jbeaster.modules.dropparty.DropPartyCRUD;
import me.mortaldev.jbeaster.modules.dropparty.DropPartyController;
import me.mortaldev.jbeaster.modules.dropparty.DropPartyData;
import me.mortaldev.jbeaster.modules.egghunt.EggHuntData;
import me.mortaldev.jbeaster.modules.egghunt.EggHuntDataCRUD;
import me.mortaldev.jbeaster.modules.miningegg.MiningEgg;
import me.mortaldev.jbeaster.modules.playerdata.PlayerData;
import me.mortaldev.jbeaster.modules.playerdata.PlayerDataManager;
import me.mortaldev.jbeaster.records.Pair;
import me.mortaldev.jbeaster.utils.ItemStackHelper;
import me.mortaldev.jbeaster.utils.TextUtil;
import me.mortaldev.menuapi.GUIManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

@CommandAlias("jbeaster||jbe")
@CommandPermission("jbeaster.admin")
@SuppressWarnings("InnerClassMayBeStatic")
public class JBEasterCommand extends BaseCommand {

  private String locationToString(Location location, boolean includeWorld) {
    if (location == null) {
      return " ~ ";
    }
    String x = String.format("%.2f", location.getX());
    String y = String.format("%.2f", location.getY());
    String z = String.format("%.2f", location.getZ());
    if (includeWorld) {
      return location.getWorld().getName() + ", " + x + ", " + y + ", " + z;
    }
    return x + ", " + y + ", " + z;
  }

  @Subcommand("reloadplayer")
  public void reloadPlayer(Player player, OfflinePlayer offlinePlayer) {
    Optional<PlayerData> playerDataOptional =
        PlayerDataManager.getInstance().getByID(offlinePlayer.getUniqueId().toString());
    if (playerDataOptional.isEmpty()) {
      player.sendMessage(TextUtil.format("&cThis player has never joined!"));
      return;
    }
    PlayerData playerData = playerDataOptional.get();
    PlayerDataManager.getInstance().loadByID(playerData.getID());
    player.sendMessage(TextUtil.format("&fLoaded player &7" + offlinePlayer.getName()));
  }

  @Subcommand("dropparty")
  public class DropPartySubcommand extends BaseCommand {

    @Subcommand("kill entities")
    public void killEntities(Player player) {
      DropPartyController.getInstance().killEntities();
      player.sendMessage(TextUtil.format("&eKilled all armorstands!"));
    }

    @Subcommand("kill schedules")
    public void killSchedules(Player player) {
      DropPartyController.getInstance().killSchedules();
      player.sendMessage(TextUtil.format("&eKilled all schedules!"));
    }

    @Subcommand("kill all")
    public void killAll(Player player) {
      DropPartyController.getInstance().killSchedules();
      DropPartyController.getInstance().killEntities();
      player.sendMessage(TextUtil.format("&eKilled all!"));
    }

    @Subcommand("add placeable")
    public void addPlaceable(Player player) {
      Block block = player.getTargetBlockExact(15);
      if (block == null) {
        player.sendMessage(TextUtil.format("&cYou must be looking at a block!"));
        return;
      }
      DropPartyData dropPartyData = DropPartyCRUD.getInstance().get();
      if (dropPartyData.hasPlaceableLocation(block.getLocation())) {
        player.sendMessage(TextUtil.format("&cThis location is already added!"));
        return;
      }
      dropPartyData.addPlaceableLocation(block.getLocation());
      player.sendMessage(
          TextUtil.format("&eAdded location: &7" + locationToString(block.getLocation(), false)));
    }

    @Subcommand("remove placeable")
    public void removePlaceable(Player player) {
      Block block = player.getTargetBlockExact(15);
      if (block == null) {
        player.sendMessage(TextUtil.format("&cYou must be looking at a block!"));
        return;
      }
      DropPartyData dropPartyData = DropPartyCRUD.getInstance().get();
      if (!dropPartyData.hasPlaceableLocation(block.getLocation())) {
        player.sendMessage(TextUtil.format("&cThis location is not added."));
        return;
      }
      dropPartyData.removePlaceableLocation(block.getLocation());
      player.sendMessage(
          TextUtil.format("&eRemoved location: &7" + locationToString(block.getLocation(), false)));
    }

    @Subcommand("list placeable")
    public void listPlaceable(Player player) {
      DropPartyData dropPartyData = DropPartyCRUD.getInstance().get();
      for (Location location : dropPartyData.getPlaceableLocations()) {
        player.sendMessage(TextUtil.format("&7" + locationToString(location, true)));
      }
    }

    @Subcommand("set item")
    public void setDropPartyItem(Player player) {
      ItemStack item = player.getInventory().getItemInMainHand();
      if (!item.getType().isBlock()) {
        player.sendMessage(TextUtil.format("&cYou must be holding a block!"));
        return;
      }
      DropPartyCRUD.getInstance().get().setItemStack(item);
      player.sendMessage(TextUtil.format("&6Item set!"));
    }

    @Subcommand("get item")
    public void getDropPartyItem(Player player) {
      ItemStack itemStack = DropPartyCRUD.getInstance().get().getItemStack();
      if (itemStack == null) {
        player.sendMessage(TextUtil.format("&cItem not set!"));
        return;
      }
      player.getInventory().addItem(itemStack);
      player.sendMessage(TextUtil.format("&eItem given."));
    }

    @Subcommand("loottable")
    public void openLoottable(Player player) {
      GUIManager.getInstance()
          .openGUI(new DropPartyLoottableMenu(DropPartyCRUD.getInstance().get()), player);
    }

    @Subcommand("roll")
    public void rollLoottable(Player player) {
      String deserialize = DropPartyCRUD.getInstance().get().getChanceMap().roll();
      ItemStack win = ItemStackHelper.deserialize(deserialize);
      player.getInventory().addItem(win);
      player.sendMessage(TextUtil.format("&aYou rolled &f" + win.getType()));
    }

    @Subcommand("test")
    public void testAnimate(Player player) {
      DropPartyController instance = DropPartyController.getInstance();
      instance.killEntities();
      instance.animate(player.getTargetBlockExact(15).getLocation(), player);
    }
  }

  @Subcommand("mining")
  public class MiningSubcommand extends BaseCommand {

    @Subcommand("roll")
    public void roll(Player player) {
      if (!MainConfig.getInstance().getMiningEggEnabled()) {
        player.sendMessage(TextUtil.format("&cMining Egg is disabled!"));
        return;
      }
      Block targetBlockExact = player.getTargetBlockExact(15);
      if (targetBlockExact == null) {
        player.sendMessage(TextUtil.format("&cYou must be looking at a block!"));
        return;
      }
      MiningEgg.getInstance().rollAt(player, targetBlockExact.getLocation());
      player.sendMessage(TextUtil.format("Rolling..."));
    }
  }

  @Subcommand("bunny")
  public class BunnySubcommand extends BaseCommand {

    @Subcommand("race")
    public void race(Player player) {
      if (!MainConfig.getInstance().getBunnyRaceEnabled()) {
        player.sendMessage(TextUtil.format("&cBunny Race is disabled!"));
        return;
      }
      BunnyRaceController.getInstance().startRace();
    }

    @Subcommand("reset")
    public void reset(Player player) {
      if (!MainConfig.getInstance().getBunnyRaceEnabled()) {
        player.sendMessage(TextUtil.format("&cBunny Race is disabled!"));
        return;
      }
      BunnyRaceController.getInstance().resetController();
    }

    @Subcommand("set name")
    @Syntax("<name> <id>")
    @CommandCompletion("<name> @range:1-4")
    public void setName(Player player, String name, int id) {
      if (id < 1 || id > 4) {
        player.sendMessage(TextUtil.format("&cMust be between 1 and 4!"));
        return;
      }
      BunnyRaceDataCRUD.getInstance().get().setBunnyName(id, name);
      player.sendMessage(TextUtil.format("&eBunny &f" + id + "&e name set to &f" + name));
    }

    @Subcommand("list finishline")
    public void listFinishLine(Player player) {
      Pair<Location, Location> finishLine = BunnyRaceDataCRUD.getInstance().get().getFinishLine();
      player.sendMessage(
          TextUtil.format(
              "&eThe finish line is from &f"
                  + locationToString(finishLine.first(), false)
                  + " &eto &f"
                  + locationToString(finishLine.second(), false)));
    }

    @Subcommand("list homes")
    public void listHomes(Player player) {
      HashMap<Integer, Location> bunnyHomes = BunnyRaceDataCRUD.getInstance().get().getBunnyHomes();
      if (bunnyHomes.isEmpty()) {
        player.sendMessage(TextUtil.format("&cNo bunny homes set!"));
        return;
      }
      for (Map.Entry<Integer, Location> entry : bunnyHomes.entrySet()) {
        Location start = entry.getValue();
        player.sendMessage(
            TextUtil.format(
                "&eBunny #"
                    + entry.getKey()
                    + "&e:&f "
                    + locationToString(start, false)
                    + locationToHoverTP(start),
                true));
      }
    }

    @Subcommand("set infoDisplay")
    public void setInfoDisplay(Player player) {
      BunnyRaceDataCRUD.getInstance().get().setInformationDisplayLocation(player.getLocation());
      player.sendMessage(
          TextUtil.format(
              "&eSet information display location to &f"
                  + locationToString(player.getLocation(), false)));
    }

    @Subcommand("set finishline")
    @Syntax("<number>")
    @CommandCompletion("@range:1-2")
    public void setFinishLine(Player player, int number) {
      if (number < 1 || number > 2) {
        player.sendMessage(TextUtil.format("&cMust be between 1 and 2!"));
        return;
      }
      Block targetBlock = player.getTargetBlockExact(15);
      if (targetBlock == null) {
        player.sendMessage(TextUtil.format("&cYou must be looking at a block!"));
        return;
      }
      Location location = targetBlock.getLocation();
      switch (number) {
        case 1:
          BunnyRaceDataCRUD.getInstance().get().setFinishCorner1(location);
        case 2:
          BunnyRaceDataCRUD.getInstance().get().setFinishCorner2(location);
      }
      player.sendMessage(
          TextUtil.format(
              "&eSet &fcorner "
                  + number
                  + " &eof the finish line to &f"
                  + locationToString(location, false)));
    }

    @Subcommand("set startpoint")
    @Syntax("<number>")
    @CommandCompletion("@range:1-4")
    public void setStart(Player player, int number) {
      if (number < 1 || number > 4) {
        player.sendMessage(TextUtil.format("&cMust be between 1 and 4!"));
        return;
      }
      BunnyRaceDataCRUD.getInstance().get().setStart(number, player.getLocation());
      player.sendMessage(
          TextUtil.format("&eSet startpoint for &fBunny " + number + "&e to your location!"));
    }

    @Subcommand("set home")
    @Syntax("<number>")
    @CommandCompletion("@range:1-4")
    public void setHome(Player player, int number) {
      if (number < 1 || number > 4) {
        player.sendMessage(TextUtil.format("&cMust be between 1 and 4!"));
        return;
      }
      BunnyRaceDataCRUD.getInstance().get().setBunnyHome(number, player.getLocation());
      player.sendMessage(
          TextUtil.format("&eSet HOME for &fBunny " + number + "&e to your location!"));
    }

    @Subcommand("set endpoint")
    @Syntax("<number>")
    @CommandCompletion("@range:1-4")
    public void setEnd(Player player, int number) {
      if (number < 1 || number > 4) {
        player.sendMessage(TextUtil.format("&cMust be between 1 and 4!"));
        return;
      }
      BunnyRaceDataCRUD.getInstance().get().setEnd(number, player.getLocation());
      player.sendMessage(
          TextUtil.format("&eSet endpoint for &fBunny " + number + "&e to your location!"));
    }

    private String locationToHoverTP(Location location) {
      if (location == null) {
        return "";
      }
      String x = String.format("%.2f", location.getX());
      String y = String.format("%.2f", location.getY());
      String z = String.format("%.2f", location.getZ());
      return "##ttp:&7[Teleport]##cmd:/tp " + x + " " + y + " " + z + "##";
    }

    @Subcommand("list points")
    public void listPoints(Player player) {
      HashMap<Integer, Pair<Location, Location>> bunnyPoints =
          BunnyRaceDataCRUD.getInstance().get().getBunnyPoints();
      if (bunnyPoints.isEmpty()) {
        player.sendMessage(TextUtil.format("&eNo bunny points set!"));
      }
      for (Map.Entry<Integer, Pair<Location, Location>> entry : bunnyPoints.entrySet()) {
        Pair<Location, Location> value = entry.getValue();
        Location start = value.first();
        Location end = value.second();
        player.sendMessage(
            TextUtil.format(
                "&eBunny #"
                    + entry.getKey()
                    + "&e:&f "
                    + locationToString(start, false)
                    + locationToHoverTP(start)
                    + "&e  to  &f"
                    + locationToString(end, false)
                    + locationToHoverTP(end),
                true));
      }
    }
  }

  @Subcommand("debug")
  public void toggleDebug(CommandIssuer issuer) {
    boolean b = Main.toggleDebug();
    issuer.sendMessage("Debug mode: " + b);
  }

  @Subcommand("config")
  public class ConfigReloadBunnySubcommand extends BaseCommand {
    @Subcommand("reload bunnyrace")
    public void reload(CommandIssuer issuer) {
      issuer.sendMessage(MainConfig.getInstance().reload());
    }
  }

  @Subcommand("config")
  public class ConfigReloadSubcommand extends BaseCommand {
    @Subcommand("reload")
    public void reload(CommandIssuer issuer) {
      issuer.sendMessage(MainConfig.getInstance().reload());
    }
  }

  @Subcommand("egghunt")
  public class EggHuntSubcommand extends BaseCommand {

    @Subcommand("editRewards")
    public void editRewards(Player player) {
      GUIManager.getInstance().openGUI(new EggHuntRewardsMenu(), player);
    }

    @Subcommand("setCurrency")
    public void setCurrency(Player player) {
      ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
      if (itemInMainHand.getType() == Material.AIR) {
        player.sendMessage(TextUtil.format("&cMust have something in your Main Hand!"));
        return;
      }
      EggHuntDataCRUD.getInstance().get().setCurrencyItemStack(itemInMainHand);
      String heldSNBT = ItemStackHelper.toSNBT(itemInMainHand, true);
      player.sendMessage(
          TextUtil.format("&eSet currency to your ##&o[held item]##itm:" + heldSNBT + "##."));
    }

    @Subcommand("getCurrency")
    public void getCurrency(Player player) {
      ItemStack currencyItemStack = EggHuntDataCRUD.getInstance().get().getCurrencyItemStack();
      player.getInventory().addItem(currencyItemStack);
      String eggSNBT = ItemStackHelper.toSNBT(currencyItemStack, true);
      player.sendMessage(
          TextUtil.format("&eGive you 1 of the ##&o[currency item]##itm:" + eggSNBT));
    }

    @Subcommand("remove")
    @Syntax("<number>")
    public void removeEgg(Player player, int input) {
      EggHuntData eggHuntData = EggHuntDataCRUD.getInstance().get();
      HashSet<Location> eggLocations = eggHuntData.getLocations();
      if (eggLocations.size() <= input || eggLocations.isEmpty()) {
        player.sendMessage(
            TextUtil.format("&cInvalid egg number. Use /jbeaster eggs list to see eggs."));
        return;
      }
      Iterator<Location> eggLocationIterator = eggLocations.iterator();
      Location location = null;
      for (int i = 0; i < eggLocations.size(); i++) {
        if (!eggLocationIterator.hasNext()) {
          break;
        }
        if (i == input) {
          location = eggLocationIterator.next();
        }
      }
      if (location == null) {
        player.sendMessage(TextUtil.format("&cFailed to remove egg."));
        return;
      }
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
    }

    @Subcommand("list")
    public void listEggs(Player player) {
      EggHuntData eggHuntData = EggHuntDataCRUD.getInstance().get();
      HashSet<Location> eggLocations = eggHuntData.getLocations();
      if (eggLocations.isEmpty()) {
        player.sendMessage(TextUtil.format("&cNo eggs currently stored."));
        return;
      }
      int i = 0;
      for (Location location : eggLocations) {
        i++;
        player.sendMessage(
            TextUtil.format(
                "&e&l"
                    + i
                    + "&r&e:&7 "
                    + location.getBlockX()
                    + "&e,&7 "
                    + location.getBlockY()
                    + "&e,&7 "
                    + location.getBlockZ()
                    + "&e,&7 "
                    + location.getWorld().getName()
                    + " - ##&c&lREMOVE##ttp:&7Click to remove egg.##cmd:/jbeaster eggs remove "
                    + i,
                true));
      }
    }

    @Subcommand("edit")
    public void editEggs(Player player) {
      if (EditingClickEvent.isEditingPlayer(player.getUniqueId())) {
        EditingClickEvent.removeEditingPlayer(player.getUniqueId());
        player.sendMessage(TextUtil.format("&e&lYou are no longer editing heads!"));
        return;
      }
      EditingClickEvent.addEditingPlayer(player.getUniqueId());
      player.sendMessage(TextUtil.format("&e&lYou are now editing heads!"));
      player.sendMessage(TextUtil.format("&7Click on &enew ones to add&7."));
      player.sendMessage(TextUtil.format("&7Click on &eexiting ones to remove&7."));
      player.sendMessage(TextUtil.format("&7Run again to stop editing."));
    }
  }
}
