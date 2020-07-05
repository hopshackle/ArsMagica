package hopshackle.simulation.arsmagica.test;

import static org.junit.Assert.*;

import java.util.*;

import hopshackle.simulation.*;
import hopshackle.simulation.arsmagica.*;

import org.junit.*;

public class Tribunals {

	private Tribunal tribunal;
	private Magus magus1, magus2;
	private World world;
	
	@Before
	public void setup() {
		world = new World(new SimpleWorldLogic<Magus>(new ArrayList<ActionEnum<Magus>>(EnumSet.allOf(MagusActions.class))));
		world.setCalendar(new FastCalendar(800 * 52));
		tribunal = new Tribunal("Test", world);
		magus1 = new Magus(world);
		magus2 = new Magus(world);
	}
	
	@Test
	public void visLevelModifierWorksAsExpected() {
		tribunal.setVisLevel(21);
		assertEquals(tribunal.getVisModifier(), 4);
		for (int i = 0; i < 17; i++) {
			VisSource source = new VisSource(Arts.CREO, 1, tribunal);
			source.changeOwnership(magus1);
		}
		tribunal.maintenance();
		assertEquals(tribunal.getVisModifier(), 0);
	}
	
	@Test
	public void populationLevelModifierWorksAsExpected() {
		tribunal.setPopulationLevel(21);
		assertEquals(tribunal.getApprenticeModifier(), 4);
		for (int i = 0; i < 5; i++) {
			tribunal.registerApprentice(magus1);
		}
		assertEquals(tribunal.getApprenticeModifier(), 0);
		tribunal.maintenance();
		assertEquals(tribunal.getApprenticeModifier(), 0);
		tribunal.maintenance();
		assertEquals(tribunal.getApprenticeModifier(), 0);
		tribunal.maintenance();
		assertEquals(tribunal.getApprenticeModifier(), 0);
		tribunal.maintenance();
		assertEquals(tribunal.getApprenticeModifier(), 0);
		tribunal.maintenance();
		assertEquals(tribunal.getApprenticeModifier(), 1);
		tribunal.maintenance();
		assertEquals(tribunal.getApprenticeModifier(), 1);
	}
	
	@Test
	public void tribunalMarketAcceptsOffers() {
		BarterOffer bo = new BarterOffer(magus2, new VisSource(Arts.CREO, 1, tribunal), 1, 1, new VisValuationFunction(magus2));
		assertTrue(tribunal.getOffersOnMarket().isEmpty());
		tribunal.addToMarket(bo);
		assertEquals(tribunal.getOffersOnMarket().size(), 1);
		
		Book unwantedBook = new Summa(Arts.AQUAM, 5, 15, null);
		magus2.addItem(unwantedBook);
		assertEquals(magus2.getInventoryOf(AMU.sampleBook).size(), 1);
		BarterOffer bo2 = new BarterOffer(magus2, unwantedBook, 1, 1, new VisValuationFunction(magus2));
		assertEquals(magus2.getInventoryOf(AMU.sampleBook).size(), 0);
		tribunal.addToMarket(bo2);
		assertEquals(tribunal.getOffersOnMarket().size(), 2);
		assertTrue(tribunal.getOffersOnMarket().get(1).getItem().equals(unwantedBook));
		
		magus1.addVis(Arts.VIM, 3);
		BarterBid bb = new BarterBid(magus1, getBidOfVimVis(3), bo2);
		assertTrue(bo2.submitBid(bb));
		
		tribunal.maintenance();
		assertEquals(tribunal.getOffersOnMarket().size(), 2);
		assertEquals(tribunal.getTotalBookSales(), 0);
		
		world.setCurrentTime((long) (807 * 52));
		tribunal.maintenance();
		
		assertEquals(tribunal.getOffersOnMarket().size(), 0);
		assertEquals(tribunal.getTotalBookSales(), 1);
		assertEquals(magus1.getInventoryOf(AMU.sampleBook).size(), 1);
		assertEquals(magus2.getInventoryOf(AMU.sampleBook).size(), 0);
		assertEquals(magus2.getInventoryOf(AMU.sampleVis).size(), 3);	// from the successful bid
		assertEquals(magus1.getInventoryOf(AMU.sampleVis).size(), 0);
		assertEquals(magus1.getHighestSumma(Arts.AQUAM), 5);
	}
	
	@Test
	public void bookOnMarketIsRemovedFromMagusRecordOnSale() {
		magus1.addXP(Arts.VIM, Arts.VIM.getXPForLevel(30));
		Summa summa = new Summa(Arts.VIM, 15, 10, magus1);
		magus1.addItem(summa);
		
		assertTrue(magus1.getInventoryOf(AMU.sampleBook).contains(summa));
		assertFalse(magus1.getInventoryOnMarketOf(AMU.sampleBook).contains(summa));
		int tribunalYear = tribunal.getDateOfNextTribunal();
		world.setCurrentTime((long) (tribunalYear * 52) - 26);
		magus1.maintenance();	// this puts the summa on the market with a reserve of 4.0
		assertFalse(magus1.getInventoryOf(AMU.sampleBook).contains(summa));
		assertTrue(magus1.getInventoryOnMarketOf(AMU.sampleBook).contains(summa));
		
		assertTrue(tribunal.getOffersOnMarket().get(0).getItem().equals(summa));
		List<Artefact> price = new ArrayList<Artefact>();
		for (int i = 0; i < 10; i++)
			price.add(new Vis(Arts.CREO));
		BarterOffer bo = tribunal.getOffersOnMarket().get(0);
		assertTrue(bo.submitBid(new BarterBid(magus2, price, bo)));
		world.setCurrentTime((long) (tribunalYear * 52) + 26);
		assertFalse(magus1.getInventoryOf(AMU.sampleBook).contains(summa));
		assertTrue(magus1.getInventoryOnMarketOf(AMU.sampleBook).contains(summa));
		tribunal.maintenance();
		assertFalse(magus1.getInventoryOf(AMU.sampleBook).contains(summa));
		assertFalse(magus1.getInventoryOnMarketOf(AMU.sampleBook).contains(summa));
		assertTrue(magus2.getInventory().contains(summa));
	}
	
	private List<Artefact> getBidOfVimVis(int numberOfPawns) {
		List<Artefact> retValue = new ArrayList<Artefact>();
		for (int i = 0; i < numberOfPawns; i++) {
			retValue.add(new Vis(Arts.VIM));
		}
		return retValue;
	}

}
