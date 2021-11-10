package com.mrjoshuat.banishmentbeacon.mixin;

import com.mrjoshuat.banishmentbeacon.handler.BeaconBlockEntityHandler;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public class BlockMixin {
    @Inject(
        at = @At("HEAD"),
        method = "onBreak(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/player/PlayerEntity;)V"
    )
    private void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player, CallbackInfo info) {
        var block = world.getBlockState(pos).getBlock();
        if (block == Blocks.BEACON)
            BeaconBlockEntityHandler.removeCachedLightningStrike(pos);
    }
}
