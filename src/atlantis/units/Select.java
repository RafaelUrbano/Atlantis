package atlantis.units;

import atlantis.Atlantis;
import atlantis.AtlantisConfig;
import atlantis.AtlantisGame;
import atlantis.constructing.AtlantisConstructingManager;
import atlantis.information.UnitData;
import atlantis.util.AtlantisUtilities;
import atlantis.util.PositionUtil;
import atlantis.wrappers.APositionedObject;
import bwapi.Position;
import bwapi.PositionedObject;
import bwapi.Unit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;


/**
 * This class allows to easily select units e.g. to select one of your Marines, nearest to given location, you
 * would run:<br />
 * <p>
 * <b> Select.our().ofType(AUnitType.Terran_Marine).nearestTo(Select.mainBase()) </b>
 * </p>
 * It uses nice flow and every next method filters out units that do not fulfill certain conditions.<br />
 * Unless clearly specified otherwise, this class returns <b>ONLY COMPLETED</b> units.
 */
public class Select<T> {

    // =====================================================================
    // Collection<AUnit> wrapper with extra methods
    //private AUnits units;
    private List<T> data;

    // CACHED variables
    private static AUnit _cached_mainBase = null;

    // =====================================================================
    // Constructor is private, use our(), enemy() or neutral() methods
    protected Select(Collection<T> unitsData) {
        data = new ArrayList<>();
        data.addAll(unitsData);
    }

    // =====================================================================
    // Helper for base object
    
    private static List<AUnit> ourUnits() {
        List<AUnit> data = new ArrayList<>();

//        System.out.println("AtlantisGame.getPlayerUs().getUnits() = " + AtlantisGame.getPlayerUs().getUnits().size());
        for (Unit u : AtlantisGame.getPlayerUs().getUnits()) {
//            System.out.println(u);
//            System.out.println("******** " + AUnit.createFrom(u));
            data.add(AUnit.createFrom(u));
//            System.out.println(AUnit.createFrom(u));
        }
        
//        System.out.println("## Our size: " + data.size());

        return data;
    }
    
    private static List<AUnit> enemyUnits() {
        List<AUnit> data = new ArrayList<>();

        for (Unit u : AtlantisGame.getEnemy().getUnits()) {
            data.add(AUnit.createFrom(u));
        }

        return data;
    }
    
    private static List<AUnit> neutralUnits() {
        List<AUnit> data = new ArrayList<>();

        for (Unit u : Atlantis.getBwapi().neutral().getUnits()) {
            data.add(AUnit.createFrom(u));
        }

        return data;
    }
    
    private static List<Unit> neutralUnitsBWMirror() {
        return Atlantis.getBwapi().neutral().getUnits();
    }

    // =====================================================================
    // Create base object
    
    /**
     * Selects all of our finished and existing units (units, buildings, but no spider mines etc).
     */
    public static Select<AUnit> our() {
        //Units units = new Units();
        List<AUnit> data = new ArrayList<>();

        for (AUnit unit : ourUnits()) {
            if (unit.exists() && unit.isCompleted() && !unit.isType(
                    AUnitType.Terran_Vulture_Spider_Mine, AUnitType.Zerg_Larva, AUnitType.Zerg_Egg)) {
                data.add(unit);	//TODO: make it more efficient by just querying the cache of known units
            }
        }
        
        return new Select<AUnit>(data);
    }

    /**
     * Selects all of our finished combat units (no buildings, workers, spider mines etc).
     */
    public static Select<AUnit> ourCombatUnits() {
        //Units units = new AUnits();
        List<AUnit> data = new ArrayList<>();

        for (AUnit unit : ourUnits()) {
            if (unit.exists() && unit.isCompleted() && !unit.isNotActuallyUnit() && !unit.getType().isBuilding()
                    && !unit.getType().equals(AtlantisConfig.WORKER)) {
                data.add(unit);	//TODO: make it more efficient by just querying the cache of known units
            }
        }

        return new Select<AUnit>(data);
    }

