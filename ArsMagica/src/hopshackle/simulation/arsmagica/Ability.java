package hopshackle.simulation.arsmagica;

import hopshackle.simulation.Agent;

public class Ability extends Skill {

	public Ability(Learnable type, int level, Agent who) {
		super(type, level, 5, who);
	}

}
