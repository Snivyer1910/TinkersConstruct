package slimeknights.tconstruct.tables.client.inventory.table;

import com.google.common.collect.Lists;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import slimeknights.tconstruct.library.MaterialRegistry;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.client.Icons;
import slimeknights.tconstruct.library.materials.IMaterial;
import slimeknights.tconstruct.library.materials.stats.IMaterialStats;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.recipe.material.MaterialRecipe;
import slimeknights.tconstruct.library.recipe.partbuilder.PartRecipe;
import slimeknights.tconstruct.tables.client.inventory.BaseStationScreen;
import slimeknights.tconstruct.tables.inventory.table.partbuilder.PartBuilderContainer;
import slimeknights.tconstruct.tables.tileentity.table.PartBuilderTileEntity;

import java.util.List;
import java.util.function.Function;

public class PartBuilderScreen extends BaseStationScreen<PartBuilderTileEntity, PartBuilderContainer> {
  private static final Text INFO_TEXT = Util.makeTranslation("gui", "part_builder.info");
  private static final Text TRAIT_TITLE = Util.makeTranslation("gui", "part_builder.trait").formatted(Formatting.UNDERLINE);
  private static final MutableText UNCRAFTABLE_MATERIAL = Util.makeTranslation("gui", "part_builder.uncraftable").formatted(Formatting.RED);
  private static final MutableText UNCRAFTABLE_MATERIAL_TOOLTIP = Util.makeTranslation("gui", "part_builder.uncraftable.tooltip");

  private static final Identifier BACKGROUND = Util.getResource("textures/gui/partbuilder.png");

  /** Part builder side panel */
  protected PartInfoPanelScreen infoPanelScreen;
  /** Current scrollbar position */
  private float sliderProgress = 0.0F;
  /** Is {@code true} if the player clicked on the scroll wheel in the GUI */
  private boolean clickedOnScrollBar;

  /**
   * The index of the first recipe to display.
   * The number of recipes displayed at any time is 12 (4 recipes per row, and 3 rows). If the player scrolled down one
   * row, this value would be 4 (representing the index of the first slot on the second row).
   */
  private int recipeIndexOffset = 0;
  private boolean hasPatternInPatternSlot;

  public PartBuilderScreen(PartBuilderContainer container, PlayerInventory playerInventory, Text title) {
    super(container, playerInventory, title);

    this.infoPanelScreen = new PartInfoPanelScreen(this, container, playerInventory, title);
    this.infoPanelScreen.setTextScale(7/9f);
    this.infoPanelScreen.backgroundHeight = this.backgroundHeight;
    this.addModule(this.infoPanelScreen);
  }

  @Override
  protected void drawBackground(MatrixStack matrices, float partialTicks, int mouseX, int mouseY) {
    this.drawBackground(matrices, BACKGROUND);

    // draw slot icons
    this.drawIconEmpty(matrices, this.handler.getPatternSlot(), Icons.PATTERN);
    this.drawIconEmpty(matrices, this.handler.getInputSlot(), Icons.INGOT);

    // draw scrollbar
    assert this.client != null;
    this.client.getTextureManager().bindTexture(BACKGROUND);
    this.drawTexture(matrices, this.cornerX + 126, this.cornerY + 15 + (int) (41.0F * this.sliderProgress), 176 + (this.canScroll() ? 0 : 12), 0, 12, 15);
    this.drawRecipesBackground(matrices, mouseX, mouseY, this.cornerX + 51, this.cornerY + 15);
    this.drawRecipesItems(matrices, this.cornerX + 51, this.cornerY + 15);

    super.drawBackground(matrices, partialTicks, mouseX, mouseY);
  }

  /** Draw backgrounds for all patterns */
  private void drawRecipesBackground(MatrixStack matrices, int mouseX, int mouseY, int left, int top) {
    int max = Math.min(this.recipeIndexOffset + 12, this.getPartRecipeCount());
    for (int i = this.recipeIndexOffset; i < max; ++i) {
      int relative = i - this.recipeIndexOffset;
      int x = left + relative % 4 * 18;
      int y = top + (relative / 4) * 18;
      int u = this.backgroundHeight;
      if (i == this.handler.getSelectedPartRecipe()) {
        u += 18;
      } else if (mouseX >= x && mouseY >= y && mouseX < x + 18 && mouseY < y + 18) {
        u += 36;
      }
      this.drawTexture(matrices, x, y, 0, u, 18, 18);
    }
  }

