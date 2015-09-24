package hopshackle.simulation.arsmagica;

import hopshackle.simulation.*;

public class ReadBook extends ArsMagicaAction {

	private Book book;
	
	public ReadBook(Agent a, Book book) {
		super(a);
		this.book = book;
		this.book.setCurrentReader(magus);
	}
	
	protected void doStuff() {
		magus.log("Reads " + book);
		magus.addXP(book.getSubject(), book.getXPGainForMagus(magus));
		book.isReadBy(magus);
		if (book instanceof Tractatus)
			magus.setTractatusAsRead(book.getTitleId());
		book.setCurrentReader(null);
	}
	
	@Override
	protected void delete() {
		book.setCurrentReader(null);
	}
	
	public String description() {
		return book.toString();
	}
}
