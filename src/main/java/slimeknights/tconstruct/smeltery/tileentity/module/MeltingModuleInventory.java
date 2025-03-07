package slimeknights.tconstruct.smeltery.tileentity.module;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.PropertyDelegate;
import slimeknights.mantle.tileentity.MantleTileEntity;
import slimeknights.tconstruct.fluids.IFluidHandler;
import slimeknights.tconstruct.misc.IItemHandler;
import slimeknights.tconstruct.misc.ItemHandlerHelper;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Predicate;

/**
 * Inventory composite made of a set of melting module inventories
 */
public class MeltingModuleInventory implements IItemHandler, Inventory {//IItemHandlerModifiable {
  private static final String TAG_SLOT = "slot";
  private static final String TAG_ITEMS = "items";
  private static final String TAG_SIZE = "size";

  /** Parent tile entity */
  private final MantleTileEntity parent;
  /** Fluid handler for outputs */
  private final IFluidHandler fluidHandler;
  /** Array of modules containing each slot */
  private MeltingModule[] modules;
  /** If true, module cannot be resized */
  private final boolean strictSize;
  /** Number of nuggets to produce when melting an ore */
  private final IntSupplier nuggetsPerOre;
  /** Cache of predicate for fluid output */
  private final Predicate<FluidVolume> outputFunction = this::tryFillTank;

  /**
   * Creates a new inventory with a fixed size
   * @param parent         Parent tile
   * @param fluidHandler   Tank for output
   * @param nuggetsPerOre  Number of nuggets to produce from an ore block
   * @param size           Size
   */
  public MeltingModuleInventory(MantleTileEntity parent, IFluidHandler fluidHandler, IntSupplier nuggetsPerOre, int size) {
    this.parent = parent;
    this.fluidHandler = fluidHandler;
    this.modules = new MeltingModule[size];
    this.nuggetsPerOre = nuggetsPerOre;
    this.strictSize = size != 0;
  }

  /**
   * Creates a new inventory with a variable size
   * @param parent         Parent tile
   * @param fluidHandler   Tank for output
   * @param nuggetsPerOre  Number of nuggets to produce from an ore block
   */
  public MeltingModuleInventory(MantleTileEntity parent, IFluidHandler fluidHandler, IntSupplier nuggetsPerOre) {
    this(parent, fluidHandler, nuggetsPerOre, 0);
  }

  /* Properties */

  @Override
  public int getSlots() {
    return modules.length;
  }

  /**
   * Checks if the given slot index is valid
   * @param slot  Slot index to check
   * @return  True if valid
   */
  public boolean validSlot(int slot) {
    return slot >= 0 && slot < getSlots();
  }

  @Override
  public int getSlotLimit(int slot) {
    return 1;
  }

  @Override
  public boolean isItemValid(int slot, ItemStack stack) {
    return true;
  }

  /** Returns true if a slot is defined in the array */
  private boolean hasModule(int slot) {
    return validSlot(slot) && modules[slot] != null;
  }

  /**
   * Gets the current time of a slot
   * @param slot  Slot index
   * @return  Slot temperature
   */
  public int getCurrentTime(int slot) {
    return hasModule(slot) ? modules[slot].getCurrentTime() : 0;
  }

  /**
   * Gets the required time for a slot
   * @param slot  Slot index
   * @return  Required time
   */
  public int getRequiredTime(int slot) {
    return hasModule(slot) ? modules[slot].getRequiredTime() : 0;
  }

  /**
   * Gets the required temperature for a slot
   * @param slot  Slot index
   * @return  Required temperature
   */
  public int getRequiredTemp(int slot) {
    return hasModule(slot) ? modules[slot].getRequiredTemp() : 0;
  }


  /* Sub modules */

  /**
   * Gets the module for the given index
   * @param slot  Index
   * @return  Module for index
   * @throws IndexOutOfBoundsException  index is invalid
   */
  public MeltingModule getModule(int slot) {
    if (!validSlot(slot)) {
      throw new IndexOutOfBoundsException();
    }
    if (modules[slot] == null) {
      modules[slot] = new MeltingModule(parent, outputFunction, nuggetsPerOre, slot);
    }
    return modules[slot];
  }

  /**
   * Resizes the module to a new size
   * @param newSize        New size
   * @param stackConsumer  Consumer for any stacks that no longer fit
   * @throws IllegalStateException  If this inventory cannot be resized
   */
  public void resize(int newSize, Consumer<ItemStack> stackConsumer) {
    if (strictSize) {
      throw new IllegalStateException("Cannot resize this melting module inventory");
    }
    // nothing to do
    if (newSize == modules.length) {
      return;
    }
    // if shrinking, drop extra items
    if (newSize < modules.length) {
      for (int i = newSize; i < modules.length; i++) {
        if (modules[i] != null && !modules[i].getStack().isEmpty()) {
          stackConsumer.accept(modules[i].getStack());
        }
      }
    }

    // resize the module array
    modules = Arrays.copyOf(modules, newSize);
    parent.markDirtyFast();
  }


  /* Item handling */

  @Override
  public ItemStack getStackInSlot(int slot) {
    if (validSlot(slot)) {
      // don't create the slot, just reading
      if (modules[slot] != null) {
        return modules[slot].getStack();
      }
    }
    return ItemStack.EMPTY;
  }

//  @Override
  public void setStackInSlot(int slot, ItemStack stack) {
    // actually set the stack
    if (validSlot(slot)) {
      if (stack.isEmpty()) {
        if (modules[slot] != null) {
          modules[slot].setStack(ItemStack.EMPTY);
        }
      } else {
        // validate size
        if (stack.getCount() > 1) {
          stack.setCount(1);
        }
        getModule(slot).setStack(stack);
      }
    }
  }

