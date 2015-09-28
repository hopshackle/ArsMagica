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
	private Book creoBook, perdoBook;

	@Before
	public void setup() {
		world = new World();
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
		perdoBook = new Summa(Arts.PERDO, 5, 12, magus);
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
		CopyBook cb = new CopyBook(apprentice);
		cb.run();
		assertEquals(magus.getInventoryOf(AMU.sampleBook).size(), 0);
		assertEquals(apprentice.getInventoryOf(AMU.sampleBook).size(), 0);
		magus.setDecider(new HardCodedDecider(MagusActions.PRACTISE_ABILITY));
		magus.decide().run();
		Action next = apprentice.getNextAction();
		assertTrue(next instanceof CopyBook);
		cb = (CopyBook) next;
		cb.run();
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
		CopyBook cb = new CopyBook(apprentice);
		cb.run();
		assertEquals(magus.getInventoryOf(AMU.sampleBook).size(), 0);
		assertEquals(apprentice.getInventoryOf(AMU.sampleBook).size(), 0);
		magus.setDecider(new HardCodedDecider(MagusActions.TEACH_APPRENTICE));
		magus.decide().run();
		Action next = apprentice.getNextAction();
		assertTrue(next instanceof BeTaught);
		assertFalse(creoBook.isInUse());
		
		creoBook.setCurrentReader(magus);
		magus.setDecider(new HardCodedDecider(MagusActions.PRACTISE_ABILITY));
		apprentice.getNextAction().run();
		assertTrue(apprentice.getCurrentCopyProject() != null);
		magus.getNextAction().run();
		
		next = apprentice.getNextAction();
		assertFalse(next instanceof CopyBook);		
		
		creoBook.setCurrentReader(null);
		apprentice.purgeActions();
		assertTrue(apprentice.getCurrentCopyProject() != null);
		next = apprentice.decide();
		assertTrue(next instanceof CopyBook);		
		cb = (CopyBook) next;
		cb.run();
		assertEquals(magus.getInventoryOf(AMU.sampleBook).size(), 1);
		assertEquals(apprentice.getInventoryOf(AMU.sampleBook).size(), 0);
		assertEquals(magus.getInventoryOf(AMU.sampleBook).get(0).getTitleId(), creoBook.getTitleId());
	}

}
