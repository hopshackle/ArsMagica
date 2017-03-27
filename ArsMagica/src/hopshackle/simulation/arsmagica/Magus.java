package hopshackle.simulation.arsmagica;

import java.io.File;
import java.util.*;

import hopshackle.simulation.*;

public class Magus extends Agent implements Persistent {

	private ArsMagicaCharacteristic strength, stamina, dexterity, quickness, intelligence, perception, presence, communication;
	private HashMap<Learnable, Skill> skills = new HashMap<Learnable, Skill>();
	private Map<Magus, Relationship> relationships = new HashMap<Magus, Relationship>();
	private List<Spell> spells = new ArrayList<Spell>();
	private static DatabaseWriter<Magus> magusWriter = new DatabaseWriter<Magus>(new MagusDAO());
	private static MagusRetriever masterAgentRetriever = new MagusRetriever();
	private int lastHarvest, magicAura;
	private Magus apprentice, parens;
	private boolean currentlyInTwilight;
	private boolean longevityContractOnOffer;
	private long apprenticeshipStart;
	protected static Name magusNamer = new Name(new File(baseDir + File.separator + "MagusNames.txt"));
	private String name;
	private WriteSumma currentBook;
	private InventSpell currentSpellResearch;
	private CopyBook currentCopyProject;
	private int apparentAge = 35;
	private int seasonsTraining = 0;
	private int longevityRitual = 0, knownLongevityRitual = 0;
	private int seasonsInTwilight = 0;
	private int[] twilightScars = new int[2];
	private Covenant covenant;
	private String parensName;
	private Tribunal tribunal;
	private HermeticHouse house;
	private int seasonsServiceOwed;
	private MagusLibraryPolicy libraryPolicy = new MagusLibraryPolicy(this);
	private Set<Integer>  tractatusRead = new HashSet<Integer>();
	private Map<Learnable, Integer> tractatusWritten = new HashMap<Learnable, Integer>();
	private MagusPreferences researchGoals;
	private boolean uniformResearchPreferences = SimProperties.getProperty("MagusUniformResearchPreferences", "false").equals("true");
	private static Policy<Action<Magus>> defaultActionPolicy = new MagusActionPolicy();
	
	public Magus(Location l, BaseDecider<Magus> d, World world) {
		super(l, d, world);
		name = magusNamer.getName();

		// need to decide on Tribunal
		Set<Tribunal> tSet = world.getAllChildLocationsOfType(AMU.sampleTribunal);
		Tribunal[] allTribunals = tSet.toArray(new Tribunal[1]);
		int roll = Dice.roll(1, allTribunals.length) - 1;
		setTribunal(allTribunals[roll]);
		log("Moves to Tribunal of " + tribunal);

		rollStatistics(2);

		setPolicy(new MagusApprenticeInheritance());
		setPolicy(defaultActionPolicy);
		this.setDebugLocal(true);
		log(this.toString());
		agentRetriever = masterAgentRetriever;
		researchGoals = new MagusPreferences(uniformResearchPreferences);
	}

	public void rollStatistics(int attempts) {
		int statRollsLeft = attempts;
		int bestScore = -1000;
		int currentScore = 0;
		do {
			int tempStr = new Attribute(Dice.roll(3, 6)).getMod();
			int tempSta = new Attribute(Dice.roll(3, 6)).getMod();
			int tempDex = new Attribute(Dice.roll(3, 6)).getMod();
			int tempQik = new Attribute(Dice.roll(3, 6)).getMod();
			int tempInt = new Attribute(Dice.roll(3, 6)).getMod();
			int tempPer = new Attribute(Dice.roll(3, 6)).getMod();
			int tempPrs = new Attribute(Dice.roll(3, 6)).getMod();
			int tempCom = new Attribute(Dice.roll(3, 6)).getMod();
			currentScore = tempInt * 20 + tempCom * 4 + tempSta * 5 + tempPer * 5 + tempQik + tempDex + tempPrs * 3 + tempStr;
			statRollsLeft--;
			if (currentScore > bestScore) {
				bestScore = currentScore;
				strength = new ArsMagicaCharacteristic(tempStr);
				stamina = new ArsMagicaCharacteristic(tempSta);
				dexterity = new ArsMagicaCharacteristic(tempDex);
				quickness = new ArsMagicaCharacteristic(tempQik);
				intelligence = new ArsMagicaCharacteristic(tempInt);
				perception = new ArsMagicaCharacteristic(tempPer);
				presence = new ArsMagicaCharacteristic(tempPrs);
				communication = new ArsMagicaCharacteristic(tempCom);
			}
		} while (statRollsLeft > 0);
	}

	public Magus(World world) {
		this(new Location(world), new BasicDecider(), world);
	}

