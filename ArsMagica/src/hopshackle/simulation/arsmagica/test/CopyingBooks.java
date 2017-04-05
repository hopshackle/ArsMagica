package hopshackle.simulation.arsmagica.test;

import static org.junit.Assert.*;

import org.junit.*;

import java.util.*;

import hopshackle.simulation.*;
import hopshackle.simulation.arsmagica.*;

public class CopyingBooks {

	private Magus magus;
	private Magus apprentice;
	private World world;
	private Covenant covenant;
	private Tribunal tribunal;
	private Book creoBook;

	@Before
	public void setup() {
		world = new World(new SimpleWorldLogic<Magus>(new ArrayList<ActionEnum<Magus>>(EnumSet.allOf(MagusActions.class))));
		tribunal = new Tribunal("test", world);
		magus = new Magus(world);
		apprentice = new Magus(world);
		magus.addXP(Abilities.LATIN, 75);
		magus.addXP(Abilities.ARTES_LIBERALES, 5);
		magus.addXP(Abilities.SCRIBE, 15);
		apprentice.addXP(Abilities.LATIN, 75);
		apprentice.addXP(Abilities.ARTES_LIBERALES, 5);
		apprentice.addXP(Abilities.SCRIBE, 15);
		List<Magus> founders = new ArrayList<Magus>();
		for (int i = 0; i < 2; i++)
			founders.add(new Magus(world));
		
		covenant = new Covenant(founders, tribunal);
		
		magus.addApprentice(apprentice);
		magus.setCovenant(covenant);
		
		creoBook = new Summa(Arts.CREO, 10, 8, magus);
	}
	
	@Test
	public void copyBookIsChooseableIfApprenticeAndDecentBookAvailable() {
		assertFalse(MagusActions.COPY_BOOK.isChooseable(magus));
		assertFalse(MagusActions.COPY_BOOK.isChooseable(apprentice));
		covenant.addItem(creoBook);
		assertFalse(MagusActions.COPY_BOOK.isChooseable(magus));
		assertFalse(MagusActions.COPY_BOOK.isChooseable(apprentice));
		assertTrue(apprentice.getBestBookToCopy() == null);
		for (int i = 0; i < 9; i ++) {
			creoBook.isReadBy(magus);
		}
		assertFalse(MagusActions.COPY_BOOK.isChooseable(magus));
		assertFalse(MagusActions.COPY_BOOK.isChooseable(apprentice));
		creoBook.isReadBy(magus);
		assertFalse(MagusActions.COPY_BOOK.isChooseable(magus));
		assertTrue(MagusActions.COPY_BOOK.isChooseable(apprentice));
		assertTrue(apprentice.getBestBookToCopy() == creoBook);
		
		magus.addItem(creoBook.createCopy());
		assertTrue(apprentice.getBestBookToCopy() == null);
		assertFalse(MagusActions.COPY_BOOK.isChooseable(magus));
		assertFalse(MagusActions.COPY_BOOK.isChooseable(apprentice));
	}
	
	@Test
	public void copyingABookPutsItIntoParensInventory() {
		copyBookIsChooseableIfApprenticeAndDecentBookAvailable();
		Book bookToRemove = magus.getInventoryOf(AMU.sampleBook).get(0);
		magus.removeItem(bookToRemove);
		assertEquals(magus.getInventoryOf(AMU.sampleBook).size(), 0);
		assertEquals(apprentice.getInventoryOf(AMU.sampleBook).size(), 0);
		apprentice.purgeActions(true);
		CopyBook cb = new CopyBook(apprentice);
		cb.addToAllPlans();
		cb.start();
		cb.run();
		assertEquals(magus.getInventoryOf(AMU.sampleBook).size(), 0);
		assertEquals(apprentice.getInventoryOf(AMU.sampleBook).size(), 0);
		magus.setDecider(new HardCodedDecider<Magus>(MagusActions.PRACTISE_ABILITY));
		magus.decide();
		runNextAction(magus);
		Action<?> next = apprentice.getNextAction();
		assertTrue(next instanceof CopyBook);
		runNextAction(apprentice);
		assertEquals(magus.getInventoryOf(AMU.sampleBook).size(), 1);
		assertEquals(apprentice.getInventoryOf(AMU.sampleBook).size(), 0);
		assertEquals(magus.getInventoryOf(AMU.sampleBook).get(0).getTitleId(), creoBook.getTitleId());
		assertTrue(apprentice.getCurrentCopyProject() == null);
	}
	
	@Test
	public void copyingABookWithASeasonPauseReleasesBookForReading() {
		copyBookIsChooseableIfApprenticeAndDecentBookAvailable();
		Book bookToRemove = magus.getInventoryOf(AMU.sampleBook).get(0);
		magus.removeItem(bookToRemove);
		assertEquals(magus.getInventoryOf(AMU.sampleBook).size(), 0);
		assertEquals(apprentice.getInventoryOf(AMU.sampleBook).size(), 0);
		apprentice.purgeActions(true);
		CopyBook cb = new CopyBook(apprentice);
		cb.addToAllPlans();
		assertFalse(cb.isDeleted());
		assertFalse(creoBook.isInUse());
		assertTrue(cb.getBookBeingCopied() == creoBook);
		cb.start();
		assertTrue(creoBook.isInUse());
		cb.run();
		assertEquals(magus.getInventoryOf(AMU.sampleBook).size(), 0);
		assertEquals(apprentice.getInventoryOf(AMU.sampleBook).size(), 0);
		assertFalse(creoBook.isInUse());
		magus.setDecider(new HardCodedDecider<Magus>(MagusActions.TEACH_APPRENTICE));
		magus.decide();
		Action<?> next = apprentice.getNextAction();
		assertTrue(next instanceof TeachApprentice);	
		assertFalse(creoBook.isInUse());
		runNextAction(magus);
		assertFalse(creoBook.isInUse());
		assertTrue(apprentice.getCurrentCopyProject() != null);
		creoBook.setCurrentReader(magus);
		magus.setDecider(new HardCodedDecider<Magus>(MagusActions.PRACTISE_ABILITY));
		assertTrue(apprentice.getCurrentCopyProject() != null);
		runNextAction(magus);
		
		next = apprentice.getNextAction();
		assertFalse(next == null);
		assertFalse(next instanceof CopyBook);		
	
		creoBook.setCurrentReader(null);
		apprentice.getActionPlan().purgeActions(true);
		apprentice.decide();
		next = apprentice.getNextAction();
		assertFalse(next == null);
		assertTrue(next instanceof CopyBook);
		
		runNextAction(apprentice);
		assertEquals(magus.getInventoryOf(AMU.sampleBook).size(), 1);
		assertEquals(apprentice.getInventoryOf(AMU.sampleBook).size(), 0);
		assertEquals(magus.getInventoryOf(AMU.sampleBook).get(0).getTitleId(), creoBook.getTitleId());
	}

	private void runNextAction(Magus m) {
		Action<?> a = m.getActionPlan().getNextAction();
		a.start();
		a.run();
	}

}
