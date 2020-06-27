package hopshackle.simulation.arsmagica;

import hopshackle.simulation.*;

import java.util.*;

public class SocialMeeting {

	public SocialMeeting(List<Magus> people, int friendMod, int enemyMod) {
		// We want to run through all these and create relationships
		// Note that every pair is tested twice - just makes relationships more
		// common
		
		for (Magus p1 : people) {
			if (p1.isDead() || p1.isInTwilight()) continue;
			for (Magus p2 : people) {
				if (p2.isDead() || p2.isInTwilight()) continue;
				if (p1 != p2 && p1.getRelationshipWith(p2) == Relationship.NONE) {
					// modifier is a general one. Positive is good and promotes
					// friendship
					// friendMod and enemyMod reflect the underlying event that
					// has caused social
					// interaction (and both of these may be positive indicating
					// an intense situation that
					// is likely to fall out one way or the other).
					int modifier = relationshipModifier(p1, p2);
					friendMod -= Math.max(0, total(p1, Relationship.FRIEND) + total(p2, Relationship.FRIEND) - 10); 
					enemyMod -= Math.max(0, total(p1, Relationship.ENEMY) + total(p2, Relationship.ENEMY) - 10);
					if (Dice.stressDieResult() + modifier + friendMod > 11) {
						p1.setRelationship(p2, Relationship.FRIEND);
						p2.setRelationship(p1, Relationship.FRIEND);
						// also exchange Longevity Ritual Services
						p2.addItem(new LongevityRitualService(p1));
						p1.addItem(new LongevityRitualService(p2));
					} else if (Dice.stressDieResult() - modifier + enemyMod > 11) {
						p1.setRelationship(p2, Relationship.ENEMY);
						p2.setRelationship(p1, Relationship.ENEMY);
					}
				}
			}
		}
	}
	
	public static int relationshipModifier(Magus m1, Magus m2) {
		int modifier = 0;
		switch (m1.getRelationshipWith(m2)) {
		case FRIEND:
			modifier += 5;
			break;
		case ENEMY:
			modifier -= 5;
			break;
		case NONE:
		}
		if (m1.getHermeticHouse() != null && m1.getHermeticHouse() == m2.getHermeticHouse())
			modifier += 2;
		modifier += commonSocialCircle(m1, m2);
		return modifier;
	}

	public static int commonSocialCircle(Magus m1, Magus m2) {
		double retValue = 0;
		Map<Agent, Relationship> r1 = m1.getRelationships();
		for (Agent contact : r1.keySet()) {
			switch (r1.get(contact)) {
			case FRIEND:
				switch (m2.getRelationshipWith(contact)) {
				case FRIEND:
					retValue += 1;
					break;
				case ENEMY:
					retValue -= 1;
					break;
				case NONE:
				}
				break;
			case ENEMY:
				switch (m2.getRelationshipWith(contact)) {
				case FRIEND:
					retValue -= 1;
					break;
				case ENEMY:
					retValue += 0.5;
					break;
				case NONE:
				}
				break;
			case NONE:
			}
		}
		return (int) retValue;
	}
	
	private int total(Magus m, Relationship type) {
		int total = 0;
		for (Relationship r : m.getRelationships().values()) {
			if (r == type) total +=1;
		}
		return total;
	}
}
