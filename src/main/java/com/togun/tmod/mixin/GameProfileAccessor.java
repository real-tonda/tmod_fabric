package com.togun.tmod.mixin;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.UUID;

@Mixin(GameProfile.class)
public interface GameProfileAccessor {
    @Accessor("name")
    String getName();

    @Accessor("id")
    UUID getId();

    @Accessor("properties")
    PropertyMap getProperties();
}
