package hopshackle.simulation.arsmagica.test;

import static org.junit.Assert.*;
import org.junit.*;
import java.util.*;

import hopshackle.simulation.*;
import hopshackle.simulation.arsmagica.*;

public class ReadingAndWriting {

	private Magus magus, apprentice;
	private World world;
	private Covenant cov;
	int communication;

	@Before
	public void setup() {
		SimProperties.setProperty("MagusUniformResearchPreferences", "true");
		world = new World();
		Tribunal trib = new Tribunal("test", world);
		cov = new Covenant(null, trib);
		magus = new Magus(world);
		magus.addXP(Abilities.LATIN, 75);
		magus.addXP(Abilities.ARTES_LIBERALES, 5);
		magus.setCommunication(1);
		communication = magus.getCommunication();
		magus.setCovenant(cov);
		apprentice = new Magus(world);
		magus.addApprentice(apprentice);
		apprentice.addXP(Abilities.LATIN, 50);
		apprentice.addXP(Abilities.ARTES_LIBERALES, 5);
		apprentice.addXP(Arts.IGNEM, 50);
	}

	@Test
	public void WriteBookIsChooseableIfArtAtLeastTen() {
		assertFalse(MagusActions.WRITE_SUMMA.isChooseable(magus));
		magus.addXP(Arts.CREO, 15);
		assertFalse(MagusActions.WRITE_SUMMA.isChooseable(magus));
		magus.addXP(Arts.CREO, 40);
		assertTrue(MagusActions.WRITE_SUMMA.isChooseable(magus));
	}

	@Test
	public void WriteBookIsChooseableIfInMiddleOfProject() {
		assertFalse(MagusActions.WRITE_SUMMA.isChooseable(magus));
		magus.addXP(Arts.AQUAM, 500);
		assertTrue(MagusActions.WRITE_SUMMA.isChooseable(magus));
		new WriteSumma(magus, Arts.AQUAM).run();
		assertTrue(MagusActions.WRITE_SUMMA.isChooseable(magus));
		assertTrue(magus.isWritingBook());

		magus.addXP(Arts.AQUAM, -500);
		magus.setCurrentBookProject(null);
		assertFalse(MagusActions.WRITE_SUMMA.isChooseable(magus));
		assertFalse(magus.isWritingBook());
	}

	@Test
	public void WriteBookIsChooseableIfAbilityAtLeastFour() {
		assertFalse(MagusActions.WRITE_SUMMA.isChooseable(magus));
		magus.addXP(Abilities.MAGIC_THEORY, 10);
		assertFalse(MagusActions.WRITE_SUMMA.isChooseable(magus));
		magus.addXP(Abilities.MAGIC_THEORY, 5);
		assertFalse(MagusActions.WRITE_SUMMA.isChooseable(magus));
		magus.addXP(Abilities.MAGIC_THEORY, 35);
		assertTrue(MagusActions.WRITE_SUMMA.isChooseable(magus));
	}

	@Test
	public void WriteBookCreatesSummaOfHighestArtIfNoSummasPossessed() {
		magus.addXP(Arts.MUTO, Arts.MUTO.getXPForLevel(20));	
		magus.addXP(Arts.ANIMAL, Arts.MUTO.getXPForLevel(16));	
		magus.addXP(Arts.VIM, Arts.MUTO.getXPForLevel(26));	
		assertEquals(magus.getTotalXPIn(Abilities.LATIN), 75);
		magus.setDecider(new HardCodedDecider(MagusActions.WRITE_SUMMA));
		runActionsUntilBookFinished();
		List<Book> books = magus.getInventoryOf(AMU.sampleBook);
		assertEquals(books.size(), 1);
		Book bookWritten = (Book) books.get(0);
		assertTrue(bookWritten.getSubject() == Arts.VIM);	
		assertEquals(magus.getTotalXPIn(Abilities.LATIN), 79);
	}


	@Test
	public void WriteBookCreatesSummaOfHighestArtWithNoSumma() {
		magus.addXP(Arts.MUTO, Arts.MUTO.getXPForLevel(20));	
		magus.addXP(Arts.ANIMAL, Arts.MUTO.getXPForLevel(16));	
		magus.addXP(Arts.VIM, Arts.MUTO.getXPForLevel(24));	
		magus.addItem(new Summa(Arts.VIM, 7, 15, null));
		magus.setDecider(new HardCodedDecider(MagusActions.WRITE_SUMMA));
		runActionsUntilBookFinished();
		List<Book> books = magus.getInventoryOf(AMU.sampleBook);
		assertEquals(books.size(), 2);
		Book bookWritten = (Book) books.get(1);
		assertTrue(bookWritten.getSubject() == Arts.MUTO);	
		assertTrue(bookWritten.getAuthor().equals(magus.toString()));
	}
	
