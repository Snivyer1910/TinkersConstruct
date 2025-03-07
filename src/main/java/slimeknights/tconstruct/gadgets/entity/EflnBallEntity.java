package slimeknights.tconstruct.gadgets.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import slimeknights.tconstruct.gadgets.Exploder;
import slimeknights.tconstruct.gadgets.TinkerGadgets;

public class EflnBallEntity extends ThrownItemEntity {

  public EflnBallEntity(EntityType<? extends EflnBallEntity> p_i50159_1_, World p_i50159_2_) {
    super(p_i50159_1_, p_i50159_2_);
  }

  public EflnBallEntity(World worldIn, LivingEntity throwerIn) {
    super(TinkerGadgets.eflnEntity, throwerIn, worldIn);
  }

  public EflnBallEntity(World worldIn, double x, double y, double z) {
    super(TinkerGadgets.eflnEntity, x, y, z, worldIn);
  }

  @Override
  protected Item getDefaultItem() {
    return TinkerGadgets.efln.get();
  }

  @Override
  protected void onCollision(HitResult result) {
    if (!this.world.isClient) {
      EFLNExplosion explosion = new EFLNExplosion(this.world, this, null, null, this.getX(), this.getY(), this.getZ(), 6f, false, Explosion.DestructionType.NONE);
      Exploder.startExplosion(this.world, explosion, this, new BlockPos(this.getX(), this.getY(), this.getZ()), 6f, 6f);
    }

    if (!this.world.isClient) {
      this.world.sendEntityStatus(this, (byte) 3);
      this.remove();
    }
  }

//  @Override
//  public void writeSpawnData(PacketByteBuf buffer) {
//    buffer.writeItemStack(this.getItem());
//  }
//
//  @Override
//  public void readSpawnData(PacketByteBuf additionalData) {
//    this.setItem(additionalData.readItemStack());
//  }
//
//  @NotNull
//  @Override
//  public Packet<?> createSpawnPacket() {
//    return NetworkHooks.getEntitySpawningPacket(this);
//  }
}
