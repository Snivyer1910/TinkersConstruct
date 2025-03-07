package slimeknights.tconstruct.library.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Matrix4f;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import org.lwjgl.opengl.GL11;
import slimeknights.mantle.client.screen.ElementScreen;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GuiUtil {
  /**
   * Draws the background of a container
   * @param matrices    Matrix context
   * @param screen      Parent screen
   * @param background  Background location
   */
  public static void drawBackground(MatrixStack matrices, HandledScreen<?> screen, Identifier background) {
    RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
    screen.client.getTextureManager().bindTexture(background);
    screen.drawTexture(matrices, screen.x, screen.y, 0, 0, screen.backgroundWidth, screen.backgroundHeight);
  }

  /**
   * Draws the container names
   * @param matrices    Matrix context
   * @param screen  Screen name
   * @param font    Screen font TODO: can remove?
   * @param inv     Player inventory TODO: can remove?
   */
  public static void drawContainerNames(MatrixStack matrices, HandledScreen<?> screen, TextRenderer font, PlayerInventory inv) {
    String name = screen.getTitle().getString();
    font.draw(matrices, name, (screen.backgroundWidth / 2f - font.getWidth(name) / 2f), 6.0F, 0x404040);
    font.draw(matrices, inv.getDisplayName().getString(), 8.0F, (screen.backgroundHeight - 96 + 2), 0x404040);
  }

  /**
   * Checks if the given area is hovered
   * @param mouseX    Mouse X position
   * @param mouseY    Mouse Y position
   * @param x         Tank X position
   * @param y         Tank Y position
   * @param width     Tank width
   * @param height    Tank height
   * @return  True if the area is hovered
   */
  public static boolean isHovered(int mouseX, int mouseY, int x, int y, int width, int height) {
    return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
  }

  /**
   * Checks if the given tank area is hovered
   * @param mouseX    Mouse X position
   * @param mouseY    Mouse Y position
   * @param amount    Current tank amount
   * @param capacity  Tank capacity
   * @param x         Tank X position
   * @param y         Tank Y position
   * @param width     Tank width
   * @param height    Tank height
   * @return  True if the tank is hovered, false otherwise
   */
  public static boolean isTankHovered(int mouseX, int mouseY, int amount, int capacity, int x, int y, int width, int height) {
    // check X position first, its easier
    if (mouseX < x || mouseX > x + width || mouseY > y + height) {
      return false;
    }
    // next, try height
    int topHeight = height - (height * amount / capacity);
    return mouseY > y + topHeight;
  }

  /**
   * Renders a fluid tank with a partial fluid level
   * @param screen    Parent screen
   * @param stack     Fluid stack
   * @param capacity  Tank capacity, determines height
   * @param x         Tank X position
   * @param y         Tank Y position
   * @param width     Tank width
   * @param height    Tank height
   * @param depth     Tank depth
   */
  public static void renderFluidTank(MatrixStack matrices, HandledScreen<?> screen, FluidVolume stack, int capacity, int x, int y, int width, int height, int depth) {
    renderFluidTank(matrices, screen, stack, stack.getAmount(), capacity, x, y, width, height, depth);
  }

  /**
   * Renders a fluid tank with a partial fluid level and an amount override
   * @param screen    Parent screen
   * @param stack     Fluid stack
   * @param capacity  Tank capacity, determines height
   * @param x         Tank X position
   * @param y         Tank Y position
   * @param width     Tank width
   * @param height    Tank height
   * @param depth     Tank depth
   */
  public static void renderFluidTank(MatrixStack matrices, HandledScreen<?> screen, FluidVolume stack, int amount, int capacity, int x, int y, int width, int height, int depth) {
    if(!stack.isEmpty()) {
      int maxY = y + height;
      int fluidHeight = Math.min(height * amount / capacity, height);
      renderTiledFluid(matrices, screen, stack, x, maxY - fluidHeight, width, fluidHeight, depth);
    }
  }

  /**
   * Colors and renders a fluid sprite
   * @param matrices    Matrix instance
   * @param screen  Parent screen
   * @param stack   Fluid stack
   * @param x       Fluid X
   * @param y       Fluid Y
   * @param width   Fluid width
   * @param height  Fluid height
   * @param depth   Fluid depth
   */
  public static void renderTiledFluid(MatrixStack matrices, HandledScreen<?> screen, FluidVolume stack, int x, int y, int width, int height, int depth) {
    if (!stack.isEmpty()) {
      FluidRenderHandler fluidRenderHandler = FluidRenderHandlerRegistry.INSTANCE.get(stack.getRawFluid());
      Sprite[] sprites = fluidRenderHandler.getFluidSprites(MinecraftClient.getInstance().world, MinecraftClient.getInstance().world == null ? null : BlockPos.ORIGIN, stack.getRawFluid().getDefaultState());

      RenderUtils.setColorRGBA(stack.getRenderColor());
      renderTiledTextureAtlas(matrices, screen, sprites[0], x, y, width, height, depth, stack.getFluidKey().gaseous);
      GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
    }
  }

  /**
   * Renders a texture atlas sprite tiled over the given area
   * @param matrices    Matrix instance
   * @param screen      Parent screen
   * @param sprite      Sprite to render
   * @param x           X position to render
   * @param y           Y position to render
   * @param width       Render width
   * @param height      Render height
   * @param depth       Render depth
   * @param upsideDown  If true, flips the sprite
   */
  public static void renderTiledTextureAtlas(MatrixStack matrices, HandledScreen<?> screen, Sprite sprite, int x, int y, int width, int height, int depth, boolean upsideDown) {
    // start drawing sprites
    screen.client.getTextureManager().bindTexture(sprite.getAtlas().getId());
    BufferBuilder builder = Tessellator.getInstance().getBuffer();
    builder.begin(GL11.GL_QUADS, VertexFormats.POSITION_TEXTURE);

    // tile vertically
    float u1 = sprite.getMinU();
    float v1 = sprite.getMinV();
    int spriteHeight = sprite.getHeight();
    int spriteWidth = sprite.getWidth();
    int startX = x + screen.x;
    int startY = y + screen.y;
    do {
      int renderHeight = Math.min(spriteHeight, height);
      height -= renderHeight;
      float v2 = sprite.getFrameV((16f * renderHeight) / spriteHeight);

      // we need to draw the quads per width too
      int x2 = startX;
      int widthLeft = width;
      Matrix4f matrix = matrices.peek().getModel();
      // tile horizontally
      do {
        int renderWidth = Math.min(spriteWidth, widthLeft);
        widthLeft -= renderWidth;

        float u2 = sprite.getFrameU((16f * renderWidth) / spriteWidth);
        if(upsideDown) {
          // FIXME: I think this causes tiling errors, look into it
          buildSquare(matrix, builder, x2, x2 + renderWidth, startY, startY + renderHeight, depth, u1, u2, v2, v1);
        } else {
          buildSquare(matrix, builder, x2, x2 + renderWidth, startY, startY + renderHeight, depth, u1, u2, v1, v2);
        }
        x2 += renderWidth;
      } while(widthLeft > 0);

      startY += renderHeight;
    } while(height > 0);

    // finish drawing sprites
    builder.end();
    RenderSystem.enableAlphaTest();
    BufferRenderer.draw(builder);
  }

  /**
   * Adds a square of texture to a buffer builder
   * @param builder  Builder instance
   * @param x1       X start
   * @param x2       X end
   * @param y1       Y start
   * @param y2       Y end
   * @param z        Depth
   * @param u1       Texture U start
   * @param u2       Texture U end
   * @param v1       Texture V start
   * @param v2       Texture V end
   */
  private static void buildSquare(Matrix4f matrix, BufferBuilder builder, int x1, int x2, int y1, int y2, int z, float u1, float u2, float v1, float v2) {
    builder.vertex(matrix, x1, y2, z).texture(u1, v2).next();
    builder.vertex(matrix, x2, y2, z).texture(u2, v2).next();
    builder.vertex(matrix, x2, y1, z).texture(u2, v1).next();
    builder.vertex(matrix, x1, y1, z).texture(u1, v1).next();
  }

  /**
   * Draws an upwards progress bar
   * @param element   Element to draw
   * @param x         X position to start
   * @param y         Y position to start
   * @param progress  Progress between 0 and 1
   */
  public static void drawProgressUp(MatrixStack matrices, ElementScreen element, int x, int y, float progress) {
    int height;
    if (progress > 1) {
      height = element.h;
    } else if (progress < 0) {
      height = 0;
    } else {
      // add an extra 0.5 so it rounds instead of flooring
      height = (int)(progress * element.h + 0.5);
    }
    // amount to offset element by for the height
    int deltaY = element.h - height;
    Screen.drawTexture(matrices, x, y + deltaY, element.x, element.y + deltaY, element.w, height, element.texW, element.texH);
  }

  /**
   * Renders a highlight overlay for the given area
   * @param matrices  Matrix instance
   * @param x         Element X position
   * @param y         Element Y position
   * @param width     Element width
   * @param height    Element height
   */
  public static void renderHighlight(MatrixStack matrices, int x, int y, int width, int height) {
      RenderSystem.disableDepthTest();
      RenderSystem.colorMask(true, true, true, false);
      DrawableHelper.fill(matrices, x, y, x + width, y + height, 0x80FFFFFF);
      RenderSystem.colorMask(true, true, true, true);
      RenderSystem.enableDepthTest();
  }
}
