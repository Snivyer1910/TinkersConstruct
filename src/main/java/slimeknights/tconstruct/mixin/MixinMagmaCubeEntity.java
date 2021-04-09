package slimeknights.tconstruct.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import slimeknights.tconstruct.shared.BlockEvents;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MagmaCubeEntity;

@Mixin(MagmaCubeEntity.class)
public class MixinMagmaCubeEntity {
  @Inject(method = "jump",at = @At("TAIL"))
  public void jump(CallbackInfo ci) {
    BlockEvents.onLivingJump((LivingEntity)(Object)this);
  }
}
