package hopshackle.simulation.arsmagica.test;

import static org.junit.Assert.*;

import java.util.*;

import hopshackle.simulation.*;
import hopshackle.simulation.arsmagica.*;

import org.junit.*;

public class VisValuationAndBidding {
	
	private Magus magus, bidder;
	private World world;
	private Summa summa;
	private Tribunal tribunal;

	@Before
	public void setUp() throws Exception {
		SimProperties.setProperty("MagusUniformResearchPreferences", "true");
		world = new World(new SimpleWorldLogic<Magus>(new ArrayList<ActionEnum<Magus>>(EnumSet.allOf(MagusActions.class))));
		world.setCalendar(new FastCalendar(800 * 52));
		tribunal = new Tribunal("test", world);
		world.setCurrentTime(806l*52l + 26l);
		magus = new Magus(world);
		bidder = new Magus(world);
		summa = new Summa(Abilities.CONCENTRATION, 5, 10, magus);
		magus.addItem(summa);
	}

	@Test
	public void valuationFunctionValuesVisCorrectly() {
		VisValuationFunction function = new VisValuationFunction(magus);
		List<Artefact> visSupply = new ArrayList<Artefact>();
		visSupply.add(new Vis(Arts.CREO));
		assertEquals(function.getValue(visSupply), 1.0, 0.001);
		visSupply.add(new Vis(Arts.CREO));
		assertEquals(function.getValue(visSupply), 2.0, 0.001);
		MagusPreferences.setResearchPreference(magus, Arts.CREO, 1.50);
		assertEquals(function.getValue(visSupply), 3.0, 0.001);
		visSupply.add(new Vis(Arts.IMAGINEM));
		assertEquals(function.getValue(visSupply), 4.0, 0.001);
	}
	
	@Test
	public void bidValueStoredInBarterBidAndOfferOnCreationBasedOnSellersPreferences() {
		MagusPreferences.setResearchPreference(bidder, Arts.PERDO, 0.5);
		bidder.addVis(Arts.PERDO, 2);
		List<Artefact> offeredVis = new ArrayList<Artefact>();
		offeredVis.add(new Vis(Arts.PERDO));
		offeredVis.add(new Vis(Arts.PERDO));
		BarterOffer bo = new BarterOffer(magus, summa, 1, 1, new VisValuationFunction(magus));
		assertEquals(bo.getBestBid(), 0.0, 0.001);
		BarterBid bb = new BarterBid(bidder, offeredVis, bo);
		assertEquals(bidder.getNumberInInventoryOf(AMU.sampleVis), 2);
		assertTrue(bo.submitBid(bb));
		assertEquals(bo.getBestBid(), 2.0, 0.001);
		assertEquals(bb.getAmount(), 2.0, 0.001);
		assertEquals(bidder.getNumberInInventoryOf(AMU.sampleVis), 0);
	}
	
	@Test
	public void bidMustAtLeastMeetReservePrice() {
		MagusPreferences.setResearchPreference(magus, Arts.PERDO, 1.4);
		bidder.addVis(Arts.PERDO, 3);
		List<Artefact> offeredVis = new ArrayList<Artefact>();
		offeredVis.add(new Vis(Arts.PERDO));
		offeredVis.add(new Vis(Arts.PERDO));
		BarterOffer bo = new BarterOffer(magus, summa, 1, 3.5, new VisValuationFunction(magus));
		BarterBid bb = new BarterBid(bidder, offeredVis, bo);
		assertFalse(bo.submitBid(bb));
		assertEquals(bidder.getNumberInInventoryOf(AMU.sampleVis), 3);
		offeredVis.add(new Vis(Arts.PERDO));
		bb = new BarterBid(bidder, offeredVis, bo);
		assertTrue(bo.submitBid(bb));
		assertEquals(bidder.getNumberInInventoryOf(AMU.sampleVis), 0);
	}
	
