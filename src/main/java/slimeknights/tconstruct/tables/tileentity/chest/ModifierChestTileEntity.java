package slimeknights.tconstruct.tables.tileentity.chest;

import net.minecraft.item.ItemStack;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.recipe.tinkerstation.modifier.ModifierRecipeLookup;
import slimeknights.tconstruct.tables.TinkerTables;

public class ModifierChestTileEntity extends TinkerChestTileEntity {
  public ModifierChestTileEntity() {
    // max 64 stacks for the modifier chest
    super(TinkerTables.modifierChestTile, Util.makeTranslationKey("gui", "modifier_chest"), 64, 16);
  }

  @Override
  public boolean isValid(int slot, ItemStack itemstack) {
    // no duplicate limit, the limit to 64 stacks handles that
    return ModifierRecipeLookup.isModifier(itemstack.getItem());
  }
}
