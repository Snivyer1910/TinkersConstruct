package slimeknights.tconstruct.library.modifiers;

import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.tag.FluidTags;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.TinkerRegistries;
import slimeknights.tconstruct.library.recipe.tinkerstation.ValidatedResult;
import slimeknights.tconstruct.library.tools.ModifierStatsBuilder;
import slimeknights.tconstruct.library.tools.ToolDefinition;
import slimeknights.tconstruct.library.tools.nbt.IModDataReadOnly;
import slimeknights.tconstruct.library.tools.nbt.IModifierToolStack;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.StatsNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.BiConsumer;

/**
 * Interface representing both modifiers and traits.
 * Any behavior special to either one is handled elsewhere.
 */
public class Modifier{
  private static final EntityAttributeModifier ANTI_KNOCKBACK_MODIFIER = new EntityAttributeModifier(TConstruct.modID + ".anti_knockback", 1f, Operation.ADDITION);

  /** Modifier random instance, use for chance based effects */
  protected static Random RANDOM = new Random();

  protected static final String KEY_LEVEL = "enchantment.level.";
  public static final int DEFAULT_PRIORITY = 100;

  /** Display color for all text for this modifier */
  private final int color;

  /** Cached key used for translations */
  @Nullable
  private String translationKey;
  /** Cached text component for display names */
  @Nullable
  private Text displayName;
  /** Cached text component for description */
  @Nullable
  private List<Text> descriptionList;
  /** Cached text component for description */
  @Nullable
  private Text description;

  public Modifier(int color) {
    this.color = color;
  }

  public int getColor() {
    return color;
  }

  public @Nullable ModifierId getRegistryName() {
    return new ModifierId(TinkerRegistries.MODIFIERS.getId(this));
  }

  /**
   * Override this method to make your modifier run earlier or later.
   * Higher numbers run earlier, 100 is default
   * @return Priority
   */
  public int getPriority() {
    return DEFAULT_PRIORITY;
  }

  /**
   * Gets the modifier ID. Unlike {@link #getRegistryName()}, this method must be nonnull
   * @return  Modifier ID
   */
  public ModifierId getId() {
    return new ModifierId(Objects.requireNonNull(TinkerRegistries.MODIFIERS.getId(this), "Modifier has null registry name"));
  }

  /* Tooltips */

  /**
   * Overridable method to create a translation key. Will be called once and the result cached
   * @return  Translation key
   */
  protected String makeTranslationKey() {
    return Util.createTranslationKey("modifier", TinkerRegistries.MODIFIERS.getId(this));
  }

  /**
   * Gets the translation key for this modifier
   * @return  Translation key
   */
  public final String getTranslationKey() {
    if (translationKey == null) {
      translationKey = makeTranslationKey();
    }
    return translationKey;
  }

  /**
   * Overridable method to create the display name for this modifier, ideal to modify colors
   * @return  Display name
   */
  protected Text makeDisplayName() {
    return new TranslatableText(getTranslationKey());
  }

  /**
   * Gets the display name for this modifier
   * @return  Display name for this modifier
   */
  public final Text getDisplayName() {
    if (displayName == null) {
      displayName = new TranslatableText(getTranslationKey()).styled(style -> style.withColor(TextColor.fromRgb(getColor())));
    }
    return displayName;
  }

  /**
   * Gets the display name for the given level of this modifier
   * @param level  Modifier level
   * @return  Display name
   */
  public Text getDisplayName(int level) {
    return new TranslatableText(getTranslationKey())
      .append(" ")
      .append(new TranslatableText(KEY_LEVEL + level))
      .styled(style -> style.withColor(TextColor.fromRgb(color)));
  }

  /**
   * Stack sensitive version of {@link #getDisplayName(int)}. Useful for displaying persistent data such as overslime or redstone amount
   * @param tool   Tool instance
   * @param level  Tool level
   * @return  Stack sensitive display name
   */
  public Text getDisplayName(IModifierToolStack tool, int level) {
    return getDisplayName(level);
  }

  /**
   * Gets the description for this modifier
   * @return  Description for this modifier
   */
  public final List<Text> getDescriptionList() {
    if (descriptionList == null) {
      descriptionList = Arrays.asList(
        new TranslatableText(getTranslationKey() + ".flavor").formatted(Formatting.ITALIC),
        new TranslatableText(getTranslationKey() + ".description"));
    }
    return descriptionList;
  }

