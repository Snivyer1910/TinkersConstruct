package slimeknights.tconstruct.tools.harvest;

import com.google.common.collect.Sets;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import slimeknights.tconstruct.library.tools.ToolDefinition;
import slimeknights.tconstruct.library.tools.helper.ToolAttackUtil;
import slimeknights.tconstruct.library.tools.helper.ToolHarvestLogic;
import slimeknights.tconstruct.library.tools.helper.aoe.CircleAOEHarvestLogic;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.tools.TinkerModifiers;
import slimeknights.tconstruct.tools.TinkerTools;

import java.util.Collections;
import java.util.Set;

public class AxeTool extends HarvestTool {
  private static final Set<Material> EXTRA_MATERIALS = Sets.newHashSet(Material.WOOD, Material.NETHER_WOOD, Material.PLANT, Material.REPLACEABLE_PLANT, Material.BAMBOO, Material.GOURD, Material.LEAVES);
  public static final MaterialHarvestLogic HARVEST_LOGIC = new MaterialHarvestLogic(EXTRA_MATERIALS, 0, 0, 0) {
    @Override
    public Iterable<BlockPos> getAOEBlocks(ToolStack tool, ItemStack stack, PlayerEntity player, BlockState state, World world, BlockPos origin, Direction sideHit, AOEMatchType matchType) {
      if (!canAOE(tool, stack, state, matchType)) {
        return Collections.emptyList();
      }
      // axe uses circular harvest
      return CircleAOEHarvestLogic.calculate(this, tool, stack, world, player, origin, sideHit, 1 + tool.getModifierLevel(TinkerModifiers.expanded), false, matchType);
    }
  };
  public AxeTool(Settings properties, ToolDefinition toolDefinition) {
    super(properties, toolDefinition);
  }

  @Override
  public ToolHarvestLogic getToolHarvestLogic() {
    return HARVEST_LOGIC;
  }

  @Override
  public ActionResult useOnBlock(ItemUsageContext context) {
    return this.getToolHarvestLogic().transformBlocks(context, FabricToolTags.AXES, SoundEvents.ITEM_AXE_STRIP, false);
  }

  @Override
  public boolean dealDamage(ToolStack tool, LivingEntity player, Entity entity, float damage, boolean isCriticalHit, boolean fullyCharged) {
    boolean hit = super.dealDamage(tool, player, entity, damage, isCriticalHit, fullyCharged);
    if (hit && fullyCharged) {
      ToolAttackUtil.spawnAttachParticle(TinkerTools.axeAttackParticle, player, 0.8d);
    }
    return hit;
  }

//  @Override
//  public boolean canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker) {
//    return true;
//  }
}
