package hopshackle.simulation.arsmagica;

import hopshackle.simulation.*;

public class ArsMagicaAction extends Action implements Persistent {
	
	private static AgentWriter<ArsMagicaAction> actionWriter = new AgentWriter<ArsMagicaAction>(new ActionDAO());
	static {actionWriter.setBufferLimit(100);}
	
	protected Magus magus;

	public ArsMagicaAction(Agent a) {
		this(a, 1);
	}
	public ArsMagicaAction(Agent a, int seasons) {
		super(a, 13 * seasons, false);
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
