package slimeknights.tconstruct.common.data.tags;

import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.ItemTagsProvider;
import net.minecraft.data.TagsProvider;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag.INamedTag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import slimeknights.mantle.data.MantleTags;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.common.registration.CastItemObject;
import slimeknights.tconstruct.common.registration.MetalItemObject;
import slimeknights.tconstruct.shared.TinkerCommons;
import slimeknights.tconstruct.shared.TinkerMaterials;
import slimeknights.tconstruct.shared.block.SlimeType;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.tools.TinkerToolParts;
import slimeknights.tconstruct.tools.TinkerTools;
import slimeknights.tconstruct.world.TinkerWorld;

import java.util.function.Consumer;

import static slimeknights.tconstruct.common.TinkerTags.Items.AOE;
import static slimeknights.tconstruct.common.TinkerTags.Items.DURABILITY;
import static slimeknights.tconstruct.common.TinkerTags.Items.HARVEST;
import static slimeknights.tconstruct.common.TinkerTags.Items.HARVEST_PRIMARY;
import static slimeknights.tconstruct.common.TinkerTags.Items.MELEE;
import static slimeknights.tconstruct.common.TinkerTags.Items.MELEE_OR_HARVEST;
import static slimeknights.tconstruct.common.TinkerTags.Items.MELEE_PRIMARY;
import static slimeknights.tconstruct.common.TinkerTags.Items.MODIFIABLE;
import static slimeknights.tconstruct.common.TinkerTags.Items.MULTIPART_TOOL;
import static slimeknights.tconstruct.common.TinkerTags.Items.ONE_HANDED;
import static slimeknights.tconstruct.common.TinkerTags.Items.STONE_HARVEST;
import static slimeknights.tconstruct.common.TinkerTags.Items.SWORD;
import static slimeknights.tconstruct.common.TinkerTags.Items.TWO_HANDED;

public class ItemTagProvider extends ItemTagsProvider {

  public ItemTagProvider(DataGenerator generatorIn, BlockTagsProvider blockTagProvider, ExistingFileHelper existingFileHelper) {
    super(generatorIn, blockTagProvider, TConstruct.MOD_ID, existingFileHelper);
  }

  @Override
  protected void registerTags() {
    this.addCommon();
    this.addWorld();
    this.addSmeltery();
    this.addTools();
  }

