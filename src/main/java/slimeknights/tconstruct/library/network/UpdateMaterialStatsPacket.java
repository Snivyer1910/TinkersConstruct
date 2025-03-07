package slimeknights.tconstruct.library.network;

import lombok.Getter;
import org.apache.logging.log4j.Logger;
import slimeknights.mantle.network.packet.IThreadsafePacket;
import slimeknights.tconstruct.library.MaterialRegistry;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.materials.MaterialId;
import slimeknights.tconstruct.library.materials.stats.IMaterialStats;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;

@Getter
public class UpdateMaterialStatsPacket extends IThreadsafePacket {
  private static final Logger log = Util.getLogger("NetworkSync");

  protected final Map<MaterialId, Collection<IMaterialStats>> materialToStats;

  public UpdateMaterialStatsPacket(PacketByteBuf buffer) {
    this(buffer, MaterialRegistry::getClassForStat);
  }

  public UpdateMaterialStatsPacket(PacketByteBuf buffer, Function<MaterialStatsId, Class<?>> classResolver) {
    super(null);
    int materialCount = buffer.readInt();
    materialToStats = new HashMap<>(materialCount);
    for (int i = 0; i < materialCount; i++) {
      MaterialId id = new MaterialId(buffer.readIdentifier());
      int statCount = buffer.readInt();
      List<IMaterialStats> statList = new ArrayList<>();
      for (int j = 0; j < statCount; j++) {
        decodeStat(buffer, classResolver).ifPresent(statList::add);
      }
      materialToStats.put(id, statList);
    }
  }

  /**
   * Decodes a single stat
   * @param buffer         Buffer instance
   * @param classResolver  Stat to decode
   * @return
   */
  private Optional<IMaterialStats> decodeStat(PacketByteBuf buffer, Function<MaterialStatsId, Class<?>> classResolver) {
    MaterialStatsId statsId = new MaterialStatsId(buffer.readIdentifier());
    try {
      Class<?> clazz = classResolver.apply(statsId);
      IMaterialStats stats = (IMaterialStats) clazz.newInstance();
      stats.decode(buffer);
      return Optional.of(stats);
    } catch (Exception e) {
      log.error("Could not load class for deserialization of stats {}. Are client and server in sync?", statsId, e);
      return Optional.empty();
    }
  }

  @Override
  public void encode(PacketByteBuf buffer) {
    buffer.writeInt(materialToStats.size());
    materialToStats.forEach((materialId, stats) -> {
      buffer.writeIdentifier(materialId);
      buffer.writeInt(stats.size());
      stats.forEach(stat -> encodeStat(buffer, stat));
    });
  }

  /**
   * Encodes a single material stat
   * @param buffer  Buffer instance
   * @param stat    Stat to encode
   */
  private void encodeStat(PacketByteBuf buffer, IMaterialStats stat) {
    buffer.writeIdentifier(stat.getIdentifier());
    stat.encode(buffer);
  }

  @Override
  public void handleThreadsafe(PlayerEntity playert) {
    MaterialRegistry.updateMaterialStatsFromServer(this);
  }
}
