package hopshackle.simulation.arsmagica;

public enum CovenantAttributes implements Learnable {
	
	WEALTH,
	GROGS,
	MUNDANE_CONNECTIONS;

	@Override
	public int getMultiplier() {
		return 5;
	}

	@Override
	public int getXPForLevel(int level) {
		int total = 0;
		for (int i = 1; i <= level; i++) {
			total += i * getMultiplier();
		}
		return total;
	}
	

}