	public Magus(String name, World world, long uniqueID, long parens, List<Long> apprentices) {
		// only for resurrecting a magus from db
		super(world, uniqueID, parens, 0, apprentices);
		this.name = name;
	}

	public int getPawnsOf(Arts visType) {
		HashMap<Arts, Integer> visInventory = AMU.getVisInventory(this);
		Integer retValue = visInventory.get(visType);
		if (retValue == null)
			retValue = 0;
		return retValue;
	}

	public int getLevelOf(Learnable skill) {
		Skill s = skills.get(skill);
		if (s == null) return 0;
		return s.getLevel();
	}

	public int getTotalXPIn(Learnable skill) {
		Skill s = skills.get(skill);
		if (s == null) return 0;
		return s.getTotalXP();
	}

	public int getUnusedXPIn(Learnable skill) {
		Skill s = skills.get(skill);
		if (s == null) return 0;
		return s.getUnusedXP();
	}

	public int getXPToNextLevelIn(Learnable skill) {
		Skill s = skills.get(skill);
		if (s == null) return skill.getMultiplier();
		return s.getXPtoNextLevel();
	}

	public void addXP(Learnable skill, int xp) {
		Skill s = skills.get(skill);
		if (s == null) {
			if (skill instanceof Abilities) 
				s = new Ability(skill, 0, this);
			else
				s = new Art(skill, 0, this);
		}
		s.addXP(xp);
		skills.put(skill, s);	
	}

	public void addVis(Arts visType, int pawns) {
		if (pawns == 0) return;
		boolean debugLevel = debug_this;
		setDebugLocal(false);
		for (int i = 0; i < pawns; i++) 
			inventory.add(new Vis(visType));
		setDebugLocal(debugLevel);
		log("Receives " + pawns + " pawns of " + visType);
	}

	public void removeVis(Arts visType, int requiredPawns) {
		if (requiredPawns == 0) return;
		List<Artefact> toRemove = new ArrayList<Artefact>();
		int removed = 0;
		for (Artefact item : inventory) {
			if (item.isA(AMU.sampleVis)) {
				Arts type = ((Vis) item).getType();
				if (type == visType) {
					toRemove.add(item);
					removed++;
					if (removed >= requiredPawns)
						break;
				}
			}
		}
		for (Artefact item : toRemove) {
			inventory.remove(item);
		}
		// Note that no error is thrown if the Magus has insufficient vis
	}


	public int getMagicAura() {
		if (covenant != null)
			return covenant.getAura();
		return magicAura;
	}

	public void setMagicAura(int newLevel) {
		magicAura = newLevel;
	}

	public int getTotalArtLevels() {
		int total = 0;
		for (Learnable l : skills.keySet()) {
			if (l instanceof Arts) 
				total += getLevelOf(l);
		}
		return total;
	}

	@Override
	public void maintenance() {
		super.maintenance();
		// Gain income from Vis Sources, and age once per year (usually in Winter, but could be a season or two later depending on Twilight)
		if (lastHarvest < world.getYear()) {
			lastHarvest = world.getYear();
			if (world.getYear() % 10 == 0 && !isDead() && !isApprentice()) {
				if (!isInTwilight())
					logStats();
				magusWriter.write(this, world.toString());
			}
			if (getAge() > 35 && !isInTwilight()) {
				new AgeingEvent(this).ageOneYear();
			}
		}

		// Apprentice graduates if 15 years have elapsed
		if (hasApprentice() && apprentice.getYearsSinceStartOfApprenticeship() >= 15 && apprentice.seasonsTraining >= 15) {
			terminateApprenticeship(true);
		}

		if (!isDead()) {
			if (getLevelOf(Abilities.DECREPITUDE) >= 5)
				die("Dies of Very Old Age at " + getAge() + " (Decrepitude 5).");

			if (!isInTwilight()) {
				libraryPolicy.run();
				if (!longevityContractOnOffer && getLabTotal(Arts.CREO, Arts.CORPUS) > 30) {
					int reservePrice = getInventoryOf(AMU.sampleVis).size() / 3; 
					tribunal.addToMarket(new BarterOffer(this,
							new LongevityRitualService(this), 1, reservePrice,
							new VisValuationFunction(this)));
				}
				if (hasApprentice())  {
					List<Magus> both = new ArrayList<Magus>();
					both.add(this); both.add(apprentice);
					new SocialMeeting(both, 2, 2);
				}
			}
		}
	}

	public void incrementSeasonsTraining() {
		seasonsTraining++;
	}

	public int getLongevityModifier() {
		// positive means beneficial
		int base = 0;
		int familiarBonus = 0;
		if (hasFamiliar())
			familiarBonus = getFamiliar().getBronze();
		if (covenant != null)
			base = Math.min(covenant.getLevelOf(CovenantAttributes.WEALTH), 2);
		return base - (int) Math.ceil((getAge() - getYearsInTwilight()) / 10.0) + longevityRitual + familiarBonus;
	}
	public int getYearsSinceStartOfApprenticeship() {
		return (int) ((world.getCurrentTime() - apprenticeshipStart) / 52);
	}

