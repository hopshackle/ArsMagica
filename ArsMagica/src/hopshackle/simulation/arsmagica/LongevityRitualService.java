package hopshackle.simulation.arsmagica;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import hopshackle.simulation.*;

public class LongevityRitualService extends ArsMagicaItem implements ArtefactRequiringMaintenance {
    /*
     * Could amend this to be a general agreement between two Magi to assist each other.
     * The customer is the end-recipient, and responsible for triggering the service. However we leave
     * it open as to whether the customer or other Magus acts as the prime researcher - it will be whichever gives the
     * best lab total.
     * Then we still use this for the override. This does not however provide a mechanism for multiple Magi to
     * assist...but that is solved by amending the other Magus to be a list of other Magi! Namely, all the friends of the
     * customer.
     * Then when triggered, we determine how many Lab Assistants can be used - and the lead research Magus.
     *
     * Or - we leave as is, and compile list of friend on the fly - yup. That's clearly better. This means:
     *  - we need to have a lrs between each pair of friends
     *  - this needs to be non-inheritable. In fact; easier to make lrs just generically non-inheritable!
     *  - lab assistants then worked out on the fly
     *
     * At a future point we can add in a quid pro quo - being a general LabAssitant service that can be generically triggered
     * - possibly in the same way, which would require a bit of advance planning.
     */
    private Magus CrCoSpecialist;
    private Magus customer;
    boolean hasBeenPurchased = false;

    public LongevityRitualService(Magus serviceOfferer) {
        CrCoSpecialist = serviceOfferer;
        if (CrCoSpecialist != null)
            CrCoSpecialist.setLongevityAvailability(false);        // only offer one at a time
    }

    public int getLabTotal() {
        return Math.max(getLTSpec(), getLTCust());
    }

    private int getLTSpec() {
        if (CrCoSpecialist != null && customer != null)
            return CrCoSpecialist.getLabTotal(Arts.CREO, Arts.CORPUS, HopshackleUtilities.listFromInstances(customer, customer.getApprentice()));
        else
            return 0;
    }

    private int getLTCust() {
        if (customer != null && customer.getLevelOf(Abilities.MAGIC_THEORY) >= customer.getAge() / 10 + 1)
            return customer.getLabTotal(Arts.CREO, Arts.CORPUS, HopshackleUtilities.listFromInstances(CrCoSpecialist, CrCoSpecialist.getApprentice()));
        else return 0;

    }

    @Override
    public void artefactMaintenance(Agent purchaser) {
        if (!hasBeenPurchased) {
            hasBeenPurchased = true;
            if (CrCoSpecialist != null)
                CrCoSpecialist.setLongevityAvailability(true);
        }
        if (!(purchaser instanceof Magus) || purchaser == CrCoSpecialist || (customer != null && purchaser != customer)) {
            purchaser.removeItem(this);
            deleteThis();
            return;
        }
        customer = (Magus) purchaser;

        List<Artefact> allContracts = customer.getInventoryOf(AMU.sampleLongevityRitualService);
        LongevityRitualService ritual = null;
        int highest = 0;
        for (Artefact a : allContracts) {
            LongevityRitualService lrs = (LongevityRitualService) a;
            if (lrs.getLabTotal() > highest) {
                highest = lrs.getLabTotal();
                ritual = lrs;
            }
        }
        if (ritual != this)
            return;    // just use the best one if multiple options
        if (CrCoSpecialist.isDead()) {
            deleteThis();
        } else if (customer.getLongevityRitualEffect() < Math.floor(getLabTotal() / 5.0)
                && InventLongevityRitual.meetsRequirements(customer, CrCoSpecialist)) {
            // i.e. only use the contract if it will be of benefit and you have the vis

            if (customer.getActionPlan().contains(MagusActions.LONGEVITY_RITUAL))
                return;        // already scheduled

            Magus primeMagus = CrCoSpecialist;

            int numberOfAssistants = primeMagus.getLevelOf(Abilities.LEADERSHIP);    // assume Leadership is always specialised in lab work
            // one assistant is always the customer
            List<Magus> labAssistants = getBestAvailableAssistants();
            List<Magus> assistantsUsed = new ArrayList<Magus>();
            if (getLTCust() > getLTSpec()) {
                primeMagus = customer;
                assistantsUsed.add(CrCoSpecialist);
            }
            for (int a = 0; a < numberOfAssistants && a < labAssistants.size(); a++) {
                Magus otherAssistant = labAssistants.get(a);
                customer.log("Obtains assistance of " + otherAssistant + " for Longevity Ritual.");
                assistantsUsed.add(otherAssistant);
            }

            List<Magus> coreMagi = new ArrayList<Magus>();
            coreMagi.add(customer);
            coreMagi.add(primeMagus);
            int offsetSeasons = ActionPlan.getNextAvailableSlot(coreMagi) / 13;

            InventLongevityRitual action = new InventLongevityRitual(primeMagus, customer, assistantsUsed, offsetSeasons);
            action.addToAllPlans();
            if (!action.isDeleted() && CrCoSpecialist.getRelationshipWith(customer) != Relationship.FRIEND)
                deleteThis();
        } else {
            // leave to use for later...can even be inherited
        }
    }

    private List<Magus> getBestAvailableAssistants() {
        List<Magus> allAssistants = new ArrayList<Magus>();
        if (customer.getApprentice() != null) allAssistants.add(customer.getApprentice());
        if (CrCoSpecialist.getApprentice() != null) allAssistants.add(CrCoSpecialist.getApprentice());
        for (Agent r : customer.getRelationships().keySet()) {
            Magus m = (Magus) r;
            if (customer.getRelationshipWith(r) == Relationship.FRIEND && !m.isInTwilight() && !m.isApprentice()) {
                allAssistants.add(m);
            }
        }
        allAssistants.sort(new Comparator<Magus>() {

            @Override
            public int compare(Magus o1, Magus o2) {
                // positive if first > second provides ordering
                // Hence, since we want high value early in the ordering, we reverse this
                int score1 = o1.getLevelOf(Abilities.MAGIC_THEORY) + o1.getIntelligence();
                int score2 = o2.getLevelOf(Abilities.MAGIC_THEORY) + o2.getIntelligence();
                return score2 - score1;
            }
        });
        return allAssistants;
    }

    private void deleteThis() {
        if (customer != null)
            customer.removeItem(this);
        customer = null;
        CrCoSpecialist = null;
    }

    @Override
    public String toString() {
        return "Longevity Ritual Service offered by " + CrCoSpecialist;
    }

    @Override
    public boolean isInheritable() {
        return false;
    }

    public int getMagicTheory() {
        return CrCoSpecialist.getLevelOf(Abilities.MAGIC_THEORY);
    }

}
