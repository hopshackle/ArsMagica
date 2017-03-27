package hopshackle.simulation.arsmagica;

import hopshackle.simulation.Dice;

import java.util.*;

public class FoundCovenant extends ArsMagicaAction {
	
	private Covenant newCovenant;
	
	public FoundCovenant(List<Magus> founders) {
		super(MagusActions.FOUND_COVENANT, founders, new ArrayList<Magus>(), 0, 1);
		if (founders.size() < 1) {
			logger.severe("No founders for Covenant");
			return;
		}
	}

	@Override
	protected void doStuff() {
		newCovenant = new Covenant(mandatoryActors, magus.getTribunal());
		int capacity = Math.max(3 + (Dice.stressDieResult() + Dice.stressDieResult()) / 2 - magus.getMagicAura(), 1);
		newCovenant.setAuraAndCapacity(magus.getMagicAura(), capacity);
		String verb = "Co-founds ";
		if (mandatoryActors.size() < 2) verb = "Founds ";
		for (Magus m : mandatoryActors) {
			m.log(verb + newCovenant.toString());
			m.setCovenant(newCovenant);
			m.addXP(Abilities.LEADERSHIP, 2);
		}
	}
	
	public String description() {
		return newCovenant + " founded";
	}

}
