package hopshackle.simulation.arsmagica;

public class DistillVis extends ArsMagicaAction {

	private boolean isCovenantService;
	private int visProduced;
	
	public DistillVis(Magus a) {
		super(MagusActions.DISTILL_VIS, a);
	}

	protected void doStuff() {
		int labTotal = magus.getLabTotal(Arts.CREO, Arts.VIM);
		visProduced = labTotal / 10;
		magus.log("Distills Vis in laboratory and produces " + visProduced + " pawns.");
		if (magus.getSeasonsServiceOwed() > 0) {
			isCovenantService = true;
			if (magus.getCovenant() == null) {
				System.out.println(magus.toString() + " has no covenant.");
			}
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
