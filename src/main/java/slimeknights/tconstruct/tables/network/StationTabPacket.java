package slimeknights.tconstruct.tables.network;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import org.apache.logging.log4j.LogManager;
import slimeknights.mantle.network.packet.IThreadsafePacket;
import slimeknights.tconstruct.library.network.TinkerNetwork;
import slimeknights.tconstruct.tables.block.ITinkerStationBlock;

import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.ScreenHandlerPropertyUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StationTabPacket extends IThreadsafePacket {

  private BlockPos pos;

  public StationTabPacket(BlockPos blockPos) {
    super(null);
    this.pos = blockPos;
  }

  public StationTabPacket(PacketByteBuf buffer) {
    super(buffer);
    this.pos = buffer.readBlockPos();
  }

  @Override
  public void encode(PacketByteBuf buffer) {
    buffer.writeBlockPos(pos);
  }

  @Override
  public void handleThreadsafe(PlayerEntity player) {
    ServerPlayerEntity sender = (ServerPlayerEntity) player;
    if (sender != null) {
      ItemStack heldStack = sender.inventory.getCursorStack();
      if (!heldStack.isEmpty()) {
        // set it to empty, so it's doesn't get dropped
        sender.inventory.setCursorStack(ItemStack.EMPTY);
      }

      World world = sender.getEntityWorld();
      if (!world.isChunkLoaded(pos)) {
        return;
      }
      BlockState state = sender.getEntityWorld().getBlockState(pos);
      if (state.getBlock() instanceof ITinkerStationBlock) {
        ((ITinkerStationBlock) state.getBlock()).openGui(sender, sender.getEntityWorld(), pos);
      } else {

        NamedScreenHandlerFactory provider = state.createScreenHandlerFactory(sender.getEntityWorld(), pos);
        if (provider != null) {

          ((ITinkerStationBlock) state.getBlock()).openGui(sender, sender.getEntityWorld(), pos);
          //throw new RuntimeException("CRAB!");
          //TODO: PORT
          //NetworkHooks.openGui(sender, provider, pos);
        }

        if (!heldStack.isEmpty()) {
          sender.inventory.setCursorStack(heldStack);
          TinkerNetwork.getInstance().sendVanillaPacket(new ScreenHandlerSlotUpdateS2CPacket(-1, -1, heldStack), player);
        }
      }
    }
  }
}
