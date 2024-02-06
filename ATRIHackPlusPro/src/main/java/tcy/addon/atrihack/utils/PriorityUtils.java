package tcy.addon.atrihack.utils;

import tcy.addon.atrihack.modules.*;
import tcy.addon.atrihack.modules.conbot.*;
import tcy.addon.atrihack.modules.conbot.AutoCraftingTable;
import tcy.addon.atrihack.modules.misc.AutoMend;
import tcy.addon.atrihack.modules.mov.ScaffoldPlus;
import tcy.addon.atrihack.modules.player.AntiAim;
import tcy.addon.atrihack.modules.player.AutoMine;

/**
 * @author OLEPOSSU
 */

public class PriorityUtils {
    // Tell me a better way to do this pls
    public static int get(Object module) {
        if (module instanceof AnchorAuraPlus) return 9;
        if (module instanceof AntiAim) return 12;
        if (module instanceof AutoCraftingTable) return 4;
        if (module instanceof AutoCrystalPlus) return 10;
        if (module instanceof AutoMend) return 4;
        if (module instanceof PistonCrystal) return 10;
        if (module instanceof AutoMine) return 9;
        if (module instanceof AutoPearl) return 6;
        if (module instanceof AutoTrapPlus) return 5;
        if (module instanceof HoleFillPlus) return 7;
        if (module instanceof HoleFillRewrite) return 7;
        if (module instanceof KillAuraPlus) return 11;
        if (module instanceof ScaffoldPlus) return 2;
        if (module instanceof SelfTrapPlus) return 1;
        if (module instanceof SurroundPlus) return 0;

        return 100;
    }
}