  private void addCommon() {
    this.getOrCreateBuilder(TinkerTags.Items.TINKERS_GUIDES)
        .add(TinkerCommons.materialsAndYou.get(), TinkerCommons.tinkersGadgetry.get(),
             TinkerCommons.punySmelting.get(), TinkerCommons.mightySmelting.get(),
             TinkerCommons.fantasticFoundry.get(), TinkerCommons.encyclopedia.get());
    this.getOrCreateBuilder(ItemTags.LECTERN_BOOKS).addTag(TinkerTags.Items.TINKERS_GUIDES);
    this.getOrCreateBuilder(TinkerTags.Items.GUIDEBOOKS).addTag(TinkerTags.Items.TINKERS_GUIDES);
    this.getOrCreateBuilder(TinkerTags.Items.BOOKS).addTag(TinkerTags.Items.GUIDEBOOKS);
    this.getOrCreateBuilder(TinkerTags.Items.STRUCTURE_DEBUG).addTag(TinkerTags.Items.TINKERS_GUIDES);

    this.getOrCreateBuilder(Tags.Items.SLIMEBALLS)
        .addTag(TinkerTags.Items.SKY_SLIMEBALL)
        .addTag(TinkerTags.Items.ENDER_SLIMEBALL)
        .addTag(TinkerTags.Items.BLOOD_SLIMEBALL)
        .addTag(TinkerTags.Items.ICHOR_SLIMEBALL);
    this.getOrCreateBuilder(TinkerTags.Items.EARTH_SLIMEBALL).add(Items.SLIME_BALL);
    this.getOrCreateBuilder(TinkerTags.Items.SKY_SLIMEBALL).add(TinkerCommons.slimeball.get(SlimeType.SKY));
    this.getOrCreateBuilder(TinkerTags.Items.ENDER_SLIMEBALL).add(TinkerCommons.slimeball.get(SlimeType.ENDER));
    this.getOrCreateBuilder(TinkerTags.Items.BLOOD_SLIMEBALL).add(TinkerCommons.slimeball.get(SlimeType.BLOOD));
    this.getOrCreateBuilder(TinkerTags.Items.ICHOR_SLIMEBALL).add(TinkerCommons.slimeball.get(SlimeType.ICHOR));

    this.getOrCreateBuilder(Tags.Items.INGOTS).add(TinkerSmeltery.searedBrick.get(), TinkerSmeltery.scorchedBrick.get());
    this.getOrCreateBuilder(TinkerTags.Items.WITHER_BONES).add(TinkerMaterials.necroticBone.get());

    // ores
    addMetalTags(TinkerMaterials.copper);
    addMetalTags(TinkerMaterials.cobalt);
    // tier 3
    addMetalTags(TinkerMaterials.slimesteel);
    addMetalTags(TinkerMaterials.tinkersBronze);
    addMetalTags(TinkerMaterials.roseGold);
    addMetalTags(TinkerMaterials.pigIron);
    // tier 4
    addMetalTags(TinkerMaterials.queensSlime);
    addMetalTags(TinkerMaterials.manyullyn);
    addMetalTags(TinkerMaterials.hepatizon);
    addMetalTags(TinkerMaterials.soulsteel);
    // tier 5
    addMetalTags(TinkerMaterials.knightslime);
    this.copy(Tags.Blocks.STORAGE_BLOCKS, Tags.Items.STORAGE_BLOCKS);

    this.getOrCreateBuilder(TinkerTags.Items.INGOTS_NETHERITE_SCRAP).add(Items.NETHERITE_SCRAP);
    this.getOrCreateBuilder(TinkerTags.Items.NUGGETS_NETHERITE).add(TinkerMaterials.netheriteNugget.get());
    this.getOrCreateBuilder(TinkerTags.Items.NUGGETS_NETHERITE_SCRAP).add(TinkerMaterials.debrisNugget.get());

    // glass
    copy(Tags.Blocks.GLASS_COLORLESS, Tags.Items.GLASS_COLORLESS);
    copy(Tags.Blocks.GLASS_PANES_COLORLESS, Tags.Items.GLASS_PANES_COLORLESS);
    copy(Tags.Blocks.STAINED_GLASS, Tags.Items.STAINED_GLASS);
    copy(Tags.Blocks.STAINED_GLASS_PANES, Tags.Items.STAINED_GLASS_PANES);
    for (DyeColor color : DyeColor.values()) {
      ResourceLocation name = new ResourceLocation("forge", "glass/" + color.getString());
      copy(BlockTags.createOptional(name), ItemTags.createOptional(name));
      name = new ResourceLocation("forge", "glass_panes/" + color.getString());
      copy(BlockTags.createOptional(name), ItemTags.createOptional(name));
    }

    copy(TinkerTags.Blocks.WORKBENCHES, TinkerTags.Items.WORKBENCHES);
    copy(TinkerTags.Blocks.TABLES, TinkerTags.Items.TABLES);
    copy(TinkerTags.Blocks.ANVIL_METAL, TinkerTags.Items.ANVIL_METAL);
  }

