package hopshackle.simulation.arsmagica;

public class PractiseAbility extends ArsMagicaAction {
	
	private Abilities abilityToPractise;
	
	public PractiseAbility(Magus a, Abilities ability) {
		super(MagusActions.PRACTISE_ABILITY, a);
		abilityToPractise = ability;
	}
	
	@Override
	protected void doStuff() {
		magus.log("Spends season practising " + abilityToPractise);
		magus.addXP(abilityToPractise, 4);
	}
	
	public String description() {
		return abilityToPractise.toString();
	}
}
