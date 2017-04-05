package hopshackle.simulation.arsmagica.test;

import hopshackle.simulation.*;
import hopshackle.simulation.arsmagica.*;

import java.util.*;

import org.junit.*;

import static org.junit.Assert.*;

public class MagusCoordinationTests {

	private Magus seller, buyer, sApprentice, bApprentice;
	private World w;
	LongevityRitualService service;

	@Before
	public void setup() {
		w = new World(new SimpleWorldLogic<Magus>(new ArrayList<ActionEnum<Magus>>(EnumSet.allOf(MagusActions.class))));
		new Tribunal("test", w);
		w.setCalendar(new FastCalendar(0));
		seller = new Magus(w);
		buyer = new Magus(w);
		sApprentice = new Magus(w);
		bApprentice = new Magus(w);
		seller.addApprentice(sApprentice);
		sApprentice.purgeActions(true);
		bApprentice.purgeActions(true);
		buyer.addApprentice(bApprentice);
		buyer.setIntelligence(2);
		seller.setIntelligence(1);
		sApprentice.setIntelligence(2);
		bApprentice.setIntelligence(0);
		buyer.setAge(40);
		buyer.addVis(Arts.CREO, 20);
		seller.addXP(Arts.CREO, 100);
		seller.addXP(Arts.CORPUS, 100);
		seller.addXP(Abilities.MAGIC_THEORY, 100);
		service = new LongevityRitualService(seller);
		buyer.addItem(service);
		seller.setDecider(new HardCodedDecider<Magus>(MagusActions.PRACTISE_ABILITY));
	}

	
	@Test
	public void basicLongevityRitualCoordWithOneApprentice() {
		seller.addXP(Abilities.LEADERSHIP, 5);

		buyer.maintenance();
		assertTrue(buyer.getNextAction().getType() == MagusActions.LONGEVITY_RITUAL);
		assertTrue(seller.getNextAction()== buyer.getNextAction());
		assertTrue(sApprentice.getNextAction() == buyer.getNextAction());
		assertTrue(bApprentice.getNextAction() != buyer.getNextAction());
	}
	
	@Test
	public void basicLongevityRitualCoordWithBothApprenti() {
		seller.addXP(Abilities.LEADERSHIP, 15);

		buyer.maintenance();
		assertTrue(buyer.getNextAction().getType() == MagusActions.LONGEVITY_RITUAL);
		assertTrue(seller.getNextAction()== buyer.getNextAction());
		assertTrue(sApprentice.getNextAction() == buyer.getNextAction());
		assertTrue(bApprentice.getNextAction() == buyer.getNextAction());
	}
	
	@Test
	public void basicLongevityRitualCoord() {

		buyer.maintenance();
		assertTrue(buyer.getNextAction().getType() == MagusActions.LONGEVITY_RITUAL);
		assertTrue(seller.getNextAction()== buyer.getNextAction());
		assertTrue(sApprentice.getNextAction() != buyer.getNextAction());
		assertTrue(bApprentice.getNextAction() != buyer.getNextAction());
	}

	@Test
	public void basicLongevityRitualCoordWithForwardSchedule() {
		seller.setDecider(new HardCodedDecider<Magus>(MagusActions.FOUND_COVENANT));	// overrides Longevity Ritual
		seller.decide();
		assertTrue(seller.getNextAction().getType() == MagusActions.FOUND_COVENANT);
		assertEquals(buyer.getNumberInInventoryOf(AMU.sampleLongevityRitualService), 1);
		assertEquals(seller.getActionPlan().timeToEndOfQueue(), 13);
		assertEquals(seller.getActionPlan().timeToNextActionStarts(), 0);
		buyer.maintenance();
		Action<?> nextAction = buyer.getNextAction();
		assertTrue(nextAction instanceof InventLongevityRitual);
		assertEquals(buyer.getActionPlan().timeToEndOfQueue(), 26);
		assertEquals(buyer.getActionPlan().timeToNextActionStarts(), 13);
		assertEquals(seller.getActionPlan().timeToEndOfQueue(), 26);
		assertEquals(seller.getActionPlan().timeToNextActionStarts(), 0);
		
		assertTrue(seller.getNextAction().getType() == MagusActions.FOUND_COVENANT);
		assertEquals(buyer.getNumberInInventoryOf(AMU.sampleLongevityRitualService), 0);
		
		w.setCurrentTime(13l);
		runNextAction(seller);
		
		assertTrue(seller.getNextAction() == nextAction);
	}

	@Test
	public void longevityRitualCancelledOnStartDueToInsufficientVisCausesRedecision() {
		buyer.maintenance();
		Action<?> nextAction = buyer.getNextAction();
		assertTrue(nextAction.getType() == MagusActions.LONGEVITY_RITUAL);
		buyer.removeVis(Arts.CREO, 18);
		nextAction.start();
		assertTrue(nextAction.isDeleted());
		nextAction = buyer.getNextAction();
		assertTrue(nextAction != null);
		assertFalse(nextAction instanceof InventLongevityRitual);
		assertTrue(seller.getNextAction() != nextAction);
		assertTrue(seller.getNextAction() != null);
		assertTrue(seller.getNextAction() != buyer.getNextAction());
		assertEquals(buyer.getActionPlan().timeToEndOfQueue(), 13);
		assertEquals(seller.getActionPlan().timeToEndOfQueue(), 13);
	}

	private void runNextAction(Magus m) {
		Action<?> a = m.getActionPlan().getNextAction();
		a.start();
		a.run();
	}
}
