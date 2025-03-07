package slimeknights.tconstruct.tools.modifiers.shared;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.TypedActionResult;
import slimeknights.tconstruct.event.LivingEntityDropXpCallback;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

public class ExperiencedModifier extends Modifier {
  public ExperiencedModifier() {
    super(0xe8db49);
    LivingEntityDropXpCallback.EVENT.register(this::onEntityDropXp);
    //TODO: porting
//    MinecraftForge.EVENT_BUS.addListener((Consumer<BreakEvent>)this::beforeBlockBreak);
  }

  /**
   * Boosts the original based on the level
   * @param original  Original amount
   * @param level     Modifier level
   * @return  Boosted XP
   */
  private static int boost(int original, int level) {
    return (int) (original  * (1 + (0.5 * level)));
  }

  /**
   * Used to modify the XP dropped, regular hook is just for canceling
   * @param event  Event
   */
//  private void beforeBlockBreak(BreakEvent event) {
//    ToolStack tool = getHeldTool(event.getPlayer());
//    if (tool != null) {
//      int level = tool.getModifierLevel(this);
//      if (level > 0) {
//        event.setExpToDrop(boost(event.getExpToDrop(), level));
//      }
//    }
//  }

  /**
   * Event handled locally as its pretty specialized
   */
  private TypedActionResult<Integer> onEntityDropXp(LivingEntity entity, DamageSource source, int expToDrop) {
    Entity attackerEntity = source.getAttacker();
    if (!(attackerEntity instanceof LivingEntity)) return TypedActionResult.pass(expToDrop);
    LivingEntity attacker = (LivingEntity) attackerEntity;
    ToolStack tool = getHeldTool(attacker);
    if (tool != null) {
      int level = tool.getModifierLevel(this);
      if (level > 0) {
        expToDrop = boost(expToDrop, level);
        return TypedActionResult.success(expToDrop);
      }
    }
    return TypedActionResult.pass(expToDrop);
  }
}
