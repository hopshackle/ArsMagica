package hopshackle.simulation.arsmagica;

import java.util.*;

import hopshackle.simulation.Agent;

public class Summa extends Book {

	public Summa(Summa copyOf) {
		super(copyOf);
	}

	public Summa(Learnable skill, int level, int quality, Agent author) {
		super(skill, level, quality, author);
	}
	
	public Summa(Learnable skill, int level, int quality, Agent author, boolean temporary) {
		super(skill, level, quality, author, temporary);
	}

	@Override
	public int getXPGainForMagus(Magus magus) {
		int currentXPOfMagus = magus.getTotalXPIn(getSubject());
		int maxTotalXP = getSubject().getXPForLevel(getLevel());
		maxTotalXP = Math.max(maxTotalXP, 0);
		return Math.min(getQuality(), maxTotalXP - currentXPOfMagus);
	}
	
	@Override
	public int getBPValue() {
			Learnable bookSubject = getSubject();
			int retValue = 0;
			if (bookSubject instanceof Abilities) {
				retValue += 3 * getLevel() + getQuality();
			} else if (bookSubject instanceof Arts) {
				retValue += getLevel() + getQuality();
			}
			return retValue;
		}
	
	@Override
	public String toString() {
		return String.format("Summa on %s - lvl: %d, Q%d. Written by %s in %d %s", 
				getSubject().toString(), getLevel(), getQuality(), getAuthor(), getYearWritten(), getDeteriorationString()); 
	}

	@Override
	public Book createCopy() {
		return new Summa(this);
	}
	
	public static List<Book> filterRelevantSummae(List<Book> library, Learnable skill) {
		List<Book> relevantBooks = new ArrayList<Book>();
		for (Book book : library) {
			if (book.getSubject() == skill && book instanceof Summa)
				relevantBooks.add(book);
		}
		return relevantBooks;
	}
}
