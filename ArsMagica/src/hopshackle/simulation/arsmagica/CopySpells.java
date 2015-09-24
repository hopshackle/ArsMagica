package hopshackle.simulation.arsmagica;

import java.util.List;

public class CopySpells extends ArsMagicaAction {

	private boolean isCovenantService;
	private List<LabText> spellsToCopy;

	public CopySpells(Magus magus) {
		super(magus);
		List<Book> toCopy = magus.getBestSpellsToCopy();
		int totalLevelsLeft = magus.getLevelOf(Abilities.SCRIBE) * 60;
		for (Book b : toCopy) {
			LabText lt = (LabText) b;
			if (lt.getLevel() <= totalLevelsLeft) {
				spellsToCopy.add(lt);
				totalLevelsLeft -= lt.getLevel();
			}
		}
		for (LabText lt : spellsToCopy) {
			lt.setCurrentReader(magus);
		}
		isCovenantService = magus.getSeasonsServiceOwed() > 0;
	}

	public void doStuff() {
		for (LabText spellToCopy : spellsToCopy) {
			spellToCopy.isCopiedBy(magus);
			magus.log("Copies " + spellToCopy);
			Book copy = spellToCopy.createCopy();
			copy.giveToRecipient(magus, isCovenantService);
			spellToCopy.setCurrentReader(null);
		}
		magus.addXP(Abilities.SCRIBE, 2);
		if (isCovenantService)
			magus.doSeasonsService();
	}

	public void delete() {
		for (LabText spellToCopy : spellsToCopy)
			spellToCopy.setCurrentReader(null);
	}
	
	public String description() {
		return "Copies " + spellsToCopy.size() + " spells. Starting with " + spellsToCopy.get(0).toString();
	}
	
	public boolean isCovenantService() {
		return isCovenantService;
	}
}