	@Override
	public void die(String reason) {
		// Apprentice is now dealt with as part of inheritance (i.e. as for any other possessed object)
		super.die(reason);

		if (covenant != null)
			setCovenant(null);

		for (Magus m : relationships.keySet()) {
			if (!m.isDead()) m.setRelationship(this, Relationship.NONE);
			// reset relationships of all friends / enemies
		}

		String logFileName = toString() + " (" + (getBirth()/52) + " - " + death / 52 + ")";
		logger.rename(logFileName);
		this.setDebugLocal(false);
	}

	public void terminateApprenticeship(boolean successful) {
		if (successful) {
			apprentice.log("Completes apprenticeship with " + toString() + " and becomes full Magus");
			apprentice.logStats();
			log("Apprentice " + apprentice + " successfully completes apprenticeship");
		} else {
			log("Apprentice " + apprentice + " has apprenticeship terminated early");
			apprentice.log("Apprenticeship with " + toString() + " terminates early");
		}
		apprentice.apprenticeshipEnds();
		apprentice = null;
	}
	public void addApprentice(Magus apprentice) {
		if (this.apprentice != null) {
			errorLogger.severe("Magus attempting to recruit apprentice when position is already filled. " + toString());
			return;
		}
		log("Finds new Apprentice: " + apprentice);
		this.apprentice = apprentice;
		if (apprentice.getParens() != null)
			apprentice.getParens().apprentice = null;
		apprentice.setApprenticeOf(this);
		children.add(apprentice.getUniqueID());
		// TODO: Removed code that checked old action queue for SearchForApprentice
		// To be reviewed. I'm hoping that the new .start() and .run() on actions may resolve this
		// or else we'll cover it in .maintenance() when forward plans are reviewed for relevance
	}
	private void setApprenticeOf(Magus parens) {
		if (parens == null)
			errorLogger.severe("Null parens assignment for " + this);
		this.parens = parens;
		parents.add(parens.getUniqueID());
		parensName = parens.toString();
		if (parens.getHermeticHouse() != null)
			parensName = parensName.substring(0, parensName.indexOf(','));
		String covenantString = parens.getCovenant()!=null ? " at " + parens.getCovenant() : "";
		log("Is apprenticed by " + parens.toString() + " at age of " + getAge() + covenantString);
		setTribunal(parens.getTribunal());
		if (apprenticeshipStart == 0)
			apprenticeshipStart = world.getCurrentTime();
		if (parents.size() == 1) {
			researchGoals = new MagusPreferences(parens.researchGoals);
		}
		setHermeticHouse(parens.getHermeticHouse());
	}
	public boolean hasApprentice() {
		return apprentice != null;
	}
	private void apprenticeshipEnds() {
		if (parens == null) {
			errorLogger.severe("Null parens when ending apprenticeship of " + toString());
		}

		if (parens != null && parens.getCovenant() != null) {
			Covenant parensCovenant = parens.getCovenant();
			CovenantApplication application = new CovenantApplication(parensCovenant, this);
			if (application.isSuccessful())
				application.acceptApplication();
		}

		if (getCovenant() == null) {
			// instead decide on a Tribunal
			Tribunal trib = getFavouredTribunal();
			if (trib != tribunal)
				setTribunal(trib);
		}

		parens = null;
		getActionPlan().purgeActions(false);		// remove any future plans, and make new decision if needed
															// but do not cancel any executing actions
	}

	public Tribunal getFavouredTribunal() {
		Set<Tribunal> allTribunals = getWorld().getAllChildLocationsOfType(AMU.sampleTribunal);
		for (Tribunal t : allTribunals) 
			addKnowledgeOfLocation(t);

		Tribunal bestTribunal = null;
		if (parens!=null) bestTribunal = parens.getTribunal();
		int bestScore = -99;
		for (Tribunal trib : allTribunals) {
			int distance = AStarPathFinder.findRoute(this, tribunal, trib, null).size();
			int score = trib.getApprenticeModifier() / 3 + trib.getVisModifier();
			score -= distance * 3;
			score += Dice.stressDieResult();
			if (score > bestScore) {
				bestTribunal = trib;
				bestScore = score;
			}
		}
		if (bestTribunal == null) {
			bestTribunal = allTribunals.toArray(new Tribunal[1])[0];
		}
		return bestTribunal;
	}
	public boolean isApprentice() {
		return parens != null;
	}

