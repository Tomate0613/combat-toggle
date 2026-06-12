package gdn.hypercube.ctoggle.mixin;

import gdn.hypercube.ctoggle.CombatToggle;
import gdn.hypercube.ctoggle.compat.area_lib.AreaLibCompat;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    public void toggle(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (source.getAttacker() instanceof PlayerEntity) {
            PlayerEntity current = ((PlayerEntity) (Object) this);
            if ((CombatToggle.TOGGLED_PLAYERS.contains(current.getUuid()) && current != source.getAttacker()) && !AreaLibCompat.isForceDisabled(current)) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "shouldDamagePlayer", at = @At("HEAD"), cancellable = true)
    public void toggle(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity current = ((PlayerEntity) (Object) this);

        // For some reason player and current are swapped in ServerPlayerEntity#damage, so we check both
        var areaOverridden = AreaLibCompat.isForceDisabled(current) || AreaLibCompat.isForceDisabled(player);

        if ((CombatToggle.TOGGLED_PLAYERS.contains(current.getUuid()) || CombatToggle.TOGGLED_PLAYERS.contains(player.getUuid())) && !areaOverridden) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "attackLivingEntity", at = @At("HEAD"), cancellable = true)
    public void disallow(LivingEntity target, CallbackInfo ci) {
        if (target instanceof PlayerEntity) {
            PlayerEntity current = ((PlayerEntity) (Object) this);
            if (CombatToggle.TOGGLED_PLAYERS.contains(current.getUuid()) && !AreaLibCompat.isForceDisabled(target)) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    public void disallow(Entity target, CallbackInfo ci) {
        if (target instanceof PlayerEntity) {
            PlayerEntity current = ((PlayerEntity) (Object) this);
            if (CombatToggle.TOGGLED_PLAYERS.contains(current.getUuid()) && !AreaLibCompat.isForceDisabled(target)) {
                ci.cancel();
            }
        }
    }
}