	@Test
	public void aHopelessEarlyOfferIsIgnoredForALaterBetterOffer() {
		// we have the first Offer with a high reserve price that we can't meet
		// the second offer has a bid, but we can supercede it
		MagusPreferences.setResearchPreference(bidder, Arts.PERDO, 0.4);
		List<Artefact> offeredVis = new ArrayList<Artefact>();
		offeredVis.add(new Vis(Arts.PERDO));
		offeredVis.add(new Vis(Arts.PERDO));
		
		Tractatus tractatus = new Tractatus(Arts.HERBAM, magus);
		magus.addItem(tractatus);
		BarterOffer bo = new BarterOffer(magus, summa, 1, 5.0, new VisValuationFunction(magus));
		tribunal.addToMarket(bo);
		BarterOffer bo2 = new BarterOffer(magus, tractatus, 1, 1.0, new VisValuationFunction(magus));
		tribunal.addToMarket(bo2);
		BarterBid competition = new BarterBid(magus, offeredVis, bo2);
		assertTrue(bo2.submitBid(competition));
		
		assertEquals(bo.getBestBid(), 0.0, 0.01);
		assertEquals(bo2.getBestBid(), 2.0, 0.01);
		
		bidder.addVis(Arts.PERDO, 3);
		bidder.maintenance();
		
		assertEquals(bo.getBestBid(), 0.0, 0.01);
		assertEquals(bo2.getBestBid(), 3.0, 0.01);
		assertTrue(bo2.getCurrentWinner().equals(bidder));
		
	}
	
	@Test
	public void barterBidOnlyAcceptedIfPreviousBidIsSuperceded() {
		MagusPreferences.setResearchPreference(magus, Arts.PERDO, 1.4);
		bidder.addVis(Arts.PERDO, 10);
		List<Artefact> offeredVis = new ArrayList<Artefact>();
		offeredVis.add(new Vis(Arts.PERDO));
		offeredVis.add(new Vis(Arts.PERDO));
		BarterOffer bo = new BarterOffer(magus, summa, 1, 1, new VisValuationFunction(magus));
		BarterBid bb = new BarterBid(bidder, offeredVis, bo);
		assertTrue(bo.submitBid(bb));
		assertEquals(bidder.getNumberInInventoryOf(AMU.sampleVis), 8);
		bb = new BarterBid(bidder, offeredVis, bo);
		assertFalse(bo.submitBid(bb));
		assertEquals(bidder.getNumberInInventoryOf(AMU.sampleVis), 8);
	}
	
	@Test
	public void supercededBarterBidIsReturnedToBidder() {
		MagusPreferences.setResearchPreference(magus, Arts.PERDO, 1.4);
		bidder.addVis(Arts.PERDO, 10);
		List<Artefact> offeredVis = new ArrayList<Artefact>();
		offeredVis.add(new Vis(Arts.PERDO));
		offeredVis.add(new Vis(Arts.PERDO));
		BarterOffer bo = new BarterOffer(magus, summa, 1, 1, new VisValuationFunction(magus));
		BarterBid bb = new BarterBid(bidder, offeredVis, bo);
		assertTrue(bo.submitBid(bb));
		assertEquals(bidder.getNumberInInventoryOf(AMU.sampleVis), 8);
		assertEquals(bo.getBestBid(), 2.8, 0.001);
		offeredVis.add(new Vis(Arts.PERDO));
		bb = new BarterBid(bidder, offeredVis, bo);
		assertEquals(bo.getBestBid(), 2.8, 0.001);
		assertTrue(bo.submitBid(bb));
		assertEquals(bidder.getNumberInInventoryOf(AMU.sampleVis), 7);
		assertEquals(bo.getBestBid(), 4.2, 0.001);
	}
	
	@Test
	public void finalBidIsAcceptedAndGoodTransferred() {
		MagusPreferences.setResearchPreference(magus, Arts.PERDO, 1.4);
		bidder.addVis(Arts.PERDO, 10);
		List<Artefact> offeredVis = new ArrayList<Artefact>();
		offeredVis.add(new Vis(Arts.PERDO));
		offeredVis.add(new Vis(Arts.PERDO));
		BarterOffer bo = new BarterOffer(magus, summa, 1, 1, new VisValuationFunction(magus));
		BarterBid bb = new BarterBid(bidder, offeredVis, bo);
		assertTrue(bo.submitBid(bb));
		assertEquals(bidder.getNumberInInventoryOf(AMU.sampleVis), 8);
		assertEquals(magus.getNumberInInventoryOf(AMU.sampleVis), 0);
		assertEquals(bidder.getNumberInInventoryOf(AMU.sampleBook), 0);
		assertEquals(magus.getNumberInInventoryOf(AMU.sampleBook), 0);

		bo.resolve();
		assertEquals(bidder.getNumberInInventoryOf(AMU.sampleVis), 8);
		assertEquals(magus.getNumberInInventoryOf(AMU.sampleVis), 2);
		assertEquals(bidder.getNumberInInventoryOf(AMU.sampleBook), 1);
		assertEquals(magus.getNumberInInventoryOf(AMU.sampleBook), 0);
	}

}
