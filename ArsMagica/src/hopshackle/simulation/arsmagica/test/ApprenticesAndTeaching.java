package hopshackle.simulation.arsmagica.test;

import static org.junit.Assert.*;

import java.util.*;

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
		w = new World(new SimpleWorldLogic<Magus>(new ArrayList<ActionEnum<Magus>>(EnumSet.allOf(MagusActions.class))));
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
		AgentArchive.switchOn(true);
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
		addStartAndRunAction(search);
		apprentice = parens.getApprentice();
		assertTrue(apprentice.isApprentice());
		assertTrue(parens.hasApprentice());
		assertTrue(apprentice.getParens() == parens);
		assertTrue(apprentice.getTribunal() != null);
		assertTrue(apprentice.getTribunal() == parens.getTribunal());
	}

	@Test
	public void whenParensTakesNonApprenticeActionApprenticeTakesTheirOwnAction() {
		apprentice.setDecider(new HardCodedDecider<Magus>(MagusActions.PRACTISE_ABILITY));
		parens.addApprentice(apprentice);
		parens.setDecider(new HardCodedDecider<Magus>(MagusActions.SEARCH_VIS));
		parens.decide();
		runNextAction(parens);
		
		ArsMagicaAction parensAction = (ArsMagicaAction) parens.getNextAction();
		assertTrue(parensAction.getType() == MagusActions.SEARCH_VIS);
		
		ArsMagicaAction apprenticeAction = (ArsMagicaAction) apprentice.getNextAction();
		assertTrue(apprenticeAction != null);
		
		runNextAction(apprentice);
		apprenticeAction = (ArsMagicaAction) apprentice.getNextAction();
		assertTrue(apprenticeAction instanceof PractiseAbility);	// as there are no other ones in this context doable by an apprentice

		addStartAndRunAction(parensAction);		// Parens will search for vis, which does not require an apprentice
		assertEquals(parens.getActionPlan().timeToNextActionStarts(), 0); 	// next action queued
		assertEquals(parens.getActionPlan().timeToEndOfQueue(), 13); 	// one season is 13 weeks, the smallest unit of time
		assertEquals(apprentice.getActionPlan().timeToNextActionStarts(), 0); 	// apprentice still has previous action, no new one has been added
		assertEquals(apprentice.getActionPlan().timeToEndOfQueue(), 13); 	
		ArsMagicaAction nextApprenticeAction = (ArsMagicaAction) apprentice.getNextAction();
		assertTrue(apprenticeAction == nextApprenticeAction);
		addStartAndRunAction(apprenticeAction);
		assertEquals(parens.getActionPlan().timeToNextActionStarts(), 0); 		// no change
		assertEquals(parens.getActionPlan().timeToEndOfQueue(), 13); 			// no change
		assertEquals(apprentice.getActionPlan().timeToNextActionStarts(), 0); 	
		assertEquals(apprentice.getActionPlan().timeToEndOfQueue(), 13); 
		runNextAction(parens);
		assertEquals(parens.getActionPlan().timeToNextActionStarts(), 0); 	
		assertEquals(parens.getActionPlan().timeToEndOfQueue(), 13); 
		assertEquals(apprentice.getActionPlan().timeToNextActionStarts(), 0); 	
		assertEquals(apprentice.getActionPlan().timeToEndOfQueue(), 13); 
		assertFalse(nextApprenticeAction == apprentice.getNextAction());
	}


	@Test
	public void whenParensTakesLabActionApprenticeTakesAssistParensAction() {
		parens.addApprentice(apprentice);
		parens.setMagicAura(2);
		parens.addXP(Arts.CREO, 100);
		parens.setDecider(new HardCodedDecider<Magus>(MagusActions.DISTILL_VIS));
		apprentice.setDecider(new HardCodedDecider<Magus>(MagusActions.PRACTISE_ABILITY));
		
		apprentice.decide();	// choice will later be overridden by parens decision
		ArsMagicaAction apprenticeAction = (ArsMagicaAction) apprentice.getNextAction();
		assertTrue(apprenticeAction.getType() == MagusActions.PRACTISE_ABILITY);
		assertEquals(apprentice.getActionPlan().timeToNextActionStarts(), 0); 
		assertEquals(apprentice.getActionPlan().timeToEndOfQueue(), 13); 
		assertFalse(apprenticeAction.isDeleted());

		ArsMagicaAction parensAction = new SearchForVis(parens);
		addStartAndRunAction(parensAction);		// this will include making the next decision, to distill vis
												// which will cancel the previous apprentice action

		parensAction = (ArsMagicaAction) parens.getNextAction();
		assertTrue(parensAction.getType() == MagusActions.DISTILL_VIS);
		assertTrue(apprenticeAction.isDeleted());
		apprenticeAction = (ArsMagicaAction) apprentice.getNextAction();
		assertTrue(apprenticeAction == parensAction);
		assertEquals(apprentice.getActionPlan().timeToNextActionStarts(), 0); 
		assertEquals(apprentice.getActionPlan().timeToEndOfQueue(), 13); 
		// apprentice is covered by parens queue in this case
		
		assertEquals(w.getCurrentTime().longValue(), 41600);
		runNextAction(parens); 	
		parensAction = (ArsMagicaAction) parens.getNextAction();
		apprenticeAction = (ArsMagicaAction) apprentice.getNextAction();
		assertEquals(parens.getActionPlan().timeToNextActionStarts(), 0); 	
		assertEquals(parens.getActionPlan().timeToEndOfQueue(), 13); 
		assertTrue(apprenticeAction == parensAction);
		assertEquals(apprentice.getActionPlan().timeToNextActionStarts(), 0); 	
		assertEquals(apprentice.getActionPlan().timeToEndOfQueue(), 13); 
	}

	@Test
	public void eachYearParensTakesTeachApprenticeActionFirst() {
		parens.addApprentice(apprentice);
		parens.decide();
		ArsMagicaAction parensAction = (ArsMagicaAction) parens.getNextAction();
		if (!(parensAction instanceof TeachApprentice))
			System.out.println(parensAction + " instead of TeachApprentice");
		assertTrue(parensAction instanceof TeachApprentice);
		assertTrue(apprentice.getNextAction() == parensAction);
		
		runNextAction(parens);
		parensAction = (ArsMagicaAction) parens.getNextAction();
		assertFalse(parensAction instanceof TeachApprentice);
		w.setCurrentTime((long) (801 * 52));
		
		runNextAction(parens);
		parensAction = (ArsMagicaAction) parens.getNextAction();
		assertTrue(parensAction instanceof TeachApprentice);
	}

	@Test
	public void afterFifteenYearsApprenticeBecomeMagusAndTakesOwnActionsIndependently() {
		parens.setDecider(new HardCodedDecider<Magus>(MagusActions.DISTILL_VIS));
		parens.addApprentice(apprentice);
		provide15SeasonsOfTraining(parens);
		w.setCurrentTime((long) (815 * 52 - 2)); // i.e. two weeks short of 15 years
		parens.maintenance();
		assertTrue(parens.hasApprentice());
		assertTrue(apprentice.isApprentice());
		w.setCurrentTime((long) (815 * 52 + 1)); // i.e. one week long of 15 years
		parens.decide();
		runNextAction(parens);
		assertFalse(parens.hasApprentice());
		assertFalse(apprentice.isApprentice());
		
		Action<?> apprenticeAction = apprentice.getNextAction();		
		assertFalse(apprenticeAction == null);
		assertEquals(apprentice.getActionPlan().timeToEndOfQueue(), 13); // action queued with end of apprenticeship

		ArsMagicaAction parensAction = (ArsMagicaAction) parens.getNextAction();
		assertFalse(parensAction == null);
		assertTrue(parensAction != apprenticeAction);
		runNextAction(parens);
		assertEquals(parens.getActionPlan().timeToEndOfQueue(), 13); 	// next action queued
		assertEquals(apprentice.getActionPlan().timeToEndOfQueue(), 13); // no impact on apprentice
		assertTrue(apprentice.getNextAction() == apprenticeAction);
	}
	
	@Test
	public void ifParensDiesThenApprenticeIsLeftOnTheirOwn() {
		parens.addApprentice(apprentice);
		parens.setDecider(new HardCodedDecider<Magus>(MagusActions.DISTILL_VIS));
		parens.decide();
		ArsMagicaAction apprenticeAction = (ArsMagicaAction) apprentice.getNextAction();
		assertTrue(apprenticeAction.getType() == MagusActions.DISTILL_VIS);
		parens.die("Oops");
		assertTrue(apprenticeAction.isDeleted());
		assertFalse(parens.hasApprentice());
		assertFalse(apprentice.isApprentice());
		assertFalse(apprentice.getNextAction() == apprenticeAction);
		assertEquals(apprentice.getActionPlan().sizeOfQueue(), 1);
		assertEquals(apprentice.getActionPlan().timeToEndOfQueue(), 13); // action queued with end of apprenticeship
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

		assertFalse(earlierApprentice1.isApprentice());
		assertFalse(earlierApprentice2.isApprentice());
		assertFalse(earlierApprentice3.isApprentice());

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
		ArsMagicaAction teaching = new TeachApprentice(parens, apprentice);
		assertEquals(apprentice.getTotalXPIn(Abilities.LATIN), 0);
		assertEquals(parens.getTotalXPIn(Abilities.TEACHING), 5);
		addStartAndRunAction(teaching);
		assertEquals(apprentice.getTotalXPIn(Abilities.LATIN), xpGain);
		assertEquals(parens.getTotalXPIn(Abilities.TEACHING), 7);
	}
	
	@Test
	public void teachingDoesNotExceedTeachersXP() {
		parens.addApprentice(apprentice);
		parens.addXP(Abilities.LATIN, 5);
		ArsMagicaAction teaching = new TeachApprentice(parens, apprentice);
		addStartAndRunAction(teaching);
		assertEquals(apprentice.getTotalXPIn(Abilities.LATIN), 5);
		assertEquals(parens.getTotalXPIn(Abilities.TEACHING), 2);
	}

	@Test
	public void apprenticeHasAccesstoParensLibrary() {
		apprentice.addXP(Abilities.LATIN, 50);
		apprentice.addXP(Abilities.ARTES_LIBERALES, 5);
		assertEquals(apprentice.getLevelOf(Arts.CREO), 0);
		assertTrue(apprentice.getBestBookToRead() == null);
		parens.addItem(new Summa(Arts.CREO, 5, 15, null));
		apprentice.setDecider(new HardCodedDecider<Magus>(MagusActions.READ_BOOK));
		parens.addApprentice(apprentice);
		ArsMagicaAction action = (ArsMagicaAction) apprentice.getNextAction();
		assertTrue(action instanceof ReadBook);
		addStartAndRunAction(action);
		assertEquals(apprentice.getLevelOf(Arts.CREO), 5);
	}
	
	@Test
	public void apprenticeshipEndsAfter15SeasonsOfTeachingAswellAsElapsedYears() {
		Magus earlierApprentice = addApprenticeAndMoveForward16Years(parens);
		assertTrue(earlierApprentice.isApprentice());
		assertTrue(parens.hasApprentice());
		provide15SeasonsOfTraining(parens);
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
		addStartAndRunAction(new TeachApprentice(parens, apprentice));
		assertEquals(apprentice.getSpells().size(), 2);
		assertEquals(apprentice.getTotalSpellLevels(), 15);
	}
	
	@Test
	public void ifParensGoesIntoTwilightThenApprenticeCarriesOnTakingActions() {
		parens.addApprentice(apprentice);
		Action<?> apprenticeAction = apprentice.getNextAction();
		assertEquals(apprentice.getActionPlan().timeToEndOfQueue(), 13);
		assertEquals(parens.getActionPlan().timeToEndOfQueue(), 0);
		ArsMagicaAction twilight = new InTwilight(parens, 4);
		assertEquals(parens.getActionPlan().timeToEndOfQueue(), 0);
		parens.getActionPlan().addAction(twilight);
		assertEquals(parens.getActionPlan().timeToEndOfQueue(), 13);
		assertEquals(apprentice.getActionPlan().timeToEndOfQueue(), 13);
		runNextAction(parens);
		assertTrue(apprentice.isApprentice());
		assertEquals(parens.getActionPlan().timeToEndOfQueue(), 13);
		assertTrue(apprentice.getNextAction() == apprenticeAction);
		assertTrue(parens.isInTwilight());
		runNextAction(apprentice);
		assertEquals(apprentice.getActionPlan().timeToEndOfQueue(), 13);
		assertFalse(apprentice.getNextAction() == apprenticeAction);
		apprenticeAction = apprentice.getNextAction();
		runNextAction(parens);
		assertTrue(parens.isInTwilight());
		assertTrue(apprentice.getNextAction() == apprenticeAction);

	}
	
	
	private Magus addApprenticeAndMoveForward16Years(Magus parens) {
		assertFalse(parens.hasApprentice());
		Magus earlierApprentice = new Magus(parens.getLocation(), new BasicDecider(), parens.getWorld());
		parens.addApprentice(earlierApprentice);
		w.setCurrentTime(w.getCurrentTime() + 16 * 52);
		parens.maintenance();
		assertTrue(parens.hasApprentice());
		return earlierApprentice;
	}
	
	private void provide15SeasonsOfTraining(Magus parens) {
		Decider<Magus> oldDecider = parens.getDecider();
		Magus currentApprentice = parens.getApprentice();
		assertEquals(currentApprentice.getSeasonsTraining(), 0);
		parens.setDecider(new HardCodedDecider<Magus>(MagusActions.TEACH_APPRENTICE));
		parens.getActionPlan().purgeActions(true);
		parens.decide();
		for (int i = 0; i < 15; i++) {
			assertTrue(parens.hasApprentice());
			runNextAction(parens);
		}
		assertEquals(currentApprentice.getSeasonsTraining(), 15);
		parens.setDecider(oldDecider);
		parens.getActionPlan().purgeActions(true);
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
