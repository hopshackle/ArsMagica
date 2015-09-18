package hopshackle.simulation.arsmagica;

import hopshackle.simulation.*;

import java.io.File;
import java.util.*;

public class Covenant extends Organisation<Magus> {

	private int magicAura;
	private static String baseDir = SimProperties.getProperty("BaseDirectory", "C:\\Simulations");
	private static Name covenantNamer = new Name(new File(baseDir + File.separator + "CovenantNames.txt"));
	private static AgentWriter<Covenant> covenantWriter = new AgentWriter<Covenant>(new CovenantDAO());
	private CovenantAgent covenantAgent;
	private CovenantVisPolicy visPolicy;
	private CovenantLibraryPolicy libraryPolicy;
	private CovenantServicePolicy servicePolicy;
	private int buildPoints;
	private int highlightsToDisplay = 12;
	private boolean fullDebug = false;
	private Tribunal tribunal;
	private HashMap<CovenantAttributes, Ability> covenantAttributes;

	public Covenant(List<Magus> founders, Tribunal trib) {
		super(covenantNamer.getName(), trib, founders);
		tribunal = trib;
		log("Located in " + parentLocation);
		covenantAgent = new CovenantAgent(this);
		visPolicy = new CovenantVisPolicy(this);
		libraryPolicy = new CovenantLibraryPolicy(this);
		servicePolicy = new CovenantServicePolicy(this);
		covenantWriter.setBufferLimit(50);
		if (trib != null) 
			trib.log("Covenant of " + getName() + " founded.");
		covenantAttributes = new HashMap<CovenantAttributes, Ability>();
	}

	public void setAura(int aura) {magicAura = aura;}
	public int getAura() {return magicAura;}
	public CovenantAgent getCovenantAgent() {return covenantAgent;}

	public void addItem(Artefact newItem) {
		if (fullDebug)
			log("Receives " + newItem);
		covenantAgent.addItem(newItem);
	}

	public List<Book> getLibrary() {
		return covenantAgent.getInventoryOf(AMU.sampleBook);
	}

	public List<VisSource> getVisSources() {
		return covenantAgent.getInventoryOf(AMU.sampleVisSource);
	}

	public int getAnnualVisSupply() {
		int total = 0;
		for (VisSource vis : getVisSources()) {
			total += vis.getAmountPerAnnum();
		}
		return total;
	}

	public int getYearFounded() {
		return (int) (getFounded() / 52);
	}

	@Override
	public void maintenance() {
		super.maintenance();
		if (covenantAgent != null) {
			covenantAgent.maintenance();
		}
		if (world.getYear() % 10 == 0) {
			covenantWriter.write(this, world.toString());
			logStatus();
		}
		if (isExtant()) {
			if (libraryPolicy != null)
				libraryPolicy.run();
			if (visPolicy != null)
				visPolicy.run();
			if (servicePolicy != null)
				servicePolicy.run();
			int size = getCurrentSize();
			int wealth = getLevelOf(CovenantAttributes.WEALTH);
			int grogs = getLevelOf(CovenantAttributes.GROGS);
			if (size > wealth * 2)
				addXP(CovenantAttributes.WEALTH, wealth * 2 - size);
			if (grogs > wealth) {
				addXP(CovenantAttributes.WEALTH, wealth - grogs);
				addXP(CovenantAttributes.GROGS, wealth - grogs);
			}
			libraryPolicy.processBookLimit((int) (Math.pow(wealth+1, 2) * 10));

			if (wealth < 2 || wealth < size * 2 && Math.random() < 0.05) {
				int currentService = servicePolicy.getServiceRequirement();
				if (currentService == 0)
					currentService = 100;
				if (currentService > 1) {
					servicePolicy.setServiceRequirement(currentService/2);
					log("Increases Service requirement.");
				}
			}
			if (Math.random() < 0.10) {
				int currentService = servicePolicy.getServiceRequirement();
				if (currentService == 0)
					return;
				servicePolicy.setServiceRequirement(currentService + 1);
				log("Decreases Service requirement.");
			}
		}
		buildPoints = calculateBuildPoints();
	}

	@Override
	public void newMember(Magus newMember) {
		super.newMember(newMember);
		log(newMember.toString() + " joins covenant");
		if (tribunal != null)
			tribunal.log(newMember.toString() + " joins "  + this.getName());
	}

	@Override
	public void memberLeaves(Magus exMember) {
		super.memberLeaves(exMember);
		if (exMember.isDead()) {
			log(exMember.toString() + " dies");
		} else {
			log(exMember.toString() + " leaves covenant");
			exMember.setLocation(tribunal);
			if (tribunal != null)
				tribunal.log(exMember.toString() + " leaves "  + this.getName());
		}
		if (getCurrentSize() == 0) {
			log("Covenant is dissolved");
			setParentLocation(null);
			covenantAgent.die("Dissolved");
			tribunal.log("Covenant of " + getName() + " is dissolved.");
			tribunal = null;
		}
	}

