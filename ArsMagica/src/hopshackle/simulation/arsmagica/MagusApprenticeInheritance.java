package hopshackle.simulation.arsmagica;

import java.util.*;

import hopshackle.simulation.*;

public class MagusApprenticeInheritance extends SimpleInheritance {

	@Override
	public <T extends Agent> void bequeathEstate(T testator) {
		// Rather than use superclass (run through Artefacts, let's be more precise, not least to reduce logging)
		Magus deadMagus = (Magus) testator;

		List<T> heirs = getInheritorsInOrder(testator);
		
		if (!heirs.isEmpty() && heirs.get(0) instanceof CovenantAgent) {
			// only covenant is left to inherit; so use that policy instead
			new MagusCovenantInheritance().bequeathEstate(deadMagus);
			return;
		}

		for (T heir : heirs)
			heir.log("Inherits from estate of " + testator + ":");

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

	@SuppressWarnings("unchecked")
	protected <T extends Agent> List<T> getInheritorsInOrder(T testator) {
		// Key difference to superclass is that current apprentices are not heirs
		List<T> heirs = super.getInheritorsInOrder(testator);
		Magus deadMagus = (Magus) testator;
		if (deadMagus.hasApprentice()) 
			heirs.remove(deadMagus.getApprentice());

		if (heirs.isEmpty() && deadMagus.getCovenant() != null)
			heirs.add((T) deadMagus.getCovenant().getCovenantAgent());	// covenant inherits if no apprentices

		if (heirs.isEmpty() && deadMagus.hasApprentice())
			heirs.add((T) deadMagus.getApprentice());	// only if no Covenant to look after current apprentice does apprentice inherit
		return heirs;
	}

	private void bequeathVisSources(Magus testator) {
		List<Magus> heirs = getInheritorsInOrder(testator);
		List<VisSource> visSources = testator.getInventoryOf(new VisSource(Arts.CORPUS, 1, null));
		distributeArtefactsToHeirs(testator, visSources, heirs);
	}

	private void bequeathBooks(Magus testator) {
		List<Magus> heirs = getInheritorsInOrder(testator);
		List<Book> books = testator.getInventoryOf(AMU.sampleBook);
		distributeArtefactsToHeirs(testator, books, heirs);
	}

	private void bequeathVis(Magus testator) {
		HashMap<Arts, Integer> vis = AMU.getVisInventory(testator);
		List<Magus> heirs = getInheritorsInOrder(testator);

		if (vis.keySet().isEmpty() || heirs.isEmpty())
			return;

		for (Arts art : vis.keySet()) {
			int totalPawns = vis.get(art);
			int pawnsEach = totalPawns / heirs.size();
			int remainder = totalPawns % heirs.size();

			for (Magus heir : heirs) {
				int pawnsTotal = 0;
				if (remainder > 0) {
					remainder--;
					pawnsTotal = pawnsEach + 1;
				} else 
					pawnsTotal = pawnsEach;
				heir.addVis(art, pawnsTotal);			
			}
		}
	}
}
