package hopshackle.simulation.arsmagica.test;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;

import hopshackle.simulation.*;
import hopshackle.simulation.arsmagica.*;

public class Covenants {
	private Magus founder, cofounder;
	private World w;
	private Covenant covenant;
	private Tribunal tribunal;

	@Before
	public void setup() {
		w = new World(new SimpleWorldLogic<Magus>(new ArrayList<ActionEnum<Magus>>(EnumSet.allOf(MagusActions.class))));
		w.setCalendar(new FastCalendar(800 * 52));
		tribunal = new Tribunal("test", w);
		founder = new Magus(w);
		founder.setCommunication(4);
		cofounder = new Magus(w);
		founder.addXP(Abilities.LATIN, 75);
		founder.addXP(Abilities.ARTES_LIBERALES, 75);
		founder.setTribunal(tribunal);
	}

	@Test
	public void covenantIsFoundedWithCorrectMembership() {
		assertTrue(founder.getLocation() == tribunal);
		List<Magus> cofounders = new ArrayList<Magus>();
		cofounders.add(founder);
		cofounders.add(cofounder);
		addStartAndRunAction(new FoundCovenant(cofounders));
		covenant = founder.getCovenant();
		assertTrue(cofounder.getCovenant() == covenant);
		assertTrue(covenant.isCurrentMember(founder));
		assertTrue(covenant.isCurrentMember(cofounder));
		assertEquals(covenant.getCurrentSize(), 2);

		assertTrue(founder.getLocation() == covenant);
		assertTrue(cofounder.getLocation() == covenant);

		assertTrue(covenant.getTribunal() == founder.getTribunal());
	}

	@Test
	public void apprenticeIsNotIncludedAsFounder() {
		Magus apprentice = new Magus(w);
		Action<?> apprenticeAction = apprentice.getNextAction();
		assertTrue(apprenticeAction == null);
		founder.addApprentice(apprentice);
		assertTrue(apprentice.isApprentice());
		Action<?> action = MagusActions.FOUND_COVENANT.getAction(founder);
		action.addToAllPlans();
		apprenticeAction = apprentice.getNextAction();
		assertFalse(apprenticeAction instanceof FoundCovenant);
		runNextAction(founder);
		covenant = founder.getCovenant();
		assertEquals(covenant.getCurrentSize(), 1);
		assertTrue(apprentice.getCovenant() == null);
		assertTrue(founder.getLocation() == covenant);
		assertTrue(apprentice.getLocation() == covenant);
	}

	@Test
	public void memberMagiUseAuraOfCovenant() {
		founder.setMagicAura(2);
		cofounder.setMagicAura(8);
		covenantIsFoundedWithCorrectMembership();
		assertEquals(covenant.getAura(), 2);
		assertEquals(founder.getMagicAura(), 2);
		assertEquals(cofounder.getMagicAura(), 2);
	}

	@Test
	public void memberJoinsCovenant() {
		Magus newMember = new Magus(w);
		covenantIsFoundedWithCorrectMembership();
		newMember.setCovenant(covenant);
		assertTrue(covenant.isCurrentMember(founder));
		assertTrue(covenant.isCurrentMember(cofounder));
		assertTrue(covenant.isCurrentMember(newMember));
		assertEquals(covenant.getCurrentSize(), 3);
		assertTrue(newMember.getCovenant() == covenant);
		assertTrue(newMember.getLocation() == covenant);
		assertTrue(newMember.getTribunal() == covenant.getTribunal());
	}

	@Test
	public void memberLeavesCovenant() {
		covenantIsFoundedWithCorrectMembership();
		cofounder.setCovenant(null);
		assertTrue(covenant.isCurrentMember(founder));
		assertFalse(covenant.isCurrentMember(cofounder));
		assertEquals(covenant.getCurrentSize(), 1);
		assertTrue(covenant.isOrHasBeenMember(cofounder));
		assertTrue(covenant.isOrHasBeenMember(founder));
		assertTrue(cofounder.getCovenant() == null);
		assertTrue(cofounder.getLocation() != covenant);
	}

	@Test
	public void memberDies() {
		covenantIsFoundedWithCorrectMembership();
		founder.die("Ooops");
		assertFalse(covenant.isCurrentMember(founder));
		assertTrue(covenant.isCurrentMember(cofounder));
		assertEquals(covenant.getCurrentSize(), 1);
		assertTrue(covenant.isOrHasBeenMember(founder));
		assertTrue(founder.getCovenant() == null);
	}

