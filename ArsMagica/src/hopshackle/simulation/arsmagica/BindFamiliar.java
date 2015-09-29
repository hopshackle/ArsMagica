package hopshackle.simulation.arsmagica;

import java.util.Map;

import hopshackle.simulation.*;
public class BindFamiliar extends ArsMagicaAction {

	public BindFamiliar(Agent a) {
		super(a);
	}

	@Override
	protected void doStuff() {

		Familiar f = magus.getFamiliar();

		int currentBond = f.getBronze() + f.getGold();
		int labTotal = magus.getLabTotal(f.getTechnique(), f.getForm());
		if (labTotal / 5 <= currentBond) {
			magus.log("Insufficient lab total to improve familiar bond");
			return;
		}
		Map<Arts, Integer> visStocks = AMU.getVisInventory(magus);
		int formVis = visStocks.get(f.getForm());
		int techniqueVis = visStocks.get(f.getTechnique());
		
		if (formVis + techniqueVis < labTotal / 5) {
			magus.log("Insufficient vis to bind familiar");
			return;
		}
		int formVisUsed = Math.min(formVis, labTotal / 5 - currentBond);
		int techniqueVisUsed = labTotal / 5 - formVisUsed - currentBond;
		magus.removeVis(f.getForm(), formVisUsed);
		magus.removeVis(f.getTechnique(), techniqueVisUsed);
		
		int newGold, newBronze = 0;
		switch (labTotal / 5) {
			case 0:
				newGold = 0; newBronze = 0;
				break;
			case 1:
			case 2:
				newGold = 0; newBronze = 1;
				break;
			case 3:
				newGold = 0; newBronze = 2;
				break;
			case 4:
			case 5:
			case 6:
				newGold = 1; newBronze = 2;
				break;
			case 7:
			case 8:
				newGold = 1; newBronze = 3;
				break;
			case 9:
			case 10:
			case 11:
			case 12:
				newGold = 2; newBronze = 3;
				break;
			case 13:
			case 14:
			case 15:
				newGold = 2; newBronze = 4;
				break;
			case 16:
			case 17:
			case 18:
			case 19:
			case 20:
				newGold = 3; newBronze = 4;
				break;
			default:
				newGold = 3; newBronze = 5;
				break;
		}
		f.setBronze(newBronze);
		f.setGold(newGold);
		magus.log("Upgrades familar to " + f.toString());
		magus.addXP(AMU.getPreferredXPGain(f.getTechnique(), f.getForm(), magus), 2);
	}
	
}
