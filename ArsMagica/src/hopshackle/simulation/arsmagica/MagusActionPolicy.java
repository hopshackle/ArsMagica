package hopshackle.simulation.arsmagica;

import hopshackle.simulation.*;

public class MagusActionPolicy extends Policy<Action<Magus>> {

	public MagusActionPolicy() {
		super("action");
	}

	@Override
	public double getValue(Action<Magus> proposal, Agent agent) {
		if (agent instanceof Magus) {
			Magus magus = (Magus) agent;
			ArsMagicaAction action = (ArsMagicaAction) proposal;
			if (magus.isInTwilight() && action instanceof InTwilight)
				return 50.0;
			if (magus.isApprentice() && action.getActor() == magus.getParens()) 
				return 25.0;	// overrides anything
			if (action instanceof FoundCovenant)
				return 20.0;
			if (action instanceof InventLongevityRitual) {
				InventLongevityRitual ritual = (InventLongevityRitual) action;
				if (ritual.isMandatoryParticipant(magus)) return 15.0;
				if (ritual.isOptionalParticipant(magus)) return 10.0;
			}
		}
		// otherwise use default value
		return 0.0;
	}

}
