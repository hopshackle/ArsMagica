package hopshackle.simulation.arsmagica;

import java.util.List;

import hopshackle.simulation.*;

public class VisValuationFunction implements ValuationFunction<List<Artefact>> {
	
	private Magus magus;
	
	public VisValuationFunction(Magus magus) {
		this.magus = magus;
	}

	@Override
	public double getValue(List<Artefact> visList) {
		double retValue = 0.0;
		for (Artefact item : visList) {
			if (item instanceof Vis) {
				Vis vis = (Vis)item;
				retValue += MagusPreferences.getResearchPreference(magus, vis.getType());
			}
		}
		return retValue;
	}

}
