package hopshackle.simulation.arsmagica;

import hopshackle.simulation.*;

public class ArsMagicaAction extends Action {

	protected Magus magus;

	public ArsMagicaAction(Agent a) {
		this(a, 1);
	}
	public ArsMagicaAction(Agent a, int seasons) {
		super(a, 13 * seasons, false);
		magus = (Magus) a;
	}

	@Override
	protected void doNextDecision() {
		if (magus.isDead())
			return;
		if (magus.isApprentice()) {
//			return;	// apprentices take action as a result of their parens taking an action
		}
		if (magus.getNextAction() == null && !magus.isInTwilight())
			super.doNextDecision();		// make own decision unless in Twilight
		
		if (magus.hasApprentice()) {
			Agent apprentice = magus.getApprentice();
			// and now decide how that impacts the apprentice
			ArsMagicaAction nextAction = (ArsMagicaAction) magus.getNextAction();
			if (nextAction != null && nextAction.requiresApprentice()) {
				Action nextApprenticeAction = null;
				if (nextAction instanceof TeachApprentice)
					nextApprenticeAction = new BeTaught(apprentice);
				else 
					nextApprenticeAction = new LabAssistant(apprentice, magus);
				
				apprentice.setActionOverride(nextApprenticeAction);
			} else {
	//			apprentice.addAction(apprentice.decide());
			}
		}
	}

	public boolean requiresApprentice() {
		return false;
	}
}
