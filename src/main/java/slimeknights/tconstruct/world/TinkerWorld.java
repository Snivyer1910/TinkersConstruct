package slimeknights.tconstruct.world;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ComposterBlock;
import net.minecraft.block.Material;
import net.minecraft.block.SlimeBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.decorator.Decorator;
import net.minecraft.world.gen.decorator.DepthAverageDecoratorConfig;
import net.minecraft.world.gen.decorator.RangeDecoratorConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.ConfiguredFeatures;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.feature.OreFeatureConfig.Rules;
import org.apache.logging.log4j.Logger;
import slimeknights.mantle.item.BlockTooltipItem;
import slimeknights.mantle.registration.object.EnumObject;
import slimeknights.mantle.registration.object.ItemEnumObject;
import slimeknights.mantle.registration.object.ItemObject;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerModule;
import slimeknights.tconstruct.common.config.TConfig;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.utils.HarvestLevels;
import slimeknights.tconstruct.shared.block.CongealedSlimeBlock;
import slimeknights.tconstruct.shared.block.SlimeType;
import slimeknights.tconstruct.world.block.BloodSlimeBlock;
import slimeknights.tconstruct.world.block.SlimeDirtBlock;
import slimeknights.tconstruct.world.block.SlimeGrassBlock;
import slimeknights.tconstruct.world.block.SlimeGrassBlock.FoliageType;
import slimeknights.tconstruct.world.block.SlimeLeavesBlock;
import slimeknights.tconstruct.world.block.SlimeSaplingBlock;
import slimeknights.tconstruct.world.block.SlimeTallGrassBlock;
import slimeknights.tconstruct.world.block.SlimeVineBlock;
import slimeknights.tconstruct.world.block.StickySlimeBlock;
import slimeknights.tconstruct.world.entity.BlueSlimeEntity;
import slimeknights.tconstruct.world.item.SlimeGrassSeedItem;
import slimeknights.tconstruct.world.worldgen.trees.SlimeTree;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Contains blocks and items relevant to structures and world gen
 */
@SuppressWarnings("unused")
public final class TinkerWorld extends TinkerModule implements ModInitializer {

  /** Tab for anything generated in the world */
  @SuppressWarnings("WeakerAccess")
  public static final ItemGroup TAB_WORLD = FabricItemGroupBuilder.create(new Identifier(TConstruct.modID, "world"))
    .icon(() -> new ItemStack(TinkerWorld.cobaltOre))
    .build();
  static final Logger log = Util.getLogger("tinker_world");

//  public static final PlantType SLIME_PLANT_TYPE = PlantType.get("slime");

  /*
   * Block base properties
   */
  private static final Item.Settings WORLD_PROPS = new Item.Settings().group(TAB_WORLD);
  private static final Function<Block, ? extends BlockItem> DEFAULT_BLOCK_ITEM = (b) -> new BlockItem(b, WORLD_PROPS);
  private static final Function<Block, ? extends BlockItem> TOOLTIP_BLOCK_ITEM = (b) -> new BlockTooltipItem(b, WORLD_PROPS);

  /*
   * Blocks
   */
  // ores
  private static final Block.Settings NETHER_ORE = builder(Material.STONE, FabricToolTags.PICKAXES, BlockSoundGroup.NETHER_ORE).requiresTool().breakByTool(FabricToolTags.PICKAXES, HarvestLevels.DIAMOND).strength(10.0F).nonOpaque();
  public static final ItemObject<Block> cobaltOre = BLOCKS.register("cobalt_ore", () -> new Block(NETHER_ORE), DEFAULT_BLOCK_ITEM);

  private static final Block.Settings OVERWORLD_ORE = builder(Material.STONE, FabricToolTags.PICKAXES, BlockSoundGroup.STONE).requiresTool().breakByTool(FabricToolTags.PICKAXES, HarvestLevels.STONE).strength(3.0F, 3.0F);
  public static final ItemObject<Block> copperOre = BLOCKS.register("copper_ore", OVERWORLD_ORE, DEFAULT_BLOCK_ITEM);

