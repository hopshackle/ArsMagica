package hopshackle.simulation.arsmagica;

import hopshackle.simulation.Artefact;

public class Vis extends ArsMagicaItem {

	private Arts type;
	
	public Vis(Arts type) {
		this.type = type;
	}

	public Arts getType() {
		return type;
	}

	@Override
	public boolean isA(Artefact item) {
		if (item instanceof Vis) return true;
		return false;
	}
	
	@Override
	public String toString() {
		return "Pawn of " + type;
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Vis))
				return false;
		Vis v = (Vis) other;
		return this.getType() == v.getType();
	}
}
