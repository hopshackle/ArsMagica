package hopshackle.simulation.arsmagica.test;

import static org.junit.Assert.*;

import org.junit.*;

import hopshackle.simulation.*;
import hopshackle.simulation.arsmagica.*;

public class ApprenticesAndTeaching {

	private Magus parens;
	private Magus apprentice;
	private World w;

	@Before
	public void setup() {
		SimProperties.setProperty("StartTemperature", "0.0");
		w = new World();
		w.setCalendar(new FastCalendar(800 * 52));
		Tribunal tribunal = new Tribunal("test", w);
		tribunal.setPopulationLevel(200);	// rich in apprentices
		tribunal.setVisLevel(0);	// poor in vis
		parens = new Magus(w);
		parens.addSpell(new Spell(Arts.CREO, Arts.CORPUS, 20, "Chirurgeons touch", parens));
		parens.addSpell(new Spell(Arts.CREO, Arts.CORPUS, 10, "Blah", parens));
		parens.addSpell(new Spell(Arts.REGO, Arts.CORPUS, 25, "Levitation", parens));
		parens.addSpell(new Spell(Arts.CREO, Arts.AQUAM, 5, "A quick dram", parens));
		parens.addXP(Abilities.AREA_LORE, 75);
		parens.setPerception(3);
		apprentice = new Magus(w);
		parens.addXP(Abilities.MAGIC_THEORY, 75);
	}

	@Test
	public void actionsIndicateApprenticeInvolved() {
		assertFalse((new SearchForVis(parens)).requiresApprentice());
		assertFalse((new StudyFromVis(parens, Arts.CREO)).requiresApprentice());
		assertFalse((new PractiseAbility(parens, Abilities.MAGIC_THEORY)).requiresApprentice());
		assertTrue((new DistillVis(parens)).requiresApprentice());
	}
	
	@Test
	public void onlyOneApprentice() {
		assertFalse(MagusActions.SEARCH_APPRENTICE.isChooseable(parens));
		for (Arts art : Arts.values())
			parens.addXP(art, 28);
		assertTrue(MagusActions.SEARCH_APPRENTICE.isChooseable(parens));
		parens.addApprentice(apprentice);
		assertFalse(MagusActions.SEARCH_APPRENTICE.isChooseable(parens));
	}

	@Test
	public void apprenticeAddsToLabTotal() {
		parens.addXP(Arts.CREO, 15);
		int labTotalBase = parens.getLabTotal(Arts.CREO, Arts.ANIMAL);
		parens.addApprentice(apprentice);
		int newLabTotal = parens.getLabTotal(Arts.CREO, Arts.ANIMAL);
		int apprenticeEffect = apprentice.getIntelligence() + apprentice.getLevelOf(Abilities.MAGIC_THEORY);
		assertEquals(labTotalBase + apprenticeEffect, newLabTotal);
	}

	@Test
	public void searchForApprenticeAddsApprenticeAndParensRelationship() {
		SearchForApprentice search = new SearchForApprentice(parens);
		assertFalse(apprentice.isApprentice());
		assertFalse(parens.hasApprentice());
		search.run();
		apprentice = parens.getApprentice();
		assertTrue(apprentice.isApprentice());
		assertTrue(parens.hasApprentice());
		assertTrue(apprentice.getParens() == parens);
		assertTrue(apprentice.getTribunal() != null);
		assertTrue(apprentice.getTribunal() == parens.getTribunal());
	}

