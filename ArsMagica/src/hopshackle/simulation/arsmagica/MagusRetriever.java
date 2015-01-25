package hopshackle.simulation.arsmagica;

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

import hopshackle.simulation.*;

public class MagusRetriever implements AgentRetriever<Magus> {

	protected static Logger logger = Logger.getLogger("hopshackle.simulation");
	private Connection con;
	
	@Override
	public Magus getAgent(long uniqueID, String tableSuffix, World world) {

		Magus retValue = null;
		try {
			String agentTable = "Magi_" + tableSuffix;
			if (con == null)
				openConnection();
			Statement st = con.createStatement();

			ResultSet rs;
			rs = st.executeQuery("SELECT * FROM " + agentTable + " WHERE id = " + uniqueID + " ORDER BY CurrentYear DESC;");
			if (!rs.first()) return null;
			long birth = rs.getInt("birth") * 52;
			long age = rs.getLong("age") * 52;
			long death = birth + age;
			long parens = rs.getLong("parens");
			String name = rs.getString("name");
			name = name.split("filius")[0].trim();
			String childrenIds = rs.getString("apprenticeIds");
			String[] childIdArray = childrenIds.split(",");
			List<Long> children = new ArrayList<Long>();
			for (String childID : childIdArray) {
				try {
				children.add(Long.valueOf(childID));
				} catch (NumberFormatException e) {
					// just move on
				}
			}
			
			retValue = new Magus(name, world, uniqueID, parens, children);
			retValue.setBirth(birth);
			retValue.setDeath(death);

			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
			logger.severe(e.toString());
		}
		
		return retValue;
	}

	@Override
	public void openConnection() {
		closeConnection();
		con = ConnectionFactory.getConnection();
	}
	
	@Override
	public void closeConnection() {
		if (con != null)
			try {
				con.close();
			} catch (SQLException e) {
				logger.severe(e.toString());
				e.printStackTrace();
			}
	}

}
