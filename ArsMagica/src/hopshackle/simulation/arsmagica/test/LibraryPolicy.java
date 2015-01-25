package hopshackle.simulation.arsmagica.test;

import static org.junit.Assert.*;

import java.util.*;

import hopshackle.simulation.*;
import hopshackle.simulation.arsmagica.*;

import org.junit.*;

public class LibraryPolicy {

	private Covenant covenant;
	private World world;
	private Tribunal tribunal;
	private List<Magus> founders;

	@Before
	public void setup() {
		SimProperties.setProperty("MagusUniformResearchPreferences", "true");
		world = new World();
		world.setCalendar(new FastCalendar(800 * 52));
		tribunal = new Tribunal("Base", world);
		tribunal.maintenance();	// sets nest Tribunal for 807
		world.setCurrentTime((long) (806 * 52 + 26));
		founders = new ArrayList<Magus>();
		for (int i = 0; i < 2; i ++) 
			founders.add(new Magus(world));

		covenant = new Covenant(founders, tribunal);
		covenant.addXP(CovenantAttributes.WEALTH, 15); // permits 40 books, so should avoid problems
		for (int i = 0; i < 2; i ++) 
			founders.get(i).setCovenant(covenant); 	// this is usually done within FoundCovenant
	}


	@Test
	public void covSupercededBookIsPutOntoMarket() {
		covenant.addItem(new Summa(Arts.CREO, 5, 15, null));
		Book book2 = new Summa(Abilities.ARTES_LIBERALES, 2, 8, null);
		covenant.addItem(book2);
		covenant.maintenance();
		assertEquals(covenant.getLibrary().size(), 2);

		Book redundantBook = new Summa(Arts.CREO, 5, 10, null);
		covenant.addItem(redundantBook);
		assertEquals(covenant.getLibrary().size(), 3);
		world.setCurrentTime((long) (805*52));	// two years before next tribunal
		covenant.maintenance();
		assertEquals(covenant.getLibrary().size(), 2);

		assertEquals(tribunal.getOffersOnMarket().size(), 1);
		assertTrue(tribunal.getOffersOnMarket().get(0).getItem().equals(redundantBook));

		Book nonSupercedingBook = new Summa(Abilities.ARTES_LIBERALES, 4, 7, null);
		covenant.addItem(nonSupercedingBook);
		covenant.maintenance();
		assertEquals(covenant.getLibrary().size(), 3);

		Book supercedingBook = new Summa(Abilities.ARTES_LIBERALES, 2, 10, null);
		covenant.addItem(supercedingBook);
		covenant.maintenance();
		assertEquals(covenant.getLibrary().size(), 3);

		assertEquals(tribunal.getOffersOnMarket().size(), 2);
		assertTrue(tribunal.getOffersOnMarket().get(1).getItem().equals(book2));
	}

	@Test
	public void covDuplicateLabTextIsPutOnMarket() {
		Spell s1 = new Spell(Arts.CREO, Arts.CORPUS, 25, "test spell", null);
		Spell s2 = new Spell(Arts.CREO, Arts.CORPUS, 25, "test spell", founders.get(0));
		Spell s3 = new Spell(Arts.CREO, Arts.CORPUS, 20, "test spell", founders.get(0));
		covenant.addItem(new LabText(s1, null));
		covenant.addItem(new LabText(s2, null));
		covenant.addItem(new LabText(s1, founders.get(0)));
		covenant.addItem(new LabText(s3, founders.get(0)));
		assertEquals(LabText.extractAllLabTextsFrom(covenant.getLibrary()).size(), 4);
		world.setCurrentTime((long) (805*52));	// two years before next tribunal
		covenant.maintenance();

		assertEquals(covenant.getLibrary().size(), 2);
		assertEquals(LabText.extractAllLabTextsFrom(covenant.getLibrary()).size(), 2);
	}
	
