package hopshackle.simulation.arsmagica;

import java.io.File;
import java.util.*;

import hopshackle.simulation.*;

public class InventSpell extends ArsMagicaAction {

	protected static String baseDir = SimProperties.getProperty("BaseDirectory", "C:\\Simulations");
	private static List<Spell> allBasicSpells = loadBasicSpells();
	private Arts technique, form;
	private Arts requisiteTechnique, requisiteForm;
	private int level;
	private int labTotal = -100;
	private int pointsAccumulatedSoFar;
	private String spellName;
	private boolean standardSpell;
	private LabText labTextUsed = null;

	public InventSpell(Magus a, InventSpell researchProject) {
		super(MagusActions.INVENT_SPELL, a);
		if (a.hasApprentice()) optionalActors.add(a.getApprentice());
		technique = researchProject.technique;
		form = researchProject.form;
		requisiteForm = researchProject.requisiteForm;
		requisiteTechnique = researchProject.requisiteTechnique;
		level = researchProject.level;
		labTotal = magus.getLabTotal(technique, form);
		spellName = researchProject.spellName;
		pointsAccumulatedSoFar = researchProject.pointsAccumulatedSoFar;
	}
	
	public InventSpell(Magus a) {
		super(MagusActions.INVENT_SPELL, a);
		if (a.hasApprentice()) optionalActors.add(a.getApprentice());
		int iterations = 0;
		boolean hasTriedOneNewSpell = false;
		List<LabText> labTexts = getAllUnknownSpellsWithAccessibleLabTexts();
		do {
			if (!standardSpell)	// from previous iteration
				hasTriedOneNewSpell = true;
			determineTechniqueAndForm(labTexts);
			determineLevel(labTexts);
			determineSpellName(labTexts);
			iterations++;
		} while (level == 0 && iterations < 10 && (!standardSpell && !hasTriedOneNewSpell));

		for (LabText lt : labTexts) {
			Spell s = lt.getSpell();
			if (s.getName() == spellName && s.getLevel() == level) {
				labTextUsed = lt;
				lt.setCurrentReader(magus);
			}
		}
	}

	private void determineSpellName(List<LabText> labTexts) {
		List<Spell> standardPossibilities = new ArrayList<Spell>();
		for (LabText lt : labTexts) {
			Spell s = lt.getSpell();
			if (isValidFromText(s))
				standardPossibilities.add(s);
		}
		for (Spell s : allBasicSpells) {
			if (isValidDeNovo(s))
				standardPossibilities.add(s);
		}

		if (standardPossibilities.size() > 0) {
			int roll = Dice.roll(1, standardPossibilities.size()) - 1;
			Spell baseSpell = standardPossibilities.get(roll);
			spellName = baseSpell.getName();
			form = baseSpell.getForm();
			technique = baseSpell.getTechnique();
			requisiteForm = baseSpell.getRequisiteForm();
			requisiteTechnique = baseSpell.getRequisiteTechnique();
			labTotal = magus.getLabTotal(baseSpell);
		}
		if (spellName == null || spellName == "")  {
			spellName = "New spell by " + magus.toString() + " invented in " + magus.getWorld().getYear();
			standardSpell = false;
		} else {
			standardSpell = true;
		}
	}
	
	private boolean isValidDeNovo(Spell s) {
		if (!isValidFromText(s))
			return false;
		int minLabTotal = magus.getLabTotal(s);
		if (minLabTotal - level < 0.6 * (labTotal - level))
			return false;	// requisites take lab total down too far
		return true;
	}

	private boolean isValidFromText(Spell s) {
		if (s.getForm() != form && s.getRequisiteForm() != form) return false;
		if (s.getTechnique() != technique && s.getRequisiteTechnique() != form) return false;
		if (s.getLevel() != level) return false;

		for (Spell currentSpell : magus.getSpells()) {
			if (Math.abs(currentSpell.getLevel() - level) <=5 && s.getName().equals(currentSpell.getName())) 
				return false;	// know a similar spell of one magnitude less
		}
		return true;
	}
	
	@Override
	public void doStuff() {
		if (level == 0) return;
		if (labTextUsed == null) {
			int newPoints = labTotal - level;
			pointsAccumulatedSoFar += newPoints;
			magus.log("Works on spell research");
			magus.setCurrentSpellResearch(this);
		}
		if (labTextUsed != null || pointsAccumulatedSoFar >= level) {
			Spell spellInvented = new Spell(technique, form, level, spellName, magus);
			spellInvented.setRequisiteForm(requisiteForm);
			spellInvented.setRequisiteTechnique(requisiteTechnique);
			magus.addSpell(spellInvented);
			String logMessage = "Invents new spell: " + spellInvented.toString();
			if (labTextUsed != null) {
				labTextUsed.setCurrentReader(null);
				labTextUsed.isReadBy(magus);
				logMessage = logMessage + " using lab text by " + labTextUsed.getAuthor();
			}
			magus.log(logMessage);
			magus.setCurrentSpellResearch(null);
		}
		exposureXPForParticipants(technique, form, 2);
		for (Magus labAssistant : optionalActors) {
			labAssistant.log("Assists " + magus + " with spell invention: " + spellName);
		}
	}

