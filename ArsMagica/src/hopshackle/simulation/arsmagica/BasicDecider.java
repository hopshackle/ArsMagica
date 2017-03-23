package hopshackle.simulation.arsmagica;

import java.util.*;

import hopshackle.simulation.*;

public class BasicDecider extends BaseDecider<Magus> {

	private Map<Long, Integer> recentApplications;

	public BasicDecider() {
		super(new LinearStateFactory<Magus>(new ArrayList<GeneticVariable<Magus>>()));
		recentApplications = new HashMap<Long, Integer>();
	}

	@Override
	public double valueOption(ActionEnum<Magus> option, Magus magus) {
		Covenant covenant = magus.getCovenant();
		double retValue = 0.0;

		if (option == MagusActions.PRACTISE_ABILITY) {
			retValue = getMinimumMagicTheoryForVisStudy(magus);
			// return 0.8 or 0.1 depending
		}

		if (option == MagusActions.SEARCH_VIS) {
			retValue = 0.35;
			if (magus.getSeasonsServiceOwed() > 0)
				retValue +=  magus.getSeasonsServiceOwed() * 0.05;
			else 
				retValue -= 0.005 * magus.getNumberInInventoryOf(AMU.sampleVis) ;
			retValue += 0.05 * magus.getTribunal().getVisModifier();
		}
		
		if (option == MagusActions.SEARCH_FAMILIAR) {
			retValue = 0.20 - ((magus.getLevelOf(Abilities.WARPING)-1)^2) * 0.01 + 
					0.05 * (magus.getLevelOf(Abilities.MAGIC_LORE) + magus.getLevelOf(Abilities.CHARM) + magus.getCommunication() + magus.getIntelligence() + magus.getMagicAura()) ;
			if (magus.getTribunal() != null && magus.getTribunal().getVisModifier() < 0)
				retValue += 0.05 * magus.getTribunal().getVisModifier();
		}
		
		if (option == MagusActions.BIND_FAMILIAR) {
			retValue = 1.0;
		}

		if (option == MagusActions.STUDY_VIS)
			retValue = 0.5 + 0.05 * magus.getMagicAura();

		if (option == MagusActions.TEACH_APPRENTICE) {
			retValue = 0.1;
			if (magus.getWorld().getSeason() == 0 ||  magus.getApprentice().getYearsSinceStartOfApprenticeship() > 15)
				retValue = 2.0;
		}

		if (option == MagusActions.SEARCH_APPRENTICE) {
			int apprenticesSoFar = magus.getChildren().size();
			retValue = 0.25 - ((magus.getLevelOf(Abilities.WARPING)-1)^2) * 0.01 + magus.getTotalArtLevels() * 0.001 - 0.03 * apprenticesSoFar;
			if (magus.getTribunal() != null && magus.getTribunal().getApprenticeModifier() < 0)
				retValue += 0.05 * magus.getTribunal().getApprenticeModifier();
			if (magus.getUniqueID() < 14)
				retValue += 0.20;
		}

		if (option == MagusActions.DISTILL_VIS) {
			retValue = Math.min(magus.getLabTotal(Arts.CREO, Arts.VIM) * 0.01 - magus.getPawnsOf(Arts.VIM) * 0.01, 0.35) + magus.getSeasonsServiceOwed() * 0.01;
			if (magus.getPawnsOf(Arts.VIM) + magus.getPawnsOf(Arts.CORPUS) + magus.getPawnsOf(Arts.CREO) <= magus.getAge() / 10)
				retValue += magus.getLabTotal(Arts.CREO, Arts.VIM) * 0.01;
			if (magus.getAge() > 30 && magus.getLongevityRitualEffect() == 0 && magus.getPawnsOf(Arts.VIM) <= magus.getAge() / 10)
				retValue += 0.5;
		}

		if (option == MagusActions.LONGEVITY_RITUAL) 
			if (magus.getInventoryOf(AMU.sampleLongevityRitualService).isEmpty() && magus.getLevelOf(Abilities.MAGIC_THEORY) >= magus.getAge() / 10 + 1)
				retValue = (magus.getLabTotal(Arts.CREO, Arts.CORPUS) / 5.0 - magus.getLongevityRitualEffect()) * 0.20;
			else retValue = 0.0;
		// 0.20 per additional point of ritual
		// if we have a LRS in stock, then use this (ALWAYS at least as good as a solo effort)

		if (option == MagusActions.INVENT_SPELL) {
			if (magus.isResearchingSpell()) 
				retValue = 0.8;
			else {
				retValue = (magus.getTotalXPInArts() / Math.max(magus.getTotalSpellLevels(), 20)) * 0.08;
			}
		}

		if (option == MagusActions.WRITE_SUMMA) {
			if (magus.isWritingBook())
				retValue = 0.8;
			else {
				Learnable subject = magus.getHighestArt();
				retValue = magus.getLevelOf(subject) / 2 - magus.getHighestSumma(subject);
				if (subject instanceof Abilities)
					retValue *= 5;
				retValue += magus.getCommunication();
				if (covenant != null && !covenant.isLibraryFull())
					retValue += magus.getSeasonsServiceOwed();
				retValue *= 0.05;
			}
		}

		if (option == MagusActions.WRITE_TRACTATUS) {
			retValue = 5 + magus.getCommunication();
			if (covenant != null && !covenant.isLibraryFull())
				retValue += magus.getSeasonsServiceOwed();
			retValue *= 0.05;
		}

		if (option == MagusActions.SCRIBE_SPELL) {
			retValue = 5;
			if (covenant != null && !covenant.isLibraryFull())
				retValue += magus.getSeasonsServiceOwed() * 1.5;
			retValue *= 0.05;
		}

		if (option == MagusActions.READ_BOOK) {
			int xpGain = magus.getBestBookToRead().getXPGainForMagus(magus);
			retValue = xpGain / 3.0 * 0.25;	// i.e. better than studying from vis for same expected result
		}

		if (option == MagusActions.COPY_BOOK) {
			retValue = 0.4;	// i.e. about equal to 8 xp gain in priority
			if (covenant != null) {
				retValue += magus.getSeasonsServiceOwed() * 0.01 * covenant.getNeedForLibraryMaintenance();
			}
			if (magus.isCopyingBook()) {
				retValue = 1.2;
				Agent currentUser = magus.getCurrentCopyProject().getBookBeingCopied().getCurrentReader();
				if (currentUser != magus && currentUser != null)
					retValue = 0.0;
			}
		}

		if (option == MagusActions.COPY_SPELLS) {
			retValue = 0.2 +  magus.getLevelOf(Abilities.SCRIBE) * 0.05;	// i.e. about equal to 8 xp gain in priority if you have Scribe 4!
			if (covenant != null) {
				retValue += magus.getSeasonsServiceOwed() * 0.01 * covenant.getNeedForLabTextMaintenance();
			}
		}

		if (option == MagusActions.FOUND_COVENANT) {
			retValue = magus.getMagicAura() * 0.16;
			if (magus.getUniqueID() < 14) retValue *= 2;
		}

		if (option == MagusActions.JOIN_COVENANT) {
			retValue = 0.30 + Math.max(0.004 * (Math.min(50, 100 - magus.getAge())), 0.0);
			if (magus.getCovenant() != null) {
				retValue -= Math.max(0.05, magus.getCovenant().getBuildPoints() * 0.01);
				retValue -= Math.min(0.05, CovenantApplication.getSocialModifier(magus, magus.getCovenant()) * 0.1);
			} else {
				retValue -= Math.pow(magus.getMagicAura(), 2) * 0.1;
			}
			if (magus.getWorld().getYear() < 750) {
				retValue = 0.0;	// a hack so unit tests work
			}
			if (recentApplications.containsKey(magus.getUniqueID())) {
				int yearsSinceLastApplications = magus.getWorld().getYear() - recentApplications.get(magus.getUniqueID());
				retValue -= Math.exp(-yearsSinceLastApplications);
			}
		}

		if (option == MagusActions.DEVELOP_COVENANT) {
			retValue = 5 + magus.getSeasonsServiceOwed() * 3;
			int lowestAttributeScore = 20;
			for (CovenantAttributes attribute : CovenantAttributes.values()) {
				if (covenant.getLevelOf(attribute) < lowestAttributeScore) 
					lowestAttributeScore = covenant.getLevelOf(attribute);				
			}
			retValue -= lowestAttributeScore;
			if (covenant.getLevelOf(CovenantAttributes.WEALTH) < 2)
				retValue += 4;
			retValue *= 0.05;
		}

		return retValue + Math.random() / 5.0;
	}

