package hopshackle.simulation.arsmagica;

import hopshackle.simulation.*;

public class PractiseAbility extends ArsMagicaAction {
	
	private Abilities abilityToPractise;
	
	public PractiseAbility(Agent a, Abilities ability) {
		super(a);
		abilityToPractise = ability;
	}
	
	@Override
	protected void doStuff() {
		magus.log("Spends season practising " + abilityToPractise);
		magus.addXP(abilityToPractise, 4);
	}

}
