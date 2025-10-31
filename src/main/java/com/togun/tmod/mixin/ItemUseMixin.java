package com.togun.tmod.mixin;

import com.togun.tmod.blacklist.ItemBlacklistManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.item.Item.class)
public class ItemUseMixin {
    
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void onUse(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (!world.isClient && user instanceof ServerPlayerEntity) {
            ItemStack stack = user.getStackInHand(hand);
            
            if (ItemBlacklistManager.isBlacklisted(stack)) {
                user.sendMessage(Text.literal("§cThis item is blacklisted and cannot be used!"), true);
                cir.setReturnValue(ActionResult.FAIL);
            }
        }
    }
}

