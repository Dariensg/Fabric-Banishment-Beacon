package com.mrjoshuat.banishmentbeacon.mixin;

import com.mrjoshuat.banishmentbeacon.config.BanishmentConfig;

import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonFight;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderDragonFight.class)
public class EnderDragonFightMixin {
    /*@Inject(
        at = @At("RETURN"),
        method = "createDragon()Lnet/minecraft/entity/boss/dragon/EnderDragonEntity;"
    )
    private void  createDragon(CallbackInfoReturnable<EnderDragonEntity> info) {
        if (BanishmentConfig.Properties.AllowBossEntities) {
            return;
        }

        var dragon = info.getReturnValue();
        var dragonWithinSpawnProofArea = BanishmentConfig.INSTANCE.isPosWithinSpawnProofArea(dragon.getBlockPos());
        if (dragonWithinSpawnProofArea) {
            dragon.remove(Entity.RemovalReason.DISCARDED);
        }
    }*/
}
