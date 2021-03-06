package hopshackle.simulation.arsmagica;

import hopshackle.simulation.HopshackleUtilities;

import java.util.*;

public class InventLongevityRitual extends ArsMagicaAction {

    private Magus customer;
    private int modifier;

    public InventLongevityRitual(Magus a) {
        super(MagusActions.LONGEVITY_RITUAL, a);
        if (a.hasApprentice()) optionalActors.add(a.getApprentice());
    }

    public InventLongevityRitual(Magus crCoSpecialist, Magus customer, List<Magus> assistants, int offset) {
        // when one Magus invents a ritual for another
        super(MagusActions.LONGEVITY_RITUAL,
                HopshackleUtilities.listFromInstances(crCoSpecialist, customer),
                assistants, offset, 1);
        if (crCoSpecialist != customer)
            this.customer = customer;
    }

    @Override
    protected void initialisation() {
        Magus subject = customer;
        if (subject == null) subject = magus;

        if (meetsRequirements(subject, magus)) {
            for (Vis v : requirementsForRitual(subject))
                subject.removeItem(v);
        } else {
            magus.log("Insufficient vis for Longevity Ritual");
            if (customer != null)
                customer.log("Insufficient vis for Longevity Rutual");
            cancel();    // if insufficient vis, then shoudl still be time for participant to do something else instead
        }
    }

    @Override
    protected void doStuff() {
        Magus subject = customer;
        if (subject == null) subject = magus;
        List<Magus> allAssistants = getAllConfirmedParticipants();
        allAssistants.remove(magus);
        int labTotal = magus.getLabTotal(Arts.CREO, Arts.CORPUS, allAssistants);
        modifier = (int) Math.ceil(labTotal / 5.0);
        subject.setLongevityRitualEffect(modifier);
        subject.setKnownLongevityEffect(Math.max(subject.getKnownLongevityEffect(), modifier));
        if (customer == null) {
            magus.log("Invents a Longevity Ritual of potency " + modifier);
            for (Magus assistant : allAssistants) {
                assistant.log("Lab assistant in creation of longevity ritual by " + magus);
            }
        } else {
            magus.log("Invents a Longevity Ritual of potency " + modifier
                    + " for " + customer);
            customer.log("Receives Longevity Ritual of potency " + modifier
                    + " from " + magus);
            allAssistants.remove(customer);
            for (Magus assistant : allAssistants) {
                assistant.log("Lab assistant in creation of longevity ritual for " + customer + " by " + magus);
            }
        }
        exposureXPForParticipants(Arts.CREO, Arts.CORPUS, 2);
    }

    public static boolean meetsRequirements(Magus subject, Magus caster) {
        return caster.getLevelOf(Abilities.MAGIC_THEORY) >= requiredMagicTheory(subject.getAge()) &&
                meetsVisRequirements(subject) && !caster.isInTwilight();
    }

    public static boolean meetsVisRequirements(Magus magus) {
        return requirementsForRitual(magus).size() >= pawnsNeededForRitual(magus);
    }

    private static int requiredMagicTheory(int ageOfRecipient) {
        return (int) Math.ceil(Math.ceil(ageOfRecipient / 5.0) / 2.0);
    }

    public static int pawnsNeededForRitual(Magus magus) {
        return (int) Math.ceil(magus.getAge() / 5.0);
    }

    public static List<Vis> requirementsForRitual(Magus visPayer) {
        List<Vis> retValue = new ArrayList<>();
        int pawnsRequired = pawnsNeededForRitual(visPayer);
        int creoPawns = visPayer.getPawnsOf(Arts.CREO);
        int vimPawns = visPayer.getPawnsOf(Arts.VIM);
        int corpusPawns = visPayer.getPawnsOf(Arts.CORPUS);
        int pawnsSpent = 0;
        int vim = 0, creo = 0, corpus = 0;
        do {
            if (vimPawns > 0 && vimPawns >= creoPawns
                    && vimPawns >= corpusPawns) {
                vim++;
                vimPawns--;
                pawnsSpent++;
                continue;
            }
            if (corpusPawns > 0 && corpusPawns >= creoPawns) {
                corpus++;
                corpusPawns--;
                pawnsSpent++;
                continue;
            }
            if (creoPawns > 0) {
                creo++;
                creoPawns--;
                pawnsSpent++;
                continue;
            }
            pawnsRequired = 0; // no more vis left
        } while (pawnsRequired > pawnsSpent);

        for (int i = 0; i < creo; i++)
            retValue.add(new Vis(Arts.CREO));
        for (int i = 0; i < corpus; i++)
            retValue.add(new Vis(Arts.CORPUS));
        for (int i = 0; i < vim; i++)
            retValue.add(new Vis(Arts.VIM));

        return retValue;
    }

    public String description() {
        return "Modifier of "
                + modifier
                + " for "
                + ((customer == actor || customer == null) ? "Self" : customer
                .toString());
    }

}