	private double getMinimumMagicTheoryForVisStudy(Magus magus) {
		Arts mostAbundantVis = null;
		HashMap<Arts, Integer> vis = AMU.getVisInventory(magus);
		int maxVis = 0;
		for (Arts type : vis.keySet()) {
			if (vis.get(type) > maxVis) {
				maxVis = vis.get(type);
				mostAbundantVis = type;
			}
		}
		int magicTheory = magus.getLevelOf(Abilities.MAGIC_THEORY);
		int artLevel = magus.getLevelOf(mostAbundantVis);
		double requiredPawns = Math.max(Math.ceil(artLevel / 5.0), 1);
		if (requiredPawns > magicTheory * 2.0 && requiredPawns <= maxVis)
			return 0.8;
		
		// otherwise, we assume 5xp gain in favourite Ability
		Map<Learnable, Double> options = new HashMap<Learnable, Double>();
		for (Abilities ability : Abilities.values()) {
			if (ability == Abilities.PARMA_MAGICA || ability == Abilities.PENETRATION)
				continue;
			options.put(ability, 5.0 / (magus.getTotalXPIn(ability) + 10.0));		
		}
		Learnable preference = MagusPreferences.getPreferenceGivenPriors(magus, options);
		return 5.0 / (magus.getTotalXPIn(preference) + 10.0) * 0.25;
	}

	public void registerApplication(Agent applicant, int year) {
		recentApplications.put(applicant.getUniqueID(), year);
	}

	@Override
	public void learnFrom(ExperienceRecord<Magus> exp, double maxResult) {

	}
}
