package hopshackle.simulation.arsmagica;

import hopshackle.simulation.Agent;

import java.util.*;

public class LabText extends Book {
	
	private Spell spell;
	private static Learnable learnableSpell = new Learnable() {
		
		@Override
		public int getXPForLevel(int level) {
			return 0;
		}
		
		@Override
		public int getMultiplier() {
			return 0;
		}
		
		@Override
		public String toString() {
			return "Spell Lab Text";
		}
		
	};
	
	public LabText(Spell spell, Agent author) {
		super(learnableSpell, spell.getLevel(), 0, author);
		this.spell = spell;
	}
	
	public LabText(LabText copyOf) {
		super(copyOf);
		spell = copyOf.spell;
	}

	@Override
	public int getXPGainForMagus(Magus magus) {
		return 0;
	}

	@Override
	public int getBPValue() {
		return spell.getLevel()/5;
	}

	@Override
	public Book createCopy() {
		return new LabText(this);
	}
	
	@Override
	public String toString() {
		return String.format("Lab Text for %s. Written by %s in %d %s", 
				spell.toString(), getAuthor(), getYearWritten(), getDeteriorationString()); 
	}
	
	public static List<LabText> extractAllLabTextsFrom(List<Book> library) {
		List<LabText> retValue = new ArrayList<LabText>();
		for (Book b : library) {
			if (b instanceof LabText)
				retValue.add((LabText)b);
		}
		return retValue;
	}

	public Spell getSpell() {
		return spell;
	}
}
