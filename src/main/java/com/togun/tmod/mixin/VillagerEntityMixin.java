package com.togun.tmod.mixin;

import com.togun.tmod.mixin.EntityAccessor;
import com.togun.tmod.config.TutilsConfigManager;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Server-side mixin to automatically restock villager trades when enabled
 */
@Mixin(VillagerEntity.class)
public class VillagerEntityMixin {

    @Unique
    private int tutilsRestockCheckCounter = 0;

    /**
     * Automatically restocks trades when they run out if infinite restocks is
     * enabled
     * Checks every 20 ticks (1 second) to avoid performance issues
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        VillagerEntity villager = (VillagerEntity) (Object) this;

        // Only run on server side
        if (((EntityAccessor) villager).getLevelField().isClient()) {
            return;
        }

        // Check if infinite restocks is enabled
        if (!TutilsConfigManager.getConfigValue(TutilsConfigManager.VILLAGER_INFINITE_RESTOCKS)) {
            return;
        }

        // Only check every 20 ticks (1 second) to avoid performance issues
        tutilsRestockCheckCounter++;
        if (tutilsRestockCheckCounter < 20) {
            return;
        }
        tutilsRestockCheckCounter = 0;

        // Check if villager has trades
        TradeOfferList offers = villager.getOffers();
        if (offers == null || offers.isEmpty()) {
            return;
        }

        // Check if any trades are out of stock and restock if needed
        boolean needsRestock = false;
        for (TradeOffer offer : offers) {
            if (offer.isDisabled()) {
                needsRestock = true;
                break;
            }
        }

        // Restock if any trades are disabled
        if (needsRestock) {
            villager.restock();
        }
    }
}
