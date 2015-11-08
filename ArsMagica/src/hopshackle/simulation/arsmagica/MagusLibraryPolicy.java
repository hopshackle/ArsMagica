package hopshackle.simulation.arsmagica;

import java.util.*;

import hopshackle.simulation.*;

public class MagusLibraryPolicy {

	private Magus magus;

	public MagusLibraryPolicy(Magus magus) {
		this.magus = magus;
	}

	public void run() {
		if (magus.getSeasonsServiceOwed() > 0) {
			// may be able to donate books to library as service
			List<Book> library = magus.getInventoryOf(AMU.sampleBook);
			Covenant covenant = magus.getCovenant();
			if (covenant != null && !covenant.isLibraryFull()) {
				// only if there is space in the library
				List<Book> donation = new ArrayList<Book>();
				int totalValue = 0;
				for (Book b : library) {
					int value = covenant.calculateIncrementalBuildPointsFrom(b);
					if (value > 0) {
						donation.add(b);
						totalValue += value;
					}
				}
				// need 8 build points for a seasons service (equivalent to a Q8 Tractatus)
				if (totalValue >= 8) {
					for (Book b  : donation) {
						magus.removeItem(b);
						covenant.addItem(b);
						magus.log("Donates "+ b.toString() + " to covenant in lieu of service.");
					}
					for (int i = totalValue; i >= 8; i -= 8) {
						magus.doSeasonsService();
					}
				}
			}
		}

		Tribunal tribunal = magus.getTribunal();
		World world = magus.getWorld();

		if (world.getSeason() == 0)
			processBookLimit(10);
		// set limit of books one Magus can have and maintain

		int yearsUntilTribunal = tribunal.getDateOfNextTribunal() - world.getYear();
		if (yearsUntilTribunal <= 2) {
			List<Book> supercededBooks = getUnwantedBooks();
			for (Book book : supercededBooks) {
				int reservePrice = (int) ((book.getLevel() + book.getQuality()) * 0.2 * (1.0 - book.getDeterioration()));
				tribunal.addToMarket(new BarterOffer(magus, book, 1, Math.max(reservePrice, 1), new VisValuationFunction(magus)));
				if (magus.getFullDebug())
					magus.log("Puts " + book + " up for sale.");
			}

			List<BarterOffer> offers = tribunal.getOffersOnMarket();
			List<Book> currentlyAccessibleBooks = magus.getAllAccessibleBooks();
			List<LabText> currentlyAccessibleLabTexts = LabText.extractAllLabTextsFrom(currentlyAccessibleBooks);
			List<Vis> vis = getAvailableVisStocks();

			// we then put up for sale any significantly large vis amounts
			// using the fact that vis is already sorted into type order
			if (!vis.isEmpty()) {
				Arts currentType = vis.get(0).getType();
				int currentCount = 0;
				for (Vis v : vis) {
					if (v.getType() == currentType) {
						currentCount++;
					} else {
						checkSurplusVis(currentType, currentCount);
						currentType = v.getType();
						currentCount = 1;
					}
				}
				checkSurplusVis(currentType, currentCount);
			}

			// need to refresh this to just spending availability
			vis = getAvailableVisStocks();
			for (BarterOffer bo : offers) {
				if (magus.equals(bo.getSeller()))
					continue;
				if (magus.equals(bo.getCurrentWinner()))
					continue;

				double value = 0.0;
				double discountFactor = 1.0;
				double surplus = Math.max(0, vis.size() - 10);
				if (bo.getItem() instanceof Book) {
					Book b = (Book) bo.getItem();
					if (b instanceof Summa) {
						value = b.getSubject().getXPForLevel(b.getLevel()) - magus.getTotalXPIn(b.getSubject());
						value *= 0.20 * MagusPreferences.getResearchPreference(magus, b.getSubject());
						// i.e. a pawn of vis for 5 xp as a rough guideline
						for (Book accessibleBook : currentlyAccessibleBooks) {
							if (accessibleBook instanceof Summa) {
								if (accessibleBook.getSubject() == b.getSubject() 
										&& accessibleBook.getLevel() >= b.getLevel() 
										&& accessibleBook.getQuality() >= b.getQuality()) {
									value = 0;	// already has access to a book at least as good
								}
							}
						}
					}
					if (b instanceof Tractatus) {
						value = b.getXPGainForMagus(magus);
						value *= 0.25 * MagusPreferences.getResearchPreference(magus, b.getSubject());
						// i.e. a pawn of vis for 4 xp as a rough guideline
						for (Book accessibleBook : currentlyAccessibleBooks)
							if (accessibleBook.getTitleId() == b.getTitleId())
								value = 0;	// already has access to a copy of this Tractatus
					}
					if (b instanceof LabText) {
						Spell s = ((LabText)b).getSpell();
						if (!magus.getSpells().contains(s) && magus.getLabTotal(s) >= s.getLevel())
							value = b.getLevel() / 20.0;	// a pawn of vis for a lvl 20 lab text (one pays more for favourite Forms/Techniques)
						for (LabText accessibleBook : currentlyAccessibleLabTexts)
							if (accessibleBook.getSpell().equals(s))
								value = 0;	// already has access to a copy of this Lab Text
						value *= MagusPreferences.getResearchPreference(magus, s.getTechnique());
						value *= MagusPreferences.getResearchPreference(magus, s.getForm());
					}
					discountFactor = Math.sqrt(1.0 - b.getDeterioration());
				} else if (bo.getItem() instanceof LongevityRitualService) {
					int currentLongevityValue = magus.getLabTotal(Arts.CREO, Arts.CORPUS);
					for (Artefact a : magus.getInventoryOf(AMU.sampleLongevityRitualService)) {
						LongevityRitualService lrs = (LongevityRitualService) a;
						int thisValue = lrs.getLabTotal();
						if (thisValue > currentLongevityValue) currentLongevityValue = thisValue;
					}
					LongevityRitualService longevity = (LongevityRitualService) bo.getItem();
					value = 2.0 * (longevity.getLabTotal() - currentLongevityValue);
					// so about 10 pawns of vis per point of additional ritual
					if (value < 0.3 || longevity.getMagicTheory() <= (magus.getAge() / 10) + 1)
						value = 0.0;	// but set a limit given waste of season
				} else if (bo.getItem() instanceof Vis) {
					Arts visType = ((Vis)bo.getItem()).getType();
					double amount = bo.getNumber();
					value = MagusPreferences.getResearchPreference(magus, visType) * amount;
					surplus = 0.0;
				}
				value *= discountFactor;
				value *= (2.0 - Math.exp(-surplus / 30.0));		// if we have lots of Vis, then we can spend it like water.
				// 0-10 pawns means no change to value
				// 15 pawns *= 1.15
				// 20 pawns *= 1.28
				// 40 pawns *= 1.63
				if (value <= 0.1 || vis.isEmpty()) continue;

				BarterBid possibleBid = getMostEffectiveBidThatWillWin(bo, value, vis);

				if (possibleBid != null) {
					boolean bidAccepted = bo.submitBid(possibleBid);
					if (bidAccepted) {
						if (magus.getFullDebug())
							magus.log("Bid for " + bo.getItem());
					} else {
						vis = getAvailableVisStocks();
						// as we need to refresh our list after an unsuccessful bid
						// we could do this after a successful one too for completeness, as the acceptance of the bid will remove Vis from inventory
					}
				}
			} 
		}
	}

