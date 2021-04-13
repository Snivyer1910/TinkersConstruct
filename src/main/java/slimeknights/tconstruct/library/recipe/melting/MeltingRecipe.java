package slimeknights.tconstruct.library.recipe.melting;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import slimeknights.mantle.recipe.RecipeHelper;
import net.minecraft.recipe.RecipeSerializer;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Recipe to melt an ingredient into a specific fuel
 */
@AllArgsConstructor
public class MeltingRecipe implements IMeltingRecipe {
  @Getter
  private final Identifier id;
  @Getter
  private final String group;
  private final Ingredient input;
  @Getter(AccessLevel.PROTECTED)
  private final FluidVolume output;
  @Getter
  private final int temperature;
  /** Number of "steps" needed to melt this, by default lava increases steps by 5 every 4 ticks (25 a second) */
  @Getter
  private final int time;

  @Override
  public boolean matches(IMeltingInventory inv, World world) {
    return input.test(inv.getStack());
  }

  @Override
  public int getTemperature(IMeltingInventory inv) {
    return temperature;
  }

  @Override
  public int getTime(IMeltingInventory inv) {
    return time;
  }

  @Override
  public FluidVolume getOutput(IMeltingInventory inv) {
    return output.copy();
  }

  @Override
  public DefaultedList<Ingredient> getPreviewInputs() {
    return DefaultedList.copyOf(Ingredient.EMPTY, input);
  }

  @Override
  public net.minecraft.recipe.RecipeSerializer<?> getSerializer() {
    return TinkerSmeltery.meltingSerializer;
  }

  /** If true, this recipe is an ore recipe with increased output based on the machine */
  public boolean isOre() {
    return false;
  }

  /** Gets the recipe output for display in JEI */
  public List<List<FluidVolume>> getDisplayOutput() {
    return Collections.singletonList(Collections.singletonList(output));
  }

  /** Interface for use in the serializer */
  @FunctionalInterface
  public interface IFactory<T extends MeltingRecipe> {
    /** Creates a new instance of this recipe */
    T create(Identifier id, String group, Ingredient input, FluidVolume output, int temperature, int time);
  }

  /**
   * Serializer for {@link MeltingRecipe}
   */
  @RequiredArgsConstructor
  public static class Serializer<T extends MeltingRecipe> implements RecipeSerializer<T> {
    private final IFactory<T> factory;

    @Override
    public T read(Identifier id, JsonObject json) {
      String group = JsonHelper.getString(json, "group", "");
      Ingredient input = Ingredient.fromJson(json.get("ingredient"));
      FluidVolume output = FluidVolume.fromJson(JsonHelper.getObject(json, "result"));

      // temperature calculates
      int temperature = JsonHelper.getInt(json, "temperature");
      int time = JsonHelper.getInt(json, "time");
      // validate values
      if (temperature < 0) throw new JsonSyntaxException("Melting temperature must be greater than zero");
      if (time <= 0) throw new JsonSyntaxException("Melting time must be greater than zero");

      return factory.create(id, group, input, output, temperature, time);
    }

    @SneakyThrows
    @Nullable
    @Override
    public T read(Identifier id, PacketByteBuf buffer) {
      try{
        String group = buffer.readString(Short.MAX_VALUE);
        Ingredient input = Ingredient.fromPacket(buffer);
        FluidVolume output = FluidVolume.fromMcBuffer(buffer);
        int temperature = buffer.readInt();
        int time = buffer.readVarInt();
        return factory.create(id, group, input, output, temperature, time);
      }catch (IOException e) {
        throw new RuntimeException("Failed to read fluid volume");
      }
    }

    @Override
    public void write(PacketByteBuf buffer, MeltingRecipe recipe) {
      buffer.writeString(recipe.group);
      recipe.input.write(buffer);
      recipe.output.toMcBuffer(buffer);
      buffer.writeInt(recipe.temperature);
      buffer.writeVarInt(recipe.time);
    }
  }
}
