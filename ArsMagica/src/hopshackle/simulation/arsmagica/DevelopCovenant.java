package hopshackle.simulation.arsmagica;

import java.util.HashMap;

import hopshackle.simulation.Dice;

public class DevelopCovenant extends ArsMagicaAction {

	private CovenantAttributes attributeToRaise;
	
	public DevelopCovenant(Magus magus) {
		super(MagusActions.DEVELOP_COVENANT, magus);
	}

	@Override
	protected void doStuff() {
		/* 
		 *  We give the covenant xp in either Wealth or Mundane Connections, based on the result of an appropriate skill roll.
		 *  
		 *  Also modify result by Arts?
		 *  
		 *  The Magus then gets xp in the relevant skill
		 */

		Covenant covenant = magus.getCovenant();
		HashMap<Learnable, Double> options = new HashMap<Learnable, Double>();
		for (CovenantAttributes attribute : CovenantAttributes.values()) {
			double baseValue = 1.0 / (covenant.getLevelOf(attribute) + 1.0);
			options.put(attribute, baseValue);
		}
		attributeToRaise = (CovenantAttributes) MagusPreferences.getPreferenceGivenPriors(magus, options);

		Abilities skillToUse = null;

		int stat = 0;
		switch (attributeToRaise) {
		case WEALTH:	
			skillToUse = Abilities.CHARM;
			stat = magus.getPresence() + (magus.getLevelOf(Arts.CREO) + magus.getLevelOf(Arts.TERRAM)) / 10;
			break;
		case GROGS:	
			skillToUse = Abilities.LEADERSHIP;
			stat = magus.getPresence() + (magus.getLevelOf(Arts.REGO) + magus.getLevelOf(Arts.MENTEM)) / 10;
			break;
		case MUNDANE_CONNECTIONS:
			skillToUse = Abilities.INTRIGUE;
			stat = magus.getCommunication() + (magus.getLevelOf(Arts.INTELLEGO) + magus.getLevelOf(Arts.MENTEM)) / 10;
			break;
		default:
		}

		int easeFactor = 3 + covenant.getLevelOf(attributeToRaise) - magus.getLevelOf(skillToUse) - stat;

		int diceRoll = Dice.stressDieResult();

		if (diceRoll < 1) {
			int botch = Dice.getBotchResult(1);
			if (botch > 0) {
				covenant.addXP(attributeToRaise, -botch * 20);
				magus.log("Botches while trying to increase covenant " + attributeToRaise);
			}
		}

		if (diceRoll >= easeFactor) {
			int xp = Math.max(5, diceRoll - easeFactor);
			covenant.addXP(attributeToRaise, xp);
			magus.log("Successfully works to increase " + attributeToRaise + " of covenant by " + xp + " xp points.");
		} else {
			magus.log("Fails to increase " + attributeToRaise + " of covenant.");
		}

		magus.addXP(skillToUse, 2);
		if (magus.getSeasonsServiceOwed() > 0)
			magus.doSeasonsService();
	}
	
	public String description() {
		return "Develops " + attributeToRaise;
	}
	
	public boolean isCovenantService() {
		return true;
	}

}
