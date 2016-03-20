package atlantis.production.strategies;

import atlantis.AtlantisConfig;
import atlantis.wrappers.SelectUnits;
import java.util.ArrayList;
import jnibwapi.Unit;
import jnibwapi.types.UnitType;

public class ProtossProductionStrategy extends AtlantisProductionStrategy {

    @Override
    protected String getFilename() {
        return "ProtossDefault.csv";
    }

    @Override
    public void produceUnit(UnitType type) {
        if (type.equals(AtlantisConfig.WORKER)) {
            produceWorker();
        }
        else if (type.isOrganic()) {
            produceInfantry(type);
        }
        else if (type.isType(UnitType.UnitTypes.Protoss_Reaver, UnitType.UnitTypes.Protoss_Observer)) {
            produceVehicle(type);
        }
        else if (type.isAirUnit()) {
            produceShip(type);
        }
        else {
            System.err.println("HolyFuckingShitException: produceUnit error for type: " + type);
        }
    }

    @Override
    public ArrayList<UnitType> produceWhenNoProductionOrders() {
        ArrayList<UnitType> units = new ArrayList<>();
        units.add(UnitType.UnitTypes.Protoss_Zealot);
        return units;
    }
    
    // =========================================================
    
    private void produceWorker() {
        Unit building = SelectUnits.ourOneIdle(AtlantisConfig.BASE);
        if (building != null) {
            building.train(AtlantisConfig.WORKER);
        }
    }

    private void produceInfantry(UnitType type) {
        Unit building = SelectUnits.ourOneIdle(AtlantisConfig.BARRACKS);
        if (building != null) {
            building.train(type);
        }
    }

    private void produceVehicle(UnitType type) {
        Unit building = SelectUnits.ourOneIdle(UnitType.UnitTypes.Protoss_Robotics_Facility);
        if (building != null) {
            building.train(type);
        }
    }

    private void produceShip(UnitType type) {
        Unit building = SelectUnits.ourOneIdle(UnitType.UnitTypes.Protoss_Stargate);
        if (building != null) {
            building.train(type);
        }
    }

}