    /**
     * Selects all of our units (units, buildings, but no spider mines etc), <b>even those unfinished</b>.
     */
    public static Select<AUnit> ourIncludingUnfinished() {
        //Units units = new AUnits();
        List<AUnit> data = new ArrayList<>();

        for (AUnit unit : ourUnits()) {

            if (unit.exists() && !unit.getType().equals(AUnitType.Terran_Vulture_Spider_Mine)) {
                data.add(unit);	//TODO: make it more efficient by just querying the cache of known units
            }
        }

        return new Select<AUnit>(data);
    }

    /**
     * Selects our unfinished units.
     */
    public static Select<AUnit> ourUnfinished() {
        //Units units = new AUnits();
        List<AUnit> data = new ArrayList<>();

        for (AUnit unit : ourUnits()) {

            if (unit.exists() && !unit.isCompleted()) {
                data.add(unit);
            }
        }

        return new Select<AUnit>(data);
    }

    /**
     * Selects our unfinished units.
     */
    public static Select<AUnit> ourRealUnits() {
        List<AUnit> data = new ArrayList<>();

        for (AUnit unit : ourUnits()) {

            if (unit.exists() && unit.isCompleted() && !unit.getType().isBuilding() && !unit.isNotActuallyUnit()) {
                data.add(unit);
            }
        }

        return new Select<AUnit>(data);
    }

    /**
     * Selects our unfinished units.
     */
    public static Select<AUnit> ourUnfinishedRealUnits() {
        List<AUnit> data = new ArrayList<>();

        for (AUnit unit : ourUnits()) {

            if (unit.exists() && !unit.isCompleted() && !unit.getType().isBuilding() && !unit.isNotActuallyUnit()) {
                data.add(unit);
            }
        }

        return new Select<AUnit>(data);
    }

    /**
     * Selects all visible enemy units. Since they're visible, the parameterized type is AUnit
     */
    public static Select<AUnit> enemy() {
        List<AUnit> data = new ArrayList<>();

        //TODO: check whether enemy().getUnits() has the same behavior as  getEnemyUnits()
        for (AUnit unit : enemyUnits()) {
            if (unit.isVisible() && unit.getHitPoints() >= 1) {
                data.add(unit);
            }
        }

        return new Select<AUnit>(data);
    }

    /**
     * Selects all visible enemy units. Since they're visible, the parameterized type is AUnit
     */
    public static Select<AUnit> enemy(boolean includeGroundUnits, boolean includeAirUnits) {
        List<AUnit> data = new ArrayList<>();

        for (AUnit unit : enemyUnits()) {
            if (unit.isVisible() && unit.getHitPoints() >= 1) {
                if ((!unit.isAirUnit()&& includeGroundUnits) || (unit.isAirUnit()&& includeAirUnits)) {
                    data.add(unit);
                }
            }
        }

        return new Select<AUnit>(data);
    }

    /**
     * Selects all visible enemy units. Since they're visible, the parameterized type is AUnit
     */
    public static Select<AUnit> enemyRealUnits() {
        List<AUnit> data = new ArrayList<>();

        for (AUnit unit : enemyUnits()) {
            if (unit.exists() && unit.isVisible() && !unit.getType().isBuilding() 
                    && !unit.isNotActuallyUnit()) {
                data.add(unit);
            }
        }

        return new Select<AUnit>(data);
    }

    /**
     * Selects all visible enemy units. Since they're visible, the parameterized type is AUnit
     */
    public static Select<AUnit> enemyRealUnits(boolean includeGroundUnits, boolean includeAirUnits) {
        List<AUnit> data = new ArrayList<>();

        for (AUnit unit : enemyUnits()) {
            if (unit.exists() && unit.isVisible() && !unit.getType().isBuilding() 
                    && ! unit.isType(AUnitType.Zerg_Larva, AUnitType.Zerg_Egg)) {
                if ((!unit.isAirUnit() && includeGroundUnits) || (unit.isAirUnit() && includeAirUnits)) {
                    data.add(unit);
                }
            }
        }

        return new Select<AUnit>(data);
    }