	private void determineLevel(List<LabText> labTexts) {
		int seasonsToSpend = 1;
		int roll = 0;
		do {
			roll = Dice.roll(1, 6);
			if (roll > 3)
				seasonsToSpend++;
		} while (roll > 3);

		level = (seasonsToSpend * labTotal) / (1 + seasonsToSpend);
		level = (level / 5) * 5;

		// Now check to see if any Lab Text will best this
		for (LabText lt : labTexts) {
			Spell s = lt.getSpell();
			if ((s.getForm() == form || s.getRequisiteForm() == form)	&& (s.getTechnique() == technique || s.getRequisiteTechnique() == technique)) {
				if (s.getLevel() > level && magus.getLabTotal(s) >= s.getLevel()) {
					level = s.getLevel();
				}
			}
		}
	}

	private void determineTechniqueAndForm(List<LabText> availableLabTexts) {
		// We pick five totally random combinations, and pick the one with highest lab total
		for (int i = 0; i < 5; i++) {
			Arts t = Arts.randomTechnique();
			Arts f = Arts.randomForm();
			int lt = magus.getLabTotal(t, f);
			for (LabText text : availableLabTexts) {
				Spell s = text.getSpell();
				if ((s.getForm() == f || s.getRequisiteForm() == f)	&& (s.getTechnique() == t || s.getRequisiteTechnique() == t)) {
					int labTotalForThisSpell = magus.getLabTotal(s);
					if (text.getLevel() <= labTotalForThisSpell) {
						lt = Math.max(2 * s.getLevel(), lt);
						// a usable lab text means a spell can be learnt in one season (so effective lab total is 2x text level)
					}
				}
			}
			if (lt > labTotal) {
				labTotal = lt;
				technique = t;
				form = f;
			}
		}
		labTotal = magus.getLabTotal(technique, form);	
		// reset to avoid messing up level determination, which can lead to ignoring the text you planned to use!
	}

	private List<LabText> getAllUnknownSpellsWithAccessibleLabTexts() {
		List<Book> allBooks = magus.getAllAccessibleBooksNotInUse();
		List<LabText> allLabTexts = LabText.extractAllLabTextsFrom(allBooks);
		List<Spell> allSpells = magus.getSpells();
		List<LabText> alreadyInvented = new ArrayList<LabText>();
		for (LabText lt : allLabTexts) {
			if (allSpells.contains(lt.getSpell()))
				alreadyInvented.add(lt);
		}
		for (LabText lt : alreadyInvented)
			allLabTexts.remove(lt);
		return allLabTexts;
	}

	private static List<Spell> loadBasicSpells() {
		List<String> rawData = HopshackleUtilities.createListFromFile(new File(baseDir + File.separator + "BasicSpells.txt"));
		List<Spell> basicSpells = new ArrayList<Spell>();
		for (String nextLine : rawData) {
			String[] spellDetails = nextLine.split("\t");
			String name = spellDetails[0].trim();
			Arts technique = Arts.getArtFromAbbreviation(spellDetails[1].trim());
			Arts form = Arts.getArtFromAbbreviation(spellDetails[3].trim());
			Arts requisiteTechnique = Arts.getArtFromAbbreviation(spellDetails[2].trim());
			Arts requisiteForm = Arts.getArtFromAbbreviation(spellDetails[4].trim());
			int level = 0;
			if (spellDetails[5].trim().equals("Gen")) {
				for (int loop = 1; loop <= 12; loop++) {
					level = loop * 5;
					if (name.contains("(Form)")) {
						for (Arts reqForm : Arts.forms) {
							String newName = name.replace("Form", reqForm.getAbbreviation());
							Spell basicSpell = new Spell(technique, form, level, newName, null);
							basicSpell.setRequisiteForm(reqForm);
							basicSpell.setRequisiteTechnique(requisiteTechnique);
							basicSpells.add(basicSpell);
						}
					} else {
						Spell basicSpell = new Spell(technique, form, level, name, null);
						basicSpell.setRequisiteForm(requisiteForm);
						basicSpell.setRequisiteTechnique(requisiteTechnique);
						basicSpells.add(basicSpell);
					}
				}
			} else {
				try {
					level = Integer.valueOf(spellDetails[5]);
				} catch (NumberFormatException e) {
					logger.severe("Invalid spell level in basic spells: " + nextLine);
					continue;
				}
				if (level < 5) level = 5;
				Spell basicSpell = new Spell(technique, form, level, name, null);
				basicSpell.setRequisiteForm(requisiteForm);
				basicSpell.setRequisiteTechnique(requisiteTechnique);
				basicSpells.add(basicSpell);
			}
		}
		return basicSpells;
	}

	@Override
	protected void delete() {
		if (labTextUsed != null)
			labTextUsed.setCurrentReader(null);
	}
	
	public String description() {
		return String.format("%s%s%d %s %s", technique.getAbbreviation(), form.getAbbreviation(), level, spellName, (labTextUsed == null) ? "" : "[Lab Text]");
	}
}
