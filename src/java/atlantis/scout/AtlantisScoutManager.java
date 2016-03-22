package atlantis.scout;

import atlantis.AtlantisConfig;
import atlantis.AtlantisGame;
import atlantis.enemy.AtlantisEnemyInformation;
import atlantis.enemy.AtlantisMap;
import atlantis.enemy.AtlantisEnemyUnits;
import atlantis.wrappers.SelectUnits;
import java.util.ArrayList;
import jnibwapi.BaseLocation;
import jnibwapi.Unit;
import jnibwapi.types.UnitType;

public class AtlantisScoutManager {

    /**
     * Current scout unit.
     */
    private static ArrayList<Unit> scouts = new ArrayList<Unit>();

    // =========================================================
    
    /**
     * If we don't have unit scout assigns one of workers to become one and then, <b>scouts and harasses</b>
     * the enemy base or tries to find it if we still don't know where the enemy is.
     */
    public static void update() {
        assignScoutIfNeeded();

        // We don't know any enemy building, scout nearest starting location.
        if (!AtlantisEnemyInformation.hasDiscoveredEnemyBuilding()) {
            for (Unit scout : scouts) {
                tryToFindEnemy(scout);
            }
        } else {
            for (Unit scout : scouts) {
                scoutForTheNextBase(scout);
            }

            // We know enemy building, but don't know any base.
//            Unit enemyBase = AtlantisEnemyInformationManager.hasDiscoveredEnemyBase();
//            if (enemyBase == null) {
//                // @TODO
//            } // We know the exact location of enemy's base.
//            else {
//                for (Unit scout : scouts) {
//                    handleScoutWhenKnowEnemyBase(scout, enemyBase);
//                }
//            }
        }
    }

    // =========================================================
    
    /**
     * Behavior for the scout if we know enemy base location.
     */
    private static void handleScoutWhenKnowEnemyBase(Unit scout, Unit enemyBase) {
        tryToFindEnemy(scout);

//        // Scout already attacking
//        if (scout.isAttacking()) {
//
//            // Scout is relatively healthy
//            if (scout.getHPPercent() >= 99) {
//                // OK
//            } // Scout is wounded
//            else {
//                scout.move(SelectUnits.mainBase(), false);
//            }
//        } // Attack
//        else if (!scout.isStartingAttack()) {
//            scout.attack(enemyBase, false);
//        }
    }

    /**
     * We don't know any enemy building, scout nearest starting location.
     */
    public static void tryToFindEnemy(Unit scout) {
        if (scout == null) {
            return;
        }
        scout.setTooltip("Find enemy");

        // Don't interrupt when moving
//        if (scout.isMoving() || scout.isAttacking()) {
//            return;
//        }
        // Define center point for our searches
        Unit ourMainBase = SelectUnits.mainBase();
        if (ourMainBase == null) {
            return;
        }

        // =========================================================
        // Get nearest unexplored starting location and go there
        BaseLocation startingLocation;
        if (scout.getType().equals(UnitType.UnitTypes.Zerg_Overlord)) {
            startingLocation = AtlantisMap.getStartingLocationBasedOnIndex(
                    scout.getUnitIndex()
            );
        }
        else {
            startingLocation = AtlantisMap.getNearestUnexploredStartingLocation(ourMainBase);
        }
        
        // =========================================================
        
        if (startingLocation != null) {
            scout.setTooltip("Scout!");
            scout.move(startingLocation, false);
        }
    }

    /**
     * If we have no scout unit assigned, make one of our units a scout.
     */
    private static void assignScoutIfNeeded() {

        // ZERG case
        if (AtlantisGame.playsAsZerg()) {
            if (AtlantisEnemyInformation.hasDiscoveredEnemyBuilding()) { // We know enemy building
                scouts.clear();
                if (AtlantisGame.getTimeSeconds() < 250) {
                    scouts.add(SelectUnits.ourWorkers().first());
                }
                else {
                    if (scouts.size() > 1) {
                        scouts.clear();
                    }
                    Unit zergling = SelectUnits.our().ofType(UnitType.UnitTypes.Zerg_Zergling).first();
                    scouts.add(zergling);
                }
            } // Haven't discovered any enemy building
            else {
                scouts.clear();
                scouts.addAll(SelectUnits.ourCombatUnits().list());
            }
        } 

        // =========================================================
        // TERRAN + PROTOSSS
        else if (scouts.isEmpty() && SelectUnits.ourWorkers().count() >= AtlantisConfig.SCOUT_IS_NTH_WORKER) {
            scouts.add(SelectUnits.ourWorkers().first());
        }
    }

    /**
     * Ensure that we have explored location of the next base location, so we can build there.
     */
    private static void scoutForTheNextBase(Unit scout) {
        BaseLocation baseLocation = AtlantisMap.getNearestUnexploredStartingLocation(scout);
        if (baseLocation != null) {
            scout.move(baseLocation);
        }
    }

}
