package com.togun.tmod.mixin;

import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerCommonPacketListenerImpl.class)
public interface ServerCommonNetworkHandlerAccessor {
    // Redundant - getLatency() is already public
}
