package hopshackle.simulation.arsmagica;

import java.util.*;

import hopshackle.simulation.*;

public enum MagusActions implements ActionEnum<Magus> {

    SEARCH_VIS,
    SEARCH_APPRENTICE,
    SEARCH_FAMILIAR,
    BIND_FAMILIAR,
    STUDY_VIS,
    PRACTISE_ABILITY,
    DISTILL_VIS,
    LONGEVITY_RITUAL,
    INVENT_SPELL,
    SCRIBE_SPELL,
    COPY_SPELLS,
    WRITE_SUMMA,
    WRITE_TRACTATUS,
    READ_BOOK,
    COPY_BOOK,
    TEACH_APPRENTICE,
    FOUND_COVENANT,
    JOIN_COVENANT,
    DEVELOP_COVENANT,
    TWILIGHT;

    @Override
    public ArsMagicaAction getAction(Magus magus) {
        switch (this) {
            case SEARCH_VIS:
                return new SearchForVis(magus);
            case SEARCH_APPRENTICE:
                return new SearchForApprentice(magus);
            case SEARCH_FAMILIAR:
                return new SearchForFamiliar(magus);
            case BIND_FAMILIAR:
                return new BindFamiliar(magus);
            case STUDY_VIS:
                return new StudyFromVis(magus, magus.getTypeOfVisToStudy());
            case PRACTISE_ABILITY:
                Abilities abilityToPractise = magus.selectAbilityToPractise();
                return new PractiseAbility(magus, abilityToPractise);
            case DISTILL_VIS:
                return new DistillVis(magus);
            case LONGEVITY_RITUAL:
                return new InventLongevityRitual(magus);
            case INVENT_SPELL:
                if (magus.isResearchingSpell())
                    return new InventSpell(magus, magus.getCurrentSpellResearchProject());
                return new InventSpell(magus);
            case SCRIBE_SPELL:
                return new ScribeSpell(magus);
            case COPY_SPELLS:
                return new CopySpells(magus);
            case WRITE_SUMMA:
                if (magus.isWritingBook())
                    return new WriteSumma(magus.getCurrentBookProject());
                WriteSumma toWrite = new WriteSumma(magus);
                if (toWrite.isWorthwhile())
                    return toWrite;
                return new WriteTractatus(magus);
            case WRITE_TRACTATUS:
                return new WriteTractatus(magus);
            case READ_BOOK:
                Book bookToRead = magus.getBestBookToRead();
                if (bookToRead != null)
                    return new ReadBook(magus, bookToRead);
                return new CopyBook(magus);
            case COPY_BOOK:
                if (magus.isCopyingBook())
                    return new CopyBook(magus.getCurrentCopyProject());
                return new CopyBook(magus);
            case TEACH_APPRENTICE:
                if (!magus.hasApprentice()) return new SearchForApprentice(magus);
                return new TeachApprentice(magus, magus.getApprentice());
            case FOUND_COVENANT:
                List<Magus> founders = new ArrayList<Magus>();
                founders.add(magus);
                for (Agent ea : magus.getChildren()) {
                    Magus exApprentice = (Magus) ea;
                    if (!exApprentice.isApprentice() && exApprentice.getCovenant() == null && !exApprentice.isDead())
                        founders.add(exApprentice);
                }
                return new FoundCovenant(founders);
            case JOIN_COVENANT:
                return new JoinCovenant(magus);
            case DEVELOP_COVENANT:
                return new DevelopCovenant(magus);
            case TWILIGHT:
                return new InTwilight(magus, 1);
        }
        return null;
    }

    @Override
    public boolean isChooseable(Magus magus) {
        switch (this) {
            case SEARCH_VIS:
                return !magus.isApprentice();
            case SEARCH_FAMILIAR:
                return !magus.isApprentice() && !magus.hasFamiliar() && !(magus.getHermeticHouse() == HermeticHouse.BJORNAER);
            case BIND_FAMILIAR:
                if (magus.hasFamiliar()) {
                    Familiar f = magus.getFamiliar();
                    return f.canImproveBond(magus);
                } else
                    return false;
            case SEARCH_APPRENTICE:
                if (magus.hasApprentice()) return false;
                return magus.getTotalArtLevels() >= 100;
            case STUDY_VIS:
                Arts artToStudy = magus.getTypeOfVisToStudy();
                if (artToStudy == null) return false;
                return true;
            case PRACTISE_ABILITY:
                return true;
            case DISTILL_VIS:
                if (magus.getMagicAura() == 0) return false;
                if (magus.getLabTotal(Arts.CREO, Arts.VIM) < 10) return false;
                return true;
            case LONGEVITY_RITUAL:
                if (magus.getAge() >= 30) {
                    int bonus = (int) (magus.getLabTotal(Arts.CREO, Arts.CORPUS) / 5.0);
                    if (magus.getLongevityRitualEffect() == 0 || bonus >= magus.getLongevityRitualEffect() + 2)
                        if (InventLongevityRitual.meetsRequirements(magus, magus))
                            return true;    // need to ensure can create an effect giving a result better than +2
                }
                return false;
            case INVENT_SPELL:
                if (magus.isApprentice()) return false;
                if (magus.getTotalArtLevels() < 25) return false;
                return true;
            case SCRIBE_SPELL:
                List<Spell> spellsToScribe = ScribeSpell.getAllUnscribedSpellsKnown(magus);
                int levels = 0;
                for (Spell s : spellsToScribe)
                    levels += s.getLevel();
                if (levels > magus.getLevelOf(Abilities.LATIN) * 10)
                    return true;
                return false;
            case COPY_SPELLS:
                if (magus.getLevelOf(Abilities.SCRIBE) < 1)
                    return false;
                if (!magus.isApprentice() && magus.getSeasonsServiceOwed() == 0)
                    return false;
                return (!magus.getBestSpellsToCopy().isEmpty());
            case WRITE_SUMMA:
                if (magus.getLevelOf(Abilities.LATIN) < 4) return false;
                if (magus.getLevelOf(Abilities.LATIN) + magus.getCommunication() < 2) return false;
                if (magus.getLevelOf(Abilities.ARTES_LIBERALES) < 1) return false;
                if (magus.isWritingBook())
                    return true;
                for (Learnable skill : magus.getSkills().keySet()) {
                    if (skill == Abilities.LATIN || skill == Abilities.DECREPITUDE || skill == Abilities.WARPING || skill == Abilities.VIS_HUNT)
                        continue;
                    int lvl = magus.getLevelOf(skill);
                    if (lvl >= 10) return true;
                    if (lvl >= 4 && skill instanceof Abilities) return true;
                }
                return false;
            case WRITE_TRACTATUS:
                return !magus.getPossibleTractactusSubjects().isEmpty();
            case READ_BOOK:
                if (magus.getBestBookToRead() == null)
                    return false;
                return true;
            case COPY_BOOK:
                if (magus.isCopyingBook() && magus.getCurrentCopyProject().getBookBeingCopied().isInUse())
                    return false;
                if (magus.getSeasonsServiceOwed() > 0 && magus.getBestDisintegratingBookToCopy() != null)
                    return true;
                return (magus.isApprentice() && magus.getBestBookToCopy() != null);
            case TEACH_APPRENTICE:
                return magus.hasApprentice();
            case FOUND_COVENANT:
                if (magus.getCovenant() == null && !magus.isApprentice())
                    return true;
                return false;
            case JOIN_COVENANT:
                return (!magus.isApprentice());
            case DEVELOP_COVENANT:
                return (magus.getCovenant() != null);
            case TWILIGHT:
                return false;
        }
        return false;

    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Enum getEnum() {
        return this;
    }

}
