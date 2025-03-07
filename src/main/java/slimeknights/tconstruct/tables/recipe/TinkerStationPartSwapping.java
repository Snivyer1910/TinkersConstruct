package slimeknights.tconstruct.tables.recipe;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.stats.IMaterialStats;
import slimeknights.tconstruct.library.materials.stats.IRepairableMaterialStats;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.recipe.casting.material.MaterialCastingLookup;
import slimeknights.tconstruct.library.recipe.material.MaterialRecipe;
import slimeknights.tconstruct.library.recipe.tinkerstation.ITinkerStationInventory;
import slimeknights.tconstruct.library.recipe.tinkerstation.ITinkerStationRecipe;
import slimeknights.tconstruct.library.recipe.tinkerstation.ValidatedResult;
import slimeknights.tconstruct.library.recipe.tinkerstation.modifier.ModifierRecipeLookup;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.part.IToolPart;
import slimeknights.tconstruct.tables.TinkerTables;

import java.util.List;

/**
 * Recipe that replaces a tool part with another
 */
@AllArgsConstructor
public class TinkerStationPartSwapping implements ITinkerStationRecipe {
  private static final ValidatedResult TOO_MANY_PARTS = ValidatedResult.failure(TConstruct.makeTranslationKey("recipe", "part_swapping.too_many_parts"));

  @Getter
  protected final Identifier id;

  @Override
  public boolean matches(ITinkerStationInventory inv, World world) {
    ItemStack tinkerable = inv.getTinkerableStack();
    if (tinkerable.isEmpty() || !(tinkerable.getItem() instanceof IModifiable)) {
      return false;
    }
    // get the list of parts, empty means its not multipart
    List<IToolPart> parts = ((IModifiable)tinkerable.getItem()).getToolDefinition().getRequiredComponents();
    if (parts.isEmpty()) {
      return false;
    }

    // we have two concerns on part swapping:
    // part must be valid in the tool, and only up to one part can be swapped at once
    boolean foundItem = false;
    for (int i = 0; i < inv.getInputCount(); i++) {
      ItemStack stack = inv.getInput(i);
      if (!stack.isEmpty()) {
        // too many items
        if (foundItem) {
          return false;
        }
        // part not in list
        Item item = stack.getItem();
        if (!(item instanceof IToolPart) || !parts.contains(item)) {
          return false;
        }
        foundItem = true;
      }
    }
    return foundItem;
  }

  /** @deprecated Use {@link #getCraftingResult(ITinkerStationInventory)}  */
  @Deprecated
  @Override
  public ItemStack getOutput() {
    return ItemStack.EMPTY;
  }

  @Override
  public ValidatedResult getValidatedResult(ITinkerStationInventory inv) {
    // copy the tool NBT to ensure the original tool is intact
    ToolStack tool = ToolStack.from(inv.getTinkerableStack());
    List<IToolPart> parts = tool.getDefinition().getRequiredComponents();

    // prevent part swapping on large tools in small tables
    if (parts.size() > inv.getInputCount()) {
      return TOO_MANY_PARTS;
    }

    // actual part swap logic
    for (int i = 0; i < inv.getInputCount(); i++) {
      ItemStack stack = inv.getInput(i);
      if (!stack.isEmpty()) {
        // not tool part, should never happen
        Item item = stack.getItem();
        if (!(item instanceof IToolPart)) {
          return ValidatedResult.PASS;
        }

        // ensure the part is valid
        IToolPart part = (IToolPart) item;
        IMaterial partMaterial = ((IToolPart)item).getMaterial(stack);
        if (partMaterial == IMaterial.UNKNOWN) {
          return ValidatedResult.PASS;
        }

        // we have a part and its not at this index, find the first copy of this part
        // means slot only matters if a tool uses a part twice
        int index = i;
        if (i >= parts.size() || parts.get(i).asItem() != item) {
          index = parts.indexOf(item);
          if (index == -1) {
            return ValidatedResult.PASS;
          }
        }

        // ensure there is a change in the part
        IMaterial toolMaterial = tool.getMaterial(index);
        if (toolMaterial == partMaterial) {
          return ValidatedResult.PASS;
        }

        // actual update
        tool = tool.copy();
        tool.replaceMaterial(index, partMaterial);

        // if swapping in a new head, repair the tool (assuming the give stats type can repair)
        // ideally we would validate before repairing, but don't want to create the stack before repairing
        IMaterialStats stats = MaterialRegistry.getInstance().getMaterialStats(partMaterial.getIdentifier(), part.getStatType()).orElse(null);
        if (stats instanceof IRepairableMaterialStats) {
          // must have a registered recipe
          int cost = MaterialCastingLookup.getItemCost(part);
          if (cost > 0) {
            // apply modifier repair boost
            float factor = cost / MaterialRecipe.INGOTS_PER_REPAIR;
            for (ModifierEntry entry : tool.getModifierList()) {
              factor = entry.getModifier().getRepairFactor(tool, entry.getLevel(), factor);
              if (factor <= 0) {
                break;
              }
            }
            if (factor > 0) {
              ToolDamageUtil.repair(tool, (int)(((IRepairableMaterialStats)stats).getDurability() * factor));
            }
          }
        }

        // ensure no modifier problems after removing
        ItemStack result = tool.createStack();
        ValidatedResult toolValidation = ModifierRecipeLookup.checkRequirements(result, tool);
        if (toolValidation.hasError()) {
          return toolValidation;
        }
        toolValidation = ModifierUtil.validateRemovedModifiers(tool, MaterialRegistry.getInstance().getTraits(toolMaterial.getIdentifier(), part.getStatType()));
        if (toolValidation.hasError()) {
          return toolValidation;
        }
        return ValidatedResult.success(result);
      }
    }
    // no item found, should never happen
    return ValidatedResult.PASS;
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return TinkerTables.tinkerStationPartSwappingSerializer;
  }
}
