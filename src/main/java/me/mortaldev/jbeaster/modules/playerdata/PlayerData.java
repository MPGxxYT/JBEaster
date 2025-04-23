package me.mortaldev.jbeaster.modules.playerdata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.*;
import me.mortaldev.crudapi.CRUD;
import me.mortaldev.jbeaster.testing.ItemStackDeserializer;
import me.mortaldev.jbeaster.testing.ItemStackSerializer;
import me.mortaldev.jbeaster.testing.LocationDeserializer;
import me.mortaldev.jbeaster.testing.LocationSerializer;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public class PlayerData implements CRUD.Identifiable {

  private final String UUID;

  @JsonSerialize(contentUsing = LocationSerializer.class)
  @JsonDeserialize(contentUsing = LocationDeserializer.class)
  private final Set<Location> collectedEggs;

  @JsonSerialize(contentUsing = ItemStackSerializer.class)
  @JsonDeserialize(contentUsing = ItemStackDeserializer.class)
  private final List<ItemStack> rewardOverflow;

  private Double previousBet = null;

  public PlayerData(@JsonProperty("UUID") String UUID, @JsonProperty("collectedEggs") Set<Location> collectedEggs, @JsonProperty("rewardOverflow") List<ItemStack> rewardOverflow) {
    this.UUID = UUID;
    this.collectedEggs = collectedEggs == null ? new HashSet<>() : collectedEggs;
    this.rewardOverflow = rewardOverflow == null ? new ArrayList<>() : rewardOverflow;
  }

  public static PlayerData create(String UUID) {
    return new PlayerData(UUID, new HashSet<>(), new ArrayList<>());
  }

  public void addRewardOverflow(ItemStack reward) {
    rewardOverflow.add(reward);
  }

  public void removeRewardOverflow(ItemStack reward) {
    rewardOverflow.remove(reward);
  }

  public void setPreviousBet(Double previousBet) {
    this.previousBet = previousBet;
  }

  public Double getPreviousBet() {
    return previousBet;
  }

  public List<ItemStack> getRewardOverflow() {
    return rewardOverflow;
  }

  @JsonIgnore
  public Set<Location> getCollectedEggs() {
    return collectedEggs;
  }

  public void addCollectedEgg(Location location) {
    collectedEggs.add(location);
  }

  public boolean hasCollectedEgg(Location location) {
    return collectedEggs.contains(location);
  }

  @JsonProperty("UUID")
  @Override
  public String getID() {
    return UUID;
  }
}
