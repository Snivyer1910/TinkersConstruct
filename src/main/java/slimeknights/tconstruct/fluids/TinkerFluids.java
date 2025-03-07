package slimeknights.tconstruct.fluids;

import alexiil.mc.lib.attributes.fluid.FluidAttributes;
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import alexiil.mc.lib.attributes.fluid.volume.FluidTemperature;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import alexiil.mc.lib.attributes.fluid.volume.SimpleFluidKey;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.Material;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import net.fabricmc.fabric.api.registry.FuelRegistry;
import org.apache.logging.log4j.Logger;
import slimeknights.mantle.registration.object.FluidObject;
import slimeknights.mantle.registration.object.MantleFluid;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerModule;
import slimeknights.tconstruct.fluids.fluids.SlimeFluid;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.shared.block.SlimeType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Contains all fluids used throughout the mod
 */
public final class TinkerFluids extends TinkerModule {

  public static final int BUCKET_VOLUME = 1000;
  static final Logger log = Util.getLogger("tinker_fluids");

  public static final FluidVolume EMPTY = FluidKeys.EMPTY.withAmount(FluidAmount.ZERO);

  // basic
  public static final FluidObject<MantleFluid> blood = registerFluid("blood", coolBuilder().setRenderColor(0xff540000).setDensity(f(1200)).setViscosity(f(1200)).setTemperature(tmp(336)), Material.WATER, 0);

  // slime -  note second name parameter is forge tag name
  public static final FluidObject<MantleFluid> earthSlime = registerComplexFluid("earth_slime", coolBuilder().setRenderColor(0xef76be6d).setDensity(f(1400)).setViscosity(f(1400)).setTemperature(tmp(350)), new SlimeFluid.Source(), new SlimeFluid.Flowing(), Material.WATER, 0);
  public static final FluidObject<MantleFluid> skySlime = registerComplexFluid("sky_slime", coolBuilder().setRenderColor(0xef67f0f5).setDensity(f(1500)).setViscosity(f(1500)).setTemperature(tmp(310)), new SlimeFluid.Source(), new SlimeFluid.Flowing(), Material.WATER, 0);
  public static final FluidObject<MantleFluid> enderSlime = registerComplexFluid("ender_slime", coolBuilder().setRenderColor(0xefd236ff).setDensity(f(1600)).setViscosity(f(1600)).setTemperature(tmp(370)), new SlimeFluid.Source(), new SlimeFluid.Flowing(), Material.WATER, 0);

  public static final FluidObject<MantleFluid> magmaCream  = registerComplexFluid("magma_cream", hotBuilder(FluidIcons.MAGMA_CREAM_STILL, FluidIcons.MAGMA_CREAM_FLOWING).setDensity(f(1900)).setViscosity(f(1900)).setTemperature(tmp(600)), new SlimeFluid.Source(), new SlimeFluid.Flowing(), Material.WATER, 4);
  public static final Map<SlimeType, FluidObject<MantleFluid>> slime;
  static {
    slime = new EnumMap<>(SlimeType.class);
    slime.put(SlimeType.EARTH, earthSlime);
    slime.put(SlimeType.SKY, skySlime);
    slime.put(SlimeType.ENDER, enderSlime);
    slime.put(SlimeType.BLOOD, blood);
  }

  // molten
  public static final FluidObject<MantleFluid> searedStone    = registerFluid("seared_stone",     stoneBuilder().setRenderColor(0xff777777).setTemperature(tmp(900)), Material.LAVA,  7);
  public static final FluidObject<MantleFluid> moltenClay     = registerFluid("molten_clay",      stoneBuilder().setRenderColor(0xffb75a40).setTemperature(tmp( 750)), Material.LAVA,  3);
  public static final FluidObject<MantleFluid> moltenGlass    = registerFluid("molten_glass",    moltenBuilder().setRenderColor(0xffc0f5fe).setTemperature(tmp(1050)), Material.LAVA, 10);
  public static final FluidObject<MantleFluid> liquidSoul     = registerFluid("liquid_soul",       coolBuilder().setRenderColor(0xffb78e77).setTemperature(tmp( 700)), Material.LAVA,  2);
  public static final FluidObject<MantleFluid> moltenObsidian = registerFluid("molten_obsidian",  stoneBuilder().setRenderColor(0xff2c0d59).setTemperature(tmp(1300)), Material.LAVA, 11);
  public static final FluidObject<MantleFluid> moltenEmerald  = registerFluid("molten_emerald",  moltenBuilder().setRenderColor(0xff41f384).setTemperature(tmp(1234)), Material.LAVA,  4);
  public static final FluidObject<MantleFluid> moltenEnder    = registerFluid("molten_ender",     stoneBuilder().setRenderColor(0xff105e51).setTemperature(tmp( 777)), Material.LAVA,  7);
  public static final FluidObject<MantleFluid> moltenBlaze    = registerFluid("molten_blaze",    hotBuilder(FluidIcons.BLAZE_STILL, FluidIcons.BLAZE_FLOWING).setDensity(f(3500)).setTemperature(tmp(1800)), Material.LAVA, 14);

