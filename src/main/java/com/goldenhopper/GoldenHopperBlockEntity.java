package com.goldenhopper;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Golden hopper: extends the vanilla hopper (so droppers, chests, comparators, minecarts... all work),
 * adds an adjustable speed, up to 7 filter items and an "inverted" mode.
 * Item filtering goes through canPlaceItem(), which vanilla uses for every way an item can enter a hopper.
 */
public class GoldenHopperBlockEntity extends HopperBlockEntity {

    public static final int FILTER_SIZE = 7;

    /** Speed multipliers relative to a vanilla hopper (vanilla = 1.0 = 1 item per 8 ticks). */
    public static final double[] SPEEDS = {0.25, 0.5, 1.0, 2.0, 4.0, 8.0};
    public static final String[] SPEED_SHORT = {"x0.25", "x0.5", "x1", "x2", "x4", "x8"};
    public static final int DEFAULT_SPEED_INDEX = 3; // x2

    private static final Codec<List<Item>> ITEM_LIST_CODEC = BuiltInRegistries.ITEM.byNameCodec().listOf();

    private int speedIndex = DEFAULT_SPEED_INDEX;
    private boolean inverted = false;
    private double progress = 0.0;

    /** "Ghost" filter slots: they only remember an item type, no real items are stored. */
    private final SimpleContainer filterContainer = new SimpleContainer(FILTER_SIZE);

    /** Synced to the open GUI: [0] = speed index, [1] = inverted (0/1). */
    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return index == 0 ? speedIndex : (inverted ? 1 : 0);
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                speedIndex = Math.floorMod(value, SPEEDS.length);
            } else {
                inverted = value != 0;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public GoldenHopperBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    /** The vanilla constructor passes BlockEntityType.HOPPER; we report our own type so saving/loading works. */
    @Override
    public BlockEntityType<?> getType() {
        return ModRegistry.GOLDEN_HOPPER_BE;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.goldenhopper.golden_hopper");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new GoldenHopperMenu(containerId, inventory, (Container) this, filterContainer, dataAccess, this);
    }

    // ------------------------------------------------------------------ ticking

    public static void serverTick(Level level, BlockPos pos, BlockState state, GoldenHopperBlockEntity be) {
        be.progress += SPEEDS[be.speedIndex];
        int guard = 0;
        // Vanilla's tick advances its internal 8-tick cooldown by one per call.
        // Calling it N times per game tick = N times faster; calling it every Nth tick = N times slower.
        while (be.progress >= 1.0 && guard++ < 16) {
            be.progress -= 1.0;
            HopperBlockEntity.pushItemsTick(level, pos, state, be);
        }
    }

    // ------------------------------------------------------------------ filter

    private boolean filterContains(Item item) {
        for (int i = 0; i < FILTER_SIZE; i++) {
            ItemStack s = filterContainer.getItem(i);
            if (!s.isEmpty() && s.is(item)) {
                return true;
            }
        }
        return false;
    }

    private boolean filterIsEmpty() {
        for (int i = 0; i < FILTER_SIZE; i++) {
            if (!filterContainer.getItem(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Normal mode:   only the selected items pass (empty filter = everything passes).
     * Inverted mode: everything passes EXCEPT the selected items.
     */
    public boolean accepts(ItemStack stack) {
        if (inverted) {
            return !filterContains(stack.getItem());
        }
        return filterIsEmpty() || filterContains(stack.getItem());
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return accepts(stack);
    }

    // ------------------------------------------------------------------ GUI actions (server side)

    public void changeSpeed(int delta) {
        speedIndex = Math.max(0, Math.min(SPEEDS.length - 1, speedIndex + delta));
        progress = 0.0;
        setChanged();
    }

    public void toggleInverted() {
        inverted = !inverted;
        setChanged();
    }

    /** Sets (or clears, if stack is empty) a filter slot. The same item can't be in two slots. */
    public void setFilterSlot(int index, ItemStack stack) {
        if (index < 0 || index >= FILTER_SIZE) {
            return;
        }
        if (!stack.isEmpty()) {
            for (int j = 0; j < FILTER_SIZE; j++) {
                if (j != index && filterContainer.getItem(j).is(stack.getItem())) {
                    return;
                }
            }
        }
        filterContainer.setItem(index, stack);
        setChanged();
    }

    // ------------------------------------------------------------------ saving

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("GoldenSpeedIndex", speedIndex);
        output.putBoolean("GoldenInverted", inverted);
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < FILTER_SIZE; i++) {
            ItemStack s = filterContainer.getItem(i);
            if (!s.isEmpty()) {
                items.add(s.getItem());
            }
        }
        output.store("GoldenFilter", ITEM_LIST_CODEC, items);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        speedIndex = Math.floorMod(input.getIntOr("GoldenSpeedIndex", DEFAULT_SPEED_INDEX), SPEEDS.length);
        inverted = input.getBooleanOr("GoldenInverted", false);
        filterContainer.clearContent();
        List<Item> items = input.read("GoldenFilter", ITEM_LIST_CODEC).orElse(List.of());
        for (int i = 0; i < items.size() && i < FILTER_SIZE; i++) {
            filterContainer.setItem(i, new ItemStack(items.get(i)));
        }
    }
}
