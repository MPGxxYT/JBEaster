package me.mortaldev.jbeaster.modules.egghunt;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import me.mortaldev.jbeaster.testing.ItemStackDeserializer;
import me.mortaldev.jbeaster.testing.ItemStackSerializer;
import me.mortaldev.jbeaster.testing.LocationDeserializer;
import me.mortaldev.jbeaster.testing.LocationSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class EggHuntData {

  @JsonSerialize(contentUsing = LocationSerializer.class)
  @JsonDeserialize(contentUsing = LocationDeserializer.class)
  private final HashSet<Location> locations;
  @JsonSerialize(using = ItemStackSerializer.class)
  @JsonDeserialize(using = ItemStackDeserializer.class)
  private ItemStack currencyItemStack = new ItemStack(Material.GOLD_INGOT);
  @JsonSerialize(contentUsing = ItemStackSerializer.class)
  @JsonDeserialize(contentUsing = ItemStackDeserializer.class)
  private final List<ItemStack> winRewards;

  public EggHuntData(@JsonProperty("winRewards") List<ItemStack> winRewards, @JsonProperty("locations") HashSet<Location> locations) {
    this.winRewards = winRewards == null ? new ArrayList<>() : winRewards;
    this.locations = locations == null ? new HashSet<>() : locations;
  }

  public void addWinReward(ItemStack reward) {
    winRewards.add(reward);
    EggHuntDataCRUD.getInstance().save(this);
  }

  @JsonIgnore
  public List<ItemStack> getWinRewards() {
    return winRewards;
  }

  @JsonIgnore
  public void setWinRewards(List<ItemStack> itemStacks) {
    winRewards.clear();
    winRewards.addAll(itemStacks);
    EggHuntDataCRUD.getInstance().save(this);
  }

  public void addLocation(Location location) {
    locations.add(location);
    EggHuntDataCRUD.getInstance().save(this);
  }

  public boolean hasLocation(Location location) {
    return locations.contains(location);
  }

  public void removeLocation(Location location) {
    locations.remove(location);
    EggHuntDataCRUD.getInstance().save(this);
  }

  public HashSet<Location> getLocations() {
    return locations;
  }

  public void setCurrencyItemStack(ItemStack currencyItemStack) {
    this.currencyItemStack = currencyItemStack;
    EggHuntDataCRUD.getInstance().save(this);
  }

  public ItemStack getCurrencyItemStack() {
    return currencyItemStack;
  }
}
