package atlantis.combat.micro;

import atlantis.AtlantisGame;
import atlantis.combat.micro.terran.TerranMedic;
import atlantis.combat.micro.zerg.ZergOverlordManager;
import atlantis.wrappers.SelectUnits;
import atlantis.wrappers.Units;
import jnibwapi.Position;
import jnibwapi.Unit;
import jnibwapi.types.UnitType;

public class DefaultRangedManager extends MicroRangedManager {

    @Override
    public boolean update(Unit unit) {
        if (canIssueOrderToUnit(unit)) {

            // =========================================================
            // Check health status
            if (handleLowHealthIfNeeded(unit)) {
                return true;
            }

            // =========================================================
            // Check chances to win the fight
            if (handleMoveAwayFromEnemiesIfNeeded(unit)) {
                return true;
            }
            if (handleUnfavorableOdds(unit)) {
                return true;
            }
//            if (handleNotExtremelyFavorableOdds(unit)) {
//                return true;
//            }

            // =========================================================
            // Don't spread too much
//            if (handleDontSpreadTooMuch(unit)) {
//                return true;
//            }

            // =========================================================
            // Attack enemy is possible
            if (AtlantisAttackEnemyUnit.handleAttackEnemyUnits(unit)) {
                return true;
            }
            
            // =========================================================
            // False: Did not use micro-manager, allow mission behavior
            // True: Do not allow mission manager to handle this unit
            boolean canGiveCommandToMissionManager = 
                    unit.getGroundWeaponCooldown() > 0 && unit.getAirWeaponCooldown() > 0;
            return canGiveCommandToMissionManager;
        } 

        // =========================================================
        // Can't give orders to unit right now
        else {
            return true;
        }
    }

    // =========================================================
    
    /**
     * @return <b>true</b> if unit can be given order<br />
     * <b>false</b> if unit is in the shooting frame or does any other thing that mustn't be interrupted
     */
    private boolean canIssueOrderToUnit(Unit unit) {
        return !unit.isJustShooting();
    }

    /**
     * If e.g. Terran Marine stands too far forward, it makes him vulnerable. Make him go back.
     */
    private boolean handleDontSpreadTooMuch(Unit unit) {
        Units ourForcesNearby = SelectUnits.ourCombatUnits().inRadius(7, unit).exclude(unit).units();
        Position goTo = null;
        if (ourForcesNearby.isEmpty()) {
            goTo = SelectUnits.ourCombatUnits().exclude(unit).first();
        } else if (ourForcesNearby.size() <= 4) {
            goTo = ourForcesNearby.positionMedian();
        }

        if (goTo != null && unit.distanceTo(goTo) > 5) {
            unit.move(goTo);
            unit.setTooltip("Stand closer");
            return true;
        }

        return false;
    }

}
