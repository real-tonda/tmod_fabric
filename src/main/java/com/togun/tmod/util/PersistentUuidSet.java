package com.togun.tmod.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.togun.tmod.TModFabric;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Set of UUIDs stored in the config dir, saved on every change
 */
public class PersistentUuidSet {
    private static final Gson GSON = new Gson();
    private final Set<UUID> uuids = ConcurrentHashMap.newKeySet();
    private final File file;

    public PersistentUuidSet(String fileName) {
        file = new File(FabricLoader.getInstance().getConfigDir().toFile(), fileName);
        if (!file.exists()) {
            return;
        }
        try (FileReader reader = new FileReader(file)) {
            Set<UUID> loaded = GSON.fromJson(reader, new TypeToken<Set<UUID>>() {
            }.getType());
            if (loaded != null) {
                uuids.addAll(loaded);
            }
        } catch (Exception e) {
            TModFabric.LOGGER.error("Failed to load {}", file.getName(), e);
        }
    }

    public boolean contains(UUID id) {
        return uuids.contains(id);
    }

    public boolean add(UUID id) {
        boolean changed = uuids.add(id);
        if (changed) {
            save();
        }
        return changed;
    }

    public boolean remove(UUID id) {
        boolean changed = uuids.remove(id);
        if (changed) {
            save();
        }
        return changed;
    }

    private synchronized void save() {
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(uuids, writer);
        } catch (Exception e) {
            TModFabric.LOGGER.error("Failed to save {}", file.getName(), e);
        }
    }
}