	@Override
	public String toString() {
		return "Covenant of " + getName() + " [" + getUniqueID() + "] in " + getTribunal();
	}


	private void logStatus() {
		log("");
		log("Aura: " + getAura());
		log("All members: ");
		for (Agent member : getCurrentMembership()) {
			String message = "\t\t" + member.toString() + "\tAge: " + member.getAge();
			if (((Magus)member).isInTwilight()) 
				message = message + " (In Twilight)";
			log(message);
		}
		log("");
		for (CovenantAttributes attribute : CovenantAttributes.values()) {
			int tabs = 3 - (attribute.toString().length() / 8);
			String tabString = "";
			for (int i = 0; i < tabs; i++)
				tabString = tabString + "\t";
			log(attribute + tabString + getLevelOf(attribute));
		}
		log("Service: \t\t" + servicePolicy.getServiceRequirement());

		log("");
		List<Book> library = getLibrary();
		if (!library.isEmpty()) {
			log("Library: (" + library.size() + " volumes)");
			Book.SortInOrderOfValue(library);
			// First Summae and Tractatus

			List<Book> libraryBySubject = getLibrary();
			Collections.sort(libraryBySubject, new Comparator<Book>() {

				@Override
				public int compare(Book b1, Book b2) {
					Learnable s1 = b1.getSubject();
					Learnable s2 = b2.getSubject();
					if (s1 instanceof Arts) {
						if (s2 instanceof Arts) {
							return ((Arts) s1).getOrder() - ((Arts)s2).getOrder();
						} else
							return -1;
					} else if (s1 instanceof Abilities) {
						if (s2 instanceof Abilities) 
							return ((Abilities) s1).toString().compareTo(((Abilities)s2).toString());
						else if (s2 instanceof Arts)
							return 1;
						else
							return -1;
					} else {
						if (s2 instanceof Arts || s2 instanceof Abilities)
							return 1;
						else
							return 0;
					}
				}
			});

			// Summary
			int spellLevels = 0;
			Learnable currentSubject = null;
			Book highestLevelSumma = AMU.sampleBook;
			Book highestQualitySumma = AMU.sampleBook;
			List<Integer> tractatus = new ArrayList<Integer>();
			for (Book book : libraryBySubject) {
				Learnable subject = book.getSubject();
				if (book instanceof LabText) 
					spellLevels += book.getLevel();
				else {
					if (currentSubject == null || !currentSubject.equals(subject)) {
						printOutputSummary(currentSubject, highestLevelSumma, highestQualitySumma, tractatus);
						currentSubject = subject;
						highestLevelSumma = AMU.sampleBook;
						highestQualitySumma = AMU.sampleBook;
						tractatus = new ArrayList<Integer>();
					}
					if (book instanceof Summa) {
						if (book.getLevel() + book.getQuality() / 100.0 > highestLevelSumma.getLevel() + highestLevelSumma.getQuality() / 100.0)
							highestLevelSumma = book;
						if (book.getLevel() / 100.0 + book.getQuality() > highestQualitySumma.getLevel() / 100.0+ highestQualitySumma.getQuality())
							highestQualitySumma = book;
					} else {
						tractatus.add(book.getQuality());
					}
				}
			}
			printOutputSummary(currentSubject, highestLevelSumma, highestQualitySumma, tractatus);
			log("");
			if (spellLevels > 0) {
				log(String.format("\tTotal Spell Levels: %d",spellLevels));
				log("");
			}

			int count = 0;
			for (Book book : library) {
				log("\t\t" + book.toString());
				count++;
				if (count >= highlightsToDisplay)
					break;
			}
			log("");
		}
		if (!getVisSources().isEmpty()) {
			log("Vis Sources: ");
			for (VisSource vs : getVisSources()) {
				log("\t\t" + vs.toString());
			}
			log("");
		}
	}

	private void printOutputSummary(Learnable subject, Book highestLevelSumma, Book highestQualitySumma,	List<Integer> tractatus) {
		if (subject == null) return;
		String subjectTitle = subject.toString();
		if (subject instanceof Arts)
			subjectTitle = ((Arts)subject).getAbbreviation();
		StringBuffer outputString = new StringBuffer(String.format("\t%15s : ", subjectTitle));
		if (highestLevelSumma.getLevel() != 0) {
			String temp = String.format("L%d Q%d", highestLevelSumma.getLevel(), highestLevelSumma.getQuality());
			outputString.append(temp);
			for (int i = 0; i < (8 - temp.length()); i++)
				outputString.append(" ");
		} else 
			outputString.append("\t");
		if (highestQualitySumma.getLevel() != 0 && highestQualitySumma != highestLevelSumma) {
			String temp = String.format("L%d Q%d", highestQualitySumma.getLevel(), highestQualitySumma.getQuality());
			outputString.append(temp);
			for (int i = 0; i < (8 - temp.length()); i++)
				outputString.append(" ");
			outputString.append("\t");
		} else
			outputString.append("\t\t");
		Collections.sort(tractatus);
		for (int quality : tractatus)
			outputString.append("Q" + quality + " ");
		if (outputString.length() > 0)
			log(outputString.toString());

	}

