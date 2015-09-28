package hopshackle.simulation.arsmagica;

public class Familiar extends ArsMagicaItem {

	private int goldCord = 0;
	private int bronzeCord = 0;
	private int magicMight = 10;
	private int size = -2;
	private Arts form =  Arts.VIM;
	private Arts technique = Arts.REGO;


	public Familiar(Magus magus) {
		super();
		if (magus == null) return;
		MagusPreferences goals = magus.getResearchGoals();
		for (Arts t : Arts.techniques) {
			if (goals.getPreference(t) + 0.05 * magus.getLevelOf(t) > goals.getPreference(technique) + 0.05 * magus.getLevelOf(technique) )
				technique = t;
		}
		for (Arts f : Arts.forms) {
			if (goals.getPreference(f) + 0.05 * magus.getLevelOf(f)  > goals.getPreference(form) + 0.05 * magus.getLevelOf(form) )
				form = f;
		}
	}
	@Override
	public boolean isInheritable() {
		return false;
	}

	@Override
	public String toString() {
		return String.format("%s%s familiar. Gold = %d, Bronze = %d.", technique.getAbbreviation(), form.getAbbreviation(), goldCord, bronzeCord);
	}

	public Arts getTechnique() {
		return technique;
	}
	public Arts getForm() {
		return form;
	}
}
