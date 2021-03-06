package atlantis.debug;

import atlantis.Atlantis;
import atlantis.AtlantisGame;
import atlantis.buildings.managers.AtlantisGasManager;
import atlantis.combat.AtlantisCombatEvaluator;
import atlantis.combat.squad.AtlantisSquadManager;
import atlantis.combat.squad.missions.MissionAttack;
import atlantis.combat.squad.missions.MissionDefend;
import atlantis.constructing.AtlantisConstructingManager;
import atlantis.constructing.ConstructionOrder;
import atlantis.constructing.ConstructionOrderStatus;
import atlantis.enemy.AtlantisEnemyUnits;
import atlantis.information.UnitData;
import atlantis.production.ProductionOrder;
import atlantis.production.orders.AtlantisBuildOrders;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.util.AtlantisUtilities;
import atlantis.util.ColorUtil;
import atlantis.util.PositionUtil;
import atlantis.workers.AtlantisWorkerManager;
import atlantis.wrappers.APosition;
import atlantis.wrappers.MappingCounter;
import bwapi.Color;
import bwapi.Game;
import bwapi.Position;
import bwapi.Text.Size.Enum;
import java.util.ArrayList;
import java.util.Map;

/**
 * Here you can include code that will draw extra informations over units etc.
 */
public class AtlantisPainter {
    
//    private static final boolean DISABLE_PAINTING = true;
    private static final boolean DISABLE_PAINTING = false;

    private static Game bwapi;
    private static int sideMessageTopCounter = 0;
    private static int sideMessageMiddleCounter = 0;
    private static int sideMessageBottomCounter = 0;

    /**
     * List of enemy units that have been targetted in the last frame.
     */
//    private static TreeSet<AUnit> _temporaryTargets = new TreeSet<>();
    // =========================================================
    /**
     * Executed once per frame, at the end of all other actions.
     */
    public static void paint() {
        sideMessageTopCounter = 0;
        sideMessageBottomCounter = 0;
        bwapi = Atlantis.getBwapi();
        bwapi.setTextSize(Enum.Small);
        
        if (DISABLE_PAINTING) {
            return;
        }

        // =========================================================
        // On-map paint
        paintImportantPlaces();
        paintColorCirclesAroundUnits();
//        paintConstructionProgress();
        paintConstructionPlaces();
        paintBuildingHealth();
        paintWorkersAssignedToBuildings();
        paintUnitsBeingTrainedInBuildings();
        paintBarsUnderUnits();
//        paintTemporaryTargets();
        paintEnemyDiscovered();
        paintCombatUnits();

        // =========================================================
        // On-screen paint
        paintInfo();
        paintUnitCounters();
        paintProductionQueue();
        paintSidebarConstructionsPending();
        paintKilledAndLost();
        paintTooltipsOverUnits();
    }

