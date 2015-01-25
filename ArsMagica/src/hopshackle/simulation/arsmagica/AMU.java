package hopshackle.simulation.arsmagica;

import java.util.*;

import hopshackle.simulation.*;

public class AMU {

	public static final Vis sampleVis = new Vis(Arts.VIM);
	public static final Book sampleBook = new Summa(Arts.CREO, 0, 0, null);
	public static final Tribunal sampleTribunal = new Tribunal();
	public static final VisSource sampleVisSource = new VisSource(Arts.CREO, 1, null);
	public static final Artefact sampleLongevityRitualService = new LongevityRitualService(null);
	public static Covenant sampleCovenant = new Covenant(null, null);

	public static Learnable getPreferredXPGain(Arts technique, Arts form, Magus magus) {
		double techniqueLvl = magus.getLevelOf(technique);
		double formLvl = magus.getLevelOf(form);
		double theoryLvl = magus.getLevelOf(Abilities.MAGIC_THEORY) * 5.0;
		double highest = Math.max(Math.max(formLvl, techniqueLvl), theoryLvl);

		Map<Learnable, Double> options = new HashMap<Learnable, Double>();
		options.put(technique, highest - techniqueLvl + 1);
		options.put(form, highest - formLvl + 1);
		options.put(Abilities.MAGIC_THEORY, highest - theoryLvl + 1);

		return MagusPreferences.getPreferenceGivenPriors(magus, options);
	}

	public static HashMap<Arts, Integer> getVisInventory(Agent agent) {
		HashMap<Arts, Integer> visInventory = new HashMap<Arts, Integer>();
		for (Arts a : Arts.values()) {
			visInventory.put(a, 0);
		}
		if (agent == null) return visInventory;
		for (Artefact item : agent.getInventoryOf(sampleVis)) {
			Vis pawn = (Vis) item;
			Arts visType = pawn.getType();
			Integer currentLevel = visInventory.get(visType);
			visInventory.put(visType, currentLevel+1);
		}
		return visInventory;
	}

	public static int getHighestSummaFrom(Learnable skill, List<Book> books) {
		int highest = 0;
		for (Book book : books) {
			if (book.getSubject() == skill && book.getLevel() > highest) 
				highest = book.getLevel();
		}
		return highest;
	}

	public static int getSeasonsToMaxFrom(Learnable skill, List<Book> books) {
		List<Book> relevantBooks = Summa.filterRelevantSummae(books, skill);
		if (relevantBooks.isEmpty())
			return 0;
		Skill dummy = null;
		if (skill instanceof Arts)
			dummy = new Art(skill, 0, null);
		if (skill instanceof Abilities)
			dummy = new Ability(skill, 0, null);

		Collections.sort(relevantBooks, new Comparator<Book>() {

			@Override
			public int compare(Book b1, Book b2) {
				int diff = b2.getQuality() - b1.getQuality();
				if (diff == 0)
					diff = b2.getLevel() - b1.getLevel();
				return diff;
			}
		});

		int maxLevel = 0;
		for (Book b : relevantBooks) 
			if (b.getLevel() > maxLevel)
				maxLevel = b.getLevel();

		int currentBookIndex = 0;
		int seasons = 0;
		do {
			seasons++;
			
			//firstly determine which book we're reading
			for (int i = currentBookIndex; i < relevantBooks.size(); i++) {
				if (dummy.getLevel() < relevantBooks.get(i).getLevel()) {
					currentBookIndex = i;
					break;
				}
			}

			Book currentBook = relevantBooks.get(currentBookIndex);
			int maxTotalXP = skill.getXPForLevel(currentBook.getLevel());
			if (currentBook.getLevel() > dummy.getLevel()) {
				int currentBookXPGain = maxTotalXP - dummy.getTotalXP();
				if (currentBook.getQuality() <= currentBookXPGain) {
					dummy.addXP(currentBook.getQuality());
					continue;
				} 
				// so we may not want to read this book, but instead read the next highest quality book of a higher level
				int temp = currentBookIndex;
				boolean finished = false;
				do {
					temp++;
					if (temp >= relevantBooks.size())
						return seasons;	// we've finished - this is the last book
					if (relevantBooks.get(temp).getLevel() > currentBook.getLevel()) {
						if (relevantBooks.get(temp).getQuality() > currentBookXPGain) {
							dummy.addXP(relevantBooks.get(temp).getQuality());
						} else {
							dummy.addXP(maxTotalXP - dummy.getTotalXP());
						}
						// and in either case we are now done
						finished = true;
						currentBookIndex = temp;
					}
				} while (temp < relevantBooks.size()-1 && !finished);
				if (!finished) // then we just finish off the last book
					dummy.addXP(maxTotalXP - dummy.getTotalXP());
			}
		} while (dummy.getLevel() < maxLevel);

		return seasons;
	}

	public static String prettyPrint(List<Artefact> itemList) {
		// bit of a hack purely for Vis at the moment
		List<Vis> visList = new ArrayList<Vis>();
		for (Artefact item : itemList) {
			if (item instanceof Vis)
				visList.add((Vis)item);
		}
		if (!visList.isEmpty()) {
			Collections.sort(visList, new Comparator<Vis>() {

				@Override
				public int compare(Vis o1, Vis o2) {
					return o1.getType().getOrder() - o2.getType().getOrder();
				}
			});

			StringBuffer temp = new StringBuffer("(");
			Arts currentType = visList.get(0).getType();
			int currentCount = 0;
			for (Vis v : visList) {
				if (v.getType() == currentType) {
					currentCount++;
				} else {
					temp.append(currentType.getAbbreviation() + currentCount + " ");
					currentCount = 1;
					currentType = v.getType();
				}
			}
			temp.append(currentType.getAbbreviation() + currentCount + ")");
			return temp.toString();
		} else {
			return "";
		}
	}
}

