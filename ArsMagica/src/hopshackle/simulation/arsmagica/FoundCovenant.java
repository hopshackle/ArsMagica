package hopshackle.simulation.arsmagica;

import hopshackle.simulation.*;

import java.util.List;

public class FoundCovenant extends ArsMagicaAction {
	
	private List<Magus> allFounders;
	private Covenant newCovenant;

	public FoundCovenant(Magus founder, List<Magus> otherMembers) {
		super(founder);
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
		for (Agent m : otherMembers) {
			m.setActionOverride(new FoundCovenant(m, newCovenant));
		}
	}
	
	public FoundCovenant(Agent coFounder, Covenant covenant) {
		super(coFounder);
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

}
