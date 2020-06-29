package hopshackle.simulation.arsmagica;

import java.util.ArrayList;
import java.util.List;

import hopshackle.simulation.*;
import org.javatuples.Triplet;

public class WriteSumma extends ArsMagicaAction {

    private Learnable skill;
    private int summaLvl;
    private int quality;
    private int pointsSoFar;
    private boolean isCovenantService;
    private boolean nothingToWriteOn;


    public WriteSumma(Magus a, Learnable skill) {
        super(MagusActions.WRITE_SUMMA, a);
        this.skill = skill;
        Triplet<Double, Integer, Integer> decision = calculateValue(skill, magus);
        summaLvl = decision.getValue1();
        quality = decision.getValue2();
        if (decision.getValue0() < 0.0)
            nothingToWriteOn = true;
        if (magus.getSeasonsServiceOwed() > 0)
            isCovenantService = true;
    }

    public WriteSumma(Magus a) {
        super(MagusActions.WRITE_SUMMA, a);
        double value = -100.0;
        for (Learnable s : skillsOnWhichSummaCouldBeWritten(magus)) {
            Triplet<Double, Integer, Integer> decision = calculateValue(s, magus);
            if (decision.getValue0() > value) {
                value = decision.getValue0();
                skill = s;
                summaLvl = decision.getValue1();
                quality = decision.getValue2();
            }
        }
        if (value <= 0.0)
            nothingToWriteOn = true;
        if (magus.getSeasonsServiceOwed() > 0)
            isCovenantService = true;
    }

    public WriteSumma(WriteSumma currentProject) {
        super(MagusActions.WRITE_SUMMA, currentProject.getActor());
        skill = currentProject.skill;
        summaLvl = currentProject.summaLvl;
        quality = currentProject.quality;
        pointsSoFar = currentProject.pointsSoFar;
        magus.setCurrentBookProject(this);
        isCovenantService = currentProject.isCovenantService;
        nothingToWriteOn = currentProject.nothingToWriteOn;
    }

    protected void doStuff() {
        magus.setCurrentBookProject(this);
        int pointsInSeason = magus.getLevelOf(Abilities.LATIN) + magus.getCommunication();
        pointsSoFar += pointsInSeason;
        if (pointsSoFar == pointsInSeason && pointsSoFar < summaLvl)
            magus.log("Starts work on new Summa on " + skill);
        else if (pointsSoFar < summaLvl)
            magus.log("Continues work on Summa on " + skill);
        else {
            if (pointsSoFar == pointsInSeason)
                magus.log("Writes new Summa on " + skill);
            else
                magus.log("Completes work on Summa on " + skill);
            Summa newSumma = new Summa(skill, summaLvl, quality, magus);
            newSumma.giveToRecipient(magus, isCovenantService);
            magus.setCurrentBookProject(null);
        }
        if (isCovenantService)
            magus.doSeasonsService();
        magus.addXP(Abilities.LATIN, 2);
    }

    public static List<Learnable> skillsOnWhichSummaCouldBeWritten(Magus magus) {
        List<Learnable> retValue = new ArrayList<>();
        for (Learnable s : magus.getSkills().keySet()) {
            if (s == Abilities.LATIN || s == Abilities.VIS_HUNT || s == Abilities.FAMILIAR_HUNT || s == Abilities.DECREPITUDE || s == Abilities.WARPING)
                continue;
            if (magus.getLevelOf(s) < 4)
                continue;
            if (magus.getLevelOf(s) < 10 && s instanceof Arts)
                continue;
            retValue.add(s);
        }
        return retValue;
    }

    public static Triplet<Double, Integer, Integer> calculateValue(Learnable skill, Magus magus) {
        int com = magus.getCommunication();
        int skillLvl = magus.getLevelOf(skill);
        int latin = magus.getLevelOf(Abilities.LATIN);
        double valueOfSumma = -100;
        int optimalLevel = 0;
        int optimalQuality = 0;
        int maxLevel = skillLvl / 2;
        int baseQuality = com + 6;
        List<Book> allBooks = magus.getAllAccessibleBooks();
        allBooks.addAll(magus.getInventoryOnMarketOf(AMU.sampleBook));
        List<Book> currentSummae = Summa.filterRelevantSummae(allBooks, skill);
        int currentSeasons = AMU.getSeasonsToMaxFrom(skill, currentSummae);
        int startingLvl = AMU.getHighestSummaFrom(skill, currentSummae);
        for (int proposedLevel = maxLevel; proposedLevel > 0; proposedLevel--) {
            double proposedQuality = Math.min(baseQuality + (maxLevel - proposedLevel), baseQuality * 2);
            int effectiveQuality = (int) Math.min(proposedQuality, skill.getXPForLevel(proposedLevel));
            Book proposedBook = new Summa(skill, proposedLevel, effectiveQuality, magus, true);
            List<Book> amendedLibrary = HopshackleUtilities.cloneList(currentSummae);
            amendedLibrary.add(proposedBook);
            int seasonsStudy = AMU.getSeasonsToMaxFrom(skill, amendedLibrary);
            int seasonsToWrite = (int) (Math.ceil(proposedLevel / (double) (com + latin)));
            int seasonGain = Math.max(currentSeasons - seasonsStudy, 0);
            int extraSeasonsStudy = Math.max(seasonsStudy - currentSeasons, 0);
            int xpGain = Math.max(skill.getXPForLevel(proposedLevel) - skill.getXPForLevel(startingLvl), 0);
            // using xpGain means that we equilibrate between Arts and Abilities
            double value = 2.0 + (xpGain / 5.0 - seasonsToWrite * 2.0 + seasonGain * 4.0 - extraSeasonsStudy);
            if (value > valueOfSumma) {
                valueOfSumma = value;
                optimalLevel = proposedLevel;
                optimalQuality = effectiveQuality;
            }
        }
        return new Triplet<>(valueOfSumma * MagusPreferences.getResearchPreference(magus, skill), optimalLevel, optimalQuality);
    }

    public boolean equals(Object other) {
        if (other instanceof WriteSumma) {
            WriteSumma wb = (WriteSumma) other;
            if (wb.skill == skill && wb.summaLvl == summaLvl && wb.magus == magus && wb.quality == quality)
                return true;
        }
        return false;
    }

    public boolean isWorthwhile() {
        return !nothingToWriteOn;
    }

    public String description() {
        return String.format("L%d Q%d on %s", summaLvl, quality, skill.toString());
    }

    public boolean isCovenantService() {
        return isCovenantService;
    }

}
