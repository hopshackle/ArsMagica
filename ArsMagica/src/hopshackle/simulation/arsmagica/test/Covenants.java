package hopshackle.simulation.arsmagica.test;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;

import hopshackle.simulation.*;
import hopshackle.simulation.arsmagica.*;
import hopshackle.simulation.arsmagica.Tractatus;

public class Covenants {
	private Magus founder, cofounder;
	private World w;
	private Covenant covenant;
	private Tribunal tribunal;

	@Before
	public void setup() {
		w = new World();
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
		cofounders.add(cofounder);
		new FoundCovenant(founder, cofounders).run();
		covenant = founder.getCovenant();
		assertTrue(cofounder.getCovenant() == null);
		cofounder.getNextAction().run();
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
		founder.addApprentice(apprentice);
		assertTrue(apprentice.isApprentice());
		Action action = MagusActions.FOUND_COVENANT.getAction(founder);
		action.run();
		covenant = founder.getCovenant();
		Action apprenticeAction = apprentice.getNextAction();
		assertTrue(apprenticeAction instanceof BeTaught);
		assertEquals(covenant.getCurrentSize(), 1);
		apprenticeAction.run();
		apprenticeAction = apprentice.getNextAction();
		assertFalse(apprenticeAction instanceof FoundCovenant);
		assertTrue(apprentice.getCovenant() == null);
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
		covenant.setAura(0);
		covenant.maintenance();
		assertEquals(covenant.getBuildPoints(), 0);
		covenant.setAura(1);
		covenant.maintenance();
		assertEquals(covenant.getBuildPoints(), 10);
		covenant.setAura(3);
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
		assertEquals(covenant.getBuildPoints(), 82);
		covenant.addItem(new Summa(Arts.CREO, 6, 8, null));
		covenant.maintenance();
		assertEquals(covenant.getBuildPoints(), 82);
		covenant.addItem(new Summa(Abilities.MAGIC_THEORY, 2, 8, null));
		covenant.maintenance();
		assertEquals(covenant.getBuildPoints(), 88);
		VisSource vis = new VisSource(Arts.CORPUS, 5, w);
		covenant.getCovenantAgent().addItem(vis);
		covenant.maintenance();
		assertEquals(covenant.getBuildPoints(), 113);
		vis.setAnnualExtinctionRate(1.0);
		w.setCurrentTime((long) (801 * 52));
		vis.maintenance();
		assertEquals(vis.getAmountPerAnnum(), 0);
		covenant.maintenance();
		assertEquals(covenant.getBuildPoints(), 88);
		LabText labText = new LabText(new Spell(Arts.REGO, Arts.AQUAM, 40, "Powerful spell", null), null);
		covenant.addItem(labText);
		covenant.maintenance();
		assertEquals(covenant.getBuildPoints(), 92);
		LabText labText2 = new LabText(new Spell(Arts.REGO, Arts.AQUAM, 40, "Powerful spell", null), null);
		covenant.addItem(labText2);
		covenant.maintenance();
		assertEquals(covenant.getBuildPoints(), 92);
		LabText labText3 = new LabText(new Spell(Arts.REGO, Arts.AQUAM, 40, "Powerful spell 2", null), null);
		covenant.addItem(labText3);
		covenant.maintenance();
		assertEquals(covenant.getBuildPoints(), 96);
	}

	@Test
	public void incrementalLibraryCalculationsWorkCorrectly() {
		covenantIsFoundedWithCorrectMembership();
		covenant.addItem(new Summa(Arts.CREO, 6, 8, null));
		List<Book> libraryAdditions = new ArrayList<Book>();
		libraryAdditions.add(new Summa(Arts.CREO, 5, 8, null));
		assertEquals(covenant.calculateIncrementalBuildPointsFrom(libraryAdditions), 0);
		libraryAdditions.add(new Summa(Arts.REGO, 12, 8, null));
		assertEquals(covenant.calculateIncrementalBuildPointsFrom(libraryAdditions), 19);
		libraryAdditions.add(new Summa(Arts.CREO, 12, 8, null));
		assertEquals(covenant.calculateIncrementalBuildPointsFrom(libraryAdditions), 25);
		libraryAdditions.add(new Summa(Arts.CREO, 12, 8, null));
		assertEquals(covenant.calculateIncrementalBuildPointsFrom(libraryAdditions), 25);
	}

