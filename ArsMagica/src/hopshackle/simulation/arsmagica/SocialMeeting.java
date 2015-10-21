package hopshackle.simulation.arsmagica;

import hopshackle.simulation.Dice;

import java.util.List;

public class SocialMeeting {

	public SocialMeeting(List<Magus> people) {
		// We want to run through all these and create relationships
		
		for (Magus p1 : people) {
			for (Magus p2 : people) {
				if (p1 != p2 && p1.getRelationshipWith(p2) == Relationship.NONE) {
					int modifier = 0;
					if (Dice.stressDieResult() + modifier > 11) {
						p1.setRelationship(p2, Relationship.FRIEND);
						p2.setRelationship(p1, Relationship.FRIEND);
					} else if (Dice.stressDieResult()  + modifier > 11) {
						p1.setRelationship(p2, Relationship.ENEMY);
						p2.setRelationship(p1, Relationship.ENEMY);
					}
				}
			}
		}
	}

}
