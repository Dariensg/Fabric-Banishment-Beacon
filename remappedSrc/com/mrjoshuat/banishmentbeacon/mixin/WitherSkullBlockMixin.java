package com.mrjoshuat.banishmentbeacon.mixin;

import Z;
import com.mrjoshuat.banishmentbeacon.config.BanishmentConfig;

import net.minecraft.block.WitherSkullBlock;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WitherSkullBlock.class)
public class WitherSkullBlockMixin {
    @Inject(
        at = @At("HEAD"),
        method = "onPlaced(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/SkullBlockEntity;)V",
        cancellable = true
    )
    private static void onPlaced(World world, BlockPos pos, SkullBlockEntity block, CallbackInfo info) {
        if (BanishmentConfig.Properties.AllowBossEntities)
            return;

        var witherWithinSpawnProofArea = BanishmentConfig.INSTANCE.isPosWithinSpawnProofArea(pos);
        if (witherWithinSpawnProofArea) {
            info.cancel();
        }
    }
}
