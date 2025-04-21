package me.mortaldev.jbeaster.menus;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import me.mortaldev.jbeaster.Main;
import me.mortaldev.jbeaster.modules.dropparty.DropPartyCRUD;
import me.mortaldev.jbeaster.modules.dropparty.DropPartyData;
import me.mortaldev.jbeaster.utils.ItemStackHelper;
import me.mortaldev.jbeaster.utils.TextUtil;
import me.mortaldev.menuapi.GUIManager;
import me.mortaldev.menuapi.InventoryButton;
import me.mortaldev.menuapi.InventoryGUI;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class DropPartyLoottableMenu extends InventoryGUI {

  DropPartyData dropPartyData;

  // TODO: Fix related bug
  public DropPartyLoottableMenu(DropPartyData dropPartyData) {
    this.dropPartyData = dropPartyData;
    allowBottomInventoryClick(true);
  }

  @Override
  protected Inventory createInventory() {
    return Bukkit.createInventory(null, 6 * 9, TextUtil.format("&3&lLoot Table"));
  }

  @Override
  public void decorate(Player player) {
    ItemStack whiteGlass =
        ItemStackHelper.builder(Material.WHITE_STAINED_GLASS_PANE).name("").build();
    for (int i = 45; i < 54; i++) {
      this.getInventory().setItem(i, whiteGlass);
    }
    int i = 0;
    for (Map.Entry<ItemStack, BigDecimal> entry : dropPartyData.getTable().entrySet()) {
      addButton(i, ItemButton(entry.getKey(), entry.getValue()));
      i++;
    }
    addButton(49, AddItem());
    addButton(47, RebalanceButton());
    super.decorate(player);
  }

  private InventoryButton ItemButton(ItemStack itemStack, BigDecimal chance) {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(itemStack.clone())
                    .addLore()
                    .addLore("&7Chance: " + chance.toString() + "%")
                    .addLore()
                    .addLore("&7[Left-Click to Change]")
                    .addLore("&7[Right-Click to Remove]")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              if (event.getClick() == ClickType.LEFT) {
                new AnvilGUI.Builder()
                    .plugin(Main.getInstance())
                    .title("Change Chance")
                    .itemLeft(
                        ItemStackHelper.builder(Material.PAPER).name(chance.toString()).build())
                    .onClick(
                        (slot, stateSnapshot) -> {
                          if (slot == 2) {
                            String textEntry = stateSnapshot.getText();
                            if (textEntry.trim().matches("^(\\d+(\\.\\d+)?)$")) {
                              dropPartyData.updateItem(itemStack, new BigDecimal(textEntry));
                              GUIManager.getInstance()
                                  .openGUI(new DropPartyLoottableMenu(dropPartyData), player);
                            }
                          }
                          return Collections.emptyList();
                        })
                    .open(player);
              } else {
                dropPartyData.removeItem(itemStack);
                GUIManager.getInstance().openGUI(new DropPartyLoottableMenu(dropPartyData), player);
              }
            });
  }

  private InventoryButton AddItem() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(Material.BUCKET)
                    .name("&e&lAdd Item")
                    .addLore("&7Click with an item to add.")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              ItemStack itemStack = event.getCursor();
              if (itemStack == null || itemStack.getType().isAir() || itemStack.getType() == Material.AIR) {
                return;
              }
              dropPartyData.addItem(itemStack);
              GUIManager.getInstance().openGUI(new DropPartyLoottableMenu(dropPartyData), player);
            });
  }

  private InventoryButton RebalanceButton() {
    return new InventoryButton()
        .creator(
            player ->
                ItemStackHelper.builder(Material.REDSTONE)
                    .name("&e&lRebalance")
                    .addLore("&7Will rebalance the percents")
                    .addLore("&7to have a sum of 100.")
                    .addLore()
                    .addLore("&eTotal: " + dropPartyData.getChanceMap().getTotal() + "%")
                    .addLore("")
                    .addLore("&7[Click to Rebalance]")
                    .build())
        .consumer(
            event -> {
              Player player = (Player) event.getWhoClicked();
              if (!dropPartyData.getChanceMap().balanceTable()) {
                return;
              }
              DropPartyCRUD.getInstance().save(dropPartyData);
              GUIManager.getInstance().openGUI(new DropPartyLoottableMenu(dropPartyData), player);
            });
  }
}