	public void logStats() {
		String twilightString = "";
		if (seasonsInTwilight > 3) {
			twilightString = "(Tw: "+seasonsInTwilight / 4+")";
		}
		log(String.format("Age: %d (%d) %s", getAge(), getApparentAge(), twilightString));
		log(String.format("Int %+d, Per %+d, Str %+d, Sta %+d, Prs %+d, Com %+d, Dex %+d, Qik %+d", 
				getIntelligence(), getPerception(), getStrength(), getStamina(), getPresence(), getCommunication(), getDexterity(), getQuickness()));

		log(String.format("Cr %d, In %d, Mu %d, Pe %d, Re %d", getLevelOf(Arts.CREO), getLevelOf(Arts.INTELLEGO), getLevelOf(Arts.MUTO), getLevelOf(Arts.PERDO), getLevelOf(Arts.REGO)));
		log(String.format("An %d, Aq %d, Au %d, Co %d, He %d, Ig %d, Im %d, Me %d, Te %d, Vi %d", 
				getLevelOf(Arts.ANIMAL), getLevelOf(Arts.AQUAM), getLevelOf(Arts.AURAM), getLevelOf(Arts.CORPUS), getLevelOf(Arts.HERBAM),
				getLevelOf(Arts.IGNEM), getLevelOf(Arts.IMAGINEM), getLevelOf(Arts.MENTEM), getLevelOf(Arts.TERRAM), getLevelOf(Arts.VIM)));

		List<Abilities> abilities = new ArrayList<Abilities>();
		for (Learnable l : skills.keySet()) {
			if (l instanceof Abilities)
				abilities.add((Abilities) l);
		}
		Collections.sort(abilities, new Comparator<Abilities>() {

			@Override
			public int compare(Abilities o1, Abilities o2) {
				return getTotalXPIn(o2) - getTotalXPIn(o1);
			}

		});

		for (Abilities a : abilities) {
			if (a == Abilities.VIS_HUNT || a == Abilities.FAMILIAR_HUNT) continue;
			log(skills.get(a).toString());
		}


		if (spells.size() > 0) {
			log("Spells:");
			Collections.sort(spells, new Comparator<Spell>() {

				@Override
				public int compare(Spell o1, Spell o2) {
					return o2.getLevel() - o1.getLevel();
				}
			});
			for (Spell s : spells) {
				log("     " + s);
			}
		}

		Map<Arts, Integer> visStores = AMU.getVisInventory(this);
		StringBuffer visString = new StringBuffer();
		for (Arts art : Arts.values()) {
			if (visStores.get(art) > 0)
				visString.append(art.getAbbreviation() + ": " + visStores.get(art) + ", ");
		}
		log("Aura: " + getMagicAura());
		if (visString.length() > 0) {
			visString.delete(visString.length()-1, visString.length());
			log("Vis Stores :-  " + visString.toString());
		}
	}

	@Override
	public double getScore() {
		return 0;
	}

	@Override
	public double getMaxScore() {
		return 0;
	}

	public int getLabTotal(Arts technique, Arts form) {
		List<Magus> defaultAssistants = new ArrayList<Magus>();
		if (hasApprentice()) defaultAssistants.add(apprentice);
		return getLabTotal(technique, form, defaultAssistants);
	}

	public int getLabTotal(Arts technique, Arts form, List<Magus> assistants) {
		if (hasApprentice() && getLevelOf(Abilities.LEADERSHIP) >= assistants.size() && !assistants.contains(apprentice)) assistants.add(apprentice);
		int labTotal = getLevelOf(technique) + getLevelOf(form) + getMagicAura() + getIntelligence() + getLevelOf(Abilities.MAGIC_THEORY);
		for (Magus assistant : assistants)
			labTotal += assistant.getIntelligence() + assistant.getLevelOf(Abilities.MAGIC_THEORY);
		return labTotal;
	}

	public int getLabTotal(Spell s)  {
		Arts primaryT = s.getTechnique();
		Arts primaryF = s.getForm();
		Arts requisiteT = s.getRequisiteTechnique();
		Arts requisiteF = s.getRequisiteForm();
		int retValue = getLabTotal(primaryT, primaryF);
		if (requisiteT != null) 
			retValue = Math.min(retValue, getLabTotal(requisiteT, primaryF));
		if (requisiteF != null)
			retValue = Math.min(retValue, getLabTotal(primaryT, requisiteF));
		if (requisiteT != null && requisiteF != null) 
			retValue = Math.min(retValue, getLabTotal(requisiteT, requisiteF));
		return retValue;
	}