  // slime
  private static final Block.Settings SLIME = Block.Settings.copy(Blocks.SLIME_BLOCK);
  public static final EnumObject<SlimeType, SlimeBlock> slime = new EnumObject.Builder<SlimeType, SlimeBlock>(SlimeType.class)
    .putDelegate(SlimeType.EARTH, Blocks.SLIME_BLOCK)
    // sky slime: sticks to anything, but will not pull back
    .put(SlimeType.SKY, () -> (SlimeBlock) BLOCKS.register("sky_slime", new StickySlimeBlock(SLIME, (state, other) -> true), TOOLTIP_BLOCK_ITEM))
    // ichor: does not stick to self, but sticks to anything else
    .put(SlimeType.ICHOR, () -> (SlimeBlock) BLOCKS.register("ichor_slime", new StickySlimeBlock(SLIME, (state, other) -> other.getBlock() != state.getBlock()), TOOLTIP_BLOCK_ITEM))
    // ender: only sticks to self
    .put(SlimeType.ENDER, () -> (SlimeBlock) BLOCKS.register("ender_slime", new StickySlimeBlock(SLIME, (state, other) -> other.getBlock() == state.getBlock()), TOOLTIP_BLOCK_ITEM))
    // blood slime: not sticky, and honey won't stick to it, good for bounce pads
    .put(SlimeType.BLOOD, () -> (SlimeBlock) BLOCKS.register("blood_slime", new BloodSlimeBlock(SLIME), TOOLTIP_BLOCK_ITEM))
    .build();
  private static final AbstractBlock.Settings CONGEALED_SLIME = builder(Material.ORGANIC_PRODUCT, NO_TOOL, BlockSoundGroup.SLIME).strength(0.5F).slipperiness(0.5F);
  public static final EnumObject<SlimeType, Block> congealedSlime = BLOCKS.registerEnum(SlimeType.values(), "congealed_slime", (type) -> new CongealedSlimeBlock(CONGEALED_SLIME), TOOLTIP_BLOCK_ITEM);

  // island blocks
  private static final Block.Settings SLIME_DIRT = builder(Material.SOIL, FabricToolTags.SHOVELS, BlockSoundGroup.SLIME).strength(0.55F);
  private static final Block.Settings SLIME_GRASS = builder(Material.SOLID_ORGANIC, FabricToolTags.SHOVELS, BlockSoundGroup.SLIME).strength(0.65F).ticksRandomly();
  public static final EnumObject<SlimeType, Block> slimeDirt = BLOCKS.registerEnum(SlimeType.TRUE_SLIME, "slime_dirt", (type) -> new SlimeDirtBlock(SLIME_DIRT), TOOLTIP_BLOCK_ITEM);
  public static final EnumObject<SlimeType, Block> allDirt = new EnumObject.Builder<>(SlimeType.class).put(SlimeType.BLOOD, () -> Blocks.DIRT).putAll(slimeDirt).build();
  public static final EnumObject<FoliageType, Block> vanillaSlimeGrass = BLOCKS.registerEnum(FoliageType.values(), "vanilla_slime_grass", (type) -> new SlimeGrassBlock(SLIME_GRASS, type), TOOLTIP_BLOCK_ITEM);
  public static final EnumObject<FoliageType, Block> earthSlimeGrass = BLOCKS.registerEnum(FoliageType.values(), "earth_slime_grass", (type) -> new SlimeGrassBlock(SLIME_GRASS, type), TOOLTIP_BLOCK_ITEM);
  public static final EnumObject<FoliageType, Block> skySlimeGrass = BLOCKS.registerEnum(FoliageType.values(), "sky_slime_grass", (type) -> new SlimeGrassBlock(SLIME_GRASS, type), TOOLTIP_BLOCK_ITEM);
  public static final EnumObject<FoliageType, Block> enderSlimeGrass = BLOCKS.registerEnum(FoliageType.values(), "ender_slime_grass", (type) -> new SlimeGrassBlock(SLIME_GRASS, type), TOOLTIP_BLOCK_ITEM);
  public static final EnumObject<FoliageType, Block> ichorSlimeGrass = BLOCKS.registerEnum(FoliageType.values(), "ichor_slime_grass", (type) -> new SlimeGrassBlock(SLIME_GRASS, type), TOOLTIP_BLOCK_ITEM);
  public static final Map<SlimeType, EnumObject<FoliageType, Block>> slimeGrass;
  static {
    slimeGrass = new EnumMap<SlimeType, EnumObject<FoliageType, Block>>(SlimeType.class);
    slimeGrass.put(SlimeType.BLOOD, vanillaSlimeGrass); // not exact match, but whatever
    slimeGrass.put(SlimeType.EARTH, earthSlimeGrass);
    slimeGrass.put(SlimeType.SKY, skySlimeGrass);
    slimeGrass.put(SlimeType.ENDER, enderSlimeGrass);
    slimeGrass.put(SlimeType.ICHOR, ichorSlimeGrass);
  }
  public static final ItemEnumObject<FoliageType, SlimeGrassSeedItem> slimeGrassSeeds = ITEMS.registerEnum(FoliageType.values(), "slime_grass_seeds", type -> new SlimeGrassSeedItem(WORLD_PROPS, type));