    // =========================================================
    // Hi-level
    /**
     * Painting for combat units can be a little different. Put here all the related code.
     */
    private static void paintCombatUnits() {
        for (AUnit unit : Select.ourCombatUnits().listUnits()) {
            APosition unitPosition = unit.getPosition();
            double combatEval = AtlantisCombatEvaluator.evaluateSituation(unit);

            // =========================================================
            // === Paint life bars bars over wounded units
            // =========================================================
            if (unit.isWounded()) {
                int boxWidth = 20;
                int boxHeight = 4;
                int boxLeft = unitPosition.getX() - boxWidth / 2;
                int boxTop = unitPosition.getY() + 23;

                Position topLeft = new APosition(boxLeft, boxTop);

                // =========================================================
                // Paint box
                int healthBarProgress = boxWidth * unit.getHitPoints() / (unit.getMaxHitPoints() + 1);
                bwapi.drawBoxMap(topLeft, new APosition(boxLeft + boxWidth, boxTop + boxHeight), Color.Red, true);
                bwapi.drawBoxMap(topLeft, new APosition(boxLeft + healthBarProgress, boxTop + boxHeight), Color.Green, true);

                // =========================================================
                // Paint box borders
                bwapi.drawBoxMap(topLeft, new APosition(boxLeft + boxWidth, boxTop + boxHeight), Color.Black, false);
            }

            // =========================================================
            // === Paint targets for combat units
            // =========================================================
            APosition targetPosition = unit.getTargetPosition();
            if (targetPosition == null) {
                targetPosition = unit.getTarget().getPosition();
            }
            if (targetPosition != null && unit.distanceTo(targetPosition) <= 15) {
                paintLine(unitPosition, targetPosition, (unit.isAttacking() ? Color.Green : Color.Red));
            }

            // =========================================================
            // === Paint white flags over running units
            // =========================================================
            if (unit.isRunning()) {
                int flagWidth = 15;
                int flagHeight = 8;

                System.out.println("is running");
                paintLine(unitPosition, targetPosition, Color.Blue); // Where unit is running to

                paintRectangleFilled(unitPosition.translate(0, -flagHeight - 2),
                        flagWidth, flagHeight, Color.Yellow); // White flag
                paintRectangle(unitPosition.translate(0, -flagHeight - 2),
                        flagWidth, flagHeight, Color.Black); // Flag border
                paintRectangleFilled(unitPosition.translate(-1, --flagHeight - 2),
                        2, flagHeight, Color.Red); // Flag stick
            }

            // =========================================================
            // === Combat Evaluation Strength
            // =========================================================
            if (combatEval < 10) {
                double eval = AtlantisCombatEvaluator.evaluateSituation(unit);
                if (eval < 999) {
                    String combatStrength = eval >= 10 ? (ColorUtil.getColorString(Color.Green) + ":)")
                            : AtlantisCombatEvaluator.getEvalString(unit);
                    paintTextCentered(new APosition(unitPosition.getX(), unitPosition.getY() - 15), combatStrength, null);
                }
                
                // =========================================================
                // === Paint circle around units with zero ground weapon 
                // === cooldown equal to 0 - meaning they can shoot now
                // =========================================================
                if (unit.getGroundWeaponCooldown() == 0) {
                    paintCircle(unitPosition, 14, Color.White);
                }
            }
        }

        // =========================================================
        for (AUnit unit : Select.enemy().combatUnits().listUnits()) {
            APosition unitPosition = unit.getPosition();
            double eval = AtlantisCombatEvaluator.evaluateSituation(unit);
            if (eval < 999) {
                String combatStrength = eval >= 10 ? (ColorUtil.getColorString(Color.Green) + ":)")
                        : AtlantisCombatEvaluator.getEvalString(unit);
                paintTextCentered(new APosition(unitPosition.getX(), unitPosition.getY() - 15), combatStrength, null);
            }
        }
    }

    /**
     * Paint focus point for global attack mission etc.
     */
    private static void paintInfo() {

        // Time
        paintSideMessage("Time: " + AtlantisGame.getTimeSeconds() + "s", Color.Grey);

        // =========================================================
        // Gas workers
        paintSideMessage("Gas workers: " + AtlantisGasManager.defineMinGasWorkersPerBuilding(), Color.Grey);
        paintSideMessage("Reserved minerals: " + AtlantisBuildOrders.getMineralsNeeded(), Color.Grey);
        paintSideMessage("Reserved gas: " + AtlantisBuildOrders.getGasNeeded(), Color.Grey);
        // =========================================================
        // Global mission
        paintSideMessage("Mission: " + AtlantisSquadManager.getAlphaSquad().getMission().getName(), Color.White);

        // =========================================================
        // Focus point
        APosition focusPoint = MissionAttack.getFocusPoint();
        AUnit mainBase = Select.mainBase();
        String desc = "";
        if (focusPoint != null && mainBase != null) {
            desc = "(" + ((int) mainBase.distanceTo(focusPoint)) + " tiles)";
        }
        paintSideMessage("Focus point: " + focusPoint + desc, Color.Blue, 0);

        // =========================================================
        paintSideMessage("Combat squad size: " + AtlantisSquadManager.getAlphaSquad().size(), Color.Blue, 0);
    }

