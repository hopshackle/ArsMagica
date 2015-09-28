package hopshackle.simulation.arsmagica;

import java.util.*;

import hopshackle.simulation.*;

public class MagusCovenantInheritance implements InheritancePolicy {

	@Override
	public <T extends Agent> void bequeathEstate(T testator) {

		Magus deadMagus = (Magus) testator;
		Covenant covenant = deadMagus.getCovenant();
		if (covenant == null) {
			return;
		}
		covenant.log("Inherits estate of " + deadMagus);
		CovenantAgent inheritor = covenant.getCovenantAgent();
		List<Artefact> allItems = testator.getInventory();
		for (Artefact item : allItems)
			inheritor.addItem(item);


		if (deadMagus.hasApprentice()) {
			boolean foundNewParens = false;
			Magus apprentice = deadMagus.getApprentice();
			List<Magus> members = covenant.getCurrentMembership();
			for (Magus sodalis : members) {
				if (!foundNewParens && !sodalis.hasApprentice() && sodalis != deadMagus && !sodalis.isApprentice() && !sodalis.isDead()) {
					apprentice.log("Parens dies before apprenticeship has finished");
					sodalis.log("Inherits apprentice: " + apprentice.toString());
					apprentice.log("New Parens is now " + sodalis.toString());
					sodalis.addApprentice(apprentice);
					if (foundNewParens) {
						System.out.println("Second inheritance has occurred for " + deadMagus + " and " + apprentice);
					}
					foundNewParens = true;
				}
			}
			if (!foundNewParens) {
				deadMagus.terminateApprenticeship(false);
			}
		}
	}
}
