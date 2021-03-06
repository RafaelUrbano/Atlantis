package atlantis.constructing;

import atlantis.combat.micro.zerg.ZergCreepColony;
import atlantis.production.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class AtlantisSpecialConstructionManager {

    /**
     * Some buildings like Zerg SUnken Colony need special treatment.
     */
    protected static boolean handledAsSpecialBuilding(AUnitType building, ProductionOrder order) {
        if (building.equals(AUnitType.Zerg_Sunken_Colony)) {
            ZergCreepColony.creepOneIntoSunkenColony();
            return true;
        }
        
        else if (building.isType(AUnitType.Zerg_Lair)) {
            morphFromZergBuildingInto(AUnitType.Zerg_Hatchery, AUnitType.Zerg_Lair);
            return true;
        }
        
        else if (building.isType(AUnitType.Zerg_Hive)) {
            morphFromZergBuildingInto(AUnitType.Zerg_Lair, AUnitType.Zerg_Hive);
            return true;
        }
        
        else if (building.isType(AUnitType.Zerg_Greater_Spire)) {
            morphFromZergBuildingInto(AUnitType.Zerg_Spire, AUnitType.Zerg_Greater_Spire);
            return true;
        }
        
        return false;
    }
    
    // =========================================================

    private static void morphFromZergBuildingInto(AUnitType from, AUnitType into) {
        AUnit building = (AUnit) Select.ourBuildings().ofType(from).first();
        if (building == null) {
            System.err.println("No " + from + " found to morph into " + into);
        }
        else {
            building.morph(into);
        }
    }
    
}