	@Test
	public void membersMayUseCovenantLibrary() {
		covenantIsFoundedWithCorrectMembership();
		assertFalse(MagusActions.READ_BOOK.isChooseable(founder));
		Book newBook = new Summa(Arts.AURAM, 15, 15, null);
		covenant.addItem(newBook);
		assertTrue(MagusActions.READ_BOOK.isChooseable(founder));
		assertTrue(founder.getBestBookToRead() == newBook);
	}

	@Test
	public void covenantIsDissolvedWithDeathOfLastMember() {
		covenantIsFoundedWithCorrectMembership();
		assertTrue(covenant.isExtant());
		founder.die("Oops");
		assertTrue(covenant.isExtant());
		cofounder.die("Oops");
		assertFalse(covenant.isExtant());
		assertTrue(covenant.getParentLocation() == null);

		for (Location l : w.getChildLocations()) {
			assertTrue(l != covenant);
		}
	}

	@Test
	public void buildPointsCalculatedCorrectly() {
		covenantIsFoundedWithCorrectMembership();
		covenant.setAuraAndCapacity(0, 10);
		covenant.maintenance();
		assertEquals(covenant.getBuildPoints(), 0);
		covenant.setAuraAndCapacity(1, 10);
		covenant.maintenance();
		assertEquals(covenant.getBuildPoints(), 10);
		covenant.setAuraAndCapacity(3, 10);
		covenant.maintenance();
		assertEquals(covenant.getBuildPoints(), 60);
		covenant.addXP(CovenantAttributes.WEALTH, 15);
		covenant.maintenance();
		assertEquals(covenant.getBuildPoints(), 66);
		covenant.addItem(new Summa(Arts.CREO, 12, 8, null));
		covenant.maintenance();
		assertEquals(covenant.getBuildPoints(), 75);
		covenant.addItem(new Summa(Arts.REGO, 6, 8, null));
		covenant.maintenance();
		assertEquals(covenant.getBuildPoints(), 77);
		covenant.addItem(new Summa(Arts.CREO, 6, 8, null));
		covenant.maintenance();
		assertEquals(covenant.getBuildPoints(), 77);
		covenant.addItem(new Summa(Abilities.MAGIC_THEORY, 2, 8, null));
		covenant.maintenance();
		assertEquals(covenant.getBuildPoints(), 79);
		VisSource vis = new VisSource(Arts.CORPUS, 5, w);
		covenant.getCovenantAgent().addItem(vis);
		covenant.maintenance();
		assertEquals(covenant.getBuildPoints(), 104);
		vis.setAnnualExtinctionRate(1.0);
		w.setCurrentTime((long) (801 * 52));
		vis.maintenance();
		assertEquals(vis.getAmountPerAnnum(), 0);
		covenant.maintenance();
		assertEquals(covenant.getBuildPoints(), 79);
		LabText labText = new LabText(new Spell(Arts.REGO, Arts.AQUAM, 40, "Powerful spell", null), null);
		covenant.addItem(labText);
		covenant.maintenance();
		assertEquals(covenant.getBuildPoints(), 83);
		LabText labText2 = new LabText(new Spell(Arts.REGO, Arts.AQUAM, 40, "Powerful spell", null), null);
		covenant.addItem(labText2);
		covenant.maintenance();
		assertEquals(covenant.getBuildPoints(), 83);
		LabText labText3 = new LabText(new Spell(Arts.REGO, Arts.AQUAM, 40, "Powerful spell 2", null), null);
		covenant.addItem(labText3);
		covenant.maintenance();
		assertEquals(covenant.getBuildPoints(), 86);
	}

