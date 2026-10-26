package com.goldenhopper;

import java.util.Set;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class ModRegistry {
    public static final String MOD_ID = "goldenhopper";

    private static final Identifier ID = Identifier.fromNamespaceAndPath(MOD_ID, "golden_hopper");

    public static final GoldenHopperBlock GOLDEN_HOPPER = Registry.register(
            BuiltInRegistries.BLOCK, ID,
            new GoldenHopperBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.HOPPER)
                    .mapColor(MapColor.GOLD)
                    .setId(ResourceKey.create(Registries.BLOCK, ID))));

    public static final Item GOLDEN_HOPPER_ITEM = Registry.register(
            BuiltInRegistries.ITEM, ID,
            new BlockItem(GOLDEN_HOPPER, new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, ID))
                    .useBlockDescriptionPrefix()));

    public static final BlockEntityType<GoldenHopperBlockEntity> GOLDEN_HOPPER_BE = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE, ID,
            new BlockEntityType<>(GoldenHopperBlockEntity::new, Set.<Block>of(GOLDEN_HOPPER)));

    public static final MenuType<GoldenHopperMenu> GOLDEN_HOPPER_MENU = Registry.register(
            BuiltInRegistries.MENU, ID,
            new MenuType<>(GoldenHopperMenu::new, FeatureFlags.VANILLA_SET));

    private ModRegistry() {}

    /** Touching this class runs the static registrations above. */
    public static void init() {
        // Our block entity extends the vanilla hopper, whose constructor uses the vanilla hopper type.
        // Minecraft checks that type accepts the block it sits in, so we add our block to it.
        BlockEntityType.HOPPER.addSupportedBlock(GOLDEN_HOPPER);
    }
}
