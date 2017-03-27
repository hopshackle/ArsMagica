package hopshackle.simulation.arsmagica;

import hopshackle.simulation.*;

import java.util.*;

public class CovenantLibraryPolicy {

	private Covenant covenant;
	private double needForLibraryMaintenance;
	private double needForLabTextMaintenance;
	private boolean libraryFull;

	public CovenantLibraryPolicy(Covenant covenant) {
		this.covenant = covenant;
	}

	public void run() {
		Tribunal localTribunal = covenant.getTribunal();
		checkTribunalMarket(localTribunal);
		double membership = covenant.getCurrentSize();
		for (Location loc : covenant.getTribunal().getAccessibleLocations()) {
			if (loc instanceof Tribunal) {
				Tribunal neighbouringTribunal = (Tribunal) loc;
				if (Math.random() < 0.05 * membership) 
					checkTribunalMarket(neighbouringTribunal);
			}
		}

		needForLibraryMaintenance = levelOfLibraryDeterioration();
		needForLabTextMaintenance = levelOfLabTextDeterioration();
	}

	private void checkTribunalMarket(Tribunal tribunal) {
		List<Book> library = covenant.getCovenantAgent().getInventoryOf(AMU.sampleBook);

		int yearsUntilTribunal = tribunal.getDateOfNextTribunal() - covenant.getWorld().getYear();
		if (tribunal == covenant.getTribunal() && yearsUntilTribunal == 2) {	// only put Books up for Sale in local tribunal
			Set<Book> supercededBooks = getSupercededBooks(library);
			for (Book book : supercededBooks) {
				tribunal.addToMarket(new BarterOffer(covenant.getCovenantAgent(), book, 1, 1, new VisValuationFunction(covenant.getCovenantAgent())));
				if (covenant.getFullDebug())
					covenant.log("Puts " + book + " up for sale.");
			}
		}

		if ((yearsUntilTribunal == 1 || yearsUntilTribunal == 2) && covenant.getCovenantAgent().getInventoryOf(AMU.sampleVis).size() > 1) {
			List<BarterOffer> offers = tribunal.getOffersOnMarket();
			List<Vis> visStores = covenant.getCovenantAgent().getInventoryOf(AMU.sampleVis);
			for (BarterOffer bo : offers) {
				if (bo.getSeller().equals(covenant.getCovenantAgent()))
					return;
				if (bo.getCurrentWinner() != null && bo.getCurrentWinner().equals(covenant.getCovenantAgent()))
					return;
				if (bo.getItem() instanceof Book) {
					Book b = (Book) bo.getItem();
					int value = covenant.calculateIncrementalLibraryPointsFrom(b) * 2;

					BarterBid possibleBid = getMostEffectiveBidThatWillWin(bo, value, visStores);

					if (possibleBid != null) {
						boolean bidAccepted = bo.submitBid(possibleBid);
						if (bidAccepted) {
							if (covenant.getFullDebug())
								covenant.log("Bid for " + bo.getItem());
						}
					}
				}
			}
		}
	}

	private BarterBid getMostEffectiveBidThatWillWin(BarterOffer bo, double value, List<Vis> vis) {
		List<Artefact> bid = new ArrayList<Artefact>();
		double reserveValue = bo.getReservePrice();
		double bidToBeat = bo.getBestBid();

		for (Vis nextPawn : vis) {
			bid.add(nextPawn);
			double newValue = bo.valueBid(bid);
			if (newValue < reserveValue || newValue <= bidToBeat)
				continue;
			double ourValue = bid.size();
			if (ourValue > value)	{
				// more than we wish to bid
				bid.remove(nextPawn);
			}
			break;
			// so we stop once we have beaten the previous bid (or equalled the reserve Price)
		}
		// remove the bid from the available stock
		for (Artefact pawnUsed : bid) {
			vis.remove((Vis)pawnUsed);
		}

		return new BarterBid(covenant.getCovenantAgent(), bid, bo);
	}

