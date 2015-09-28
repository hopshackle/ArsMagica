package hopshackle.simulation.arsmagica;

import hopshackle.simulation.Dice;

public enum Abilities implements Learnable {

	MAGIC_THEORY,
	AREA_LORE,
	LATIN,
	TEACHING,
	ARTES_LIBERALES,
	PHILOSOPHIAE,
	MAGIC_LORE,
	FAERIE_LORE,
	PARMA_MAGICA,
	PENETRATION,
	FINESSE,
	SCRIBE,
	CONCENTRATION,
	DECREPITUDE,
	WARPING, 
	LEADERSHIP, 
	CHARM, 
	VIS_HUNT,
	FAMILIAR_HUNT,
	INTRIGUE;
	
	public static Abilities random() {
		Abilities[] allAbilities = Abilities.values();
		int roll = Dice.roll(1, allAbilities.length);
		return allAbilities[roll-1];
	}
	
	public int getMultiplier() {
		return 5;
	}

	@Override
	public int getXPForLevel(int level) {
		int total = 0;
		for (int i = 1; i <= level; i++) {
			total += i * getMultiplier();
		}
		return total;
	}
}
