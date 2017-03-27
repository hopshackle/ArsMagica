package hopshackle.simulation.arsmagica.test;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;

import hopshackle.simulation.*;
import hopshackle.simulation.arsmagica.*;

public class SeasonsAndDates {
	
	private Magus magus1, magus2;
	private World w;
	
	@Before
	public void setup() {
		w = new World(new SimpleWorldLogic<Magus>(new ArrayList<ActionEnum<Magus>>(EnumSet.allOf(MagusActions.class))));
		new Tribunal("test", w);
		w.setCalendar(new FastCalendar(0));
		magus1 = new Magus(w);
		magus2 = new Magus(w);
	}

	@Test
	public void seasons() {
		World w = new World();
		w.setCalendar(new FastCalendar(800 * 52));
		assertEquals(w.getYear(), 800);
		assertEquals(w.getSeason(), 0);
		w.setCurrentTime(w.getCurrentTime() + 13);
		assertEquals(w.getYear(), 800);
		assertEquals(w.getSeason(), 1);
		w.setCurrentTime(w.getCurrentTime() + 13);
		assertEquals(w.getYear(), 800);
		assertEquals(w.getSeason(), 2);
		w.setCurrentTime(w.getCurrentTime() + 13);
		assertEquals(w.getYear(), 800);
		assertEquals(w.getSeason(), 3);
		w.setCurrentTime(w.getCurrentTime() + 13);
		assertEquals(w.getYear(), 801);
		assertEquals(w.getSeason(), 0);
	}
	
	@Test
	public void startAndEndQueueTimes() {
		magus1.getActionPlan().addAction(new InTwilight(magus1, 1));
		assertEquals(magus1.getNextAction().getStartTime(), 0);
		assertTrue(magus2.getNextAction() == null);
		w.setCurrentTime((long) (13));
		
		magus2.getActionPlan().addAction(new SearchForVis(magus2));
		assertEquals(magus1.getNextAction().getStartTime(), 0);
		assertEquals(magus2.getNextAction().getStartTime(), 13);
		
		magus1.getActionPlan().addAction(new JoinCovenant(magus1, 1));
		assertEquals(magus1.getNextAction().getStartTime(), 0);
		assertEquals(magus2.getNextAction().getStartTime(), 13);
		assertEquals(magus1.getActionPlan().timeToNextActionStarts(), -13);
		assertEquals(magus1.getActionPlan().timeToEndOfQueue(), 26);
		assertEquals(magus2.getActionPlan().timeToNextActionStarts(), 0);
		assertEquals(magus2.getActionPlan().timeToEndOfQueue(), 13);
		assertTrue(magus2.getNextAction() instanceof SearchForVis);
		assertTrue(magus1.getNextAction() instanceof InTwilight);
		
		magus1.getNextAction().start();
		w.setCurrentTime((long) (26));
		magus1.getNextAction().run();
		assertEquals(magus1.getNextAction().getStartTime(), 26);
		assertEquals(magus2.getNextAction().getStartTime(), 13);
		assertTrue(magus2.getNextAction() instanceof SearchForVis);
		assertTrue(magus1.getNextAction() instanceof JoinCovenant);
		
		assertEquals(magus1.getActionPlan().timeToEndOfQueue(), 13);
		assertEquals(magus2.getActionPlan().timeToEndOfQueue(), 0);
	}
	
}
