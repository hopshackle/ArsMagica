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
		world = new World(new SimpleWorldLogic<>(new ArrayList<>(EnumSet.allOf(MagusActions.class))));
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
		magus.setDecider(new HardCodedDecider<>(MagusActions.PRACTISE_ABILITY));
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
	public void doNotCopyABookWithAnotherGoodCopyAlreadyAvailableInCovenantLibrary() {
		covenant.addItem(creoBook);
		for (int i = 0; i < 20; i ++) {
			creoBook.isReadBy(magus);
		}
		assertFalse(MagusActions.COPY_BOOK.isChooseable(magus));
		magus.setSeasonsServiceOwed(1);
		assertFalse(MagusActions.COPY_BOOK.isChooseable(magus));
		creoBook.increaseDeterioration(200);
		assertTrue(MagusActions.COPY_BOOK.isChooseable(magus));

		covenant.addItem(creoBook.createCopy());
		assertFalse(MagusActions.COPY_BOOK.isChooseable(magus));
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
		assertSame(cb.getBookBeingCopied(), creoBook);
		cb.start();
		assertTrue(creoBook.isInUse());
		cb.run();
		assertEquals(magus.getInventoryOf(AMU.sampleBook).size(), 0);
		assertEquals(apprentice.getInventoryOf(AMU.sampleBook).size(), 0);
		assertFalse(creoBook.isInUse());
		magus.setDecider(new HardCodedDecider<>(MagusActions.TEACH_APPRENTICE));
		magus.decide();
		Action<?> next = apprentice.getNextAction();
		assertTrue(next instanceof TeachApprentice);	
		assertFalse(creoBook.isInUse());
		runNextAction(magus);
		assertFalse(creoBook.isInUse());
		assertNotNull(apprentice.getCurrentCopyProject());
		creoBook.setCurrentReader(magus);
		magus.setDecider(new HardCodedDecider<>(MagusActions.PRACTISE_ABILITY));
		assertNotNull(apprentice.getCurrentCopyProject());
		runNextAction(magus);
		
		next = apprentice.getNextAction();
		assertNotNull(next);
		assertFalse(next instanceof CopyBook);		
	
		creoBook.setCurrentReader(null);
		apprentice.getActionPlan().purgeActions(true);
		apprentice.decide();
		next = apprentice.getNextAction();
		assertNotNull(next);
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