	@Test
	public void whenParensTakesNonApprenticeActionApprenticeTakesTheirOwnAction() {
		parens.addApprentice(apprentice);
		parens.setDecider(new HardCodedDecider(MagusActions.SEARCH_VIS));
		apprentice.setDecider(new HardCodedDecider(MagusActions.PRACTISE_ABILITY));
		ArsMagicaAction parensAction = new SearchForVis(parens);
		parensAction.run();
		
		parensAction = (ArsMagicaAction) parens.getNextAction();
		assertFalse(parensAction.requiresApprentice());
		
		Action apprenticeAction = apprentice.getNextAction();
		assertTrue(apprenticeAction == null);
		
		apprentice.decide().run();
		apprenticeAction = apprentice.getNextAction();
		assertTrue(apprenticeAction instanceof PractiseAbility);	// as there are no other ones in this context doable by an apprentice

		parensAction.run();
		assertEquals(parens.getActionQueue().size(), 1); 	// next action queued
		assertEquals(apprentice.getActionQueue().size(), 1); // apprentice still has previous action, no new one has been added
		Action nextApprenticeAction = apprentice.getNextAction();
		assertTrue(apprenticeAction == nextApprenticeAction);
		apprenticeAction.run();
		assertEquals(parens.getActionQueue().size(), 1); 	// next action queued
		assertEquals(apprentice.getActionQueue().size(), 1); // ditto
		parens.getNextAction().run();
		assertEquals(parens.getActionQueue().size(), 1); 	// next action queued
		assertEquals(apprentice.getActionQueue().size(), 1); // no change
		assertFalse(nextApprenticeAction == apprentice.getNextAction());
	}

	@Test
	public void whenParensTakesLabActionApprenticeTakesAssistParensAction() {
		parens.addApprentice(apprentice);
		parens.setMagicAura(2);
		parens.addXP(Arts.CREO, 100);
		parens.setDecider(new HardCodedDecider(MagusActions.DISTILL_VIS));
		ArsMagicaAction parensAction = new SearchForVis(parens);
		parensAction.run();

		parensAction = (ArsMagicaAction) parens.getNextAction();
		assertTrue(parensAction.requiresApprentice());
		Action apprenticeAction = apprentice.getNextAction();
		assertTrue(apprenticeAction != null);
		assertTrue(apprenticeAction instanceof LabAssistant);	// as there are no other ones in this context doable by an apprentice

		apprenticeAction.run();
		assertEquals(parens.getActionQueue().size(), 1); 	// next action still queued (the one requiring assistance)
		assertTrue(parens.getNextAction() == parensAction);
		assertEquals(apprentice.getActionQueue().size(), 1); // apprentice still makes a decision - but it may not be executed
		assertFalse(apprentice.getNextAction() instanceof LabAssistant);

		parensAction.run();
		assertEquals(parens.getActionQueue().size(), 1); 	// next action queued
		assertTrue(parens.getNextAction() != parensAction);
		assertEquals(apprentice.getActionQueue().size(), 1); // decision should now have been overridden
		assertTrue(apprentice.getNextAction() instanceof LabAssistant);
	}

	@Test
	public void eachWinterSeasonParensTakesTeachApprenticeAction() {
		parens.addApprentice(apprentice);
		ArsMagicaAction parensAction = new SearchForVis(parens);
		parensAction.run();
		// calendar starts at Winter
		assertEquals(w.getSeason(), 0);
		parensAction = (ArsMagicaAction) parens.getNextAction();
		if (!(parensAction instanceof TeachApprentice))
			System.out.println(parensAction);
		if (!(parensAction instanceof TeachApprentice))
			System.out.println(parensAction + " instead of TeachApprentice");
		assertTrue(parensAction instanceof TeachApprentice);
		assertTrue(apprentice.getNextAction() instanceof BeTaught);
	}

	@Test
	public void afterFifteenYearsApprenticeBecomeMagusAndTakesOwnActionsIndependently() {
		parens.addApprentice(apprentice);
		provide15SeasonsOfTraining(parens);
		w.setCurrentTime((long) (815 * 52 - 2)); // i.e. two weeks short of 15 years
		parens.maintenance();
		assertTrue(parens.hasApprentice());
		assertTrue(apprentice.isApprentice());
		w.setCurrentTime((long) (815 * 52 + 1)); // i.e. two weeks short of 15 years
		parens.maintenance();
		assertFalse(parens.hasApprentice());
		assertFalse(apprentice.isApprentice());
		
		ArsMagicaAction parensAction = (ArsMagicaAction) parens.decide();
		Action apprenticeAction = apprentice.getNextAction();
		
		assertEquals(parens.getActionQueue().size(), 0); 	// no action queues
		assertFalse(apprenticeAction == null);
		assertEquals(apprentice.getActionQueue().size(), 1); // action queued with end of apprenticeship

		parensAction.run();
		assertEquals(parens.getActionQueue().size(), 1); 	// next action queued
		assertEquals(apprentice.getActionQueue().size(), 1); // no impact on apprentice
	}
	
