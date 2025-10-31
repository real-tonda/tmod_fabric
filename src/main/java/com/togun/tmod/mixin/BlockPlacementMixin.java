package com.togun.tmod.mixin;

import com.togun.tmod.blacklist.ItemBlacklistManager;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockPlacementMixin {
    
    @Inject(method = "getPlacementState", at = @At("HEAD"), cancellable = true)
    private void onGetPlacementState(ItemPlacementContext context, CallbackInfoReturnable<BlockState> cir) {
        if (!context.getWorld().isClient && context.getPlayer() instanceof ServerPlayerEntity player) {
            if (ItemBlacklistManager.isBlacklisted(context.getStack())) {
                player.sendMessage(Text.literal("§cThis block is blacklisted and cannot be placed!"), true);
                cir.setReturnValue(null);
            }
        }
    }
}

