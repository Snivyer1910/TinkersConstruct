package slimeknights.tconstruct.library.recipe.tinkerstation.modifier;

import com.google.gson.JsonObject;
import lombok.Getter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;
import net.minecraft.util.Lazy;
import net.minecraft.world.World;
import net.minecraft.recipe.RecipeSerializer;
import slimeknights.mantle.util.JsonHelper;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.recipe.tinkerstation.IMutableTinkerStationInventory;
import slimeknights.tconstruct.library.recipe.tinkerstation.ITinkerStationInventory;
import slimeknights.tconstruct.library.recipe.tinkerstation.ITinkerStationRecipe;
import slimeknights.tconstruct.library.recipe.tinkerstation.ValidatedResult;
import slimeknights.tconstruct.library.tools.ToolDefinition;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.tools.TinkerModifiers;
import slimeknights.tconstruct.tools.modifiers.free.OverslimeModifier;

import org.jetbrains.annotations.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Recipe to add overslime to a tool
 */
public class OverslimeModifierRecipe implements ITinkerStationRecipe, IDisplayModifierRecipe {
  private static final ValidatedResult AT_CAPACITY = ValidatedResult.failure(Util.makeTranslationKey("recipe", "overslime.at_capacity"));

  @Getter
  private final Identifier id;
  private final Ingredient ingredient;
  private final int restoreAmount;

  public OverslimeModifierRecipe(Identifier id, Ingredient ingredient, int restoreAmount) {
    this.id = id;
    this.ingredient = ingredient;
    this.restoreAmount = restoreAmount;
    ModifierRecipeLookup.addIngredient(ingredient);
  }

  @Override
  public boolean matches(ITinkerStationInventory inv, World world) {
    if (!TinkerTags.Items.MODIFIABLE.contains(inv.getTinkerableStack().getItem())) {
      return false;
    }
    // must find at least one slime, but multiple is fine, as is empty slots
    return IncrementalModifierRecipe.containsOnlyIngredient(inv, ingredient);
  }

  @Override
  public ValidatedResult getValidatedResult(ITinkerStationInventory inv) {
    ToolStack tool = ToolStack.from(inv.getTinkerableStack());
    int current = 0;
    int cap = OverslimeModifier.getCap(tool);
    // if the tool lacks true overslime, add overslime
    if (tool.getUpgrades().getLevel(TinkerModifiers.overslime) == 0) {
      // however, if we have overslime though a trait and reached our cap, also do nothing
      if (tool.getModifierLevel(TinkerModifiers.overslime) > 0) {
        current = OverslimeModifier.getOverslime(tool);
        if (current >= cap) {
          return AT_CAPACITY;
        }
      }

      // truely add overslime, this will cost a slime crystal if full durability
      tool = tool.copy();
      tool.addModifier(TinkerModifiers.overslime, 1);
    } else {
      // ensure we are not at the cap already
      current = OverslimeModifier.getOverslime(tool);
      if (current >= cap) {
        return AT_CAPACITY;
      }
      // copy the tool as we will change it later
      tool = tool.copy();
    }

    // see how much value is available, update overslime to the max possible
    int available = IncrementalModifierRecipe.getAvailableAmount(inv, ingredient, restoreAmount);
    OverslimeModifier.setOverslime(tool, Math.min(current + available, cap));
    return ValidatedResult.success(tool.createStack());
  }

  /**
   * Updates the input stacks upon crafting this recipe
   * @param result  Result from {@link #getCraftingResult(ITinkerStationInventory)}. Generally should not be modified
   * @param inv     Inventory instance to modify inputs
   */
  @Override
  public void updateInputs(ItemStack result, IMutableTinkerStationInventory inv) {
    ToolStack tool = ToolStack.from(inv.getTinkerableStack());
    // if the original tool did not have overslime, its treated as having no slime
    int current = 0;
    if (tool.getModifierLevel(TinkerModifiers.overslime) != 0) {
      current = OverslimeModifier.getOverslime(tool);
    }

    // how much did we actually consume?
    int maxNeeded = OverslimeModifier.getOverslime(ToolStack.from(result)) - current;
    IncrementalModifierRecipe.updateInputs(inv, ingredient, maxNeeded, restoreAmount, ItemStack.EMPTY);
  }

  /** @deprecated use {@link #getCraftingResult(ITinkerStationInventory)} */
  @Deprecated
  @Override
  public ItemStack getOutput() {
    return ItemStack.EMPTY;
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return TinkerModifiers.overslimeSerializer;
  }

  /* JEI display */
  /** Cache of modifier result, same for all overslime */
  private static final Lazy<ModifierEntry> RESULT = new Lazy<>(() -> new ModifierEntry(TinkerModifiers.overslime, 1));
  /** Cache of tools for input, same for all overslime */
  private static final Lazy<List<ItemStack>> DISPLAY_TOOLS = new Lazy<>(() -> IDisplayModifierRecipe.getAllModifiable().map(MAP_TOOL_FOR_RENDERING).collect(Collectors.toList()));
  /** Cache of display outputs, value depends on recipe */
  private List<List<ItemStack>> displayItems = null;

  @Override
  public List<List<ItemStack>> getDisplayItems() {
    if (displayItems == null) {
      // set cap and amount based on the restore amount for output
      NbtCompound volatileNBT = new NbtCompound();
      ModDataNBT volatileData = ModDataNBT.readFromNBT(volatileNBT);
      OverslimeModifier.setCap(volatileData, 500);
      NbtCompound persistentNBT = new NbtCompound();
      OverslimeModifier.setOverslime(ToolDefinition.EMPTY, ModDataNBT.readFromNBT(persistentNBT), volatileData, restoreAmount);
      List<ItemStack> displayOutputs = IDisplayModifierRecipe.getAllModifiable()
                                                             .map(MAP_TOOL_FOR_RENDERING)
                                                             .map(stack -> {
                                                               ItemStack result = IDisplayModifierRecipe.withModifiers(stack, null, RESULT.get());
                                                               NbtCompound nbt = result.getOrCreateTag();
                                                               nbt.put(ToolStack.TAG_VOLATILE_MOD_DATA, volatileNBT);
                                                               nbt.put(ToolStack.TAG_PERSISTENT_MOD_DATA, persistentNBT);
                                                               return result;
                                                             })
                                                             .collect(Collectors.toList());
      displayItems = Arrays.asList(
        displayOutputs,
        DISPLAY_TOOLS.get(),
        Arrays.asList(ingredient.getMatchingStacksClient()));
    }
    return displayItems;
  }

  @Override
  public ModifierEntry getDisplayResult() {
    return RESULT.get();
  }

  public static class Serializer implements RecipeSerializer<OverslimeModifierRecipe> {
    @Override
    public OverslimeModifierRecipe read(Identifier id, JsonObject json) {
      Ingredient ingredient = Ingredient.fromJson(JsonHelper.getElement(json, "ingredient"));
      int restoreAmount = net.minecraft.util.JsonHelper.getInt(json, "restore_amount");
      return new OverslimeModifierRecipe(id, ingredient, restoreAmount);
    }

    @Nullable
    @Override
    public OverslimeModifierRecipe read(Identifier id, PacketByteBuf buffer) {
      Ingredient ingredient = Ingredient.fromPacket(buffer);
      int restoreAmount = buffer.readVarInt();
      return new OverslimeModifierRecipe(id, ingredient, restoreAmount);
    }

    @Override
    public void write(PacketByteBuf buffer, OverslimeModifierRecipe recipe) {
      recipe.ingredient.write(buffer);
      buffer.writeVarInt(recipe.restoreAmount);
    }
  }
}