	@Test
	public void incrementalLibraryCalculationsWorkCorrectly() {
		covenantIsFoundedWithCorrectMembership();
		covenant.addItem(new Summa(Arts.CREO, 6, 8, null));
		List<Book> libraryAdditions = new ArrayList<Book>();
		libraryAdditions.add(new Summa(Arts.CREO, 5, 8, null));
		assertEquals(covenant.calculateIncrementalLibraryPointsFrom(libraryAdditions), 0);
		libraryAdditions.add(new Summa(Arts.REGO, 12, 8, null));
		assertEquals(covenant.calculateIncrementalLibraryPointsFrom(libraryAdditions), 18);
		libraryAdditions.add(new Summa(Arts.CREO, 12, 8, null));
		assertEquals(covenant.calculateIncrementalLibraryPointsFrom(libraryAdditions), 32);
		libraryAdditions.add(new Summa(Arts.CREO, 12, 8, null));
		assertEquals(covenant.calculateIncrementalLibraryPointsFrom(libraryAdditions), 32);
	}

	@Test
	public void covenantApplicationBasicRoll() {
		covenantIsFoundedWithCorrectMembership();
		covenant.setAuraAndCapacity(3, 10);
		covenant.addXP(CovenantAttributes.WEALTH, 10);
		covenant.addItem(new Summa(Arts.CORPUS, 15, 15, null));
		covenant.addItem(new Summa(Arts.VIM, 12, 12, null));
		covenant.maintenance();
		// starting covenant build points are 90
		Magus applicant = new Magus(w);
		applicant.setIntelligence(3);
		applicant.setPresence(-1);
		// total modifier for applicant is +2 (no magic theory or charm)
		// with -2 (for the size of the covenant currently)
		// -0.40 for shortfall from 50 build points per member
		// for a total basic modifier of -0.40
		// so a roll of 7 should work
		// and a roll of 6 should fail

		CovenantApplication application = new CovenantApplication(covenant, applicant, 7);
		assertTrue(application.isSuccessful());
		application = new CovenantApplication(covenant, applicant, 6);
		assertFalse(application.isSuccessful());
	}

	@Test
	public void buildPointsOnlyCountUniqueTractatus() {
		Tractatus t1 = new Tractatus(Abilities.ARTES_LIBERALES, founder);
		Tractatus t2 = new Tractatus(Abilities.ARTES_LIBERALES, founder);
		Tractatus t3 = new Tractatus(t1);
		covenantIsFoundedWithCorrectMembership();
		covenant.addXP(CovenantAttributes.WEALTH, 10);
		covenant.addItem(t1);
		covenant.maintenance();
		assertEquals(covenant.calculateIncrementalLibraryPointsFrom(t2), 10);
		assertEquals(covenant.calculateIncrementalLibraryPointsFrom(t3), 0);
	}

	@Test
	public void valueOfCovenantApplicationTakesIntoAccountApplicantsCurrentSituation() {
		covenantIsFoundedWithCorrectMembership();
		covenant.setAuraAndCapacity(3, 10);
		covenant.addXP(CovenantAttributes.WEALTH, 12);
		covenant.addItem(new Summa(Arts.CORPUS, 30, 8, null));
		covenant.maintenance();
		assertEquals(covenant.getLevelOf(CovenantAttributes.WEALTH), 1);
		assertEquals(covenant.getBuildPoints(), 123);

		Magus applicant = new Magus(w);
		CovenantApplication application = new CovenantApplication(covenant, applicant);
		assertEquals(application.getNetValueToApplicant(), 123);
		applicant.setMagicAura(2);		// removes 30 effective points
		application = new CovenantApplication(covenant, applicant);
		assertEquals(application.getNetValueToApplicant(), 93);	

		List<Magus> newFounder = new ArrayList<Magus>();
		newFounder.add(applicant);
		Covenant newCov = new Covenant(newFounder, tribunal);
		newCov.setAuraAndCapacity(5, 10);
		newCov.maintenance();
		assertEquals(newCov.getBuildPoints(), 150);
		application = new CovenantApplication(newCov, founder);
		assertEquals(SocialMeeting.relationshipModifier(founder, cofounder), 0);
		assertEquals(SocialMeeting.relationshipModifier(founder, applicant), 0);
		assertEquals(CovenantApplication.getSocialModifier(founder, newCov), -1);	// default of -1 per member
		assertEquals(CovenantApplication.getSocialModifier(founder, covenant), -2);
		assertEquals(application.getNetValueToApplicant(), 150 - 123 + 20);		// equal to difference between covenant bp
																				// +20 from 'overcrowding' at home
	}

