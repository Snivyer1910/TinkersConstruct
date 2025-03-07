package slimeknights.tconstruct.tools.modifiers.traits;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.Direction;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.tools.nbt.IModifierToolStack;

/**
 * Shared logic for jagged and stonebound. Trait boosts attack damage as it lowers mining speed.
 */
public class DamageSpeedTradeModifier extends Modifier {
  private static final String KEY_MINING_BOOST = Util.makeTranslationKey("modifier", "damage_speed_trade.suffix");
  private final float multiplier;

  /**
   * Creates a new instance of
   * @param color       Modifier text color
   * @param multiplier  Multiplier. Positive boosts damage, negative boosts mining speed
   */
  public DamageSpeedTradeModifier(int color, float multiplier) {
    super(color);
    this.multiplier = multiplier;
  }

  /** Gets the multiplier for this modifier at the current durability and level */
  private double getMultiplier(IModifierToolStack tool, int level) {
    return Math.sqrt(tool.getDamage() * level / tool.getDefinition().getBaseStatDefinition().getDurabilityModifier()) * multiplier;
  }

  @Override
  public Text getDisplayName(IModifierToolStack tool, int level) {
    double boost = Math.abs(getMultiplier(tool, level));
    Text name = super.getDisplayName(level);
    if (boost > 0) {
      name = name.shallowCopy().append(new TranslatableText(KEY_MINING_BOOST, Util.dfPercent.format(boost)));
    }
    return name;
  }

  @Override
  public float applyLivingDamage(IModifierToolStack tool, int level, LivingEntity attacker, LivingEntity target, float baseDamage, float damage, boolean isCritical, boolean fullyCharged) {
    return (int)(damage * (1 + getMultiplier(tool, level)));
  }

  @Override
  public void onBreakSpeed(IModifierToolStack tool, int level, PlayerEntity player, Direction sideHit, boolean isEffective, float miningSpeedModifier) {
    throw new RuntimeException("crab!");
    //TODO: PORTING
//    player.setNewSpeed((float)(player.getNewSpeed() * (1 - getMultiplier(tool, level))));
  }
}
