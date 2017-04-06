package hopshackle.simulation.arsmagica;

import hopshackle.simulation.HopshackleUtilities;

import java.util.*;

public class InventLongevityRitual extends ArsMagicaAction {

	private Magus customer;
	private int modifier;

	public InventLongevityRitual(Magus a) {
		super(MagusActions.LONGEVITY_RITUAL, a);
		if (a.hasApprentice()) optionalActors.add(a.getApprentice());
	}

	public InventLongevityRitual(Magus crCoSpecialist, Magus customer, List<Magus> assistants, int offset) {
		// when one Magus invents a ritual for another
		super(MagusActions.LONGEVITY_RITUAL, 
				HopshackleUtilities.listFromInstances(crCoSpecialist, customer), 
				assistants, offset, 1);
		if (crCoSpecialist != customer)
			this.customer = customer;
	}

	@Override
	protected void initialisation() {
		Magus subject = customer;
		if (subject == null) subject = magus;
		int requiredMT = (subject.getAge() / 10) + 1;
		if (magus.getLevelOf(Abilities.MAGIC_THEORY) < requiredMT) {
			magus.log("Insufficient Magic Theory for Longevity Ritual");
			cancel();
		}
		if (hasSufficientVis(subject)) {
			for (Vis v : requirementsForRitual(subject))
				subject.removeItem(v);
		} else {
			magus.log("Insufficient vis for Longevity Ritual");
			if (customer != null)
				customer.log("Insufficient vis for Longevity Rutual");
			cancel();	// if insufficient vis, then shoudl still be time for participant to do something else instead
		}
	}

	@Override
	protected void doStuff() {
		Magus subject = customer;
		if (subject == null) subject = magus;
		List<Magus> allAssistants = getAllConfirmedParticipants();
		allAssistants.remove(magus);
		int labTotal = magus.getLabTotal(Arts.CREO, Arts.CORPUS, allAssistants);
		modifier = (int) Math.ceil(labTotal / 5.0);
		subject.setLongevityRitualEffect(modifier);
		subject.setKnownLongevityEffect(Math.max(subject.getKnownLongevityEffect(), modifier));
		if (customer == null) {
			magus.log("Invents a Longevity Ritual of potency " + modifier);
			for (Magus assistant : allAssistants) {
				assistant.log("Lab assistant in creation of longevity ritual by " + magus);
			}
		}
		else {
			magus.log("Invents a Longevity Ritual of potency " + modifier
					+ " for " + customer);
			customer.log("Receives Longevity Ritual of potency " + modifier
					+ " from " + magus);
			allAssistants.remove(customer);
			for (Magus assistant : allAssistants) {
				assistant.log("Lab assistant in creation of longevity ritual for " + customer + " by " + magus);
			}
		}
		exposureXPForParticipants(Arts.CREO, Arts.CORPUS, 2);
	}

	public static boolean hasSufficientVis(Magus subject) {
		return !requirementsForRitual(subject).isEmpty();
	}

	public static List<Vis> requirementsForRitual(Magus visPayer) {
		List<Vis> retValue = new ArrayList<Vis>();
		int pawnsRequired = (int) Math.ceil(visPayer.getAge() / 5.0);
		int creoPawns = visPayer.getPawnsOf(Arts.CREO);
		int vimPawns = visPayer.getPawnsOf(Arts.VIM);
		int corpusPawns = visPayer.getPawnsOf(Arts.CORPUS);
		int pawnsSpent = 0;
		int vim = 0, creo = 0, corpus = 0;
		do {
			if (vimPawns > 0 && vimPawns >= creoPawns
					&& vimPawns >= corpusPawns) {
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
		return magus + " invents +"
				+ modifier
				+ " longevity ritual for "
				+ ((customer == actor || customer == null) ? "Self" : customer
						.toString());
	}

}
