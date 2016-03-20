package atlantis.production.strategies;

import atlantis.AtlantisConfig;
import atlantis.wrappers.SelectUnits;
import com.sun.javafx.css.SizeUnits;
import java.util.ArrayList;
import jnibwapi.Unit;
import jnibwapi.types.UnitType;

public class TerranProductionStrategy extends AtlantisProductionStrategy {

    @Override
    protected String getFilename() {
        return "TerranDefault.csv";
    }
    
    @Override
    public void produceUnit(UnitType type) {
        if (type.equals(AtlantisConfig.WORKER)) {
            produceWorker();
        }
        else if (type.isOrganic()) {
            produceInfantry(type);
        }
        else if (type.isVehicle()) {
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
        
        int marines = SelectUnits.our().countUnitsOfType(UnitType.UnitTypes.Terran_Marine);
        int medics = SelectUnits.our().countUnitsOfType(UnitType.UnitTypes.Terran_Medic);
        
        if ((double) marines / medics < 3) {
            units.add(UnitType.UnitTypes.Terran_Marine);
        }
        else {
            units.add(UnitType.UnitTypes.Terran_Medic);
        }
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
        Unit building = SelectUnits.ourOneIdle(UnitType.UnitTypes.Terran_Factory);
        if (building != null) {
            building.train(type);
        }
    }

    private void produceShip(UnitType type) {
        Unit building = SelectUnits.ourOneIdle(UnitType.UnitTypes.Terran_Starport);
        if (building != null) {
            building.train(type);
        }
    }
    
}
