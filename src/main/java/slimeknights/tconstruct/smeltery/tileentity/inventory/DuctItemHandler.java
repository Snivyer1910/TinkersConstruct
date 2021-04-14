package slimeknights.tconstruct.smeltery.tileentity.inventory;

import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.network.TinkerNetwork;
import slimeknights.tconstruct.misc.SingleItemHandler;
import slimeknights.tconstruct.smeltery.tileentity.DuctTileEntity;
import slimeknights.tconstruct.tools.common.network.InventorySlotSyncPacket;

/**
 * Item handler for the duct
 */
public class DuctItemHandler extends SingleItemHandler<DuctTileEntity> {

  public DuctItemHandler(DuctTileEntity parent) {
    super(parent, 1);
  }

  /**
   * Sets the stack in this duct
   * @param newStack  New stack
   */
  @Override
  public void setStack(ItemStack newStack) {
    World world = parent.getWorld();
    boolean hasChange = world != null && !ItemStack.areEqual(getStack(), newStack);
    super.setStack(newStack);
    if (hasChange) {
      if (!world.isClient) {
        BlockPos pos = parent.getPos();
        TinkerNetwork.getInstance().sendToClientsAround(new InventorySlotSyncPacket(newStack, 0, pos), world, pos);
      } else {
        parent.updateFluid();
      }
    }
  }

  @Override
  protected boolean isItemValid(ItemStack stack) {
    if (stack.getItem().isIn(TinkerTags.Items.DUCT_CONTAINERS)) {
      return true;
    }
    
    ItemStack container = ItemStack.EMPTY;
    return !container.isEmpty() && container.getItem().isIn(TinkerTags.Items.DUCT_CONTAINERS);
  }

  /**
   * Gets the fluid filter for this duct
   * @return  Fluid filter
   */
  public Fluid getFluid() {
    ItemStack stack = getStack();
    if (stack.isEmpty()) {
      return Fluids.EMPTY;
    }
    throw new RuntimeException("CRAB!"); // FIXME: PORT
//    return FluidUtil.getFluidHandler(stack)
//                    .map(handler -> handler.getFluidInTank(0).getFluid())
//                    .orElse(Fluids.EMPTY);
  }
}
