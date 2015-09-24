package hopshackle.simulation.arsmagica;

import hopshackle.simulation.Agent;

public class DistillVis extends ArsMagicaAction {

	private boolean isCovenantService;
	private int visProduced;
	
	public DistillVis(Agent a) {
		super(a);
	}

	protected void doStuff() {
		int labTotal = magus.getLabTotal(Arts.CREO, Arts.VIM);
		visProduced = labTotal / 10;
		magus.log("Distills Vis in laboratory and produces " + visProduced + " pawns.");
		if (magus.getSeasonsServiceOwed() > 0) {
			isCovenantService = true;
			for (int i = 0; i < visProduced; i++)
				magus.getCovenant().addItem(new Vis(Arts.VIM));
			magus.doSeasonsService();
		} else 
			magus.addVis(Arts.VIM, visProduced);

		magus.addXP(AMU.getPreferredXPGain(Arts.CREO, Arts.VIM, magus), 2);
	}

	@Override
	public boolean requiresApprentice() {
		return true;
	}
	
	public String description() {
		return "Distills " + visProduced + " pawns of Vis";
	}
	
	public boolean isCovenantService() {
		return isCovenantService;
	}

}
