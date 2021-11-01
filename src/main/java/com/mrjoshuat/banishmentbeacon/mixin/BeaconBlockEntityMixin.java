package com.mrjoshuat.banishmentbeacon.mixin;

import com.mrjoshuat.banishmentbeacon.config.BanishmentConfig;
import com.mrjoshuat.banishmentbeacon.handler.BeaconBlockEntityHandler;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BeaconBlockEntity.class)
public class BeaconBlockEntityMixin {
    @Inject(
            at = @At("HEAD"),
            method = "updateLevel(Lnet/minecraft/world/World;III)I",
            cancellable = true
    )
    private static void updateLevelCachedBeacon(World world, int x, int y, int z, CallbackInfoReturnable<Integer> info) {
        var level = BeaconBlockEntityHandler.updateLevelHandler(world, x, y, z);
        info.setReturnValue(level);
        info.cancel();
    }

    @Inject(
            at = @At("HEAD"),
            method = "playSound(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/sound/SoundEvent;)V"
    )
    private static void playSoundUpdateCachedBanishmentBeacons(World world, BlockPos pos, SoundEvent sound, CallbackInfo info) {
        if (sound == SoundEvents.BLOCK_BEACON_DEACTIVATE) {
            BanishmentConfig.INSTANCE.removeCachedBeacon(pos);
        }
    }
}
