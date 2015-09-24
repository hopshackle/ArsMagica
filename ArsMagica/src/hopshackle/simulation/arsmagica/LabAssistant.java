package hopshackle.simulation.arsmagica;

import hopshackle.simulation.Agent;

public class LabAssistant extends ArsMagicaAction {

	private Agent researcher;
	
	public LabAssistant(Agent assistant, Agent researcher) {
		super(assistant);
		this.researcher = researcher;
	}
	
	@Override
	protected void doStuff() {
		magus.log("Acts as Lab Assistant to " + researcher);
		magus.addXP(Abilities.MAGIC_THEORY, 2);
	}
	
	public String description() {
		return "For " + researcher;
	}
}
