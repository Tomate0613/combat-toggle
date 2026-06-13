package gdn.hypercube.ctoggle.compat.area_lib;

import dev.doublekekse.area_lib.AreaLib;
import dev.doublekekse.area_lib.component.EntityTrackedAreaComponentType;
import dev.doublekekse.area_lib.registry.AreaComponentRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;

public class CombatToggleAreaComponents {
    private static final EntityTrackedAreaComponentType<Unit> FORCE_DISABLE = AreaComponentRegistry.registerEntityTracked(Identifier.of("ctoggle", "force_disable"), Unit.CODEC);

    static void register() {

    }

    public static boolean isForceDisabled(Entity entity) {
        return AreaLib.getSavedData(entity.getEntityWorld()).isInEntityTrackedAreaWith(FORCE_DISABLE, entity);
    }
}