	@Test
	public void surplusVisIsPutOnMarket() {
		Magus m1 = founders.get(0);
		Magus m2 = founders.get(1);
		m1.addVis(Arts.CREO, 12);
		m2.addVis(Arts.TERRAM, 5);
		m2.addVis(Arts.REGO, 10);
		m1.setAge(50);
		MagusPreferences.setResearchPreference(m2, Arts.TERRAM, 1.5);
		MagusPreferences.setResearchPreference(m1, Arts.CREO, 0.5);
		
		m2.addXP(Arts.TERRAM, Arts.TERRAM.getXPForLevel(10));
		// so m1 should keep 5 pawns of CREO for Longevity ritual
		// and m2 should keep 4 pawns of TERRAM vis for experimentation
		
		// We then say that the first 5 pawns of any type is kept for spending
		// and the remainder is then put up for auction
		
		// so we expect m1 to put up 2 pawns CREO for auction
		// and m2 to put up 5 pawns of REGO, and 0 pawns of TERRAM
		m1.maintenance();
		m2.maintenance();
		
		assertEquals((long)AMU.getVisInventory(m1).get(Arts.CREO),10);
		assertEquals((long)AMU.getVisInventory(m2).get(Arts.TERRAM),5);
		assertEquals((long)AMU.getVisInventory(m2).get(Arts.REGO), 4);	// but m2 will then but 1 pawns of REGO for the two pawns of CREO (meeting their reserve price)
		boolean foundBid = false;
		for (BarterOffer bo : tribunal.getOffersOnMarket()) {
			if (bo.getItem().equals(new Vis(Arts.CREO)) && bo.getBestBid() == 1.0)
				foundBid = true;
		}
		assertTrue(foundBid);
	}

	@Test
	public void sameVisIsNotUsedForTwoDifferentBids() {
		Magus m1 = founders.get(0);
		Magus m2 = founders.get(1);
		m1.addVis(Arts.CREO, 10);
		m1.setAge(50);
		MagusPreferences.setResearchPreference(m1, Arts.CREO, 0.5);
		
		BarterOffer bo1 = new BarterOffer(m2, new Summa(Arts.AQUAM, 10, 10, m2), 1, 2, null);
		BarterOffer bo2 = new BarterOffer(m2, new Summa(Arts.VIM, 10, 10, m2), 1, 2, null);
		tribunal.addToMarket(bo1);
		tribunal.addToMarket(bo2);
		
		m1.maintenance();
		// m1 should have five pawns of CREO to spend, so should have one left (plus the five from reserve for Longevity ritual)
		assertEquals((long)AMU.getVisInventory(m1).get(Arts.CREO), 6);
		assertEquals(tribunal.getOffersOnMarket().get(0).getBestBid(), 2.0, 0.01);
	}
	
	@Test
	public void magusBidsOnUsefulSummaWithSufficientVis() {
		Book forSale = new Summa(Arts.CREO, 5, 15, null);
		founders.get(0).addItem(new Summa(Arts.CORPUS, 5, 15, null));
		for (int i = 0; i < 10; i++)
			founders.get(0).addItem(new Vis(Arts.VIM));
		BarterOffer offer = new BarterOffer(founders.get(1), forSale, 1, 0, null);
		tribunal.addToMarket(offer);
		founders.get(0).maintenance();
		assertEquals(offer.getBestBid(), 1.0, 0.001);
	}
	

	@Test
	public void magusDoesNotBidIfInsufficientVis() {
		Magus m1 = founders.get(0);
		Magus m2 = founders.get(1);
		m1.addVis(Arts.CREO, 10);
		m1.setAge(50);
		MagusPreferences.setResearchPreference(m1, Arts.CREO, 0.5);
		
		BarterOffer bo1 = new BarterOffer(m2, new Summa(Arts.AQUAM, 10, 10, m2), 1, 10, null);
		tribunal.addToMarket(bo1);
		
		m1.maintenance();
		assertEquals((long)AMU.getVisInventory(m1).get(Arts.CREO), 10);
		assertEquals(tribunal.getOffersOnMarket().get(0).getBestBid(), 0.0, 0.01);
	}
	
