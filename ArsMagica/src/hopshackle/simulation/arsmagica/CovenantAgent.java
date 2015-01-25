package hopshackle.simulation.arsmagica;

import hopshackle.simulation.*;

public class CovenantAgent extends Agent {
	
	Covenant covenant;

	public CovenantAgent(Covenant cov) {
		super(cov.getWorld());
		covenant = cov;
	}

	public Covenant getCovenant() {return covenant;}
	
	public String toString() {
		return covenant.toString();
	}
	
	
	@Override
	public double getScore() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getMaxScore() {
		// TODO Auto-generated method stub
		return 0;
	}

	
	@Override 
	public int getMaxAge() {
		return 2000 * 52;
	}
	
}
