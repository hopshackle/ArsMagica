package hopshackle.simulation.arsmagica.test;

import static org.junit.Assert.*;

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
		World world = new World();
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
		assertEquals(magus.getPawnsOf(Arts.CREO), 10);
		assertEquals(magus.getLevelOf(Arts.CREO), 0);
		StudyFromVis studyCreo = new StudyFromVis(magus, Arts.CREO);
		studyCreo.setDieRoll(6);
		studyCreo.run();
		assertEquals(magus.getPawnsOf(Arts.CREO), 9);
		assertEquals(magus.getLevelOf(Arts.CREO), 3);

		magus.setMagicAura(2);
		studyCreo = new StudyFromVis(magus, Arts.CREO);
		studyCreo.setDieRoll(7);
		studyCreo.run();
		assertEquals(magus.getPawnsOf(Arts.CREO), 8);
		assertEquals(magus.getLevelOf(Arts.CREO), 5);
		magus.setMagicAura(0);

		studyCreo = new StudyFromVis(magus, Arts.CREO);
		studyCreo.setDieRoll(9);
		studyCreo.run();
		assertEquals(magus.getPawnsOf(Arts.CREO), 7);
		assertEquals(magus.getLevelOf(Arts.CREO), 6);

		studyCreo = new StudyFromVis(magus, Arts.CREO);
		studyCreo.run();
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
		search.run();
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
		StudyFromVis study = new StudyFromVis(magus, Arts.HERBAM);
		study.setDieRoll(0, 1);
		study.run();
		assertEquals(magus.getTotalXPIn(Abilities.WARPING), 1);
		assertEquals(magus.getTotalXPIn(Arts.HERBAM), 0);
		assertEquals(magus.getPawnsOf(Arts.HERBAM), 4);
		study.setDieRoll(0, 2);
		study.run();
		int warpingPoints = magus.getTotalXPIn(Abilities.WARPING);
		assertTrue(warpingPoints >= 3);
		assertEquals(magus.getTotalXPIn(Arts.HERBAM), 0);
		assertEquals(magus.getPawnsOf(Arts.HERBAM), 3);
		study.setDieRoll(0, 0);
		study.run();
		assertEquals(magus.getTotalXPIn(Abilities.WARPING), warpingPoints);
		assertEquals(magus.getTotalXPIn(Arts.HERBAM), 2);
		assertEquals(magus.getPawnsOf(Arts.HERBAM), 2);
		assertFalse(magus.isDead());
	}

	@Test
	public void twilightMayOccurOnADoubleBotch() {
		magus.setStamina(2);	// to offset double botch, and give 50:50 chance of avoiding twilight
		magus.addVis(Arts.HERBAM, 50);
		StudyFromVis study = new StudyFromVis(magus, Arts.HERBAM);
		for (int i = 0; i < 20; i++) {
			study.setDieRoll(0, 2);
			study.run();
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
		study.run();
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
		study.run();
		assertFalse(magus.isDead());
		assertEquals(magus.getActionQueue().size(), 1);
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

}
