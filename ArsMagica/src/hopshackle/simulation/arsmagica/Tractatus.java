package hopshackle.simulation.arsmagica;

public class Tractatus extends Book {
	
	private int cardinalNumber;
	
	public Tractatus(Tractatus copyOf) {
		super(copyOf);
		cardinalNumber = copyOf.cardinalNumber;
	}
	public Tractatus(Learnable skill, Magus author) {
		super(skill, 0, 6 + author.getCommunication(), author);
		cardinalNumber = author.getNumberOfTractatusWritten(skill) + 1;
		author.writesTractatusIn(skill);
	}

	@Override
	public int getXPGainForMagus(Magus magus) {
		if (magus.getUniqueID() == getAuthorId())
			return 0;
		if (magus.hasReadTractatus(getTitleId()))
			return 0;
		return getQuality();
	}

	@Override
	public int getBPValue() {
		return getQuality();
	}

	@Override
	public Book createCopy() {
		return new Tractatus(this);
	}
	
	@Override
	public String toString() {
		return String.format("Tractatus %d on %s - Q%d. Written by %s in %d %s", 
				cardinalNumber, 
				getSubject() == null ? "NULL" : getSubject().toString(),
				getQuality(), 
				getAuthor(), 
				getYearWritten(), 
				getDeteriorationString()); 
	}

}
