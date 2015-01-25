package hopshackle.simulation.arsmagica.test;

import static org.junit.Assert.*;

import java.util.*;

import hopshackle.simulation.arsmagica.*;

import org.junit.*;

public class SeasonsToReadAllSummae {
	
	private Summa L5Q15, L7Q28, L7Q25, L9Q14, L18Q10, L18Q13, L10Q14, L4Q20;
	private List<Book> library;
	
	@Before
	public void setUp() throws Exception {
		L5Q15 = new Summa(Arts.CREO, 5, 15, null);
		L7Q28 = new Summa(Arts.CREO, 7, 28, null);
		L7Q25 = new Summa(Arts.CREO, 7, 25, null);
		L9Q14 = new Summa(Arts.CREO, 9, 14, null);
		L18Q10 = new Summa(Arts.CREO, 18, 10, null);
		L18Q13 = new Summa(Arts.CREO, 18, 13, null);
		L10Q14 = new Summa(Arts.CREO, 10, 14, null);
		L4Q20 = new Summa(Arts.CREO, 4, 20, null);
		library = new ArrayList<Book>();
	}

	@Test
	public void justOneBookI() {
		library.add(L5Q15);
		assertEquals(AMU.getSeasonsToMaxFrom(Arts.CREO, library), 1);
	}
	@Test
	public void justOneBookII() {
		library.add(L7Q28);
		assertEquals(AMU.getSeasonsToMaxFrom(Arts.CREO, library), 1);
	}
	@Test
	public void justOneBookIII() {
		library.add(L7Q25);
		assertEquals(AMU.getSeasonsToMaxFrom(Arts.CREO, library), 2);
	}
	@Test
	public void justOneBookIV() {
		library.add(L18Q10);
		assertEquals(AMU.getSeasonsToMaxFrom(Arts.CREO, library), 18);
	}
	@Test
	public void twoBooksWithFirstCompleted() {
		library.add(L18Q10);
		library.add(L5Q15);
		assertEquals(AMU.getSeasonsToMaxFrom(Arts.CREO, library), 17);
	}
	@Test
	public void twoBooksWithFirstIgnored() {
		library.add(L5Q15);
		library.add(L4Q20);
		assertEquals(AMU.getSeasonsToMaxFrom(Arts.CREO, library), 1);
	}
	@Test
	public void threeBooksWithAllReadToCompletion() {
		library.add(L5Q15);
		library.add(L10Q14);
		library.add(L18Q10);
		assertEquals(AMU.getSeasonsToMaxFrom(Arts.CREO, library), 16);
	}
	@Test
	public void threeBooksWithMiddleNotReadToCompletion() {
		library.add(L5Q15);
		library.add(L9Q14);
		library.add(L18Q13);
		assertEquals(AMU.getSeasonsToMaxFrom(Arts.CREO, library), 13);
	}

}
