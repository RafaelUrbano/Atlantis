package atlantis.combat;

import atlantis.combat.micro.zerg.ZergOverlordManager;
import atlantis.combat.squad.Squad;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class AtlantisCombatUnitManager {

    protected static boolean update(AUnit unit) {
//        unit.removeTooltip();
        Squad squad = unit.getSquad();

        // =========================================================
        // DON'T INTERRUPT shooting units
        if (shouldNotDisturbUnit(unit)) {
            return true;
        }

        // =========================================================
        // Handle MICRO-MANAGERS for given unit according to its type
        if (handledAsSpecialUnit(unit)) {
            return true;
        }

        // =========================================================
        // Act with proper micro-manager and decide if mission manager can issue orders afterward.
        boolean microManagerForbidsOtherActions;
//        if (unit.isRangedUnit()) {
//            microManagerForbidsOtherActions = squad.getMicroRangedManager().update(unit);
//        } else {
        microManagerForbidsOtherActions = squad.getMicroMeleeManager().update(unit);
//        }

        // MICRO-MANAGER indicates that its orders should not be overriden by mission manager
        if (microManagerForbidsOtherActions) {
            return true;
        } 

        // =========================================================
        // It's okay to handle MISSION orders according to current mission (e.g. DEFEND, ATTACK)
        else {
//                    if (!unit.isMoving() && !unit.isAttacking() && !unit.isJustShooting()) {
            return squad.getMission().update(unit);
//                    }
        }
    }

    // =========================================================
    /**
     *
     */
    private static boolean shouldNotDisturbUnit(AUnit unit) {
//        return false;
        return unit.isAttackFrame() || unit.isStartingAttack();
    }

    /**
     * There are some units that should have individual micro managers like Zerg Overlord. If unit is special
     * unit it will run proper micro managers here and return true, meaning no other managers should be used.
     * False will give command to standard Melee of Micro managers.
     */
    private static boolean handledAsSpecialUnit(AUnit unit) {
        if (unit.getType().equals(AUnitType.Zerg_Overlord)) {
            ZergOverlordManager.update(unit);
            unit.setTooltip("Overlord");
            return true;
        } else {
            return false;
        }
    }

}