    /**
     * Selects all visible neutral units (minerals, geysers, critters). Since they're visible, the
     * parameterized type is AUnit
     */
    public static Select<AUnit> neutral() {
//        List<AUnit> data = new ArrayList<>();
//
//        data.addAll(neutralUnits());

        return new Select<AUnit>(neutralUnits());
    }

    /**
     * Selects all (accessible) minerals on the map.
     */
    public static Select<AUnit> minerals() {
//        /*Units units = new AUnits();

        List<AUnit> data = new ArrayList<>();

        for (Unit u : neutralUnitsBWMirror()) {
            data.add(AUnit.createFrom(u));
        }

        return new Select<>(data);
    }

    /**
     * Selects all geysers on the map.
     */
    public static Select<AUnit> geysers() {
        /*Units units = new AUnits();

        units.addUnits(Atlantis.getBwapi().getNeutralUnits());*/
        Select<AUnit> selectUnits = neutral();

        return (Select<AUnit>) selectUnits.ofType(AUnitType.Resource_Vespene_Geyser);
    }

    /**
     * Create initial search-pool of units from given collection of units.
     */
    public static Select<AUnit> from(List<AUnit> units) {
        Select<AUnit> selectUnits = new Select<AUnit>(units);
        return selectUnits;
    }

    /**
     * Create initial search-pool of units from given collection of units.
     */
    public static Select<UnitData> fromData(Collection<UnitData> units) {
        Select<UnitData> selectUnits = new Select<UnitData>(units);
        return selectUnits;
    }

    /**
     * Returns all units that are closer than <b>maxDist</b> tiles from given <b>position</b>.
     */
    public Select<?> inRadius(double maxDist, Position position) {
        Iterator<T> unitsIterator = data.iterator();// units.iterator();
        while (unitsIterator.hasNext()) {
            APositionedObject unit = (APositionedObject) unitsIterator.next();
            if (unit.distanceTo(position) > maxDist) {
                unitsIterator.remove();
            }
        }

        return this;
    }

    // =====================================================================
    // Filter units
    /**
     * Selects only units of given type(s).
     */
    public Select<?> ofType(AUnitType... types) {
        Iterator<T> unitsIterator = data.iterator();
        while (unitsIterator.hasNext()) {
            Object unitOrData = unitsIterator.next();
            boolean typeMatches = (unitOrData instanceof AUnit ? typeMatches((AUnit) unitOrData, types) : typeMatches((UnitData) unitOrData, types));
            if (!typeMatches) {
                unitsIterator.remove();
            }
        }

        return this;
    }

