package com.mrjoshuat.banishmentbeacon.handler;

import com.mrjoshuat.banishmentbeacon.config.BanishmentConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

public class BeaconBlockEntityHandler {
    private static List<BlockPos> lightningProducedAtCache = new ArrayList<>();

    public static void handleCanSpawn(World world, BlockPos pos, CallbackInfoReturnable<Boolean> info) {
        var withinArea = BanishmentConfig.INSTANCE.isPosWithinSpawnProofArea(pos);
        if (withinArea) {
            info.setReturnValue(false);
            info.cancel();
        }
    }

    public static void produceLightning(World world, BlockPos pos) {
        LightningEntity lightningEntity = EntityType.LIGHTNING_BOLT.create(world);
        lightningEntity.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(pos));
        lightningEntity.setChanneler(null);
        lightningEntity.setCosmetic(true);
        world.spawnEntity(lightningEntity);
    }

    private static void produceParticles(World world, ParticleEffect parameters, BlockPos pos) {
        var random = world.getRandom();
        for(int i = 0; i < 5; ++i) {
            double d = random.nextGaussian() * 0.02D;
            double e = random.nextGaussian() * 0.02D;
            double f = random.nextGaussian() * 0.02D;
            var randomX = pos.getX() + random.nextDouble();
            var randomY = pos.getY() + 1 + random.nextDouble();
            var randomZ = pos.getZ() + random.nextDouble();
            world.addParticle(parameters, randomX, randomY, randomZ, d, e, f);
        }
    }

    public static int updateLevelHandler(World world, int x, int y, int z) {
        var banishmentShape = BanishmentConfig.Properties.IndicatorShape;
        int i = 0;
        var validBanishmentBeacon = false;

        for(int j = 1; j <= 4; i = j++) {
            int k = y - j;
            if (k < world.getBottomY()) {
                break;
            }

            boolean bl = true;

            for(int l = x - j; l <= x + j && bl; ++l) {
                for(int m = z - j; m <= z + j; ++m) {
                    var blockState = world.getBlockState(new BlockPos(l, k, m));
                    if (!blockState.isIn(BlockTags.BEACON_BASE_BLOCKS)) {
                        bl = false;
                        break;
                    }

                    if (banishmentShape == BanishmentConfig.BanishmentProperties.Shape.FULL_BASE
                        && blockState.getBlock() != BanishmentConfig.Properties.IndicatorBlock) {
                        bl = false;
                        break;
                    }

                    var isCorner =
                        (l == x - j && m == z - j) || // top left
                        (l == x + j && m == z - j) || // top right
                        (l == x - j && m == z + j) || // bottom left
                        (l == x + j && m == z + j); // bottom right
                    var isCentre = l == x && m == z;

                    if (banishmentShape == BanishmentConfig.BanishmentProperties.Shape.UNDER_BEACON) {
                        if (j == 1 && isCentre && blockState.getBlock() == BanishmentConfig.Properties.IndicatorBlock) {
                            validBanishmentBeacon = true;
                        }
                    } else if (banishmentShape == BanishmentConfig.BanishmentProperties.Shape.CENTRE_COLUMN) {
                        if (isCentre && blockState.getBlock() == BanishmentConfig.Properties.IndicatorBlock) {
                            validBanishmentBeacon = true;
                        }
                    }
                    else if (banishmentShape == BanishmentConfig.BanishmentProperties.Shape.CORNERS) {
                        if (isCorner && blockState.getBlock() == BanishmentConfig.Properties.IndicatorBlock) {
                            validBanishmentBeacon = true;
                        }
                    }
                }
            }

            if (!bl) {
                break;
            }
        }

        if (validBanishmentBeacon && i >= BanishmentConfig.Properties.MinTier) {
            var pos = new BlockPos(x, y, z);
            if (BanishmentConfig.INSTANCE.addCachedBeacon(pos)) {
                if (BanishmentConfig.Properties.ProduceThunderOnBeaconActivation && !lightningProducedAtCache.contains(pos)) {
                    BeaconBlockEntityHandler.produceLightning(world, pos);
                    lightningProducedAtCache.add(pos);
                }

                world.getOtherEntities(null, BanishmentConfig.INSTANCE.getCachedBeaconBox(pos))
                    .stream()
                    .filter(f -> f instanceof Monster)
                    .filter(f -> !f.hasCustomName())
                    .forEach(Entity::discard);
            }
        }

        return i;
    }

    public static void removeCachedLightningStrike(BlockPos pos) {
        if (lightningProducedAtCache.contains(pos)) {
            lightningProducedAtCache.remove(pos);
        }
    }

    public static void produceBanishmentAreaParticles(World world, BlockPos pos) {
        produceParticles(world, ParticleTypes.ENCHANT, pos);
    }
}
