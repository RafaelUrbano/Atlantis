package atlantis.combat.micro;

import atlantis.units.AUnit;

public class DefaultRangedManager extends MicroRangedManager {

    @Override
    public boolean update(AUnit unit) {
        System.err.println("##########################################");
        System.err.println("## Currently this is inactive ############");
        System.err.println("## Class: DefaultMeleeManager ############");
        System.err.println("## is the only active micro manager ######");
        System.err.println("##########################################");
        System.exit(-1);
        return false;
    }

}