	@Test
	public void WriteSummaDoesNotDuplicateExistingBook() {
		magus.setDecider(new HardCodedDecider(MagusActions.WRITE_SUMMA));
		magus.addXP(Arts.VIM, 66);	// lvl 11
		magus.addItem(new Summa(Arts.VIM, 5, 15, null));
		// Should now not be worth writing a book
		Action action = magus.decide();
		assertTrue(action instanceof SearchForVis);
	}

	@Test
	public void WriteBookCreatesSummaWithOptimalLevelAndQuality() {
		magus.addXP(Arts.MUTO, Arts.MUTO.getXPForLevel(20));	// lvl 20
		magus.setCommunication(1);
		/* 
		 * 					Writing Time	xp Gain		Reduction Study		Increase Study		Total
		 * L10 Q7			-4				11				0					-8					 1
		 *  L9 Q8			-4				 9				0					-6					 1
		 *  L8 Q9			-4				 7				0					-4					 1
		 *  L7 Q10			-4				 5				0					-3					 0
		 *  L6 Q11			-2				 4				0					-2					 2
		 *  L5 Q12			-2				 3				0					-2					 1
		 */
		magus.setDecider(new HardCodedDecider(MagusActions.WRITE_SUMMA));
		runActionsUntilBookFinished();
		List<Book> books = magus.getInventoryOf(AMU.sampleBook);
		assertEquals(books.size(), 1);
		Book bookWritten = (Book) books.get(0);
		assertEquals(bookWritten.getLevel(), 6);
		assertEquals(bookWritten.getQuality(), 11);

		/* 
		 * 					Writing Time	xp Gain		Reduction Study		Increase Study		Total
		 * L10 Q7			-4				 6				0					-5					-1
		 *  L9 Q8			-4				 4				0					-3					-1
		 *  L8 Q9			-4				 3				0					-2					 0
		 *  L7 Q10			-4				 1				0					-1					-2
		 *  L6 Q11			-2				 0				0					 0					 0
		 *  L5 Q12			-2				 0				0					 0					 0
		 */
		Action action = magus.decide();
		assertTrue(action instanceof SearchForVis);

		magus.removeItem(bookWritten);
		magus.setCommunication(3);
		
		/* 
		 * 					Writing Time	xp Gain		Reduction Study		Increase Study		Total
		 * L10 Q9			-4			 	11				0					-7					 2
		 *  L9 Q10			-4				 9				0					-5					 2
		 *  L8 Q11			-2				 7				0					-4					 3
		 *  L7 Q12			-2				 5				0					-3					 2
		 *  L6 Q13			-2				 4				0					-2					 2
		 *  L5 Q14			-2				 3				0					-2					 1
		 */
		runActionsUntilBookFinished();
		books = magus.getInventoryOf(AMU.sampleBook);
		assertEquals(books.size(), 1);
		bookWritten = (Book) books.get(0);
		assertEquals(bookWritten.getLevel(), 8);
		assertEquals(bookWritten.getQuality(), 11);
		/* 
		 * 					Writing Time	xp Gain		Reduction Study		Increase Study		Total
		 * L10 Q9			-4				 3				0					-2					-1
		 *  L9 Q10			-4				 1				0					-1					-2
		 *  L8 Q11			-2				 0				0					 0					 0
		 *  L7 Q12			-2				 0				0					 0					 0
		 *  L6 Q13			-2				 0				0					 0					 0
		 *  L5 Q14			-2				 0				4					 0					 4
		 */
		
		runActionsUntilBookFinished();
		books = magus.getInventoryOf(AMU.sampleBook);
		assertEquals(books.size(), 2);
		bookWritten = (Book) books.get(1);
		assertEquals(bookWritten.getLevel(), 5);
		assertEquals(bookWritten.getQuality(), 14);
	}