    /**
     * Paints small progress bars over units that have cooldown.
     */
    private static void paintBarsUnderUnits() {
//        for (AUnit unit : Select.ourCombatUnits().listUnits()) {
//
//            // =========================================================
//            // === Paint life bars bars over wounded units
//            // =========================================================
//            if (UnitUtil.getHPPercent(unit) < 100) {
//                int boxWidth = 20;
//                int boxHeight = 4;
//                int boxLeft = unit.getPosition().getX() - boxWidth / 2;
//                int boxTop = unit.getPosition().getY() + 23;
//
//                Position topLeft = new APosition(boxLeft, boxTop);
//
//                // =========================================================
//                // Paint box
//                int healthBarProgress = boxWidth * unit.getHitPoints() / (unit.getMaxHitPoints() + 1);
//                bwapi.drawBoxMap(topLeft, new APosition(boxLeft + boxWidth, boxTop + boxHeight), Color.Red, true);
//                bwapi.drawBoxMap(topLeft, new APosition(boxLeft + healthBarProgress, boxTop + boxHeight), Color.Green, true);
//
//                // =========================================================
//                // Paint box borders
//                bwapi.drawBoxMap(topLeft, new APosition(boxLeft + boxWidth, boxTop + boxHeight), Color.Black, false);
//            }

        // =========================================================
        // === Paint cooldown progress bars over units
        // =========================================================
//            if (unit.getGroundWeaponCooldown() > 0) {
//                int cooldownWidth = 20;
//                int cooldownHeight = 4;
//                int cooldownLeft = unit.getPX() - cooldownWidth / 2;
//                int cooldownTop = unit.getPY() + 23;
//                String cooldown = Color.getColorString(Color.Yellow) + "(" + unit.getGroundWeaponCooldown() + ")";
//
//                Position topLeft = new APosition(cooldownLeft, cooldownTop);
//
//                // =========================================================
//                // Paint box
//                int cooldownProgress = cooldownWidth * unit.getGroundWeaponCooldown()
//                        / (unit.getType().getGroundWeapon().getDamageCooldown() + 1);
//                bwapi.drawBox(topLeft, new APosition(cooldownLeft + cooldownProgress, cooldownTop + cooldownHeight),
//                        Color.Brown, true, false);
//
//                // =========================================================
//                // Paint box borders
//                bwapi.drawBox(topLeft, new APosition(cooldownLeft + cooldownWidth, cooldownTop + cooldownHeight),
//                        Color.Black, false, false);
//
//                // =========================================================
//                // Paint label
////                paintTextCentered(new APosition(cooldownLeft + cooldownWidth - 4, cooldownTop), cooldown, false);
//            }
        // =========================================================
        // === Paint battle squad
        // =========================================================
//            if (unit.getSquad() != null) {
//                paintTextCentered(new APosition(unit.getPX(), unit.getPY() + 3), Color.getColorString(Color.Grey)
//                        + "#" + unit.getSquad().getID(), false);
//            }
        // =========================================================
        // === Paint num of other units around this unit
        // =========================================================
//            int ourAround = Select.ourCombatUnits().inRadius(1.7, unit).count();
//            paintTextCentered(new APosition(unit.getPX(), unit.getPY() - 15), Color.getColorString(Color.Orange)
//                    + "(" + ourAround + ")", false);
//            // =========================================================
//            // === Combat Evaluation Strength
//            // =========================================================
//            if (AtlantisCombatEvaluator.evaluateSituation(unit) < 10) {
//                double eval = AtlantisCombatEvaluator.evaluateSituation(unit);
//                if (eval < 999) {
//                    String combatStrength = eval >= 10 ? (ColorUtil.getColorString(Color.Green) + "++")
//                            : AtlantisCombatEvaluator.getEvalString(unit);
//                    paintTextCentered(new APosition(unit.getPosition().getX(), unit.getPosition().getY() - 15), combatStrength, null);
//                }
//            }
//        }
//
//        for (AUnit unit : Select.enemy().combatUnits().listUnits()) {
//            double eval = AtlantisCombatEvaluator.evaluateSituation(unit);
//            if (eval < 999) {
//                String combatStrength = eval >= 10 ? (ColorUtil.getColorString(Color.Green) + "++")
//                        : AtlantisCombatEvaluator.getEvalString(unit);
//                paintTextCentered(new APosition(unit.getPosition().getX(), unit.getPosition().getY() - 15), combatStrength, null);
//            }
//        }
    }

