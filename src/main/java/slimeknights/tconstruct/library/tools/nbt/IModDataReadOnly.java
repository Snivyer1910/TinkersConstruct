package slimeknights.tconstruct.library.tools.nbt;

import java.util.function.BiFunction;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;

/**
 * Read only view of {@link ModDataNBT}
 */
public interface IModDataReadOnly {
  /** Empty variant of mod data */
  IModDataReadOnly EMPTY = new IModDataReadOnly() {
    @Override
    public int getUpgrades() {
      return 0;
    }

    @Override
    public int getAbilities() {
      return 0;
    }

    @Override
    public <T> T get(Identifier name, BiFunction<NbtCompound,String,T> function) {
      return function.apply(new NbtCompound(), name.toString());
    }

    @Override
    public boolean contains(Identifier name, int type) {
      return false;
    }
  };

  /** Gets the number of modifiers provided by this data */
  int getUpgrades();

  /** Gets the number of ability slots provided by this data */
  int getAbilities();


  /**
   * Gets a namespaced key from NBT
   * @param name      Namedspaced key
   * @param function  Function to get data using the key
   * @param <T>  NBT type of output
   * @return  Data based on the function
   */
  <T> T get(Identifier name, BiFunction<NbtCompound,String,T> function);

  /**
   * Checks if the data contains the given tag
   * @param name  Namespaced key
   * @param type  Tag type, see {@link net.minecraftforge.common.util.Constants.NBT} for values
   * @return  True if the tag is contained
   */
  boolean contains(Identifier name, int type);


  /* Helpers */

  /**
   * Reads an generic NBT value from the mod data
   * @param name  Name
   * @return  Integer value
   */
  default NbtElement get(Identifier name) {
    return get(name, NbtCompound::get);
  }

  /**
   * Reads an integer from the mod data
   * @param name  Name
   * @return  Integer value
   */
  default int getInt(Identifier name) {
    return get(name, NbtCompound::getInt);
  }

  /**
   * Reads an boolean from the mod data
   * @param name  Name
   * @return  Boolean value
   */
  default boolean getBoolean(Identifier name) {
    return get(name, NbtCompound::getBoolean);
  }

  /**
   * Reads an float from the mod data
   * @param name  Name
   * @return  Float value
   */
  default float getFloat(Identifier name) {
    return get(name, NbtCompound::getFloat);
  }

  /**
   * Reads a string from the mod data
   * @param name  Name
   * @return  String value
   */
  default String getString(Identifier name) {
    return get(name, NbtCompound::getString);
  }

  /**
   * Reads a compound from the mod data
   * @param name  Name
   * @return  Compound value
   */
  default NbtCompound getCompound(Identifier name) {
    return get(name, NbtCompound::getCompound);
  }
}
