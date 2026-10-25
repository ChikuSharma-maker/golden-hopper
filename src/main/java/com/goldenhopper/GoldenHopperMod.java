package com.goldenhopper;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.world.item.CreativeModeTabs;

public class GoldenHopperMod implements ModInitializer {
    @Override
    public void onInitialize() {
        ModRegistry.init();

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.REDSTONE_BLOCKS)
                .register(output -> output.accept(ModRegistry.GOLDEN_HOPPER_ITEM));
    }
}