	public int getIntelligence() { return intelligence.getModifier();}
	public int getPerception() {return perception.getModifier();}
	public int getStrength() {return strength.getModifier();}
	public int getStamina() {return stamina.getModifier();}
	public int getDexterity() {return dexterity.getModifier();}
	public int getCommunication() {return communication.getModifier();}
	public int getPresence() {return presence.getModifier();}
	public int getQuickness() {return quickness.getModifier();}
	public ArsMagicaCharacteristic getIntelligenceAMC() { return intelligence;}
	public ArsMagicaCharacteristic getPerceptionAMC() {return perception;}
	public ArsMagicaCharacteristic getStrengthAMC() {return strength;}
	public ArsMagicaCharacteristic getStaminaAMC() {return stamina;}
	public ArsMagicaCharacteristic getDexterityAMC() {return dexterity;}
	public ArsMagicaCharacteristic getCommunicationAMC() {return communication;}
	public ArsMagicaCharacteristic getPresenceAMC() {return presence;}
	public ArsMagicaCharacteristic getQuicknessAMC() {return quickness;}

	public void setIntelligence(int value) { intelligence.setModifier(value);}
	public void setPerception(int value) {perception.setModifier(value);}
	public void setStrength(int value) {strength.setModifier(value);}
	public void setStamina(int value) {stamina.setModifier(value);}
	public void setDexterity(int value) {dexterity.setModifier(value);}
	public void setCommunication(int value) {communication.setModifier(value);}
	public void setPresence(int value) {presence.setModifier(value);}
	public void setQuickness(int value) {quickness.setModifier(value);}

	public Magus getApprentice() {
		return apprentice;
	}
	public Magus getParens() {
		if (parens != null)
			return parens;
		if (parents.isEmpty())
			return null;	
		// else we always regard the parens as the magus with whom apprenticeship is terminated
		return (Magus) Agent.getAgent(parents.get(parents.size()-1), agentRetriever, world);
	}
	public String getName() {
		return name;
	}
	public void setName(String newName) {
		name = newName;
	}
	public String getParensName() {
		return parensName;
		// a bit of a hack to store this locally to avoid an iterative search through all ancestors
		// if parens is dead and no longer in cache
	}

	public String toString() {
		StringBuffer temp = new StringBuffer();
		if (name != null)
			temp.append(name);
		Magus p = getParens();
		if (p != null) {
			temp.append(" filius of " + p.getName());
		}
		if (house != null) {
			String houseName = house.toString().toLowerCase();
			houseName = houseName.substring(0, 1).toUpperCase() + houseName.substring(1);
			temp.append(", of House " + houseName);
		}
		temp.append(" [" + getUniqueID() + "]");
		return temp.toString();
	}


	public Abilities selectAbilityToPractise() {
		Map<Learnable, Double> options = new HashMap<Learnable, Double>();
		for (Abilities ability : Abilities.values())
			options.put(ability, 1.0 / (getLevelOf(ability) + 1.0));

		Abilities retValue = (Abilities) MagusPreferences.getPreferenceGivenPriors(this, options);
		if (isApprentice() && retValue == Abilities.PARMA_MAGICA)
			retValue = Abilities.MAGIC_THEORY;

		if (getLevelOf(Abilities.MAGIC_THEORY) < getLevelOf(getHighestArt()) / 5)
			retValue = Abilities.MAGIC_THEORY;
		else if (getLevelOf(Abilities.LATIN) < 4)
			retValue = Abilities.LATIN;
		else if (getLevelOf(Abilities.ARTES_LIBERALES) < 2)
			retValue = Abilities.ARTES_LIBERALES;

		return retValue;
	}

	public Arts getHighestArt() {
		int highestLevel = 0;
		Arts highestArt = Arts.CREO;
		for (Arts type : Arts.values()) {
			if (getLevelOf(type) > highestLevel) {
				highestArt = type;
				highestLevel = getLevelOf(type);
			}
		}
		return highestArt;
	}


	public int getHighestSumma(Learnable skill) {
		return Math.max(getHighestReadableSumma(skill), getHighestSummaOnMarket(skill));
	}

	public int getHighestReadableSumma(Learnable skill) {
		List<Book> accessibleLibrary = getAllAccessibleBooks();
		accessibleLibrary = Summa.filterRelevantSummae(accessibleLibrary, skill);
		return AMU.getHighestSummaFrom(skill, accessibleLibrary);
	}

	public int getHighestSummaOnMarket(Learnable skill) {
		return AMU.getHighestSummaFrom(skill, getInventoryOnMarketOf(AMU.sampleBook));
	}

	public List<Book> getAllAccessibleBooksNotInUse() {
		List<Book> retValue = getAllAccessibleBooks();
		List<Book> inUse = new ArrayList<Book>();
		for (Book b : retValue) {
			if (b.isInUse())
				inUse.add(b);
		}
		for (Book toRemove : inUse) {
			retValue.remove(toRemove);
		}
		return retValue;
	}

	public List<Book> getAllAccessibleBooks() {
		List<Book> library = new ArrayList<Book>();
		for (Artefact book : getInventoryOf(AMU.sampleBook)) {
			library.add((Book) book);
		}
		if (covenant != null)
			library.addAll(covenant.getLibrary());
		if (isApprentice()) {
			library.addAll(parens.getAllAccessibleBooks());
		}
		return library;
	}

