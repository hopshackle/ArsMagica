package hopshackle.simulation.arsmagica;

public class LabAssistant extends ArsMagicaAction {

	private Magus researcher;
	
	public LabAssistant(Magus assistant, Magus researcher) {
		super(MagusActions.LAB_ASSISTANT, assistant);
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
