package com.goldenhopper;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Right-click opens the Golden Hopper screen (vanilla HopperBlock opens whatever menu the
 * block entity provides, and our block entity provides GoldenHopperMenu).
 */
public class GoldenHopperBlock extends HopperBlock {

    public GoldenHopperBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GoldenHopperBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide()
                ? null
                : createTickerHelper(type, ModRegistry.GOLDEN_HOPPER_BE, GoldenHopperBlockEntity::serverTick);
    }
}
