package com.togun.tmod.mixin;

import com.togun.tmod.config.TutilsConfigManager;
import net.minecraft.screen.AnvilScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AnvilScreenHandler.class)
public class AnvilScreenHandlerMixin {

    /**
     * Removes the "Too Expensive!" limit when anvil.notExpensive is enabled: vanilla
     * tests the level cost against 40, so making that test never true leaves no limit
     * ("Integer.MAX_VALUE" is only the value that disables the test, not a new cap).
     */
    // ordinal 0 is the cost assigned to stacked items, only 1 and 2 are the ">= 40" tests
    @ModifyConstant(method = "updateResult", constant = {
            @Constant(intValue = 40, ordinal = 1),
            @Constant(intValue = 40, ordinal = 2)
    })
    private int removeTooExpensiveCap(int original) {
        return TutilsConfigManager.getConfigValue(TutilsConfigManager.ANVIL_NOT_EXPENSIVE) ? Integer.MAX_VALUE : original;
    }
}
