package com.togun.tmod.mixin;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.togun.tmod.config.TPAllowListManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.TeleportCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Predicate;

@Mixin(TeleportCommand.class)
public class TeleportCommandMixin {

    /**
     * Redirects the .requires() call in TeleportCommand.register to use our custom
     * permission check.
     * This allows players on the TP allow list to use /tp and /teleport.
     */
    @Redirect(method = "register", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;requires(Ljava/util/function/Predicate;)Lcom/mojang/brigadier/builder/ArgumentBuilder;", remap = false))
    private static ArgumentBuilder<ServerCommandSource, ?> overrideRequires(
            LiteralArgumentBuilder<ServerCommandSource> builder, Predicate<ServerCommandSource> original) {
        return builder.requires(TPAllowListManager::canUseTP);
    }
}
