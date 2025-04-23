package me.mortaldev.jbeaster.modules.dropparty;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import me.mortaldev.jbeaster.testing.*;
import me.mortaldev.jbeaster.utils.ChanceMap;
import me.mortaldev.jbeaster.utils.ItemStackHelper;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

public class DropPartyData {
  @JsonSerialize(using = ItemStackSerializer.class)
  @JsonDeserialize(using = ItemStackDeserializer.class)
  private ItemStack itemStack;

  private final ChanceMap<String> chanceMap;

  @JsonSerialize(contentUsing = LocationSerializer.class)
  @JsonDeserialize(contentUsing = LocationDeserializer.class)
  @JsonProperty("placeableLocations")
  private final HashSet<Location> placeableLocations;

  @JsonCreator
  public DropPartyData(@JsonProperty("chanceMap") ChanceMap<String> chanceMap, @JsonProperty("placeableLocations") HashSet<Location> placeableLocations) {
    this.chanceMap = chanceMap == null ? new ChanceMap<>() : chanceMap;
    this.placeableLocations = placeableLocations == null ? new HashSet<>() : placeableLocations;
  }

  public static DropPartyData getDefault() {
    return new DropPartyData(new ChanceMap<>(), new HashSet<>());
  }

  public void addPlaceableLocation(Location location) {
    placeableLocations.add(location);
    DropPartyCRUD.getInstance().save(this);
  }

  public boolean hasPlaceableLocation(Location location) {
    for (Location placeableLocation : getPlaceableLocations()) {
      if (placeableLocation.equals(location)) {
        return true;
      }
    }
    return false;
  }

  public void removePlaceableLocation(Location location) {
    placeableLocations.remove(location);
    DropPartyCRUD.getInstance().save(this);
  }

  @JsonProperty("placeableLocations")
  public HashSet<Location> getPlaceableLocations() {
    return placeableLocations;
  }

  public void setItemStack(ItemStack itemStack) {
    this.itemStack = itemStack;
    DropPartyCRUD.getInstance().save(this);
  }

  public ItemStack getItemStack() {
    if (itemStack == null) {
      return null;
    }
    return itemStack;
  }

  @JsonIgnore
  public LinkedHashMap<ItemStack, BigDecimal> getTable() {
    LinkedHashMap<ItemStack, BigDecimal> newTable = new LinkedHashMap<>();
    for (Map.Entry<String, BigDecimal> entry : chanceMap.getTable().entrySet()) {
      newTable.put(ItemStackHelper.deserialize(entry.getKey()), entry.getValue());
    }
    return newTable;
  }

  public ChanceMap<String> getChanceMap() {
    return chanceMap;
  }

  public void addItem(ItemStack item) {
    if (item == null || item.getType().isAir() || item.getType() == Material.AIR) {
      return;
    }
    String key = ItemStackHelper.serialize(item); // Serialize to get String key
    chanceMap.put(key, true); // Use String key
    DropPartyCRUD.getInstance().save(this);
  }

  public void removeItem(ItemStack item) {
    // Need the string key to remove
    String key = ItemStackHelper.serialize(item);
    chanceMap.remove(key, true); // Use String key
    DropPartyCRUD.getInstance().save(this);
  }

  public void updateItem(ItemStack item, BigDecimal chance) {
    if (item == null || item.getType().isAir() || item.getType() == Material.AIR) {
      return;
    }
    String key = ItemStackHelper.serialize(item); // Serialize to get String key
    chanceMap.updateKey(key, chance); // Use String key
    DropPartyCRUD.getInstance().save(this);
  }
}
