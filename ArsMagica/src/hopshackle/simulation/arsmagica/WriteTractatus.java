package hopshackle.simulation.arsmagica;

import java.util.*;

import hopshackle.simulation.*;

public class WriteTractatus extends ArsMagicaAction {
	
	private boolean isCovenantService;

	public WriteTractatus(Agent m) {
		super(m);
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
			Tractatus newBook = new Tractatus(subject, magus);
			magus.log("Writes Tractatus on " + subject);
			newBook.giveToRecipient(magus, isCovenantService);
			if (isCovenantService)
				magus.doSeasonsService();
		}
		magus.addXP(Abilities.LATIN, 2);
	}

}