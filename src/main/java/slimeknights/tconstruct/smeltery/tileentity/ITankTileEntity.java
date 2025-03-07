package slimeknights.tconstruct.smeltery.tileentity;

import alexiil.mc.lib.attributes.SearchOption;
import alexiil.mc.lib.attributes.SearchOptions;
import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.fluid.FixedFluidInv;
import alexiil.mc.lib.attributes.fluid.FixedFluidInvView;
import alexiil.mc.lib.attributes.fluid.FluidAttributes;
import alexiil.mc.lib.attributes.fluid.FluidExtractable;
import alexiil.mc.lib.attributes.fluid.FluidInsertable;
import alexiil.mc.lib.attributes.fluid.FluidInvUtil;
import alexiil.mc.lib.attributes.fluid.FluidTransferable;
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import slimeknights.tconstruct.fluids.FluidUtil;
import slimeknights.tconstruct.fluids.TinkerFluids;
import slimeknights.tconstruct.library.fluid.FluidTankAnimated;
import slimeknights.tconstruct.library.fluid.IFluidTankUpdater;
import slimeknights.tconstruct.smeltery.network.FluidUpdatePacket;

/**
 * Common logic between the tank and the melter
 */
public interface ITankTileEntity extends IFluidTankUpdater, FluidUpdatePacket.IFluidPacketReceiver, FixedFluidInvView {
  /**
   * Gets the tank in this tile entity
   * @return  Tank
   */
  FluidTankAnimated getTank();

  @Override
  default boolean isFluidValidForTank(int tank, FluidKey fluid) {
    return getTank().isFluidValidForTank(tank, fluid);
  }

  @Override
  default int getTankCount() {
    return getTank().getTankCount();
  }

  @Override
  default FluidVolume getInvFluid(int tank) {
    return getTank().getInvFluid(tank);
  }

  /*
   * Comparator
   */

  /**
   * Gets the comparator strength for the tank
   * @return  Tank comparator strength
   */
  default int comparatorStrength() {
    FluidTankAnimated tank = getTank();
    return tank.getFluidAmount().mul(15).div(tank.getTankCapacity(0)).asInt(1000);
  }

  /**
   * Gets the last comparator strength for this tank
   * @return  Last comparator strength
   */
  int getLastStrength();

  /**
   * Updates the last comparator strength for this tank
   * @param strength  Last comparator strength
   */
  void setLastStrength(int strength);

  @Override
  default void onTankContentsChanged() {
    int newStrength = this.comparatorStrength();
    BlockEntity te = getTE();
    World world = te.getWorld();
    if (newStrength != getLastStrength() && world != null) {
      world.updateNeighborsAlways(te.getPos(), te.getCachedState().getBlock());
      setLastStrength(newStrength);
    }
  }

  /*
   * Fluid tank updater
   */
  @Override
  default void updateFluidTo(FluidVolume fluid) {
    // update tank fluid
    FluidTankAnimated tank = getTank();
    int oldAmount = tank.getFluidAmount().asInt(1000);
    int newAmount = fluid.getAmount();
    tank.setFluid(fluid);

    // update the tank render offset from the change
    tank.setRenderOffset(tank.getRenderOffset() + newAmount - oldAmount);

    // update the block model
    throw new RuntimeException("CRAB!"); // FIXME: PORT
//    DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
//      if (TConfig.CLIENT.tankFluidModel.get()) {
//        // if the amount change is bigger than a single increment, or we changed whether we have a fluid, update the world renderer
//        BlockEntity te = getTE();
//        TankModel.BakedModel model = ModelHelper.getBakedModel(te.getCachedState(), TankModel.BakedModel.class);
//        if (model != null && (Math.abs(newAmount - oldAmount) >= (tank.getCapacity() / model.getFluid().getIncrements()) || (oldAmount == 0) != (newAmount == 0))) {
//          //this.requestModelDataUpdate();
//          MinecraftClient.getInstance().worldRenderer.updateBlock(null, te.getPos(), null, null, 3);
//        }
//      }
//    });
  }

  /*
   * Tile entity methods
   */

  /** @return tile entity world */
  default BlockEntity getTE() {
    return (BlockEntity) this;
  }

  /*
   * Helpers
   */

  /**
   * Attempts to interact with a flilled bucket on a fluid tank. This is unique as it handles fish buckets, which don't expose fluid capabilities
   * @param world    World instance
   * @param pos      Block position
   * @param player   Player
   * @param hand     Hand
   * @param hit      Hit side
   * @param offset   Direction to place fish
   * @return True if using a bucket
   */
  static boolean interactWithBucket(World world, BlockPos pos, PlayerEntity player, Hand hand, Direction hit, Direction offset) {
    ItemStack held = player.getStackInHand(hand);
    if (held.getItem() instanceof BucketItem) {
      BucketItem bucket = (BucketItem) held.getItem();
      Fluid fluid = bucket.fluid;
      if (fluid != Fluids.EMPTY) {
        if (!world.isClient) {
          BlockEntity te = world.getBlockEntity(pos);
          if (te != null) {
            final FixedFluidInv fluidInv = FluidAttributes.FIXED_INV.get(world, pos);
            FluidVolume fluidStack = FluidKeys.get(fluid).withAmount(FluidAmount.BUCKET);
            // must empty the whole bucket
            if (fluidInv.getInsertable().attemptInsertion(fluidStack, Simulation.SIMULATE) == fluidStack) {
              fluidInv.getInsertable().attemptInsertion(fluidStack, Simulation.ACTION);
              bucket.onEmptied(world, held, pos.offset(offset));
              world.playSound(null, pos, SoundEvents.BLOCK_WATER_AMBIENT, SoundCategory.BLOCKS, 1.0F, 1.0F);
              if (!player.isCreative()) {
//                player.setStackInHand(hand, held.getContainerItem());
              }
            }
          }
        }
        return true;
      }
    }
    return false;
  }

  /**
   * Base logic to interact with a tank
   * @param world   World instance
   * @param pos     Tank position
   * @param player  Player instance
   * @param hand    Hand used
   * @param hit     Hit position
   * @return  True if further interactions should be blocked, false otherwise
   */
  static boolean interactWithTank(World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
    // success if the item is a fluid handler, regardless of if fluid moved
      if (!world.isClient()) {
        FluidInsertable insertable = FluidAttributes.INSERTABLE.get(world, pos, SearchOptions.inDirection(hit.getSide()));
        return FluidInvUtil.interactHandWithTank(FluidTransferable.from(insertable), player, hand).asActionResult().isAccepted();
      }
      return true;
    // fall back to buckets for fish buckets
//    return interactWithBucket(world, pos, player, hand, face, face);
  }

  /**
   * Implements logic for {@link net.minecraft.block.Block#getComparatorOutput(BlockState, World, BlockPos)}
   * @param world  World instance
   * @param pos    Block position
   * @return  Comparator power
   */
  static int getComparatorInputOverride(WorldAccess world, BlockPos pos) {
    BlockEntity te = world.getBlockEntity(pos);
    if (!(te instanceof ITankTileEntity)) {
      return 0;
    }
    return ((ITankTileEntity) te).comparatorStrength();
  }
}