	@Test
	public void covenantApplicationBasicRoll() {
		covenantIsFoundedWithCorrectMembership();
		covenant.setAura(3);
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
		assertEquals(covenant.calculateIncrementalBuildPointsFrom(t2), 10);
		assertEquals(covenant.calculateIncrementalBuildPointsFrom(t3), 0);
	}

	@Test
	public void valueOfCovenantApplicationTakesIntoAccountApplicantsCurrentSituation() {
		covenantIsFoundedWithCorrectMembership();
		covenant.setAura(3);
		covenant.addXP(CovenantAttributes.WEALTH, 12);
		covenant.addItem(new Summa(Arts.CORPUS, 30, 8, null));
		covenant.maintenance();
		assertEquals(covenant.getLevelOf(CovenantAttributes.WEALTH), 1);
		assertEquals(covenant.getBuildPoints(), 81);

		Magus applicant = new Magus(w);
		CovenantApplication application = new CovenantApplication(covenant, applicant);
		assertEquals(application.getNetValueToApplicant(), 81);
		applicant.setMagicAura(2);
		application = new CovenantApplication(covenant, applicant);
		assertEquals(application.getNetValueToApplicant(), 51);

		List<Magus> newFounder = new ArrayList<Magus>();
		newFounder.add(applicant);
		Covenant newCov = new Covenant(newFounder, tribunal);
		newCov.setAura(5);
		newCov.maintenance();
		assertEquals(newCov.getBuildPoints(), 150);
		application = new CovenantApplication(newCov, founder);
		assertEquals(application.getNetValueToApplicant(), 150 - 81);
	}

	@Test
	public void covenantApplicationCanBeBoostedWithBooks() {
		covenantIsFoundedWithCorrectMembership();
		covenant.setAura(2);
		covenant.addXP(CovenantAttributes.WEALTH, 50);
		covenant.addItem(new Summa(Arts.AURAM, 18, 9, null));
		covenant.addItem(new VisSource(Arts.MENTEM, 7, tribunal));
		covenant.addItem(new Tractatus(Arts.ANIMAL, founder));
		covenant.addItem(new Tractatus(Arts.MENTEM, founder));
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
		assertEquals(covenant.getBuildPoints(), 110);
	}

	@Test
	public void covenantApplicationCanBeBoostedWithVisSource() {
		covenantIsFoundedWithCorrectMembership();
		covenant.setAura(2);
		covenant.addXP(CovenantAttributes.WEALTH, 20);
		covenant.addXP(CovenantAttributes.GROGS, 35);
		assertEquals(covenant.calculateIncrementalBuildPointsFrom(new Summa(Arts.AURAM, 10, 11, null)), 21);
		covenant.addItem(new Summa(Arts.AURAM, 10, 11, null));
		covenant.addItem(new VisSource(Arts.MENTEM, 9, tribunal));
		covenant.maintenance();
		assertEquals(covenant.getBuildPoints(), 100);
		// starting covenant build points are 100 (exactly in line with 2 members)
		Magus applicant = new Magus(w);
		applicant.setIntelligence(3);
		applicant.setPresence(0);
		applicant.addXP(Abilities.MAGIC_THEORY, 15);	//lvl 2
		// total modifier is +3

		applicant.addItem(new VisSource(Arts.AQUAM, 2, tribunal));	// takes modifier to +6
		CovenantApplication application = new CovenantApplication(covenant, applicant, -1);
		assertFalse(application.isSuccessful());
		application = new CovenantApplication(covenant, applicant, 3);
		assertTrue(application.isSuccessful());
		assertEquals(applicant.getInventoryOf(AMU.sampleVisSource).size(), 1);
		assertEquals(application.getNetValueToApplicant(), 60);
		application.acceptApplication();
		assertTrue(applicant.getCovenant() == covenant);
		assertEquals(applicant.getInventoryOf(AMU.sampleVisSource).size(), 0);
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
		// base modifier is -3
		applicant.addVis(Arts.VIM, 200);
		CovenantApplication application = new CovenantApplication(covenant, applicant, 7);	// therefore 75 pawns of Vis needed
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
		// base modifier is -3
		applicant.addVis(Arts.VIM, 200);
		applicant.addItem(new Summa(Arts.ANIMAL, 5, 5, null));
		applicant.addItem(new VisSource(Arts.AURAM, 2, tribunal));
		applicant.addItem(new VisSource(Arts.MENTEM, 2, tribunal));
		CovenantApplication application = new CovenantApplication(covenant, applicant, 7);	// therefore needs 15 build points worth
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
		founder.setInheritancePolicy(new MagusApprenticeInheritance());
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

}
