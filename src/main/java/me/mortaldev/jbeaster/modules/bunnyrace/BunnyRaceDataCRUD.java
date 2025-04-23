package me.mortaldev.jbeaster.modules.bunnyrace;

import com.fasterxml.jackson.databind.module.SimpleModule;
import me.mortaldev.crudapi.CRUDAdapters;
import me.mortaldev.crudapi.SingleCRUD;
import me.mortaldev.crudapi.handlers.Jackson;
import me.mortaldev.jbeaster.Main;
import me.mortaldev.jbeaster.testing.LocationDeserializer;
import me.mortaldev.jbeaster.testing.LocationSerializer;
import org.bukkit.Location;

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
    super(Jackson.getInstance());
  }

  @Override
  public BunnyRaceData construct() {
    return new BunnyRaceData(new HashMap<>());
  }

  @Override
  public String getPath() {
    return PATH;
  }

  @Override
  public CRUDAdapters getCRUDAdapters() {
    CRUDAdapters crudAdapters = new CRUDAdapters();
    SimpleModule simpleModule = new SimpleModule();
    simpleModule.addSerializer(Location.class, new LocationSerializer());
    simpleModule.addDeserializer(Location.class, new LocationDeserializer());
    crudAdapters.setModule(simpleModule);
    return crudAdapters;
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