	@Test
	public void AbilitiesAreValuedAtTwiceArtBasedOnLevel() {
		magus.addXP(Arts.MUTO, Arts.MUTO.getXPForLevel(20));
		magus.addXP(Abilities.MAGIC_THEORY, Abilities.MAGIC_THEORY.getXPForLevel(10)); 
		magus.setDecider(new HardCodedDecider(MagusActions.WRITE_SUMMA));
		runActionsUntilBookFinished();
		List<Book> books = magus.getInventoryOf(AMU.sampleBook);
		assertEquals(books.size(), 1);
		Book bookWritten = (Book) books.get(0);
		assertTrue(bookWritten.getSubject() == Abilities.MAGIC_THEORY);	
		magus.removeItem(bookWritten);

		magus.addXP(Arts.MUTO, Arts.MUTO.getXPForLevel(30) - Arts.MUTO.getXPForLevel(20));	// lvl 26		
		runActionsUntilBookFinished();
		books = magus.getInventoryOf(AMU.sampleBook);
		assertEquals(books.size(), 1);
		bookWritten = (Book) books.get(0);
		assertTrue(bookWritten.getSubject() == Arts.MUTO);	
	}
	
	@Test
	public void DecisionTakesIntoAccountResearchPreferences() {
		
		/* MUTO
		 * 					Writing Time	xp Gain		Reduction Study		Increase Study		Total
		 *  L10 Q7			-4				 11				0					-8					1
		 *  L9 Q8			-4				 9				0					-6					1
		 *  L8 Q9			-4				 7				0					-4					1
		 *  L7 Q10			-4				 5				0					-3					0
		 *  L6 Q11			-2				 4				0					-2					2
		 *  L5 Q12			-2				 3				0					-2					1
		 *  
		 *  MAGIC_THEORY
		 *  		 		Writing Time	xp Gain		Reduction Study		Increase Study		Total
		 *  L5 Q7			-2				 15				0					-11					4
		 *  L4 Q8			-2				 10				0					-7					3
		 *  L3 Q9			-2				  6				0					-4					2
		 */
		
		MagusPreferences.setResearchPreference(magus, Arts.MUTO, 1.1);
		MagusPreferences.setResearchPreference(magus, Abilities.MAGIC_THEORY, 0.5);
		magus.addXP(Arts.MUTO, Arts.MUTO.getXPForLevel(20));	
		magus.addXP(Abilities.MAGIC_THEORY, Abilities.MAGIC_THEORY.getXPForLevel(10)); //
		magus.setDecider(new HardCodedDecider(MagusActions.WRITE_SUMMA));
		runActionsUntilBookFinished();
		List<Book> books = magus.getInventoryOf(AMU.sampleBook);
		assertEquals(books.size(), 1);
		Book bookWritten = (Book) books.get(0);
		assertTrue(bookWritten.getSubject() == Arts.MUTO);	
		magus.removeItem(bookWritten);
		assertFalse(magus.isWritingBook());

		/*
		 *  MAGIC_THEORY
		 *  		 		Writing Time	xp Gain		Reduction Study		Increase Study		Total
		 *  L6 Q7			-2				 21				0					-15					7
		 *  L5 Q8			-2				 15				0					-10					6
		 *  L4 Q9			-2				 10				0					-6					5
		 *  L3 Q10			-2				  6				0					-3					4
		 */
		magus.addXP(Abilities.MAGIC_THEORY, 115);	// lvl 12, so can write lvl 4 summa (equiv 8)
		runActionsUntilBookFinished();
		books = magus.getInventoryOf(AMU.sampleBook);
		assertEquals(books.size(), 1);
		bookWritten = (Book) books.get(0);
		assertTrue(bookWritten.getSubject() == Abilities.MAGIC_THEORY);	
	}

	@Test
	public void bookIsInheritedOnDeath() {
		magus.addItem(new Summa(Arts.CREO, 10, 10, magus));
		magus.addItem(new LabText(new Spell(Arts.CREO, Arts.VIM, 20, "test", magus), magus));
		magus.setCovenant(null);
		magus.die("Ooops");
		assertEquals(apprentice.getInventoryOf(AMU.sampleBook).size(),2);
	}

	@Test
	public void bookGoesToMasterIfApprentice() {
		assertEquals(magus.getInventoryOf(AMU.sampleBook).size(),0);
		apprentice.setDecider(new HardCodedDecider(MagusActions.WRITE_TRACTATUS));
		apprentice.decide().run();
		assertEquals(magus.getInventoryOf(AMU.sampleBook).size(),1);
		assertEquals(apprentice.getInventoryOf(AMU.sampleBook).size(),0);
	}

	@Test
	public void bookGoesToCovenantIfServiceOwed() {
		magus.addXP(Arts.IGNEM, 300);
		assertEquals(magus.getInventoryOf(AMU.sampleBook).size(),0);
		magus.setSeasonsServiceOwed(1);
		magus.setDecider(new HardCodedDecider(MagusActions.WRITE_SUMMA));
		runActionsUntilBookFinished();
		assertEquals(magus.getInventoryOf(AMU.sampleBook).size(),0);
		assertEquals(cov.getLibrary().size(),1);
		assertTrue(magus.getSeasonsServiceOwed() < 0);
	}