  // plants
  private static final Block.Settings GRASS = builder(Material.PLANT, NO_TOOL, BlockSoundGroup.GRASS).breakInstantly().noCollision().ticksRandomly();
  public static final EnumObject<FoliageType, Block> slimeFern = BLOCKS.registerEnum(FoliageType.values(), "slime_fern", (type) -> new SlimeTallGrassBlock(GRASS, type, SlimeTallGrassBlock.SlimePlantType.FERN), DEFAULT_BLOCK_ITEM);
  public static final EnumObject<FoliageType, Block> slimeTallGrass = BLOCKS.registerEnum(FoliageType.values(), "slime_tall_grass", (type) -> new SlimeTallGrassBlock(GRASS, type, SlimeTallGrassBlock.SlimePlantType.TALL_GRASS), DEFAULT_BLOCK_ITEM);

  // trees
  private static final Block.Settings SAPLING = builder(Material.PLANT, NO_TOOL, BlockSoundGroup.GRASS).breakInstantly().noCollision().ticksRandomly();
  public static final EnumObject<FoliageType, Block> slimeSapling = BLOCKS.registerEnum(FoliageType.values(), "slime_sapling", (type) -> new SlimeSaplingBlock(new SlimeTree(type), type, SAPLING), TOOLTIP_BLOCK_ITEM);
  private static final Block.Settings SLIME_LEAVES = builder(Material.LEAVES, NO_TOOL, BlockSoundGroup.GRASS).strength(0.3F).ticksRandomly().nonOpaque().allowsSpawning((s, w, p, e) -> false);
  public static final EnumObject<FoliageType, Block> slimeLeaves = BLOCKS.registerEnum(FoliageType.values(), "slime_leaves", (type) -> new SlimeLeavesBlock(SLIME_LEAVES, type), DEFAULT_BLOCK_ITEM);

  // slime vines
  private static final Block.Settings VINE = builder(Material.REPLACEABLE_PLANT, NO_TOOL, BlockSoundGroup.GRASS).strength(0.3F).noCollision().ticksRandomly();
  public static final ItemObject<Block> enderSlimeVine = BLOCKS.register("ender_slime_vine", () -> new SlimeVineBlock(VINE, FoliageType.ENDER), DEFAULT_BLOCK_ITEM);
  public static final ItemObject<Block> skySlimeVine = BLOCKS.register("sky_slime_vine", () -> new SlimeVineBlock(VINE, FoliageType.SKY), DEFAULT_BLOCK_ITEM);

