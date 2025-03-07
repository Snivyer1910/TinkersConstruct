package slimeknights.tconstruct.library;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.server.network.ServerPlayerEntity;

import slimeknights.tconstruct.library.exception.TinkerAPIMaterialException;
import slimeknights.tconstruct.library.materials.IMaterial;
import slimeknights.tconstruct.library.materials.MaterialId;
import slimeknights.tconstruct.library.materials.MaterialManager;
import slimeknights.tconstruct.library.materials.stats.IMaterialStats;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsManager;
import slimeknights.tconstruct.library.network.TinkerNetwork;
import slimeknights.tconstruct.library.traits.MaterialTraitsManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Holds all materials and the extra information registered for them (stat classes).
 * Materials are reset on every world load/join. Registered extra stuff is not.
 * <p>
 * For the Server, materials are loaded on server start/reload from the data packs.
 * For the Client, materials are synced from the server on server join.
 */
public class MaterialRegistryImpl implements IMaterialRegistry {

  private final MaterialManager materialManager;
  private final MaterialStatsManager materialStatsManager;
  private final MaterialTraitsManager materialTraitsManager;
  private final List<Runnable> onMaterialReload = new ArrayList<>();

  /**
   * Used for the defaults and for existence/class checks.
   * Basically all existing stat types need to be in this map.. or they don't exist
   */
  private Map<MaterialStatsId, IMaterialStats> materialStatDefaults = new HashMap<>();

  protected MaterialRegistryImpl(MaterialManager materialManager, MaterialStatsManager materialStatsManager, MaterialTraitsManager materialTraitsManager) {
    this.materialManager = materialManager;
    this.materialStatsManager = materialStatsManager;
    this.materialTraitsManager = materialTraitsManager;
//    FIXME: PORT these methods magicly dissapeared at some point? check the git history. i definitely remember fixing them
//    ServerPlayConnectionEvents.JOIN.register(materialManager::handleLogin);
//    ServerPlayConnectionEvents.JOIN.register(materialStatsManager::handleLogin);
  }

  @Override
  public IMaterial getMaterial(MaterialId id) {
    return materialManager.getMaterial(id).orElse(IMaterial.UNKNOWN);
  }

  @Override
  public IMaterial getMaterial(Fluid fluid) {
    return materialManager.getMaterial(fluid).orElse(IMaterial.UNKNOWN);
  }

  @Override
  public Collection<IMaterial> getMaterials() {
    return materialManager.getAllMaterials();
  }

  @Override
  public <T extends IMaterialStats> Optional<T> getMaterialStats(MaterialId materialId, MaterialStatsId statsId) {
    return materialStatsManager.getStats(materialId, statsId);
  }

  @Override
  public Collection<IMaterialStats> getAllStats(MaterialId materialId) {
    return materialStatsManager.getAllStats(materialId);
  }

  @Override
  public <T extends IMaterialStats> T getDefaultStats(MaterialStatsId statsId) {
    //noinspection unchecked
    return (T) materialStatDefaults.get(statsId);
  }

  @Override
  public <T extends IMaterialStats> void registerStatType(T defaultStats, Class<T> clazz) {
    MaterialStatsId statsId = defaultStats.getIdentifier();
    if (materialStatDefaults.containsKey(statsId)) {
      throw TinkerAPIMaterialException.materialStatsTypeRegisteredTwice(statsId);
    }
    // todo: implement check if class is compatible with the requirements for a network syncable stats class
    materialStatsManager.registerMaterialStat(statsId, clazz);
    materialStatDefaults.put(statsId, defaultStats);
  }

  /* Reloading */
  //TODO: fix this hydos I am to lazy to
  /** Called when the player logs in to send packets */
  private void handleLogin(/*PlayerLoggedInEvent event*/) {
    PlayerEntity player = null;//event.getPlayer();
    if (player instanceof ServerPlayerEntity) {
      ServerPlayerEntity serverPlayer = (ServerPlayerEntity)player;
      TinkerNetwork network = TinkerNetwork.getInstance();
      //PacketTarget target = PacketDistributor.PLAYER.with(() -> serverPlayer);
      //network.send(target, materialManager.getUpdatePacket());
      //network.send(target, materialStatsManager.getUpdatePacket());
    }
  }

  @Override
  public void addMaterialSyncListener(Runnable listener) {
    onMaterialReload.add(listener);
  }

  @Override
  public void onMaterialSync() {
    for (Runnable runnable : onMaterialReload) {
      runnable.run();
    }
  }
}