  private void addWorld() {
    this.copy(TinkerTags.Blocks.SLIME_BLOCK, TinkerTags.Items.SLIME_BLOCK);
    this.copy(TinkerTags.Blocks.CONGEALED_SLIME, TinkerTags.Items.CONGEALED_SLIME);
    this.copy(TinkerTags.Blocks.SLIMY_LOGS, TinkerTags.Items.SLIMY_LOGS);
    this.copy(TinkerTags.Blocks.SLIMY_PLANKS, TinkerTags.Items.SLIMY_PLANKS);
    this.copy(TinkerTags.Blocks.SLIMY_LEAVES, TinkerTags.Items.SLIMY_LEAVES);
    this.copy(TinkerTags.Blocks.SLIMY_SAPLINGS, TinkerTags.Items.SLIMY_SAPLINGS);
    this.copy(BlockTags.LEAVES, ItemTags.LEAVES);
    this.copy(BlockTags.SAPLINGS, ItemTags.SAPLINGS);

    this.copy(Tags.Blocks.ORES, Tags.Items.ORES);
    this.copy(TinkerTags.Blocks.ORES_COBALT, TinkerTags.Items.ORES_COBALT);
    this.copy(TinkerTags.Blocks.ORES_COPPER, TinkerTags.Items.ORES_COPPER);

    // wood
    this.copy(BlockTags.NON_FLAMMABLE_WOOD, ItemTags.NON_FLAMMABLE_WOOD);
    // planks
    this.copy(BlockTags.PLANKS, ItemTags.PLANKS);
    this.copy(BlockTags.WOODEN_SLABS, ItemTags.WOODEN_SLABS);
    this.copy(BlockTags.WOODEN_STAIRS, ItemTags.WOODEN_STAIRS);
    // logs
    this.copy(TinkerWorld.greenheart.getLogBlockTag(), TinkerWorld.greenheart.getLogItemTag());
    this.copy(TinkerWorld.skyroot.getLogBlockTag(), TinkerWorld.skyroot.getLogItemTag());
    this.copy(TinkerWorld.bloodshroom.getLogBlockTag(), TinkerWorld.bloodshroom.getLogItemTag());
    this.copy(BlockTags.LOGS, ItemTags.LOGS);
    this.copy(BlockTags.LOGS_THAT_BURN, ItemTags.LOGS_THAT_BURN);
    // doors
    this.copy(BlockTags.WOODEN_FENCES, ItemTags.WOODEN_FENCES);
    this.copy(Tags.Blocks.FENCES_WOODEN, Tags.Items.FENCES_WOODEN);
    this.copy(Tags.Blocks.FENCE_GATES_WOODEN, Tags.Items.FENCE_GATES_WOODEN);
    this.copy(BlockTags.WOODEN_DOORS, ItemTags.WOODEN_DOORS);
    this.copy(BlockTags.WOODEN_TRAPDOORS, ItemTags.WOODEN_TRAPDOORS);
    // redstone
    this.copy(BlockTags.WOODEN_BUTTONS, ItemTags.WOODEN_BUTTONS);
    this.copy(BlockTags.WOODEN_PRESSURE_PLATES, ItemTags.WOODEN_PRESSURE_PLATES);
    this.copy(BlockTags.STANDING_SIGNS, ItemTags.SIGNS);
  }


