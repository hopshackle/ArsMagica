package hopshackle.simulation.arsmagica;

import java.util.List;

import hopshackle.simulation.*;

public class VisValuationFunction implements ValuationFunction<List<Artefact>> {

	private Magus magus;

	public VisValuationFunction(Agent agent) {
		if (agent instanceof Magus)
			this.magus = (Magus) agent;
	}

	@Override
	public double getValue(List<Artefact> visList) {
		double retValue = 0.0;
		for (Artefact item : visList) {
			if (item instanceof Vis) {
				Vis vis = (Vis)item;
				if (magus != null)
					retValue += MagusPreferences.getResearchPreference(magus, vis.getType());
				else 
					retValue += 1.0;
			}
		}
		return retValue;
	}
	@Override
	public String toString(List<Artefact> visList) {
		return AMU.prettyPrint(visList);
	}

}
