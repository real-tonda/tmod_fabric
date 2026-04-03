package com.togun.tmod.mixin;

import com.togun.tmod.config.TutilsConfigManager;
import net.minecraft.block.entity.HopperBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(HopperBlockEntity.class)
public class HopperBlockEntityMixin {

    /**
     * Modifies the hopper transfer cooldown based on the configured value.
     * The vanilla hopper uses a cooldown of 8 game ticks (4 redstone ticks).
     * This can be configured using /tutils cfg redstone.hopperTicks <value>
     */
    @ModifyConstant(method = "insertAndExtract", constant = @Constant(intValue = 8))
    private static int modifyTransferCooldown(int original) {
        return TutilsConfigManager.getIntConfigValue(TutilsConfigManager.REDSTONE_HOPPER_TICKS, 8);
    }
}
