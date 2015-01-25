package hopshackle.simulation.arsmagica;

public enum HermeticHouse {
	
	BONISAGUS,
	TRIANOMA,
	FLAMBEAU,
	TREMERE,
	TYTALUS,
	VERDITIUS,
	DIEDNE,
	BJORNAER,
	CRIAMON,
	JERBITON,
	MERINITA,
	MERCERE,
	GUERNICUS;

	private int apprenticeshipModifier;
	
	public void updateApprenticeshipModifier(int membership, int totalMagi) {
		double meanPerHouse = (double)totalMagi / 11.5;
		switch (this) {
		case BONISAGUS:
		case TRIANOMA:
		case MERCERE:
			meanPerHouse /= 2;
		default:
		}

		apprenticeshipModifier = 0;
		if (membership <= meanPerHouse/2.0)
			apprenticeshipModifier +=1;
		if (membership <= meanPerHouse/3.0)
			apprenticeshipModifier +=1;
		if (membership <= meanPerHouse/4.0)
			apprenticeshipModifier +=1;
		if (membership <= meanPerHouse/5.0)
			apprenticeshipModifier +=1;

		if (membership >= meanPerHouse * 2.0)
			apprenticeshipModifier -=1;
		if (membership >= meanPerHouse * 3.0)
			apprenticeshipModifier -=1;
		if (membership >= meanPerHouse * 4.0)
			apprenticeshipModifier -=1;
	}
	
	public int getApprenticeshipModifier() {
		return apprenticeshipModifier;
	}

}
