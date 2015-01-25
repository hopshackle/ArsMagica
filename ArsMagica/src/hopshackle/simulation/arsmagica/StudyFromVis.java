package hopshackle.simulation.arsmagica;

import hopshackle.simulation.*;

public class StudyFromVis extends ArsMagicaAction {

	private Arts visType;
	private int overrideDieRoll, overrideBotchLevel;
	private boolean override = false;
	
	public StudyFromVis(Agent a, Arts visType) {
		super(a);
		this.visType = visType;
	}
	
	public void setDieRoll(int roll, int botchLevel) {
		overrideDieRoll = roll;
		overrideBotchLevel = botchLevel;
		override = true;
	}
	public void setDieRoll(int roll) {
		setDieRoll(roll, 0);
	}

	protected void doStuff() {
		magus.log("Spends Season studying " + visType + " vis");
		int currentArtLevel = magus.getLevelOf(visType);
		int requiredPawns = (int) Math.max(Math.ceil(currentArtLevel / 5.0), 1);
		magus.removeVis(visType, requiredPawns);
		int roll = Dice.stressDieResult();
		if (override)
			roll = overrideDieRoll;

		if (roll == 0) {
			boolean botched = determineBotchResults(1 + requiredPawns);
			if (botched) return;
		}
		
		roll += magus.getMagicAura();
		magus.addXP(visType, roll);
	}
	
	private boolean determineBotchResults(int botchDice) {
		int botches = Dice.getBotchResult(botchDice);
		if (override)
			botches = overrideBotchLevel;
		
		if (botches == 0)
			return false;
		
		magus.log("Botches - level " + botches);
		magus.addXP(Abilities.WARPING, botches);
		
		if (botches > 1) 
			new TwilightEpisode(magus, botches);
		
		return true;
	}

}
