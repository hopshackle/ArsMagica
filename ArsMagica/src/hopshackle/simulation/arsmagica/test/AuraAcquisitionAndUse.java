package hopshackle.simulation.arsmagica.test;

import static org.junit.Assert.*;

import org.junit.*;

import hopshackle.simulation.*;
import hopshackle.simulation.arsmagica.*;

public class AuraAcquisitionAndUse {
	
	private Magus magus;
	private World world;
	
	@Before
	public void setup() {
		world = new World();
		Tribunal tribunal = new Tribunal("Test", world);
		tribunal.setVisLevel(200);
		magus = new Magus(world);
	}

	@Test
	public void useBetterAuraIfFound() {
		assertEquals(magus.getMagicAura(), 0);
		SearchForVis search = new SearchForVis(magus);
		search.run();
		assertTrue(magus.getMagicAura() > 0);
	}
	
	@Test
	public void doNotUseAuraIfInferiorToCurrentOne() {
		magus.setMagicAura(12);
		SearchForVis search = new SearchForVis(magus);
		search.run();
		assertEquals(magus.getMagicAura(), 12);
	}
	
	@Test
	public void mayDistillVisInMagicAura() {
		assertFalse(MagusActions.DISTILL_VIS.isChooseable(magus));
		magus.setMagicAura(2);
		int intelligence = magus.getIntelligence();
		magus.addXP(Arts.CREO, 55);	// Cr10
		magus.addXP(Arts.VIM, 55);	// Vi10
		int expected = (20 + intelligence + 2) / 10;
		assertTrue(MagusActions.DISTILL_VIS.isChooseable(magus));
		DistillVis distillation = new DistillVis(magus);
		distillation.run();
		assertEquals(magus.getPawnsOf(Arts.VIM), expected);
	}

}
