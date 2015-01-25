package hopshackle.simulation.arsmagica;

import hopshackle.simulation.Agent;

abstract class Skill {

	private int level;
	private int xp;
	private int multiplier;
	private Learnable type;
	private Agent owner;

	public Skill(Learnable type, int level, int multiplier, Agent who) {
		this.type = type;
		this.level = level;
		this.multiplier = multiplier;
		xp = 0;
		owner = (Agent) who;
	}

	public void addXP(int change) {
		xp = xp + change;
		if (owner != null && owner.getFullDebug())
			owner.log("Gains " + change + " XP in " + type);
		while (xp >= (level + 1) * multiplier) {
			level++;
			if (owner != null && owner.getFullDebug())
				owner.log("Increases " + type + " to level " + level);
			xp = xp - (level * multiplier);
		}
		while (xp < 0 && level > 0) {
			xp = xp + level * multiplier;
			level--;
			if (owner != null & owner.getFullDebug())
				owner.log("Level in " + type + " falls to " + level);
		}
		if (xp < 0) xp = 0;
	}

	public int getLevel() {
		return level;
	}

	public int getUnusedXP() {
		return xp;
	}

	public int getXPtoNextLevel() {
		return (level + 1) * multiplier - xp;
	}

	public String toString() {
		return String.format("%-16s: %d (%d)", type.toString(), level, xp);
	}

	public int getTotalXP() {
		int total = 0;
		for (int l = level; l > 0; l--)
			total += l * multiplier;
		return total + xp;
	}
}