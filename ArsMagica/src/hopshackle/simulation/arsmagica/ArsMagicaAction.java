package hopshackle.simulation.arsmagica;

import java.util.*;

import hopshackle.simulation.*;

public class ArsMagicaAction extends Action<Magus> implements Persistent {

	private static DatabaseWriter<ArsMagicaAction> actionWriter = new DatabaseWriter<ArsMagicaAction>(new ActionDAO());

	protected Magus magus;

	public ArsMagicaAction(ActionEnum<Magus> type, Magus a) {
		this(type, a, 1);
	}
	public ArsMagicaAction(ActionEnum<Magus> type, Magus a, int seasons) {
		super(type, a, 13 * seasons, false);
		magus = (Magus) a;
	}
	public ArsMagicaAction(ActionEnum<Magus> type, Magus a, int offset, int seasons) {
		super(type, a, 13 * offset, 13 * seasons, false);
		magus = (Magus) a;
	}

	public ArsMagicaAction(ActionEnum<Magus> type, List<Magus> mandatoryParticipants, List<Magus> optionalAssistants, int offset, int seasons) {
		super(type, mandatoryParticipants, optionalAssistants, 13 * offset, 13 * seasons, false);
		magus = (Magus) actor;
	}

	public String description() {
		return "";
	}

	@Override
	protected void doCleanUp() {
		super.doCleanUp();
		if (!isDeleted()) {
			List<Magus> allParticipants = new ArrayList<Magus>();
			allParticipants.addAll(mandatoryActors);
			allParticipants.addAll(optionalActors);
			new SocialMeeting(allParticipants, 0, 0);
			actionWriter.write(this, this.getWorld().toString());
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
