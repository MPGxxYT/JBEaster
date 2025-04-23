package me.mortaldev.jbeaster.testing;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import me.mortaldev.jbeaster.Main;
import me.mortaldev.jbeaster.utils.ItemStackHelper;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;

public class ItemStackDeserializer extends JsonDeserializer<ItemStack> {
  @Override
  public ItemStack deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
    String serialized = jsonParser.getValueAsString();
    if (serialized == null || serialized.isEmpty()) {
      Main.log("Failed to deserialize ItemStack!");
      return null;
    }
    return ItemStackHelper.deserialize(serialized);
  }
}
