package hopshackle.simulation.arsmagica;

import hopshackle.simulation.Dice;

public class AgeingEvent {

	private Magus magus;

	public AgeingEvent(Magus magus) {
		this.magus = magus;
	}

	public void ageOneYear() {
		int ageingResult = Dice.stressDieResult() - magus.getLongevityModifier();
		
		if (magus.getLongevityRitualEffect() > 0)
			magus.addXP(Abilities.WARPING, 1);

		if (ageingResult < 2)
			return;
		else if (ageingResult < 10)
			magus.setApparentAge(magus.getApparentAge() +1);
		else if (ageingResult < 13) 
			gainAgeingPoint();
		else if (ageingResult == 13 || ageingResult > 21)
			ageToCrisis();
		else if (ageingResult == 14)
			gainAgeingPoint(AttributeTypes.QUICKNESS);
		else if (ageingResult == 15)
			gainAgeingPoint(AttributeTypes.STAMINA);
		else if (ageingResult == 16)
			gainAgeingPoint(AttributeTypes.PERCEPTION);
		else if (ageingResult == 17)
			gainAgeingPoint(AttributeTypes.PRESENCE);
		else if (ageingResult == 18) {
			gainAgeingPoint(AttributeTypes.STRENGTH);
			gainAgeingPoint(AttributeTypes.STAMINA);
		}
		else if (ageingResult == 19) {
			gainAgeingPoint(AttributeTypes.DEXTERITY);
			gainAgeingPoint(AttributeTypes.QUICKNESS);
		}
		else if (ageingResult == 20) {
			gainAgeingPoint(AttributeTypes.COMMUNICATION);
			gainAgeingPoint(AttributeTypes.PRESENCE);
		}
		else  {
			gainAgeingPoint(AttributeTypes.INTELLIGENCE);
			gainAgeingPoint(AttributeTypes.PERCEPTION);
		}

	}

	public void ageToCrisis() {
		magus.log("Experiences Ageing Crisis at " + magus.getAge());
		int xpToNextLevelOfDecrepitude = magus.getXPToNextLevelIn(Abilities.DECREPITUDE);
		do {
			gainAgeingPoint();
			xpToNextLevelOfDecrepitude--;
		} while (xpToNextLevelOfDecrepitude > 0);

		if (magus.getLongevityRitualEffect() > 0) {
			magus.setLongevityRitualEffect(0);
			magus.log("Crisis averted at cost of Longevity Ritual expiry");
			return;
		}
		int crisisTotal = Dice.roll(1, 10) + (int) Math.ceil(magus.getAge() / 10.0) + magus.getLevelOf(Abilities.DECREPITUDE);
		if (crisisTotal < 15)
			magus.log("Survives crisis without major problems.");
		else {
			int easeFactor = (crisisTotal - 14) * 3;
			if (Dice.stressDieResult() + magus.getStamina() >= easeFactor) 
				magus.log("Experiences major illness, but pulls through (ease factor " + easeFactor + ").");
			else {
				magus.die("Dies of old age.");
			}
		}
	}


	public void gainAgeingPoint() {
		gainAgeingPoint(AttributeTypes.randomChoice());
	}

	public void gainAgeingPoint(AttributeTypes attr) {
		ArsMagicaCharacteristic characteristic = attr.getStat(magus);
		characteristic.addAgeingPoints(1);
		magus.addXP(Abilities.DECREPITUDE, 1);
	}
}