  @Override
  public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
    if (stack.isEmpty()) {
      return ItemStack.EMPTY;
    }
    if (slot < 0 || slot >= getSlots()) {
      return stack;
    }

    // if the slot is empty, we can insert. Ignores stack sizes at this time, assuming always 1
    MeltingModule module = getModule(slot);
    boolean canInsert = module.getStack().isEmpty();
    if (!simulate && canInsert) {
      final ItemStack copy = stack.copy();
      copy.setCount(1);
      setStackInSlot(slot, copy);
    }
    return canInsert ? ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - 1) : stack;
  }

  @Override
  public ItemStack extractItem(int slot, int amount, boolean simulate) {
    if (amount == 0) {
      return ItemStack.EMPTY;
    }
    if (!validSlot(slot)) {
      return ItemStack.EMPTY;
    }

    ItemStack existing = getStackInSlot(slot);
    if (existing.isEmpty()) {
      return ItemStack.EMPTY;
    }

    if (simulate) {
      return existing.copy();
    } else {
      setStackInSlot(slot, ItemStack.EMPTY);
      return existing;
    }
  }


  /* Heating */

  /**
   * Checks if any slot can heat
   * @param temperature  Temperature to try
   * @return  True if a slot can heat
   */
  public boolean canHeat(int temperature) {
    for (MeltingModule module : modules) {
      if (module != null && module.canHeatItem(temperature)) {
        return true;
      }
    }
    return false;
  }

  /**
   *
   * Tries to fill the fluid handler with the given fluid
   * @param fluid  Fluid to add
   * @return  True if filled, false if not enough space for the whole fluid
   */
  protected boolean tryFillTank(FluidVolume fluid) {
    if (Objects.equals(fluidHandler.fill(fluid.copy(), Simulation.SIMULATE).getAmount_F(), fluid.getAmount_F())) {
      fluidHandler.fill(fluid, Simulation.ACTION);
      return true;
    }
    return false;
  }

  /**
   * Heats all items in the inventory
   * @param temperature  Heating structure temperature
   */
  public void heatItems(int temperature) {
    for (MeltingModule module : modules) {
      if (module != null) {
        module.heatItem(temperature);
      }
    }
  }

  /**
   * Cools down all items in the inventory, used when there is no fuel
   */
  public void coolItems() {
    for (MeltingModule module : modules) {
      if (module != null) {
        module.coolItem();
      }
    }
  }

  /**
   * Writes this module to NBT
   * @return  Module in NBT
   */
  public NbtCompound writeToNBT() {
    NbtCompound nbt = new NbtCompound();
    NbtList list = new NbtList();
    for (int i = 0; i < modules.length; i++) {
      if (modules[i] != null && !modules[i].getStack().isEmpty()) {
        NbtCompound moduleNBT = modules[i].writeToNBT();
        moduleNBT.putByte(TAG_SLOT, (byte)i);
        list.add(moduleNBT);
      }
    }
    if (!list.isEmpty()) {
      nbt.put(TAG_ITEMS, list);
    }
    nbt.putByte(TAG_SIZE, (byte)modules.length);
    return nbt;
  }

  /**
   * Reads this inventory from NBT
   * @param nbt  NBT compound
   */
  public void readFromNBT(NbtCompound nbt) {
    if (!strictSize) {
      // no need to resize, as we ignore old data
      modules = new MeltingModule[nbt.getByte(TAG_SIZE) & 255];
    }
    NbtList list = nbt.getList(TAG_ITEMS, NbtType.COMPOUND);
    for (int i = 0; i < list.size(); i++) {
      NbtCompound item = list.getCompound(i);
      if (item.contains(TAG_SLOT, NbtType.BYTE)) {
        int slot = item.getByte(TAG_SLOT) & 255;
        if (validSlot(slot)) {
          getModule(slot).readFromNBT(item);
        }
      }
    }
  }


  /* Container sync */

  /**
   * Sets up all sub slots for tracking
   * @param consumer  IIntArray consumer
   */
  public void trackInts(Consumer<PropertyDelegate> consumer) {
    for (int i = 0; i < getSlots(); i++) {
      consumer.accept(getModule(i));
    }
  }

  @Override
  public int size() {
    return getSlots();
  }

  @Override
  public boolean isEmpty() {
    throw new RuntimeException("CRAB!"); // FIXME: PORT
  }

  @Override
  public ItemStack getStack(int slot) {
    return getStackInSlot(slot);
  }

  @Override
  public ItemStack removeStack(int slot, int amount) {
    ItemStack stack = getStack(slot);
    if(amount != stack.getCount()) {
      throw new RuntimeException("CRAB!"); // FIXME: PORT
    }
    setStack(slot, ItemStack.EMPTY);
    return stack;
  }

  @Override
  public ItemStack removeStack(int slot) {
    throw new RuntimeException("CRAB!"); // FIXME: PORT
  }

  @Override
  public void setStack(int slot, ItemStack stack) {
    setStackInSlot(slot, stack);
  }

  @Override
  public void markDirty() {
  }

  @Override
  public boolean canPlayerUse(PlayerEntity player) {
    return true;
  }

  @Override
  public void clear() {
  }
}
