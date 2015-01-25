package hopshackle.simulation.arsmagica;

import hopshackle.simulation.*;

public class InTwilight extends ArsMagicaAction {

	private int seasons;

	public InTwilight(Agent a, int seasons) {
		super(a, 1);
		this.seasons = seasons;
		magus.setInTwilight(true);
	}

	@Override
	protected void doStuff() {
		magus.addTimeInTwilight(1);
		if (seasons <= 1)
			magus.setInTwilight(false);

	}

	@Override
	protected void doNextDecision() {
		if (magus.isInTwilight())
			magus.addAction(new InTwilight(magus, seasons-1));

		super.doNextDecision();	// will sort out any apprentice; and take action if not in Twilight
	}
}
