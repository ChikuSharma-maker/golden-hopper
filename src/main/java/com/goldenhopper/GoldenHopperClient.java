package com.goldenhopper;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;

public class GoldenHopperClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.register(ModRegistry.GOLDEN_HOPPER_MENU, GoldenHopperScreen::new);
    }
}
