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
	protected void doNextDecision() {
		// TODO: If intwilight, then may need to do something here
		super.doNextDecision();	// will sort out any apprentice; and take action if not in Twilight
	}
	
	public String description() {
		return "In Twilight";
	}
	
}