	@Test
	public void covenantApplicationCanBeBoostedWithBooks() {
		covenantIsFoundedWithCorrectMembership();
		covenant.setAuraAndCapacity(2, 10);				// +30 from Aura
		covenant.addXP(CovenantAttributes.WEALTH, 55);	// lvl 4, so +12
		assertEquals(covenant.calculateIncrementalLibraryPointsFrom(new Summa(Arts.AURAM, 18, 10, null)), 46);
		covenant.addItem(new Summa(Arts.AURAM, 18, 10, null));	// + 23
		covenant.addItem(new VisSource(Arts.MENTEM, 7, tribunal));  // + 35
		assertEquals(covenant.calculateIncrementalLibraryPointsFrom(new Tractatus(Arts.ANIMAL, founder)), 10);
		covenant.maintenance();
		assertEquals(covenant.getBuildPoints(), 100);
		// starting covenant build points are 100 (exactly in line with 2 members)
		Magus applicant = new Magus(w);
		applicant.setIntelligence(3);
		applicant.setPresence(0);
		applicant.addXP(Abilities.MAGIC_THEORY, 15);	//lvl 2
		// total modifier is +3
		CovenantApplication application = new CovenantApplication(covenant, applicant, 6);
		assertTrue(application.isSuccessful());
		application = new CovenantApplication(covenant, applicant, 2);
		assertFalse(application.isSuccessful());

		assertEquals(covenant.calculateIncrementalLibraryPointsFrom(new Summa(Arts.IMAGINEM, 10, 10, null)), 14);
		applicant.addItem(new Summa(Arts.IMAGINEM, 10, 10, null));	// takes modifier to +9
		application = new CovenantApplication(covenant, applicant, -13);
		assertFalse(application.isSuccessful());
		application = new CovenantApplication(covenant, applicant, 3);
		assertTrue(application.isSuccessful());
		assertEquals(applicant.getInventoryOf(AMU.sampleBook).size(), 1);
		assertEquals(application.getNetValueToApplicant(), 100);
		application.acceptApplication();
		assertTrue(applicant.getCovenant() == covenant);
		assertEquals(applicant.getInventoryOf(AMU.sampleBook).size(), 0);
		covenant.maintenance();
		assertEquals(covenant.getBuildPoints(), 107);
	}

	@Test
	public void covenantApplicationCanBeBoostedWithVisSource() {
		covenantIsFoundedWithCorrectMembership();
		assertEquals(covenant.getBuildPoints(), 0);
		covenant.setAuraAndCapacity(2, 10);						// +30
		covenant.maintenance();
		assertEquals(covenant.getBuildPoints(), 30);
		covenant.addXP(CovenantAttributes.WEALTH, 30);	// +3 per level (so +6)
		covenant.addXP(CovenantAttributes.GROGS, 65);	// +3 per level (so +12)
		covenant.maintenance();
		assertEquals(covenant.getBuildPoints(), 48);
		assertEquals(covenant.calculateIncrementalLibraryPointsFrom(new Summa(Arts.AURAM, 10, 11, null)), 15);
		covenant.addItem(new Summa(Arts.AURAM, 10, 11, null));
		covenant.maintenance();
		assertEquals(covenant.getBuildPoints(), 55);	// added at half rate
		covenant.addItem(new VisSource(Arts.MENTEM, 9, tribunal));	// +5 per pawn per annum, so +45
		covenant.maintenance();
		assertEquals(covenant.getBuildPoints(), 100);
		Magus applicant = new Magus(w);
		applicant.setIntelligence(3);
		applicant.setPresence(0);
		applicant.addXP(Abilities.MAGIC_THEORY, 15);	//lvl 2
		// total modifier is +3, -2 from membership is +1, +0 from covenant BP vs target
		assertEquals(CovenantApplication.getSocialModifier(applicant, covenant), -2);
		
		applicant.addItem(new VisSource(Arts.AQUAM, 2, tribunal));	// takes modifier to +4.3
		CovenantApplication application = new CovenantApplication(covenant, applicant, 1);
		assertFalse(application.isSuccessful());
		application = new CovenantApplication(covenant, applicant, 2);
		assertTrue(application.isSuccessful());
		assertEquals(applicant.getInventoryOf(AMU.sampleVisSource).size(), 1);
		assertEquals(application.getNetValueToApplicant(), 60);
		application.acceptApplication();
		assertTrue(applicant.getCovenant() == covenant);
		assertEquals(applicant.getInventoryOf(AMU.sampleVisSource).size(), 0);
		assertEquals(applicant.getSeasonsServiceOwed(), 10);
		covenant.maintenance();
		assertEquals(covenant.getBuildPoints(), 110);
	}