  /** Draw slot icons for all patterns */
  private void drawRecipesItems(MatrixStack matrices, int left, int top) {
    // use block texture list
    assert this.client != null;
    this.client.getTextureManager().bindTexture(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE);
    Function<Identifier, Sprite> spriteGetter = this.client.getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE);
    // iterate all recipes
    List<PartRecipe> list = this.handler.getPartRecipes();
    int max = Math.min(this.recipeIndexOffset + 12, this.getPartRecipeCount());
    for (int i = this.recipeIndexOffset; i < max; ++i) {
      int relative = i - this.recipeIndexOffset;
      int x = left + relative % 4 * 18 + 1;
      int y = top + (relative / 4) * 18 + 1;
      // get the sprite for the pattern and draw
      PartRecipe recipe = list.get(i);
      Identifier pattern = recipe.getPattern();
      Sprite sprite = spriteGetter.apply(new Identifier(pattern.getNamespace(), "gui/tinker_pattern/" + pattern.getPath()));
      drawSprite(matrices, x, y, 100, 16, 16, sprite);
    }
  }

  @Override
  public void updateDisplay() {
    // update slider
    this.hasPatternInPatternSlot = this.handler.hasPatternInPatternSlot();
    if (!this.hasPatternInPatternSlot) {
      this.sliderProgress = 0.0F;
      this.recipeIndexOffset = 0;
    }

    // update part recipe cost
    PartRecipe partRecipe = this.handler.getPartRecipe();
    if (partRecipe != null) {
      this.infoPanelScreen.setPatternCost(partRecipe.getCost());
    } else {
      this.infoPanelScreen.clearPatternCost();
    }

    // update material
    MaterialRecipe materialRecipe = this.handler.getMaterialRecipe();
    if (materialRecipe != null) {
      this.setDisplayForMaterial(materialRecipe);
    } else {
      // default text
      this.infoPanelScreen.setCaption(this.getTitle());
      this.infoPanelScreen.setText(INFO_TEXT);
      this.infoPanelScreen.clearMaterialValue();
    }
  }

  /**
   * Updates the data in the material display
   * @param materialRecipe  New material recipe
   */
  private void setDisplayForMaterial(MaterialRecipe materialRecipe) {
    IMaterial material = materialRecipe.getMaterial();
    this.infoPanelScreen.setCaption(new TranslatableText(material.getTranslationKey()).styled(style -> style.withColor(material.getColor())));

    // determine how much material we have
    // get exact number of material, rather than rounded

    float value = materialRecipe.getMaterialValue(this.handler.getCraftInventory());
    MutableText formatted = new LiteralText(Util.df.format(value));

    // if we have a part recipe, mark material red when not enough
    PartRecipe partRecipe = this.handler.getPartRecipe();
    if (partRecipe != null && value < partRecipe.getCost()) {
      formatted = formatted.formatted(Formatting.DARK_RED);
    }
    this.infoPanelScreen.setMaterialValue(formatted);

    // update stats and traits
    List<Text> stats = Lists.newLinkedList();
    List<Text> tips = Lists.newArrayList();

    // add warning that the material is uncraftable
    if (!material.isCraftable()) {
      stats.add(UNCRAFTABLE_MATERIAL);
      stats.add(LiteralText.EMPTY);
      tips.add(UNCRAFTABLE_MATERIAL_TOOLTIP);
      tips.add(LiteralText.EMPTY);
    }

    List<ModifierEntry> traits = material.getTraits();
    if (!traits.isEmpty()) {
      stats.add(TRAIT_TITLE);
      tips.add(LiteralText.EMPTY);
      for (ModifierEntry trait : traits) {
        Modifier mod = trait.getModifier();
        stats.add(mod.getDisplayName(trait.getLevel()));
        tips.add(mod.getDescription());
      }
      stats.add(LiteralText.EMPTY);
      tips.add(LiteralText.EMPTY);
    }

    for (IMaterialStats stat : MaterialRegistry.getInstance().getAllStats(material.getIdentifier())) {
      List<Text> info = stat.getLocalizedInfo();

      if (!info.isEmpty()) {
        stats.add(stat.getLocalizedName().formatted(Formatting.UNDERLINE));
        tips.add(LiteralText.EMPTY);

        stats.addAll(info);
        tips.addAll(stat.getLocalizedDescriptions());

        stats.add(LiteralText.EMPTY);
        tips.add(LiteralText.EMPTY);
      }
    }

    // remove last line if empty
    if (!stats.isEmpty() && stats.get(stats.size() - 1).getString().isEmpty()) {
      stats.remove(stats.size() - 1);
      tips.remove(tips.size() - 1);
    }

    this.infoPanelScreen.setText(stats, tips);
  }


  /* Scrollbar logic */

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
    this.clickedOnScrollBar = false;

    if (this.infoPanelScreen.handleMouseClicked(mouseX, mouseY, mouseButton)) {
      return false;
    }

    if (this.hasPatternInPatternSlot) {
      int x = this.cornerX + 51;
      int y = this.cornerY + 15;
      int maxIndex = Math.min((this.recipeIndexOffset + 12), this.getPartRecipeCount());
      for (int l = this.recipeIndexOffset; l < maxIndex; ++l) {
        int relative = l - this.recipeIndexOffset;
        double buttonX = mouseX - (double) (x + relative % 4 * 18);
        double buttonY = mouseY - (double) (y + relative / 4 * 18);

        assert this.client != null;
        if (buttonX >= 0.0D && buttonY >= 0.0D && buttonX < 18.0D && buttonY < 18.0D && this.handler.onButtonClick(this.client.player, l)) {
          MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
          assert this.client.interactionManager != null;
          this.client.interactionManager.clickButton((this.handler).syncId, l);
          return true;
        }
      }
      // scrollbar position
      x = this.cornerX + 126;
      y = this.cornerY + 15;
      if (mouseX >= x && mouseX < (x + 12) && mouseY >= y && mouseY < (y + 54)) {
        this.clickedOnScrollBar = true;
      }
    }

    return super.mouseClicked(mouseX, mouseY, mouseButton);
  }

  @Override
  public boolean mouseDragged(double mouseX, double mouseY, int clickedMouseButton, double timeSinceLastClick, double unknown) {
    if (this.infoPanelScreen.handleMouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick)) {
      return false;
    }

    if (this.clickedOnScrollBar && this.canScroll()) {
      int i = this.cornerY + 14;
      int j = i + 54;
      this.sliderProgress = ((float) mouseY - i - 7.5F) / ((float) (j - i) - 15.0F);
      this.sliderProgress = MathHelper.clamp(this.sliderProgress, 0.0F, 1.0F);
      this.recipeIndexOffset = (int) ((this.sliderProgress * this.getHiddenRows()) + 0.5D) * 4;
      return true;
    } else {
      return super.mouseDragged(mouseX, mouseY, clickedMouseButton, timeSinceLastClick, unknown);
    }
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
    if (this.infoPanelScreen.handleMouseScrolled(mouseX, mouseY, delta)) {
      return false;
    }

    if (this.canScroll()) {
      int i = this.getHiddenRows();
      this.sliderProgress = MathHelper.clamp((float) (this.sliderProgress - delta / i), 0.0F, 1.0F);
      this.recipeIndexOffset = (int) ((this.sliderProgress * (float) i) + 0.5f) * 4;
    }

    return true;
  }

  @Override
  public boolean mouseReleased(double mouseX, double mouseY, int state) {
    if (this.infoPanelScreen.handleMouseReleased(mouseX, mouseY, state)) {
      return false;
    }

    return super.mouseReleased(mouseX, mouseY, state);
  }


  /* Update error logic */

  @Override
  public void error(Text message) {
    this.infoPanelScreen.setCaption(COMPONENT_ERROR);
    this.infoPanelScreen.setText(message);
  }

  @Override
  public void warning(Text message) {
    this.infoPanelScreen.setCaption(COMPONENT_WARNING);
    this.infoPanelScreen.setText(message);
  }


  /* Helpers */

  /** Gets the number of part recipes */
  private int getPartRecipeCount() {
    return handler.getPartRecipes().size();
  }

  /** If true, we can scroll */
  private boolean canScroll() {
    return this.hasPatternInPatternSlot && this.getPartRecipeCount() > 12;
  }

  /** Gets the number of hidden part recipe rows */
  private int getHiddenRows() {
    return (this.getPartRecipeCount() + 4 - 1) / 4 - 3;
  }
}
