package me.mortaldev.jbeaster.modules.playerdata;

import me.mortaldev.crudapi.CRUD;
import me.mortaldev.jbeaster.records.Pair;
import me.mortaldev.jbeaster.utils.ItemStackHelper;
import me.mortaldev.jbeaster.utils.Utils;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PlayerData implements CRUD.Identifiable {

  private final String UUID;
  private final Set<String> collectedEggs;
  private final List<String> rewardOverflow;
  private Double previousBet = null;

  public PlayerData(String UUID) {
    this.UUID = UUID;
    this.collectedEggs = new HashSet<>();
    this.rewardOverflow = new ArrayList<>();
  }

  public void addRewardOverflow(ItemStack reward) {
    rewardOverflow.add(ItemStackHelper.serialize(reward));
  }

  public void removeRewardOverflow(ItemStack reward) {
    rewardOverflow.remove(ItemStackHelper.serialize(reward));
  }

  public void removeRewardOverflowString(String reward) {
    rewardOverflow.remove(reward);
  }

  public void setPreviousBet(Double previousBet) {
    this.previousBet = previousBet;
  }

  public Double getPreviousBet() {
    return previousBet;
  }

  public List<ItemStack> getRewardOverflow() {
    List<ItemStack> rewardOverflow = new ArrayList<>();
    for (String reward : this.rewardOverflow) {
      rewardOverflow.add(ItemStackHelper.deserialize(reward));
    }
    return rewardOverflow;
  }

  public List<String> getRewardOverlowStrings() {
    return rewardOverflow;
  }

  public Set<Location> getCollectedEggs() {
    return collectedEggs.stream()
        .map(Utils::locationDeserialize)
        .collect(Collectors.toCollection(HashSet::new));
  }

  public void addCollectedEgg(Location location) {
    collectedEggs.add(Utils.locationSerialize(location));
  }

  public boolean hasCollectedEgg(Location location) {
    return collectedEggs.contains(Utils.locationSerialize(location));
  }

  @Override
  public String getID() {
    return UUID;
  }
}
