package hopshackle.simulation.arsmagica;

import hopshackle.simulation.Agent;


public class Death extends ArsMagicaAction {
	
	private String reason;

	public Death(Agent a) {
		super(a);
	}
	
	public Death(Agent a, String r) {
		this(a);
		reason = r;
		doCleanUp();
	}
	public String description() {
		return reason;
	}

}
