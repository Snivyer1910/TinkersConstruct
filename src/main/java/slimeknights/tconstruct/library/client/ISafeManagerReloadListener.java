package slimeknights.tconstruct.library.client;

import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;

/**
 * Same as {@link SynchronousResourceReloader}, but only runs if the mod loader state is valid
 */
public interface ISafeManagerReloadListener extends SynchronousResourceReloader {
  @Override
  default void reload(ResourceManager resourceManager) {
    onReloadSafe(resourceManager);
  }

  /**
   * Safely handle a resource manager reload. Only runs if the mod loading state is valid
   * @param resourceManager  Resource manager
   */
  void onReloadSafe(ResourceManager resourceManager);
}
