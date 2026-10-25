package com.goldenhopper;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

/**
 * Minecraft 26.x GUI code: GuiGraphics was renamed to GuiGraphicsExtractor, and
 * render* methods became extract*. The panel is drawn with plain colored boxes (no texture file needed).
 */
public class GoldenHopperScreen extends AbstractContainerScreen<GoldenHopperMenu> {

    private Button speedLabel;
    private Button invertButton;
    private int lastSpeedShown = -1;
    private int lastInvertShown = -1;

    public GoldenHopperScreen(GoldenHopperMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 212);
        this.inventoryLabelY = 120;
    }

    @Override
    protected void init() {
        super.init();
        int x = this.leftPos;
        int y = this.topPos;

               StringWidget filterLabel = new StringWidget(x + 8, y + 17, 160, 9,
                Component.literal("Filter: click a slot holding an item")
                        .withStyle(style -> style.withColor(0x404040)),
                this.font);
        this.addRenderableWidget(filterLabel);

        // speed row
        this.addRenderableWidget(Button.builder(Component.literal("-"), b -> press(GoldenHopperMenu.BTN_SPEED_DOWN))
                .bounds(x + 8, y + 50, 28, 20)
                .tooltip(Tooltip.create(Component.literal("Slower")))
                .build());
        this.speedLabel = this.addRenderableWidget(Button.builder(Component.empty(), b -> { })
                .bounds(x + 38, y + 50, 100, 20)
                .tooltip(Tooltip.create(Component.literal("Hopper speed (x1 = normal hopper)")))
                .build());
        this.speedLabel.active = false;
        this.addRenderableWidget(Button.builder(Component.literal("+"), b -> press(GoldenHopperMenu.BTN_SPEED_UP))
                .bounds(x + 140, y + 50, 28, 20)
                .tooltip(Tooltip.create(Component.literal("Faster")))
                .build());

        // inverted row
        this.invertButton = this.addRenderableWidget(Button.builder(Component.empty(), b -> press(GoldenHopperMenu.BTN_INVERT))
                .bounds(x + 8, y + 72, 160, 20)
                .build());

        this.lastSpeedShown = -1;
        this.lastInvertShown = -1;
        refreshLabels();
    }

    private void press(int buttonId) {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, buttonId);
        }
    }

    /** Updates button texts when the synced values from the server change. */
    private void refreshLabels() {
        if (this.speedLabel == null || this.invertButton == null) {
            return;
        }
        int speed = this.menu.getSpeedIndex();
        int inv = this.menu.isInverted() ? 1 : 0;
        if (speed != lastSpeedShown) {
            lastSpeedShown = speed;
            int safe = Math.max(0, Math.min(GoldenHopperBlockEntity.SPEED_SHORT.length - 1, speed));
            this.speedLabel.setMessage(Component.literal("Speed: " + GoldenHopperBlockEntity.SPEED_SHORT[safe]));
        }
        if (inv != lastInvertShown) {
            lastInvertShown = inv;
            if (inv == 1) {
                this.invertButton.setMessage(Component.literal("Inverted: ON"));
                this.invertButton.setTooltip(Tooltip.create(
                        Component.literal("ON: everything passes EXCEPT the selected items")));
            } else {
                this.invertButton.setMessage(Component.literal("Inverted: OFF"));
                this.invertButton.setTooltip(Tooltip.create(
                        Component.literal("OFF: only the selected items pass (nothing selected = everything passes)")));
            }
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        refreshLabels();
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        int x = this.leftPos;
        int y = this.topPos;

        // panel
        graphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, 0xFF373737);
        graphics.fill(x + 2, y + 2, x + this.imageWidth - 2, y + this.imageHeight - 2, 0xFFC6C6C6);

        // slot boxes (gold border for the filter slots)
        for (Slot slot : this.menu.slots) {
            int sx = x + slot.x - 1;
            int sy = y + slot.y - 1;
            boolean filter = slot instanceof GoldenHopperMenu.FilterSlot;
            graphics.fill(sx, sy, sx + 18, sy + 18, filter ? 0xFFD4A017 : 0xFF373737);
            graphics.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF8B8B8B);
        }
    }

    /** Clicking a filter slot: tell the server (it reads the item on your cursor). */
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (this.hoveredSlot instanceof GoldenHopperMenu.FilterSlot filterSlot) {
            press(GoldenHopperMenu.BTN_FILTER_BASE + filterSlot.filterIndex);
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }
}
