package atlantis.buildings.managers;

import atlantis.AtlantisConfig;
import atlantis.AtlantisGame;
import atlantis.workers.AtlantisWorkerManager;
import atlantis.wrappers.SelectUnits;
import java.util.Collection;
import jnibwapi.Unit;
import jnibwapi.types.TechType;
import jnibwapi.types.UpgradeType;

public class AtlantisGasManager {

    private static final int MIN_GAS_WORKERS_PER_BUILDING = 3;
    private static final int MAX_GAS_WORKERS_PER_BUILDING = 3;

    // =========================================================
    
    /**
     * If any of our gas extracting buildings needs worker, it will assign exactly one worker per frame (until
     * no more needed).
     */
    public static void handleGasBuildings() {
        if (AtlantisGame.getTimeFrames() % 10 != 0) {
            return;
        }
        
        // =========================================================
        
        Collection<Unit> gasBuildings = SelectUnits.ourBuildings().ofType(AtlantisConfig.GAS_BUILDING).list();
        Collection<Unit> workers = SelectUnits.ourWorkers().list();
        
        // =========================================================
        
        int MIN_GAS_WORKERS_PER_BUILDING = defineMinGasWorkersPerBuilding();

        for (Unit gasBuilding : gasBuildings) {
            int numberOfWorkersAssigned = AtlantisWorkerManager.getHowManyWorkersAt(gasBuilding);
            
            // Assign when LOWER THAN MIN
            if (numberOfWorkersAssigned < MIN_GAS_WORKERS_PER_BUILDING) {
                Unit worker = getWorkerForGasBuilding(gasBuilding);
                if (worker != null) {
                    worker.gather(gasBuilding, false);
                }
                break;
            }
            
            // Deassign when MORE THAN MAX
            else if (numberOfWorkersAssigned > MAX_GAS_WORKERS_PER_BUILDING) {
                Unit worker = AtlantisWorkerManager.getRandomWorkerAssignedTo(gasBuilding);
                if (worker != null) {
                    worker.stop(false);
                }
                break;
            }
        }
        
        // =========================================================
        
//        Unit gasBuildingNeedingWorker = AtlantisGasManager.getOneGasBuildingNeedingWorker();
//        if (gasBuildingNeedingWorker != null) {
//            Unit worker = SelectUnits.ourWorkers().gatheringMinerals(true).first();
//            if (worker != null) {
//                worker.gather(gasBuildingNeedingWorker, false);
//            }
//        }
    }
    
    // =========================================================
    
    private static Unit getWorkerForGasBuilding(Unit gasBuilding) {
        Unit worker = SelectUnits.ourWorkers().gatheringMinerals(true).first();
        return worker;
    }

    public static int defineMinGasWorkersPerBuilding() {
        int seconds = AtlantisGame.getTimeSeconds();
        
        if (seconds < 150) {
            return 1;
        }
        else if (seconds < 200) {
            return 2;
        }
        else {
            return 3;
        }
    }

}
