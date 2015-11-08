package hopshackle.simulation.arsmagica;

import java.util.*;
import hopshackle.simulation.*;

public class CovenantApplication {

	private Magus applicant;
	private Covenant covenant;
	private int baseRoll;
	private int seasonsService;
	private double modifiedRoll, negativeValue;
	private List<Artefact> joiningFee = new ArrayList<Artefact>();

	public CovenantApplication(Covenant covenant, Magus applicant) {
		this(covenant, applicant, -99);
	}
	public CovenantApplication(Covenant covenant, Magus applicant, int rollOverride) {
		this.covenant = covenant;
		this.applicant = applicant;
		if (applicant.getCovenant() != null) {
			negativeValue = applicant.getCovenant().getBuildPoints();
			negativeValue += getSocialModifier(applicant, applicant.getCovenant()) * 10;
		} else {
			for (int i = 1; i <= applicant.getMagicAura(); i++)
				negativeValue += i * 10;
		}
		baseRoll = Dice.stressDieResult();
		if (rollOverride > -99) 
			baseRoll = rollOverride;

		int size = covenant.getCurrentSize();
		modifiedRoll = baseRoll + applicant.getIntelligence() + applicant.getPresence() + applicant.getLevelOf(Abilities.CHARM);
		modifiedRoll -= size;
		int availableCapacity = covenant.getCapacity() - size;
		if (availableCapacity < 0) modifiedRoll -= 100;
		switch (availableCapacity) {
		case 0: modifiedRoll -= 100;
		case 1: modifiedRoll -= 3;
		case 2: modifiedRoll -= 2;
		case 3: modifiedRoll -= 1;
		case 4: modifiedRoll -= 1;
		default:
		}
		modifiedRoll -= Math.max(size - covenant.getLevelOf(CovenantAttributes.WEALTH) * 2, 0);	// if not currently able to support all members in comfort

		modifiedRoll += getSocialModifier(applicant, covenant);

		if (covenant.getBuildPoints() < size * 50) {
			modifiedRoll -= (size * 50 - covenant.getBuildPoints()) / 25.0;
		}

		if (modifiedRoll < 9) {
			// consider bonuses
			List<Book> personalBooks = applicant.getInventoryOf(AMU.sampleBook);
			int bookValueToCovenant = covenant.calculateIncrementalBuildPointsFrom(personalBooks);
			if (bookValueToCovenant > 0) {
				joiningFee.addAll(personalBooks);
				modifiedRoll += bookValueToCovenant / 3.0;
			}
		}

		if (modifiedRoll < 9) {			
			List<VisSource> sources = applicant.getInventoryOf(AMU.sampleVisSource);
			for (VisSource source : sources) {
				modifiedRoll += source.getAmountPerAnnum() * 5.0 / 3.0;
				negativeValue += source.getAmountPerAnnum() * 20.0;
				joiningFee.add(source);
				if (modifiedRoll >= 9)
					break;
			}
		}

		if (modifiedRoll < 9) {
			List<Vis> vis = applicant.getInventoryOf(AMU.sampleVis);
			double totalVis = (double) vis.size();
			int requiredPawns = (int) Math.ceil((9.0 - modifiedRoll) * 15.0);
			if (totalVis >= requiredPawns) {
				for (int i = 0; i < requiredPawns; i++) {
					joiningFee.add(vis.get(i));
				}
				negativeValue += requiredPawns * 0.8;
				modifiedRoll = 9;
			}
		}

		if (modifiedRoll < 9 && modifiedRoll > 6) {
			// can then use seasons service owed to get in
			seasonsService = (int) ((9 - modifiedRoll) * 4);
			modifiedRoll = 9;
		}
	}

	public static int getSocialModifier(Magus applicant, Covenant covenant) {
		int retValue = 0;
		for (Agent m : covenant.getCurrentMembership()) {
			retValue -= 5;
			retValue += SocialMeeting.relationshipModifier(applicant, (Magus) m);
			if (applicant.getParens() == m) retValue += 10; // was apprentice at this covenant
		}
		return retValue / 5;
	}


	public boolean isSuccessful() {
		return (modifiedRoll >= 9);
	}

	public int getNetValueToApplicant() {
		return covenant.getBuildPoints() - (int) negativeValue;
	}

	public boolean acceptApplication() {
		if (isSuccessful()) {
			for (Artefact gift : joiningFee) {
				applicant.removeItem(gift);
				covenant.addItem(gift);
			}
			applicant.setCovenant(covenant);
			applicant.setSeasonsServiceOwed(seasonsService);
			applicant.log("Agrees to " + seasonsService + " seasons service to Covenant.");
		}
		return isSuccessful();
	}
}