  /**
   * Gets the description for this modifier
   * @return  Description for this modifier
   */
  public final Text getDescription() {
    if (description == null) {
      description = getDescriptionList().stream()
                                        .reduce((c1, c2) -> new LiteralText("").append(c1).append("\n").append(c2))
                                        .orElse(LiteralText.EMPTY);
    }
    return description;
  }


  /* Tool building hooks */

  /**
   * Adds any relevant volatile data to the tool data. This data is rebuilt every time modifiers rebuild.
   * <br>
   * Alternatives:
   * <ul>
   *   <li>Persistent mod data (accessed via {@link IModifierToolStack}): Can be written to freely, but will not automatically remove if the modifier is removed.</li>
   * </ul>
   * @param toolDefinition  Tool definition, will be empty for non-multitools
   * @param baseStats       Base material stats. Does not take tool definition or other modifiers into account. Not stored, so if you want any data store it in volatile data
   * @param persistentData  Extra modifier NBT. Note that if you rely on a value in persistent data, it is up to you to ensure tool stats refresh if it changes
   * @param level           Modifier level
   * @param volatileData    Mutable mod NBT data, result of this method
   */
  public void addVolatileData(ToolDefinition toolDefinition, StatsNBT baseStats, IModDataReadOnly persistentData, int level, ModDataNBT volatileData) {}

  /**
   * Adds raw stats to the tool. Called whenever tool stats are rebuilt.
   * <br>
   * Alternatives:
   * <ul>
   *   <li>{@link #addAttributes(IModifierToolStack, int, BiConsumer)}: Allows dynamic stats based on any tool stat, but does not support mining speed, mining level, or durability.</li>
   *   <li>{@link #onBreakSpeed(IModifierToolStack, int, PlayerEntity, Direction, boolean, float)}: Allows dynamic mining speed based on the block mined and the entity mining. Will not show in tooltips.</li>
   * </ul>
   * @param toolDefinition  Tool definition, will be empty for non-multitools
   * @param baseStats       Base material stats. Does not take tool definition or other modifiers into account
   * @param persistentData  Extra modifier NBT. Note that if you rely on a value in persistent data, it is up to you to ensure tool stats refresh if it changes
   * @param volatileData    Modifier NBT calculated from modifiers in {@link #addVolatileData(ToolDefinition, StatsNBT, IModDataReadOnly, int, ModDataNBT)}
   * @param level           Modifier level
   * @param builder         Tool stat builder
   */
  public void addToolStats(ToolDefinition toolDefinition, StatsNBT baseStats, IModDataReadOnly persistentData, IModDataReadOnly volatileData, int level, ModifierStatsBuilder builder) {}

  /**
   * Adds attributes from this modifier's effect. Called whenever the item stack refreshes capabilities.
   * <br>
   * Alternatives:
   * <ul>
   *   <li>{@link #addToolStats(ToolDefinition, StatsNBT, IModDataReadOnly, IModDataReadOnly, int, ModifierStatsBuilder)}: Limited context, but can affect durability, mining level, and mining speed.</li>
   * </ul>
   * @param tool      Current tool instance
   * @param level     Modifier level
   * @param consumer  Attribute consumer
   */
  public void addAttributes(IModifierToolStack tool, int level, BiConsumer<EntityAttribute,EntityAttributeModifier> consumer) {}

  /**
   * Called when modifiers or tool materials change to validate the tool. You are free to modify persistent data in this hook if needed.
   * Do not validate max level here, simply ignore levels over max if needed.
   * @param tool   Current tool instance
   * @param level  Modifier level
   * @return  PASS result if success, failure if there was an error.
   */
  public ValidatedResult validate(ToolStack tool, int level) {
    return ValidatedResult.PASS;
  }

  /* Hooks */

  /**
   * Called when the tool is damaged. Can be used to cancel, decrease, or increase the damage.
   * @param toolStack  Tool stack
   * @param level      Tool level
   * @param amount     Amount of damage to deal
   * @return  Replacement damage. Returning 0 cancels the damage and stops other modifiers from processing.
   */
  public int onDamageTool(IModifierToolStack toolStack, int level, int amount) {
    return amount;
  }

