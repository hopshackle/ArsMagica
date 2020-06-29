package hopshackle.simulation.arsmagica.test;

import static org.junit.Assert.*;

import java.util.*;

import hopshackle.simulation.*;
import hopshackle.simulation.arsmagica.*;
import org.junit.*;

public class SocialMeetings {

    Magus[] magi = new Magus[3];
    World w = new World(new SimpleWorldLogic<>(new ArrayList<>(EnumSet.allOf(MagusActions.class))));

    @Before
    public void setup() {
        for (int i = 0; i < 3; i++)
            magi[i] = new Magus(w);
    }

    @Test
    public void getRelationship() {
        assertEquals(magi[0].getRelationshipWith(magi[1]), Relationship.NONE);
        magi[0].setRelationship(magi[1], Relationship.FRIEND);
        assertEquals(SocialMeeting.relationshipModifier(magi[0], magi[1]), 5);
        assertEquals(SocialMeeting.relationshipModifier(magi[1], magi[0]), 0);
        assertEquals(magi[0].getRelationshipWith(magi[1]), Relationship.FRIEND);
        assertEquals(magi[1].getRelationshipWith(magi[1]), Relationship.NONE);
        magi[1].setRelationship(magi[0], Relationship.FRIEND);
        assertEquals(magi[0].getRelationshipWith(magi[1]), Relationship.FRIEND);
        assertEquals(magi[1].getRelationshipWith(magi[0]), Relationship.FRIEND);
        assertEquals(SocialMeeting.relationshipModifier(magi[0], magi[1]), 5);
        assertEquals(SocialMeeting.relationshipModifier(magi[1], magi[0]), 5);
    }

    @Test
    public void removeRelationship() {
        magi[0].setRelationship(magi[1], Relationship.ENEMY);
        assertEquals(magi[0].getRelationshipWith(magi[1]), Relationship.ENEMY);
        assertEquals(SocialMeeting.relationshipModifier(magi[0], magi[1]), -5);
        assertEquals(SocialMeeting.relationshipModifier(magi[1], magi[0]), 0);
        magi[0].setRelationship(magi[1], Relationship.NONE);
        assertEquals(SocialMeeting.relationshipModifier(magi[0], magi[1]), 0);
        assertEquals(SocialMeeting.relationshipModifier(magi[1], magi[0]), 0);
    }


}