	@Override
	public void addItem(Artefact item) {
		boolean originalDebug = debug_this;
		setDebugLocal(false);
		super.addItem(item);
		setDebugLocal(originalDebug);
	}

	public Map<Learnable, Skill> getSkills() {
		return HopshackleUtilities.cloneMap(skills);
	}

	public Book getBestBookToRead() {
		// simple algorithm of percentage xp gain (with base of 10 at start)
		// Abilities should have a slightly lower preference, so no need to modify these
		if (getLevelOf(Abilities.LATIN) < 4) return null;
		if (getLevelOf(Abilities.ARTES_LIBERALES) < 1) return null;
		List<Book> library = getAllAccessibleBooksNotInUse();
		Map<Learnable, Double> options = new HashMap<Learnable, Double>();
		Map<Learnable, Book> bestBook = new HashMap<Learnable, Book>();

		int magicTheory = getLevelOf(Abilities.MAGIC_THEORY);
		boolean magicTheoryBoost = magicTheory <= (getAge() / 10) + 1 && magicTheory >= (getAge() / 10) - 2; 
		for (Book book : library) {
			if (book instanceof LabText) continue;
			Learnable subject = book.getSubject();
			double bestScore = 0.0;
			if (options.containsKey(subject))
				bestScore = options.get(subject);
			double bookValue = book.getXPGainForMagus(this) / (getTotalXPIn(book.getSubject()) + 10.0);
			if (book instanceof Summa)
				bookValue *= 1.3;	// give preference to Summa over Tractatus
			if (subject == Abilities.MAGIC_THEORY && magicTheoryBoost)
				bookValue *= 5;	// to try and make Magic Theory keep up with age for longevity rituals
			if (bookValue >  bestScore) {
				options.put(subject, bookValue);
				bestBook.put(subject, book);
			}
		}
		Learnable decision = MagusPreferences.getPreferenceGivenPriors(this, options);
		return bestBook.get(decision);
	}


	public Arts getTypeOfVisToStudy() {
		// first find out what types of Vis the magus has
		// and pick our favourite amongst those types for which we have sufficient vis
		// above all we always retain sufficient for a longevity ritual in an emergency
		boolean needsLongevityRitual =  true ; // getLongevityRitualEffect() == 0 || getLongevityModifier() < -2;
		HashMap<Arts, Integer> vis = AMU.getVisInventory(this);
		int magicTheory = getLevelOf(Abilities.MAGIC_THEORY);
		Map<Learnable, Double> options = new HashMap<Learnable, Double>();
		for (Arts art : vis.keySet()) {
			int artLevel = getLevelOf(art);
			int pawnsNeeded = (int) Math.max(Math.ceil(artLevel / 5.0), 1.0);
			int actualPawns = vis.get(art);
			if (needsLongevityRitual && (art == Arts.CREO || art == Arts.CORPUS || art == Arts.VIM)
					&& actualPawns - pawnsNeeded < Math.ceil(getAge()/10.0))
				continue;	// best to keep for longevity ritual
			if (pawnsNeeded <= magicTheory * 2 && pawnsNeeded <= actualPawns) {
				options.put(art, 1.0 / (getTotalXPIn(art) + 10.0));	// as we always get the same absolute XP if we have the requisite pawns of vis
			}
		}
		if (options.isEmpty()) return null;
		return (Arts) MagusPreferences.getPreferenceGivenPriors(this, options);
	}

	public boolean isWritingBook() {
		return currentBook != null;
	}

	public WriteSumma getCurrentBookProject() {
		return currentBook;
	}

	public void setCurrentBookProject(WriteSumma newProject) {
		currentBook = newProject;
	}

	public boolean isResearchingSpell() {
		return currentSpellResearch != null;
	}

	public InventSpell getCurrentSpellResearchProject() {
		return currentSpellResearch;
	}

	public void setCurrentSpellResearch(InventSpell newProject) {
		currentSpellResearch = newProject;
	}

	public boolean isCopyingBook() {
		return currentCopyProject != null;
	}
	public void setCurrentCopyProject(CopyBook copyBook) {
		currentCopyProject = copyBook;
	}
	public CopyBook getCurrentCopyProject() {
		return currentCopyProject;
	}

	public void addTwilightScar(boolean beneficial) {
		if (beneficial)
			twilightScars[0]++;
		else
			twilightScars[1]++;
	}
	public int getTwilightScars(boolean beneficial) {
		if (beneficial)
			return twilightScars[0];
		else
			return twilightScars[1];
	}


