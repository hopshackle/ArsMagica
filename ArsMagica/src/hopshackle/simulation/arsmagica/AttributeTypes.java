package hopshackle.simulation.arsmagica;

import hopshackle.simulation.Dice;

public enum AttributeTypes {
	
	INTELLIGENCE,
	PERCEPTION,
	COMMUNICATION,
	PRESENCE,
	STAMINA,
	STRENGTH,
	QUICKNESS,
	DEXTERITY;
	
	public ArsMagicaCharacteristic getStat(Magus magus) {
		switch (this) {
		case INTELLIGENCE:
			return magus.getIntelligenceAMC();
		case PERCEPTION:
			return magus.getPerceptionAMC();
		case PRESENCE:
			return magus.getPresenceAMC();
		case STAMINA:
			return magus.getStaminaAMC();
		case STRENGTH:
			return magus.getStrengthAMC();
		case QUICKNESS:
			return magus.getQuicknessAMC();
		case DEXTERITY:
			return magus.getDexterityAMC();
		case COMMUNICATION:
			return magus.getCommunicationAMC();
		}
		return null;
	}

	public static AttributeTypes randomChoice() {
		AttributeTypes[] array = AttributeTypes.values();
		int number = array.length;
		return array[Dice.roll(1, number) -1];
	}
	
}
