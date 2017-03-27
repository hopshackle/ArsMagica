package hopshackle.simulation.arsmagica.test;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;

import hopshackle.simulation.*;
import hopshackle.simulation.arsmagica.*;


public class VisSourceMaintenance {
	
	private Magus magus;
	private World world;
	private VisSource creoSource, perdoSource;
	
	@Before
	public void setup() {
		world = new World(new SimpleWorldLogic<Magus>(new ArrayList<ActionEnum<Magus>>(EnumSet.allOf(MagusActions.class))));
		new Tribunal("Test", world);
		world.setCalendar(new FastCalendar(800 * 52));
		magus = new Magus(world);
		creoSource = new VisSource(Arts.CREO, 4, world);
		perdoSource = new VisSource(Arts.PERDO, 1, world);
		
		magus.addItem(creoSource);
		magus.addItem(perdoSource);
	}
	
	@Test
	public void magusMaintenanceUpdatesVisStocksOncePerYear() {
		creoSource.setAnnualExtinctionRate(0.0);
		perdoSource.setAnnualExtinctionRate(0.0);
		
		assertEquals(magus.getPawnsOf(Arts.CREO), 0);
		assertEquals(magus.getPawnsOf(Arts.PERDO), 0);
		magus.maintenance();
		assertEquals(magus.getPawnsOf(Arts.CREO), 4);
		assertEquals(magus.getPawnsOf(Arts.PERDO), 1);
		magus.maintenance();
		assertEquals(magus.getPawnsOf(Arts.CREO), 4);
		assertEquals(magus.getPawnsOf(Arts.PERDO), 1);
		world.setCurrentTime((long) 800.5 * 52); // Less than a year, so maintenance should do nothing
		magus.maintenance();
		assertEquals(magus.getPawnsOf(Arts.CREO), 4);
		assertEquals(magus.getPawnsOf(Arts.PERDO), 1);
		world.setCurrentTime((long) 801 * 52);	// Now a full year has passed
		magus.maintenance();
		assertEquals(magus.getPawnsOf(Arts.CREO), 8);
		assertEquals(magus.getPawnsOf(Arts.PERDO), 2);
	}
	
	@Test
	public void visSourcesExpireAndAreRemovedFromInventoryDuringLocationMaintenance() {
		creoSource.setAnnualExtinctionRate(1.0);
		perdoSource.setAnnualExtinctionRate(0.0);
		magus.maintenance();
		assertEquals(magus.getPawnsOf(Arts.CREO), 4);
		assertEquals(magus.getPawnsOf(Arts.PERDO), 1);
		List<Artefact> inventory = magus.getInventory();
		assertTrue(inventory.contains(creoSource));
		assertTrue(inventory.contains(perdoSource));
		
		world.maintenance();
		inventory = magus.getInventory();
		assertTrue(inventory.contains(creoSource));
		assertTrue(inventory.contains(perdoSource));
		assertEquals(creoSource.getAmountPerAnnum(), 0);
		assertTrue(creoSource.getParentLocation() == null);
		assertTrue(perdoSource.getParentLocation() == world);
		
		world.setCurrentTime((long) (802 * 52));
		magus.maintenance();
		inventory = magus.getInventory();
		assertFalse(inventory.contains(creoSource));
		assertTrue(inventory.contains(perdoSource));
	}
	
	@Test
	public void visSourcesReleasedOnDeathOfMagusWithoutHeirs() {
		magus.die("Ooops");
		List<Artefact> inventory = magus.getInventory();
		assertFalse(inventory.contains(creoSource));
		assertFalse(inventory.contains(perdoSource));
		world.maintenance();
		assertEquals(creoSource.getAmountPerAnnum(), 0);
		assertTrue(creoSource.getParentLocation() == null);
		assertTrue(perdoSource.getParentLocation() == null);
		assertEquals(perdoSource.getAmountPerAnnum(), 0);
	}
	
	@Test
	public void visSourcesRemainExtantIfInherited() {
		Magus apprentice = new Magus(world);
		magus.addApprentice(apprentice);
		
		magus.die("Ooops");
		List<Artefact> inventory = magus.getInventory();
		assertFalse(inventory.contains(creoSource));
		assertFalse(inventory.contains(perdoSource));
		assertEquals(creoSource.getAmountPerAnnum(), 4);
		assertTrue(creoSource.getParentLocation() == world);
		assertTrue(perdoSource.getParentLocation() == world);
		assertEquals(perdoSource.getAmountPerAnnum(), 1);
		
		inventory = apprentice.getInventory();
		assertTrue(inventory.contains(creoSource));
		assertTrue(inventory.contains(perdoSource));
	}

}
