package slimeknights.tconstruct.library.tools.item;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.util.TranslationHelper;
import slimeknights.tconstruct.common.config.TConfig;
import slimeknights.tconstruct.library.MaterialRegistry;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.materials.IMaterial;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tinkering.MaterialItem;
import slimeknights.tconstruct.library.tools.IToolPart;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.library.utils.Tags;

import java.util.List;

public class ToolPartItem extends MaterialItem implements IToolPart {

  public final MaterialStatsId materialStatId;

  public ToolPartItem(Settings properties, MaterialStatsId id) {
    super(properties);

    this.materialStatId = id;
  }

  @Override
  public void appendStacks(ItemGroup group, DefaultedList<ItemStack> items) {
    if (this.isIn(group)) {
      if (MaterialRegistry.initialized()) {
        for (IMaterial material : MaterialRegistry.getInstance().getMaterials()) {
          if (this.canUseMaterial(material)) {
            items.add(this.withMaterial(material));
            if (!TConfig.common.listAllPartMaterials) {
              break;
            }
          }
        }
      }
    }
  }

  @Override
  public boolean canUseMaterial(IMaterial material) {
    return MaterialRegistry.getInstance().getMaterialStats(material.getIdentifier(), this.materialStatId).isPresent();
  }

  @Override
  public MaterialStatsId getStatType() {
    return this.materialStatId;
  }

  @Override
  @Environment(EnvType.CLIENT)
  public void appendTooltip(ItemStack stack, @Nullable World worldIn, List<Text> tooltip, TooltipContext flagIn) {
    super.appendTooltip(stack, worldIn, tooltip, flagIn);
    IMaterial material = this.getMaterial(stack);

    // Material traits/info
    boolean shift = Util.isShiftKeyDown();

    if (!this.checkMissingMaterialTooltip(stack, tooltip)) {
      for (ModifierEntry entry : material.getTraits()) {
        tooltip.add(entry.getModifier().getDisplayName(entry.getLevel()));
      }
    }

    // Stats
    if (TConfig.client.extraToolTips) {
      if (!shift) {
        // info tooltip for detailed and component info
        tooltip.add(LiteralText.EMPTY);
        tooltip.add(ToolCore.TOOLTIP_HOLD_SHIFT);
      }
      else {
        tooltip.addAll(this.getTooltipStatsInfo(material));
      }
    }

    tooltip.addAll(this.getAddedByInfo(material));
  }

  public List<Text> getTooltipStatsInfo(IMaterial material) {
    ImmutableList.Builder<Text> builder = ImmutableList.builder();

    MaterialRegistry.getInstance().getMaterialStats(material.getIdentifier(), this.materialStatId).ifPresent((stat) -> {
      List<Text> text = stat.getLocalizedInfo();
      if (!text.isEmpty()) {
        builder.add(new LiteralText(""));
        builder.add(stat.getLocalizedName().formatted(Formatting.WHITE, Formatting.UNDERLINE));
        builder.addAll(stat.getLocalizedInfo());
      }
    });

    return builder.build();
  }

  public List<Text> getAddedByInfo(IMaterial material) {
    ImmutableList.Builder<Text> builder = ImmutableList.builder();

    if (MaterialRegistry.getInstance().getMaterial(material.getIdentifier()) != IMaterial.UNKNOWN) {
      builder.add(new LiteralText(""));
      for (ModContainer modInfo : FabricLoader.getInstance().getAllMods()) {
        if (modInfo.getMetadata().getId().equalsIgnoreCase(material.getIdentifier().getNamespace())) {
          builder.add(new TranslatableText("tooltip.part.material_added_by", modInfo.getMetadata().getName()));
        }
      }
    }

    return builder.build();
  }

  public boolean checkMissingMaterialTooltip(ItemStack stack, List<Text> tooltip) {
    IMaterial material = this.getMaterial(stack);

    if (material == IMaterial.UNKNOWN) {
      NbtCompound tagSafe = TagUtil.getTagSafe(stack);
      String materialId = tagSafe.getString(Tags.PART_MATERIAL);
      if (!materialId.isEmpty()) {
        tooltip.add(new TranslatableText("tooltip.part.missing_material", materialId));
      }
      else {
        tooltip.add(new TranslatableText("tooltip.part.missing_info"));
      }
      return true;
    }
    else if(!MaterialRegistry.getInstance().getMaterialStats(material.getIdentifier(), materialStatId).isPresent()) {
      TranslationHelper.addEachLine(I18n.translate("tooltip.part.missing_stats", material.getTranslationKey(), materialStatId), tooltip);
    }

    return false;
  }
}
