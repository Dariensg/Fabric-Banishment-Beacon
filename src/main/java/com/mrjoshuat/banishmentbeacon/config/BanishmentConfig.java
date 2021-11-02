package com.mrjoshuat.banishmentbeacon.config;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mrjoshuat.banishmentbeacon.ModInit;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.util.*;

public class BanishmentConfig {
    protected final File file;
    public static final BanishmentConfig INSTANCE = new BanishmentConfig(FabricLoader.getInstance().getConfigDir().resolve(ModInit.ModID + ".properties").toFile());
    public static final BanishmentProperties Properties = new BanishmentProperties();

    private static final BiMap<BlockPos, Box> beaconCacheLocations = HashBiMap.create();

    public BanishmentConfig(File file) {
        this.file = file;
    }

    public void load() {
        if (!file.exists()) {
            ModInit.LOGGER.error("Could not find properties config file at " + file.getAbsolutePath());
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            fromProperties(reader);
        } catch (Exception ex) {
            ModInit.LOGGER.error("Could not load properties config from file " + file.getAbsolutePath(), ex);
        }
    }

    public void save() {
        // TODO
    }

    private void fromProperties(FileReader reader) {
        try {
            Properties prop = new Properties();
            prop.load(reader);

            var blockStr = prop.getProperty("block", "minecraft:diamond_block");
            var shapeStr = prop.getProperty("shape", "UNDER_BEACON");
            var minTier = prop.getProperty("minTier", "4");
            var range = prop.getProperty("range", "100");

            var blockIdentifier = Identifier.tryParse(blockStr);
            if (blockIdentifier == null) {
                ModInit.LOGGER.info("Property 'block' with value '{}' is not a valid block identifier, falling back to {}", blockStr, Properties.IndicatorBlock);
            } else {
                Properties.IndicatorBlock = Registry.BLOCK.get(blockIdentifier);
            }

            try {
                Properties.IndicatorShape = BanishmentProperties.Shape.valueOf(shapeStr);
            } catch (Exception ex) {
                ModInit.LOGGER.info("Property 'shape' with value '{}' is not a valid shape, falling back to {}", shapeStr, Properties.IndicatorShape);
            }

            try {
                Properties.MinTier = Integer.parseInt(minTier);
            } catch (Exception ex) {
                ModInit.LOGGER.info("Property 'minTier' with value '{}' is not a number, falling back to {}", shapeStr, Properties.MinTier);
            }

            try {
                Properties.Range = Integer.parseInt(range);
            } catch (Exception ex) {
                ModInit.LOGGER.info("Property 'range' with value '{}' is not a number, falling back to {}", shapeStr, Properties.Range);
            }
        }
        catch (Exception ex) {
            ModInit.LOGGER.error("Could not read config from file", ex);
        }
    }

    public boolean isPosWithinSpawnProofArea(@NotNull BlockPos pos) {
        var vec = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
        return beaconCacheLocations.values().stream().anyMatch(beaconPox -> {
           return beaconPox.contains(vec);
        });
    }

    public boolean addCachedBeacon(@NotNull BlockPos pos) {
        if (!beaconCacheLocations.containsKey(pos)) {
            ModInit.LOGGER.info("Banishment beacon cache added at " + pos);
            try {
                beaconCacheLocations.put(pos, createBoxBoundary(pos));
                return true;
            } catch (Exception ex) {
                ModInit.LOGGER.error("Failed to add cached beacon at " + pos);
                return false;
            }
        }
        return false;
    }

    public void removeCachedBeacon(@NotNull BlockPos pos) {
        if (beaconCacheLocations.containsKey(pos)) {
            ModInit.LOGGER.info("Banishment beacon cache removed at " + pos);
            try {
                beaconCacheLocations.remove(pos);
            } catch (Exception ex) {
                ModInit.LOGGER.error("Failed to remove cached beacon at " + pos);
            }
        }
    }

    public Box getCachedBeaconBox(@NotNull BlockPos pos) { return beaconCacheLocations.get(pos); }

    public boolean isCachedBeacon(@NotNull BlockPos pos) { return beaconCacheLocations.containsKey(pos); }

    private Box createBoxBoundary(BlockPos pos) {
        return new Box(pos).expand(BanishmentConfig.Properties.Range);
    }

    public static class BanishmentProperties {
        // Set defaults here
        public Block IndicatorBlock = Blocks.DIAMOND_BLOCK;
        public Shape IndicatorShape = Shape.UNDER_BEACON;
        public int MinTier = 4;
        public int Range = 100;

        public enum Shape {
            UNDER_BEACON("UNDER_BEACON"), // Just 1 block under beacon
            CORNERS("CORNERS"), // Just corners
            CENTRE_COLUMN("CENTRE_COLUMN"), // Full column down to minTier
            FULL_BASE("FULL_BASE"); // All blocks

            private String name;
            Shape(String name) {
                this.name = name;
            }
        }
    }
}
