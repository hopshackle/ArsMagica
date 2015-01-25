package hopshackle.simulation.arsmagica;

import hopshackle.simulation.*;

public class TwilightEpisode {
	
	public TwilightEpisode(Magus magus, int botches) {
		int magusRoll = Dice.stressDieResult();
		int twilightAvoidanceRoll = magusRoll - Dice.stressDieResult() - botches;
		twilightAvoidanceRoll += getTwilightAvoidanceModifier(magus);
		
		if (twilightAvoidanceRoll >= 0) {
			magus.log("Avoids entering Twilight");
			return;
		}
		
		int warpingRoll = Dice.roll(1, 10);
		magus.addXP(Abilities.WARPING, warpingRoll);// simple die in warping points regardless of outcome
		
		magusRoll = Dice.stressDieResult();
		int comprehensionBotchLevel = 0;
		if (magusRoll == 0) {
			comprehensionBotchLevel = Dice.getBotchResult(1 + botches);
		}
		
		int twilightComprehensionRoll = magusRoll - Dice.stressDieResult() + getTwilightComprehensionModifier(magus);
		int timeInTwilight = magus.getLevelOf(Abilities.WARPING);
		if (twilightComprehensionRoll >= 0 && comprehensionBotchLevel == 0) {
			magus.log("Comprehends Twilight episode");
			magus.addTwilightScar(true);
			magus.addXP(Arts.random(), (warpingRoll + botches) * 2);
			timeInTwilight -= twilightComprehensionRoll;
		} else {
			magus.log("Fails to comprehend Twilight episode");
			magus.addTwilightScar(false);
			magus.addXP(Abilities.MAGIC_THEORY, - (warpingRoll + botches) * 2);
			timeInTwilight += comprehensionBotchLevel;
		}

		int seasons = 0;
		if (timeInTwilight > 9) {
			magus.die("Enters Final Twilight");
		} 
		if (timeInTwilight == 9) {
			int years = 7 + Dice.stressDieResult();
			magus.log("Spends " + years + " years in Twilight");
			seasons = years * 4;
		}
		if (timeInTwilight < 9) {
			magus.log("Spends " + twilightTimeToString(timeInTwilight) + " in Twilight");
			if (timeInTwilight == 5 || timeInTwilight == 6) 
				seasons = 1;
			if (timeInTwilight == 7)
				seasons = 4;
			if (timeInTwilight == 8)
				seasons = 28;
		}
		
		if (seasons > 0) {
			magus.purgeActions();
			magus.addAction(new InTwilight(magus, seasons));
			// No ageing or vis collection whilst in Twilight
		}
		
	}
	
	public static int getTwilightAvoidanceModifier(Magus magus) {
		int modifier = magus.getStamina() + magus.getLevelOf(Abilities.CONCENTRATION) + magus.getLevelOf(Arts.VIM) / 5;
		modifier -= magus.getLevelOf(Abilities.WARPING) + magus.getMagicAura();
		return modifier;
	}
	
	public static int getTwilightComprehensionModifier(Magus magus) {
		int modifier = magus.getIntelligence() - magus.getLevelOf(Abilities.WARPING);
		return modifier;
	}
	
	public static String twilightTimeToString(int time) {
		if (time < 1)
			return "a moment";
		if (time == 1)
			return "two minutes";
		if (time == 2)
			return "two hours";
		if (time == 3)
			return "until sunset";
		if (time == 4)
			return "a day";
		if (time == 5)
			return "a month";
		if (time == 6)
			return "a season";
		if (time == 7)
			return "a year";
		if (time == 8)
			return "seven years";
		return "an Unknown period of " + time + " ";
	}
}
