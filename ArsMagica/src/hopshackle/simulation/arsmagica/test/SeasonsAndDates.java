package hopshackle.simulation.arsmagica.test;

import static org.junit.Assert.*;
import org.junit.*;

import hopshackle.simulation.*;
import hopshackle.simulation.arsmagica.*;

public class SeasonsAndDates {
	
	private Agent magus1, magus2;
	private World w;
	
	@Before
	public void setup() {
		w = new World();
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
	public void actionOverrideWorksWithEmptyQueue() {
		magus1.addAction(new InTwilight(magus1, 1));
		assertEquals(magus1.getNextAction().getStartTime(), 13);
		assertTrue(magus2.getNextAction() == null);
		w.setCurrentTime((long) (13));
		
		magus2.setActionOverride(new SearchForVis(magus2));
		assertEquals(magus1.getNextAction().getStartTime(), 13);
		assertEquals(magus2.getNextAction().getStartTime(), 26);
		
		magus1.setActionOverride(new JoinCovenant(magus1));
		assertEquals(magus1.getNextAction().getStartTime(), 13);
		assertEquals(magus2.getNextAction().getStartTime(), 26);
		assertTrue(magus2.getNextAction() instanceof SearchForVis);
		assertTrue(magus1.getNextAction() instanceof InTwilight);
		
		magus1.getNextAction().run();
		assertEquals(magus1.getNextAction().getStartTime(), 26);
		assertEquals(magus2.getNextAction().getStartTime(), 26);
		assertTrue(magus2.getNextAction() instanceof SearchForVis);
		assertTrue(magus1.getNextAction() instanceof JoinCovenant);
		
		assertEquals(magus1.getActionQueue().size(),1);
		assertEquals(magus2.getActionQueue().size(),1);
	}
	
}
