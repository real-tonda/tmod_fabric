package com.togun.tmod.mixin;

import com.togun.tmod.config.TutilsConfigManager;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(CreeperEntity.class)
public class CreeperEntityMixin {

    /**
     * With creeper.noGriefing enabled, creeper explosions no longer break blocks
     * (entity damage and knockback are unchanged). Other explosions are untouched.
     */
    @ModifyArg(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;createExplosion(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/world/World$ExplosionSourceType;)Lnet/minecraft/world/explosion/Explosion;"), index = 5)
    private World.ExplosionSourceType noBlockDamage(World.ExplosionSourceType original) {
        return TutilsConfigManager.getConfigValue(TutilsConfigManager.CREEPER_NO_GRIEFING)
                ? World.ExplosionSourceType.NONE
                : original;
    }
}
