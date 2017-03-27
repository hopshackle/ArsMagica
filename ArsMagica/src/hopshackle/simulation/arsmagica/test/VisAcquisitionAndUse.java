package hopshackle.simulation.arsmagica.test;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;

import hopshackle.simulation.*;
import hopshackle.simulation.arsmagica.*;

public class VisAcquisitionAndUse {

	private Magus magus;
	private Vis creoVis, vimVis;
	private static VisSource sampleVisSource = new VisSource(Arts.VIM, 1, null);

	@Before
	public void setUp() {
		SimProperties.setProperty("MagusUniformResearchPreferences", "true");
		World world = new World(new SimpleWorldLogic<Magus>(new ArrayList<ActionEnum<Magus>>(EnumSet.allOf(MagusActions.class))));
		magus = new Magus(world);
		magus.addXP(Abilities.MAGIC_THEORY, 15);
		creoVis = new Vis(Arts.CREO);
		vimVis = new Vis(Arts.VIM);
		Tribunal tribunal = new Tribunal("Vis rich", world);
		tribunal.setVisLevel(200);
		magus.setTribunal(tribunal);
	}

	@Test
	public void GainPawnOfVisInInventory() {
		magus.addItem(creoVis);
		assertEquals(magus.getNumberInInventoryOf(creoVis), 1);
		assertEquals(magus.getNumberInInventoryOf(vimVis), 1);
		// All Vis pawns are regarded as being of a single type for inventory purposes
		// But adding and removing items from inventory requires Object identity
		magus.removeItem(creoVis);
		assertEquals(magus.getNumberInInventoryOf(creoVis), 0);
		assertEquals(magus.getNumberInInventoryOf(vimVis), 0);
	}

	@Test
	public void VisInventoryGivesCorrectResults() {
		magus.addVis(Arts.CREO, 10);
		magus.addVis(Arts.VIM, 4);
		assertEquals(magus.getPawnsOf(Arts.CREO), 10);
		assertEquals(magus.getPawnsOf(Arts.VIM), 4);
		assertEquals(magus.getPawnsOf(Arts.REGO), 0);
		magus.removeVis(Arts.VIM, 2);
		magus.removeVis(Arts.CREO, 3);
		assertEquals(magus.getPawnsOf(Arts.CREO), 7);
		assertEquals(magus.getPawnsOf(Arts.VIM), 2);
		assertEquals(magus.getPawnsOf(Arts.REGO), 0);
	}

	@Test
	public void StudyingFromVisUsesItUpAndIncreasesArtTakingMagicAuraIntoAccount() {
		magus.addVis(Arts.CREO, 10);
		magus.setDecider(new HardCodedDecider<Magus>(MagusActions.STUDY_VIS));
		assertEquals(magus.getPawnsOf(Arts.CREO), 10);
		assertEquals(magus.getLevelOf(Arts.CREO), 0);
		StudyFromVis studyCreo = new StudyFromVis(magus, Arts.CREO);
		studyCreo.setDieRoll(6);
		addStartAndRunAction(studyCreo);
		assertEquals(magus.getPawnsOf(Arts.CREO), 9);
		assertEquals(magus.getLevelOf(Arts.CREO), 3);

		magus.setMagicAura(2);
		studyCreo = (StudyFromVis) magus.getNextAction();
		studyCreo.setDieRoll(7);
		runNextAction(magus);
		assertEquals(magus.getPawnsOf(Arts.CREO), 8);
		assertEquals(magus.getLevelOf(Arts.CREO), 5);
		magus.setMagicAura(0);

		studyCreo = (StudyFromVis) magus.getNextAction();
		studyCreo.setDieRoll(9);
		runNextAction(magus);
		assertEquals(magus.getPawnsOf(Arts.CREO), 7);
		assertEquals(magus.getLevelOf(Arts.CREO), 6);

		studyCreo = (StudyFromVis) magus.getNextAction();
		runNextAction(magus);
		assertEquals(magus.getPawnsOf(Arts.CREO), 5);
	}