	@Test
	public void readingAndWritingRequireLatinFour() {
		Magus apprentice = new Magus(world); // no Latin
		apprentice.setCommunication(0);
		apprentice.addXP(Arts.REGO,  55);
		apprentice.addXP(Abilities.ARTES_LIBERALES, 5);
		apprentice.addItem(new Summa(Arts.CREO, 10, 10, null));
		assertFalse(MagusActions.READ_BOOK.isChooseable(apprentice));
		assertFalse(MagusActions.WRITE_SUMMA.isChooseable(apprentice));

		apprentice.addXP(Abilities.LATIN, 49); // just shy of lvl 4
		assertFalse(MagusActions.READ_BOOK.isChooseable(apprentice));
		assertFalse(MagusActions.WRITE_SUMMA.isChooseable(apprentice));

		apprentice.addXP(Abilities.LATIN, 1);
		assertTrue(MagusActions.READ_BOOK.isChooseable(apprentice));
		assertTrue(MagusActions.WRITE_SUMMA.isChooseable(apprentice));
	}

	@Test
	public void readBookIsChooseableIfThereIsASummaOfHigherLevelInInventory() {
		assertFalse(MagusActions.READ_BOOK.isChooseable(magus));
		magus.addItem(new Summa(Arts.CREO, 10, 10, null));
		assertTrue(MagusActions.READ_BOOK.isChooseable(magus));
		magus.addXP(Arts.CREO, 55);	// to lvl 10
		assertFalse(MagusActions.READ_BOOK.isChooseable(magus));

		magus.addItem(new Summa(Abilities.MAGIC_THEORY, 2, 2, null));
		assertTrue(MagusActions.READ_BOOK.isChooseable(magus));
	}

	@Test
	public void readBookIncreasesXPinArtOrAbilityUpToLevelOfBook() {
		Book creoSumma = new Summa(Arts.CREO, 7, 10, null);
		Book philosophySumma = new Summa(Abilities.PHILOSOPHIAE, 2, 8, null);
		MagusPreferences.setResearchPreference(magus, Abilities.PHILOSOPHIAE, 0.5);
		magus.addItem(creoSumma);
		magus.addItem(philosophySumma);
		assertFalse(creoSumma.isInUse());
		assertFalse(philosophySumma.isInUse());
		magus.setDecider(new HardCodedDecider(MagusActions.READ_BOOK));
		apprentice.setDecider(new HardCodedDecider(MagusActions.PRACTISE_ABILITY));	// otherwise apprentice reads the Philosophy book
		Action firstAction = magus.decide();
		firstAction.run();
		assertTrue(creoSumma.isInUse());	// still reading
		assertFalse(philosophySumma.isInUse());
		assertEquals(magus.getTotalXPIn(Arts.CREO), 10);
		magus.getNextAction().run();
		assertFalse(creoSumma.isInUse());	// has finished reading for the moment
		assertTrue(philosophySumma.isInUse());
		assertEquals(magus.getTotalXPIn(Arts.CREO), 20);
		magus.getNextAction().run();
		assertTrue(creoSumma.isInUse());
		assertFalse(philosophySumma.isInUse());
		assertEquals(magus.getTotalXPIn(Arts.CREO), 20);
		assertEquals(magus.getTotalXPIn(Abilities.PHILOSOPHIAE), 8);
		magus.getNextAction().run();
		assertEquals(magus.getTotalXPIn(Arts.CREO), 28);
		assertEquals(magus.getTotalXPIn(Abilities.PHILOSOPHIAE), 8);
	}

	@Test
	public void purgingAReadBookActionMarksTheBookAsNotInUse() {
		Book creoSumma = new Summa(Arts.CREO, 10, 10, null);
		magus.addItem(creoSumma);

		magus.setDecider(new HardCodedDecider(MagusActions.READ_BOOK));
		magus.decide().run();
		assertTrue(creoSumma.isInUse());
		magus.purgeActions();
		assertFalse(creoSumma.isInUse());
	}

	private void runActionsUntilBookFinished() {
		Action action = magus.decide();
		Action originalAction = action;
		do {
			assertTrue(action instanceof WriteSumma);
			assertTrue(action.equals(originalAction));
			action.run();
			action = magus.getNextAction();
		} while (magus.isWritingBook() && magus.getCurrentBookProject().equals(originalAction));
		magus.purgeActions();
	}
}
