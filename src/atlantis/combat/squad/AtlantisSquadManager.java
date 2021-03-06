package atlantis.combat.squad;

import atlantis.combat.squad.missions.Missions;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import java.util.ArrayList;

/**
 * Commands all existing battle squads.
 */
public class AtlantisSquadManager {

    /**
     * List of all unit squads.
     */
    protected static ArrayList<Squad> squads = new ArrayList<>();

    // =========================================================
    public static void possibleCombatUnitCreated(AUnit unit) {
        if (shouldSkipUnit(unit)) {
            return;
        }

        Squad squad = getAlphaSquad();
        squad.addUnit(unit);
        unit.setSquad(squad);
    }

    public static void battleUnitDestroyed(AUnit unit) {
        if (shouldSkipUnit(unit)) {
            return;
        }

        Squad squad = unit.getSquad();
        if (squad != null) {
            squad.removeUnit(unit);
            unit.setSquad(null);
        }
    }

    /**
     * Skips buildings, workers and Zerg Larva
     * @param unit
     * @return
     */
    private static boolean shouldSkipUnit(AUnit unit) {
        return unit.getType().isBuilding() || unit.isWorker() || unit.getType().equals(AUnitType.Zerg_Larva);
    }

    // =========================================================
    // Manage squads
    /**
     * Get first, main squad of units.
     */
    public static Squad getAlphaSquad() {

        // If no squad exists, create main squad
        if (squads.isEmpty()) {
            Squad unitSquad = Squad.createNewSquad(null, Missions.getInitialMission());
            squads.add(unitSquad);
        }

        return squads.get(0);
    }

    // =========================================================
    // Getters & Setters
    public static ArrayList<Squad> getSquads() {
        return squads;
    }

    public static void setSquads(ArrayList<Squad> squads) {
        AtlantisSquadManager.squads = squads;
    }

}