	private void checkSurplusVis(Arts currentType, int currentCount) {
		double pref = MagusPreferences.getResearchPreference(magus, currentType);
		int pawnsToKeep = 5;
		if (pawnsToKeep < currentCount) {
			// we put it up for auction
			int pawnsToSell = (currentCount - pawnsToKeep) / 2;
			do {
				double lotSize = Math.min(5.0, pawnsToSell);
				pawnsToSell -= lotSize;
				BarterOffer lot = new BarterOffer(magus, new Vis(currentType), lotSize, pref * lotSize * 1.01, new VisValuationFunction(magus));
				magus.getTribunal().addToMarket(lot);
			} while (pawnsToSell > 0);
		}
	}

	/*
	 * Runs through vis stocks, adding them to bid until we achieve a level that is sufficient to win
	 * We stop if the total value exceeds the value to us of the item(s)
	 * We take into account large vis supplies - these reduce the value to us of *any* type of vis (?)
	 * 
	 * We remove the used pawns from the vis List, so we don't bid them multiple times
	 */
	private BarterBid getMostEffectiveBidThatWillWin(BarterOffer bo, double value, List<Vis> vis) {
		List<Artefact> bid = new ArrayList<Artefact>();
		double reserveValue = bo.getReservePrice();
		double bidToBeat = bo.getBestBid();
		VisValuationFunction valueToBidder = new VisValuationFunction(magus);

		for (Vis nextPawn : vis) {
			bid.add(nextPawn);
			double newValue = bo.valueBid(bid);
			if (newValue < reserveValue || newValue <= bidToBeat)
				continue;
			double ourValue = valueToBidder.getValue(bid);
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

		return new BarterBid(magus, bid, bo);
	}

	/*
	 * Return vis, sorted in order of increasing value to the magus.
	 * Reserves are subtracted first, on the basis of:
	 * 		- sufficient Vis for a Longevity ritual
	 * 		- sufficient Vis for 2 seasons experimentation in any Art with a score of 10 or greater
	 */
	private List<Vis> getAvailableVisStocks() {
		Map<Arts, Integer> reserve = new HashMap<Arts, Integer>();
		for (Arts art : Arts.values()) {
			int requirement = 0;
			if (magus.getLevelOf(art) >= 10)
				requirement = (int) Math.ceil(magus.getLevelOf(art) / 2.5);
			reserve.put(art,  requirement);
		}
		for (Vis v : InventLongevityRitual.requirementsForRitual(magus)) {
			int req = reserve.get(v.getType());
			reserve.put(v.getType(), req+1);
		}

		List<Vis> retValue = new ArrayList<Vis>();
		for (Vis v : magus.getInventoryOf(AMU.sampleVis)) {
			int req = reserve.get(v.getType());
			if (req > 0) {
				req--;
				reserve.put(v.getType(), req);
			} else {
				retValue.add(v);
			}
		}
		Collections.sort(retValue, new Comparator<Vis>() {

			@Override
			public int compare(Vis v1, Vis v2) {
				// sorts in ascending order of preference
				double value1 = MagusPreferences.getResearchPreference(magus, v1.getType());
				double value2 = MagusPreferences.getResearchPreference(magus, v2.getType());
				return (int)((value1 - value2) * 1000.0);
			}
		});
		return retValue;
	}

	private List<Book> getUnwantedBooks() {
		List<Book> library = magus.getInventoryOf(AMU.sampleBook);
		List<Book> retValue = new ArrayList<Book>(); 
		for (Book book : library) {
			if (book instanceof Summa && magus.getLevelOf(book.getSubject()) >= book.getLevel())
				retValue.add(book);
			if (book instanceof Tractatus && book.getXPGainForMagus(magus) == 0)
				retValue.add(book);
			if (book instanceof LabText) {
				LabText lt = (LabText) book;
				if(magus.getSpells().contains(lt.getSpell()))
					retValue.add(book);
			}
		}
		return retValue;
	}

	public void processBookLimit(int limit) {
		List<Book> library = magus.getInventoryOf(AMU.sampleBook);
		Book.SortInOrderOfValue(library);
		int total = library.size();
		if (total > limit) {
			for (int i = limit; i < total; i++) {
				Book book = library.get(i);
				book.increaseDeterioration(2);
			}
		}
	}
}
