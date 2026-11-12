package com.togun.tmod.util;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;

import java.util.function.Predicate;

public final class Perms {
    private Perms() {
    }

    /**
     * Permission node check (LuckPerms etc.), falling back to the vanilla op level
     */
    public static Predicate<ServerCommandSource> require(String node, int level) {
        return source -> Permissions.check(source, node, level);
    }
}