    /**
     * Returns whether the type in needle matches one in the haystack
     *
     * @param AUnit|UnitData needle
     * @param haystack
     * @return
     */
    private boolean typeMatches(AUnit needle, AUnitType... haystack) {
        AUnit unit = unitFrom(needle);

        for (AUnitType type : haystack) {
            if (unit.getType().equals(type)
                    || (unit.getType().equals(AUnitType.Zerg_Egg) && unit.getBuildType().equals(type))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether the type in needle matches one in the haystack
     *
     * @param needle
     * @param haystack
     * @return
     */
    private boolean typeMatches(UnitData needle, AUnitType... haystack) {

        for (AUnitType type : haystack) {
            if (needle.getType().equals(type)
                    || (needle.getType().equals(AUnitType.Zerg_Egg) && needle.getBuildType().equals(type))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Selects only units of given type(s).
     */
    public int countUnitsOfType(AUnitType... types) {
        int total = 0;
        Iterator<T> unitsIterator = data.iterator();
        while (unitsIterator.hasNext()) {
            AUnit unit = unitFrom(unitsIterator.next());
            boolean typeMatches = false;
            for (AUnitType type : types) {
                if (unit.getType().equals(type)
                        || (unit.getType().equals(AUnitType.Zerg_Egg) && unit.getBuildType().equals(type))) {
                    typeMatches = true;
                    break;
                }
            }
            if (typeMatches) {
                total++;
            }
        }

        return total;
    }

    /**
     * Selects only those units which are idle. Idle is unit's class flag so be careful with that.
     */
    public Select<T> idle() {
        Iterator<T> unitsIterator = data.iterator();
        while (unitsIterator.hasNext()) {
            AUnit unit = unitFrom(unitsIterator.next());	//TODO: will probably not work with enemy units
            if (!unit.isIdle()) {
                unitsIterator.remove();
            }
        }

        return this;
    }

    /**
     * Selects units that are gathering minerals.
     */
    public Select<T> gatheringMinerals(boolean onlyNotCarryingMinerals) {
        Iterator<T> unitsIterator = data.iterator();
        while (unitsIterator.hasNext()) {
            AUnit unit = unitFrom(unitsIterator.next());	//TODO: will probably not work with enemy units
            if (!unit.isGatheringMinerals()) {
                if (onlyNotCarryingMinerals && !unit.isCarryingMinerals()) {
                    unitsIterator.remove();
                } else {
                    unitsIterator.remove();
                }
            }
        }

        return this;
    }

    /**
     * Selects units being infantry.
     */
    public Select<T> infantry() {
        Iterator<T> unitsIterator = data.iterator();
        while (unitsIterator.hasNext()) {
            UnitData unit = dataFrom(unitsIterator.next());	//(unitOrData instanceof AUnit ? (AUnit) unitOrData : ((UnitData)unitOrData).getUnit()); 
            if (!unit.getType().isOrganic()) { //replaced  isInfantry()
                unitsIterator.remove();
            }
        }

        return this;
    }

    /**
     * Selects only units that do not currently have max hit points.
     */
    public Select<T> wounded() {
        Iterator<T> unitsIterator = data.iterator();
        while (unitsIterator.hasNext()) {
            AUnit unit = unitFrom(unitsIterator.next());	//TODO: will work properly only on visible units
            // unit.getHitPoints() >= unit.getType().maxHitPoints() replaces !isWounded()
            if (unit.getHitPoints() >= unit.getMaxHitPoints()) {
                unitsIterator.remove();
            }
        }

        return this;
    }

    /**
     * Selects only buildings.
     */
    public Select<T> buildings() {
        Iterator<T> unitsIterator = data.iterator();
        while (unitsIterator.hasNext()) {
            UnitData uData = dataFrom(unitsIterator.next());
            if (!uData.getType().isBuilding()) {
                unitsIterator.remove();
            }
        }
        return this;
    }

    /**
     * Selects only units that can fight in any way including: 
     * - infantry including Terran Medics, but not workers 
     * - military buildings like Photon Cannon, Bunker, Spore Colony, Sunken Colony
     */
    public Select<T> combatUnits() {
        Iterator<T> unitsIterator = data.iterator();
        while (unitsIterator.hasNext()) {
            UnitData uData = dataFrom(unitsIterator.next());
            boolean isMilitaryBuilding = uData.getType().isMilitaryBuilding();
            AUnit u = uData.getUnit();	//TODO: will work only on visible units...
            if (u == null || !u.isCompleted() || !u.exists() || (uData.getType().isBuilding() && !isMilitaryBuilding)) {
                unitsIterator.remove();
            }
        }
        return this;
    }

    /**
     * Selects only those Terran vehicles that can be repaired so it has to be:<br />
     * - mechanical<br />
     * - not 100% healthy<br />
     */
    public Select<T> toRepair() {
        Iterator<T> unitsIterator = data.iterator();
        while (unitsIterator.hasNext()) {
            AUnit unit = unitFrom(unitsIterator.next());

            //isMechanical replaces  isRepairableMechanically
            //unit.getHitPoints() >= unit.getType().maxHitPoints() replaces isFullyHealthy
            if (!unit.getType().isMechanical() || unit.getHitPoints() >= unit.getMaxHitPoints() 
                    || !unit.isCompleted()) {
                unitsIterator.remove();
            }
        }
        return this;
    }

    /**
     * Selects only those units from current selection, which are both <b>capable of attacking</b> given unit
     * (e.g. Zerglings can't attack Overlord) and are <b>in shot range</b> to the given <b>unit</b>.
     */
    public Select<T> thatCanShoot(AUnit targetUnit) {
        Iterator<T> unitsIterator = data.iterator();
        while (unitsIterator.hasNext()) {
            AUnit unit = unitFrom(unitsIterator.next());
            if (!unit.isCompleted() || !unit.isAlive()) {
                boolean isInShotRange = unit.hasRangeToAttack(targetUnit, 0.5);
                if (!isInShotRange) {
                    unitsIterator.remove();
                } else {
                    System.out.println(unit.getType().getShortName() + " in range ("
                            + unit.distanceTo(targetUnit) + ") to attack " + targetUnit.getType().getShortName());
                }
            }
        }
        return this;
    }

    // =========================================================
    // Hi-level auxiliary methods
    
    /**
     * Selects all of our bases.
     */
    public static Select<AUnit> ourBases() {
        if (AtlantisGame.playsAsZerg()) {
            return (Select<AUnit>) ourIncludingUnfinished().ofType(AUnitType.Zerg_Hatchery, AUnitType.Zerg_Lair, 
                    AUnitType.Zerg_Hive, AUnitType.Protoss_Nexus, AUnitType.Terran_Command_Center);
        }
        else {
            return (Select<AUnit>) ourIncludingUnfinished().ofType(AtlantisConfig.BASE);
        }
    }

    /**
     * Selects our workers (that is of type Terran SCV or Zerg Drone or Protoss Probe).
     */
    public static Select<AUnit> ourWorkers() {
        Select<AUnit> selectedUnits = Select.our();
        //for (AUnit unit : selectedUnits.list()) {
//        System.out.println("########## OUR SIZE = " + selectedUnits.data.size());
        for (Iterator<AUnit> unitIter = selectedUnits.list().iterator(); unitIter.hasNext();) {
            AUnit unit = unitIter.next();
            
//            System.out.println(unit + " --> " +  !unit.isCompleted() + " / " +  !unit.isWorker() + " / " +  !unit.exists());
            
            if (!unit.isCompleted() || !unit.isWorker() || !unit.exists()) {
                unitIter.remove();
            }
        }
        return selectedUnits;
    }

    /**
     * Selects our workers (that is of type Terran SCV or Zerg Drone or Protoss Probe) that are either
     * gathering minerals or gas.
     */
    public static Select<AUnit> ourWorkersThatGather() {
        Select<AUnit> selectedUnits = Select.our();
        //for (AUnit unit : selectedUnits.list()) {
        for (Iterator<AUnit> unitIter = selectedUnits.list().iterator(); unitIter.hasNext();) {
            AUnit unit = unitIter.next();
            if (!unit.isWorker() || (!unit.isGatheringGas() && !unit.isGatheringMinerals())) {
                unitIter.remove();
            }
        }
        return selectedUnits;
    }

    /**
     * Selects our workers that are free to construct building or repair a unit. That means they mustn't
     * repait any other unit or construct other building.
     */
    public static Select<AUnit> ourWorkersFreeToBuildOrRepair() {
        Select<AUnit> selectedUnits = ourWorkers();

        for (Iterator<AUnit> unitIter = selectedUnits.list().iterator(); unitIter.hasNext();) {
            AUnit unit = unitIter.next();
            if (unit.isConstructing() || unit.isRepairing() || AtlantisConstructingManager.isBuilder(unit)) {
                unitIter.remove();
            }
        }

        return selectedUnits;
    }

    /**
     * Selects all our finished buildings.
     */
    public static Select<AUnit> ourBuildings() {
        return our().buildings();
    }

    /**
     * Selects all our buildings including those unfinished.
     */
    public static Select<AUnit> ourBuildingsIncludingUnfinished() {
        Select<AUnit> selectedUnits = Select.ourIncludingUnfinished();
        for (Iterator<AUnit> unitIter = selectedUnits.list().iterator(); unitIter.hasNext();) {
            AUnit unit = unitIter.next();
            if (!unit.getType().isBuilding()) {
                unitIter.remove();
            }
        }
        return selectedUnits;
    }

    /**
     * Selects all our tanks, both sieged and unsieged.
     */
    public static Select<AUnit> ourTanks() {
        //cast is safe 'cuz our units are visible
        return (Select<AUnit>) our().ofType(AUnitType.Terran_Siege_Tank_Siege_Mode, AUnitType.Terran_Siege_Tank_Tank_Mode);
    }

    /**
     * Selects all our sieged tanks.
     */
    public static Select<AUnit> ourTanksSieged() {
        //cast is safe 'cuz our units are visible
        return (Select<AUnit>) our().ofType(AUnitType.Terran_Siege_Tank_Siege_Mode);
    }

    /**
     * Selects all of our Marines, Firebats, Ghosts and Medics.
     */
    public static Select<AUnit> ourTerranInfantry() {
        //cast is safe 'cuz our units are visible
        return (Select<AUnit>) our().ofType(AUnitType.Terran_Marine, AUnitType.Terran_Medic,
                AUnitType.Terran_Firebat, AUnitType.Terran_Ghost);
    }

    /**
     * Selects all of our Marines, Firebats, Ghosts.
     */
    public static Select<AUnit> ourTerranInfantryWithoutMedics() {
        //cast is safe 'cuz our units are visible
        return (Select<AUnit>) our().ofType(AUnitType.Terran_Marine,
                AUnitType.Terran_Firebat, AUnitType.Terran_Ghost);
    }

    /**
     * Selects all of our Zerg Larvas.
     */
    public static Select<AUnit> ourLarva() {
        Select<AUnit> selectedUnits = Select.ourIncludingUnfinished();
        for (Iterator<AUnit> unitIter = selectedUnits.list().iterator(); unitIter.hasNext();) {
            AUnit unit = unitIter.next();
            if (!unit.getType().equals(AUnitType.Zerg_Larva)) {
                unitIter.remove();
            }
        }
        return selectedUnits;
    }

    /**
     * Selects all of our Zerg Eggs.
     */
    public static Select<AUnit> ourEggs() {
        Select<AUnit> selectedUnits = Select.ourIncludingUnfinished();
        for (Iterator<AUnit> unitIter = selectedUnits.list().iterator(); unitIter.hasNext();) {
            AUnit unit = unitIter.next();
            if (!unit.getType().equals(AUnitType.Zerg_Egg)) {
                unitIter.remove();
            }
        }
        return selectedUnits;
    }

    // =========================================================
    // Localization-related methods
    /**
     * From all units currently in selection, returns closest unit to given <b>position</b>.
     */
    public T nearestTo(Position position) {
        if (data.isEmpty() || position == null) {
            return null;
        }

        sortDataByDistanceTo(position, true);
        return data.get(0);	//first();
    }

    /**
     * Returns first unit being base. For your units this is most likely your main base, for enemy it will be
     * first discovered base.
     */
    public static AUnit mainBase() {
        if (_cached_mainBase == null || !_cached_mainBase.isAlive()) {
            List<AUnit> bases = ourBases().list();
            _cached_mainBase = bases.isEmpty() ? null : bases.get(0);
        }
        return _cached_mainBase;
    }

    /**
     * Returns second (natural) base <b>or if we have only one base</b>, it returns the only base we have.
     */
    public static AUnit secondBaseOrMainIfNoSecond() {
        Collection<AUnit> bases = Select.ourBases().list();

        int counter = 0;
        for (AUnit base : bases) {
            if (bases.size() <= 1) {
                return base;
            } else if (counter > 0) {
                return base;
            }

            counter++;
        }

        return null;
    }

    /**
     * Returns first idle our unit of given type or null if no idle units found.
     */
    public static AUnit ourOneIdle(AUnitType type) {
        for (AUnit unit : ourUnits()) {
            if (unit.isCompleted() && unit.isIdle() && unit.getType().equals(type)) {
                return unit;
            }
        }
        return null;
    }

    /**
     * Returns nearest enemy to the given position (or unit).
     */
    public static AUnit nearestEnemy(Position position) {
        return Select.enemy().nearestTo(position);
    }

    // =========================================================
    // Auxiliary methods
    /**
     * Returns <b>true</b> if current selection contains at least one unit.
     */
    public boolean anyExists() {
        return !data.isEmpty();
    }

    /**
     * Returns first unit that matches previous conditions or null if no units match conditions.
     */
    public T first() {
        return data.isEmpty() ? null : data.get(0);	// first();
    }

    /**
     * Returns random unit that matches previous conditions or null if no units matched all conditions.
     */
    public T random() {
        return (T) AtlantisUtilities.getRandomElement(data); //units.random();
    }

    /**
     * Returns a AUnit out of an entity that is either a AUnit or UnitData
     *
     * @param unitOrData
     * @return
     */
    private AUnit unitFrom(Object unitOrData) {
        return (unitOrData instanceof AUnit ? (AUnit) unitOrData : ((UnitData) unitOrData).getUnit());
    }

    /**
     * Returns a UnitData out of an entity that is either a AUnit or UnitData
     *
     * @param unitOrData
     * @return
     */
    private UnitData dataFrom(Object unitOrData) {
        return (unitOrData instanceof UnitData ? (UnitData) unitOrData : new UnitData((AUnit) unitOrData));
    }

    // =========================================================
    // Operations on set of units
    /**
     * @return all units except for the given one
     */
    public Select<T> exclude(T unitToExclude) {
        data.remove(unitToExclude);
        return this;
    }

    @SuppressWarnings("unused")
    private Select<T> filterOut(Collection<T> unitsToRemove) {
        data.removeAll(unitsToRemove);
        return this;
    }

    // private Select filterOut(AUnit unitToRemove) {
    // // units.removeUnit(unitToRemove);
    // Iterator<AUnit> unitsIterator = units.iterator();
    // while (unitsIterator.hasNext()) {
    // AUnit unit = unitsIterator.next();
    // if (unitToRemove.equals(unit)) {
    // units.removeUnit(unit);
    // }
    // }
    // return this;
    // }
    @SuppressWarnings("unused")
    private Select<T> filterAllBut(T unitToLeave) {
        Iterator<T> unitsIterator = data.iterator();
        while (unitsIterator.hasNext()) {
            T unit = unitsIterator.next();
            if (unitToLeave != unit) {
                data.remove(unit);
            }
        }
        return this;
    }

    @Override
    public String toString() {
        String string = "Units (" + data.size() + "):\n";

        for (Object unitOrData : data) {
            AUnit unit = unitFrom(unitOrData);
            string += "   - " + unit.getType() + " (ID:" + unit.getID() + ")\n";
        }

        return string;
    }

    // =========================================================
    // Get results
    /**
     * Selects units that match all previous criteria. <b>Units</b> class is used as a wrapper for result. See
     * its javadoc too learn what it can do.
     */
//    public AUnitsData unitsData() { 
//        return data;
//    }
    /**
     * Selects result as an iterable collection (list).
     */
    public List<T> list() {
        return data;
    }

    /**
     * Selects units as an iterable collection (list).
     */
    public List<AUnit> listUnits() {
        return (List<AUnit>) data;
    }

    /**
     * Returns number of units matching all previous conditions.
     */
    public int count() {
        return data.size();
    }

    /**
     * Sorts data list by distance to a given position
     *
     * @param position
     * @param nearestFirst
     * @return
     */
    public List<T> sortDataByDistanceTo(final Position position, final boolean nearestFirst) {
        if (position == null) {
            return null;
        }

        Collections.sort(data, new Comparator<T>() {
            @Override
            public int compare(T p1, T p2) {
                if (p1 == null || !(p1 instanceof PositionedObject)) {
                    return -1;
                }
                if (p2 == null || !(p2 instanceof PositionedObject)) {
                    return 1;
                }
                UnitData data1 = dataFrom(p1);
                UnitData data2 = dataFrom(p2);
                double distance1 = PositionUtil.distanceTo(position, data1.getPosition());	//TODO: check whether this doesn't mix up position types
                double distance2 = PositionUtil.distanceTo(position, data2.getPosition());
                if (distance1 == distance2) {
                    return 0;
                } else {
                    return distance1 < distance2 ? (nearestFirst ? -1 : 1) : (nearestFirst ? 1 : -1);
                }
            }
        });

        return data;
    }

}
