package atlantis.combat.micro;

import atlantis.units.AUnit;
import atlantis.units.Select;
import bwapi.Position;
import java.util.List;


/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class AtlantisRunManager {

    private static int MIN_TIME_FRAMES_TO_STOP_RUNNING = 20;
    
    // =========================================================
    
    /**
     * Makes unit run (from close enemies) in the most reasonable way possible.
     */
    public static boolean run(AUnit unit) {
        return unit.runFrom(null); // Run from the nearest enemy

//        // Define the range-wise closest enemy and run from it
//        AUnit nearestEnemy = Select.enemyRealUnits().nearestTo(unit);
//        if (nearestEnemy != null) {
//            if (nearestEnemy.distanceTo(nearestEnemy) <= 6.5) {
//                unit.runFrom(nearestEnemy);
//                return true;
//            }
//        } 
//        
//        // =========================================================
//        // Try running to the main base
//        else {
//            AUnit mainBase = Select.mainBase();
//            if (mainBase != null && mainBase.distanceTo(unit) > 10) {
//                unit.setTooltip("Run to base");
//                unit.move(mainBase);
//                return true;
//            }
//        }
//        
//        // Weird case: we didn't find a way to run.
//        System.err.println("Weird case: we didn't find a way to run");
//        return false;
    }

    /**
     * Indicates that this unit is not running any more.
     */
    public static void unitWantsStopRunning(AUnit unit) {
//        if (unit.getRunning().getTimeSinceLastRun() >= defineMinFramesToStopRunning(unit)) {
        unit.getRunning().stopRunning(); //unit.getRunning().stopRunning();
//        }
    }
    
    public static int getHowManyFramesUnitShouldStillBeRunning(AUnit unit) {
        if (!unit.isRunning()) {
            return 0;
        }
        else {
            return Math.max(0, defineMinFramesToStopRunning(unit) - AtlantisRunning.getTimeSinceLastRun(unit));
        }
    }
    
    // =========================================================

    private static int defineMinFramesToStopRunning(AUnit unit) {
//        return MIN_TIME_FRAMES_TO_STOP_RUNNING + countNearbyUnits(unit) * 20 +
//                (100 - unit.getHPPercent()) / 2;
        return MIN_TIME_FRAMES_TO_STOP_RUNNING + countNearbyUnits(unit.getPosition()) * 10;
    }

    private static int countNearbyUnits(Position position) {
        int total = 0;
        List<AUnit> unitsInRange = (List<AUnit>) Select.our().inRadius(6, position).listUnits();	//TODO check cast safety
        for (AUnit unit : unitsInRange) {
            if (!unit.isRunning()) {
                total++;
            }
        }
        return total;
    }
    
}
