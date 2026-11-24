package com.togun.tmod.mixin;

import com.mojang.authlib.GameProfile;
import com.togun.tmod.util.PremiumManager;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.util.Uuids;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * /premium support: names locked with /premium go through the normal Mojang
 * login even on an offline-mode server, so a cracked client can't use them.
 */
@Mixin(ServerLoginNetworkHandler.class)
public abstract class ServerLoginNetworkHandlerMixin {
    @Shadow
    @Final
    MinecraftServer server;
    @Shadow
    GameProfile profile;

    @Unique
    private String tmod$name;

    @Inject(method = "onHello", at = @At("HEAD"))
    private void tmod$captureName(LoginHelloC2SPacket packet, CallbackInfo ci) {
        tmod$name = packet.name();
    }

    // Offline mode skips the encryption/auth handshake; ask for it for locked names
    @Redirect(method = "onHello", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;isOnlineMode()Z"))
    private boolean tmod$requireAuth(MinecraftServer server) {
        return server.isOnlineMode() || PremiumManager.isLocked(tmod$name);
    }

    // Mojang auth hands back the online UUID; keep the offline one so the
    // player's existing data (inventory, op status, ...) still matches
    @Inject(method = "acceptPlayer", at = @At("HEAD"))
    private void tmod$keepOfflineUuid(CallbackInfo ci) {
        if (!server.isOnlineMode() && profile != null && profile.getId() != null
                && PremiumManager.isLocked(profile.getName())) {
            GameProfile offline = new GameProfile(Uuids.getOfflinePlayerUuid(profile.getName()), profile.getName());
            offline.getProperties().putAll(profile.getProperties());
            profile = offline;
        }
    }
}
