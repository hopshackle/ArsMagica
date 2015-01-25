package hopshackle.simulation.arsmagica.test;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.*;

import hopshackle.simulation.*;
import hopshackle.simulation.arsmagica.*;

public class InventingSpellsAndLabTexts {

	private Magus magus;
	private World w;

	@Before
	public void setup() {
		w = new World();
		new Tribunal("test", w);
		magus = new Magus(w);
		magus.setMagicAura(5);
		magus.setIntelligence(3);
		magus.addXP(Abilities.MAGIC_THEORY, 100);
		magus.addXP(Abilities.LATIN, 50);	// to level 4
		magus.addXP(Arts.CREO, 100);	// to level 13
		magus.addXP(Arts.MUTO, 100);
		magus.addXP(Arts.INTELLEGO, 100);
		magus.addXP(Arts.PERDO, 100);
		magus.addXP(Arts.REGO, 100);
	}

	@Test
	public void inventSpellChooseableWithSufficientArtsAndNotApprentice() {
		assertTrue(MagusActions.INVENT_SPELL.isChooseable(magus));
		Magus otherMagus = new Magus(w);
		assertFalse(MagusActions.INVENT_SPELL.isChooseable(otherMagus));
		otherMagus.addApprentice(magus);
		assertFalse(MagusActions.INVENT_SPELL.isChooseable(magus));
	}

	@Test
	public void inventingASpellDoesSoOverANumberOfSeasons() {
		magus.setDecider(new HardCodedDecider(MagusActions.INVENT_SPELL));
		magus.addAction(magus.decide());
		assertFalse(magus.isResearchingSpell());
		int totalSpellLevels = 0;
		int numberOfSpells = 0;
		int seasons = 0;
		boolean hasInventedSpell = false;
		for (int i = 0; i < 10; i++) {
			hasInventedSpell = false;
			do {
				Action nextAction = magus.getNextAction();
				seasons++;
				assertTrue(nextAction instanceof InventSpell);
				nextAction.run();
				if (magus.isResearchingSpell()) {
					assertEquals(magus.getTotalSpellLevels(), totalSpellLevels);
					assertEquals(magus.getSpells().size(), numberOfSpells);
					assertTrue(magus.getCurrentSpellResearchProject() == nextAction);
				} else {
					// has therefore completed spell
					hasInventedSpell = true;
					assertTrue(magus.getTotalSpellLevels() > totalSpellLevels);
					assertEquals(magus.getSpells().size(), numberOfSpells +1);
					totalSpellLevels = magus.getTotalSpellLevels();
					numberOfSpells++;
				}
			} while (!hasInventedSpell);
		} 
		assertTrue(seasons > 11);
	}

	@Test
	public void scribeSpellIsOnlyChooseableWhenSufficientUnscribedSpellsAreKnown() {
		assertFalse(MagusActions.SCRIBE_SPELL.isChooseable(magus));
		magus.addSpell(new Spell(Arts.REGO, Arts.ANIMAL, 10, "ReAn10", magus));
		assertFalse(MagusActions.SCRIBE_SPELL.isChooseable(magus));
		magus.addSpell(new Spell(Arts.CREO, Arts.CORPUS, 20, "CrCo20", magus));
		assertFalse(MagusActions.SCRIBE_SPELL.isChooseable(magus));
		magus.addSpell(new Spell(Arts.MUTO, Arts.CORPUS, 25, "MuCo25", magus));
		assertTrue(MagusActions.SCRIBE_SPELL.isChooseable(magus));
	}