	@Test
	public void ifParensDiesThenApprenticeIsLeftOnTheirOwn() {
		parens.addApprentice(apprentice);
		parens.die("Oops");
		assertFalse(parens.hasApprentice());
		assertFalse(apprentice.isApprentice());
		assertEquals(apprentice.getActionQueue().size(), 1); // action queued with end of apprenticeship
	}
	
	@Test
	public void ifParensDiesWithHeirsThenApprenticeDoesNotInheritAndIsGivenToEldestWithoutTheirOwnApprentice() {
		Magus earlierApprentice1 = addApprenticeAndMoveForward16Years(parens);
		provide15SeasonsOfTraining(parens);
		Magus earlierApprentice2 = addApprenticeAndMoveForward16Years(parens);
		provide15SeasonsOfTraining(parens);
		Magus earlierApprentice3 = addApprenticeAndMoveForward16Years(parens);
		provide15SeasonsOfTraining(parens);
		Magus apprenticeOfApprentice = new Magus(w);
		earlierApprentice1.addApprentice(apprenticeOfApprentice);
		
		parens.addVis(Arts.REGO, 12);
		parens.addApprentice(apprentice);
		assertEquals(parens.getPawnsOf(Arts.REGO), 12);
		assertEquals(earlierApprentice1.getPawnsOf(Arts.REGO), 0);
		assertEquals(earlierApprentice2.getPawnsOf(Arts.REGO), 0);
		assertEquals(apprentice.getPawnsOf(Arts.REGO), 0);
		parens.die("Ooops");
		
		assertFalse(parens.hasApprentice());
		assertEquals(parens.getPawnsOf(Arts.REGO), 0);
		assertEquals(earlierApprentice1.getPawnsOf(Arts.REGO), 4);
		assertEquals(earlierApprentice2.getPawnsOf(Arts.REGO), 4);
		assertEquals(apprentice.getPawnsOf(Arts.REGO), 0);
		
		assertTrue(apprentice.isApprentice());
		assertTrue(earlierApprentice2.hasApprentice());
		assertTrue(earlierApprentice2.getApprentice() == apprentice);
		assertTrue(apprentice.getParens() == earlierApprentice2);
		
		assertFalse(earlierApprentice3.hasApprentice());
	}
	
	
	@Test
	public void teachingGivesExpectedNumberOfXP() {
		parens.addApprentice(apprentice);
		parens.addXP(Abilities.LATIN, 15);
		parens.addXP(Abilities.TEACHING, 5);
		int xpGain = parens.getCommunication() + 10;
		ArsMagicaAction teaching = new TeachApprentice(parens);
		assertEquals(apprentice.getTotalXPIn(Abilities.LATIN), 0);
		assertEquals(parens.getTotalXPIn(Abilities.TEACHING), 5);
		teaching.run();
		assertEquals(apprentice.getTotalXPIn(Abilities.LATIN), xpGain);
		assertEquals(parens.getTotalXPIn(Abilities.TEACHING), 7);
	}
	
	@Test
	public void teachingDoesNotExceedTeachersXP() {
		parens.addApprentice(apprentice);
		parens.addXP(Abilities.LATIN, 5);
		ArsMagicaAction teaching = new TeachApprentice(parens);
		teaching.run();
		assertEquals(apprentice.getTotalXPIn(Abilities.LATIN), 5);
		assertEquals(parens.getTotalXPIn(Abilities.TEACHING), 2);
	}

