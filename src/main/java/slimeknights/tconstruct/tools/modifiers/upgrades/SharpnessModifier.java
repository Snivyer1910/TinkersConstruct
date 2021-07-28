package slimeknights.tconstruct.tools.modifiers.upgrades;

import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableText;
import slimeknights.tconstruct.library.modifiers.IncrementalModifier;
import slimeknights.tconstruct.library.tools.ToolDefinition;
import slimeknights.tconstruct.library.tools.nbt.IModDataReadOnly;
import slimeknights.tconstruct.library.tools.nbt.StatsNBT;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

public class SharpnessModifier extends IncrementalModifier {
  public SharpnessModifier() {
    super(0xEAE5DE);
  }

  @Override
  public Text getDisplayName(int level) {
    // displays special names for levels of sharpness
    if (level <= 5) {
      return new TranslatableText(getTranslationKey() + "." + level)
        .styled(style -> style.withColor(TextColor.fromRgb(getColor())));
    }
    return super.getDisplayName(level);
  }

  @Override
  public void addToolStats(ToolDefinition toolDefinition, StatsNBT baseStats, IModDataReadOnly persistentData, IModDataReadOnly volatileData, int level, ModifierStatsBuilder builder) {
    // vanilla give +1, 1.5, 2, 2.5, 3, but that is stupidly low
    // we instead do +1, 2,  3, 4,   5
    ToolStats.ATTACK_DAMAGE.add(builder, getScaledLevel(persistentData, level));
  }
}
