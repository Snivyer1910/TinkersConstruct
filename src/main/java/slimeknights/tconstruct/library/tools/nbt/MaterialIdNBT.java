package slimeknights.tconstruct.library.tools.nbt;

import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import org.jetbrains.annotations.Nullable;
import slimeknights.tconstruct.library.materials.IMaterial;
import slimeknights.tconstruct.library.materials.MaterialId;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Similar to {@link MaterialNBT}, but does not check materials against the registry.
 * Used for rendering so we can have client side only materials for UIs. Anything logic based should use the regular material NBT
 */
@EqualsAndHashCode
@ToString
public class MaterialIdNBT {
  /** Instance containing no materials, for errors with parsing NBT */
  final static MaterialIdNBT EMPTY = new MaterialIdNBT(ImmutableList.of());

  /** List of materials contained in this NBT */
  @Getter
  private final List<MaterialId> materials;

  /** Creates a new material NBT */
  public MaterialIdNBT(List<MaterialId> materials) {
    this.materials = ImmutableList.copyOf(materials);
  }

  /**
   * Gets the material at the given index
   * @param index  Index
   * @return  Material, or unknown if index is invalid
   */
  public MaterialId getMaterial(int index) {
    if (index >= materials.size() || index < 0) {
      return IMaterial.UNKNOWN_ID;
    }
    return materials.get(index);
  }

  /**
   * Parses the material list from NBT
   * @param nbt  NBT instance
   * @return  MaterialNBT instance
   */
  public static MaterialIdNBT readFromNBT(@Nullable NbtElement nbt) {
    if (nbt == null || nbt.getType() != NbtType.LIST) {
      return EMPTY;
    }
    NbtList listNBT = (NbtList) nbt;
    if (listNBT.getHeldType() != NbtType.STRING) {
      return EMPTY;
    }

    List<MaterialId> materials = listNBT.stream()
      .map(NbtElement::asString)
      .map(MaterialId::tryParse)
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
    return new MaterialIdNBT(materials);
  }

  /**
   * Writes this material list to NBT
   * @return  List of materials
   */
  public NbtList serializeToNBT() {
    return materials.stream()
                    .map(MaterialId::toString)
                    .map(NbtString::of)
                    .collect(Collectors.toCollection(NbtList::new));
  }

  /**
   * Parses the material list from a stack
   * @param stack  Tool stack instance
   * @return  MaterialNBT instance
   */
  public static MaterialIdNBT from(ItemStack stack) {
    NbtCompound nbt = stack.getTag();
    if (nbt != null) {
      return readFromNBT(nbt.getList(ToolStack.TAG_MATERIALS, NbtType.STRING));
    }
    return EMPTY;
  }

  /** Writes this material list to the given stack */
  public ItemStack updateStack(ItemStack stack) {
    stack.getOrCreateTag().put(ToolStack.TAG_MATERIALS, serializeToNBT());
    return stack;
  }
}
