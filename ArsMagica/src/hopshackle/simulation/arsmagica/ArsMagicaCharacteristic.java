package hopshackle.simulation.arsmagica;

public class ArsMagicaCharacteristic {
	
	private int modifier;
	private int ageingPoints;
	
	public ArsMagicaCharacteristic(int modifier) {
		this.modifier = modifier;
	}
	
	public int getModifier() {
		return modifier;
	}
	
	public void setModifier(int newValue) {
		modifier = newValue;
	}

	public void addAgeingPoints(int i) {
		ageingPoints += i;
		if (ageingPoints >= Math.abs(modifier) + 1) {
			ageingPoints = ageingPoints - Math.abs(modifier) - 1;
			modifier--;
		}
	}
	
	public int getAgeingPoints() {
		return ageingPoints;
	}

}
