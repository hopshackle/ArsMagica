package hopshackle.simulation.arsmagica.test;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;

import hopshackle.simulation.*;
import hopshackle.simulation.arsmagica.*;

public class AgeingAndDecrepitude {

    private Magus magus, caster;
    private World w;
    private AgeingEvent ae;

    @Before
    public void setup() {
        w = new World(new SimpleWorldLogic<>(new ArrayList<>(EnumSet.allOf(MagusActions.class))));
        new Tribunal("test", w);
        w.setCalendar(new FastCalendar(0));
        magus = new Magus(w);
        caster = new Magus(w);
        caster.setIntelligence(1);
        magus.setIntelligence(1);
        magus.setMagicAura(2);
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
    public void ageingCausesCharacteristicDecline() {
        ;
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
        magus.addXP(Abilities.MAGIC_THEORY, 105); // MT:6
        assertEquals(magus.getAge(), 0);
        assertEquals(magus.getLongevityModifier(), 0);
        ArsMagicaAction a = new FoundCovenant(HopshackleUtilities.listFromInstance(magus));
        magus.setDecider(new HardCodedDecider<>(MagusActions.LONGEVITY_RITUAL));
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
        covenant.setAuraAndCapacity(2, 10);
        magus.addVis(Arts.VIM, 13);

        runNextAction(magus);
        assertEquals(magus.getPawnsOf(Arts.VIM), 2); // age 51, so 11 pawns needed
        // Lab Total = 19 (CrCo = 5 + 5, Int +1, Aura +2 MT +6
        assertEquals(magus.getLongevityModifier(), 0);  // -6 from age, +4 from Longevity, +2 from wealth

        magus.addXP(Abilities.MAGIC_THEORY, (35 + 40));
        magus.addVis(Arts.VIM, 11);
        runNextAction(magus);
        assertEquals(magus.getPawnsOf(Arts.VIM), 2);
        assertEquals(magus.getLongevityModifier(), +1);
    }

    @Test
    public void longevityRitualChooseableOnlyIfMagicTheoryAndVisAvailable() {
        magus.setAge(33);
        assertEquals(magus.getAge(), 33);
        magus.addXP(Arts.CREO, 15);
        magus.addXP(Arts.CORPUS, 15);
        assertEquals(magus.getLabTotal(Arts.CREO, Arts.CORPUS), 13);
        assertFalse(MagusActions.LONGEVITY_RITUAL.isChooseable(magus));
        magus.setAge(34);
        assertEquals(magus.getAge(), 34);
        assertFalse(MagusActions.LONGEVITY_RITUAL.isChooseable(magus));
        // and now magic theory
        magus.addXP(Abilities.MAGIC_THEORY, 105); // MT:6
        assertFalse(MagusActions.LONGEVITY_RITUAL.isChooseable(magus));
        assertEquals(magus.getLabTotal(Arts.CREO, Arts.CORPUS), 19);
        // now add vis
        magus.addVis(Arts.VIM, 10);
        assertTrue(MagusActions.LONGEVITY_RITUAL.isChooseable(magus));
        addStartAndRunAction(new InventLongevityRitual(magus));
        assertFalse(MagusActions.LONGEVITY_RITUAL.isChooseable(magus));
    }

    @Test
    public void longevityRitualRequiresSufficientVis() {
        magus.setAge(29);
        magus.addXP(Abilities.MAGIC_THEORY, 75);
        assertFalse(InventLongevityRitual.meetsRequirements(magus, magus));
        magus.addVis(Arts.CORPUS, 5);
        assertFalse(InventLongevityRitual.meetsRequirements(magus, magus));
        magus.addVis(Arts.CREO, 1);
        assertTrue(InventLongevityRitual.meetsRequirements(magus, magus));
    }

    @Test
    public void longevityRitualServiceRegistersCorrectValues() {
        magus.setAge(40);
        LongevityRitualService lrs = new LongevityRitualService(caster);
        caster.addXP(Abilities.MAGIC_THEORY, 75); // MT 5
        caster.addXP(Arts.CREO, 75); // Cr 10
        assertEquals(lrs.getMagicTheory(), 5);
        assertEquals(caster.getLabTotal(Arts.CREO, Arts.CORPUS), 17); // 5 MT + 11 Creo + 1 Int
        assertEquals(lrs.getLabTotal(), caster.getLabTotal(Arts.CREO, Arts.CORPUS));
    }

    @Test
    public void longevityRitualServiceTriggersIfRequirementsMet() {
        magus.setAge(50);
        LongevityRitualService lrs = new LongevityRitualService(caster);
        magus.addItem(lrs);
        assertEquals(magus.getNumberInInventoryOf(AMU.sampleLongevityRitualService), 1);
        caster.addXP(Abilities.MAGIC_THEORY, 75); // level 5
        magus.addVis(Arts.CREO, 10);
        assertEquals(magus.getLongevityRitualEffect(), 0);
        assertTrue(InventLongevityRitual.meetsRequirements(magus, caster));
        lrs.artefactMaintenance(magus);

        Action a = magus.getActionPlan().getNextAction();
        assertEquals(a.getType(), MagusActions.LONGEVITY_RITUAL);
        a.start();
        a.run();
        assertEquals(magus.getLongevityRitualEffect(), 2);  // MT=5, Int = 1 + 1, Aura = 2; LabTotal = 9
        assertEquals(magus.getNumberInInventoryOf(lrs), 0);
    }

    @Test
    public void longevityRitualServiceMaintenanceOnlyTriggersIfVisRequirementsMet() {
        magus.setAge(50);
        magus.addItem(new LongevityRitualService(caster));
        caster.addXP(Abilities.MAGIC_THEORY, 75); // level 5
        caster.addVis(Arts.CREO, 20);
        assertEquals(magus.getLongevityRitualEffect(), 0);
        magus.maintenance();
        assertNull(magus.getActionPlan().getNextAction());
    }

    @Test
    public void longevityRitualServiceMaintenanceOnlyTriggersIfMagicTheoryRequirementsMet() {
        magus.setAge(50);
        magus.addItem(new LongevityRitualService(caster));
        caster.addXP(Abilities.MAGIC_THEORY, 70); // level 4
        magus.addVis(Arts.CREO, 10);
        assertEquals(magus.getLongevityRitualEffect(), 0);
        magus.maintenance();
        assertNull(magus.getActionPlan().getNextAction());
    }

    @Test
    public void longevityRitualServiceMaintenanceOnlyTriggersIfWouldImproveCurrentRitual() {
        magus.setAge(50);
        magus.setLongevityRitualEffect(2);
        magus.addItem(new LongevityRitualService(caster));
        caster.addXP(Abilities.MAGIC_THEORY, 75); // level 5
        magus.addVis(Arts.CREO, 10);
        assertEquals(magus.getLongevityRitualEffect(), 2);
        magus.maintenance();
        assertNull(magus.getActionPlan().getNextAction());
        assertEquals(magus.getLongevityRitualEffect(), 2);
    }


    @Test
    public void longevityRitualUsesUpVisCorrectly() {
        magus.addXP(Abilities.MAGIC_THEORY, 105); // MT:6
        magus.setAge(29);
        magus.addXP(Arts.CREO, 15);
        magus.addXP(Arts.CORPUS, 15);
        magus.addVis(Arts.CREO, 3);
        magus.addVis(Arts.CORPUS, 1);
        magus.addVis(Arts.VIM, 3);
        InventLongevityRitual ritual = new InventLongevityRitual(magus);
        addStartAndRunAction(ritual);
        assertEquals(magus.getPawnsOf(Arts.CREO), 1);
        assertEquals(magus.getPawnsOf(Arts.CORPUS), 0);
        assertEquals(magus.getPawnsOf(Arts.VIM), 0);
    }

    @Test
    public void longevityRitualExpiresAfterAgeingCrisis() {
        magus.addXP(Arts.CREO, 15);
        magus.addXP(Arts.CORPUS, 15);
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
