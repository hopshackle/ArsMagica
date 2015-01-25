package hopshackle.simulation.arsmagica;

import java.util.List;

import hopshackle.simulation.*;

public class AMBarterOffer extends BarterOffer {

	public AMBarterOffer(Agent seller, Artefact item, double number, double reservePrice,
			ValuationFunction<List<Artefact>> valuationFunction) {
		super(seller, item, number, reservePrice, valuationFunction);
	}

	public void resolve() {
		Agent seller = getSeller();
		Artefact item = getItem();
		if (seller != null) {
			seller.removeItemFromThoseOnMarket(item);
			if (getBestBid() > 0.0) {
				List<Artefact> priceReceived = bestBid.getBarterItems();
				String message = String.format("Sells %s for %.2f %s", item.toString(), bestBid.getAmount(), AMU.prettyPrint(bestBid.getBarterItems()));
				if (numberOfItem > 1) 
					message = String.format("Sells %d %s for %.2f %s", (int)numberOfItem, item.toString(), bestBid.getAmount(), AMU.prettyPrint(bestBid.getBarterItems()));
				seller.log(message);
				for (Artefact a : priceReceived) 
					seller.addItem(a);
			}
		}
		if (bestBid != null && !bestBid.getBuyer().isDead()) {
			Agent buyer = bestBid.getBuyer();
			for (int i = 0; i < numberOfItem; i++) {
				buyer.addItem(item);
			}
			String message = String.format("Buys %s for %.2f %s", item.toString(), getBestBid(), AMU.prettyPrint(bestBid.getBarterItems()));
			if (numberOfItem > 1) 
				message = String.format("Buys %d %s for %.2f %s", (int)numberOfItem, item.toString(), getBestBid(), AMU.prettyPrint(bestBid.getBarterItems()));
			buyer.log(message);
			bestBid.resolve(true);
		}
		bestBid = null;
	}

}
