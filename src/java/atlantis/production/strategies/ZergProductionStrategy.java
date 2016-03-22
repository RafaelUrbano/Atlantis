package atlantis.production.strategies;

import atlantis.AtlantisConfig;
import atlantis.AtlantisGame;
import atlantis.buildings.managers.AtlantisBaseManager;
import atlantis.workers.AtlantisWorkerCommander;
import atlantis.workers.AtlantisWorkerManager;
import atlantis.wrappers.SelectUnits;
import java.util.ArrayList;
import jnibwapi.Unit;
import jnibwapi.UnitCommand;
import jnibwapi.types.UnitCommandType;
import jnibwapi.types.UnitType;
import jnibwapi.types.UnitType.UnitTypes;

public class ZergProductionStrategy extends AtlantisProductionStrategy {

    @Override
    protected String getFilename() {
        String ourRaceLetter = AtlantisGame.getPlayerUs().getRace().getLetter();
        String enemyRaceLetter = AtlantisGame.getEnemy().getRace().getLetter();
        return ourRaceLetter + "v" + enemyRaceLetter + ".csv";
    }

    @Override
    public void produceUnit(UnitType type) {
        produceZergUnit(type);
    }
    
    @Override
    public ArrayList<UnitType> produceWhenNoProductionOrders() {
        ArrayList<UnitType> units = new ArrayList<>();
        if (AtlantisWorkerCommander.shouldTrainWorkers(false)) {
            units.add(UnitTypes.Zerg_Drone);
        }
        else {
            if (AtlantisGame.hasBuildingsToProduce(UnitTypes.Zerg_Mutalisk)) {
                units.add(UnitTypes.Zerg_Mutalisk);
            }
            if (AtlantisGame.hasMinerals(400) && AtlantisGame.hasBuildingsToProduce(UnitTypes.Zerg_Zergling)) {
                units.add(UnitTypes.Zerg_Zergling);
            }
            if (AtlantisGame.hasBuildingsToProduce(UnitTypes.Zerg_Hydralisk)) {
                units.add(UnitTypes.Zerg_Hydralisk);
            }
        }
        return units;
    }

    // =========================================================
    
    /**
     * Produce zerg unit from free larva. Will do nothing if no free larva is available.
     */
    public void produceZergUnit(UnitType unitType) {
        Unit larva = SelectUnits.ourLarva().first();
        if (larva != null) {
            larva.train(unitType);
            return;
        }
        
        if (larva != null) {
            System.err.println("Shouldn't be here, couldn't produce unit: " + unitType);
        }
    }

}
