package me.mortaldev.jbeaster.modules.dropparty;

import me.mortaldev.crudapi.CRUDAdapters;
import me.mortaldev.crudapi.SingleCRUD;
import me.mortaldev.crudapi.handlers.Jackson;
import me.mortaldev.jbeaster.Main;

public class DropPartyCRUD extends SingleCRUD<DropPartyData> {

  private static class Singleton {
    private static final DropPartyCRUD INSTANCE = new DropPartyCRUD();
  }

  public static DropPartyCRUD getInstance() {
    return Singleton.INSTANCE;
  }

  private DropPartyCRUD() {
    super(Jackson.getInstance());
  }

  private static final String PATH = Main.getInstance().getDataFolder() + "/dropparty/";

  @Override
  public DropPartyData construct() {
    return DropPartyData.getDefault();
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
  public Class<DropPartyData> getClazz() {
    return DropPartyData.class;
  }

  @Override
  public String getID() {
    return "data";
  }

}
