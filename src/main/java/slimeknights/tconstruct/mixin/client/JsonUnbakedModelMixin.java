package slimeknights.tconstruct.mixin.client;

import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import slimeknights.mantle.client.model.fluid.FluidsModel;
import slimeknights.mantle.client.model.util.SimpleBlockModel;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ItemModelGenerator;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelElement;
import net.minecraft.client.render.model.json.ModelElementFace;
import net.minecraft.client.render.model.json.ModelOverride;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;

@Mixin(JsonUnbakedModel.class)
public abstract class JsonUnbakedModelMixin {

  @Shadow
  @Nullable
  protected Identifier parentId;

  @Shadow
  @Nullable
  protected JsonUnbakedModel parent;

  @Shadow
  @Final
  private static Logger LOGGER;

  @Shadow
  public abstract List<ModelElement> getElements();

  @Shadow
  public abstract SpriteIdentifier resolveSprite(String spriteName);

  @Shadow
  public String id;

  @Shadow
  @Final
  private List<ModelOverride> overrides;

  @Shadow
  public abstract JsonUnbakedModel getRootModel();

//  /**
//   * @author
//   */
//  @Overwrite
//  public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
//    Set<UnbakedModel> set = Sets.newLinkedHashSet();
//    for(JsonUnbakedModel jsonUnbakedModel = (JsonUnbakedModel) (Object) this; ((JsonUnbakedModelAccessor)jsonUnbakedModel).getParentId() != null && ((JsonUnbakedModelAccessor)jsonUnbakedModel).getParent() == null; jsonUnbakedModel = ((JsonUnbakedModelAccessor)jsonUnbakedModel).getParent()) {
//      set.add(jsonUnbakedModel);
//      UnbakedModel unbakedModel = unbakedModelGetter.apply(((JsonUnbakedModelAccessor)jsonUnbakedModel).getParentId());
//      if (unbakedModel == null) {
//        LOGGER.warn("No parent '{}' while loading model '{}'", this.parentId, jsonUnbakedModel);
//      }
//
//      if (set.contains(unbakedModel)) {
//        LOGGER.warn("Found 'parent' loop while loading model '{}' in chain: {} -> {}", jsonUnbakedModel, set.stream().map(Object::toString).collect(Collectors.joining(" -> ")), this.parentId);
//        unbakedModel = null;
//      }
//
//      if (unbakedModel == null) {
//        ((JsonUnbakedModelAccessor)jsonUnbakedModel).setParentId(ModelLoader.MISSING_ID);
//        unbakedModel = unbakedModelGetter.apply(((JsonUnbakedModelAccessor)jsonUnbakedModel).getParentId());
//      }
//
//      if (!(unbakedModel instanceof JsonUnbakedModel || unbakedModel instanceof FluidsModel || unbakedModel instanceof SimpleBlockModel)) {
//        throw new IllegalStateException("BlockModel parent has to be a block model.");
//      }
//      if(unbakedModel instanceof SimpleBlockModel) {
//        ((JsonUnbakedModelAccessor)jsonUnbakedModel).setParent(((SimpleBlockModel)unbakedModel).getParent());
//      }else if(unbakedModel instanceof FluidsModel) {
//        ((JsonUnbakedModelAccessor)jsonUnbakedModel).setParent(((FluidsModel)unbakedModel)g);
//      }else {
//        ((JsonUnbakedModelAccessor)jsonUnbakedModel).setParent((JsonUnbakedModel)unbakedModel);
//      }
//    }
//
//    Set<SpriteIdentifier> set2 = Sets.newHashSet((this.resolveSprite("particle")));
//    Iterator var6 = this.getElements().iterator();
//
//    while(var6.hasNext()) {
//      ModelElement modelElement = (ModelElement)var6.next();
//
//      SpriteIdentifier spriteIdentifier;
//      for(Iterator var8 = modelElement.faces.values().iterator(); var8.hasNext(); set2.add(spriteIdentifier)) {
//        ModelElementFace modelElementFace = (ModelElementFace)var8.next();
//        spriteIdentifier = this.resolveSprite(modelElementFace.textureId);
//        if (Objects.equals(spriteIdentifier.getTextureId(), MissingSprite.getMissingSpriteId())) {
//          unresolvedTextureReferences.add(Pair.of(modelElementFace.textureId, this.id));
//        }
//      }
//    }
//
//    this.overrides.forEach((modelOverride) -> {
//      UnbakedModel unbakedModel = unbakedModelGetter.apply(modelOverride.getModelId());
//      if (!Objects.equals(unbakedModel, this)) {
//        set2.addAll(unbakedModel.getTextureDependencies(unbakedModelGetter, unresolvedTextureReferences));
//      }
//    });
//    if (this.getRootModel() == ModelLoader.GENERATION_MARKER) {
//      ItemModelGenerator.LAYERS.forEach((string) -> {
//        set2.add(this.resolveSprite(string));
//      });
//    }
//
//    return set2;
//  }
}
