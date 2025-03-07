package slimeknights.tconstruct.tables.tileentity.chest;

import net.minecraft.item.ItemStack;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.tinkering.IMaterialItem;
import slimeknights.tconstruct.tables.TinkerTables;

public class PartChestTileEntity extends TinkerChestTileEntity {

  public PartChestTileEntity() {
    // limit of 4 parts per slot
    super(TinkerTables.partChestTile, Util.makeTranslationKey("gui", "part_chest"), DEFAULT_MAX, 16);
  }

  @Override
  public boolean isValid(int slot, ItemStack itemstack) {
    // check if there is no other slot containing that item
    for (int i = 0; i < this.size(); i++) {
      // don't compare count
      if (ItemStack.areItemsEqualIgnoreDamage(itemstack, this.getStack(i))
        && ItemStack.areTagsEqual(itemstack, this.getStack(i))) {
        return i == slot; // only allowed in the same slot
      }
    }
    return itemstack.getItem() instanceof IMaterialItem;
  }
}
