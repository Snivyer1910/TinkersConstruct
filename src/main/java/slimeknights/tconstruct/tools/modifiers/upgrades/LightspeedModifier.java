package slimeknights.tconstruct.tools.modifiers.upgrades;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import slimeknights.tconstruct.library.modifiers.IncrementalModifier;
import slimeknights.tconstruct.library.tools.item.ToolCore;
import slimeknights.tconstruct.library.tools.nbt.IModifierToolStack;

public class LightspeedModifier extends IncrementalModifier {
  public LightspeedModifier() {
    super(0xFFBC5E);
  }

  @Override
  public int getPriority() {
    return 125; // run before trait boosts such as dwarven
  }

  @Override
  public void onBreakSpeed(IModifierToolStack tool, int level, PlayerEntity player, Direction sideHit, boolean isEffective, float miningSpeedModifier) {
    if (!isEffective) {
      return;
    }
    throw new RuntimeException("CRAB!"); // FIXME: PORT
    /*BlockPos pos = event.getPos();
    if (pos != null) {
      int light = player.getEntityWorld().getLightFor(LightType.BLOCK, pos.offset(sideHit));
      // bonus is +9 mining speed at light level 15, +3 at light level 10, +1 at light level 5
      float boost = (float)(level * Math.pow(3, (light - 5) / 5f) * tool.getDefinition().getBaseStatDefinition().getMiningSpeedModifier() * miningSpeedModifier);
      throw new RuntimeException("CRAB");
      //event.setNewSpeed(event.getNewSpeed() + boost);
    }*/
  }
}
