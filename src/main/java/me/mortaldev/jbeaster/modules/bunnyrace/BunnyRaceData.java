package me.mortaldev.jbeaster.modules.bunnyrace;

import me.mortaldev.jbeaster.configs.MainConfig;
import me.mortaldev.jbeaster.records.Pair;
import org.bukkit.Location;

import java.util.*;

public class BunnyRaceData {

  private final HashMap<Integer, Pair<Map<String, Object>, Map<String, Object>>> bunnyPoints = new HashMap<>();
  private final HashMap<Integer, Map<String, Object>> bunnyHome = new HashMap<>();
  private final HashMap<Integer, String> bunnyNames = new HashMap<>();
  private Pair<Map<String, Object>, Map<String, Object>> finishLine = new Pair<>(defaultPoint, defaultPoint);
  private final HashMap<UUID, Double> refundOnLoad = new HashMap<>();
  private Map<String, Object> informationDisplayLocation = defaultPoint;
  private final transient List<String> informationDisplay =
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
  private static final Map<String, Object> defaultPoint = new Location(MainConfig.getInstance().getEggHuntWorld(), 0, 0, 0).serialize();

  public void addRefund(UUID uuid, double amount) {
    double previousValue = refundOnLoad.containsKey(uuid) ? refundOnLoad.get(uuid) : 0;
    refundOnLoad.put(uuid, amount + previousValue);
    BunnyRaceDataCRUD.getInstance().save(this);
  }

  public void removeRefund(UUID uuid) {
    refundOnLoad.remove(uuid);
    BunnyRaceDataCRUD.getInstance().save(this);
  }

  public double getRefund(UUID uuid) {
    return refundOnLoad.containsKey(uuid) ? refundOnLoad.get(uuid) : 0;
  }

  public HashMap<UUID, Double> getRefunds() {
    return refundOnLoad;
  }

  public List<String> getInformationDisplay() {
    return informationDisplay;
  }

  public void setInformationDisplayLocation(Location informationDisplayLocation) {
    this.informationDisplayLocation = informationDisplayLocation.serialize();
    BunnyRaceDataCRUD.getInstance().save(this);
  }

  public Location getInformationDisplayLocation() {
    return Location.deserialize(informationDisplayLocation);
  }

  public void setBunnyHome(int index, Location location) {
    bunnyHome.put(index, location.serialize());
    BunnyRaceDataCRUD.getInstance().save(this);
  }

  public static Location getDefaultPoint() {
    return Location.deserialize(defaultPoint);
  }

  public Location getBunnyHome(int index) {
    if (!bunnyHome.containsKey(index)) return Location.deserialize(defaultPoint);
    return Location.deserialize(bunnyHome.get(index));
  }

  public HashMap<Integer, Location> getBunnyHomes() {
    HashMap<Integer, Location> bunnyHome = new HashMap<>();
    this.bunnyHome.forEach((index, location) -> bunnyHome.put(index, Location.deserialize(location)));
    return bunnyHome;
  }

  public void setBunnyName(int index, String name) {
    bunnyNames.put(index, name);
    BunnyRaceDataCRUD.getInstance().save(this);
  }

  public String getBunnyName(int index) {
    if (!bunnyNames.containsKey(index)) return "#" + index;
    return bunnyNames.get(index);
  }

  public void setFinishCorner1(Location location) {
    Map<String, Object> second = finishLine.second();
    finishLine = new Pair<>(location.serialize(), second);
    BunnyRaceDataCRUD.getInstance().save(this);
  }

  public void setFinishCorner2(Location location) {
    Map<String, Object> first = finishLine.first();
    finishLine = new Pair<>(location.serialize(), first);
    BunnyRaceDataCRUD.getInstance().save(this);
  }

  public Pair<Location, Location> getFinishLine() {
    return pairToLocationPair(finishLine);
  }

  public void setPoint(int index, Location start, Location end) {
    bunnyPoints.put(index, new Pair<>(start.serialize(), end.serialize()));
    BunnyRaceDataCRUD.getInstance().save(this);
  }

  public void setStart(int index, Location start) {
    Map<String, Object> second = defaultPoint;
    if (bunnyPoints.containsKey(index)) {
      Map<String, Object> currentSecond = bunnyPoints.get(index).second();
      if (currentSecond != null) second = currentSecond;
    }
    bunnyPoints.put(index, new Pair<>(start.serialize(), second));
    BunnyRaceDataCRUD.getInstance().save(this);
  }

  public void setEnd(int index, Location end) {
    Map<String, Object> first = defaultPoint;
    if (bunnyPoints.containsKey(index)) {
      Map<String, Object> currentFirst = bunnyPoints.get(index).first();
      if (currentFirst != null) first = currentFirst;
    }
    bunnyPoints.put(index, new Pair<>(first, end.serialize()));
    BunnyRaceDataCRUD.getInstance().save(this);
  }

  public void clearPoint(int index) {
    bunnyPoints.put(index, new Pair<>(defaultPoint, defaultPoint));
    BunnyRaceDataCRUD.getInstance().save(this);
  }

  public Pair<Location, Location> getPoints(int index) {
    return pairToLocationPair(bunnyPoints.get(index));
  }

  public HashMap<Integer, Pair<Location, Location>> getBunnyPoints() {
    HashMap<Integer, Pair<Location, Location>> bunnyPoints = new HashMap<>();
    this.bunnyPoints.forEach((index, pair) -> bunnyPoints.put(index, pairToLocationPair(pair)));
    return bunnyPoints;
  }

  private Pair<Location, Location> pairToLocationPair(Pair<Map<String, Object>, Map<String, Object>> pair) {
    return new Pair<>(Location.deserialize(pair.first()), Location.deserialize(pair.second()));
  }
}
