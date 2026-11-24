package com.togun.tmod.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.togun.tmod.TModFabric;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Names locked to a premium (Mojang-authenticated) login, stored lowercase in
 * the config dir and saved on every change. Edit premium_names.json to unlock
 * someone who lost their account.
 */
public final class PremiumManager {
    private static final Gson GSON = new Gson();
    private static final Set<String> names = ConcurrentHashMap.newKeySet();
    private static final File file = new File(FabricLoader.getInstance().getConfigDir().toFile(),
            "premium_names.json");

    static {
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                Set<String> loaded = GSON.fromJson(reader, new TypeToken<Set<String>>() {
                }.getType());
                if (loaded != null) {
                    loaded.forEach(n -> names.add(n.toLowerCase(Locale.ROOT)));
                }
            } catch (Exception e) {
                TModFabric.LOGGER.error("Failed to load {}", file.getName(), e);
            }
        }
    }

    private PremiumManager() {
    }

    public static boolean isLocked(String name) {
        return name != null && names.contains(name.toLowerCase(Locale.ROOT));
    }

    /** @return true if the lock state changed */
    public static boolean setLocked(String name, boolean locked) {
        String key = name.toLowerCase(Locale.ROOT);
        boolean changed = locked ? names.add(key) : names.remove(key);
        if (changed) {
            save();
        }
        return changed;
    }

    private static synchronized void save() {
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(names, writer);
        } catch (Exception e) {
            TModFabric.LOGGER.error("Failed to save {}", file.getName(), e);
        }
    }
}
