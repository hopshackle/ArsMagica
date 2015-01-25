package hopshackle.simulation.arsmagica.test;

import static org.junit.Assert.*;

import org.junit.*;

import hopshackle.simulation.*;
import hopshackle.simulation.arsmagica.*;

import org.junit.Test;

public class BasicMagusActionChooseability {

	private Magus magus, apprentice;
	private Covenant cov;

	@Before
	public void setUp() {
		World w = new World();
		Tribunal t = new Tribunal("test", w);
		cov = new Covenant(null, t);
		magus = new Magus(w);
		magus.setCovenant(cov);
		apprentice = new Magus(w);
		magus.addApprentice(apprentice);
		magus.setLongevityRitualEffect(10);
		apprentice.addXP(Abilities.LATIN, 50);
		apprentice.addXP(Abilities.ARTES_LIBERALES, 50);
	}

	@Test
	public void defaultValues() {
		assertFalse(MagusActions.STUDY_VIS.isChooseable(magus));
		assertTrue(MagusActions.PRACTISE_ABILITY.isChooseable(magus));
		assertTrue(MagusActions.SEARCH_VIS.isChooseable(magus));
	}

	@Test
	public void studyFromVis() {
		magus.addVis(Arts.CREO, 1);
		assertFalse(MagusActions.STUDY_VIS.isChooseable(magus));
		magus.addXP(Abilities.MAGIC_THEORY, 5);		// may now use up to 2 pawns of vis
		magus.addXP(Arts.CREO, 16);
		assertTrue(MagusActions.STUDY_VIS.isChooseable(magus));
		magus.addXP(Arts.CREO, 5);
		assertEquals(magus.getLevelOf(Arts.CREO), 6);
		assertFalse(MagusActions.STUDY_VIS.isChooseable(magus));
		magus.addVis(Arts.CREO, 1);
		assertTrue(MagusActions.STUDY_VIS.isChooseable(magus));
		magus.addXP(Arts.CREO, 44); 	// to one xp shy of Cr 10
		assertTrue(MagusActions.STUDY_VIS.isChooseable(magus));
		magus.addXP(Arts.CREO, 1);
		assertFalse(MagusActions.STUDY_VIS.isChooseable(magus));
	}

	@Test
	public void apprenticeRestrictionsApply() {
		Book popularBook = new Summa(Arts.ANIMAL, 10, 10, null);
		for (int i = 0; i < 10; i++)
			popularBook.isReadBy(magus);
		cov.addItem(popularBook);
		apprentice.addVis(Arts.CREO, 1);
		assertFalse(MagusActions.STUDY_VIS.isChooseable(apprentice));
		assertTrue(MagusActions.PRACTISE_ABILITY.isChooseable(apprentice));
		assertFalse(MagusActions.SEARCH_VIS.isChooseable(apprentice));
		assertFalse(MagusActions.SEARCH_APPRENTICE.isChooseable(apprentice));
		assertFalse(MagusActions.DISTILL_VIS.isChooseable(apprentice));
		assertFalse(MagusActions.INVENT_SPELL.isChooseable(apprentice));
		assertFalse(MagusActions.LONGEVITY_RITUAL.isChooseable(apprentice));
		assertFalse(MagusActions.FOUND_COVENANT.isChooseable(apprentice));
		assertFalse(MagusActions.JOIN_COVENANT.isChooseable(apprentice));
		assertTrue(MagusActions.READ_BOOK.isChooseable(apprentice));
		assertTrue(MagusActions.COPY_BOOK.isChooseable(apprentice));
		assertTrue(MagusActions.WRITE_TRACTATUS.isChooseable(apprentice));
		assertTrue(MagusActions.WRITE_SUMMA.isChooseable(apprentice));
	}
}
