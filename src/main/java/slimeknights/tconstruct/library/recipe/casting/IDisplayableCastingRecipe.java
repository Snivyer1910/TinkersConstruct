package slimeknights.tconstruct.library.recipe.casting;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeType;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;

import java.util.List;

/** Interface for casting recipes that are displayable in JEI */
public interface IDisplayableCastingRecipe {
  /** Recipe type, should be basin or table */
  RecipeType<?> getType();

  /** If true, the recipe has a cast item */
  boolean hasCast();

  /** Gets a list of cast items */
  List<ItemStack> getCastItems();

  /** If true, the cast is consumed */
  boolean isConsumed();

  /** Gets a list of fluid */
  List<FluidVolume> getFluids();

  /** Gets the recipe output */
  ItemStack getOutput();

  /** Recipe cooling time */
  int getCoolingTime();
}
