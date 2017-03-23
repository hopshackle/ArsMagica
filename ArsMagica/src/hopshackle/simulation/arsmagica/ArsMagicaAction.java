package hopshackle.simulation.arsmagica;

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
	public String description() {
		return "";
	}

	@Override
	protected void doNextDecision() {
		if (magus.isDead())
			return;
		
		if (magus.getNextAction() == null && !magus.isInTwilight())
			super.doNextDecision();		// make own decision unless in Twilight
		
		if (magus.hasApprentice()) {
			Magus apprentice = magus.getApprentice();
			// and now decide how that impacts the apprentice
			ArsMagicaAction nextAction = (ArsMagicaAction) magus.getNextAction();
			if (nextAction != null && nextAction.requiresApprentice()) {
				Action<Magus> nextApprenticeAction = null;
				if (nextAction instanceof TeachApprentice)
					nextApprenticeAction = new BeTaught(apprentice);
				else 
					nextApprenticeAction = new LabAssistant(apprentice, magus);
				// TODO: Need to rework how we co-ordinate between Parens and Apprentice from scratch
			} else {
	//			apprentice.addAction(apprentice.decide());
			}
		}
	}

	@Override
	protected void doCleanUp() {
		super.doCleanUp();
		actionWriter.write(this, this.getWorld().toString());
	}
	
	public boolean requiresApprentice() {
		return false;
	}
	@Override
	public World getWorld() {
		return actor.getWorld();
	}
	
	public boolean isCovenantService() {
		return false;
	}
}
