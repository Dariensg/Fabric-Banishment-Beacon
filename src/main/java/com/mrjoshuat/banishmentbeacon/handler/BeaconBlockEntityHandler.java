package com.mrjoshuat.banishmentbeacon.handler;

import com.mrjoshuat.banishmentbeacon.ModInit;

import com.mrjoshuat.banishmentbeacon.config.BanishmentConfig;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class BeaconBlockEntityHandler {
    public static void handleCanSpawn(BlockPos pos, CallbackInfoReturnable<Boolean> info) {
        var withinArea = BanishmentConfig.INSTANCE.isPosWithinSpawnProofArea(pos);
        if (withinArea) {
            //ModInit.LOGGER.info("Preventing mob spawn at x:{}, y:{}, z:{}", pos.getX(), pos.getY(), pos.getZ());
            info.setReturnValue(false);
            info.cancel();
        }
    }

    public static int updateLevelHandler(World world, int x, int y, int z) {
        var banishmentShape = BanishmentConfig.Properties.IndicatorShape;
        int i = 0;
        var validBanishmentBeacon = false;
        var addedBanishmentBeacon = false;

        for(int j = 1; j <= 4; i = j++) {
            int k = y - j;
            if (k < world.getBottomY()) {
                break;
            }

            if (!addedBanishmentBeacon && banishmentShape == BanishmentConfig.BanishmentProperties.Shape.UNDER_BEACON) {
                var underBeaconBlock = world.getBlockState(new BlockPos(x, k, z)).getBlock();
                if (underBeaconBlock == BanishmentConfig.Properties.IndicatorBlock) {
                    BanishmentConfig.INSTANCE.addCachedBeacon(new BlockPos(x, y, z));
                    addedBanishmentBeacon = true;
                }
            }

            boolean bl = true;

            for(int l = x - j; l <= x + j && bl; ++l) {
                for(int m = z - j; m <= z + j; ++m) {
                    var blockState = world.getBlockState(new BlockPos(l, k, m));
                    if (banishmentShape == BanishmentConfig.BanishmentProperties.Shape.FULL_BASE
                        && blockState.getBlock() != BanishmentConfig.Properties.IndicatorBlock) {
                        bl = false;
                        break;
                    } else if (!blockState.isIn(BlockTags.BEACON_BASE_BLOCKS)) {
                        var isCorner =
                            (l == x - j && m == z - j) || // top left
                            (l == x + j && m == z - j) || // top right
                            (l == x - j && m == z + j) || // bottom left
                            (l == x + j && m == z + j); // bottom right
                        var isCentre = l == x && m == z;

                        if (banishmentShape == BanishmentConfig.BanishmentProperties.Shape.UNDER_BEACON) {
                            if (!addedBanishmentBeacon && isCentre && blockState.getBlock() != BanishmentConfig.Properties.IndicatorBlock) {
                                bl = false;
                                break;
                            } else {
                                validBanishmentBeacon = true;
                            }
                        } else if (banishmentShape == BanishmentConfig.BanishmentProperties.Shape.CENTRE_COLUMN) {
                            if (isCentre && blockState.getBlock() != BanishmentConfig.Properties.IndicatorBlock) {
                                bl = false;
                                break;
                            } else {
                                validBanishmentBeacon = true;
                            }
                        }
                        else if (banishmentShape == BanishmentConfig.BanishmentProperties.Shape.CORNERS) {
                            if (isCorner && blockState.getBlock() != BanishmentConfig.Properties.IndicatorBlock)
                            {
                                bl = false;
                                break;
                            } else {
                                validBanishmentBeacon = true;
                            }
                        } else {
                            bl = false;
                            break;
                        }
                    }
                }
            }

            if (!bl) {
                break;
            }

            if (validBanishmentBeacon) {
                BanishmentConfig.INSTANCE.addCachedBeacon(new BlockPos(x, y, z));
            }
        }

        return i;
    }
}
