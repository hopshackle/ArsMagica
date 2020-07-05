package hopshackle.simulation.arsmagica;

import hopshackle.simulation.*;

public class SearchForApprentice extends ArsMagicaAction {
	
	private Magus apprentice;

	public SearchForApprentice(Magus a) {
		super(MagusActions.SEARCH_APPRENTICE, a);
	}

	@Override
	protected void doStuff() {
		int searchRoll = Dice.stressDieResult() + magus.getPerception() + magus.getLevelOf(Abilities.AREA_LORE);
		if (magus.getHermeticHouse() != null)
			searchRoll += magus.getHermeticHouse().getApprenticeshipModifier();
		Covenant covenant = magus.getCovenant();
		Tribunal tribunal = magus.getTribunal();
		if (tribunal != null)
			searchRoll += tribunal.getApprenticeModifier();
		if (covenant != null)
			searchRoll += covenant.getLevelOf(CovenantAttributes.MUNDANE_CONNECTIONS);
		if (magus.getUniqueID() < 14) // i.e. is a founder
			searchRoll += 6;
		if (searchRoll >= 12) {
			apprentice = new Magus(magus.getLocation(), new MagusBaseDecider(), magus.getWorld());
			apprentice.setAge(6 + Dice.roll(1, 10));
			int quality = 2 + (searchRoll - 12) / 3;
			apprentice.rollStatistics(quality);
			if (tribunal != null)
				tribunal.registerApprentice(apprentice);
			magus.addApprentice(apprentice);
		} else {
			magus.log("Searches unsuccessfully for apprentice.");
		}
		int roll = Dice.roll(1, 10);
		Learnable skillUsed = Abilities.AREA_LORE;
		if (roll > 5 && roll < 9)
			skillUsed = Abilities.CHARM;
		if (roll > 9)
			skillUsed = Abilities.LEADERSHIP;
		magus.addXP(skillUsed, 5);
		if (covenant != null)
			covenant.addXP(CovenantAttributes.MUNDANE_CONNECTIONS, -1);
	}
	
	public String description() {
		return (apprentice == null) ? "None found." : apprentice.toString();
	}
}
