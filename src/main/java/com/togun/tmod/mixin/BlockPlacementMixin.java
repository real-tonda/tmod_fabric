package com.togun.tmod.mixin;

import com.togun.tmod.blacklist.ItemBlacklistManager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockPlacementMixin {

    @Inject(method = "getStateForPlacement", at = @At("HEAD"), cancellable = true)
    private void onGetPlacementState(BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir) {
        if (!context.getLevel().isClientSide() && context.getPlayer() instanceof ServerPlayer player) {
            if (ItemBlacklistManager.isBlacklisted(context.getItemInHand())) {
                player.sendSystemMessage(Component.literal("§cThis block is blacklisted and cannot be placed!"));
                cir.setReturnValue(null);
            }
        }
    }
}
