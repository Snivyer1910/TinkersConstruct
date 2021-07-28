package slimeknights.tconstruct.mixin.client;

import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.util.Identifier;

import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import slimeknights.mantle.client.model.fluid.FluidsModel;
import slimeknights.mantle.client.model.util.SimpleBlockModel;
import java.util.ArrayList;
import java.util.Collection;

@Mixin(ModelLoader.class)
public class ModelLoaderMixin {

  @Shadow
  @Final
  private static Logger LOGGER;

  @Inject(method = "getOrLoadModel", at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V"), locals = LocalCapture.CAPTURE_FAILSOFT, remap = false)
  private void logErrorsBecauseIntellijSucks(Identifier id, CallbackInfoReturnable<UnbakedModel> cir, UnbakedModel unbakedModel, Identifier identifier, Exception exception) {
    exception.printStackTrace();
  }

}
