package hopshackle.simulation.arsmagica;

import hopshackle.simulation.*;

import java.util.List;

public class TeachApprentice extends ArsMagicaAction {
	
	private Magus apprentice;

	public TeachApprentice(Agent parens) {
		super(parens);		
	}

	@Override
	protected void doStuff() {

		apprentice = magus.getApprentice();

		if (apprentice.getTotalSpellLevels() < apprentice.getTotalXPInArts() && apprentice.getTotalArtLevels() > 25 && apprentice.getYearsSinceStartOfApprenticeship() != 14) {
			teachSpells();
		} else {
			teachArtOrAbility();
		}
		
		apprentice.incrementSeasonsTraining();
		magus.addXP(Abilities.TEACHING, 2);
		
	}

	private void teachSpells() {
		// first filter spells that apprentice can actually learn
		// then pick one at random. 
		// get the teacher's labtotal (and if higher than best so far, update it as the maximum)
		// if spellLevels learnt so far, plus this one are still under or equal to the best lab total then learn spell
		// else try again
		// after three consecutive attempts to find a teachable spell, stop
		
		List<Spell> spellsAvailable = magus.getSpells();
		for (Spell s : magus.getSpells()) {
			int apprenticeLabTotal = apprentice.getLabTotal(s.getTechnique(), s.getForm()) + magus.getMagicAura();
			if (apprenticeLabTotal < s.getLevel())
				spellsAvailable.remove(s);
			if (apprentice.getSpells().contains(s))
				spellsAvailable.remove(s);	// already known
		}
		
		if (spellsAvailable.size() < 2) {	// not enough options to be worthwhile
			teachArtOrAbility();
			return;
		}
		
		int failedAttempts = 0;
		int totalSpellLevelsTaught = 0;
		int numberOfSpells = spellsAvailable.size();
		int withoutApprenticeModifier = apprentice.getIntelligence() + apprentice.getLevelOf(Abilities.MAGIC_THEORY);
		int highestLabTotal = 0;
		if (numberOfSpells == 0)
			return;
		do {
			Spell spellToTeach = spellsAvailable.get(Dice.roll(1, numberOfSpells) -1);
			int teachersLabTotal = magus.getLabTotal(spellToTeach.getTechnique(), spellToTeach.getForm()) - withoutApprenticeModifier;
			if (teachersLabTotal > highestLabTotal)
				highestLabTotal = teachersLabTotal;
			if (totalSpellLevelsTaught + spellToTeach.getLevel() <= highestLabTotal) {
				apprentice.log("Is taught " + spellToTeach);
				magus.log("Teaches apprentice " + spellToTeach);
				totalSpellLevelsTaught += spellToTeach.getLevel();
				apprentice.addSpell(spellToTeach);
				spellsAvailable.remove(spellToTeach);
				numberOfSpells--;
			} else {
				failedAttempts++;
			}
		} while (failedAttempts < 4 && numberOfSpells > 0);
	}

	private void teachArtOrAbility() {
		Learnable skillToBeTaught = null;
		if (apprentice.getLevelOf(Abilities.LATIN) < 4)
			skillToBeTaught = Abilities.LATIN;
		else if (apprentice.getLevelOf(Abilities.ARTES_LIBERALES) < 1)
			skillToBeTaught = Abilities.ARTES_LIBERALES;
		else if (apprentice.getLevelOf(Abilities.MAGIC_THEORY) < 2 && magus.getLevelOf(Abilities.MAGIC_THEORY) >= 2) 
			skillToBeTaught = Abilities.MAGIC_THEORY;
		else {	// pick an Art or Ability
			// teach the Art with biggest gap in levels between magus and apprentice
			int biggestGap = 0;
			for (Learnable skill : magus.getSkills().keySet()) {
				if (skill == Abilities.PARMA_MAGICA) continue;
				int gap = magus.getLevelOf(skill) - apprentice.getLevelOf(skill);
				if (skill instanceof Abilities)
					gap *= 2;
				if (gap > biggestGap) {
					biggestGap = gap;
					skillToBeTaught = skill;
				}
			}
		}
		
		if (apprentice.getYearsSinceStartOfApprenticeship() == 14 && magus.getLevelOf(Abilities.PARMA_MAGICA) > 0)
			skillToBeTaught = Abilities.PARMA_MAGICA;
		
		if (skillToBeTaught != null) {
			int xp = magus.getCommunication() + magus.getLevelOf(Abilities.TEACHING) + 9;
			int currentXP = apprentice.getTotalXPIn(skillToBeTaught);
			int parensXP = magus.getTotalXPIn(skillToBeTaught);
			if (currentXP > parensXP)
				return;
			magus.log("Teaches " + skillToBeTaught + " to apprentice.");
			apprentice.log("Is taught " + skillToBeTaught + " by parens.");
			if (currentXP + xp > parensXP)
				xp = parensXP - currentXP;
			apprentice.addXP(skillToBeTaught, xp);
		}
	}

	@Override
	public boolean requiresApprentice() {
		return true;
	}

}