  private void addTools() {
    // stone
    addToolTags(TinkerTools.pickaxe,      MULTIPART_TOOL, DURABILITY, HARVEST_PRIMARY, STONE_HARVEST, MELEE,         ONE_HANDED, AOE);
    addToolTags(TinkerTools.sledgeHammer, MULTIPART_TOOL, DURABILITY, HARVEST_PRIMARY, STONE_HARVEST, MELEE_PRIMARY, TWO_HANDED, AOE);
    addToolTags(TinkerTools.veinHammer,   MULTIPART_TOOL, DURABILITY, HARVEST_PRIMARY, STONE_HARVEST, MELEE,         TWO_HANDED, AOE);
    // dirt
    addToolTags(TinkerTools.mattock,   MULTIPART_TOOL, DURABILITY, HARVEST_PRIMARY, MELEE, ONE_HANDED, AOE);
    addToolTags(TinkerTools.excavator, MULTIPART_TOOL, DURABILITY, HARVEST_PRIMARY, MELEE, TWO_HANDED, AOE);
    // wood
    addToolTags(TinkerTools.handAxe,  MULTIPART_TOOL, DURABILITY, HARVEST_PRIMARY, MELEE_PRIMARY, ONE_HANDED, AOE);
    addToolTags(TinkerTools.broadAxe, MULTIPART_TOOL, DURABILITY, HARVEST_PRIMARY, MELEE_PRIMARY, TWO_HANDED, AOE);
    // plants
    addToolTags(TinkerTools.kama,   MULTIPART_TOOL, DURABILITY, HARVEST_PRIMARY, MELEE,         ONE_HANDED, AOE);
    addToolTags(TinkerTools.scythe, MULTIPART_TOOL, DURABILITY, HARVEST_PRIMARY, MELEE_PRIMARY, TWO_HANDED, AOE);
    // sword
    addToolTags(TinkerTools.dagger,  MULTIPART_TOOL, DURABILITY, HARVEST, MELEE_PRIMARY, ONE_HANDED);
    addToolTags(TinkerTools.sword,   MULTIPART_TOOL, DURABILITY, HARVEST, MELEE_PRIMARY, ONE_HANDED, SWORD, AOE);
    addToolTags(TinkerTools.cleaver, MULTIPART_TOOL, DURABILITY, HARVEST, MELEE_PRIMARY, TWO_HANDED, SWORD, AOE);

    // add tags to other tags
    // harvest primary and stone harvest are both automatically harvest
    this.getOrCreateBuilder(TinkerTags.Items.HARVEST).addTag(HARVEST_PRIMARY).addTag(STONE_HARVEST);
    // melee primary and swords
    this.getOrCreateBuilder(MELEE).addTag(MELEE_PRIMARY).addTag(SWORD);
    // modifier helper tag
    this.getOrCreateBuilder(MELEE_OR_HARVEST).addTag(MELEE).addTag(HARVEST);

    // general
    this.getOrCreateBuilder(MODIFIABLE)
        .addTag(MULTIPART_TOOL).addTag(DURABILITY)
        .addTag(MELEE_OR_HARVEST).addTag(AOE)
        .addTag(ONE_HANDED).addTag(TWO_HANDED);
    this.getOrCreateBuilder(MantleTags.Items.OFFHAND_COOLDOWN).addTag(TinkerTags.Items.MELEE);

    // kamas are a shear type, when broken we don't pass it to loot tables
    this.getOrCreateBuilder(Tags.Items.SHEARS).add(TinkerTools.kama.get());
    // mark kama and scythe for mods like thermal to use
    this.getOrCreateBuilder(TinkerTags.Items.SCYTHES).add(TinkerTools.kama.get(), TinkerTools.scythe.get());
    // nothing to blacklist, just want the empty tag so it appears in datapacks
    this.getOrCreateBuilder(TinkerTags.Items.AUTOSMELT_BLACKLIST);

    // carrots and potatoes are not seeds in vanilla, so make a tag with them
    this.getOrCreateBuilder(TinkerTags.Items.SEEDS)
        .addTag(Tags.Items.SEEDS)
        .add(Items.CARROT, Items.POTATO, Items.NETHER_WART);

    // tag for tool parts, mostly used by JEI right now
    this.getOrCreateBuilder(TinkerTags.Items.TOOL_PARTS)
        .add(TinkerToolParts.pickaxeHead.get(), TinkerToolParts.hammerHead.get(),
						 TinkerToolParts.smallAxeHead.get(), TinkerToolParts.broadAxeHead.get(),
						 TinkerToolParts.smallBlade.get(), TinkerToolParts.broadBlade.get(),
						 TinkerToolParts.toolBinding.get(), TinkerToolParts.largePlate.get(),
						 TinkerToolParts.toolHandle.get(), TinkerToolParts.toughHandle.get(),
						 TinkerToolParts.repairKit.get()); // repair kit is not strictly a tool part, but this list just helps out JEI
  }

