package hopshackle.simulation.arsmagica.test;

import static org.junit.Assert.*;
import hopshackle.simulation.Dice;

import org.junit.Test;

public class StressDieResults {

	@Test
	public void hundredRolls() {
		int overTen = 0, potentialBotch = 0;
		double average = 0.0;
		for (int i = 0; i < 200; i++)  {
			int result = Dice.stressDieResult();
			if (result > 10)
				overTen++;
			if (result == 0)
				potentialBotch++;
			average += result / 200.0;
		}
		
		assertTrue(overTen > 1);
		assertFalse(overTen > 20);
		assertTrue(potentialBotch > 8);
		assertTrue(potentialBotch < 30);
		assertEquals(average, 5.5, 1.0);
	}

}
