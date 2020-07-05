package hopshackle.simulation.arsmagica;

import hopshackle.simulation.*;

public class JoinCovenant extends ArsMagicaAction {

	private Covenant joinedCovenant;

	public JoinCovenant(Magus magus) {
		super(MagusActions.JOIN_COVENANT, magus);
	}

	public JoinCovenant(Magus magus, int offset) {
		super(MagusActions.JOIN_COVENANT, magus, offset, 1);
	}

	@Override
	protected void doStuff() {
		// for the moment we pick five covenants at random
		// make an attempt to Join each of them
		// and Join the best one (i.e. the highest Covenant score)
		// for the moment a Join roll is stress die + Prs + Charm + Magic Theory + Int - covenant size
		// we'll make this independent of anything else for the moment (but come back and change this later)

		Covenant[] covenants = magus.getTribunal().getAllChildLocationsOfType(AMU.sampleCovenant).toArray(new Covenant[1]);

		int numberOfCovenants = covenants.length;
		if (covenants[0] == null) {
			magus.log("Looks for covenant, but none exist.");
			magus.addXP(Abilities.AREA_LORE, 5);
			return;
		}
		CovenantApplication preferredApplication = null;
		int newCovenantScore = 0;
		for (int i = 0; i < 7; i++) {
			Covenant candidate = covenants[Dice.roll(1, numberOfCovenants) -1];
			CovenantApplication application = new CovenantApplication(candidate, magus);
			if (application.isSuccessful() && application.getNetValueToApplicant() > newCovenantScore) {
				newCovenantScore = application.getNetValueToApplicant();
				preferredApplication = application;
			}
		}

		if (preferredApplication == null) {
			magus.log("Applies to new Covenants, but fails to be accepted.");
			Tribunal next = magus.getFavouredTribunal();
			if (magus.getCovenant() == null && next != magus.getTribunal() && Math.random() < 0.25) {
				magus.log("Decides to move to new Tribunal");
				magus.setTribunal(next);
			} else if (magus.getCovenant() == null && Dice.roll(2, 100) < magus.getAge() && magus.getMagicAura() > 0) {
				magus.log("So decides to found a new one.");
				// TODO: Need to put logic here to actually create Foundcovenant action in plan
			}
		} else {
			magus.log("Successfully applies to new Covenant.");
			preferredApplication.acceptApplication();
			joinedCovenant = magus.getCovenant();
		}
		magus.addXP(Abilities.CHARM, 2);
		Decider<?> d = magus.getDecider();
		if (d instanceof MagusBaseDecider) {	// a truly horrendous hack for decider to keep track of covenant applications
			MagusBaseDecider bd = (MagusBaseDecider) d;
			bd.registerApplication(magus, magus.getWorld().getYear());
		}
	}
	
	public String description() {
		return (joinedCovenant == null) ? "Fails to join any covenant" : "Joins " + joinedCovenant.toString();
	}

}
