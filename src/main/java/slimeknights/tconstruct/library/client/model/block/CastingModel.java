package slimeknights.tconstruct.library.client.model.block;

import com.google.gson.JsonObject;
import lombok.Getter;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import org.apache.logging.log4j.LogManager;
import slimeknights.mantle.client.model.HBMABFIB;
import slimeknights.mantle.client.model.JsonModelResourceProvider;
import slimeknights.mantle.client.model.fluid.FluidCuboid;
import slimeknights.mantle.client.model.inventory.InventoryModel;
import slimeknights.mantle.client.model.inventory.ModelItem;
import slimeknights.mantle.client.model.util.SimpleBlockModel;
import slimeknights.tconstruct.TConstruct;
import java.util.List;
import java.util.function.Function;

import net.minecraft.client.realms.util.JsonUtils;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

/**
 * This model contains a single fluid region that is scaled in the TESR, and a list of two items displayed in the TESR
 */
public class CastingModel extends InventoryModel {
  /** Shared loader instance */
  public static final Loader LOADER = new Loader();

  private final FluidCuboid fluid;

  @SuppressWarnings("WeakerAccess")
  protected CastingModel(SimpleBlockModel model, List<ModelItem> items, FluidCuboid fluid) {
    super(model, items, null);
    this.fluid = fluid;
  }

  @Override
  public net.minecraft.client.render.model.BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
    net.minecraft.client.render.model.BakedModel baked = model.bakeModel(owner, rotationContainer, ModelOverrideList.EMPTY, textureGetter, modelId);
    return new CastingModel.BakedModel(baked, items, fluid);
  }

  /** Baked model, mostly a data wrapper around a normal model */
  public static class BakedModel extends InventoryModel.MantleBakedModel {
    @Getter
    private final FluidCuboid fluid;
    private BakedModel(net.minecraft.client.render.model.BakedModel originalModel, List<ModelItem> items, FluidCuboid fluid) {
      super(originalModel, items);
      this.fluid = fluid;
    }
  }

  /** Loader for this model */
  public static class Loader extends JsonModelResourceProvider {

    /**
     * Shared loader instance
     */
    public static final CastingModel.Loader INSTANCE = new CastingModel.Loader();

    public Loader() {
      super(new Identifier(TConstruct.modID, "casting"));
    }

    @Override
    public UnbakedModel loadJsonModelResource(Identifier resourceId, JsonObject modelContents, ModelProviderContext context) {
      LogManager.getLogger().info(HBMABFIB.getModelSafe(resourceId));
      SimpleBlockModel model = SimpleBlockModel.deserialize(getContext(), modelContents, HBMABFIB.getModelSafe(resourceId));
      List<ModelItem> items = ModelItem.listFromJson(modelContents, "items");
      LogManager.getLogger().info("");
      FluidCuboid fluid = FluidCuboid.fromJson(JsonHelper.asObject(modelContents, "fluid"));
      return new CastingModel(model, items, fluid);
    }
  }
}
