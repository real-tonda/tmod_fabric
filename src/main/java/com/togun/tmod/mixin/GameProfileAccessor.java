package com.togun.tmod.mixin;

import com.mojang.authlib.GameProfile;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = GameProfile.class, remap = false)
public interface GameProfileAccessor {
    // Redundant - all methods are public
}
