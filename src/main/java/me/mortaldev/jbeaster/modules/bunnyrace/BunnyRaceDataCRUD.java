package me.mortaldev.jbeaster.modules.bunnyrace;

import me.mortaldev.crudapi.CRUDAdapters;
import me.mortaldev.crudapi.SingleCRUD;
import me.mortaldev.crudapi.handlers.GSON;
import me.mortaldev.jbeaster.Main;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class BunnyRaceDataCRUD extends SingleCRUD<BunnyRaceData> {
  private static final String PATH = Main.getInstance().getDataFolder() + "/bunnyrace/";

  private static class Singleton {
    private static final BunnyRaceDataCRUD INSTANCE = new BunnyRaceDataCRUD();
  }

  public static BunnyRaceDataCRUD getInstance() {
    return Singleton.INSTANCE;
  }

  private BunnyRaceDataCRUD() {
    super(GSON.getInstance());
  }

  @Override
  public BunnyRaceData construct() {
    return new BunnyRaceData();
  }

  @Override
  public String getPath() {
    return PATH;
  }

  @Override
  public CRUDAdapters getCRUDAdapters() {
    return new CRUDAdapters();
  }

  @Override
  public Class<BunnyRaceData> getClazz() {
    return BunnyRaceData.class;
  }

  @Override
  public String getID() {
    return "data";
  }
}