	@Test
	public void withChoiceOfVisStudyOneWithGreatestPercentageIncreaseInXP() {
		magus.addVis(Arts.CREO, 10);
		magus.addVis(Arts.MUTO, 10);
		magus.addXP(Arts.CREO, 5);
		assertTrue(magus.getTypeOfVisToStudy() == Arts.MUTO);

		magus.addXP(Arts.MUTO, 10);
		assertTrue(magus.getTypeOfVisToStudy() == Arts.CREO);

		magus.addVis(Arts.REGO, 1);
		assertTrue(magus.getTypeOfVisToStudy() == Arts.REGO);
	}

	@Test
	public void choiceOfVisTakesResearchPreferencesIntoAccount() {
		MagusPreferences.setResearchPreference(magus, Arts.MUTO, 0.6);
		MagusPreferences.setResearchPreference(magus, Arts.CREO, 1.0);
		magus.addVis(Arts.CREO, 10);
		magus.addVis(Arts.MUTO, 10);
		magus.addXP(Arts.CREO, 5);
		// base is 0.1 for no XP, and 0.0667 for 5 xp
		assertTrue(magus.getTypeOfVisToStudy() == Arts.CREO);

		magus.addXP(Arts.CREO, 5);
		assertTrue(magus.getTypeOfVisToStudy() == Arts.MUTO);

		magus.addXP(Arts.MUTO, 10);
		assertTrue(magus.getTypeOfVisToStudy() == Arts.CREO);
	}

	@Test
	public void SearchingForVisProvidesVisAndVisSource() {
		SearchForVis search = new SearchForVis(magus);
		addStartAndRunAction(search);
		assertEquals(magus.getNumberInInventoryOf(sampleVisSource), 1);

		int pawnsOfVis = magus.getNumberInInventoryOf(creoVis);
		assertTrue(pawnsOfVis > 0);
		int visSources = magus.getNumberInInventoryOf(sampleVisSource);
		assertEquals(visSources, 1);
		VisSource vs = magus.getInventoryOf(sampleVisSource).get(0);
		assertEquals(pawnsOfVis, vs.getAmountPerAnnum() * 2);
	}


	@Test
	public void botchAddsWarpingPointsAndNoXPInArt() {
		magus.setMagicAura(2);
		magus.addVis(Arts.HERBAM, 5);
		magus.addXP(Abilities.MAGIC_THEORY, 200);
		magus.setDecider(new HardCodedDecider<Magus>(MagusActions.STUDY_VIS));
		StudyFromVis study = new StudyFromVis(magus, Arts.HERBAM);
		study.setDieRoll(0, 1);
		addStartAndRunAction(study);
		assertEquals(magus.getTotalXPIn(Abilities.WARPING), 1);
		assertEquals(magus.getTotalXPIn(Arts.HERBAM), 0);
		assertEquals(magus.getPawnsOf(Arts.HERBAM), 4);

		study = (StudyFromVis) magus.getNextAction();
		study.setDieRoll(0, 2);
		runNextAction(magus);
		int warpingPoints = magus.getTotalXPIn(Abilities.WARPING);
		assertTrue(warpingPoints >= 3);
		assertEquals(magus.getTotalXPIn(Arts.HERBAM), 0);
		assertEquals(magus.getPawnsOf(Arts.HERBAM), 3);

		assertTrue(magus.getNextAction() instanceof StudyFromVis);
		study = (StudyFromVis) magus.getNextAction();
		study.setDieRoll(0, 0);
		runNextAction(magus);
		assertEquals(magus.getTotalXPIn(Abilities.WARPING), warpingPoints);
		assertEquals(magus.getTotalXPIn(Arts.HERBAM), 2);
		assertEquals(magus.getPawnsOf(Arts.HERBAM), 2);
		assertFalse(magus.isDead());
	}

