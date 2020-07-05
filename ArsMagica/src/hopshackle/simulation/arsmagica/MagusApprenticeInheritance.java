package hopshackle.simulation.arsmagica;

import java.util.*;

import hopshackle.simulation.*;

public class MagusApprenticeInheritance extends SimpleInheritance<Magus> {

	@Override
	public void apply(Magus deadMagus) {
		// Rather than use superclass (run through Artefacts, let's be more precise, not least to reduce logging)

		List<Agent> heirs = this.getInheritorsInOrder(deadMagus);
		
		if (!heirs.isEmpty() && heirs.get(0) instanceof CovenantAgent) {
			// only covenant is left to inherit; so use that policy instead
			new MagusCovenantInheritance().apply(deadMagus);
			return;
		}

		for (Agent heir : heirs)
			heir.log("Inherits from estate of " + deadMagus + ":");

		bequeathVisSources(deadMagus);
		bequeathBooks(deadMagus);
		bequeathVis(deadMagus);

		if (deadMagus.hasApprentice()) {
			Magus apprentice = deadMagus.getApprentice();

			// Now find senior heir without apprentice
			boolean foundNewParens = false;
			for (Agent a : heirs) {
				if (a instanceof Magus) {	// could be covenant
					Magus heir = (Magus) a;
					if (!foundNewParens && !heir.hasApprentice() && heir != apprentice && !heir.isApprentice()) {
						apprentice.log("Parens " + deadMagus + " dies before apprenticeship has finished");
						heir.log("Inherits apprentice: " + apprentice.toString());
						apprentice.log("New Parens is now " + heir.toString());
						heir.addApprentice(apprentice);
						if (foundNewParens) {
							System.out.println("Second inheritance has occurred for " + deadMagus + " and " + apprentice);
						}
						foundNewParens = true;
					}
				}
			} // if none qualify, then apprentice is now on their own in a hostile world

			if (!foundNewParens)
				deadMagus.terminateApprenticeship(false);
		}
	}

	@Override
	protected List<Agent> getInheritorsInOrder(Magus deadMagus) {
		// Key difference to superclass is that current apprentices are not heirs
		List<Agent> heirs = super.getInheritorsInOrder(deadMagus);
		if (deadMagus.hasApprentice()) 
			heirs.remove(deadMagus.getApprentice());

		if (heirs.isEmpty() && deadMagus.getCovenant() != null)
			heirs.add(deadMagus.getCovenant().getCovenantAgent());	// covenant inherits if no apprentices

		if (heirs.isEmpty() && deadMagus.hasApprentice())
			heirs.add(deadMagus.getApprentice());	// only if no Covenant to look after current apprentice does apprentice inherit
		return heirs;
	}

	private void bequeathVisSources(Magus testator) {
		List<Agent> heirs = getInheritorsInOrder(testator);
		List<VisSource> visSources = testator.getInventoryOf(AMU.sampleVisSource);
		distributeArtefactsToHeirs(testator, visSources, heirs);
	}

	private void bequeathBooks(Magus testator) {
		List<Agent> heirs = getInheritorsInOrder(testator);
		List<Book> books = testator.getInventoryOf(AMU.sampleBook);
		distributeArtefactsToHeirs(testator, books, heirs);
	}

	private void bequeathVis(Magus testator) {
		HashMap<Arts, Integer> vis = AMU.getVisInventory(testator);
		List<Agent> heirs = getInheritorsInOrder(testator);

		if (vis.keySet().isEmpty() || heirs.isEmpty())
			return;

		for (Arts art : vis.keySet()) {
			int totalPawns = vis.get(art);
			int pawnsEach = totalPawns / heirs.size();
			int remainder = totalPawns % heirs.size();

			for (Agent h : heirs) {
				int pawnsTotal = 0;
				if (remainder > 0) {
					remainder--;
					pawnsTotal = pawnsEach + 1;
				} else 
					pawnsTotal = pawnsEach;
				if (h instanceof Magus) {
					Magus heir = (Magus) h;
					heir.addVis(art, pawnsTotal);			
				} else {
					for (int i = 0; i < pawnsTotal; i++) h.addItem(new Vis(art));
				}
			}
		}
	}
}
