package me.mortaldev.jbeaster.modules.egghunt;

import java.util.ArrayList;
import java.util.HashSet;
import me.mortaldev.crudapi.CRUDAdapters;
import me.mortaldev.crudapi.SingleCRUD;
import me.mortaldev.crudapi.handlers.Jackson;
import me.mortaldev.jbeaster.Main;

public class EggHuntDataCRUD extends SingleCRUD<EggHuntData> {
  private static final String PATH = Main.getInstance().getDataFolder() + "/egghunt/";

  private static class Singleton {
    private static final EggHuntDataCRUD INSTANCE = new EggHuntDataCRUD();
  }

  public static EggHuntDataCRUD getInstance() {
    return Singleton.INSTANCE;
  }

  private EggHuntDataCRUD() {
    super(Jackson.getInstance());
  }

  @Override
  public String getID() {
    return "data";
  }

  @Override
  public EggHuntData construct() {
    return new EggHuntData(new ArrayList<>(), new HashSet<>());
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
  public Class<EggHuntData> getClazz() {
    return EggHuntData.class;
  }
}
