package com.mrjoshuat.banishmentbeacon.mixin;

import com.mrjoshuat.banishmentbeacon.config.BanishmentConfig;
import com.mrjoshuat.banishmentbeacon.handler.BeaconBlockEntityHandler;

import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.raid.Raid;

import org.jetbrains.annotations.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Raid.class)
public class RaidMixin {
    // NOTE: this is not used, as clients need to have the same translation, if just raw text all clients see it :/
    // private static final Text RAID_BANISHED_TEXT = new TranslatableText("event.minecraft.raid.banished");

    @Shadow
    private ServerBossBar bar;

    @Inject(
        at = @At("TAIL"),
        method = "addRaider(ILnet/minecraft/entity/raid/RaiderEntity;Lnet/minecraft/util/math/BlockPos;Z)V"
    )
    private void addRaider(int wave, RaiderEntity raider, @Nullable BlockPos pos, boolean existing, CallbackInfo info) {
        if (!BanishmentConfig.Properties.AllowRaiderEntities && BanishmentConfig.INSTANCE.isPosWithinSpawnProofArea(pos)) {
            try {
                raider.setRemoved(Entity.RemovalReason.DISCARDED);
                BeaconBlockEntityHandler.produceAdHocParticlesAtPos(raider.world, pos);
                bar.setName(Text.of("Raid - Banished"));
            } catch (Exception ignored) {}
        }
    }

    @Inject(
        at = @At("HEAD"),
        method = "playRaidHorn(Lnet/minecraft/util/math/BlockPos;)V",
        cancellable = true
    )
    private void playRaidHorn(BlockPos pos, CallbackInfo info) {
        if (!BanishmentConfig.Properties.AllowRaiderEntities && BanishmentConfig.INSTANCE.isPosWithinSpawnProofArea(pos)) {
            info.cancel();
        }
    }
}
