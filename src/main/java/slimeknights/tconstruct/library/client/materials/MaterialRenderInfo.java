package slimeknights.tconstruct.library.client.materials;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import slimeknights.tconstruct.library.materials.MaterialId;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Determines the type of texture used for rendering a specific material
 */
@RequiredArgsConstructor
public class MaterialRenderInfo {
  /** ID of this render info */
  @Getter
  private final MaterialId identifier;
  private final Identifier texture;
  private final String[] fallbacks;
  /* color used to tint this model as an item colors handler */
  @Getter
  private final int vertexColor;

  /**
   * Tries to get a sprite for the given texture
   * @param base           Base texture
   * @param suffix         Sprite suffix
   * @param spriteGetter   Logic to get the sprite
   * @return  Sprite if valid, null if missing
   */
  @Nullable
  private Sprite trySprite(SpriteIdentifier base, String suffix, Function<SpriteIdentifier,Sprite> spriteGetter) {
    Sprite sprite = spriteGetter.apply(getMaterial(base.getTextureId(), suffix));
    if (!MissingSprite.getMissingSpriteId().equals(sprite.getId())) {
      return sprite;
    }
    return null;
  }

  /**
   * Gets the texture for this render material
   * @param base          Base texture
   * @param spriteGetter  Logic to get a sprite
   * @return  Pair of the sprite, and a boolean indicating whether the sprite should be tinted
   */
  public TintedSprite getSprite(SpriteIdentifier base, Function<SpriteIdentifier,Sprite> spriteGetter) {
    Sprite sprite = trySprite(base, getSuffix(texture), spriteGetter);
    if (sprite != null) {
      return TintedSprite.of(sprite, false);
    }
    for (String fallback : fallbacks) {
      sprite = trySprite(base, fallback, spriteGetter);
      if (sprite != null) {
        return TintedSprite.of(sprite, true);
      }
    }
    return TintedSprite.of(spriteGetter.apply(base), true);
  }

  /**
   * Gets all dependencies for this render info
   * @param textures  Texture consumer
   * @param base      Base texture, will be used to generate texture names
   */
  public void getTextureDependencies(Consumer<SpriteIdentifier> textures, SpriteIdentifier base) {
    textures.accept(getMaterial(base.getTextureId(), getSuffix(texture)));
    for (String fallback : fallbacks) {
      textures.accept(getMaterial(base.getTextureId(), fallback));
    }
  }

  /**
   * Converts a material ID into a sprite suffix
   * @param material  Material ID
   * @return  Sprite name
   */
  private static String getSuffix(Identifier material) {
    // namespace will only be minecraft for a texture override, so this lets you select to always use an untinted base texture as the materials texture
    if ("data/minecraft".equals(material.getNamespace())) {
      return material.getPath();
    }
    return material.getNamespace() + "_" + material.getPath();
  }

  /**
   * Gets a material for the given resource locations
   * @param texture   Texture path
   * @param suffix    Material or fallback suffix name
   * @return  Material instance
   */
  private static SpriteIdentifier getMaterial(Identifier texture, String suffix) {
    //throw new RuntimeException("CRAB!");
    //TODO: PORT
    return new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(texture.getNamespace(), texture.getPath() + "_" + suffix));
    //return ModelLoaderRegistry.blockMaterial(new Identifier(texture.getNamespace(), texture.getPath() + "_" + suffix));
  }

  /** Data class for a sprite that may be tinted */
  @Data(staticConstructor = "of")
  public static class TintedSprite {
    private final Sprite sprite;
    private final boolean isTinted;
  }
}