    /**
     * Paints important choke point near the base.
     */
    private static void paintImportantPlaces() {
        APosition position;

        // Main DEFEND focus point
        if (MissionDefend.getFocusPoint() != null && MissionDefend.getFocusPoint().getCenter() != null) {
            position = APosition.createFrom(MissionDefend.getFocusPoint().getCenter());
            paintCircle(position, 20, Color.Black);
            paintCircle(position, 19, Color.Black);
            paintTextCentered(position, "DEFEND", Color.Grey);
        }

        // Mission ATTACK focus point
        position = MissionAttack.getFocusPoint();
        if (position != null) {
            paintCircle(position, 20, Color.Red);
            //        paintCircle(position, 19, Color.Black);
            paintTextCentered(position, "ATTACK", Color.Red);
        }
    }

    /**
     * Paints list of units we have in top left corner.
     */
    private static void paintUnitCounters() {
        // Unfinished
        MappingCounter<AUnitType> unitTypesCounter = new MappingCounter<>();
        for (AUnit unit : Select.ourUnfinishedRealUnits().listUnits()) {
//        for (AUnit unit : Select.our().listUnits()) {
            unitTypesCounter.incrementValueFor(unit.getType());
        }

        Map<AUnitType, Integer> counters = unitTypesCounter.map();
        counters = AtlantisUtilities.sortByValue(counters, false);
        boolean paintedMessage = false;
        for (AUnitType unitType : counters.keySet()) {
            paintSideMessage("+" + counters.get(unitType) + " " + unitType.toString(), Color.Blue, 0);
            paintedMessage = true;
        }

        if (paintedMessage) {
            paintSideMessage("", Color.White, 0);
        }

        // =========================================================
        // Finished
        unitTypesCounter = new MappingCounter<>();
        for (AUnit unit : Select.our().listUnits()) {
            unitTypesCounter.incrementValueFor(unit.getType());
        }

        counters = unitTypesCounter.map();
        counters = AtlantisUtilities.sortByValue(counters, false);
        for (AUnitType unitType : counters.keySet()) {
            if (!unitType.isBuilding()) {
                paintSideMessage(counters.get(unitType) + "x " + unitType.toString(), Color.Grey, 0);
            }
        }
        paintSideMessage("", Color.White, 0);
    }

    /**
     * Paints next units to build in top left corner.
     */
    private static void paintProductionQueue() {
        paintSideMessage("Prod. queue:", Color.White);

        // Display units currently in production
        for (AUnit unit : Select.ourUnfinished().listUnits()) {
            AUnitType type = unit.getType();
            if (type.equals(AUnitType.Zerg_Egg)) {
                type = unit.getBuildType();
            }
            paintSideMessage(type.getShortName(), Color.Green);
        }

        // Display units that should be produced right now or any time
        ArrayList<ProductionOrder> produceNow = AtlantisGame.getBuildOrders().getThingsToProduceRightNow(false);
        for (ProductionOrder order : produceNow) {
            paintSideMessage(order.getShortName(), Color.Yellow);
        }

        // Display next units to produce
        ArrayList<ProductionOrder> fullQueue = AtlantisGame.getBuildOrders().getProductionQueueNext(
                5 - produceNow.size());
        for (int index = produceNow.size(); index < fullQueue.size(); index++) {
            ProductionOrder order = fullQueue.get(index);
            if (order != null && order.getShortName() != null) {
                if (order.getUnitType() != null
                        && !AtlantisGame.hasBuildingsToProduce(order.getUnitType(), true)) {
                    continue;
                }
                paintSideMessage(order.getShortName(), Color.Red);
            }
        }
    }

