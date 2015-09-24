package hopshackle.simulation.arsmagica;

import java.util.*;

import hopshackle.simulation.*;

public class InventLongevityRitual extends ArsMagicaAction {

	private Magus customer;
	private int modifier;

	public InventLongevityRitual(Agent a) {
		super(a);
	}

	public InventLongevityRitual(Magus crCoSpecialist, Magus customer) {
		// when one Magus invents a ritual for another
		super(crCoSpecialist);
		this.customer = customer;
	}

	@Override
	public boolean requiresApprentice() {
		return true;
	}

	@Override
	protected void doStuff() {
		Magus subject = magus;
		int labTotal = magus.getLabTotal(Arts.CREO, Arts.CORPUS);
		if (customer != null) {
			subject = customer;
			labTotal = magus.getLabTotal(Arts.CREO, Arts.CORPUS, customer);
			int numberOfAssistants = Math.max(1, magus.getLevelOf(Abilities.LEADERSHIP));
			if (numberOfAssistants > 2 && magus.hasApprentice()) {
				labTotal += magus.getApprentice().getIntelligence();
				labTotal += magus.getApprentice().getLevelOf(Abilities.MAGIC_THEORY);
				numberOfAssistants--;
			}
			if (numberOfAssistants > 2 && customer.hasApprentice()) {
				labTotal += customer.getApprentice().getIntelligence();
				labTotal += customer.getApprentice().getLevelOf(Abilities.MAGIC_THEORY);
				numberOfAssistants--;
			}
		}
		modifier = (int) Math.ceil(labTotal / 5.0);
		if (hasSufficientVis(subject)) {
			for (Vis v : requirementsForRitual(subject))
				subject.removeItem(v);
			subject.setLongevityRitualEffect(modifier);
			if (customer == null) 
				magus.log("Invents a Longevity Ritual of potency " + modifier);
			else {
				magus.log("Invents a Longevity Ritual of potency " + modifier + " for " + customer);
				customer.log("Receives Longevity Ritual of potency " + modifier + " from " + magus);
			}
			magus.addXP(AMU.getPreferredXPGain(Arts.CREO, Arts.CORPUS, magus), 2);
		} else {
			modifier = 0;
			magus.log("Insufficient vis for Longevity Ritual");
			if (customer != null)
				customer.log("Insufficient vis for Longevity Rutual");
		}
	}

	public static boolean hasSufficientVis(Magus subject) {
		return !requirementsForRitual(subject).isEmpty();
	}

	public static List<Vis> requirementsForRitual(Magus visPayer) {
		List<Vis> retValue = new ArrayList<Vis>();
		int pawnsRequired = (int) Math.ceil(visPayer.getAge() / 10.0);
		int creoPawns = visPayer.getPawnsOf(Arts.CREO);
		int vimPawns = visPayer.getPawnsOf(Arts.VIM);
		int corpusPawns = visPayer.getPawnsOf(Arts.CORPUS);
		int pawnsSpent = 0;
		int vim = 0, creo = 0, corpus = 0;
		do {
			if (vimPawns > 0 && vimPawns >= creoPawns && vimPawns >= corpusPawns) {
				vim++;
				vimPawns--;
				pawnsSpent++;
				continue;
			}
			if (corpusPawns > 0 && corpusPawns >= creoPawns) {
				corpus++;
				corpusPawns--;
				pawnsSpent++;
				continue;
			}
			if (creoPawns > 0) {
				creo++;
				creoPawns--;
				pawnsSpent++;
				continue;
			}
			return retValue;
		} while (pawnsRequired > pawnsSpent);

		for (int i = 0; i < creo; i++) 
			retValue.add(new Vis(Arts.CREO));
		for (int i = 0; i < corpus; i++) 
			retValue.add(new Vis(Arts.CORPUS));
		for (int i = 0; i < vim; i++) 
			retValue.add(new Vis(Arts.VIM));

		return retValue;
	}
	
	public String description() {
		return "Invents +" + modifier + " longevity ritual for " + ((customer == actor || customer == null) ? "Self" : customer.toString());
	}
	
}