	@Test
	public void apprenticeHasAccesstoParensLibrary() {
		apprentice.addXP(Abilities.LATIN, 50);
		apprentice.addXP(Abilities.ARTES_LIBERALES, 5);
		assertEquals(apprentice.getLevelOf(Arts.CREO), 0);
		assertTrue(apprentice.getBestBookToRead() == null);
		parens.addApprentice(apprentice);
		parens.addItem(new Summa(Arts.CREO, 5, 15, null));
		assertTrue(apprentice.getBestBookToRead() != null);
		apprentice.setDecider(new HardCodedDecider(MagusActions.READ_BOOK));
		Action action = apprentice.decide();
		assertTrue(action instanceof ReadBook);
		action.run();
		assertEquals(apprentice.getLevelOf(Arts.CREO), 5);
	}
	
	@Test
	public void apprenticeshipEndsAfter15SeasonsOfTeachingAswellAsElapsedYears() {
		Magus earlierApprentice = addApprenticeAndMoveForward16Years(parens);
		assertTrue(earlierApprentice.isApprentice());
		assertTrue(parens.hasApprentice());
		for (int i = 0; i < 15; i++) {
			assertTrue(earlierApprentice.isApprentice());
			new TeachApprentice(parens).run();
			parens.maintenance();
		}
		assertFalse(earlierApprentice.isApprentice());
	}
	
	@Test
	public void spellsAreTaughtIfConditionsApply() {
		parens.addApprentice(apprentice);
		apprentice.setIntelligence(2);
		apprentice.addXP(Abilities.MAGIC_THEORY, 30);
		parens.addXP(Arts.CREO, 55);
		parens.addXP(Arts.CORPUS, 55);
		parens.setIntelligence(3);
		parens.addXP(Abilities.MAGIC_THEORY, 50);
		apprentice.addXP(Arts.CREO, 15);
		apprentice.addXP(Arts.CORPUS, 15);
		apprentice.addXP(Arts.PERDO, 55);
		apprentice.addXP(Arts.HERBAM, 55);
		// apprentice now has lab total of 15 in CrCo, 10 in Cr?? and 10 in ??Co
		new TeachApprentice(parens).run();
		assertEquals(apprentice.getSpells().size(), 2);
		assertEquals(apprentice.getTotalSpellLevels(), 15);
	}
	
	@Test
	public void ifParensGoesIntoTwilightThenApprenticeCarriesOnTakingActions() {
		parens.addApprentice(apprentice);
		assertEquals(apprentice.getActionQueue().size(), 0);
		assertEquals(parens.getActionQueue().size(), 0);
		Action twilight = new InTwilight(parens, 4);
		assertEquals(parens.getActionQueue().size(), 0);
		parens.addAction(twilight);
		assertEquals(parens.getActionQueue().size(), 1);
		twilight.run();
		assertEquals(parens.getActionQueue().size(), 1);
		assertTrue(parens.isInTwilight());
		assertEquals(apprentice.getActionQueue().size(), 0);
		apprentice.decide().run();
		assertEquals(apprentice.getActionQueue().size(), 1);
		parens.getNextAction().run();
		assertTrue(parens.isInTwilight());
		assertEquals(apprentice.getActionQueue().size(), 1);
	}
	
	
	private Magus addApprenticeAndMoveForward16Years(Magus parens) {
		Magus earlierApprentice = new Magus(parens.getLocation(), new BasicDecider(), parens.getWorld());
		parens.addApprentice(earlierApprentice);
		w.setCurrentTime(w.getCurrentTime() + 16 * 52);
		parens.maintenance();
		return earlierApprentice;
	}
	
	private void provide15SeasonsOfTraining(Magus parens) {
		for (int i = 0; i < 15; i++) {
			assertTrue(parens.hasApprentice());
			new TeachApprentice(parens).run();
		}
		parens.purgeActions();
		apprentice.purgeActions();
		parens.maintenance();
	}
	
}
