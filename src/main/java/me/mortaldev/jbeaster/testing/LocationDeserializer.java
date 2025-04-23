package me.mortaldev.jbeaster.testing;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import me.mortaldev.jbeaster.configs.MainConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class LocationDeserializer extends JsonDeserializer<Location> {

  private String getDefaultWorld() {
    return MainConfig.getInstance().getEggHuntWorld().getName();
  }

  @Override
  public Location deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
    ObjectMapper mapper = (ObjectMapper) jsonParser.getCodec();
    JsonNode node = mapper.readTree(jsonParser);

    if (node == null || !node.isObject()) {
      return null;
    }

    String worldName = node.has("world") ? node.get("world").asText(getDefaultWorld()) : null;
    double x = node.has("x") ? node.get("x").asDouble(0.0) : 0.0;
    double y = node.has("y") ? node.get("y").asDouble(0.0) : 0.0;
    double z = node.has("z") ? node.get("z").asDouble(0.0) : 0.0;
    float yaw = node.has("yaw") ? (float) node.get("yaw").asDouble(0.0) : 0.0f;
    float pitch = node.has("pitch") ? (float) node.get("pitch").asDouble(0.0) : 0.0f;

    if (worldName == null) {
      System.err.println("LocationDeserializer: World name is missing in JSON node: " + node);
      return null;
    }

    World world = Bukkit.getWorld(worldName);
    if (world == null) {
      System.err.println("LocationDeserializer: Bukkit World not found for name: " + worldName);
      return null;
    }

    return new Location(world, x, y, z, yaw, pitch);
  }
}