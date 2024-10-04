package com.mrjoshuat.banishmentbeacon.handler;

import com.mrjoshuat.banishmentbeacon.config.BanishmentConfig;

import net.minecraft.entity.*;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BeaconBlockEntityHandler {
    public static final List<EntityType<?>> bossEntities = Arrays.asList(EntityType.ENDER_DRAGON, EntityType.WITHER, EntityType.ELDER_GUARDIAN);
    private static final List<BlockPos> lightningProducedAtCache = new ArrayList<>();

    public static boolean handleCanSpawn(SpawnGroup group, EntityType<?> entityType, BlockPos.Mutable pos) {
        var isWithinSpawnProofArea = BanishmentConfig.INSTANCE.isPosWithinSpawnProofArea(pos);
        if (!isWithinSpawnProofArea) {
            return true;
        }

        if (BanishmentConfig.PROPERTIES.allowlistEntities.size() > 0 && BanishmentConfig.PROPERTIES.allowlistEntities.contains(entityType)) {
            return true;
        }

        if (BanishmentConfig.PROPERTIES.denylistEntities.size() > 0 && BanishmentConfig.PROPERTIES.denylistEntities.contains(entityType)) {
            return false;
        }

        if (BanishmentConfig.PROPERTIES.allowBossEntities && BeaconBlockEntityHandler.bossEntities.contains(entityType)) {
            return true;
        }

        return !BanishmentConfig.PROPERTIES.removeSpawnGroups.contains(group);
    }

    public static void produceLightning(World world, BlockPos pos) {
        LightningEntity lightningEntity = EntityType.LIGHTNING_BOLT.create(world);
        assert lightningEntity != null;
        lightningEntity.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(pos));
        lightningEntity.setChanneler(null);
        lightningEntity.setCosmetic(true);
        world.spawnEntity(lightningEntity);
    }

    public static int updateLevelHandler(World world, int x, int y, int z) {
        var banishmentShape = BanishmentConfig.PROPERTIES.indicatorShape;
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
                        && blockState.getBlock() != BanishmentConfig.PROPERTIES.indicatorBlock) {
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
                        if (j == 1 && isCentre && blockState.getBlock() == BanishmentConfig.PROPERTIES.indicatorBlock) {
                            validBanishmentBeacon = true;
                        }
                    } else if (banishmentShape == BanishmentConfig.BanishmentProperties.Shape.CENTRE_COLUMN) {
                        if (isCentre && blockState.getBlock() == BanishmentConfig.PROPERTIES.indicatorBlock) {
                            validBanishmentBeacon = true;
                        }
                    }
                    else if (banishmentShape == BanishmentConfig.BanishmentProperties.Shape.CORNERS) {
                        if (isCorner && blockState.getBlock() == BanishmentConfig.PROPERTIES.indicatorBlock) {
                            validBanishmentBeacon = true;
                        }
                    }
                }
            }

            if (!bl) {
                break;
            }
        }

        if (validBanishmentBeacon && i >= BanishmentConfig.PROPERTIES.minTier) {
            var pos = new BlockPos(x, y, z);
            if (BanishmentConfig.INSTANCE.addCachedBeacon(pos)) {
                if (BanishmentConfig.PROPERTIES.produceThunderOnBeaconActivation && !lightningProducedAtCache.contains(pos)) {
                    BeaconBlockEntityHandler.produceLightning(world, pos);
                    lightningProducedAtCache.add(pos);
                }

                world.getEntitiesByClass(MobEntity.class, BanishmentConfig.INSTANCE.getCachedBeaconBox(pos), f -> !f.hasCustomName())
                    .forEach(e -> {
                        var entityType = e.getType();
                        var spawnGroup = entityType.getSpawnGroup();
                        var entityPos = e.getBlockPos().mutableCopy();
                        if (!handleCanSpawn(spawnGroup, entityType, entityPos)) {
                            e.discard();
                        }
                    });
            }
        }

        if (!validBanishmentBeacon) {
            removeCachedLightningStrike(new BlockPos(x, y, z));
        }

        return i;
    }

    public static void removeCachedLightningStrike(BlockPos pos) {
        lightningProducedAtCache.remove(pos);
    }

    public static void produceBanishmentAreaParticles(World world, BlockPos pos) {
        if (world.isClient)
            return;

        if (world.getTime() % 10 == 0) {
            produceBeaconParticles(world, pos);
        }

        if (world.getTime() % BanishmentConfig.PROPERTIES.particleInterval == 0L) {
            //if (BanishmentConfig.Properties.ProduceParticlesAtBeacon)
                // produceBeaconParticles(world, pos); // produceBeaconParticles(world, pos, ParticleTypes.ENCHANT);
            if (BanishmentConfig.PROPERTIES.produceParticlesBoarder)
                produceBoarderParticles(world, pos, ParticleTypes.WHITE_ASH);
        }
    }

    public static void removeWanderingEntitiesInArea(World world, BlockPos pos) {
        if (world.isClient)
            return;

        var box = BanishmentConfig.INSTANCE.getCachedBeaconBox(pos);
        if (box == null)
            return; // Should this ever be hit, hm?
        var entities = world.getEntitiesByClass(LivingEntity.class, box, e -> {
            var entityType = Registries.ENTITY_TYPE.getOrEmpty(EntityType.getId(e.getType())).get();
            return !handleCanSpawn(entityType.getSpawnGroup(), entityType, pos.mutableCopy());
        });
        entities.stream().filter(e -> !e.hasCustomName()).forEach(e -> {
            e.discard();
            produceAdHocParticlesAtPos(world, e.getBlockPos(), ParticleTypes.HEART);
        });
    }

    public static void produceAdHocParticlesAtPos(World world, BlockPos pos) {
        produceAdHocParticlesAtPos(world, pos, ParticleTypes.WHITE_ASH);
    }

    public static void produceAdHocParticlesAtPos(World world, BlockPos pos, SimpleParticleType parameters) {
        if (world.isClient)
            return;

        var random = world.getRandom();
        var serverWorld = (ServerWorld)world;
        for(int i = 0; i < 5; ++i) {
            double d = random.nextGaussian() * 0.02D;
            double e = random.nextGaussian() * 0.02D;
            double f = random.nextGaussian() * 0.02D;
            var randomX = pos.getX() + random.nextDouble();
            var randomY = pos.getY() + 1 + random.nextDouble();
            var randomZ = pos.getZ() + random.nextDouble();
            serverWorld.spawnParticles(parameters, randomX, randomY, randomZ, 2, d, e, f, 0);
        }
    }

    private static void produceBeaconParticles(World world, BlockPos pos) {
        var serverWorld = (ServerWorld)world;

        serverWorld.spawnParticles(ParticleTypes.ENCHANT,
            (double)pos.getX() + 0.5,
            (double)pos.getY() + 1.0,
            (double)pos.getZ() + 0.5,
            20,
            0,
            0,
            0,
            5);
    }

    private static void produceBoarderParticles(World world, BlockPos pos, SimpleParticleType particleType) {
        var x = pos.getX();
        var y = 0;
        var z = pos.getZ();
        var serverWorld = (ServerWorld)world;
        var radius = BanishmentConfig.PROPERTIES.range;

        for (int worldX = x - radius; worldX <= x + radius + 1; ++worldX) {
            for (int worldZ = z - radius;worldZ <= z + radius + 1; ++worldZ) {
                if ((worldX == x - radius) || (worldX == x + radius + 1) || (worldZ == z - radius) || (worldZ == z + radius + 1)) {
                    y = world.getTopY(Heightmap.Type.WORLD_SURFACE, worldX, worldZ);
                    serverWorld.spawnParticles(particleType, worldX, y + 0.5, worldZ, 10, 0.125, 0.125, 0.125, 0.0);
                }
            }
        }
    }

    public static boolean hasCachedPositionAt(BlockPos pos) {
        return lightningProducedAtCache.contains(pos);
    }
}
