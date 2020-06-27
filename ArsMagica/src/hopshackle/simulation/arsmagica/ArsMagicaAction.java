package hopshackle.simulation.arsmagica;

import java.util.*;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import hopshackle.simulation.*;

public class ArsMagicaAction extends Action<Magus> implements Persistent {

    protected Magus magus;

    public ArsMagicaAction(ActionEnum<Magus> type, Magus a) {
        this(type, a, 1);
    }

    public ArsMagicaAction(ActionEnum<Magus> type, Magus a, int seasons) {
        super(type, a, 13 * seasons, false);
        magus = a;
    }

    public ArsMagicaAction(ActionEnum<Magus> type, Magus a, int offset, int seasons) {
        super(type, a, 13 * offset, 13 * seasons, false);
        magus = a;
    }

    public ArsMagicaAction(ActionEnum<Magus> type, List<Magus> mandatoryParticipants, List<Magus> optionalAssistants, int offset, int seasons) {
        super(type, mandatoryParticipants, optionalAssistants, 13 * offset, 13 * seasons, false);
        magus = actor;
    }

    @Override
    public int compareTo(Delayed d) {
        int retValue = super.compareTo(d);
        if (retValue == 0) {
            ArsMagicaAction other = (ArsMagicaAction) d;
            retValue = (int) (this.magus.getUniqueID() - other.magus.getUniqueID());
        }
        return retValue;
    }

    public String description() {
        return "";
    }

    @Override
    protected void doCleanUp() {
        super.doCleanUp();
        if (!isCancelled()) {
            List<Magus> allParticipants = new ArrayList<Magus>();
            allParticipants.addAll(mandatoryActors);
            allParticipants.addAll(optionalActors);
            if (allParticipants.size() > 1)
                new SocialMeeting(allParticipants, 0, 0);
            DatabaseWriter<ArsMagicaAction> actionWriter = world.getDBWriter(ArsMagicaAction.class);
            if (actionWriter != null) actionWriter.write(this, this.getWorld().toString());
        }
    }

    @Override
    public World getWorld() {
        return actor.getWorld();
    }

    public void exposureXPForParticipants(Arts technique, Arts form, int xp) {
        for (Magus participant : mandatoryActors) {
            participant.addXP(AMU.getPreferredXPGain(technique, form, participant), xp);
        }
        for (Magus participant : optionalActors) {
            participant.addXP(AMU.getPreferredXPGain(technique, form, participant), xp);
        }
    }

    public boolean isCovenantService() {
        return false;
    }
}
