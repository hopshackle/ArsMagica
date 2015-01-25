package hopshackle.simulation.arsmagica;

import hopshackle.simulation.Dice;

public enum Arts implements Learnable {

	CREO ("Cr", 1),
	INTELLEGO ("In", 2),
	MUTO ("Mu", 3),
	PERDO ("Pe", 4),
	REGO ("Re", 5),
	ANIMAL ("An", 6),
	AQUAM ("Aq", 7),
	AURAM ("Au", 8),
	CORPUS ("Co", 9),
	HERBAM ("He", 10),
	IGNEM ("Ig", 11),
	IMAGINEM ("Im", 12),
	MENTEM ("Me", 13),
	TERRAM ("Te", 14),
	VIM ("Vi", 15);
	
	public final static Arts[] techniques = {CREO, INTELLEGO, MUTO, PERDO, REGO};
	public final static Arts[] forms = {ANIMAL, AQUAM, AURAM, CORPUS, HERBAM, IGNEM, IMAGINEM, MENTEM, TERRAM, VIM};
	private String abbreviation;
	private int order;
	
	private Arts(String abbreviation, int order) {
		this.abbreviation = abbreviation;
		this.order = order;
	}
	
	public static Arts random() {
		Arts[] allArts = Arts.values();
		int roll = Dice.roll(1, allArts.length);
		return allArts[roll-1];
	}
	
	public static Arts randomTechnique() {
		int roll = Dice.roll(1, 5);
		return techniques[roll-1];
	}
	public static Arts randomForm() {
		int roll = Dice.roll(1, 10);
		return forms[roll-1];
	}
	
	public int getMultiplier() {
		return 1;
	}
	
	@Override
	public int getXPForLevel(int level) {
		int total = 0;
		for (int i = 1; i <= level; i++) {
			total += i * getMultiplier();
		}
		return total;
	}

	public String getAbbreviation() {
		return abbreviation;
	}

	public static Arts getArtFromAbbreviation(String abbrev) {
		if (abbrev.equals("Cr")) return Arts.CREO;
		if (abbrev.equals("In")) return Arts.INTELLEGO;
		if (abbrev.equals("Mu")) return Arts.MUTO;
		if (abbrev.equals("Pe")) return Arts.PERDO;
		if (abbrev.equals("Re")) return Arts.REGO;
		if (abbrev.equals("An")) return Arts.ANIMAL;
		if (abbrev.equals("Aq")) return Arts.AQUAM;
		if (abbrev.equals("Au")) return Arts.AURAM;
		if (abbrev.equals("Co")) return Arts.CORPUS;
		if (abbrev.equals("He")) return Arts.HERBAM;
		if (abbrev.equals("Ig")) return Arts.IGNEM;
		if (abbrev.equals("Im")) return Arts.IMAGINEM;
		if (abbrev.equals("Me")) return Arts.MENTEM;
		if (abbrev.equals("Te")) return Arts.TERRAM;
		if (abbrev.equals("Vi")) return Arts.VIM;
		return null;
	}
	
	public int getOrder() {
		return order;
	}
}
