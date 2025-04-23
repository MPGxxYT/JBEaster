package me.mortaldev.jbeaster.modules.bunnyrace;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.*;
import me.mortaldev.jbeaster.configs.MainConfig;
import me.mortaldev.jbeaster.records.Pair;
import org.bukkit.Location;

public class BunnyRaceData {

  private final HashMap<Integer, Pair<Location, Location>> bunnyPoints = new HashMap<>();

  private final HashMap<Integer, Location> bunnyHome;
  private final HashMap<Integer, String> bunnyNames = new HashMap<>();
  private Pair<Location, Location> finishLine = new Pair<>(defaultPoint, defaultPoint);
  private final HashMap<UUID, Double> refundOnLoad = new HashMap<>();
  private Location informationDisplayLocation = defaultPoint;

  @JsonIgnore
  private final List<String> informationDisplay =
      new ArrayList<>() {
        {
          add("");
          add("&6&lNext Race in&f: 15 mins");
          add("");
          add("&6&l1st:&e Full Return in Eggs");
          add("&6&l2nd:&e 1/2 Money Back");
          add("&6&l3rd:&e 1/3 Money Back");
          add("&6&l4th:&e No Money Back");
          add("");
          add("&6&lBETTING");
          add("&e&lMIN &e- &lMAX");
          add("&e10k - 150k");
          add("");
          add("&6&lRETURNS");
          add("&e1 - 16 Eggs");
          add("&7Scaled with the bet size.");
          add("");
        }
      };

  @JsonCreator
  public BunnyRaceData(@JsonProperty("bunnyHome") HashMap<Integer, Location> bunnyHome) {
    this.bunnyHome = bunnyHome == null ? new HashMap<>() : bunnyHome;
  }

  @JsonIgnore
  private static final Location defaultPoint =
      new Location(MainConfig.getInstance().getEggHuntWorld(), 0, 0, 0);


  public void addRefund(UUID uuid, double amount) {
    double previousValue = refundOnLoad.containsKey(uuid) ? refundOnLoad.get(uuid) : 0;
    refundOnLoad.put(uuid, amount + previousValue);
    BunnyRaceDataCRUD.getInstance().save(this);
  }

  public void removeRefund(UUID uuid) {
    refundOnLoad.remove(uuid);
    BunnyRaceDataCRUD.getInstance().save(this);
  }

  public HashMap<Integer, String> getBunnyNames() {
    return bunnyNames;
  }

  @JsonIgnore
  public double getRefundByUUID(UUID uuid) {
    return refundOnLoad.containsKey(uuid) ? refundOnLoad.get(uuid) : 0;
  }

  @JsonProperty("refundOnLoad")
  public HashMap<UUID, Double> getRefunds() {
    return refundOnLoad;
  }

  public List<String> getInformationDisplay() {
    return informationDisplay;
  }

  public void setInformationDisplayLocation(Location informationDisplayLocation) {
    this.informationDisplayLocation = informationDisplayLocation;
    BunnyRaceDataCRUD.getInstance().save(this);
  }

  public Location getInformationDisplayLocation() {
    return informationDisplayLocation;
  }

  @JsonIgnore
  public void setBunnyHome(int index, Location location) {
    bunnyHome.put(index, location);
    BunnyRaceDataCRUD.getInstance().save(this);
  }

  public static Location getDefaultPoint() {
    return defaultPoint;
  }

  public Location getBunnyHome(int index) {
    if (!bunnyHome.containsKey(index)) return defaultPoint;
    return bunnyHome.get(index);
  }

  @JsonProperty("bunnyHome")
  public HashMap<Integer, Location> getBunnyHomes() {
    return this.bunnyHome;
  }

  public void setBunnyName(int index, String name) {
    bunnyNames.put(index, name);
    BunnyRaceDataCRUD.getInstance().save(this);
  }

  @JsonIgnore
  public String getBunnyNameByIndex(int index) {
    if (!bunnyNames.containsKey(index)) return "#" + index;
    return bunnyNames.get(index);
  }

  @JsonIgnore
  public void setFinishCorner1(Location location) {
    Location second = finishLine.second();
    finishLine = new Pair<>(location, second);
    BunnyRaceDataCRUD.getInstance().save(this);
  }

  @JsonIgnore
  public void setFinishCorner2(Location location) {
    Location first = finishLine.first();
    finishLine = new Pair<>(location, first);
    BunnyRaceDataCRUD.getInstance().save(this);
  }

  public Pair<Location, Location> getFinishLine() {
    return finishLine;
  }

  public void setFinishLine(Pair<Location, Location> finishLine) {
    this.finishLine = finishLine;
  }

  public void setPoint(int index, Location start, Location end) {
    bunnyPoints.put(index, new Pair<>(start, end));
    BunnyRaceDataCRUD.getInstance().save(this);
  }

  public void setStart(int index, Location start) {
    Location second = defaultPoint;
    if (bunnyPoints.containsKey(index)) {
      Location currentSecond = bunnyPoints.get(index).second();
      if (currentSecond != null) second = currentSecond;
    }
    bunnyPoints.put(index, new Pair<>(start, second));
    BunnyRaceDataCRUD.getInstance().save(this);
  }

  public void setEnd(int index, Location end) {
    Location first = defaultPoint;
    if (bunnyPoints.containsKey(index)) {
      Location currentFirst = bunnyPoints.get(index).first();
      if (currentFirst != null) first = currentFirst;
    }
    bunnyPoints.put(index, new Pair<>(first, end));
    BunnyRaceDataCRUD.getInstance().save(this);
  }

  public void clearPoint(int index) {
    bunnyPoints.put(index, new Pair<>(defaultPoint, defaultPoint));
    BunnyRaceDataCRUD.getInstance().save(this);
  }

  @JsonIgnore
  public Pair<Location, Location> getPointsByIndex(int index) {
    return bunnyPoints.get(index);
  }

  public HashMap<Integer, Pair<Location, Location>> getBunnyPoints() {
    return bunnyPoints;
  }
}