	public int getApparentAge() {
		if (getAge() < 35) return getAge();
		return apparentAge;
	}
	public void setApparentAge(int age) {
		apparentAge = age;
	}
	@Override
	public int getAge() {
		return super.getAge() / 52;
	}
	@Override
	public void setAge(int newAge) {
		int yearsDifference = newAge - getAge();
		super.setAge(super.getAge() + yearsDifference * 52);
	}
	@Override
	public void addAge(int change) {
		super.addAge(change * 52);
	}
	@Override
	public int getMaxAge() {
		return 1000;
	}

	public void setLongevityRitualEffect(int modifier) {
		longevityRitual = modifier;
	}
	public int getLongevityRitualEffect() {
		return longevityRitual;
	}
	public void setKnownLongevityEffect(int modifier) {
		knownLongevityRitual = modifier;
	}
	public int getKnownLongevityEffect() {
		return knownLongevityRitual;
	}

	public void addTimeInTwilight(int seasons) {
		seasonsInTwilight += seasons;
	}
	public int getYearsInTwilight() {
		return seasonsInTwilight / 4;
	}

	public void addSpell(Spell newSpell) {
		if (newSpell != null)
			spells.add(newSpell);
	}

	public int getTotalSpellLevels() {
		int total = 0;
		for (Spell s : spells) {
			total += s.getLevel();
		}
		return total;
	}

	public List<Spell> getSpells() {
		return HopshackleUtilities.cloneList(spells);
	}

	public int getTotalXPInArts() {
		int totalXP = 0;
		for (Arts art : Arts.values()) {
			totalXP += getTotalXPIn(art);
		}
		return totalXP;
	}

	public Covenant getCovenant() {return covenant;}
	public void setCovenant(Covenant newCovenant) {
		if (covenant == newCovenant) return;
		setSeasonsServiceOwed(0);
		if (covenant != null) {
			covenant.memberLeaves(this);
			log("No longer a member of " + covenant);
			setPolicy(new MagusApprenticeInheritance());
		}
		covenant = newCovenant;
		if (covenant != null) {
			covenant.newMember(this);
			log("Joins " + covenant);
			setLocation(covenant);
			if (hasApprentice())
				apprentice.setLocation(covenant);
		}
		if (covenant != null && Math.random() > 0.5) {
			setPolicy(new MagusCovenantInheritance());
			log("Sets covenant as sole heir");
		}
	}

	public void setSeasonsServiceOwed(int debt) {
		seasonsServiceOwed = debt;
	}
	public int getSeasonsServiceOwed() {
		return seasonsServiceOwed;
	}
	public void doSeasonsService() {
		seasonsServiceOwed--;
		log("Completes a season of service ("+ seasonsServiceOwed +" left)");
	}

	public void setInTwilight(boolean inTwilight) {
		currentlyInTwilight = inTwilight;
	}
	public boolean isInTwilight() {
		return currentlyInTwilight;
	}

	public Tribunal getTribunal() {
		return tribunal;
	}

	public void setTribunal(Tribunal tribunal) {
		if (getCovenant() == null && tribunal != null)  {
			setLocation(tribunal);
		}
		this.tribunal = tribunal;
	}

	public Book getBestDisintegratingBookToCopy() {
		ValuationFunction<Book> vFunction = new ValuationFunction<Book>() {

			@Override
			public double getValue(Book item) {
				if (item.isInUse() || item instanceof LabText)
					return 0.0;
				if (item.getDeterioration() < 0.6)
					return 0.0;
				for (Book b : getAllAccessibleBooksNotInUse()) {
					if (b.getDeterioration() < 0.3 && b.getTitleId() == item.getTitleId())
						return 0.0;		// i.e. we already have another good copy
				}
				return  (item.getBPValue() * item.getDeterioration() * 2) + item.getPopularity() * 2;
			}
			@Override
			public String toString(Book item) {
				return item.toString();
			}
		};

		List<Book> inOrder = getBestBookToCopyUsingValuationFunction(vFunction);
		if (inOrder == null || inOrder.isEmpty())
			return null;
		return inOrder.get(0);
	}

	public List<Book> getBestSpellsToCopy() {
		ValuationFunction<Book> vFunction = new ValuationFunction<Book>() {

			@Override
			public double getValue(Book temp) {
				if (!(temp instanceof LabText)) 
					return 0.0;
				LabText item = (LabText) temp;
				if (item.isInUse())
					return 0.0;
				if (item.getDeterioration() < 0.6)
					return 0.0;
				for (LabText lt : LabText.extractAllLabTextsFrom(getAllAccessibleBooksNotInUse())) {
					if (lt.getDeterioration() < 0.3 && lt.getSpell().equals(item.getSpell()));
					return 0.0;		// i.e. we already have another good copy
				}
				return  (item.getBPValue() * item.getDeterioration() * 2) + item.getPopularity() * 2;
			}

			@Override
			public String toString(Book temp) {
				return temp.toString();
			}
		};

		return getBestBookToCopyUsingValuationFunction(vFunction);
	}

