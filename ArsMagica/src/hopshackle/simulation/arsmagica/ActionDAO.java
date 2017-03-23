package hopshackle.simulation.arsmagica;

import hopshackle.simulation.*;

public class ActionDAO implements DAO<ArsMagicaAction> {

	@Override
	public String getTableCreationSQL(String tableSuffix) {
		return  "CREATE TABLE IF NOT EXISTS Actions_" + tableSuffix +
				" (magusId 		INT		NOT NULL, "	+
				" year	 		INT		NOT NULL, "	+
				" season		INT		NOT NULL, " +
				" action 		VARCHAR(25)		NOT NULL, "	+
				" description	VARCHAR(100)		NOT NULL, " +
				" covenant		INT		NOT NULL, " +
				" age			INT		NOT NULL, " + 
				" apprentice 	BOOLEAN 	NOT NULL, " + 
				" service	 	BOOLEAN 	NOT NULL " + 
				");";
	}

	@Override
	public String getTableUpdateSQL(String tableSuffix) {
		return "INSERT INTO Actions_" + tableSuffix + 
				" (magusId, year, season, action, description, covenant, age, apprentice, service) VALUES";
	}

	@Override
	public String getValues(ArsMagicaAction action) {
		World w = action.getWorld();
		Magus m = (Magus) action.getActor();
		return String.format(" (%d, %d, %d, '%s', '%s', %d, %d, %b, %b) ",  
				m.getUniqueID(),
				w.getYear(),
				w.getSeason(),
				action.getClass().getName().replaceFirst("hopshackle.simulation.arsmagica.", ""),
				action.description(),
				(m.getCovenant() != null) ? m.getCovenant().getUniqueID() : 0,
				m.getAge(),
				m.isApprentice(),
				action.isCovenantService()
				);
	}

	@Override
	public String getTableDeletionSQL(String tableSuffix) {
		return "DROP TABLE IF EXISTS Actions_" + tableSuffix + ";";
	}

}
