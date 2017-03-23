package hopshackle.simulation.arsmagica;

import hopshackle.simulation.*;

public class SearchForFamiliar extends ArsMagicaAction {

	private boolean override;
	public SearchForFamiliar(Magus a) {
		super(MagusActions.SEARCH_FAMILIAR, a);
	}

	public void setResultAsFamiliar(boolean findSource) {
		override = findSource;
	}

	@Override
	protected void doStuff() {
		magus.log("Spends season seaching for Familiar");

		Learnable skillUsed = Abilities.MAGIC_LORE;
		Tribunal tribunal = magus.getTribunal();
		Covenant covenant = magus.getCovenant();
		int easeFactor = 6 - magus.getLevelOf(Abilities.MAGIC_LORE) - magus.getIntelligence() - magus.getLevelOf(Abilities.FAMILIAR_HUNT);
		if (covenant != null)
			easeFactor -= covenant.getLevelOf(CovenantAttributes.GROGS);
		if (tribunal != null)
			easeFactor -= tribunal.getVisModifier();

		int roll = Dice.stressDieResult();
		if (roll < easeFactor || override) {
			magus.log("But fails to find one...");
			magus.addXP(Abilities.FAMILIAR_HUNT, 5);
		} else {
			magus.addXP(Abilities.FAMILIAR_HUNT, -magus.getTotalXPIn(Abilities.FAMILIAR_HUNT));
			magus.log("Finds potential familiar");

			skillUsed = Abilities.CHARM;
			easeFactor = 9 - magus.getLevelOf(Abilities.CHARM) - magus.getCommunication() - magus.getMagicAura() - 
					magus.getLevelOf(magus.getHighestArt()) / 5;

			roll = Dice.stressDieResult();
			if (roll >= easeFactor || override) {
				magus.log("And persuades it to become one.");
				Familiar f = new Familiar(magus);
				magus.log("Gains a " + f);
				magus.addItem(f);
			} else {
				magus.log("And fails to persuade it to become one");
			}

		}
		magus.addXP(skillUsed, 5);
		if (covenant != null)
			covenant.addXP(CovenantAttributes.GROGS, -1);
	}

}
