package atlantis.enemy;

import atlantis.AtlantisConfig;
import atlantis.AtlantisGame;
import atlantis.wrappers.MappingCounter;
import atlantis.wrappers.SelectUnits;
import java.util.ArrayList;
import jnibwapi.Unit;
import jnibwapi.types.UnitType;

public class AtlantisEnemyUnits {

    protected static ArrayList<Unit> enemyUnitsDiscovered = new ArrayList<>();

    // =========================================================
    // Special methods
    
    /**
     * Forgets and refreshes info about given unit
     */
    public static void refreshEnemyUnit(Unit unit) {
        forgetUnit(unit.getID());
        
        if (unit.isEnemy()) {
            discoveredEnemyUnit(unit);
        }
    }

    /**
     * Informs this class that new (possibly unfinished) unit exists in the game. Both our (including
     * unfinished) and enemy's.
     */
    public static void forgetUnit(int unitID) {
        Unit unit = getEnemyUnitByID(unitID);
        if (unit != null) {
            enemyUnitsDiscovered.remove(unit);
        }
    }

    /**
     * Based on a stored collection, returns unit object for given unitID.
     */
    public static Unit getEnemyUnitByID(int unitID) {
        for (Unit unit : enemyUnitsDiscovered) {
            if (unit.getID() == unitID) {
                return unit;
            }
        }

        return null;
    }

    // =========================================================
    // Number of units changed
    
    /**
     * Saves information about enemy unit that we see for the first time.
     */
    public static void discoveredEnemyUnit(Unit unit) {
        enemyUnitsDiscovered.add(unit);
    }

    /**
     * Saves information about given unit being destroyed, so counting units works properly.
     */
    public static void unitDestroyed(Unit unit) {
//        System.out.println("Destroyed " + unit + " / " + unit.getID() + " / enemy: " + unit.isEnemy());
        enemyUnitsDiscovered.remove(unit);
    }

    // =========================================================
    // ESTIMATE
    
    /**
     * Returns number of discovered and alive enemy units of given type. Some of them (maybe even all of them)
     * may not be visible right now.
     */
    public static int estimateEnemyUnitsOfType(UnitType type) {
        int total = 0;
        for (Unit unit : enemyUnitsDiscovered) {
            if (unit.getType().equals(type)) {
                total++;
            }
        }
        return total;
    }

}
