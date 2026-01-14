package com.togun.tmod.mixin;

import com.togun.tmod.config.TutilsConfigManager;
import net.minecraft.block.entity.HopperBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(HopperBlockEntity.class)
public class HopperBlockEntityMixin {

    /**
     * Modifies the hopper transfer cooldown from 8 ticks to 4 ticks when faster
     * hoppers is enabled.
     * The vanilla hopper uses a cooldown of 8 game ticks (4 redstone ticks).
     * When enabled, this reduces it to 4 game ticks (2 redstone ticks).
     */
    @ModifyConstant(method = "insertAndExtract", constant = @Constant(intValue = 8))
    private static int modifyTransferCooldown(int original) {
        if (TutilsConfigManager.getConfigValue(TutilsConfigManager.REDSTONE_FASTER_HOPPERS)) {
            return 4; // 2 redstone ticks (4 game ticks)
        }
        return original; // 4 redstone ticks (8 game ticks) - vanilla behavior
    }
}