	public void log(String logMessage) {
		if (covenantAgent != null) {
			covenantAgent.setDebugLocal(true);
			covenantAgent.log(logMessage);
			covenantAgent.setDebugLocal(false);
		}
	}

	public int calculateIncrementalBuildPointsFrom(List<Book> potentialLibraryAdditions) {
		List<Book> fullLibrary = getLibrary();
		fullLibrary.addAll(potentialLibraryAdditions);
		int fullPoints = calculateLibraryPoints(fullLibrary);
		int startingPoints = calculateLibraryPoints(getLibrary());
		return fullPoints - startingPoints;
	}


	public int calculateIncrementalBuildPointsFrom(Book b) {
		List<Book> books = new ArrayList<Book>();
		books.add(b);
		return calculateIncrementalBuildPointsFrom(books);
	}
	private int calculateLibraryPoints(List<Book> library) {
		int retValue = 0;
		Map<Learnable, Book> highestSummaPerSubject = new HashMap<Learnable, Book>();
		Set<Spell> spellsInLibrary = new HashSet<Spell>();
		Set<Integer> tractatusInLibrary = new HashSet<Integer>();
		for (Book book : library) {
			if (book instanceof LabText) {
				LabText lt = (LabText) book;
				if (!spellsInLibrary.contains(lt.getSpell()))
					spellsInLibrary.add(lt.getSpell());
				else 
					continue;
			} else {
				Learnable bookSubject = book.getSubject();
				if (book instanceof Tractatus) {
					Tractatus tract = (Tractatus)book;
					if (!tractatusInLibrary.contains(tract.getTitleId()))
						tractatusInLibrary.add(tract.getTitleId());
					else
						continue;
				}
				if (book instanceof Summa) {
					if (!highestSummaPerSubject.containsKey(bookSubject)) {
						highestSummaPerSubject.put(bookSubject, book);
					} else {
						Book previousBestBook = highestSummaPerSubject.get(bookSubject);
						int previousPointsValue = previousBestBook.getBPValue();
						if (book.getBPValue() > previousPointsValue) {
							retValue -= previousPointsValue;
							highestSummaPerSubject.put(bookSubject, book);
						} else {
							continue;
						}
					}
				}
			}
			retValue += book.getBPValue();
		}
		for (Learnable l : highestSummaPerSubject.keySet()) {
			int seasonsToCompleteStudy = AMU.getSeasonsToMaxFrom(l, library);
			int level = highestSummaPerSubject.get(l).getLevel();
			// set a baseline of Q8
			int baselineSeasons = (int) Math.ceil(l.getXPForLevel(level) / 8.0);
			retValue += (baselineSeasons - seasonsToCompleteStudy);
		}
		return retValue;
	}

	private int calculateBuildPoints() {
		int retValue = 0;
		retValue += calculateLibraryPoints(getLibrary())/2;
		retValue += (getAnnualVisSupply() * 5);
		for (int i = 1; i <= getAura(); i++) 
			retValue += i * 10; 
		for (CovenantAttributes attribute : CovenantAttributes.values()) {
			retValue += getLevelOf(attribute) * 3;
		}
		return retValue;
	}

	public int getBuildPoints() {return buildPoints;}

	public Tribunal getTribunal() {
		return (Tribunal) getParentLocation();
	}

	public void setTribunal(Tribunal tribunal) {
		setParentLocation(tribunal);
	}

	public boolean getFullDebug() {return fullDebug;}

	public int getLevelOf(CovenantAttributes attribute) {
		if (covenantAttributes.containsKey(attribute))
			return covenantAttributes.get(attribute).getLevel();
		return 0;
	}

	public int getTotalXPIn(CovenantAttributes attribute) {
		if (covenantAttributes.containsKey(attribute))
			return covenantAttributes.get(attribute).getTotalXP();
		return 0;
	}

	public void addXP(CovenantAttributes attribute, int xp) {
		if (covenantAttributes.containsKey(attribute))
			covenantAttributes.get(attribute).addXP(xp);
		else {
			Ability a = new Ability(attribute, 0, covenantAgent);
			a.addXP(xp);
			covenantAttributes.put(attribute, a);
		}
	}

	public int getServiceLevel() {
		return servicePolicy.getServiceRequirement();
	}
	public double getNeedForLibraryMaintenance() {
		return libraryPolicy.getNeedForLibraryMaintenance();
	}
	public double getNeedForLabTextMaintenance() {
		return libraryPolicy.getNeedForLabTextMaintenance();
	}
	public boolean isLibraryFull() {
		return libraryPolicy.isLibraryFull();
	}
}

