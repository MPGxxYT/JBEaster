package me.mortaldev.jbeaster.testing;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import me.mortaldev.jbeaster.Main;
import me.mortaldev.jbeaster.utils.ItemStackHelper;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;

public class ItemStackSerializer extends JsonSerializer<ItemStack> {
  @Override
  public void serialize(ItemStack itemStack, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
    if (itemStack == null) {
      jsonGenerator.writeNull();
      Main.log("Writing null ItemStack.");
    } else {
      String serialized = ItemStackHelper.serialize(itemStack);
      jsonGenerator.writeString(serialized);
    }
  }
}