	@Test
	public void twilightMayOccurOnADoubleBotch() {
		magus.setStamina(2);	// to offset double botch, and give 50:50 chance of avoiding twilight
		magus.addVis(Arts.HERBAM, 300);
		magus.addXP(Abilities.MAGIC_THEORY, 300);
		magus.setDecider(new HardCodedDecider<Magus>(MagusActions.STUDY_VIS));
		StudyFromVis study = new StudyFromVis(magus, Arts.HERBAM);
		study.addToAllPlans();
		for (int i = 0; i < 20; i++) {
			Action<?> nextAction = magus.getNextAction();
			if (nextAction instanceof StudyFromVis) {
				study = (StudyFromVis) magus.getNextAction();
				study.setDieRoll(0, 2);
			}
			runNextAction(magus);
			magus.addXP(Abilities.WARPING, -10);
		}
		int twilightScars = magus.getTwilightScars(true) + magus.getTwilightScars(false);
		assertEquals(twilightScars, 10, 6);
		assertFalse(magus.isDead());
	}

	@Test
	public void finalTwilightKillsMagus() {
		magus.setStamina(0);
		magus.setIntelligence(0);
		magus.addVis(Arts.HERBAM, 50);
		magus.addXP(Abilities.WARPING, Abilities.WARPING.getXPForLevel(10));
		StudyFromVis study = new StudyFromVis(magus, Arts.HERBAM);
		study.setDieRoll(0, 2);
		addStartAndRunAction(study);
		assertTrue(magus.isDead());
	}

	@Test
	public void twilightTakesMagusOutForRequisiteNumberOfSeasons() {
		magus.setMagicAura(6);
		magus.setStamina(0);
		magus.setIntelligence(0);
		magus.addVis(Arts.HERBAM, 50);
		magus.addXP(Abilities.WARPING, Abilities.WARPING.getXPForLevel(7));
		StudyFromVis study = new StudyFromVis(magus, Arts.HERBAM);
		study.setDieRoll(0, 2);
		addStartAndRunAction(study);
		assertFalse(magus.isDead());
		assertEquals(magus.getActionPlan().timeToEndOfQueue(), 13);
		assertTrue(magus.getNextAction() instanceof InTwilight);
	}

	@Test
	public void twilightModifierCalculatedCorrectly() {
		magus.setStamina(0);
		assertEquals(TwilightEpisode.getTwilightAvoidanceModifier(magus), 0);
		magus.setStamina(2);
		assertEquals(TwilightEpisode.getTwilightAvoidanceModifier(magus), 2);
		magus.setMagicAura(3);
		assertEquals(TwilightEpisode.getTwilightAvoidanceModifier(magus), -1);
		magus.addXP(Abilities.CONCENTRATION, 5);
		assertEquals(TwilightEpisode.getTwilightAvoidanceModifier(magus), 0);
		magus.addXP(Arts.VIM, 4);
		assertEquals(TwilightEpisode.getTwilightAvoidanceModifier(magus), 0);
		magus.addXP(Arts.VIM, 11);
		assertEquals(TwilightEpisode.getTwilightAvoidanceModifier(magus), 1);
		magus.addXP(Abilities.WARPING, 5);
		assertEquals(TwilightEpisode.getTwilightAvoidanceModifier(magus), 0);
	}

	@Test
	public void twilightUnderstandingModifierCalculatedCorrectly() {
		magus.setIntelligence(0);
		assertEquals(TwilightEpisode.getTwilightComprehensionModifier(magus), 0);
		magus.setIntelligence(2);
		assertEquals(TwilightEpisode.getTwilightComprehensionModifier(magus), 2);
		magus.setMagicAura(3);
		assertEquals(TwilightEpisode.getTwilightComprehensionModifier(magus), 2);
		magus.addXP(Abilities.WARPING, 5);
		assertEquals(TwilightEpisode.getTwilightComprehensionModifier(magus), 1);
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
