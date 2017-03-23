package hopshackle.simulation.arsmagica;

public class Attribute {

	private int value;

	public Attribute(int v) {
		super();
		value = v;
		if (value<1) value=1;
	}

	public int getValue() {
		return value;
	}

	public int getMod() {
		return Math.round(value/2)-5;
	}

	public String toString() {
		return String.valueOf(getValue());
	}
}
