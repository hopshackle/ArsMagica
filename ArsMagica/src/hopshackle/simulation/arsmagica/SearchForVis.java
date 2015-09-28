package hopshackle.simulation.arsmagica;

import java.util.*;

import hopshackle.simulation.*;

public class SearchForVis extends ArsMagicaAction {

	private boolean sourceOverride;
	private boolean isCovenantService;
	private VisSource visSource;

	public SearchForVis(Agent a) {
		super(a);
	}

	public void setResultAsVisSource(boolean findSource) {
		sourceOverride = findSource;
	}

	@Override
	protected void doStuff() {
		magus.log("Spends season seaching for Vis");
		Abilities skillUsed = Abilities.MAGIC_LORE;
		int roll = Dice.roll(1, 10);
		if (roll > 4 && roll < 6) 
			skillUsed = Abilities.FAERIE_LORE;
		if (roll == 7)
			skillUsed = Abilities.PHILOSOPHIAE;
		if (roll == 8)
			skillUsed = Abilities.ARTES_LIBERALES;
		if (roll > 8)
			skillUsed = Abilities.AREA_LORE;

		Tribunal tribunal = magus.getTribunal();
		Covenant covenant = magus.getCovenant();
		int easeFactor = 9 - magus.getLevelOf(skillUsed) - magus.getPerception() - magus.getLevelOf(Abilities.VIS_HUNT);
		if (covenant != null)
			easeFactor -= covenant.getLevelOf(CovenantAttributes.GROGS);
		if (tribunal != null)
			easeFactor -= tribunal.getVisModifier();

		roll = Dice.stressDieResult();
		if (roll < easeFactor) {
			magus.log("But fails to find any...");
			magus.addXP(Abilities.VIS_HUNT, 5);
		} else {
			magus.addXP(Abilities.VIS_HUNT, -magus.getTotalXPIn(Abilities.VIS_HUNT));
			int roll2 = Dice.roll(1, 10);
			if (roll2 == 6) 
				skillUsed = Abilities.PARMA_MAGICA;
			if (roll2 == 7)
				skillUsed = Abilities.CHARM;
			if (roll2 == 8)
				skillUsed = Abilities.LEADERSHIP;
			if (roll2 == 9)
				skillUsed = Abilities.PENETRATION;
			if (roll2 == 10)
				skillUsed = Abilities.FINESSE;

			int visAmount = getVisAmount();
			int bonus = (roll - easeFactor) / 3;
			Map<Learnable, Double> options = new HashMap<Learnable, Double>();
			for (int i = 0; i < 1 + bonus; i++) {
				options.put(Arts.random(), 1.0);
			}
			Arts visType = (Arts) MagusPreferences.getPreferenceGivenPriors(magus, options);
			magus.log(String.format("Finds %d pawns of %s (Bonus: %d)", visAmount * 2, visType.toString(), bonus));
			if (magus.getSeasonsServiceOwed() > 0) {
				for (int i = 0; i < visAmount * 2; i ++)
					covenant.addItem(new Vis(visType));
			} else {
				magus.addVis(visType, visAmount * 2);
			}

			if (Dice.stressDieResult() >= easeFactor || sourceOverride) {
				// permanent vis source found.
				visSource = new VisSource(visType, visAmount, tribunal);
				if (magus.getSeasonsServiceOwed() > 0)
					covenant.addItem(visSource);
				else
					magus.addItem(visSource);
				magus.log(String.format("Finds a %s vis source providing %d pawns per year", visType.toString(), visAmount));

				int auraLevel = Math.min(10, (int) Math.pow(visAmount, 0.62));
				if (auraLevel > magus.getMagicAura()) {
					if (covenant == null) {
						magus.log("Finds a better magic aura of level " + auraLevel);
						magus.setMagicAura(auraLevel);
					} else {
						double chanceOfMove = Math.pow(auraLevel - covenant.getAura(), 2) * 0.25;
						chanceOfMove = chanceOfMove / Math.sqrt(covenant.getCurrentSize());
						if (covenant.getCurrentSize() == 1) chanceOfMove = 1.0;
						if (Math.random() < chanceOfMove) {
							covenant.setAura(auraLevel);
							magus.log("Moves covenant to new site");
							covenant.log("Site is moved to better aura of level " + auraLevel + " found by " + magus.toString());
							for (CovenantAttributes attribute : CovenantAttributes.values()) {
								int currentXP = covenant.getTotalXPIn(attribute);
								covenant.addXP(attribute, -currentXP/2);
								covenant.addXP(attribute, -15);
							}
						} else {
							int effectiveAuraLevel = auraLevel;
							if (magus.getBestBookToRead() == null)
								effectiveAuraLevel += 1;	// exhausted library
							if (Math.pow(effectiveAuraLevel, 2) * 10 + Dice.roll(1, 100) > covenant.getBuildPoints()) {
								magus.log("Leaves " + covenant + " to reside in better aura of level " + auraLevel);
								magus.setCovenant(null);
								magus.setMagicAura(auraLevel);
							}
						}
					}
				}
			} 
		}
		magus.addXP(skillUsed, 5);
		if (covenant != null)
			covenant.addXP(CovenantAttributes.GROGS, -1);
		if (magus.getSeasonsServiceOwed() > 0) {
			magus.doSeasonsService();
			isCovenantService = true;
		}
	}

	private int getVisAmount() {
		int roll = Dice.roll(1, 6);
		int multiplier = 1;
		while (roll == 1) {
			roll = Dice.roll(1, 6);
			multiplier *= 2;
		}
		return (roll+1) * multiplier / 2;
	}
	
	public String description() {
		return (visSource == null) ? "None found." : visSource.toString();
	}
	
	public boolean isCovenantService() {
		return isCovenantService;
	}
}
