package hopshackle.simulation.arsmagica;

import hopshackle.simulation.*;
import java.util.*;

public class CovenantVisPolicy {
	
	private Covenant covenant;
	private static Vis sampleVis = new Vis(Arts.VIM);
	
	public CovenantVisPolicy(Covenant covenant) {
		this.covenant= covenant;
	}
	
	public void run() {
		Agent covenantAgent = covenant.getCovenantAgent();
		List<Vis> visStores = covenantAgent.getInventoryOf(sampleVis);
		
		List<Magus> recipients = covenant.getCurrentMembership();
		int totalRecipients = recipients.size();
		int currentRecipient = 0;
		if (totalRecipients == 0) return;
		int reserve = 2 * Math.max(covenant.getAnnualVisSupply(), covenant.getCurrentSize());
		int pawnsEach = (visStores.size()-reserve) / totalRecipients;
		if (pawnsEach < 1) return;
		
		covenant.log("Vis distribution occurs of " + pawnsEach + " per magus.");
		for (Vis item : visStores) {
			recipients.get(currentRecipient).addItem(item);
			covenantAgent.removeItem(item);
			currentRecipient++;
			if (currentRecipient >= totalRecipients) {
				currentRecipient = 0;
				pawnsEach--;
			}
			if (pawnsEach == 0) {
				break;
			}
		}
	}

}
