package hopshackle.simulation.arsmagica;

import java.util.List;

import hopshackle.simulation.*;

public class LongevityRitualService extends ArsMagicaItem implements ArtefactRequiringMaintenance {

	private Magus CrCoSpecialist;
	private Magus customer;
	boolean hasBeenPurchased = false;

	public LongevityRitualService(Magus serviceOfferer) {
		CrCoSpecialist = serviceOfferer;
		if (CrCoSpecialist != null)
			CrCoSpecialist.setLongevityAvailability(false);		// only offer one at a time
	}

	public int getLabTotal() {
		return CrCoSpecialist.getLabTotal(Arts.CREO, Arts.CORPUS, null);
		// as won't use apprentice in this case
	}

	@Override
	public void artefactMaintenance(Agent purchaser) {
		if (!hasBeenPurchased) {
			hasBeenPurchased = true;
			if (CrCoSpecialist != null)
				CrCoSpecialist.setLongevityAvailability(true);
		}
		if (!(purchaser instanceof Magus) || purchaser == CrCoSpecialist) {
			// could have been inherited by a covenant, or been returned to initiator
			purchaser.removeItem(this);
			deleteThis();
			return;
		}
		customer = (Magus) purchaser; 
		List<Artefact> allContracts = customer.getInventoryOf(AMU.sampleLongevityRitualService);
		int highest = 0;
		for (Artefact a : allContracts) {
			LongevityRitualService lrs = (LongevityRitualService)a;
			if (lrs.getLabTotal() > highest) {
				highest = lrs.getLabTotal();
			}
		}
		if (getLabTotal() < highest)
			return;	// just use the best one if multiple options
		if (CrCoSpecialist.isDead()) {
			deleteThis();
		} else if (customer.getLongevityRitualEffect() < Math.ceil(getLabTotal() / 5.0) 
				&& InventLongevityRitual.hasSufficientVis(customer) && !CrCoSpecialist.isInTwilight()) {
			// i.e. only use the contract if it will be of benefit and you have the vis
			Action n = CrCoSpecialist.getNextAction();
			if (n instanceof LabAssistant || n instanceof InventLongevityRitual)
				return;	// these two take priority
			n = customer.getNextAction();
			if (n instanceof LabAssistant || n instanceof InventLongevityRitual)
				return;	// these two take priority
			InventLongevityRitual action = new InventLongevityRitual(CrCoSpecialist, customer);
			CrCoSpecialist.setActionOverride(action);
			LabAssistant assistAction = new LabAssistant(customer, CrCoSpecialist);
			customer.setActionOverride(assistAction);
			int numberOfAssistants = Math.max(1, CrCoSpecialist.getLevelOf(Abilities.LEADERSHIP));
			if (numberOfAssistants > 2 && CrCoSpecialist.hasApprentice()) {
				LabAssistant firstApprentice = new LabAssistant(CrCoSpecialist.getApprentice(), CrCoSpecialist);
				CrCoSpecialist.getApprentice().setActionOverride(firstApprentice);
				numberOfAssistants--;
			}
			if (numberOfAssistants > 2 && customer.hasApprentice()) {
				LabAssistant secondApprentice = new LabAssistant(customer.getApprentice(), CrCoSpecialist);
				customer.getApprentice().setActionOverride(secondApprentice);
				numberOfAssistants--;
			}
			deleteThis();
		}
		else {
			// leave to use for later...can even be inherited
		}
	}

	private void deleteThis() {
		if (customer != null)
			customer.removeItem(this);
		customer = null;
		CrCoSpecialist = null;
	}

	@Override
	public String toString() {
		return "Longevity Ritual Service offered by " + CrCoSpecialist;
	}
}
