package slimeknights.tconstruct.library.recipe.casting;

import lombok.Data;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeType;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;

import java.util.List;

/**
 * Simple implementation of a display casting recipe, generated by certain recipe types
 */
@Data
public class DisplayCastingRecipe implements IDisplayableCastingRecipe {
  private final RecipeType<?> type;
  private final List<ItemStack> castItems;
  private final List<FluidVolume> fluids;
  private final ItemStack output;
  private final int coolingTime;
  private final boolean consumed;

  @Override
  public boolean hasCast() {
    return !castItems.isEmpty();
  }
}
