package me.mortaldev.jbeaster.listeners.bunnyrace;

import eu.decentsoftware.holograms.event.HologramClickEvent;
import me.mortaldev.jbeaster.Main;
import me.mortaldev.jbeaster.modules.bunnyrace.BunnyRaceController;
import me.mortaldev.jbeaster.modules.bunnyrace.BunnyRaceDataCRUD;
import me.mortaldev.jbeaster.modules.bunnyrace.PlayerBet;
import me.mortaldev.jbeaster.modules.playerdata.PlayerData;
import me.mortaldev.jbeaster.modules.playerdata.PlayerDataManager;
import me.mortaldev.jbeaster.utils.ItemStackHelper;
import me.mortaldev.jbeaster.utils.TextUtil;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BunnyRaceBetEvent implements Listener {

  @EventHandler
  public void onClickOnHologram(HologramClickEvent event) {
    if (!BunnyRaceController.getInstance().isEnabled()) {
      return;
    }
    String hologramName = event.getHologram().getName();
    if (!hologramName.contains("bunnyBet_")) {
      return;
    }
    String bunnyID = hologramName.replaceAll("bunnyBet_", "");
    Player player = event.getPlayer();
    int id = 0;
    try {
      id = Integer.parseInt(bunnyID);
    } catch (NumberFormatException e) {
      player.sendMessage(TextUtil.format("&cFailed to place bet."));
    }
    String bunnyName = BunnyRaceDataCRUD.getInstance().get().getBunnyNameByIndex(id);
    PlayerData playerData =
        PlayerDataManager.getInstance()
            .getByID(player.getUniqueId().toString())
            .orElse(PlayerData.create(player.getUniqueId().toString()));
//    if (!playerData.getRewardOverflow().isEmpty()) {
//      player.sendMessage(TextUtil.format("You cannot place bets while you have rewards to claim."));
//      player.sendMessage(TextUtil.format("Use /easterRewards to claim them."));
//      return;
//    }
    ItemStackHelper.Builder itemLeft = ItemStackHelper.builder(Material.GOLD_NUGGET).emptyName();
    Double previousBet = playerData.getPreviousBet();
    if (previousBet != null) {
      itemLeft.name(BunnyRaceController.getInstance().formatNumber(previousBet, false));
    }
    int finalId = id;
    if (!BunnyRaceController.getInstance().isBetsOpen()) {
      player.sendMessage(TextUtil.format("&cYou cannot bet while bets are closed."));
      return;
    }
    Bukkit.getScheduler()
        .runTask(
            Main.getInstance(),
            () ->
                new AnvilGUI.Builder()
                    .plugin(Main.getInstance())
                    .title("Bet on " + TextUtil.removeDecoration(TextUtil.removeColors(bunnyName)))
                    .itemLeft(itemLeft.build())
                    .onClick(
                        (slot, stateSnapshot) -> {
                          if (slot == 2) {
                            if (!BunnyRaceController.getInstance().isBetsOpen()) {
                              player.sendMessage(TextUtil.format("&cYou cannot bet while bets are closed."));
                              return Collections.emptyList();
                            }
                            String textEntry = stateSnapshot.getText();
                            String pattern = "^\\d*\\.?\\d{0,2}$";
                            Pattern regex = Pattern.compile(pattern);
                            Matcher matcher = regex.matcher(textEntry);
                            if (!matcher.matches() || textEntry.isBlank()) {
                              player.sendMessage(TextUtil.format("&cPlease enter a valid number."));
                              player.closeInventory();
                              return Collections.emptyList();
                            }
                            double amount = Double.parseDouble(textEntry);
                            if (amount < 10000 || amount > 150000) {
                              player.sendMessage(
                                  TextUtil.format("&cBet must be between 10k & 150k."));
                              player.closeInventory();
                              return Collections.emptyList();
                            }
                            if (!(Main.getEconomy().getBalance(player) >= amount)) {
                              player.sendMessage(TextUtil.format("&cYou don't have enough money."));
                              player.closeInventory();
                              return Collections.emptyList();
                            }
                            BunnyRaceController instance = BunnyRaceController.getInstance();
                            if (instance.playerHasBet(player.getUniqueId())) {
                              PlayerBet playerBet = instance.getPlayerBet(player.getUniqueId());
                              if (playerBet.getBunnyID() != finalId) {
                                player.sendMessage(TextUtil.format("&7Previous bet cancelled"));
                                player.sendMessage(
                                    TextUtil.format("&7You can only bet on one bunny at a time."));
                              }
                            }
                            if (previousBet == null || previousBet != amount) {
                              playerData.setPreviousBet(amount);
                              PlayerDataManager.getInstance().update(playerData, true);
                            }
                            instance.playerBet(player.getUniqueId(), amount, finalId);
                            player.sendMessage(
                                TextUtil.format(
                                    "&eBet placed on bunny &f"
                                        + bunnyName
                                        + "&f&e for &f$"
                                        + BunnyRaceController.getInstance().formatNumber(amount)));
                          }
                          player.closeInventory();
                          return Collections.emptyList();
                        })
                    .open(player));
  }
}
