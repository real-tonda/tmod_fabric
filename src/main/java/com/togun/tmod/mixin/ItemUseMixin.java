package com.togun.tmod.mixin;

import com.togun.tmod.blacklist.ItemBlacklistManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.world.item.Item.class)
public class ItemUseMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void onUse(Level world, Player user, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (!world.isClientSide() && user instanceof ServerPlayer) {
            ItemStack stack = user.getItemInHand(hand);

            if (ItemBlacklistManager.isBlacklisted(stack)) {
                ((ServerPlayer) user).sendSystemMessage(Component.literal("§cThis item is blacklisted and cannot be used!"));
                cir.setReturnValue(InteractionResult.FAIL);
            }
        }
    }
}