    /**
     * Paints all pending contstructions, including those not yet started, even if only in the AI memory.
     */
    private static void paintSidebarConstructionsPending() {
        int yOffset = 220;
        ArrayList<ConstructionOrder> allOrders = AtlantisConstructingManager.getAllConstructionOrders();
        if (!allOrders.isEmpty()) {
            paintSideMessage("Constructing (" + allOrders.size() + ")", Color.White, yOffset);
            for (ConstructionOrder constructionOrder : allOrders) {
                Color color = null;
                switch (constructionOrder.getStatus()) {
                    case CONSTRUCTION_NOT_STARTED:
                        color = Color.Red;
                        break;
                    case CONSTRUCTION_IN_PROGRESS:
                        color = Color.Blue;
                        break;
                    case CONSTRUCTION_FINISHED:
                        color = Color.Teal;
                        break;
                    default:
                        color = Color.Purple;
                        break;
                }

                String status = constructionOrder.getStatus().toString().replace("CONSTRUCTION_", "");
                String builder = (constructionOrder.getBuilder() + "").replace("AUnit(", "");
                paintSideMessage(constructionOrder.getBuildingType().getShortName()
                        + ", " + status + ", " + builder, color, yOffset);
            }
        }
    }

    /**
     * Paints places where buildings that do not yet exist are planned to be placed.
     */
    private static void paintConstructionPlaces() {
        for (ConstructionOrder order : AtlantisConstructingManager.getAllConstructionOrders()) {
            if (order.getStatus() == ConstructionOrderStatus.CONSTRUCTION_NOT_STARTED) {
                APosition positionToBuild = order.getPositionToBuild();
                AUnitType buildingType = order.getBuildingType();
                if (positionToBuild == null || buildingType == null) {
                    continue;
                }

                // Paint box
                paintRectangle(positionToBuild,
                        buildingType.getTileWidth() * 32, buildingType.getTileHeight() * 32, Color.Teal);

                // Draw X
                paintLine(
                        PositionUtil.translate(positionToBuild, buildingType.getTileWidth() * 32, 0),
                        PositionUtil.translate(positionToBuild, 0, buildingType.getTileHeight() * 32),
                        Color.Teal
                );
                paintLine(positionToBuild,
                        buildingType.getTileWidth() * 32,
                        buildingType.getTileHeight() * 32,
                        Color.Teal
                );

                // Draw text
                paintTextCentered(positionToBuild, buildingType.getShortName(), Color.Grey);
            }
        }
    }

    /**
     * Paints circles around units which mean what's their mission.
     */
    private static void paintColorCirclesAroundUnits() {
        for (AUnit unit : Select.ourWorkers().listUnits()) {

//            // STARTING ATTACK
//            if (unit.isStartingAttack()) {
//                paintCircle(unit, 10, Color.Yellow);
//                paintCircle(unit, 9, Color.Yellow);
//            }
//            // ATTACK FRAME
//            if (unit.isAttackFrame()) {
//                paintCircle(unit, 13, Color.Orange);
//                paintCircle(unit, 14, Color.Orange);
//            }
//            // ATTACKING
//            if (unit.isAttacking()) {
//                paintCircle(unit, 12, Color.Red);
//                paintCircle(unit, 11, Color.Red);
//            }
//            // MOVE
//            if (unit.isMoving()) {
//                paintCircle(unit, 8, Color.White);
//                paintCircle(unit, 7, Color.White);
//            }
//            // CONSTRUCTING
//            if (unit.isConstructing()) {
//                paintCircle(unit, 6, Color.Teal);
//                paintCircle(unit, 5, Color.Teal);
//            }
//
//            // RUN
//            if (AtlantisRunning.isRunning(unit)) {
//                paintLine(unit.getPosition(), AtlantisRunning.getNextPositionToRunTo(unit), Color.Blue);
//            }
//            
//            // Paint #ID
//            paintTextCentered(unit, "#" + unit.getID(), Color.Cyan);
            // BUILDER
//            if (AtlantisConstructingManager.isBuilder(unit)) {
//                paintCircle(unit, 15, Color.Teal);
//                paintCircle(unit, 13, Color.Teal);
//                paintCircle(unit, 11, Color.Teal);
//            }
            // Current COMMAND
            if (!unit.isMoving()) {
                paintTextCentered(unit, unit.getLastCommand().getUnitCommandType().toString(), Color.Purple);
            }
        }
    }