  /**
   * Called when the tool is repair. Can be used to decrease, increase, or cancel the repair.
   * @param toolStack  Tool stack
   * @param level      Tool level
   * @param factor     Original factor
   * @return  Replacement factor. Returning 0 prevents repair
   */
  public float getRepairFactor(IModifierToolStack toolStack, int level, float factor) {
    return factor;
  }

  /**
   * Called when the stack updates in the player inventory
   * @param tool           Current tool instance
   * @param level          Modifier level
   * @param world          World containing tool
   * @param holder         Entity holding tool
   * @param itemSlot       Slot containing this tool
   * @param isSelected     If true, this item is currently in the player's main hand
   * @param isCorrectSlot  If true, this item is in the proper slot. For tools, that is main hand or off hand. For armor, this means its in the correct armor slot
   * @param stack          Item stack instance to check other slots for the tool. Do not modify
   */
  public void onInventoryTick(IModifierToolStack tool, int level, World world, LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {}

  /**
   * Called on entity or block loot to allow modifying loot
   * @param tool           Current tool instance
   * @param level          Modifier level
   * @param generatedLoot  Current loot list before this modifier
   * @param context        Full loot context
   * @return  Loot replacement
   */
  public List<ItemStack> processLoot(ToolStack tool, int level, List<ItemStack> generatedLoot, LootContext context) {
    return generatedLoot;
  }


  /* Harvest hooks */

  /**
   * Called when break speed is being calculated to affect mining speed conditionally.
   * <br>
   * Alternatives:
   * <ul>
   *   <li>{@link #addToolStats(ToolDefinition, StatsNBT, IModDataReadOnly, IModDataReadOnly, int, ModifierStatsBuilder)}: Limited context, but effect shows in the tooltip.</li>
   * </ul>
   * @param tool                 Current tool instance
   * @param level                Modifier level
   * @param player                Event instance
   * @param sideHit              Side of the block that was hit
   * @param isEffective          If true, the tool is effective against this block type
   * @param miningSpeedModifier  Calculated modifier from potion effects such as haste and environment such as water, use for additive bonuses to ensure consistency with the mining speed stat
   */
  public void onBreakSpeed(IModifierToolStack tool, int level, PlayerEntity player, Direction sideHit, boolean isEffective, float miningSpeedModifier) {}

  /**
   * Adds loot table related enchantments from this modifier's effect, called before breaking a block.
   * For looting, see {@link net.minecraftforge.event.entity.living.LootingLevelEvent}.
   * Needed to add enchantments for silk touch, fortune, and looting. Can add conditionally if needed.
   * @param tool      Tool used
   * @param level     Modifier level
   * @param consumer  Consumer accepting any enchantments
   */
  public void applyEnchantments(IModifierToolStack tool, int level, BiConsumer<Enchantment, Integer> consumer) {}

  /**
   * Called after a block is broken to apply special effects
   * @param tool          Tool used
   * @param level         Modifier level
   * @param world         World instance
   * @param state         Block broken
   * @param pos           Position broken
   * @param living        Entity breaking the block
   * @param wasEffective  If true, tool was effective at breaking this block
   */
  public void afterBlockBreak(IModifierToolStack tool, int level, World world, BlockState state, BlockPos pos, LivingEntity living, boolean wasEffective) {}


  /* Attack hooks */

  /**
   * Called when a living entity is attacked, before critical hit damage is calculated. Allows modifying the damage dealt. Do not modify the entity here, its possible the attack will still be canceled
   * <br>
   * Alternatives:
   * <ul>
   *   <li>{@link #addToolStats(ToolDefinition, StatsNBT, IModDataReadOnly, IModDataReadOnly, int, ModifierStatsBuilder)}: Adjusts the base tool stats that show in the tooltip, but has less context for modification</li>
   *   <li>{@link #afterLivingHit(IModifierToolStack, int, LivingEntity, LivingEntity, float, boolean, boolean)}: Perform special attacks on entity hit beyond damage boosts</li>
   *   <li>{@link #beforeLivingHit(IModifierToolStack, int, LivingEntity, LivingEntity, float, float, float, boolean, boolean)}: Apply effects that must run before hit</li>
   * </ul>
   * @param tool          Tool used to attack
   * @param level         Modifier level
   * @param attacker      Entity doing the attacking
   * @param target        Entity being attacked
   * @param baseDamage    Base damage dealt before modifiers
   * @param damage        Computed damage from all prior modifiers
   * @param isCritical    If true, this attack is a critical hit
   * @param fullyCharged  If true, this attack was fully charged (could perform a sword sweep)
   * @return  New damage to deal
   */
  public float applyLivingDamage(IModifierToolStack tool, int level, LivingEntity attacker, LivingEntity target, float baseDamage, float damage, boolean isCritical, boolean fullyCharged) {
    return damage;
  }

  /**
   * Called right before an entity is hit, used to modify knockback applied or to apply special effects that need to run before damage. Damage is final damage including critical damage.
   * Note there is still a chance this attack won't deal damage, if that happens {@link #failedLivingHit(IModifierToolStack, int, LivingEntity, LivingEntity, boolean, boolean)} will run.
   * <br>
   * Alternatives:
   * <ul>
   *   <li>{@link #afterLivingHit(IModifierToolStack, int, LivingEntity, LivingEntity, float, boolean, boolean)}: Perform special attacks on entity hit beyond knockback boosts</li>
   * </ul>
   * @param tool           Tool used to attack
   * @param level          Modifier level
   * @param attacker       Entity doing the attacking
   * @param target         Entity being attacked
   * @param damage         Damage to deal to the attacker
   * @param baseKnockback  Base knockback before modifiers
   * @param knockback      Computed knockback from all prior modifiers
   * @param isCritical     If true, this attack is a critical hit
   * @param fullyCharged   If true, this attack was fully charged (could perform a sword sweep)
   * @return  New knockback to apply. 0.5 is equivelent to 1 level of the vanilla enchant
   */
  public float beforeLivingHit(IModifierToolStack tool, int level, LivingEntity attacker, LivingEntity target, float damage, float baseKnockback, float knockback, boolean isCritical, boolean fullyCharged) {
    return knockback;
  }

  /**
   * Called after a living entity is successfully attacked. Used to apply special effects on hit.
   * <br>
   * Alternatives:
   * <ul>
   *   <li>{@link #addToolStats(ToolDefinition, StatsNBT, IModDataReadOnly, IModDataReadOnly, int, ModifierStatsBuilder)}: Adjusts the base tool stats that affect damage</li>
   *   <li>{@link #applyLivingDamage(IModifierToolStack, int, LivingEntity, LivingEntity, float, float, boolean, boolean)}: Change the amount of damage dealt with attacker context</li>
   *   <li>{@link #beforeLivingHit(IModifierToolStack, int, LivingEntity, LivingEntity, float, float, float, boolean, boolean)}: Change the amount of knockback dealt</li>
   *   <li>{@link #failedLivingHit(IModifierToolStack, int, LivingEntity, LivingEntity, boolean, boolean)}: Called after living hit when damage was not dealt</li>
   * </ul>
   * @param tool          Tool used to attack
   * @param level         Modifier level
   * @param attacker      Entity doing the attacking
   * @param target        Entity being attacked
   * @param damageDealt   Amount of damage successfully dealt
   * @param isCritical    If true, this attack is a critical hit
   * @param fullyCharged  If true, this attack was fully charged (could perform a sword sweep)
   * @return  Extra damage to deal to the tool
   */
  public int afterLivingHit(IModifierToolStack tool, int level, LivingEntity attacker, LivingEntity target, float damageDealt, boolean isCritical, boolean fullyCharged) {
    return 0;
  }

  /**
   * Called after attacking an entity when no damage was dealt
   * @param tool          Tool used to attack
   * @param level         Modifier level
   * @param attacker      Entity doing the attacking
   * @param target        Entity being attacked
   * @param isCritical    If true, this attack is a critical hit
   * @param fullyCharged  If true, this attack was fully charged (could perform a sword sweep)
   */
  public void failedLivingHit(IModifierToolStack tool, int level, LivingEntity attacker, LivingEntity target, boolean isCritical, boolean fullyCharged) {}


  /* Display */

  /**
   * Determines if the modifier should display
   * @param advanced  If true, in an advanced view such as the tinker station. False for tooltips
   * @return  True if the modifier should show
   */
  public boolean shouldDisplay(boolean advanced) {
    return true;
  }

  /**
   * Gets the damage percentage for display.  First tool returning something other than NaN will determine display durability
   * @param tool   Tool instance
   * @param level  Modifier level
   * @return  Damage percentage. 0 is undamaged, 1 is fully damaged.
   */
  public double getDamagePercentage(IModifierToolStack tool, int level) {
    return Double.NaN;
  }

  /**
   * Override the default tool logic for showing the durability bar
   * @param tool   Tool instance
   * @param level  Modifier level
   * @return  True forces the bar to show, false forces it to hide. Return null to allow default behavior
   */
  @Nullable
  public Boolean showDurabilityBar(IModifierToolStack tool, int level) {
    return null;
  }

  /**
   * Gets the RGB for the durability bar
   * @param tool   Tool instance
   * @param level  Modifier level
   * @return  RGB, or -1 to not handle it
   */
  public int getDurabilityRGB(IModifierToolStack tool, int level) {
    return -1;
  }


  /* Utils */

  /**
   * Adds secondary damage to an entity
   * @param source       Damage source
   * @param damage       Damage amount
   * @param target       Target
   * @param noKnockback  If true, prevents extra knockback
   * @return  True if damaged
   */
  public static boolean attackEntitySecondary(DamageSource source, float damage, LivingEntity target, boolean noKnockback) {
    Optional<EntityAttributeInstance> knockbackResistance = Optional.ofNullable(target.getAttributeInstance(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE))
                                                                        .filter(attribute -> !attribute.hasModifier(ANTI_KNOCKBACK_MODIFIER));
    // store last damage before secondary attack
    float oldLastDamage = target.lastDamageTaken;

    // prevent knockback in secondary attacks, if requested
    if (noKnockback) {
      knockbackResistance.ifPresent(attribute -> attribute.addTemporaryModifier(ANTI_KNOCKBACK_MODIFIER));
    }

    // set hurt resistance time to 0 because we always want to deal damage in traits
    target.timeUntilRegen = 0;
    boolean hit = target.damage(source, damage);
    // set total received damage, important for AI and stuff
    target.lastDamageTaken += oldLastDamage;

    // remove no knockback marker
    if (noKnockback) {
      knockbackResistance.ifPresent(attribute -> attribute.removeModifier(ANTI_KNOCKBACK_MODIFIER));
    }

    return hit;
  }

  /**
   * Gets the tool stack from the given entities mainhand. Useful for specialized event handling in modifiers
   * @param living  Entity instance
   * @return  Tool stack
   */
  @Nullable
  public static ToolStack getHeldTool(@Nullable LivingEntity living) {
    if (living == null) {
      return null;
    }
    ItemStack stack = living.getMainHandStack();
    if (stack.isEmpty() || !stack.getItem().isIn(TinkerTags.Items.MODIFIABLE)) {
      return null;
    }
    ToolStack tool = ToolStack.from(stack);
    return tool.isBroken() ? null : ToolStack.from(stack);
  }

  /**
   * Gets the mining speed modifier for the current conditions, notably potions and armor enchants
   * @param entity  Entity to check
   * @return  Mining speed modifier
   */
  public static float getMiningModifier(LivingEntity entity) {
    float modifier = 1.0f;
    // haste effect
    if (StatusEffectUtil.hasHaste(entity)) {
      modifier *= 1.0F + (StatusEffectUtil.getHasteAmplifier(entity) + 1) * 0.2f;
    }
    // mining fatigue
    StatusEffectInstance miningFatigue = entity.getStatusEffect(StatusEffects.MINING_FATIGUE);
    if (miningFatigue != null) {
      switch(miningFatigue.getAmplifier()) {
        case 0:
          modifier *= 0.3F;
          break;
        case 1:
          modifier *= 0.09F;
          break;
        case 2:
          modifier *= 0.0027F;
          break;
        case 3:
        default:
          modifier *= 8.1E-4F;
      }
    }
    // water
    if (entity.isSubmergedIn(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(entity)) {
      modifier /= 5.0F;
    }
    if (!entity.isOnGround()) {
      modifier /= 5.0F;
    }
    return modifier;
  }
}
