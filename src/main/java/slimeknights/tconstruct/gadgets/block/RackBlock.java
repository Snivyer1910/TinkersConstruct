package slimeknights.tconstruct.gadgets.block;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import slimeknights.tconstruct.shared.block.TableBlock;

public class RackBlock extends TableBlock {

  protected RackBlock(Settings builder) {
    super(builder);
  }

//  @NotNull
//  @Override
//  public BlockEntity createTileEntity(BlockState blockState, BlockView iBlockReader) {
//    return null;
//  }

  @Override
  protected boolean openGui(PlayerEntity playerEntity, World world, BlockPos blockPos) {
    return false;
  }
}
