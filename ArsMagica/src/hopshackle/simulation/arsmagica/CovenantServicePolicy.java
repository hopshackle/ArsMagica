package hopshackle.simulation.arsmagica;

import java.util.*;

public class CovenantServicePolicy {
	
	private Covenant covenant;
	private int serviceRequirement;
	private HashMap<Long, Integer> magusLastServiceUpdate = new HashMap<Long, Integer>();

	public CovenantServicePolicy(Covenant covenant) {
		this.covenant = covenant;
		serviceRequirement = 0;
	}
	
	public void setServiceRequirement(int newPeriodBetweenService) {
		serviceRequirement = newPeriodBetweenService;
	}
	
	public int getServiceRequirement() {
		return serviceRequirement;
	}

	public void run() {
		int year = covenant.getWorld().getYear();
		if (serviceRequirement > 0) {
			List<Magus> currentMembers = covenant.getCurrentMembership();
			
			for (Magus member : currentMembers) {
				Long uniqueId = member.getUniqueID();
				int lastUpdate = 0; 
				if (magusLastServiceUpdate.containsKey(uniqueId))
					lastUpdate = magusLastServiceUpdate.get(uniqueId);
				if (lastUpdate + serviceRequirement < year) {
					member.setSeasonsServiceOwed(member.getSeasonsServiceOwed() + 1);
					magusLastServiceUpdate.put(uniqueId, year);
				}
			}
		}
	}

}
