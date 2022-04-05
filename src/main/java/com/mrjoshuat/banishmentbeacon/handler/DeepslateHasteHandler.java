package com.mrjoshuat.banishmentbeacon.handler;

import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class DeepslateHasteHandler {
    public static void addHasteToNetheritePickOnDeepslate(ServerPlayerEntity player, ServerWorld world, BlockPos pos, PlayerActionC2SPacket.Action action, Direction direction, int worldHeight) {
        var hasHaste = player.getStatusEffect(StatusEffects.HASTE);
        // Check player hasn't already got haste 3, i.e haste 2 from a beacon
        if (hasHaste != null && hasHaste.getAmplifier() == 3) {
            return;
        }

        // Are they destorying a block now?
        if (action == net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action.START_DESTROY_BLOCK) {
            var mainItem = player.getMainHandStack().getItem();
            // Check they have a netherite pick
            if (mainItem != Items.NETHERITE_PICKAXE) {
                return;
            }

            var block = world.getBlockState(pos);
            // Check trying to break deepslate
            if (block.getBlock() == Blocks.DEEPSLATE) {
                player.setStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 60, 10), null);
            }
        }
    }
}
