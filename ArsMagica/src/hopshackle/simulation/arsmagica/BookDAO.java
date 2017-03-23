package hopshackle.simulation.arsmagica;

import hopshackle.simulation.*;

public class BookDAO implements DAO<Book> {
	@Override
	public String getTableCreationSQL(String tableSuffix) {
		return  "CREATE TABLE IF NOT EXISTS Books_" + tableSuffix +
				" (id 		INT		NOT NULL, "	+
				" titleId 		INT		NOT NULL, "	+
				" subject 		VARCHAR(20)		NOT NULL, "	+
				" title			VARCHAR(80)		NOT NULL, " +
				" author	INT		NOT NULL," 	+
				" currentYear	INT		NOT NULL, "	+
				" yearWritten		INT		NOT NULL,"		+
				" level		INT		NOT NULL,"	+
				" quality		INT		NOT NULL,"		+
				" popularity	INT		NOT NULL,"		+
				" deterioration	INT		NOT NULL,"		+
				" lastOwner		VARCHAR(70)	NOT NULL"	+
				");";
	}

	@Override
	public String getTableUpdateSQL(String tableSuffix) {
		return "INSERT INTO Books_" + tableSuffix + 
				" (id, titleId, subject, title, author, currentYear, yearWritten, level, quality, popularity, deterioration, lastOwner) VALUES";
	}

	@Override
	public String getValues(Book book) {
		
		String bookName = book.toString().substring(0, book.toString().indexOf("Written by"));
		if (bookName.length() > 80)
			bookName = bookName.substring(0, 80);
	
		return String.format(" (%d, %d, '%s', '%s', %d, %d, %d, %d, %d, %d, %.2f, '%s') ",  
				book.getID(),
				book.getTitleId(),
				book.getSubject().toString(),
				bookName,
				book.getAuthorId(),
				book.getWorld().getYear(),
				book.getYearWritten(),
				book.getLevel(),
				book.getQuality(),
				book.getSeasonsRead(),
				book.getDeterioration() * 100,
				book.getLastOwner()
				);
	}

	@Override
	public String getTableDeletionSQL(String tableSuffix) {
		return "DROP TABLE IF EXISTS Books_" + tableSuffix + ";";
	}
}