	@Test
	public void magiDoNotBidOnSummaAlreadyOwned() {
		Book forSale = new Summa(Arts.CREO, 5, 15, null);
		founders.get(0).addItem(new Summa(Arts.CREO, 5, 15, null));
		for (int i = 0; i < 10; i++)
			founders.get(0).addItem(new Vis(Arts.VIM));
		BarterOffer offer = new BarterOffer(founders.get(1), forSale, 1, 0, null);
		tribunal.addToMarket(offer);
		founders.get(0).maintenance();
		assertEquals(offer.getBestBid(), 0.0, 0.001);
	}

	@Test
	public void magPutsLabTextOnMarketIfSpellLearnt() {
		LabText knownSpell = new LabText(new Spell(Arts.CREO, Arts.AQUAM, 10, "Known Spell", null), null);
		LabText unknownSpell = new LabText(new Spell(Arts.CREO, Arts.AQUAM, 10, "Unknown Spell", null), null);
		founders.get(0).addItem(knownSpell);
		founders.get(0).addItem(unknownSpell);
		assertEquals(founders.get(0).getInventoryOf(AMU.sampleBook).size(), 2);
		founders.get(0).addSpell(new Spell(Arts.CREO, Arts.AQUAM, 10, "Known Spell", null));
		founders.get(0).maintenance();
		assertEquals(founders.get(0).getInventoryOf(AMU.sampleBook).size(), 1);
		assertTrue(founders.get(0).getInventoryOf(AMU.sampleBook).get(0).toString().startsWith("Lab Text for CrAq 10  Unknown Spell"));
		assertEquals(tribunal.getOffersOnMarket().size(), 1);
	}

	@Test
	public void booksInExcessOfLimitDeteriorate() {
		Summa creoSumma = new Summa(Arts.CREO, 10, 10, null);
		covenant.addItem(creoSumma);
		for (int i = 0; i < 10; i++)
			covenant.addItem(new Tractatus(Arts.CREO, founders.get(0)));
		LabText labText = new LabText(new Spell(Arts.CREO, Arts.TERRAM, 10, "Random", null), null);
		covenant.addItem(labText);
		Summa perdoSumma = new Summa(Arts.PERDO, 8, 10, null);
		covenant.addItem(perdoSumma);

		assertEquals(covenant.getLibrary().size(), 13);
		covenant.maintenance();		
		assertEquals(labText.getDeterioration(), 0.00, 0.001);
		assertEquals(creoSumma.getDeterioration(), 0.00, 0.001);
		assertEquals(covenant.getLibrary().size(), 13);
		covenant.addXP(CovenantAttributes.WEALTH, -15);
		covenant.maintenance();		
		assertEquals(covenant.getLibrary().size(), 13);
		assertEquals(labText.getDeterioration(), 2.0/300.0, 0.001);
		assertEquals(creoSumma.getDeterioration(), 0.00, 0.001);
	}
	
	@Test
	public void surplusBooksDonatedToCovenantInLieuOfCovenantService() {
		Magus m1 = founders.get(0);
		Summa creoSumma = new Summa(Arts.CREO, 10, 10, null);
		m1.addItem(creoSumma);
		m1.setSeasonsServiceOwed(2);
		assertEquals(m1.getInventoryOf(AMU.sampleBook).size(), 1);
		assertTrue(m1.getSeasonsServiceOwed() == 2);
		assertEquals(covenant.getLibrary().size(), 0);
		assertTrue(m1.getCovenant() != null);
		m1.maintenance();
		assertEquals(m1.getInventoryOf(AMU.sampleBook).size(), 0);
		assertEquals(m1.getSeasonsServiceOwed(), 0);
		assertEquals(covenant.getLibrary().size(), 1);
	}

}