	public Book getBestBookToCopy() {
		ValuationFunction<Book> vFunction = new ValuationFunction<Book>() {

			@Override
			public double getValue(Book item) {
				if (item.isInUse() || item instanceof LabText)
					return 0.0;
				return  item.getBPValue() + item.getPopularity() * 2;
			}
			@Override
			public String toString(Book temp) {
				return temp.toString();
			}
		};

		List<Book> inOrder = getBestBookToCopyUsingValuationFunction(vFunction);
		if (inOrder == null || inOrder.isEmpty())
			return null;
		return inOrder.get(0);
	}

	public List<Book> getBestBookToCopyUsingValuationFunction(final ValuationFunction<Book> valuer) {
		if (getLevelOf(Abilities.LATIN) < 4) return null;
		if (getLevelOf(Abilities.ARTES_LIBERALES) < 1) return null;
		List<Book> library = getAllAccessibleBooksNotInUse();
		List<Book> personalBooks = new ArrayList<Book>();
		if (isApprentice())
			personalBooks = getParens().getInventoryOf(AMU.sampleBook);
		else
			personalBooks = getInventoryOf(AMU.sampleBook);


		List<Book> toRemove = new ArrayList<Book>();
		for (Book potential : library) {
			if (potential.getPopularity() < 10)
				toRemove.add(potential);
			if (personalBooks.contains(potential))
				toRemove.add(potential);
			boolean notUseful = false;
			for (Book own : personalBooks) {
				if (own.getSubject() == potential.getSubject())
					if (own.getLevel() >= potential.getLevel())
						notUseful = true;;
			}
			if (notUseful)
				toRemove.add(potential);
			double score = valuer.getValue(potential);
			if (score < 20) 
				toRemove.add(potential);
		}

		library.removeAll(toRemove);
		Collections.sort(library, new Comparator<Book>() {

			@Override
			public int compare(Book o1, Book o2) {
				return (int) (100.0 * (valuer.getValue(o2) - valuer.getValue(o1)));
			}
		});

		return library;
	}

	public boolean hasReadTractatus(int titleId) {
		return tractatusRead.contains(titleId);
	}
	public void setTractatusAsRead(int titleId) {
		tractatusRead.add(titleId);
	}
	public void writesTractatusIn(Learnable skill) {
		int totalWritten = 0;
		if (tractatusWritten.containsKey(skill))
			totalWritten = tractatusWritten.get(skill);
		totalWritten++;
		tractatusWritten.put(skill, totalWritten);
	}
	public Set<Learnable> getPossibleTractactusSubjects() {
		Set<Learnable> retValue = new HashSet<Learnable>();
		for (Learnable skill : skills.keySet()) {
			int levelsPerTractatus = 5;
			if (skill instanceof Abilities) 
				levelsPerTractatus = 2;
			if (skill == Abilities.LATIN || skill == Abilities.WARPING || skill == Abilities.DECREPITUDE || skill == Abilities.VIS_HUNT)
				continue;
			if (getLevelOf(skill) < levelsPerTractatus)
				continue;	// not high enough level to write even one
			int writtenSoFar = 0;
			if (tractatusWritten.containsKey(skill))
				writtenSoFar = tractatusWritten.get(skill);
			if (getLevelOf(skill) <= writtenSoFar * levelsPerTractatus)
				continue;	// already written Quota
			retValue.add(skill);
		}
		return retValue;
	}

	public int getNumberOfTractatusWritten(Learnable skill) {
		if (tractatusWritten.containsKey(skill))
			return tractatusWritten.get(skill);
		return 0;
	}

	protected MagusPreferences getResearchGoals() {
		return researchGoals;
	}

	public void setLongevityAvailability(boolean b) {
		longevityContractOnOffer = !b;
	}

	public void setHermeticHouse(HermeticHouse newHouse) {
		house = newHouse;
	}

	public HermeticHouse getHermeticHouse() {
		return house;
	}

	public Familiar getFamiliar() {
		List<Familiar> temp = getInventoryOf(AMU.sampleFamiliar);
		if (temp.isEmpty()) 
			return null;
		else 
			return temp.get(0);
	}

	public boolean hasFamiliar() {
		return getFamiliar() != null;
	}
	public Relationship getRelationshipWith(Magus m) {
		if (relationships.containsKey(m))
			return relationships.get(m);
		return Relationship.NONE;
	}
	public void setRelationship(Magus m, Relationship r) {
		if (r == Relationship.NONE) {
			log ("Ceases to be " + getRelationshipWith(m) + " of " + m.toString());	
			relationships.remove(m);
		} else {
			log("Becomes " + r.name() + " of " + m.toString());
			relationships.put(m, r);
		}
	}
	public Map<Magus, Relationship> getRelationships() {
		return relationships;
	}
}


