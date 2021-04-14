package slimeknights.tconstruct.smeltery.client.render;

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayer.MultiPhaseParameters;
import net.minecraft.client.render.RenderPhase.Texture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Matrix4f;
import slimeknights.mantle.client.render.FluidRenderer;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.smeltery.client.inventory.module.GuiSmelteryTank;
import slimeknights.tconstruct.smeltery.tileentity.tank.SmelteryTank;

import java.util.List;

/** Helper class to render the smeltery tank */
public class SmelteryTankRenderer {
  /** Like {@link FluidRenderer#RENDER_TYPE}, but disables cull so both sides show */
  private static final RenderLayer RENDER_TYPE = RenderLayer.of(
    Util.resource("smeltery_fluid"), VertexFormats.POSITION_COLOR_TEXTURE_LIGHT, 7, 256, true, false,
    MultiPhaseParameters.builder()
         .texture(new Texture(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, false, false))
         .shadeModel(RenderLayer.SMOOTH_SHADE_MODEL)
         .lightmap(RenderLayer.ENABLE_LIGHTMAP)
         .texture(RenderLayer.MIPMAP_BLOCK_ATLAS_TEXTURE)
         .transparency(RenderLayer.TRANSLUCENT_TRANSPARENCY)
         .cull(RenderLayer.DISABLE_CULLING)
         .build(false));

  /** Distance between the liquid and the edge of the block */
  private static final float FLUID_OFFSET = 0.005f;
  /** Amount to subtract from the height for fluid offset */
  private static final int HEIGHT_OFFSET = (int) (FLUID_OFFSET * 2000d);

  /**
   * Gets the integer bounds for rendering a fluid with the given delta
   * @param delta  Delta
   * @return  Position array
   */
  private static float[] getBlockBounds(int delta) {
    return getBlockBounds(delta, FLUID_OFFSET, delta + 1f - FLUID_OFFSET);
  }

  /**
   * Gets the integer bounds for rendering a fluid with the given delta
   * @param delta  Delta
   * @param start  Start position
   * @param end    End position
   * @return  Position array
   */
  private static float[] getBlockBounds(int delta, float start, float end) {
    float[] bounds = new float[2 + delta];
    bounds[0] = start;
    int offset = (int) start;
    for(int i = 1; i <= delta; i++) bounds[i] = i + offset;
    bounds[delta+1] = end;
    return bounds;
  }

  /**
   * Renders the smeltery tank fluids, relative to tankMinPos
   * @param matrices    Matrix stack instance
   * @param buffer      Buffer instance
   * @param tank        Smeltery tank
   * @param brightness  Packed lighting values
   * @param tankMinPos  Min position for fluid rendering
   * @param tankMaxPos  Max position for fluid rendering
   */
  public static void renderFluids(MatrixStack matrices, VertexConsumerProvider buffer, SmelteryTank tank,
                                  BlockPos tankMinPos, BlockPos tankMaxPos, int brightness) {
    List<FluidVolume> fluids = tank.getFluids();
    // empty smeltery :(
    if(!fluids.isEmpty()) {
      // determine x and z bounds, constant
      int xd = tankMaxPos.getX() - tankMinPos.getX();
      int zd = tankMaxPos.getZ() - tankMinPos.getZ();
      float[] xBounds = getBlockBounds(xd);
      float[] zBounds = getBlockBounds(zd);

      // calc heights, we use mB capacities and then convert it over to blockheights during rendering
      int yd = 1 + Math.max(0, tankMaxPos.getY() - tankMinPos.getY());
      // one block height = 1000 mb
      int[] heights = GuiSmelteryTank.calcLiquidHeights(fluids, FluidAmount.of1620(tank.getCapacity()), yd * 1000 - HEIGHT_OFFSET, 100);

      // rendering time
      VertexConsumer builder = buffer.getBuffer(RENDER_TYPE);
      float curY = FLUID_OFFSET;
      for (int i = 0; i < fluids.size(); i++) {
        float h = (float) heights[i] / 1000f;
        renderLargeFluidCuboid(matrices, builder, fluids.get(i), brightness, xd, xBounds, zd, zBounds, curY, curY + h);
        curY += h;
      }
    }
  }