  // ores
  public static final FluidObject<MantleFluid> moltenIron   = registerFluid("molten_iron",   moltenBuilder().setRenderColor(0xffa81212).setTemperature(tmp(1100)), Material.LAVA, 12);
  public static final FluidObject<MantleFluid> moltenGold   = registerFluid("molten_gold",   moltenBuilder().setRenderColor(0xfff6d609).setTemperature(tmp(1000)), Material.LAVA, 13);
  public static final FluidObject<MantleFluid> moltenCopper = registerFluid("molten_copper", moltenBuilder().setRenderColor(0xfffba165).setTemperature(tmp( 800)), Material.LAVA, 11);
  public static final FluidObject<MantleFluid> moltenCobalt = registerFluid("molten_cobalt", moltenBuilder().setRenderColor(0xff2376dd).setTemperature(tmp(1250)), Material.LAVA, 10);
  public static final FluidObject<MantleFluid> moltenDebris = registerFluid("molten_debris", moltenBuilder().setRenderColor(0xff5d342c).setTemperature(tmp(1475)), Material.LAVA,  9);
  // alloys
  public static final FluidObject<MantleFluid> moltenSlimesteel    = registerFluid("molten_slimesteel",     moltenBuilder().setRenderColor(0xffb3e0dc).setTemperature(tmp(1200)), Material.LAVA, 10);
  public static final FluidObject<MantleFluid> moltenTinkersBronze = registerFluid("molten_tinkers_bronze", moltenBuilder().setRenderColor(0xfff9cf72).setTemperature(tmp(1000)), Material.LAVA, 12);
  public static final FluidObject<MantleFluid> moltenRoseGold      = registerFluid("molten_rose_gold",      moltenBuilder().setRenderColor(0xfff7cdbb).setTemperature(tmp( 850)), Material.LAVA, 12);
  public static final FluidObject<MantleFluid> moltenPigIron       = registerFluid("molten_pig_iron",       moltenBuilder().setRenderColor(0xfff0a8a4).setTemperature(tmp(1111)), Material.LAVA, 10);

  public static final FluidObject<MantleFluid> moltenManyullyn   = registerFluid("molten_manyullyn",    moltenBuilder().setRenderColor(0xff9261cc).setTemperature(tmp(1500)), Material.LAVA,  9);
  public static final FluidObject<MantleFluid> moltenHepatizon   = registerFluid("molten_hepatizon",    moltenBuilder().setRenderColor(0xff60496b).setTemperature(tmp(1700)), Material.LAVA,  5);
  public static final FluidObject<MantleFluid> moltenQueensSlime = registerFluid("molten_queens_slime", moltenBuilder().setRenderColor(0xff236c45).setTemperature(tmp(1450)), Material.LAVA,  7);
  public static final FluidObject<MantleFluid> moltenSoulsteel   = registerFluid("molten_soulsteel",    moltenBuilder().setRenderColor(0xc46a5244).setTemperature(tmp(1500)), Material.LAVA,  9);
  public static final FluidObject<MantleFluid> moltenNetherite   = registerFluid("molten_netherite",    moltenBuilder().setRenderColor(0xff3c3232).setTemperature(tmp(1550)), Material.LAVA, 11);
  public static final FluidObject<MantleFluid> moltenKnightslime = registerFluid("molten_knightslime",  moltenBuilder().setRenderColor(0xfff18ff0).setTemperature(tmp(1425)), Material.LAVA,  9);

  // compat ores
  public static final FluidObject<MantleFluid> moltenTin      = registerFluid("molten_tin",      moltenBuilder().setRenderColor(0xffc1cddc).setTemperature(tmp(525)), Material.LAVA, 12);

  public static final FluidObject<MantleFluid> moltenAluminum = registerFluid("molten_aluminum", moltenBuilder().setRenderColor(0xffefe0d5).setTemperature(tmp( 725)), Material.LAVA, 12);
  public static final FluidObject<MantleFluid> moltenLead     = registerFluid("molten_lead",     moltenBuilder().setRenderColor(0xff4d4968).setTemperature(tmp( 630)), Material.LAVA, 12);
  public static final FluidObject<MantleFluid> moltenSilver   = registerFluid("molten_silver",   moltenBuilder().setRenderColor(0xffd1ecf6).setTemperature(tmp(1090)), Material.LAVA, 12);
  public static final FluidObject<MantleFluid> moltenNickel   = registerFluid("molten_nickel",   moltenBuilder().setRenderColor(0xffc8d683).setTemperature(tmp(1250)), Material.LAVA, 12);
  public static final FluidObject<MantleFluid> moltenZinc     = registerFluid("molten_zinc",     moltenBuilder().setRenderColor(0xffd3efe8).setTemperature(tmp( 720)), Material.LAVA, 12);
  public static final FluidObject<MantleFluid> moltenPlatinum = registerFluid("molten_platinum", moltenBuilder().setRenderColor(0xffb5aea4).setTemperature(tmp(1270)), Material.LAVA, 12);
  public static final FluidObject<MantleFluid> moltenTungsten = registerFluid("molten_tungsten", moltenBuilder().setRenderColor(0xffd1c08b).setTemperature(tmp(1250)), Material.LAVA, 12);
  public static final FluidObject<MantleFluid> moltenOsmium   = registerFluid("molten_osmium",   moltenBuilder().setRenderColor(0xffbed3cd).setTemperature(tmp(1275)), Material.LAVA, 12);
  public static final FluidObject<MantleFluid> moltenUranium  = registerFluid("molten_uranium",  moltenBuilder().setRenderColor(0xff7f9374).setTemperature(tmp(1130)), Material.LAVA, 12);

