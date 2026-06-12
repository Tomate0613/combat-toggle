package gdn.hypercube.ctoggle.compat.area_lib;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;

public class AreaLibCompat {
    public static void register() {
        if (FabricLoader.getInstance().isModLoaded("area_lib")) {
            CombatToggleAreaComponents.register();
        }
    }

    public static boolean isForceDisabled(Entity entity) {
        if (FabricLoader.getInstance().isModLoaded("area_lib")) {
            return CombatToggleAreaComponents.isForceDisabled(entity);
        }

        return false;
    }
}
