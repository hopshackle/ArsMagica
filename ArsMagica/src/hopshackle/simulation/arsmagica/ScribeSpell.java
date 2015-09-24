package hopshackle.simulation.arsmagica;

import java.util.*;

public class ScribeSpell extends ArsMagicaAction {

	private boolean isCovenantService = false;
	private List<Spell> spellsScribed = new ArrayList<Spell>();

	public ScribeSpell(Magus magus) {
		super(magus);
		if (magus.getSeasonsServiceOwed() > 0)
			isCovenantService = true;
	}

	@Override
	public void doStuff() {
		List<Spell> spellsToScribe = getAllUnscribedSpellsKnown(magus);
		Collections.sort(spellsToScribe, new Comparator<Spell>() {

			@Override
			public int compare(Spell s1, Spell s2) {
				return s2.getLevel() - s1.getLevel();
			}
		});

		// Now in order of spell level

		int totalPossible = magus.getLevelOf(Abilities.LATIN) * 20;
		int totalSoFar = 0;
		for (Spell s : spellsToScribe) {
			if (s.getLevel() <= totalPossible - totalSoFar) {
				totalSoFar += s.getLevel();
				LabText newText = new LabText(s, magus);
				magus.log("Writes up Lab Text on " + s.toString());
				spellsScribed.add(s);
				newText.giveToRecipient(magus, isCovenantService);
			}
		}
		if (isCovenantService) 
			magus.doSeasonsService();
		magus.addXP(Abilities.LATIN, 2);
	}

	public static List<Spell> getAllUnscribedSpellsKnown(Magus m) {
		List<Book> allBooks = m.getAllAccessibleBooks();
		allBooks.addAll(m.getInventoryOf(AMU.sampleBook));
		List<LabText> allLabTexts = LabText.extractAllLabTextsFrom(allBooks);
		Set<Spell> spellsWithText = new HashSet<Spell>();
		for (LabText lt : allLabTexts) {
			spellsWithText.add(lt.getSpell());
		}

		List<Spell> unscribedSpells = new ArrayList<Spell>();
		for (Spell s : m.getSpells()) {
			if (!spellsWithText.contains(s))
				unscribedSpells.add(s);
		}

		return unscribedSpells;
	}
	
	public String description() {
		return "Scribes " + (spellsScribed.isEmpty() ? "No Spells" : spellsScribed.size() + " spells. Starting with " + spellsScribed.get(0).toString());
	}
	
	public boolean isCovenantService() {
		return isCovenantService;
	}
}
