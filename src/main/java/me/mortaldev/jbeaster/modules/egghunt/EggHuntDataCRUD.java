package me.mortaldev.jbeaster.modules.egghunt;

import me.mortaldev.crudapi.CRUDAdapters;
import me.mortaldev.crudapi.SingleCRUD;
import me.mortaldev.crudapi.handlers.GSON;
import me.mortaldev.jbeaster.Main;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class EggHuntDataCRUD extends SingleCRUD<EggHuntData> {
  private static final String PATH = Main.getInstance().getDataFolder() + "/egghunt/";

  private static class Singleton {
    private static final EggHuntDataCRUD INSTANCE = new EggHuntDataCRUD();
  }

  public static EggHuntDataCRUD getInstance() {
    return Singleton.INSTANCE;
  }

  private EggHuntDataCRUD() {
    super(GSON.getInstance());
  }

  @Override
  public String getID() {
    return "data";
  }

  @Override
  public EggHuntData construct() {
    return new EggHuntData();
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
