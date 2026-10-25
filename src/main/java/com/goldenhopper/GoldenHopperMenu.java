package com.goldenhopper;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Menu layout (slot order matters for shift-click):
 *   0..4    the hopper's 5 real item slots
 *   5..40   player inventory + hotbar
 *   41..47  7 "ghost" filter slots (remember an item type, never hold real items)
 *
 * Buttons are sent from the screen with the vanilla "menu button" mechanism (no custom packets):
 *   0 = speed down, 1 = speed up, 2 = toggle inverted, 10..16 = click filter slot 0..6
 */
public class GoldenHopperMenu extends AbstractContainerMenu {

    public static final int HOPPER_SLOTS = 5;
    public static final int FILTER_SLOTS = GoldenHopperBlockEntity.FILTER_SIZE;

    public static final int BTN_SPEED_DOWN = 0;
    public static final int BTN_SPEED_UP = 1;
    public static final int BTN_INVERT = 2;
    public static final int BTN_FILTER_BASE = 10;

    private static final int PLAYER_START = HOPPER_SLOTS;
    private static final int FILTER_START = HOPPER_SLOTS + 36;

    private final Container hopper;
    private final ContainerData data;
    /** Only set on the server. */
    private final GoldenHopperBlockEntity blockEntity;

    /** Client-side constructor (MenuType factory). The server fills the dummy containers with packets. */
    public GoldenHopperMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(HOPPER_SLOTS), new SimpleContainer(FILTER_SLOTS),
                new SimpleContainerData(2), null);
    }

    /** Server-side constructor. */
    public GoldenHopperMenu(int containerId, Inventory inventory, Container hopper, Container filter,
                            ContainerData data, GoldenHopperBlockEntity blockEntity) {
        super(ModRegistry.GOLDEN_HOPPER_MENU, containerId);
        checkContainerSize(hopper, HOPPER_SLOTS);
        checkContainerSize(filter, FILTER_SLOTS);
        checkContainerDataCount(data, 2);
        this.hopper = hopper;
        this.data = data;
        this.blockEntity = blockEntity;

        // 0..4: hopper slots
        for (int i = 0; i < HOPPER_SLOTS; i++) {
            this.addSlot(new Slot(hopper, i, 44 + i * 18, 98));
        }
        // 5..40: player inventory (3 rows) then hotbar
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 130 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inventory, col, 8 + col * 18, 188));
        }
        // 41..47: ghost filter slots
        for (int i = 0; i < FILTER_SLOTS; i++) {
            this.addSlot(new FilterSlot(filter, i, 25 + i * 18, 28));
        }

        this.addDataSlots(data);
    }

    public int getSpeedIndex() {
        return data.get(0);
    }

    public boolean isInverted() {
        return data.get(1) != 0;
    }

    // ------------------------------------------------------------------ button clicks (server)

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (blockEntity == null) {
            return false;
        }
        if (id == BTN_SPEED_DOWN) {
            blockEntity.changeSpeed(-1);
        } else if (id == BTN_SPEED_UP) {
            blockEntity.changeSpeed(1);
        } else if (id == BTN_INVERT) {
            blockEntity.toggleInverted();
        } else {
            int index = id - BTN_FILTER_BASE;
            if (index < 0 || index >= FILTER_SLOTS) {
                return false;
            }
            ItemStack carried = this.getCarried();
            // item on the cursor -> select that item type (the item is NOT used up); empty cursor -> clear the slot
            blockEntity.setFilterSlot(index, carried.isEmpty() ? ItemStack.EMPTY : carried.copyWithCount(1));
        }
        return true;
    }

    // ------------------------------------------------------------------ shift-click

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index >= FILTER_START) {
            return ItemStack.EMPTY;
        }
        ItemStack original = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            original = stack.copy();
            if (index < HOPPER_SLOTS) {
                if (!this.moveItemStackTo(stack, PLAYER_START, FILTER_START, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 0, HOPPER_SLOTS, false)) {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.hopper.stillValid(player);
    }

    // ------------------------------------------------------------------ ghost slot

    /** Shows an item but can never receive or give real items. Clicks are handled by the screen. */
    public static class FilterSlot extends Slot {
        public final int filterIndex;

        public FilterSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
            this.filterIndex = index;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }
    }
}