    /**
     * Paints progress bar with percent of completion over all buildings under construction.
     */
    private static void paintConstructionProgress() {
        for (AUnit unit : Select.ourBuildingsIncludingUnfinished().listUnits()) {
            if (unit.isCompleted()) {
                continue;
            }

            String stringToDisplay;

            int labelMaxWidth = 56;
            int labelHeight = 6;
            int labelLeft = unit.getPosition().getX() - labelMaxWidth / 2;
            int labelTop = unit.getPosition().getY() + 13;

            double progress = (double) unit.getHitPoints() / unit.getMaxHitPoints();
            int labelProgress = (int) (1 + 99 * progress);
            String color = AtlantisUtilities.assignStringForValue(
                    progress,
                    1.0,
                    0.0,
                    new String[]{ColorUtil.getColorString(Color.Red), ColorUtil.getColorString(Color.Yellow),
                        ColorUtil.getColorString(Color.Green)});
            stringToDisplay = color + labelProgress + "%";

            // Paint box
            bwapi.drawBoxMap(
                    new APosition(labelLeft, labelTop),
                    new APosition(labelLeft + labelMaxWidth * labelProgress / 100, labelTop + labelHeight),
                    Color.Blue,
                    true
            );
            //bwapi.drawBox(new APosition(labelLeft, labelTop), new APosition(labelLeft + labelMaxWidth * labelProgress / 100, labelTop + labelHeight), Color.Blue, true, false);

            // Paint box borders
            bwapi.drawBoxMap(
                    new APosition(labelLeft, labelTop),
                    new APosition(labelLeft + labelMaxWidth, labelTop + labelHeight),
                    Color.Black,
                    false
            );
            //bwapi.drawBox(new APosition(labelLeft, labelTop), new APosition(labelLeft + labelMaxWidth, labelTop + labelHeight), Color.Black, false, false);

            // Paint label
            paintTextCentered(new APosition(labelLeft, labelTop - 3), stringToDisplay, false);

            // Display name of unit
            String name = unit.getBuildType().getShortName();
            paintTextCentered(new APosition(unit.getPosition().getX(), unit.getPosition().getY() - 4), ColorUtil.getColorString(Color.Green)
                    + name, false);
        }
    }

    /**
     * For buildings not 100% healthy, paints its hit points using progress bar.
     */
    private static void paintBuildingHealth() {
        for (AUnit unit : Select.ourBuildings().listUnits()) {
            if (unit.getHitPoints() >= unit.getMaxHitPoints()) { //isWounded()
                continue;
            }
            int labelMaxWidth = 56;
            int labelHeight = 4;
            int labelLeft = unit.getPosition().getX() - labelMaxWidth / 2;
            int labelTop = unit.getPosition().getY() + 13;

            double hpRatio = (double) unit.getHitPoints() / unit.getMaxHitPoints();
            int hpProgress = (int) (1 + 99 * hpRatio);

            Color color = Color.Green;
            if (hpRatio < 0.66) {
                color = Color.Yellow;
                if (hpRatio < 0.33) {
                    color = Color.Red;
                }
            }

            // Paint box
            bwapi.drawBoxMap(
                    new APosition(labelLeft, labelTop),
                    new APosition(labelLeft + labelMaxWidth * hpProgress / 100, labelTop + labelHeight),
                    color,
                    true
            );
            //bwapi.drawBox(new APosition(labelLeft, labelTop), new APosition(labelLeft + labelMaxWidth * hpProgress / 100, labelTop + labelHeight), color, true, false);

            // Paint box borders
            bwapi.drawBoxMap(
                    new APosition(labelLeft, labelTop),
                    new APosition(labelLeft + labelMaxWidth, labelTop + labelHeight),
                    Color.Black,
                    false
            );
            //bwapi.drawBox(new APosition(labelLeft, labelTop), new APosition(labelLeft + labelMaxWidth, labelTop + labelHeight), Color.Black, false, false);
        }
    }

    /**
     * Paints the number of workers that are gathering to this building.
     */
    private static void paintWorkersAssignedToBuildings() {
        for (AUnit building : Select.ourBuildings().listUnits()) {

            // Paint text
            int workers = AtlantisWorkerManager.getHowManyWorkersAt(building);
            if (workers > 0) {
                String workersAssigned = "Workers: " + workers;
                paintTextCentered(PositionUtil.translate(building.getPosition(), 0, -15), workersAssigned, Color.Blue);
            }
        }
    }

