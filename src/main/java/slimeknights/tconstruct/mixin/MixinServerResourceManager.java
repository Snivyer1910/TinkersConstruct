package slimeknights.tconstruct.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import slimeknights.tconstruct.common.recipe.RecipeCacheInvalidator;

import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.resource.ServerResourceManager;
import net.minecraft.server.command.CommandManager;

@Mixin(ServerResourceManager.class)
public class MixinServerResourceManager {

  @Shadow
  @Final
  private ReloadableResourceManager resourceManager;

  @Inject(method = "<init>",at = @At("TAIL"))
  public void init(CommandManager.RegistrationEnvironment registrationEnvironment, int i, CallbackInfo ci) {
    this.resourceManager.registerListener(RecipeCacheInvalidator.onReloadListenerReload());
  }
}
