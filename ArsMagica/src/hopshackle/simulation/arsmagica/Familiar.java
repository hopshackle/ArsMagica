package hopshackle.simulation.arsmagica;

import java.util.Map;

public class Familiar extends ArsMagicaItem {

	private int goldCord = 0;
	private int bronzeCord = 0;
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
	public int getGold() {
		return goldCord;
	}
	public int getBronze() {
		return bronzeCord;
	}
	public void setBronze(int newLevel) {
		bronzeCord = newLevel;
	}
	public void setGold(int newLevel) {
		goldCord = newLevel;
	}
	public boolean canImproveBond(Magus magus) {
		int labTotal = magus.getLabTotal(technique, form);
		if (labTotal < labTotalForImprovement()) return false;
		Map<Arts, Integer> visStocks = AMU.getVisInventory(magus);
		int vis = visStocks.get(technique) + visStocks.get(form);
		if (vis < labTotal / 5) return false;
		return true;
	}
	public int labTotalForImprovement() {
		int currentLabTotal = Abilities.AREA_LORE.getXPForLevel(goldCord) + Abilities.AREA_LORE.getXPForLevel(bronzeCord);
		if (currentLabTotal  < 5) return 5;
		if (currentLabTotal  < 15) return 15;
		if (currentLabTotal  < 20) return 20;
		if (currentLabTotal  < 35) return 35;
		if (currentLabTotal  < 45) return 45;
		if (currentLabTotal  < 65) return 65;
		if (currentLabTotal  < 80) return 80;
		if (currentLabTotal  < 105) return 105;
		return 500;
	}
}