  /**
   * Renders a large fluid cuboid
   * @param matrices   Matrix stack intance
   * @param builder    Builder instance
   * @param fluid      Fluid to render
   * @param brightness Packed lighting values
   * @param xd         X size for renderer
   * @param xBounds    X positions to render
   * @param zd         Z size for renderer
   * @param zBounds    Z positions to render
   * @param yMin       Min y position
   * @param yMax       Max y position
   */
  private static void renderLargeFluidCuboid(MatrixStack matrices, VertexConsumer builder, FluidVolume fluid, int brightness,
                                             int xd, float[] xBounds, int zd, float[] zBounds, float yMin, float yMax) {
    throw new RuntimeException("CRAB!"); // FIXME: PORT
    /*if(yMin >= yMax || fluid.isEmpty()) {
      return;
    }
    // fluid attributes
    FluidAttributes attributes = fluid.getFluid().getAttributes();
    Sprite still = FluidRenderer.getBlockSprite(attributes.getStillTexture(fluid));
    int color = attributes.getColor(fluid);
    brightness = FluidRenderer.withBlockLight(brightness, attributes.getLuminosity(fluid));
    boolean upsideDown = attributes.isGaseous(fluid);

    // the liquid can stretch over more blocks than the subtracted height is if yMin's decimal is bigger than yMax's decimal (causing UV over 1)
    // ignoring the decimals prevents this, as yd then equals exactly how many ints are between the two
    // for example, if yMax = 5.1 and yMin = 2.3, 2.8 (which rounds to 2), with the face array becoming 2.3, 3, 4, 5.1
    int yd = (int) (yMax - (int) yMin);
    // except in the rare case of yMax perfectly aligned with the block, causing the top face to render multiple times
    // for example, if yMax = 3 and yMin = 1, the values of the face array become 1, 2, 3, 3 as we then have middle ints
    if (yMax % 1d == 0) yd--;
    float[] yBounds = getBlockBounds(yd, yMin, yMax);

    // render each side
    Matrix4f matrix = matrices.peek().getModel();
    Vector3f from = new Vector3f();
    Vector3f to = new Vector3f();
    int rotation = upsideDown ? 180 : 0;
    for(int y = 0; y <= yd; y++) {
      for(int z = 0; z <= zd; z++) {
        for(int x = 0; x <= xd; x++) {
          from.set(xBounds[x], yBounds[y], zBounds[z]);
          to.set(xBounds[x + 1], yBounds[y + 1], zBounds[z + 1]);
          if (x == 0)  FluidRenderer.putTexturedQuad(builder, matrix, still, from, to, Direction.WEST,  color, brightness, rotation, false);
          if (x == xd) FluidRenderer.putTexturedQuad(builder, matrix, still, from, to, Direction.EAST,  color, brightness, rotation, false);
          if (z == 0)  FluidRenderer.putTexturedQuad(builder, matrix, still, from, to, Direction.NORTH, color, brightness, rotation, false);
          if (z == zd) FluidRenderer.putTexturedQuad(builder, matrix, still, from, to, Direction.SOUTH, color, brightness, rotation, false);
          if (y == yd) FluidRenderer.putTexturedQuad(builder, matrix, still, from, to, Direction.UP,    color, brightness, rotation, false);
          if (y == 0) {
            // increase Y position slightly to prevent z fighting on neighboring fluids
            from.setY(from.getY() + 0.001f);
            FluidRenderer.putTexturedQuad(builder, matrix, still,   from, to, Direction.DOWN,  color, brightness, rotation, false);
          }
        }
      }
    }*/
  }
}
