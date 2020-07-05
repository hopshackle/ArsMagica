package hopshackle.simulation.arsmagica;

import java.awt.event.*;
import java.util.*;

import hopshackle.simulation.*;

public class Tribunal extends Location {

    private int visLevel;
    private int populationLevel;
    private double recentApprentices;
    private int extantVisSources;
    private List<BarterOffer> marketOffers;
    private int lastTribunal;
    private int totalBookSales;
    private int totalLongevitySales;
    private EntityLog logger;
    private boolean fullDebug = false;

    public Tribunal(String name, World world) {
        super(world);
        setName(name);
        visLevel = Dice.roll(2, 15) + 15;
        populationLevel = Dice.roll(3, 15)  + 15 - visLevel;
        if (populationLevel < 0) populationLevel = 0;
        marketOffers = new ArrayList<>();
        lastTribunal = world.getYear();
        logger = new EntityLog("Tribunal of " + toString(), world.getCalendar());
        log("Tribunal Founded");

        ActionListener actionListener = arg0 -> {
            if (arg0.getActionCommand().equals("Death")) {
                log("Simulation terminated.");
                logger.close();
            }
        };
        world.addListener(actionListener);
    }

    public Tribunal() {
        super();
    }

    public int getVisLevel() {
        return visLevel;
    }

    public void setVisLevel(int visLevel) {
        this.visLevel = visLevel;
    }

    public int getPopulationLevel() {
        return populationLevel;
    }

    public void setPopulationLevel(int populationLevel) {
        this.populationLevel = populationLevel;
    }

    public int getApprenticeModifier() {
        // the higher the population, the better
        return (int) (populationLevel - recentApprentices) / 5;
    }

    public int getVisModifier() {
        return (visLevel - extantVisSources) / 5;
    }

    public void registerApprentice(Agent apprentice) {
        recentApprentices += 5;
    }

    @Override
    public void maintenance() {
        super.maintenance();
        recentApprentices *= 0.90;
        // refreshes over about 15 years
        extantVisSources = getAllChildLocationsOfType(AMU.sampleVisSource).size();
        if (world.getYear() >= lastTribunal + 7) {
            resolveMarket();
            lastTribunal = world.getYear();
        }
        if (world.getYear() % 10 == 0) {
            DatabaseWriter<Tribunal> tribunalWriter = world.getDBWriter(Tribunal.class);
            if (tribunalWriter != null)
                tribunalWriter.write(this, world.toString());
        }
    }

    public void addToMarket(BarterOffer offer) {
        marketOffers.add(offer);
    }

    public List<BarterOffer> getOffersOnMarket() {
        Collections.sort(marketOffers, new Comparator<BarterOffer>() {
            // sorts so that the Offers with lowest bids are first
            @Override
            public int compare(BarterOffer o1, BarterOffer o2) {
                return Double.compare(o1.getBestBid(), o2.getBestBid());
            }
        });
        return HopshackleUtilities.cloneList(marketOffers);
    }

    private void resolveMarket() {
        int totalEconomySize = 0;
        int bookSales = 0;
        int visSales = 0;
        int longevitySales = 0;
        for (BarterOffer offer : marketOffers) {

            double bestPrice = offer.getBestBid();
            if (bestPrice > 0.0) {
                if (fullDebug)
                    log("\tMarket: " + offer.getItem() + " offered by " + offer.getSeller() + " is purchased.");
                offer.resolve();
                totalEconomySize += bestPrice;
                if (offer.getItem() instanceof LongevityRitualService) {
                    longevitySales++;
                    totalLongevitySales++;
                } else if (offer.getItem() instanceof Book) {
                    totalBookSales++;
                    bookSales++;
                } else {
                    visSales += offer.getNumber();
                }
            } else {
                offer.withdraw();
            }
        }

        marketOffers.clear();
        if (totalEconomySize > 0)
            log(String.format("\t\tAt %d Tribunal, total of %d books, %d pawns of vis and %d longevity rituals sold for %d notional pawns.",
                    world.getYear(), bookSales, visSales, longevitySales, totalEconomySize));
    }

    public int getTotalBookSales() {
        return totalBookSales;
    }

    public int getTotalLongevitySales() {return totalLongevitySales;}

    public void log(String message) {
        logger.log(message);
    }

    public int getDateOfNextTribunal() {
        return lastTribunal + 7;
    }

    @Override
    public synchronized boolean addAgent(Agent a) {
        if (a instanceof Magus) {
            Magus m = (Magus) a;
            if (m.getAge() > 20 && !m.isDead() && m.getTribunal() != this)
                log(m.toString() + " arrives in Tribunal");
        }
        return super.addAgent(a);
    }

    @Override
    public synchronized boolean removeAgent(Agent a) {
        if (a instanceof Magus) {
            Magus m = (Magus) a;
            if (!m.isApprentice() && m.getAge() > 20) {
                String covenantString = "";
                if (m.getCovenant() != null)
                    covenantString = " of " + m.getCovenant().toString();
                if (!m.isDead() && m.getTribunal() != this)
                    log(m.toString() + covenantString + " leaves Tribunal");
                else if (m.isDead())
                    log(m.toString() + covenantString + " dies");
            }
        }
        return super.removeAgent(a);
    }
}