    /**
     * If buildings are training units, it paints what unit is trained and the progress.
     */
    private static void paintUnitsBeingTrainedInBuildings() {
        for (AUnit unit : Select.ourBuildingsIncludingUnfinished().listUnits()) {
            if (!unit.getType().isBuilding() || !unit.isTraining()) {
                continue;
            }

            int labelMaxWidth = 100;
            int labelHeight = 10;
            int labelLeft = unit.getPosition().getX() - labelMaxWidth / 2;
            int labelTop = unit.getPosition().getY() + 5;

            int operationProgress = 1;
            AUnit trained = unit.getBuildUnit();
            String trainedUnitString = "";
            if (trained != null) {
                operationProgress = trained.getHPPercent(); // trained.getHP() * 100 / trained.getMaxHP();
                trainedUnitString = trained.getShortName();
            }

            // Paint box
            bwapi.drawBoxMap(
                    new APosition(labelLeft, labelTop),
                    new APosition(labelLeft + labelMaxWidth * operationProgress / 100, labelTop + labelHeight),
                    Color.White,
                    true
            );
            //bwapi.drawBox(new APosition(labelLeft, labelTop), new APosition(labelLeft + labelMaxWidth * operationProgress / 100, labelTop + labelHeight), Color.White, true, false);

            // Paint box borders
            bwapi.drawBoxMap(
                    new APosition(labelLeft, labelTop),
                    new APosition(labelLeft + labelMaxWidth, labelTop + labelHeight),
                    Color.Black,
                    false
            );
            //bwapi.drawBox(new APosition(labelLeft, labelTop), new APosition(labelLeft + labelMaxWidth, labelTop + labelHeight), Color.Black, false, false);

            // =========================================================
            // Display label
            paintTextCentered(
                    new APosition(unit.getPosition().getX() - 4 * trainedUnitString.length(), unit.getPosition().getY() + 16),
                    ColorUtil.getColorString(Color.White) + trainedUnitString, false
            );
        }
    }

    /**
     * Paints number of units killed and lost in the top right corner.
     */
    private static void paintKilledAndLost() {
        int x = 574;
        int y = 18;
        int dx = 30;
        int dy = 9;

        paintMessage("Killed: ", Color.White, x, y, true);
        paintMessage("Lost: ", Color.White, x, y + dy, true);
        paintMessage("-----------", Color.Grey, x, y + 2 * dy, true);
        paintMessage("Price: ", Color.White, x, y + 3 * dy, true);

        paintMessage(Atlantis.KILLED + "", Color.Green, x + dx, y, true);
        paintMessage(Atlantis.LOST + "", Color.Red, x + dx, y + dy, true);

        int balance = Atlantis.KILLED_RESOURCES - Atlantis.LOST_RESOURCES;
        Color color = balance >= 0 ? Color.Green : Color.Red;
        paintMessage((balance >= 0 ? "+" : "") + balance, color, x + dx, y + 3 * dy, true);
    }

    /**
     * Paint red "X" on every enemy unit that has been targetted.
     */
    private static void paintTemporaryTargets() {
        for (AUnit ourUnit : Select.our().listUnits()) {

            // Paint "x" on every unit that has been targetted by one of our units.
            if (ourUnit.isAttacking() && ourUnit.getTarget() != null) {
//                paintMessage("X", Color.Red, ourUnit.getTarget().getPX(), ourUnit.getTarget().getPY(), false);
                paintLine(ourUnit.getPosition(), ourUnit.getTarget().getPosition(), Color.Red);
            }
        }
    }

    /**
     * Tooltips are units messages that appear over them and allow to report actions like "Repairing" or "Run
     * from enemy" etc.
     */
    private static void paintTooltipsOverUnits() {
        for (AUnit unit : Select.our().listUnits()) {
            if (unit.hasTooltip()) { 
                paintTextCentered(unit.getPosition(), unit.getTooltip(), Color.White);
            }
        }
    }

