package hopshackle.simulation.arsmagica;

import java.util.concurrent.atomic.*;

import hopshackle.simulation.*;

public class VisSource extends Location implements ArtefactRequiringMaintenance {

	private Arts type;
	private int amountPerAnnum;
	private double extinctionRate = 0.01;
	private static AtomicInteger idFountain = new AtomicInteger(1);
	private int uniqueID;
	private int lastHarvested;
	private Agent claimant;

	public VisSource(Arts type, int perAnnum, Location parentLocation) {
		super(parentLocation);
		this.type = type;
		amountPerAnnum = perAnnum;
		uniqueID = idFountain.getAndIncrement();
	}

	public int getUniqueID() {
		return uniqueID;
	}

	public Arts getType() {
		return type;
	}

	public int getAmountPerAnnum() {
		return amountPerAnnum;
	}

	@Override
	public void maintenance() {
		// Location maintenance once per year
		if (Math.random() < extinctionRate || claimant == null || claimant.isDead()) {
			amountPerAnnum = 0;
			this.setParentLocation(null);
		}
	}

	@Override
	public void artefactMaintenance(Agent owner) {
		if (owner == null) return;
		if (owner != claimant)
			logger.severe("Claimant " + claimant + " not equal to owner " + owner + " of vis source");
		if (owner.getWorld().getYear() > lastHarvested) {
			lastHarvested = owner.getWorld().getYear();
			if (amountPerAnnum == 0) {
				owner.log(String.format("%s vis source (ID:%d) has expired", getType(), getUniqueID()));
				owner.removeItem(this);
			} else {
				if (owner instanceof Magus) {
					Magus magus = (Magus) owner;
					if (!magus.isInTwilight())
						magus.addVis(getType(), getAmountPerAnnum());
				} else {
					for (int i = 0; i < getAmountPerAnnum(); i++) {
						owner.addItem(new Vis(getType()));
					}
				}
			}
		}
	}

	@Override
	public boolean isA(Artefact item) {
		if (item instanceof VisSource)
			return true;
		return false;
	}

	public void setAnnualExtinctionRate(double rate) {
		extinctionRate = rate;
	}


	@Override
	public String toString() {
		return String.format("%s vis source supplying %d pawns per annum (ID:%d)", type, amountPerAnnum, uniqueID);
	}

	@Override
	public int getMakeDC(){return 0;}
	@Override
	public Recipe getRecipe() {return null;}
	@Override
	public double costToMake(Agent a) {return 0;}
	@Override
	public long getTimeToMake(Agent a) {return 0;}
	@Override
	public void changeOwnership(Agent newOwner) {claimant = newOwner;}
	@Override
	public boolean isInheritable() {return true;}

	public void setAnnualYield(int i) {
		amountPerAnnum = i;
	}

}
