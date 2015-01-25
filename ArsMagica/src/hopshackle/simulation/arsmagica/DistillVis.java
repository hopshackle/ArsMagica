package hopshackle.simulation.arsmagica;

import hopshackle.simulation.Agent;

public class DistillVis extends ArsMagicaAction {

	public DistillVis(Agent a) {
		super(a);
	}

	protected void doStuff() {
		int labTotal = magus.getLabTotal(Arts.CREO, Arts.VIM);
		int visProduced = labTotal / 10;
		magus.log("Distills Vis in laboratory and produces " + visProduced + " pawns.");
		if (magus.getSeasonsServiceOwed() > 0) {
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

}
