package hopshackle.simulation.arsmagica;

import java.util.*;

import hopshackle.simulation.*;

public class TribunalDAO implements DAO<Tribunal> {

	@Override
	public String getTableCreationSQL(String tableSuffix) {
		return  "CREATE TABLE IF NOT EXISTS Tribunals_" + tableSuffix +
				" (name 		VARCHAR(50)		NOT NULL, "	+
				" currentYear		INT			NOT NULL,"		+
				" covenants		INT		NOT NULL,"	+
				" magi		INT		NOT NULL,"		+
				" apprentices	INT	NOT NULL,"		+
				" visMod		INT	NOT NULL," 		+
				" apprenticeMod		INT	NOT NULL,"		+
				" bookSales		INT	NOT NULL,"	+
				" longevityRitualSales	INT NOT NULL," +
				" visSources	INT	NOT NULL,"	+
				" visSupply		INT	NOT NULL"	+
				");";
	}

	@Override
	public String getTableUpdateSQL(String tableSuffix) {
		return "INSERT INTO Tribunals_" + tableSuffix + 
				" (name, currentYear, covenants, magi, apprentices, visMod, apprenticeMod, bookSales, longevityRitualSales, visSources, visSupply) VALUES";
	}

	@Override
	public String getValues(Tribunal tribunal) {
		List<Agent> allMagi = tribunal.getAgentsIncludingChildLocations();
		int totalMagi = 0, totalApprenti = 0;
		for (Agent a : allMagi) {
			if (a instanceof Magus) {
				Magus m = (Magus) a;
				if (m.isApprentice())
					totalApprenti++;
				else
					totalMagi++;
			}
		}
		Set<VisSource> visSources = tribunal.getAllChildLocationsOfType(AMU.sampleVisSource);
		int totalVisSupply = 0;
		for (VisSource vs : visSources) {
			totalVisSupply += vs.getAmountPerAnnum();
		}
		
		return String.format(" ('%s', %d, %d, %d, %d, %d, %d, %d, %d, %d, %s) ",
				tribunal.toString(),
				tribunal.getWorld().getYear(),
				tribunal.getAllChildLocationsOfType(AMU.sampleCovenant).size(),
				totalMagi,
				totalApprenti,
				tribunal.getVisModifier(),
				tribunal.getApprenticeModifier(),
				tribunal.getTotalBookSales(),
				tribunal.getTotalLongevitySales(),
				visSources.size(),
				totalVisSupply
				);
	}

	@Override
	public String getTableDeletionSQL(String tableSuffix) {
		return "DROP TABLE IF EXISTS Tribunals_" + tableSuffix + ";";
	}

}
