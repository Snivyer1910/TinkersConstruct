package slimeknights.tconstruct.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.util.Identifier;

@Mixin(JsonUnbakedModel.class)
public interface JsonUnbakedModelAccessor {
  @Accessor
  Identifier getParentId();
  @Accessor
  JsonUnbakedModel getParent();
  @Accessor
  void setParentId(Identifier id);
  @Accessor
  void setParent(JsonUnbakedModel model);
}
