package hopshackle.simulation.arsmagica;

public class InTwilight extends ArsMagicaAction {

	private int seasons;

	public InTwilight(Magus a, int seasons) {
		super(MagusActions.TWILIGHT, a, 1);
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
	public void doNextDecision() {
		if (seasons > 1) {
			InTwilight nextSeason = new InTwilight(magus, seasons - 1);
			nextSeason.addToAllPlans();
		} else {
			super.doNextDecision();
		}
	}
	
	public String description() {
		return "In Twilight";
	}
	
}
