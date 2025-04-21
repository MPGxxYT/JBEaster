package me.mortaldev.jbeaster.modules.playerdata;

import me.mortaldev.crudapi.CRUD;
import me.mortaldev.crudapi.CRUDAdapters;
import me.mortaldev.crudapi.handlers.GSON;
import me.mortaldev.crudapi.interfaces.Handler;
import me.mortaldev.jbeaster.Main;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class PlayerDataCRUD extends CRUD<PlayerData> {
  private static final String PATH = Main.getInstance().getDataFolder() + "/playerdata/";

  public PlayerDataCRUD() {
    super(GSON.getInstance());
  }

  private static class Singleton {
    private static final PlayerDataCRUD INSTANCE = new PlayerDataCRUD();
  }

  public static PlayerDataCRUD getInstance() {
    return Singleton.INSTANCE;
  }

  @Override
  public Class<PlayerData> getClazz() {
    return PlayerData.class;
  }

  @Override
  public CRUDAdapters getCRUDAdapters() {
    return new CRUDAdapters();
  }

  @Override
  public String getPath() {
    return PATH;
  }
}