package hopshackle.simulation.arsmagica;

public class CopyBook extends ArsMagicaAction {

	private int pointsSoFar;
	private Book bookToCopy;
	private boolean isCovenantService;

	public CopyBook(Magus magus) {
		super(MagusActions.COPY_BOOK, magus);
		if (magus.isApprentice())
			bookToCopy = magus.getBestBookToCopy();
		else 
			bookToCopy = magus.getBestDisintegratingBookToCopy();
		magus.setCurrentCopyProject(this);

	}

	public CopyBook(CopyBook project) {
		super(MagusActions.COPY_BOOK, project.getActor());
		this.pointsSoFar = project.pointsSoFar;
		this.bookToCopy = project.bookToCopy;
		magus.setCurrentCopyProject(this);
		this.isCovenantService = project.isCovenantService;
	}

	public Book getBookBeingCopied() {
		return bookToCopy;
	}

	@Override
	public void initialisation() {
		if (bookToCopy.isInUse() && bookToCopy.getCurrentReader() != magus) {
			magus.log("Cannot copy " + bookToCopy + " because it is in use by " + bookToCopy.getCurrentReader());
			cancel();
		} else {
			bookToCopy.setCurrentReader(magus);
			isCovenantService = magus.getSeasonsServiceOwed() > 0;
		}
	}

	@Override
	public void doStuff() {
		int pointsInSeason = magus.getLevelOf(Abilities.SCRIBE) + 6;
		pointsSoFar += pointsInSeason;
		bookToCopy.isCopiedBy(magus);
		if (pointsSoFar == pointsInSeason && pointsSoFar < bookToCopy.getLevel())
			magus.log("Starts work on copying " + bookToCopy);
		else if (pointsSoFar < bookToCopy.getLevel())
			magus.log("Continues to copy " + bookToCopy);
		else {
			if (pointsSoFar == pointsInSeason)
				magus.log("Copies " + bookToCopy);
			else
				magus.log("Completes copying of " + bookToCopy);
			Book copy = bookToCopy.createCopy();
			copy.giveToRecipient(magus, isCovenantService);

			magus.setCurrentCopyProject(null);
		}
		magus.addXP(Abilities.SCRIBE, 2);
		if (isCovenantService)
			magus.doSeasonsService();
		bookToCopy.setCurrentReader(null);		// and release this in case we do not continue work  next season
	}

	public boolean equals(Object other) {
		if (other instanceof CopyBook) {
			CopyBook cb = (CopyBook) other;
			if (cb.bookToCopy.getTitleId() == bookToCopy.getTitleId())
				return true;
		}
		return false;
	}

	public void delete() {
		bookToCopy.setCurrentReader(null);
	}

	public String description() {
		return "Copies " + bookToCopy.toString();
	}

	public boolean isCovenantService() {
		return isCovenantService;
	}
}
