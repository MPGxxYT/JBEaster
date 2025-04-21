package me.mortaldev.jbeaster.modules.miningegg;

public enum ChanceRollRarity {
    COMMON("&a&lCommon"),
    RARE("&3&lRare"),
    LEGENDARY("&6&lLegendary");
    
    private final String id;

    ChanceRollRarity(String id) {
      this.id = id;
    }

    public String getId() {
      return id;
    }
  }
