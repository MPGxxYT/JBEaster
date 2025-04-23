package me.mortaldev.jbeaster.modules;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.File;
import java.io.IOException;
import java.util.*;
import me.mortaldev.crudapi.CRUDAdapters;
import me.mortaldev.crudapi.handlers.Jackson;
import me.mortaldev.jbeaster.modules.dropparty.DropPartyData;
import me.mortaldev.jbeaster.testing.ItemStackDeserializer;
import me.mortaldev.jbeaster.testing.ItemStackSerializer;
import me.mortaldev.jbeaster.testing.LocationDeserializer;
import me.mortaldev.jbeaster.testing.LocationSerializer;
import me.mortaldev.jbeaster.utils.ChanceMap;
import me.mortaldev.jbeaster.utils.ItemStackHelper;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public class DropPartyDataConverter {

  // Define input and output file paths
  private static final String OLD_JSON_PATH =
      "plugins/JBEaster/dropparty_old.json";
  private static final String NEW_JSON_PATH =
      "plugins/JBEaster/dropparty/data.json";

  public static void convertOldJsonDropParty() {
    // --- 1. Create and Configure ObjectMapper ---
    ObjectMapper objectMapper = new ObjectMapper();

    // Register Location handlers (like in BunnyRaceDataCRUD)
    SimpleModule locationModule = new SimpleModule("LocationModule");
    locationModule.addSerializer(Location.class, new LocationSerializer());
    locationModule.addDeserializer(Location.class, new LocationDeserializer());
    locationModule.addSerializer(ItemStack.class, new ItemStackSerializer());
    locationModule.addDeserializer(ItemStack.class, new ItemStackDeserializer());
    objectMapper.registerModule(locationModule);

    DropPartyData newDataObject = DropPartyData.getDefault();

    try {
      // --- 2. Read Old JSON as JsonNode Tree ---
      File inputFile = new File(OLD_JSON_PATH);
      if (!inputFile.exists()) {
        System.err.println("Input file not found: " + OLD_JSON_PATH);
        return;
      }
      // IMPORTANT: Assumes the syntax error (escape sequence) in inputFile is already fixed
      // manually!
      JsonNode rootNode = objectMapper.readTree(inputFile);

      // --- 3. Extract Data ---
      String itemStackString =
          rootNode.has("itemStack") ? rootNode.get("itemStack").asText() : null;
      JsonNode locationsNode =
          rootNode.has("placeableLocations") ? rootNode.get("placeableLocations") : null;
      JsonNode chanceMapNode = rootNode.has("chanceMap") ? rootNode.get("chanceMap") : null;

      // --- 4. Transform chanceMap ---
      ChanceMap<String> newChanceMap = new ChanceMap<>();
      if (chanceMapNode != null
          && chanceMapNode.has("table")
          && chanceMapNode.get("table").isObject()) {
        JsonNode tableNode = chanceMapNode.get("table");
        Iterator<Map.Entry<String, JsonNode>> fields = tableNode.fields();
        while (fields.hasNext()) {
          Map.Entry<String, JsonNode> entry = fields.next();
          String key = entry.getKey(); // The Base64 string key
          double value = entry.getValue().asDouble(); // The chance value
          newDataObject.getChanceMap().put(key, value, false);
        }
      } else {
        System.err.println("Could not find 'chanceMap.table' structure in old JSON.");
      }

      // --- 5. Deserialize placeableLocations ---
      List<Location> placeableLocationsList;
      if (locationsNode != null && locationsNode.isArray()) {
        // Use treeToValue which utilizes the registered LocationDeserializer
        try {
          placeableLocationsList =
              objectMapper.treeToValue(locationsNode, new TypeReference<>() {});
          placeableLocationsList.forEach(newDataObject::addPlaceableLocation);
        } catch (IOException e) {
          System.err.println("Error deserializing placeableLocations: " + e.getMessage());
          e.printStackTrace();
        }
      }

      newDataObject.setItemStack(
          ItemStackHelper.deserialize(itemStackString)); // Assuming a setter exists

      System.out.println(
          "newDataObject.getItemStack().getType() = " + newDataObject.getItemStack().getType());
      // --- 7. Write New JSON ---
      File outputFile = new File(NEW_JSON_PATH);
      Jackson.getInstance().saveJsonObject(outputFile, newDataObject, new CRUDAdapters().setModule(locationModule));

      System.out.println("Successfully converted data to: " + NEW_JSON_PATH);

    } catch (IOException e) {
      System.err.println("An error occurred during conversion: " + e.getMessage());
      e.printStackTrace(); // Print the full stack trace
    }
  }
}
