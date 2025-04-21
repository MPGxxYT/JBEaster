package me.mortaldev.jbeaster.modules.dropparty;

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
  private String itemStack;
  private final ChanceMap<String> chanceMap = new ChanceMap<>() {};
  private final HashSet<Map<String, Object>> placeableLocations = new HashSet<>();

  public void addPlaceableLocation(Location location) {
    placeableLocations.add(location.serialize());
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
    placeableLocations.remove(location.serialize());
    DropPartyCRUD.getInstance().save(this);
  }

  public HashSet<Location> getPlaceableLocations() {
    HashSet<Location> locations = new HashSet<>();
    for (Map<String, Object> location : placeableLocations) {
      locations.add(Location.deserialize(location));
    }
    return locations;
  }

  public void setItemStack(ItemStack itemStack) {
    this.itemStack = ItemStackHelper.serialize(itemStack);
    DropPartyCRUD.getInstance().save(this);
  }

  public ItemStack getItemStack() {
    if (itemStack == null) {
      return null;
    }
    return ItemStackHelper.deserialize(itemStack);
  }

  public LinkedHashMap<ItemStack, BigDecimal> getTable() {
    LinkedHashMap<ItemStack, BigDecimal> table = new LinkedHashMap<>();
    for (Map.Entry<String, BigDecimal> entry : chanceMap.getTable().entrySet()) {
      table.put(ItemStackHelper.deserialize(entry.getKey()), entry.getValue());
    }
    return table;
  }

  public ChanceMap<String> getChanceMap() {
    return chanceMap;
  }

  public void addItem(ItemStack item) {
    if (item.getType().isAir() || item.getType() == Material.AIR) {
      return;
    }
    chanceMap.put(ItemStackHelper.serialize(item), true);
    DropPartyCRUD.getInstance().save(this);
  }

  public void removeItem(ItemStack item) {
    chanceMap.remove(ItemStackHelper.serialize(item), true);
    DropPartyCRUD.getInstance().save(this);
  }

  public void updateItem(ItemStack item, BigDecimal chance) {
    if (item.getType().isAir() || item.getType() == Material.AIR) {
      return;
    }
    chanceMap.updateKey(ItemStackHelper.serialize(item), chance);
    DropPartyCRUD.getInstance().save(this);
  }
}
