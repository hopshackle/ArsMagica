package hopshackle.simulation.arsmagica.test;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;

import hopshackle.simulation.*;
import hopshackle.simulation.arsmagica.*;

public class LearningArtsAndAbilities {
	
	private Magus magus;
	
	
	@Before
	public void setUp() {
		World w = new World(new SimpleWorldLogic<Magus>(new ArrayList<ActionEnum<Magus>>(EnumSet.allOf(MagusActions.class))));
		magus = new Magus(w);
		magus.setTribunal(new Tribunal("test", w));
	}

	@Test
	public void GainXPInAnArt() {
		assertEquals(magus.getLevelOf(Arts.REGO), 0);
		magus.addXP(Arts.REGO, 2);
		assertEquals(magus.getLevelOf(Arts.REGO), 1);
		assertEquals(magus.getTotalXPIn(Arts.REGO), 2);
		assertEquals(magus.getUnusedXPIn(Arts.REGO), 1);
		assertEquals(magus.getXPToNextLevelIn(Arts.REGO), 1);
		magus.addXP(Arts.REGO, 2);
		assertEquals(magus.getLevelOf(Arts.REGO), 2);
		assertEquals(magus.getTotalXPIn(Arts.REGO), 4);
		assertEquals(magus.getUnusedXPIn(Arts.REGO), 1);
		assertEquals(magus.getXPToNextLevelIn(Arts.REGO), 2);
		magus.addXP(Arts.REGO, 2);
		assertEquals(magus.getLevelOf(Arts.REGO), 3);
		assertEquals(magus.getTotalXPIn(Arts.REGO), 6);
		assertEquals(magus.getUnusedXPIn(Arts.REGO), 0);
		assertEquals(magus.getXPToNextLevelIn(Arts.REGO), 4);
		magus.addXP(Arts.REGO, 2);
		assertEquals(magus.getLevelOf(Arts.REGO), 3);
		assertEquals(magus.getTotalXPIn(Arts.REGO), 8);
		assertEquals(magus.getUnusedXPIn(Arts.REGO), 2);
		assertEquals(magus.getXPToNextLevelIn(Arts.REGO), 2);
		magus.addXP(Arts.REGO, 20);
		assertEquals(magus.getLevelOf(Arts.REGO), 7);
		assertEquals(magus.getTotalXPIn(Arts.REGO), 28);
		assertEquals(magus.getUnusedXPIn(Arts.REGO), 0);
		assertEquals(magus.getXPToNextLevelIn(Arts.REGO), 8);
	}
	
	@Test
	public void GainXPInAnAbility() {
		assertEquals(magus.getLevelOf(Abilities.MAGIC_THEORY), 0);
		magus.addXP(Abilities.MAGIC_THEORY, 2);
		assertEquals(magus.getLevelOf(Abilities.MAGIC_THEORY), 0);
		assertEquals(magus.getTotalXPIn(Abilities.MAGIC_THEORY), 2);
		assertEquals(magus.getUnusedXPIn(Abilities.MAGIC_THEORY), 2);
		assertEquals(magus.getXPToNextLevelIn(Abilities.MAGIC_THEORY), 3);
		magus.addXP(Abilities.MAGIC_THEORY, 2);
		assertEquals(magus.getLevelOf(Abilities.MAGIC_THEORY), 0);
		assertEquals(magus.getTotalXPIn(Abilities.MAGIC_THEORY), 4);
		assertEquals(magus.getUnusedXPIn(Abilities.MAGIC_THEORY), 4);
		assertEquals(magus.getXPToNextLevelIn(Abilities.MAGIC_THEORY), 1);
		magus.addXP(Abilities.MAGIC_THEORY, 2);
		assertEquals(magus.getLevelOf(Abilities.MAGIC_THEORY), 1);
		assertEquals(magus.getTotalXPIn(Abilities.MAGIC_THEORY), 6);
		assertEquals(magus.getUnusedXPIn(Abilities.MAGIC_THEORY), 1);
		assertEquals(magus.getXPToNextLevelIn(Abilities.MAGIC_THEORY), 9);
		magus.addXP(Abilities.MAGIC_THEORY, 2);
		assertEquals(magus.getLevelOf(Abilities.MAGIC_THEORY), 1);
		assertEquals(magus.getTotalXPIn(Abilities.MAGIC_THEORY), 8);
		assertEquals(magus.getUnusedXPIn(Abilities.MAGIC_THEORY), 3);
		assertEquals(magus.getXPToNextLevelIn(Abilities.MAGIC_THEORY), 7);
		magus.addXP(Abilities.MAGIC_THEORY, 20);
		assertEquals(magus.getLevelOf(Abilities.MAGIC_THEORY), 2);
		assertEquals(magus.getTotalXPIn(Abilities.MAGIC_THEORY), 28);
		assertEquals(magus.getUnusedXPIn(Abilities.MAGIC_THEORY), 13);
		assertEquals(magus.getXPToNextLevelIn(Abilities.MAGIC_THEORY), 2);
	}
	
	@Test
	public void LoseXPInAnAbilityAlsoWorks() {
		assertEquals(magus.getLevelOf(Abilities.MAGIC_THEORY), 0);
		magus.addXP(Abilities.MAGIC_THEORY, 7);
		assertEquals(magus.getLevelOf(Abilities.MAGIC_THEORY), 1);
		assertEquals(magus.getTotalXPIn(Abilities.MAGIC_THEORY), 7);
		assertEquals(magus.getUnusedXPIn(Abilities.MAGIC_THEORY), 2);
		magus.addXP(Abilities.MAGIC_THEORY, -3);
		assertEquals(magus.getLevelOf(Abilities.MAGIC_THEORY), 0);
		assertEquals(magus.getTotalXPIn(Abilities.MAGIC_THEORY), 4);
		assertEquals(magus.getUnusedXPIn(Abilities.MAGIC_THEORY), 4);
		magus.addXP(Abilities.MAGIC_THEORY, -7);
		assertEquals(magus.getLevelOf(Abilities.MAGIC_THEORY), 0);
		assertEquals(magus.getTotalXPIn(Abilities.MAGIC_THEORY), 0);
		assertEquals(magus.getUnusedXPIn(Abilities.MAGIC_THEORY), 0);
	}
	
	@Test
	public void PractiseAnAbility() {
		magus.getActionPlan().addAction(new PractiseAbility(magus, Abilities.MAGIC_THEORY));
		assertEquals(magus.getLevelOf(Abilities.MAGIC_THEORY), 0);
		runNextAction(magus);
		assertEquals(magus.getLevelOf(Abilities.MAGIC_THEORY), 0);
		assertEquals(magus.getTotalXPIn(Abilities.MAGIC_THEORY), 4);
	}
	
	private void runNextAction(Magus m) {
		Action<?> a = m.getActionPlan().getNextAction();
		a.start();
		a.run();
	}


}
