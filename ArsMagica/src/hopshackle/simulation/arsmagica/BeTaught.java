package hopshackle.simulation.arsmagica;

public class BeTaught extends ArsMagicaAction {

	public BeTaught(Magus magus) {
		super(MagusActions.BE_TAUGHT, magus);
	}
	
	protected void doStuff() {
		// Nothing - covered in the teaching action
	}
	
	public String description() {
		return "";
	}
}
