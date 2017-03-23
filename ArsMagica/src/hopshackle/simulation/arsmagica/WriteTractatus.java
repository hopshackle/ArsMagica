package hopshackle.simulation.arsmagica;

import java.util.*;

public class WriteTractatus extends ArsMagicaAction {
	
	private boolean isCovenantService;
	private Tractatus newBook;

	public WriteTractatus(Magus m) {
		super(MagusActions.WRITE_TRACTATUS, m);
		if (magus.getSeasonsServiceOwed() > 0) 
			isCovenantService = true;
	}

	protected void doStuff() {
		Learnable[] possibleSubjects = new Learnable[0];
		possibleSubjects = magus.getPossibleTractactusSubjects().toArray(possibleSubjects);
		Map<Learnable, Double> options = new HashMap<Learnable, Double>();
		for (Learnable option : possibleSubjects)
			options.put(option,  1.0);
		if (possibleSubjects.length > 0) {
			Learnable subject = MagusPreferences.getPreferenceGivenPriors(magus, options);
			newBook = new Tractatus(subject, magus);
			magus.log("Writes Tractatus on " + subject);
			newBook.giveToRecipient(magus, isCovenantService);
			if (isCovenantService)
				magus.doSeasonsService();
		}
		magus.addXP(Abilities.LATIN, 2);
	}

	public String description() {
		return newBook.toString();
	}
	
	public boolean isCovenantService() {
		return isCovenantService;
	}
}
