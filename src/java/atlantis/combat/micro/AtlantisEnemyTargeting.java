package atlantis.combat.micro;

import atlantis.AtlantisGame;
import atlantis.wrappers.SelectUnits;
import jnibwapi.Unit;
import jnibwapi.types.UnitType;

public class AtlantisEnemyTargeting {

    /**
     * For given <b>unit</b> it defines the best close range target from enemy units. The target is not
     * necessarily in the shoot range. Will return <i>null</i> if no enemy can is visible.
     */
    public static Unit defineBestEnemyToAttackFor(Unit unit) {
        boolean prioritizeAntiAirTargets = defineIfShouldPrioritizeAntiAirTargets(unit);
        boolean canOurUnitAttackGround = unit.canAttackGroundUnits();
        boolean canOurUnitAttackAir = unit.canAttackAirUnits();
        Unit nearestEnemy = null;
        
        // =========================================================
        // Attack ANTI-AIR units if needed
        nearestEnemy = SelectUnits.enemy(canOurUnitAttackGround, canOurUnitAttackAir)
                .inRadius(14, unit)
                .ofType(
                        UnitType.UnitTypes.Terran_Missile_Turret,
                        UnitType.UnitTypes.Terran_Goliath,
                        UnitType.UnitTypes.Terran_Bunker,
                        UnitType.UnitTypes.Zerg_Spore_Colony,
                        UnitType.UnitTypes.Zerg_Hydralisk,
                        UnitType.UnitTypes.Protoss_Photon_Cannon,
                        UnitType.UnitTypes.Protoss_Dragoon
                ).nearestTo(unit);
        if (nearestEnemy != null) {
            return nearestEnemy;
        }
        
        // =========================================================
        // Attack top priority units
        nearestEnemy = SelectUnits.enemy(canOurUnitAttackGround, canOurUnitAttackAir)
                .inRadius(14, unit)
                .ofType(
                        UnitType.UnitTypes.Terran_Siege_Tank_Siege_Mode,
                        UnitType.UnitTypes.Terran_Siege_Tank_Tank_Mode,
                        UnitType.UnitTypes.Protoss_Reaver,
                        UnitType.UnitTypes.Zerg_Lurker
                ).nearestTo(unit);
        if (nearestEnemy != null) {
            return nearestEnemy;
        }
        
        // =========================================================
        // Attack nearest enemy
        if (AtlantisGame.getTimeSeconds() < 180) {
            nearestEnemy = SelectUnits.enemyRealUnits(canOurUnitAttackGround, canOurUnitAttackAir).nearestTo(unit);
            if (nearestEnemy != null && nearestEnemy.isWorker() 
                    && nearestEnemy.distanceTo(SelectUnits.mainBase()) < 30) {
//                return null;
            }
            else {
                return nearestEnemy;
            }
        }
        
        // =========================================================
        // If no real units found, try selecting important buildings
        nearestEnemy = SelectUnits.enemy(canOurUnitAttackGround, canOurUnitAttackAir)
                .ofType(UnitType.UnitTypes.Protoss_Zealot, UnitType.UnitTypes.Protoss_Dragoon, 
                        UnitType.UnitTypes.Terran_Marine, UnitType.UnitTypes.Terran_Medic, 
                        UnitType.UnitTypes.Terran_Firebat, UnitType.UnitTypes.Zerg_Zergling, 
                        UnitType.UnitTypes.Zerg_Hydralisk).nearestTo(unit);
        if (nearestEnemy != null) {
            return nearestEnemy;
        }
        
        // =========================================================
        // Try selecting defensive buildings
        nearestEnemy = SelectUnits.enemy(canOurUnitAttackGround, canOurUnitAttackAir)
                .ofType(UnitType.UnitTypes.Protoss_Photon_Cannon, UnitType.UnitTypes.Zerg_Sunken_Colony, 
                        UnitType.UnitTypes.Terran_Bunker).nearestTo(unit);
        if (nearestEnemy != null) {
            return nearestEnemy;
        }
        
        // =========================================================
        // Try selecting real units
        nearestEnemy = SelectUnits.enemyRealUnits(canOurUnitAttackGround, canOurUnitAttackAir).nearestTo(unit);
        if (nearestEnemy != null) {
            return nearestEnemy;
        }
        
        // =========================================================
        // If no real units found, try selecting important buildings
        nearestEnemy = SelectUnits.enemy(canOurUnitAttackGround, canOurUnitAttackAir)
                .ofType(UnitType.UnitTypes.Protoss_Pylon, UnitType.UnitTypes.Zerg_Spawning_Pool, 
                        UnitType.UnitTypes.Terran_Command_Center).nearestTo(unit);
        if (nearestEnemy != null) {
            return nearestEnemy;
        }
        
        // =========================================================
        // Okay, try targeting any-fuckin-thing
        nearestEnemy = SelectUnits.enemy(canOurUnitAttackGround, canOurUnitAttackAir).nearestTo(unit);
        if (nearestEnemy != null) {
            return nearestEnemy;
        }
        return nearestEnemy;
    }
    
    // =========================================================

    /**
     * To make successfull air strike, we should make sure we've destroyed all anti-air units first.
     */
    private static boolean defineIfShouldPrioritizeAntiAirTargets(Unit unit) {
        if (unit.isAirUnit()) {
            return true;
        }
        else {
            return SelectUnits.our().ofType(UnitType.UnitTypes.Zerg_Mutalisk, UnitType.UnitTypes.Zerg_Guardian,
                    UnitType.UnitTypes.Terran_Wraith, UnitType.UnitTypes.Terran_Battlecruiser,
                    UnitType.UnitTypes.Protoss_Carrier, UnitType.UnitTypes.Protoss_Scout).count() >= 3;
        }
    }
    
}
