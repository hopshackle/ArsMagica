package hopshackle.simulation.arsmagica;

import hopshackle.simulation.AgentDAO;

public class CovenantDAO implements AgentDAO<Covenant> {

	@Override
	public String getTableCreationSQL(String tableSuffix) {
		return  "CREATE TABLE IF NOT EXISTS Covenants_" + tableSuffix +
				" ( id 		INT			NOT NULL,"		+
				" name 		VARCHAR(50)		NOT NULL, "	+
				" tribunal	VARCHAR(50)		NOT NULL, " +
				" founder	INT		NOT NULL, " +
				" founded		INT			NOT NULL,"		+
				" currentYear		INT			NOT NULL,"		+
				" aura		INT		NOT NULL,"	+
				" magi		INT		NOT NULL,"		+
				" books		INT		NOT NULL," +
				" annualVis	INT		NOT NULL, " +
				" wealth	INT		NOT NULL," +
				" grogs	INT		NOT NULL," +
				" mundane	INT		NOT NULL," +
				" service INT		NOT NULL," +
				" buildPoints	INT	NOT NULL" +
				");";
	}

	@Override
	public String getTableUpdateSQL(String tableSuffix) {
		return "INSERT INTO Covenants_" + tableSuffix + 
				" (id,  name, tribunal, founder, founded, currentYear, aura, magi, books, annualVis, wealth, grogs, mundane, service, buildPoints) VALUES";
	}

	@Override
	public String getValuesForAgent(Covenant covenant) {
		return String.format(" (%d, '%s', '%s', %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d) ",  
				covenant.getUniqueID(),
				covenant.getName(),
				covenant.getTribunal().toString(),
				covenant.getFounder(),
				covenant.getYearFounded(),
				covenant.getWorld().getYear(),
				covenant.getAura(),
				covenant.getCurrentSize(),
				covenant.getCovenantAgent().getInventoryOf(AMU.sampleBook).size(),
				covenant.getAnnualVisSupply(),
				covenant.getLevelOf(CovenantAttributes.WEALTH),
				covenant.getLevelOf(CovenantAttributes.GROGS),
				covenant.getLevelOf(CovenantAttributes.MUNDANE_CONNECTIONS),
				covenant.getServiceLevel(),
				covenant.getBuildPoints()
				);
	}

	@Override
	public String getTableDeletionSQL(String tableSuffix) {
		return "DROP TABLE IF EXISTS Covenants_" + tableSuffix + ";";
	}

}