    /**
     * Paints information about enemy units that are not visible, but as far as we know are alive.
     */
    private static void paintEnemyDiscovered() {
        for (UnitData enemyUnitData : AtlantisEnemyUnits.getDiscoveredAndAliveUnits()) {
            APosition topLeft;
//            if (enemyUnitData.getType().isBuilding()) {
            topLeft = enemyUnitData.getPosition().translate(
                    -enemyUnitData.getType().getDimensionLeft(),
                    -enemyUnitData.getType().getDimensionUp()
            );
            paintRectangle(topLeft, enemyUnitData.getType().getDimensionRight(),
                    enemyUnitData.getType().getDimensionDown(), Color.Grey);
            paintText(topLeft, enemyUnitData.getType().getShortName(), Color.White);
//            }
//            else {
//                paintCircle(enemyUnitData.getPosition(), 10, Color.Red);
//            }
//            paintTextCentered(enemyUnitData.getPosition().translate(0, 10), 
//                    enemyUnitData.getPosition().toString(), Color.White);
        }
    }

    // =========================================================
    // Lo-level
    private static void paintSideMessage(String text, Color color) {
        paintSideMessage(text, color, 0);
    }

    private static void paintSideMessage(String text, Color color, int yOffset) {
        if (color == null) {
            color = Color.White;
        }

        int screenX = 10;
        int screenY = 5 + 9 * (yOffset == 0 ? sideMessageTopCounter : sideMessageBottomCounter);
        paintMessage(text, color, screenX, yOffset + screenY, true);

        if (yOffset == 0) {
            sideMessageTopCounter++;
        } else {
            sideMessageBottomCounter++;
        }
    }

    private static void paintMessage(String text, Color color, int x, int y, boolean screenCoord) {
        if (screenCoord) {
            bwapi.drawTextScreen(new APosition(x, y), ColorUtil.getColorString(color) + text);
        } else {
            bwapi.drawTextMap(new APosition(x, y), ColorUtil.getColorString(color) + text);
        }
    }

    private static void paintRectangle(APosition position, int width, int height, Color color) {
        if (position == null) {
            return;
        }
        bwapi.drawBoxMap(position, PositionUtil.translate(position, width, height), color, false);
    }

    private static void paintRectangleFilled(APosition position, int width, int height, Color color) {
        if (position == null) {
            return;
        }
        bwapi.drawBoxMap(position, PositionUtil.translate(position, width, height), color, true);
    }

    private static void paintCircle(AUnit unit, int radius, Color color) {
        paintCircle(unit.getPosition(), radius, color);
    }

    private static void paintCircle(Position position, int radius, Color color) {
        if (position == null) {
            return;
        }
        bwapi.drawCircleMap(position, radius, color, false);
    }

    private static void paintLine(APosition start, int dx, int dy, Color color) {
        paintLine(start, PositionUtil.translate(start, dx, dy), color);
    }

    private static void paintLine(Position start, Position end, Color color) {
        if (start == null || end == null) {
            return;
        }
        bwapi.drawLineMap(start, end, color);
        //getBwapi().drawLine(start, end, color, false);
    }

    private static void paintTextCentered(AUnit unit, String text, Color color) {
        paintTextCentered(unit.getPosition(), text, color, false);
    }

    private static void paintTextCentered(APosition position, String text, Color color) {
        paintTextCentered(position, text, color, false);
    }

    private static void paintTextCentered(AUnit unit, String text, boolean screenCords) {
        paintTextCentered(unit.getPosition(), text, null, screenCords);
    }

    private static void paintTextCentered(APosition position, String text, boolean screenCords) {
        paintTextCentered(position, text, null, screenCords);
    }

    private static void paintTextCentered(APosition position, String text, Color color, boolean screenCoords) {
        if (position == null || text == null) {
            return;
        }

        if (screenCoords) {
            bwapi.drawTextScreen(
                    PositionUtil.translate(position, (int) (-2.7 * text.length()), -2),
                    ColorUtil.getColorString(color) + text
            );
        } else {
            bwapi.drawTextMap(
                    PositionUtil.translate(position, (int) (-2.7 * text.length()), -2),
                    ColorUtil.getColorString(color) + text
            );
        }
    }

    private static void paintText(APosition position, String text, Color color) {
        if (position == null || text == null) {
            return;
        }

        bwapi.drawTextMap(position, ColorUtil.getColorString(color) + text);
    }

}