	@Test
	public void labTextsToCorrectTotalCreatedWhenScribingSpells() {
		magus.addSpell(new Spell(Arts.REGO, Arts.ANIMAL, 10, "ReAn10", magus));
		magus.addSpell(new Spell(Arts.CREO, Arts.CORPUS, 20, "CrCo20", magus));
		magus.addSpell(new Spell(Arts.MUTO, Arts.CORPUS, 25, "MuCo25", magus));
		magus.addSpell(new Spell(Arts.PERDO, Arts.MENTEM, 25, "PeMe25", magus));
		magus.addSpell(new Spell(Arts.INTELLEGO, Arts.MENTEM, 40, "InMe40", magus));
		magus.addSpell(new Spell(Arts.INTELLEGO, Arts.AQUAM, 5, "InAq5", magus));
		magus.addSpell(new Spell(Arts.MUTO, Arts.MENTEM, 5, "InMe5", magus));

		// Total of 130 levels, of which 80 are scribeable
		assertEquals(ScribeSpell.getAllUnscribedSpellsKnown(magus).size(), 7);

		magus.setDecider(new HardCodedDecider(MagusActions.SCRIBE_SPELL));
		magus.addAction(magus.decide());
		Action nextAction = magus.getNextAction();
		assertTrue(nextAction instanceof ScribeSpell);
		nextAction.run();

		List<Book> library = magus.getInventoryOf(AMU.sampleBook);
		List<LabText> labTexts = LabText.extractAllLabTextsFrom(library);
		assertEquals(labTexts.size(), 4);
		int[] levels = new int[4];
		for (int i = 0; i < 4; i++)
			levels[i] = labTexts.get(i).getSpell().getLevel();
		assertTrue(levels[0] == 40 || levels[1] == 40 || levels[2] == 40 || levels[3] == 40);
		assertTrue(levels[0] == 25 || levels[1] == 25 || levels[2] == 25 || levels[3] == 25);
		assertTrue(levels[0] == 10 || levels[1] == 10 || levels[2] == 10 || levels[3] == 10);
		assertTrue(levels[0] == 5 || levels[1] == 5 || levels[2] == 5 || levels[3] == 5);
		assertEquals(ScribeSpell.getAllUnscribedSpellsKnown(magus).size(), 3);

		nextAction = magus.getNextAction();
		assertTrue(nextAction instanceof ScribeSpell);
		nextAction.run();
		library = magus.getInventoryOf(AMU.sampleBook);
		labTexts = LabText.extractAllLabTextsFrom(library);
		assertEquals(labTexts.size(), 7);

		assertFalse(MagusActions.SCRIBE_SPELL.isChooseable(magus));
		assertTrue(ScribeSpell.getAllUnscribedSpellsKnown(magus).isEmpty());
	}

	@Test
	public void useLabTextsWhenInventingSpells() {
		Spell[] spellsWithText = new Spell[7];
		spellsWithText[0] = new Spell(Arts.REGO, Arts.ANIMAL, 20, "ReAn20", magus);
		spellsWithText[1] = new Spell(Arts.CREO, Arts.CORPUS, 20, "CrCo20", magus);
		spellsWithText[2] = new Spell(Arts.MUTO, Arts.CORPUS, 30, "MuCo30", magus);
		spellsWithText[3] = new Spell(Arts.PERDO, Arts.MENTEM, 30, "PeMe30", magus);
		spellsWithText[4] = new Spell(Arts.INTELLEGO, Arts.MENTEM, 40, "InMe40", magus);
		spellsWithText[5] = new Spell(Arts.INTELLEGO, Arts.AQUAM, 20, "InAq20", magus);
		spellsWithText[6] = new Spell(Arts.MUTO, Arts.MENTEM, 20, "MuMe20", magus);
		for (int i = 0; i < 7; i++) {
			magus.addItem(new LabText(spellsWithText[i], null));
		}
		magus.setDecider(new HardCodedDecider(MagusActions.INVENT_SPELL));
		magus.addAction(magus.decide());

		for (int i = 0; i < 24; i++) {
			magus.getNextAction().run();
		}

		List<Spell> allSpells = magus.getSpells();
		assertTrue(allSpells.size() > 5);
		assertFalse(allSpells.contains(spellsWithText[2])); // too high level
		assertFalse(allSpells.contains(spellsWithText[3])); // too high level
		assertFalse(allSpells.contains(spellsWithText[4])); // too high level

		int spellsFromText = 0;
		int[] validSpellIndices = {0, 1, 5, 6};
		for (int i=0; i < validSpellIndices.length; i++)
			if (allSpells.contains(spellsWithText[validSpellIndices[i]]))
				spellsFromText++;
		
		assertTrue(spellsFromText > 0);
		assertTrue(spellsFromText < 5);
	}


}
