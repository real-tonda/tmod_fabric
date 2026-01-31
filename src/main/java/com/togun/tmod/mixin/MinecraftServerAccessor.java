package com.togun.tmod.mixin;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MinecraftServer.class)
public interface MinecraftServerAccessor {
    // Redundant - using reflection instead to avoid mapping issues
}
