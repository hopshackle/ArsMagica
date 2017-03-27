package hopshackle.simulation.arsmagica.test;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;

import hopshackle.simulation.*;
import hopshackle.simulation.arsmagica.*;

public class TractatusTest {
	
	private Magus magus;
	private World w;
	
	@Before
	public void setup() {
		w = new World(new SimpleWorldLogic<Magus>(new ArrayList<ActionEnum<Magus>>(EnumSet.allOf(MagusActions.class))));
		new Tribunal("Test", w);
		magus = new Magus(w);
		magus.setCommunication(1);
	}

	@Test
	public void mayWriteTractatusWhenLevelIsAtMinimum() {
		assertFalse(MagusActions.WRITE_TRACTATUS.isChooseable(magus));
		assertTrue(magus.getPossibleTractactusSubjects().isEmpty());
		
		magus.addXP(Abilities.LATIN, 75);
		magus.addXP(Abilities.ARTES_LIBERALES, 5);
		assertFalse(MagusActions.WRITE_TRACTATUS.isChooseable(magus));
		assertTrue(magus.getPossibleTractactusSubjects().isEmpty());
		
		magus.addXP(Arts.CREO, 15);
		assertTrue(MagusActions.WRITE_TRACTATUS.isChooseable(magus));
		assertEquals(magus.getPossibleTractactusSubjects().size(), 1);
		assertTrue(magus.getPossibleTractactusSubjects().contains(Arts.CREO));
	}
	
	@Test
	public void mayNotWriteTractatusIfQuotaIsAlreadyFilled() {
		mayWriteTractatusWhenLevelIsAtMinimum();
		new Tractatus(Arts.CREO, magus);
		assertFalse(MagusActions.WRITE_TRACTATUS.isChooseable(magus));
		assertTrue(magus.getPossibleTractactusSubjects().isEmpty());
		
		magus.addXP(Abilities.ARTES_LIBERALES, 10);
		assertTrue(MagusActions.WRITE_TRACTATUS.isChooseable(magus));
		assertEquals(magus.getPossibleTractactusSubjects().size(), 1);
		assertTrue(magus.getPossibleTractactusSubjects().contains(Abilities.ARTES_LIBERALES));
	}
	
	@Test
	public void mayNotReadOwnTractatus() {
		Tractatus tract = new Tractatus(Arts.PERDO, magus);
		assertEquals(tract.getQuality(), 7);
		assertEquals(tract.getXPGainForMagus(magus), 0);
		assertEquals(tract.getXPGainForMagus(new Magus(w)), 7);
	}
	
	@Test
	public void mayNotReadTheSameTractatusAgain() {
		Magus temp = new Magus(w);
		temp.setCommunication(-1);
		Tractatus tract = new Tractatus(Arts.PERDO, temp);
		assertEquals(tract.getXPGainForMagus(magus), 5);
		magus.setTractatusAsRead(tract.getTitleId());
		assertEquals(tract.getXPGainForMagus(magus), 0);
	}
	
	@Test
	public void onReadingGainsXPEqualToQuality() {
		Magus temp = new Magus(w);
		temp.setCommunication(0);
		Tractatus tract = new Tractatus(Arts.PERDO, temp);
		magus.addItem(tract);
		addStartAndRunAction(new ReadBook(magus, tract));
		assertEquals(magus.getTotalXPIn(Arts.PERDO), 6);
		assertTrue(magus.hasReadTractatus(tract.getTitleId()));
	}
	
	@Test
	public void writingATractatus() {
		mayWriteTractatusWhenLevelIsAtMinimum();
		addStartAndRunAction(new WriteTractatus(magus));
		assertEquals(magus.getInventoryOf(AMU.sampleBook).size(), 1);
		
	}

	private void addStartAndRunAction(ArsMagicaAction a) {
		a.addToAllPlans();
		a.start();
		a.run();
	}
}
