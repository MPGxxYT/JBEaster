package me.mortaldev.jbeaster.testing;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import me.mortaldev.jbeaster.Main;
import me.mortaldev.jbeaster.configs.MainConfig;
import org.bukkit.Location;

import java.io.IOException;

// Tells Jackson how to WRITE a Location object to JSON
public class LocationSerializer extends JsonSerializer<Location> {

  private String getDefaultWorld() {
    return MainConfig.getInstance().getEggHuntWorld().getName();
  }

  @Override
  public void serialize(
      Location location, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
      throws IOException {
    if (location == null) {
      jsonGenerator.writeNull();
    } else {
      jsonGenerator.writeStartObject();
      if (location.getWorld() != null) {
        jsonGenerator.writeStringField("world", location.getWorld().getName());
      } else {
        jsonGenerator.writeStringField("world", getDefaultWorld());
        Main.log("LocationSerializer: World name is missing in Location object: " + location);
      }
      jsonGenerator.writeNumberField("x", location.getX());
      jsonGenerator.writeNumberField("y", location.getY());
      jsonGenerator.writeNumberField("z", location.getZ());
      jsonGenerator.writeNumberField("yaw", location.getYaw());
      jsonGenerator.writeNumberField("pitch", location.getPitch());
      jsonGenerator.writeEndObject();
    }
  }
}