	@Test
	public void covenantApplicationCanBeBoostedWithVis() {
		covenantIsFoundedWithCorrectMembership();
		covenant.addXP(CovenantAttributes.WEALTH, 15);
		Magus applicant = new Magus(w);
		applicant.setIntelligence(3);
		applicant.setPresence(0);
		// base modifier is +3, with additional -2 from social modifier, -4 from build points of 0 (with aim of 100 for 2 members)
		applicant.addVis(Arts.VIM, 200);
		CovenantApplication application = new CovenantApplication(covenant, applicant, 7);	
		// therefore 75 pawns of Vis needed (15 pawns per missing point)
		assertTrue(application.isSuccessful());
		assertTrue(applicant.getCovenant() == null);
		application.acceptApplication();
		assertEquals(applicant.getPawnsOf(Arts.VIM), 125);
		assertTrue(applicant.getCovenant() == covenant);
	}

	@Test
	public void covenantApplicationUsesBooksBeforeVisAndVisSources() {
		covenantIsFoundedWithCorrectMembership();
		covenant.addXP(CovenantAttributes.WEALTH, 15);
		Magus applicant = new Magus(w);
		applicant.setIntelligence(3);
		applicant.setPresence(0);
		// base modifier is +3, -2 from social modifier, -4 from 0 bp vs 100 target to give net -3
		applicant.addVis(Arts.VIM, 200);
		List<Book> books = new ArrayList<Book>();
		books.add(new Summa(Arts.ANIMAL, 10, 11, null));
		applicant.addItem(books.get(0));
		applicant.addItem(new VisSource(Arts.AURAM, 2, tribunal));
		applicant.addItem(new VisSource(Arts.MENTEM, 2, tribunal));
		assertEquals(covenant.calculateIncrementalLibraryPointsFrom(books), 15);
		CovenantApplication application = new CovenantApplication(covenant, applicant, 6);	
		// therefore needs 15 build points worth of books (+5)
		// and then 1 vis source to get to 9
		assertTrue(application.isSuccessful());
		application.acceptApplication();
		assertEquals(applicant.getPawnsOf(Arts.VIM), 200);
		assertEquals(applicant.getInventoryOf(AMU.sampleBook).size(), 0);
		assertEquals(applicant.getInventoryOf(AMU.sampleVisSource).size(), 1);
		assertEquals(covenant.getLibrary().size(), 1);
		assertEquals(covenant.getAnnualVisSupply(), 2);
	}

	@Test
	public void covenantInheritsEverythingIfMagusDiesWithoutHeirs() {
		covenantIsFoundedWithCorrectMembership();
		founder.setPolicy(new MagusApprenticeInheritance());
		founder.addItem(new Summa(Arts.CREO, 5, 15, founder));
		founder.addVis(Arts.CREO, 10);
		founder.addItem(new VisSource(Arts.CREO, 5, tribunal));
		founder.addApprentice(new Magus(w));
		assertFalse(cofounder.hasApprentice());
		Agent cAgent = covenant.getCovenantAgent();
		assertEquals(cAgent.getInventoryOf(AMU.sampleBook).size(), 0);
		assertEquals(cAgent.getInventoryOf(AMU.sampleVisSource).size(), 0);
		assertEquals(cAgent.getInventoryOf(AMU.sampleVis).size(), 0);
		founder.die("oops");
		assertTrue(cofounder.hasApprentice());
		assertEquals(cAgent.getInventoryOf(AMU.sampleBook).size(), 1);
		assertEquals(cAgent.getInventoryOf(AMU.sampleVisSource).size(), 1);
		assertEquals(cAgent.getInventoryOf(AMU.sampleVis).size(), 10);
	}
	
	private void addStartAndRunAction(ArsMagicaAction a) {
		a.addToAllPlans();
		a.start();
		a.run();
	}
	private void runNextAction(Magus m) {
		Action<?> a = m.getActionPlan().getNextAction();
		a.start();
		a.run();
	}
	
}
