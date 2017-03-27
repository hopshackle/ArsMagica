package hopshackle.simulation.arsmagica.test;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;

import hopshackle.simulation.*;
import hopshackle.simulation.arsmagica.*;

public class AgeingAndDecrepitude {

	private Magus magus;
	private World w;
	private AgeingEvent ae;

	@Before
	public void setup() {
		w = new World(new SimpleWorldLogic<Magus>(new ArrayList<ActionEnum<Magus>>(EnumSet.allOf(MagusActions.class))));
		new Tribunal("test", w);
		w.setCalendar(new FastCalendar(0));
		magus = new Magus(w);
		ae = new AgeingEvent(magus);
	}

	@Test
	public void ageingCausesDecrepitudeGain() {
		assertEquals((long) w.getCurrentTime(), 0);
		assertEquals(magus.getBirth(), 0);
		assertEquals(magus.getAge(), 0);
		magus.setAge(50);
		assertEquals(magus.getAge(), 50);
		ae.gainAgeingPoint();
		assertEquals(magus.getTotalXPIn(Abilities.DECREPITUDE), 1);
		ae.gainAgeingPoint(AttributeTypes.COMMUNICATION);
		assertEquals(magus.getTotalXPIn(Abilities.DECREPITUDE), 2);
	}

	@Test
	public void crisisIncreasesDecrepitudeScore() {
		magus.addXP(Abilities.DECREPITUDE, 7);
		ae.ageToCrisis();
		assertEquals(magus.getLevelOf(Abilities.DECREPITUDE), 2);
		assertEquals(magus.getTotalXPIn(Abilities.DECREPITUDE), 15);
	}

	@Test
	public void ageingCausesCharacteristicDecline() {;
	magus.setCommunication(0);
	ae.gainAgeingPoint(AttributeTypes.COMMUNICATION);
	assertEquals(magus.getCommunication(), -1);
	assertEquals(magus.getCommunicationAMC().getAgeingPoints(), 0);
	ae.gainAgeingPoint(AttributeTypes.COMMUNICATION);
	assertEquals(magus.getCommunication(), -1);
	assertEquals(magus.getCommunicationAMC().getAgeingPoints(), 1);
	}

	@Test
	public void ageingModifierIncludesLongevityRitualCorrectly() {
		assertEquals(magus.getAge(), 0);
		assertEquals(magus.getLongevityModifier(), 0);
		ArsMagicaAction a = new FoundCovenant(HopshackleUtilities.listFromInstance(magus));
		magus.setDecider(new HardCodedDecider<Magus>(MagusActions.LONGEVITY_RITUAL));
		addStartAndRunAction(a);
		Covenant covenant = magus.getCovenant();
		covenant.addXP(CovenantAttributes.WEALTH, 5);
		assertEquals(magus.getLongevityModifier(), 1);
		covenant.addXP(CovenantAttributes.WEALTH, 50);
		assertEquals(magus.getLongevityModifier(), 2);
		magus.setAge(50);
		assertEquals(magus.getLongevityModifier(), -3);
		magus.addAge(1);
		assertEquals(magus.getLongevityModifier(), -4);
		magus.addXP(Arts.CREO, 15);
		magus.addXP(Arts.CORPUS, 15);
		magus.setIntelligence(1);
		covenant.setAuraAndCapacity(2, 10);
		magus.addVis(Arts.VIM, 13);
		
		runNextAction(magus);
		assertEquals(magus.getPawnsOf(Arts.VIM), 7);
		assertEquals(magus.getLongevityModifier(), -1);
		magus.addXP(Abilities.MAGIC_THEORY, 30);

		runNextAction(magus);
		assertEquals(magus.getPawnsOf(Arts.VIM), 1);
		assertEquals(magus.getLongevityModifier(), 0);
	}

	@Test
	public void longevityRitualChooseableOnlyIfNoneCurrentlyInPlace() {
		magus.setAge(33);
		magus.addXP(Arts.CREO, 15);
		magus.addXP(Arts.CORPUS, 15);
		magus.setIntelligence(1);
		magus.setMagicAura(2);
		assertEquals(magus.getLabTotal(Arts.CREO, Arts.CORPUS), 13);
		assertFalse(MagusActions.LONGEVITY_RITUAL.isChooseable(magus));
		magus.setAge(34);
		assertFalse(MagusActions.LONGEVITY_RITUAL.isChooseable(magus));
		// now add vis
		magus.addVis(Arts.VIM, 10);
		assertTrue(MagusActions.LONGEVITY_RITUAL.isChooseable(magus));
		addStartAndRunAction(new InventLongevityRitual(magus));
		assertFalse(MagusActions.LONGEVITY_RITUAL.isChooseable(magus));
	}

	@Test
	public void longevityRitualRequiresSufficientVis() {
		magus.setAge(29);
		assertFalse(InventLongevityRitual.hasSufficientVis(magus));
		magus.addVis(Arts.CORPUS, 1);
		assertFalse(InventLongevityRitual.hasSufficientVis(magus));
		magus.addVis(Arts.CREO, 2);
		assertTrue(InventLongevityRitual.hasSufficientVis(magus));
	}

	@Test
	public void longevityRitualServiceIsFoundInInventory() {
		magus.addItem(new LongevityRitualService(magus));
		assertEquals(magus.getNumberInInventoryOf(AMU.sampleLongevityRitualService), 1);
	}

	@Test
	public void longevityRitualUsesUpVisCorrectly() {
		magus.setAge(29);
		magus.addXP(Arts.CREO, 15);
		magus.addXP(Arts.CORPUS, 15);
		magus.setIntelligence(1);
		magus.setMagicAura(2);
		magus.addVis(Arts.CREO, 3);
		magus.addVis(Arts.CORPUS, 1);
		magus.addVis(Arts.VIM, 3);
		InventLongevityRitual ritual = new InventLongevityRitual(magus);
		addStartAndRunAction(ritual);
		assertEquals(magus.getPawnsOf(Arts.CREO), 2);
		assertEquals(magus.getPawnsOf(Arts.CORPUS), 1);
		assertEquals(magus.getPawnsOf(Arts.VIM), 1);
	}

	@Test
	public void longevityRitualExpiresAfterAgeingCrisis() {
		magus.addXP(Arts.CREO, 15);
		magus.addXP(Arts.CORPUS, 15);
		magus.setIntelligence(1);
		magus.setMagicAura(2);
		magus.addVis(Arts.VIM, 3);
		addStartAndRunAction(new InventLongevityRitual(magus));
		assertEquals(magus.getLongevityRitualEffect(), 3);
		ae.ageToCrisis();
		assertEquals(magus.getLongevityRitualEffect(), 0);
	}

	@Test
	public void gainsWarpingPointsEachYearFromLongevityRitual() {
		assertEquals(magus.getTotalXPIn(Abilities.WARPING), 0);
		ae.ageOneYear();
		assertEquals(magus.getTotalXPIn(Abilities.WARPING), 0);
		magus.addXP(Arts.CREO, 28);
		magus.addVis(Arts.CREO, 3);
		addStartAndRunAction(new InventLongevityRitual(magus));
		ae.ageOneYear();
		assertEquals(magus.getTotalXPIn(Abilities.WARPING), 1);
	}

	private void addStartAndRunAction(ArsMagicaAction a) {
		a.getActor().getActionPlan().addAction(a);
		a.start();
		a.run();
	}
	private void runNextAction(Magus m) {
		Action<?> a = m.getActionPlan().getNextAction();
		a.start();
		a.run();
	}
}
