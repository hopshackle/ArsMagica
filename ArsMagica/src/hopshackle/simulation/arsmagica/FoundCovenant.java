package hopshackle.simulation.arsmagica;

import hopshackle.simulation.*;

import java.util.List;

public class FoundCovenant extends ArsMagicaAction {
	
	private List<Magus> allFounders;
	private Covenant newCovenant;

	public FoundCovenant(Magus founder, List<Magus> otherMembers) {
		super(MagusActions.FOUND_COVENANT, founder);
		allFounders = HopshackleUtilities.cloneList(otherMembers);
		allFounders.add(0, founder);
		if (allFounders.size() < 1) {
			logger.severe("No founders for Covenant");
			return;
		}
		if (founder == null) {
			logger.severe("No founder for covenant");
			return;
		}
		newCovenant = new Covenant(allFounders, founder.getTribunal());
		newCovenant.setAura(magus.getMagicAura());
		// TODO: Remove actionOverride - should now be dealt with via actionPlan
	}
	
	public FoundCovenant(Magus coFounder, Covenant covenant) {
		super(MagusActions.FOUND_COVENANT, coFounder);
		newCovenant = covenant;
	}

	@Override
	protected void doStuff() {
		if (allFounders != null) {
			magus.log("Founds " + newCovenant.toString());
		} else {
			magus.log("Co-founds " + newCovenant.toString());
		}
		magus.setCovenant(newCovenant);
		magus.addXP(Abilities.LEADERSHIP, 2);
	}
	
	public String description() {
		return "Founds " + newCovenant;
	}

}
