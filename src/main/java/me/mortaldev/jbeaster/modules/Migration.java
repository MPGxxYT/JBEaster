package me.mortaldev.jbeaster.modules;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.File;
import java.io.IOException;
import java.util.List;
import me.mortaldev.crudapi.handlers.Jackson;
import me.mortaldev.jbeaster.Main;
import me.mortaldev.jbeaster.modules.playerdata.PlayerData;
import me.mortaldev.jbeaster.modules.playerdata.PlayerDataCRUD;
import me.mortaldev.jbeaster.testing.ItemStackDeserializer;
import me.mortaldev.jbeaster.utils.Utils;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public class Migration {
  private static class Singleton {
    private static final Migration INSTANCE = new Migration();
  }

  public static synchronized Migration getInstance() {
    return Singleton.INSTANCE;
  }

  private Migration() {}

  public boolean performPlayerDataLocationMigration() {
    String playerDataPath = PlayerDataCRUD.getInstance().getPath();
    File dataDir = new File(playerDataPath);
    if (!dataDir.exists() || !dataDir.isDirectory()) {
      Main.getInstance().getLogger().severe("Migration Failed: PlayerData directory not found: " + playerDataPath);
      return false;
    }

    ObjectMapper readingMapper = new ObjectMapper(); // For reading raw tree

    // Get the ObjectMapper instance that will be used for WRITING the final PlayerData
    // AND for the intermediate convertValue step.
    ObjectMapper migrationMapper = Jackson.getInstance().getObjectMapper();

    // *** EXPLICITLY REGISTER ItemStackDeserializer for this mapper instance ***
    SimpleModule itemStackModule = new SimpleModule("ItemStackMigrationModule");
    itemStackModule.addDeserializer(ItemStack.class, new ItemStackDeserializer()); // Register your deserializer
    migrationMapper.registerModule(itemStackModule);
    // ***********************************************************************

    TypeReference<List<ItemStack>> listItemStackTypeRef = new TypeReference<>() {};

    int processedCount = 0;
    int errorCount = 0;
    File[] files = dataDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));

    if (files == null || files.length == 0) {
      Main.getInstance().getLogger().info("No PlayerData JSON files found for migration.");
      return true; // Nothing to migrate, success.
    }

    Main.getInstance().getLogger().info("Migrating " + files.length + " PlayerData files...");

    for (File file : files) {
      try {
        JsonNode rootNode = readingMapper.readTree(file);
        String uuid = rootNode.path("UUID").asText(null);
        if (uuid == null) {
          Main.getInstance().getLogger().warning("Skipping file " + file.getName() + ": Missing UUID.");
          errorCount++;
          continue;
        }

        PlayerData newData = PlayerData.create(uuid);

        // Previous Bet
        JsonNode previousBetNode = rootNode.path("previousBet");
        if (previousBetNode.isNumber()) {
          newData.setPreviousBet(previousBetNode.asDouble());
        } else {
          newData.setPreviousBet(null);
        }

        // Reward Overflow - Use the migrationMapper which now knows about ItemStackDeserializer
        JsonNode rewardsNode = rootNode.path("rewardOverflow");
        if (rewardsNode.isArray()) {
          try {
            // THIS CALL WILL NOW WORK because we registered the deserializer above
            List<ItemStack> rewards = migrationMapper.convertValue(rewardsNode, listItemStackTypeRef);
            if (rewards != null) {
              newData.getRewardOverflow().addAll(rewards);
            }
          } catch (Exception e) {
            Main.getInstance().getLogger().warning("Could not convert rewardOverflow for " + uuid + " in " + file.getName() + ". Skipping rewards. Error: " + e.getMessage());
            errorCount++; // Count as error if rewards fail to convert
          }
        }

        // Collected Eggs (Convert Strings to Locations)
        JsonNode eggsNode = rootNode.path("collectedEggs");
        if (eggsNode.isArray()) {
          for (JsonNode eggNode : eggsNode) {
            if (eggNode.isTextual()) {
              String locString = eggNode.asText();
              try {
                Location loc = Utils.locationDeserialize(locString);
                newData.getCollectedEggs().add(loc); // Add Location object
              } catch (Exception e) {
                Main.getInstance().getLogger().warning("Error deserializing location string '" + locString + "' for " + uuid + " in " + file.getName() + ": " + e.getMessage());
                errorCount++;
              }
            } else {
              Main.getInstance().getLogger().warning("Skipping non-textual entry in collectedEggs for " + uuid + " in " + file.getName());
              errorCount++;
            }
          }
        }

        // Write New Data - Use the same migrationMapper. It should also have the necessary
        // serializers (ItemStackSerializer, standard LocationSerializer) configured via
        // the annotations on the updated PlayerData class when writing the object.
        migrationMapper.writerWithDefaultPrettyPrinter().writeValue(file, newData);
        processedCount++;

      } catch (IOException e) {
        Main.getInstance().getLogger().severe("Migration IO Error processing file " + file.getName() + ": " + e.getMessage());
        errorCount++;
      } catch (Exception e) {
        Main.getInstance().getLogger().severe("Unexpected Error processing file " + file.getName() + ": " + e.getMessage());
        e.printStackTrace();
        errorCount++;
      }
    }

    Main.getInstance().getLogger().info("Migration finished. Processed: " + processedCount + ", Errors: " + errorCount);
    return errorCount == 0;
  }
}