  /*
   * Entities
   */
  public static final EntityType<BlueSlimeEntity> skySlimeEntity = ENTITIES.registerWithEgg("sky_slime", () -> {
    return EntityType.Builder.create(BlueSlimeEntity::new, SpawnGroup.MONSTER);
//      .setShouldReceiveVelocityUpdates(true)
//      .setUpdateInterval(5)
//      .setTrackingRange(64)
//      .setDimensions(2.04F, 2.04F)
//      .setCustomClientFactory((spawnEntity, world) -> TinkerWorld.skySlimeEntity.create(world));
  }, 0x47eff5, 0xacfff4);

  /*
   * Particles
   */
  public static final DefaultParticleType slimeParticle = Registry.register(Registry.PARTICLE_TYPE, id("slime"), FabricParticleTypes.simple());

  /*
   * Features
   */
  public static ConfiguredFeature<?, ?> COPPER_ORE_FEATURE;
  public static RegistryKey<ConfiguredFeature<?, ?>> COPPER_ORE_FEATURE_KEY = RegistryKey.of(Registry.CONFIGURED_FEATURE_WORLDGEN, location("copper_ore"));
  public static ConfiguredFeature<?, ?> COBALT_ORE_FEATURE_SMALL;
  public static RegistryKey<ConfiguredFeature<?, ?>> COBALT_ORE_FEATURE_SMALL_KEY = RegistryKey.of(Registry.CONFIGURED_FEATURE_WORLDGEN, location("cobalt_ore_small"));
  public static ConfiguredFeature<?, ?> COBALT_ORE_FEATURE_LARGE;
  public static RegistryKey<ConfiguredFeature<?, ?>> COBALT_ORE_FEATURE_LARGE_KEY = RegistryKey.of(Registry.CONFIGURED_FEATURE_WORLDGEN, location("cobalt_ore_large"));

  @Override
  public void onInitialize() {
    FabricDefaultAttributeRegistry.register(skySlimeEntity, HostileEntity.createHostileAttributes());
    SpawnRestriction.register(skySlimeEntity, SpawnRestriction.Location.ON_GROUND, Heightmap.Type.WORLD_SURFACE, BlueSlimeEntity::canSpawnHere);

    // compostables

    slimeLeaves.forEach(block -> ComposterBlock.registerCompostableItem(0.35f, block));
    slimeSapling.forEach(block -> ComposterBlock.registerCompostableItem(0.35f, block));
    slimeTallGrass.forEach(block -> ComposterBlock.registerCompostableItem(0.35f, block));
    slimeFern.forEach(block -> ComposterBlock.registerCompostableItem(0.65f, block));
    slimeGrassSeeds.forEach(block -> ComposterBlock.registerCompostableItem(0.35F, block));
    ComposterBlock.registerCompostableItem(0.5f, skySlimeVine);
    ComposterBlock.registerCompostableItem(0.5f, enderSlimeVine);

    // ores
    COPPER_ORE_FEATURE = Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, location("copper_ore"),
      Feature.ORE.configure(new OreFeatureConfig(Rules.BASE_STONE_OVERWORLD, TinkerWorld.copperOre.get().getDefaultState(), 9))
        .decorate(Decorator.RANGE.configure(new RangeDecoratorConfig(40, 0, 60)))
        .spreadHorizontally()
        .repeat(TConfig.common.veinCountCopper));
    // small veins, standard distribution
    COBALT_ORE_FEATURE_SMALL = Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, location("cobalt_ore_small"),
      Feature.ORE.configure(new OreFeatureConfig(Rules.NETHERRACK, cobaltOre.get().getDefaultState(), 4))
        .decorate(ConfiguredFeatures.Decorators.NETHER_ORE)
        .spreadHorizontally().repeat(TConfig.common.veinCountCobalt / 2));
    // large veins, around y=16, up to 48
    COBALT_ORE_FEATURE_LARGE = Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, location("cobalt_ore_large"),
      Feature.ORE.configure(new OreFeatureConfig(Rules.NETHERRACK, cobaltOre.get().getDefaultState(), 8))
        .decorate(Decorator.DEPTH_AVERAGE.configure(new DepthAverageDecoratorConfig(32, 16)))
        .spreadHorizontally().repeat(TConfig.common.veinCountCobalt / 2));
  }
}