	private double levelOfLibraryDeterioration() {
		// We look for any highly deteriorated book that has been read alot, and for which we do not have a non-deteriorated copy
		List<Book> fullLibrary = covenant.getCovenantAgent().getInventoryOf(AMU.sampleBook);
		Set<Book> supercededBooks = getSupercededBooks(fullLibrary);
		double problemLevel = 0.0;
		for (Book b : fullLibrary) {
			if (b instanceof LabText)
				continue;
			if (supercededBooks.contains(b))
				continue;
			problemLevel += Math.max(0.0, Math.pow(b.getDeterioration(),2) * b.getPopularity() - 10);
			// this means a book that has been read 20 times, and is 71% deteriorated will not trigger a problem (the cusp)
			// a book that has been read 50 times, and is 0.95 deteriorated will give a value of 35.125
		}

		return problemLevel;
	}
	private double levelOfLabTextDeterioration() {
		// We look for any highly deteriorated book that has been read alot, and for which we do not have a non-deteriorated copy
		List<Book> fullLibrary = covenant.getCovenantAgent().getInventoryOf(AMU.sampleBook);
		Set<Book> supercededBooks = getSupercededBooks(fullLibrary);
		double problemLevel = 0.0;
		for (Book b : fullLibrary) {
			if (!(b instanceof LabText))
				continue;
			if (supercededBooks.contains(b))
				continue;
			problemLevel += Math.max(0.0, Math.pow(b.getDeterioration(),2) * b.getPopularity() * 2 - 10);	
			// this means a text that has been read 10 times, and is 71% deteriorated will not trigger a problem (the cusp)
			// a book that has been read 25 times, and is 0.95 deteriorated will give a value of 35.125
		}

		return problemLevel;
	}
	public double getNeedForLibraryMaintenance() {
		return needForLibraryMaintenance;
	}
	public double getNeedForLabTextMaintenance() {
		return needForLabTextMaintenance;
	}

	public Set<Book> getSupercededBooks(List<Book> library) {
		Set<Book> retValue = new HashSet<Book>();

		for (Book book1 : library) {
			for (Book book2 : library) {
				if (book1.getClass().equals(book2.getClass()) && !(book1 == book2) && !book2.isInUse()) {
					if (book1 instanceof LabText) {
						Spell s1 = ((LabText)book1).getSpell();
						Spell s2 = ((LabText)book2).getSpell();
						if (s1.equals(s2)) {
							if (book2.getDeterioration() >= book1.getDeterioration()) {
								if (!retValue.contains(book1))
									retValue.add(book2);
							}
						}
					} else if (book1.getSubject() == book2.getSubject()) {
						// comparable: decide if Book2 is superseded
						if (book2 instanceof Summa && ((book2.getLevel() < book1.getLevel() && book2.getQuality() < book1.getQuality())
								|| (book2.getLevel() == book1.getLevel() && book2.getQuality() < book1.getQuality()) 
								|| (book2.getLevel() < book1.getLevel() && book2.getQuality() == book1.getQuality())
								|| (book2.getLevel() == book1.getLevel() && book2.getQuality() == book1.getQuality() && book2.getDeterioration() >= book1.getDeterioration())))
							retValue.add(book2);
						if (book2.getTitleId() == book1.getTitleId() && book2.getDeterioration() >= book1.getDeterioration()) {
							if (!retValue.contains(book1))
								retValue.add(book2);
						}
					}
				}
			}
		}
		return retValue;
	}

	public void processBookLimit(int limit) {
		libraryFull = false;
		List<Book> library = covenant.getCovenantAgent().getInventoryOf(AMU.sampleBook);
		try {
			Book.SortInOrderOfValue(library);
		} catch (Exception e) {
			System.out.println(e.toString());
			System.out.println(covenant.toString());
			for (Book b : library) {
				String message = String.format("%s, BP: %d, TitleId: %d, Det: %.2f", b.toString(), b.getBPValue(), b.getTitleId(), b.getDeterioration());
				System.out.println(message);
			}
			throw new AssertionError("Problem");
		}
		int total = library.size();
		if (total > limit) {
			libraryFull = true;
			for (int i = limit; i < total; i++) {
				Book book = library.get(i);
				book.increaseDeterioration(2);
			}
		}
	}
	public boolean isLibraryFull() {
		return libraryFull;
	}
}
