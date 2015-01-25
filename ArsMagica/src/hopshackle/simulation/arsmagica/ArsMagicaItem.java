package hopshackle.simulation.arsmagica;

import hopshackle.simulation.*;

public abstract class ArsMagicaItem implements Artefact {

	@Override
	public int getMakeDC() {
		return 0;
	}

	@Override
	public Recipe getRecipe() {
		return null;
	}

	@Override
	public double costToMake(Agent a) {
		return 0;
	}

	@Override
	public long getTimeToMake(Agent a) {
		return 0;
	}

	@Override
	public boolean isA(Artefact item) {
		return (item.getClass() == this.getClass());
	}

	@Override
	public void changeOwnership(Agent newOwner) {
	}

	@Override
	public boolean isInheritable() {
		return true;
	}

}