  private void addSmeltery() {
    this.copy(TinkerTags.Blocks.SEARED_BRICKS, TinkerTags.Items.SEARED_BRICKS);
    this.copy(TinkerTags.Blocks.SEARED_BLOCKS, TinkerTags.Items.SEARED_BLOCKS);
    this.copy(TinkerTags.Blocks.SCORCHED_BLOCKS, TinkerTags.Items.SCORCHED_BLOCKS);
    this.copy(BlockTags.SOUL_FIRE_BASE_BLOCKS, ItemTags.SOUL_FIRE_BASE_BLOCKS);

    // tag each type of cast
    TagsProvider.Builder<Item> goldCasts = this.getOrCreateBuilder(TinkerTags.Items.GOLD_CASTS);
    TagsProvider.Builder<Item> sandCasts = this.getOrCreateBuilder(TinkerTags.Items.SAND_CASTS);
    TagsProvider.Builder<Item> redSandCasts = this.getOrCreateBuilder(TinkerTags.Items.RED_SAND_CASTS);
    TagsProvider.Builder<Item> singleUseCasts = this.getOrCreateBuilder(TinkerTags.Items.SINGLE_USE_CASTS);
    TagsProvider.Builder<Item> multiUseCasts = this.getOrCreateBuilder(TinkerTags.Items.MULTI_USE_CASTS);
    Consumer<CastItemObject> addCast = cast -> {
      // tag based on material
      goldCasts.add(cast.get());
      sandCasts.add(cast.getSand());
      redSandCasts.add(cast.getRedSand());
      // tag based on usage
      singleUseCasts.addTag(cast.getSingleUseTag());
      this.getOrCreateBuilder(cast.getSingleUseTag()).add(cast.getSand(), cast.getRedSand());
      multiUseCasts.addTag(cast.getMultiUseTag());
      this.getOrCreateBuilder(cast.getMultiUseTag()).add(cast.get());
    };
    // basic
    addCast.accept(TinkerSmeltery.blankCast);
    addCast.accept(TinkerSmeltery.ingotCast);
    addCast.accept(TinkerSmeltery.nuggetCast);
    addCast.accept(TinkerSmeltery.gemCast);
    addCast.accept(TinkerSmeltery.rodCast);
    addCast.accept(TinkerSmeltery.repairKitCast);
    // compatibility
    addCast.accept(TinkerSmeltery.plateCast);
    addCast.accept(TinkerSmeltery.gearCast);
    addCast.accept(TinkerSmeltery.coinCast);
    // small heads
    addCast.accept(TinkerSmeltery.pickaxeHeadCast);
    addCast.accept(TinkerSmeltery.smallAxeHeadCast);
    addCast.accept(TinkerSmeltery.smallBladeCast);
    // large heads
    addCast.accept(TinkerSmeltery.hammerHeadCast);
    addCast.accept(TinkerSmeltery.broadAxeHeadCast);
    addCast.accept(TinkerSmeltery.broadBladeCast);
    // bindings
    addCast.accept(TinkerSmeltery.toolBindingCast);
    addCast.accept(TinkerSmeltery.largePlateCast);
    // tool rods
    addCast.accept(TinkerSmeltery.toolHandleCast);
    addCast.accept(TinkerSmeltery.toughHandleCast);

    // add all casts to a common tag
    this.getOrCreateBuilder(TinkerTags.Items.CASTS)
        .addTag(TinkerTags.Items.GOLD_CASTS)
        .addTag(TinkerTags.Items.SAND_CASTS)
        .addTag(TinkerTags.Items.RED_SAND_CASTS);

    this.getOrCreateBuilder(TinkerTags.Items.DUCT_CONTAINERS).add(Items.BUCKET, TinkerSmeltery.copperCan.get(), TinkerSmeltery.searedLantern.asItem(), TinkerSmeltery.scorchedLantern.asItem());

    // tank tag
    this.copy(TinkerTags.Blocks.SEARED_TANKS, TinkerTags.Items.SEARED_TANKS);
    this.copy(TinkerTags.Blocks.SCORCHED_TANKS, TinkerTags.Items.SCORCHED_TANKS);
    this.getOrCreateBuilder(TinkerTags.Items.TANKS)
        .addTag(TinkerTags.Items.SEARED_TANKS)
        .addTag(TinkerTags.Items.SCORCHED_TANKS);
  }

  @Override
  public String getName() {
    return "Tinkers Construct Item Tags";
  }


  /**
   * Adds relevant tags for a metal object
   * @param metal  Metal object
   */
  private void addMetalTags(MetalItemObject metal) {
    this.getOrCreateBuilder(metal.getIngotTag()).add(metal.getIngot());
    this.getOrCreateBuilder(Tags.Items.INGOTS).addTag(metal.getIngotTag());
    this.getOrCreateBuilder(metal.getNuggetTag()).add(metal.getNugget());
    this.getOrCreateBuilder(Tags.Items.NUGGETS).addTag(metal.getNuggetTag());
    this.copy(metal.getBlockTag(), metal.getBlockItemTag());
  }

  @SafeVarargs
  private final void addToolTags(IItemProvider tool, INamedTag<Item>... tags) {
    Item item = tool.asItem();
    for (INamedTag<Item> tag : tags) {
      this.getOrCreateBuilder(tag).add(item);
    }
  }
}
