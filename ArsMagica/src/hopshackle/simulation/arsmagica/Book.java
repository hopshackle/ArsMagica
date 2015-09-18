package hopshackle.simulation.arsmagica;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import hopshackle.simulation.*;

public abstract class Book extends ArsMagicaItem implements ArtefactRequiringMaintenance, Persistent {

	private int level;
	private int quality;
	private Learnable skill;
	private int authorId;
	private String author;
	private Agent currentReader;
	private int yearWritten;
	private int deterioration;
	private int lastMaintained;
	private int id, titleId;
	private int seasonsRead;
	private static AtomicInteger idFountain = new AtomicInteger(0);
	private static AtomicInteger titleIdFountain = new AtomicInteger(0);
	private static AgentWriter<Book> bookWriter = new AgentWriter<Book>(new BookDAO());
	private World world;
	private String lastOwner;

	public Book(Book copyOf) {
		level = copyOf.level;
		quality = copyOf.quality;
		skill = copyOf.skill;
		authorId = copyOf.authorId;
		author = copyOf.author;
		yearWritten = copyOf.yearWritten;
		deterioration = 0;
		lastMaintained = -1;
		titleId = copyOf.titleId;
		world = copyOf.world;
		id = idFountain.incrementAndGet();
	}

	public Book(Learnable skill, int level, int quality, Agent author) {
		this(skill, level, quality, author, false);
	}

	public Book(Learnable skill, int level, int quality, Agent author, boolean temporary) {
		this.skill = skill;
		this.level = level;
		this.quality = quality;
		if (author != null) {
			this.authorId = (int) author.getUniqueID();
			this.author = author.toString();
			yearWritten = author.getWorld().getYear();
			lastMaintained = yearWritten;
			world = author.getWorld();
		} else {
			this.author = "Anonymous";
			lastMaintained = -1;
		}
		if (!temporary) {
			id = idFountain.incrementAndGet();
			titleId = titleIdFountain.incrementAndGet();
		} 
	}

	public void giveToRecipient(Magus creator, boolean service) {
		if (creator.isApprentice())
			creator.getParens().addItem(this);
		else if (service) {
			Covenant cov = creator.getCovenant();
			if (cov != null)
				cov.addItem(this);
			else
				creator.addItem(this);
		} else
			creator.addItem(this);
	}

	public int getLevel() {
		return level;
	}

	public int getQuality() {
		return quality;
	}

	public Learnable getSubject() {
		return skill;
	}

	public String getAuthor() {
		return author;
	}
	public int getAuthorId() {
		return authorId;
	}
	public int getYearWritten() {
		return yearWritten;
	}
	public String getLastOwner() {
		return lastOwner;
	}

	public Agent getCurrentReader() {
		return currentReader;
	}

	public void setCurrentReader(Agent reader) {
		currentReader = reader;
	}

	public boolean isInUse() {
		return currentReader != null;
	}

	public abstract int getXPGainForMagus(Magus magus);

	@Override
	public boolean isA(Artefact item) {
		if (item instanceof Book)
			return true;
		return false;
	}


	public String getDeteriorationString() {
		String ageString = "";
		if (deterioration > 150)
			ageString = "(Worn)";
		if (deterioration > 250) 
			ageString = "(Disintegrating)";
		return ageString;
	}

	public void isReadBy(Agent reader) {
		deterioration++;
		seasonsRead++;
	}
	public void isCopiedBy(Agent copier) {
		deterioration++;
	}

	@Override
	public void artefactMaintenance(Agent owner) {
		int year = owner.getWorld().getYear();
		lastOwner = owner.toString();
		if (lastMaintained == -1)
			lastMaintained = year;
		if (year > lastMaintained) {
			deterioration += year - lastMaintained;
			lastMaintained = year;
			if (deterioration > 300) {
				owner.log(this + " finally disintegrates");
				owner.removeItem(this);
			}
			if (year % 10 == 0 && world != null) {
				bookWriter.write(this, world.toString());
			}
		}
	}

	public int getID() {
		return id;
	}

	public int getTitleId() {
		return titleId;
	}

	public double getDeterioration() {
		return deterioration / 300.0;
	}

	public void increaseDeterioration(int amount) {
		deterioration += amount;
	}

	public abstract int getBPValue();

	public int getPopularity() {
		return seasonsRead;
	}

	@Override
	public World getWorld() {
		return world;
	}

	public abstract Book createCopy();

	public static void SortInOrderOfValue(List<Book> listToSort) {
		Collections.sort(listToSort, new Comparator<Book>() {
			@Override
			public int compare(Book b1, Book b2) {
				int retValue =  b2.getBPValue() - b1.getBPValue();
				if (retValue == 0) 
					retValue = b1.getTitleId() - b2.getTitleId();
				if (retValue == 0) {
					retValue = (int)((b1.getDeterioration() - b2.getDeterioration()) * 100 + 0.5);
				}
				return retValue;
			}
		});
	}

}
