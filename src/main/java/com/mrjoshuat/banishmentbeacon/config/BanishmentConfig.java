package com.mrjoshuat.banishmentbeacon.config;

import com.mrjoshuat.banishmentbeacon.ModInit;

import com.google.common.base.Enums;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class BanishmentConfig {
    protected final File file;
    public static final BanishmentConfig INSTANCE = new BanishmentConfig(FabricLoader.getInstance().getConfigDir().resolve(ModInit.ModID + ".properties").toFile());
    public static final BanishmentProperties PROPERTIES = new BanishmentProperties();

    private static final BiMap<BlockPos, Box> BEACON_CACHE_LOCATIONS = HashBiMap.create();

    public BanishmentConfig(File file) {
        this.file = file;
    }

    public void load() {
        if (!file.exists()) {
            ModInit.LOGGER.error("Could not find properties config file at " + file.getAbsolutePath());
            ModInit.LOGGER.info("Writing default properties file to " + file.getAbsolutePath());
            save();
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            fromProperties(reader);
            // save back so any new properties get merged in
            save();
        } catch (Exception ex) {
            ModInit.LOGGER.error("Could not load properties config from file " + file.getAbsolutePath(), ex);
        }
    }

    public void save() {
        try (FileWriter writer = new FileWriter(file)) {
            toProperties(writer);
            writer.flush();
        } catch (Exception ex) {
            ModInit.LOGGER.error("Could not load properties config from file " + file.getAbsolutePath(), ex);
        }
    }

    private void fromProperties(FileReader reader) {
        try {
            Properties prop = new Properties();
            prop.load(reader);

            var blockStr = prop.getProperty("block", "minecraft:diamond_block");
            var shapeStr = prop.getProperty("shape", "UNDER_BEACON");
            var minTier = prop.getProperty("minTier", "4");
            var range = prop.getProperty("range", "200");
            var denylistEntities = prop.getProperty("denylistEntities", "");
            var allowlistEntities = prop.getProperty("allowlistEntities", "minecraft:ender_dragon");
            var removeSpawnGroups = prop.getProperty("removeSpawnGroups", SpawnGroup.MONSTER.toString());
            var produceThunderOnBeaconActivation = prop.getProperty("produceThunderOnBeaconActivation", String.valueOf(false));
            var produceParticlesBoarder = prop.getProperty("produceParticlesBoarder", String.valueOf(true));
            var produceParticlesAtBeacon = prop.getProperty("produceParticlesAtBeacon", String.valueOf(true));
            var allowRaiderEntities = prop.getProperty("allowRaiderEntities", String.valueOf(true));
            var particleInterval = prop.getProperty("particleInterval", "160");
            var allowBossEntities = prop.getProperty("allowBossEntities", String.valueOf(true));
            var removeEntitiesWanderingIntoSpawnProofArea = prop.getProperty("removeEntitiesWanderingIntoSpawnProofArea", String.valueOf(true));
            var allowSpawnProofingWhileCoveredUp = prop.getProperty("allowSpawnProofingWhileCoveredUp", String.valueOf(true));
            var giveHasteEffect = prop.getProperty("giveHasteEffect", String.valueOf(true));

            var blockIdentifier = Identifier.tryParse(blockStr);
            if (blockIdentifier == null) {
                ModInit.LOGGER.info("Property 'block' with value '{}' is not a valid block identifier, falling back to {}", blockStr, PROPERTIES.indicatorBlock);
            } else {
                PROPERTIES.indicatorBlock = Registries.BLOCK.get(blockIdentifier);
            }

            try {
                PROPERTIES.indicatorShape = BanishmentProperties.Shape.valueOf(shapeStr);
            } catch (Exception ex) {
                ModInit.LOGGER.info("Property 'shape' with value '{}' is not a valid shape, falling back to {}", shapeStr, PROPERTIES.indicatorShape);
            }

            try {
                PROPERTIES.minTier = Integer.parseInt(minTier);
            } catch (Exception ex) {
                ModInit.LOGGER.info("Property 'minTier' with value '{}' is not a number, falling back to {}", shapeStr, PROPERTIES.minTier);
            }

            try {
                PROPERTIES.range = Integer.parseInt(range);
            } catch (Exception ex) {
                ModInit.LOGGER.info("Property 'range' with value '{}' is not a number, falling back to {}", shapeStr, PROPERTIES.range);
            }

            try {
                PROPERTIES.range = Integer.parseInt(range);
            } catch (Exception ex) {
                ModInit.LOGGER.info("Property 'range' with value '{}' is not a number, falling back to {}", shapeStr, PROPERTIES.range);
            }

            try {
                if (denylistEntities.length() > 0) {
                    PROPERTIES.denylistEntities = Arrays.stream(denylistEntities.split(","))
                        .map(val -> {
                            var split = val.split("\\.");
                            return split[split.length - 1];
                        })
                        .map(Identifier::tryParse)
                        .map(Registries.ENTITY_TYPE::get)
                        .toList();
                }
            } catch (Exception ex) {
                ModInit.LOGGER.info("Property 'denylistEntities' with value '{}' is not able to be parsed", denylistEntities);
            }

            try {
                if (allowlistEntities.length() > 0) {
                    PROPERTIES.allowlistEntities = Arrays.stream(allowlistEntities.split(","))
                        .map(val -> {
                            var split = val.split("\\.");
                            return split[split.length - 1];
                        })
                        .map(Identifier::tryParse)
                        .map(Registries.ENTITY_TYPE::get)
                        .toList();
                }
            } catch (Exception ex) {
                ModInit.LOGGER.info("Property 'allowlistEntities' with value '{}' is not able to be parsed", allowlistEntities);
            }

            try {
                PROPERTIES.removeSpawnGroups = Arrays.stream(removeSpawnGroups.split(","))
                    .map(id -> Enums.getIfPresent(SpawnGroup.class, removeSpawnGroups).get())
                    .toList();
            } catch (Exception ex) {
                ModInit.LOGGER.info("Property 'removeSpawnGroups' with value '{}' is not able to be parsed", removeSpawnGroups);
            }

            try {
                PROPERTIES.particleInterval = Integer.parseInt(particleInterval);
            } catch (Exception ex) {
                ModInit.LOGGER.info("Property 'particleInterval' with value '{}' is not a number, falling back to {}", shapeStr, PROPERTIES.particleInterval);
            }

            PROPERTIES.produceThunderOnBeaconActivation = Boolean.parseBoolean(produceThunderOnBeaconActivation);
            PROPERTIES.produceParticlesAtBeacon = Boolean.parseBoolean(produceParticlesAtBeacon);
            PROPERTIES.produceParticlesBoarder = Boolean.parseBoolean(produceParticlesBoarder);
            PROPERTIES.allowRaiderEntities = Boolean.parseBoolean(allowRaiderEntities);
            PROPERTIES.allowBossEntities = Boolean.parseBoolean(allowBossEntities);
            PROPERTIES.removeEntitiesWanderingIntoSpawnProofArea = Boolean.parseBoolean(removeEntitiesWanderingIntoSpawnProofArea);
            PROPERTIES.allowSpawnProofingWhileCoveredUp = Boolean.parseBoolean(allowSpawnProofingWhileCoveredUp);
            PROPERTIES.giveHasteEffect = Boolean.parseBoolean(giveHasteEffect);
        }
        catch (Exception ex) {
            ModInit.LOGGER.error("Could not read config from file", ex);
        }
    }

    private void toProperties(FileWriter writer) throws IOException {
        Properties prop = new Properties();
        prop.setProperty("block", Registries.BLOCK.getId(PROPERTIES.indicatorBlock).toString());
        prop.setProperty("shape", PROPERTIES.indicatorShape.name);
        prop.setProperty("minTier", String.valueOf(PROPERTIES.minTier));
        prop.setProperty("range", String.valueOf(PROPERTIES.range));
        prop.setProperty("particleInterval", String.valueOf(PROPERTIES.particleInterval));
        var denylistEntities = String.join(",", PROPERTIES.denylistEntities.stream().map(EntityType::toString).toList());
        prop.setProperty("denylistEntities", denylistEntities);
        var allowlistEntities = String.join(",", PROPERTIES.allowlistEntities.stream().map(EntityType::toString).toList());
        prop.setProperty("allowlistEntities", allowlistEntities);
        prop.setProperty("produceThunderOnBeaconActivation", String.valueOf(PROPERTIES.produceThunderOnBeaconActivation));
        var removeOnlySpawnGroups = String.join(",", PROPERTIES.removeSpawnGroups.stream().map(Enum::toString).toList());
        prop.setProperty("removeSpawnGroups", removeOnlySpawnGroups);
        prop.setProperty("produceParticlesBoarder", String.valueOf(PROPERTIES.produceParticlesBoarder));
        prop.setProperty("produceParticlesAtBeacon", String.valueOf(PROPERTIES.produceParticlesAtBeacon));
        prop.setProperty("allowBossEntities", String.valueOf(PROPERTIES.allowBossEntities));
        prop.setProperty("allowRaiderEntities", String.valueOf(PROPERTIES.allowRaiderEntities));
        prop.setProperty("removeEntitiesWanderingIntoSpawnProofArea", String.valueOf(PROPERTIES.removeEntitiesWanderingIntoSpawnProofArea));
        prop.setProperty("allowSpawnProofingWhileCoveredUp", String.valueOf(PROPERTIES.allowSpawnProofingWhileCoveredUp));
        prop.setProperty("giveHasteEffect", String.valueOf(PROPERTIES.giveHasteEffect));
        prop.store(writer, "");
    }

    public boolean isPosWithinSpawnProofArea(@NotNull BlockPos pos) {
        var vec = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
        return BEACON_CACHE_LOCATIONS.values().stream().anyMatch(beaconPox -> beaconPox.contains(vec));
    }

    public boolean addCachedBeacon(@NotNull BlockPos pos) {
        if (!BEACON_CACHE_LOCATIONS.containsKey(pos)) {
            ModInit.LOGGER.info("Banishment beacon cache added at " + pos);
            try {
                BEACON_CACHE_LOCATIONS.put(pos, createBoxBoundary(pos));
                return true;
            } catch (Exception ex) {
                ModInit.LOGGER.error("Failed to add cached beacon at " + pos);
                return false;
            }
        }
        return false;
    }

    public void removeCachedBeacon(@NotNull BlockPos pos) {
        if (BEACON_CACHE_LOCATIONS.containsKey(pos)) {
            ModInit.LOGGER.info("Banishment beacon cache removed at " + pos);
            try {
                BEACON_CACHE_LOCATIONS.remove(pos);
            } catch (Exception ex) {
                ModInit.LOGGER.error("Failed to remove cached beacon at " + pos);
            }
        }
    }

    public Box getCachedBeaconBox(@NotNull BlockPos pos) { return BEACON_CACHE_LOCATIONS.get(pos); }

    public boolean isCachedBeacon(@NotNull BlockPos pos) { return BEACON_CACHE_LOCATIONS.containsKey(pos); }

    private Box createBoxBoundary(BlockPos pos) {
        return new Box(pos).expand(BanishmentConfig.PROPERTIES.range);
    }

    public static class BanishmentProperties {
        // Set defaults here
        public Block indicatorBlock = Blocks.DIAMOND_BLOCK;
        public Shape indicatorShape = Shape.UNDER_BEACON;
        public int minTier = 4;
        public int range = 200;
        public boolean produceThunderOnBeaconActivation = true;
        public boolean produceParticlesBoarder = true;
        public boolean produceParticlesAtBeacon = true;
        public boolean allowRaiderEntities = true;
        public boolean allowBossEntities = true;
        public boolean allowSpawnProofingWhileCoveredUp = true;
        public boolean removeEntitiesWanderingIntoSpawnProofArea = true;
        public List<? extends EntityType<?>> denylistEntities = new ArrayList<>();
        public List<? extends EntityType<?>> allowlistEntities = new ArrayList<>();
        public List<SpawnGroup> removeSpawnGroups = List.of(SpawnGroup.MONSTER);
        public int particleInterval = 160;
        // Particle interval

        public boolean giveHasteEffect = true;

        public enum Shape {
            UNDER_BEACON("UNDER_BEACON"), // Just 1 block under beacon
            CORNERS("CORNERS"), // Just corners
            CENTRE_COLUMN("CENTRE_COLUMN"), // Full column down to minTier
            FULL_BASE("FULL_BASE"); // All blocks

            private final String name;
            Shape(String name) {
                this.name = name;
            }
        }
    }
}
