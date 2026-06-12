package gdn.hypercube.ctoggle.compat.area_lib;

import dev.doublekekse.area_lib.AreaLib;
import dev.doublekekse.area_lib.component.EntityTrackedAreaComponentType;
import dev.doublekekse.area_lib.registry.AreaComponentRegistry;
import gdn.hypercube.ctoggle.CombatToggle;
import net.minecraft.entity.Entity;
import net.minecraft.util.Unit;

public class CombatToggleAreaComponents {
    private static final EntityTrackedAreaComponentType<Unit> FORCE_DISABLE = AreaComponentRegistry.registerEntityTracked(CombatToggle.id("force_disable"), Unit.CODEC);

    static void register() {

    }

    public static boolean isForceDisabled(Entity entity) {
        return AreaLib.getSavedData(entity.getEntityWorld()).isInEntityTrackedAreaWith(FORCE_DISABLE, entity);
    }
}
