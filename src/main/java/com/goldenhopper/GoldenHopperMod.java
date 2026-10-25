package com.goldenhopper;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.world.item.CreativeModeTabs;

public class GoldenHopperMod implements ModInitializer {
    @Override
    public void onInitialize() {
        ModRegistry.init();

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.REDSTONE_BLOCKS)
                .register(entries -> entries.accept(ModRegistry.GOLDEN_HOPPER_ITEM));
    }
}
