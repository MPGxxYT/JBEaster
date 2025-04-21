package me.mortaldev.jbeaster.modules.egghunt;

import me.mortaldev.jbeaster.utils.ItemStackHelper;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class EggHuntData {

  private final HashSet<Map<String, Object>> locations = new HashSet<>();
  private String currencyItemStack = ItemStackHelper.serialize(new ItemStack(Material.EGG));
  private final List<String> winRewards = new ArrayList<>();

  public void addWinReward(ItemStack reward) {
    winRewards.add(ItemStackHelper.serialize(reward));
    EggHuntDataCRUD.getInstance().save(this);
  }

  public List<ItemStack> getWinRewards() {
    List<ItemStack> winRewards = new ArrayList<>();
    for (String reward : this.winRewards) {
      winRewards.add(ItemStackHelper.deserialize(reward));
    }
    return winRewards;
  }

  public void setWinRewards(List<ItemStack> itemStacks) {
    winRewards.clear();
    for (ItemStack itemStack : itemStacks) {
      winRewards.add(ItemStackHelper.serialize(itemStack));
    }
    EggHuntDataCRUD.getInstance().save(this);
  }

  public void addLocation(Location location) {
    locations.add(location.serialize());
    EggHuntDataCRUD.getInstance().save(this);
  }

  public boolean hasLocation(Location location) {
    return locations.contains(location.serialize());
  }

  public void removeLocation(Location location) {
    locations.remove(location.serialize());
    EggHuntDataCRUD.getInstance().save(this);
  }

  public HashSet<Location> getLocations() {
    HashSet<Location> locations = new HashSet<>();
    for (Map<String, Object> location : this.locations) {
      locations.add(Location.deserialize(location));
    }
    return locations;
  }

  public void setCurrencyItemStack(ItemStack currencyItemStack) {
    this.currencyItemStack = ItemStackHelper.serialize(currencyItemStack);
    EggHuntDataCRUD.getInstance().save(this);
  }

  public ItemStack getCurrencyItemStack() {
    return ItemStackHelper.deserialize(currencyItemStack);
  }
}