  // compat alloys
  public static final FluidObject<MantleFluid> moltenBronze     = registerFluid("molten_bronze",     moltenBuilder().setRenderColor(0xffcea179).setTemperature(tmp(1000)), Material.LAVA, 12);
  public static final FluidObject<MantleFluid> moltenBrass      = registerFluid("molten_brass",      moltenBuilder().setRenderColor(0xffede38b).setTemperature(tmp( 905)), Material.LAVA, 12);
  public static final FluidObject<MantleFluid> moltenElectrum   = registerFluid("molten_electrum",   moltenBuilder().setRenderColor(0xffe8db49).setTemperature(tmp(1060)), Material.LAVA, 12);
  public static final FluidObject<MantleFluid> moltenInvar      = registerFluid("molten_invar",      moltenBuilder().setRenderColor(0xffbab29b).setTemperature(tmp(1200)), Material.LAVA, 12);
  public static final FluidObject<MantleFluid> moltenConstantan = registerFluid("molten_constantan", moltenBuilder().setRenderColor(0xffff9e7f).setTemperature(tmp(1220)), Material.LAVA, 12);
  public static final FluidObject<MantleFluid> moltenPewter     = registerFluid("molten_pewter",     moltenBuilder().setRenderColor(0xffa09b6b).setTemperature(tmp( 700)), Material.LAVA, 10);
  public static final FluidObject<MantleFluid> moltenSteel      = registerFluid("molten_steel",      moltenBuilder().setRenderColor(0xffa7a7a7).setTemperature(tmp(1250)), Material.LAVA, 12);


  /** Creates a builder for a cool fluid */
  private static FluidKey.FluidKeyBuilder coolBuilder() {
    return new FluidKey.FluidKeyBuilder().setSprites(FluidIcons.LIQUID_STILL, FluidIcons.LIQUID_FLOWING);
  }

  /** Creates a builder for a hot fluid */
  private static FluidKey.FluidKeyBuilder hotBuilder(Identifier stillTexture, Identifier flowingTexture) {
    return new FluidKey.FluidKeyBuilder().setSprites(stillTexture, flowingTexture).setDensity(f(2000)).setViscosity(f(10000)).setTemperature(tmp(1000));
  }

  /** Creates a builder for a molten fluid */
  private static FluidKey.FluidKeyBuilder stoneBuilder() {
    return hotBuilder(FluidIcons.STONE_STILL, FluidIcons.STONE_FLOWING);
  }

  /** Creates a builder for a molten fluid */
  private static FluidKey.FluidKeyBuilder moltenBuilder() {
    return hotBuilder(FluidIcons.MOLTEN_STILL, FluidIcons.MOLTEN_FLOWING);
  }

  private static FluidObject<MantleFluid> registerFluid(String name, FluidKey.FluidKeyBuilder builder, Material material, int i) {
    return registerComplexFluid(name,
      builder,
      new MantleFluid.Still(null, null),
      new MantleFluid.Flowing(null, null),
      material,
      i
    );
  }

  private static FluidObject<MantleFluid> registerComplexFluid(String name, FluidKey.FluidKeyBuilder builder, MantleFluid.Still still, MantleFluid.Flowing flowing, Material material, int i) {
    BucketItem bucket = new BucketItem(flowing, new Item.Settings().group(ItemGroup.MISC));

    still.setFlowing(flowing);
    still.setBucketItem(bucket);
    flowing.setStill(still);
    flowing.setBucketItem(bucket);

    Registry.register(Registry.FLUID, id("flowing_" + name), flowing);
    Registry.register(Registry.FLUID, id(name), still);
    Registry.register(Registry.ITEM, id("bucket_" + name), bucket);
    FluidBlock block = Registry.register(Registry.BLOCK, id("fluid_" + name), new FluidBlock(still, AbstractBlock.Settings.of(material)));
    still.setBlockState(block.getDefaultState());
    flowing.setBlockState(block.getDefaultState());

    new SimpleFluidKey(builder
      .setRawFluid(still)
      .setName(new TranslatableText("fluids.tconstruct." + name))
      .setIdEntry(id(name))
    ).register();

    return new FluidObject<>(
      id(name),
      name,
      () -> still,
      () -> flowing,
      () -> block
    );
  }

  private static FluidTemperature.ContinuousFluidTemperature tmp(int temp) {
    return fluid -> temp;
  }

  private static FluidAmount f(int i) {
    return FluidAmount.of(1000, i);
  }

  @Override
  public void onInitialize() {
    FuelRegistry.INSTANCE.add(moltenBlaze.asItem(), 30000);
  }
}
