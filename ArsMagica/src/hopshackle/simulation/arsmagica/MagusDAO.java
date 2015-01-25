package hopshackle.simulation.arsmagica;

import java.util.List;

import hopshackle.simulation.*;

public class MagusDAO implements AgentDAO<Magus> {

	@Override
	public String getTableCreationSQL(String tableSuffix) {
		return  "CREATE TABLE IF NOT EXISTS Magi_" + tableSuffix +
		" ( id 		INT			NOT NULL,"		+
		" name 		VARCHAR(70)		NOT NULL, "	+
		" parensName	VARCHAR(50)		NOT NULL, " +
		" tribunal		VARCHAR(50)		NOT NULL, " +
		" age		INT			NOT NULL,"		+
		" apparentAge		INT			NOT NULL,"		+
		" currentYear		INT			NOT NULL,"		+
		" birth		INT		NOT NULL,"		+
		" parens	INT			NOT NULL,"		+
		" isApprentice	INT		NOT NULL,"	+
		" apprentices	INT			NOT NULL,"		+
		" apprenticeIds VARCHAR(100) NOT NULL,"	+
		" Intl		INT			NOT NULL,"	+
		" Per		INT			NOT NULL,"	+
		" Pre		INT			NOT NULL,"	+
		" Com		INT			NOT NULL,"	+
		" Str		INT			NOT NULL,"	+
		" Sta		INT			NOT NULL,"	+
		" Dex		INT			NOT NULL,"	+
		" Qik		INT			NOT NULL,"	+
		" Cr		INT			NOT NULL,"	+
		" Intellego		INT			NOT NULL,"	+
		" Mu		INT			NOT NULL,"	+
		" Pe		INT			NOT NULL,"	+
		" Re		INT			NOT NULL,"	+
		" An		INT			NOT NULL,"	+
		" Aq		INT			NOT NULL,"	+
		" Au		INT			NOT NULL,"	+
		" Co		INT			NOT NULL,"	+
		" He		INT			NOT NULL,"	+
		" Ig		INT			NOT NULL,"	+
		" Im		INT			NOT NULL,"	+
		" Me		INT			NOT NULL,"	+
		" Te		INT			NOT NULL,"	+
		" Vi		INT			NOT NULL,"	+
		" TwilightScarsPos		INT			NOT NULL,"	+
		" TwilightScarsNeg		INT			NOT NULL,"	+
		" MagicTheory		INT			NOT NULL,"	+
		" Scribe		INT			NOT NULL,"	+
		" ParmaMagica		INT			NOT NULL,"	+
		" Warping			INT			NOT NULL,"	+
		" Decrepitude		INT			NOT NULL,"	+
		" Teaching		INT			NOT NULL,"	+
		" Concentration		INT			NOT NULL,"	+
		" Latin		INT			NOT NULL,"	+
		" ArtesLiberales		INT			NOT NULL,"	+
		" MagicLore		INT			NOT NULL,"	+
		" FaerieLore		INT			NOT NULL,"	+
		" Philosophiae		INT			NOT NULL,"	+
		" TotalVis		INT			NOT NULL,"	+
		" AnnualVis		INT			NOT NULL,"	+
		" Aura		INT			NOT NULL,"	+
		" LongevityRitual		INT			NOT NULL,"	+
		" yearsInTwilight		INT			NOT NULL,"	+
		" covenant				INT			NOT NULL," +
		" house					VARCHAR(15)	NOT NULL" +
		");";
	}

	@Override
	public String getTableUpdateSQL(String tableSuffix) {
		return "INSERT INTO Magi_" + tableSuffix + 
				" (id,  name, parensName, tribunal, age, apparentAge, currentYear, birth, parens, isApprentice, apprentices, apprenticeIds, Intl, Per, Pre, Com, Str, Sta, Dex, Qik, " +
				"Cr, Intellego, Mu, Pe, Re, An, Aq, Au, Co, He, Ig, Im, Me, Te, Vi, TwilightScarsPos, TwilightScarsNeg, MagicTheory, Scribe, ParmaMagica, Warping, Decrepitude, " +
				"Teaching, Concentration, Latin, ArtesLiberales, MagicLore, FaerieLore, Philosophiae, TotalVis, AnnualVis, Aura, LongevityRitual, yearsInTwilight, " +
				"covenant, house) VALUES";
	}

	@Override
	public String getValuesForAgent(Magus agent) {
		World w = agent.getWorld();
		List<Agent> apprentices = agent.getChildren();
		StringBuffer apprenticeIds = Agent.convertToStringVersionOfIDs(apprentices);
		
		int totalVisSupply = 0;
		List<VisSource> sources = agent.getInventoryOf(new VisSource(Arts.CREO, 1, null));
		for (VisSource source : sources) {
			totalVisSupply += source.getAmountPerAnnum();
		}

		return String.format(" (%d, '%s', '%s', '%s', %d, %d, %d,%d, %d, %d, %d, '%s', %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, " +
								"%d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, '%s')",
				agent.getUniqueID(),
				agent.toString(),
				agent.getParens()!= null ? agent.getParensName() : "None",
				agent.getTribunal()!=null ? agent.getTribunal().toString() : "None",
				agent.getAge(),
				agent.getApparentAge(),
				w.getYear(),
				agent.getBirth() / 52,
				agent.getParens()!=null ? agent.getParens().getUniqueID() : 0,
				agent.isApprentice() ? 1 : 0,
				apprentices.size(),
				apprenticeIds.toString(),
				agent.getIntelligence(),
				agent.getPerception(),
				agent.getPresence(),
				agent.getCommunication(),
				agent.getStrength(),
				agent.getStamina(),
				agent.getDexterity(),
				agent.getQuickness(),
				agent.getLevelOf(Arts.CREO),
				agent.getLevelOf(Arts.INTELLEGO),
				agent.getLevelOf(Arts.MUTO),
				agent.getLevelOf(Arts.PERDO),
				agent.getLevelOf(Arts.REGO),
				agent.getLevelOf(Arts.ANIMAL),
				agent.getLevelOf(Arts.AQUAM),
				agent.getLevelOf(Arts.AURAM),
				agent.getLevelOf(Arts.CORPUS),
				agent.getLevelOf(Arts.HERBAM),
				agent.getLevelOf(Arts.IGNEM),
				agent.getLevelOf(Arts.IMAGINEM),
				agent.getLevelOf(Arts.MENTEM),
				agent.getLevelOf(Arts.TERRAM),
				agent.getLevelOf(Arts.VIM),
				agent.getTwilightScars(true),
				agent.getTwilightScars(false),
				agent.getLevelOf(Abilities.MAGIC_THEORY),
				agent.getLevelOf(Abilities.SCRIBE),
				agent.getLevelOf(Abilities.PARMA_MAGICA),
				agent.getLevelOf(Abilities.WARPING),
				agent.getLevelOf(Abilities.DECREPITUDE),
				agent.getLevelOf(Abilities.TEACHING),
				agent.getLevelOf(Abilities.CONCENTRATION),
				agent.getLevelOf(Abilities.LATIN),
				agent.getLevelOf(Abilities.ARTES_LIBERALES),
				agent.getLevelOf(Abilities.MAGIC_LORE),
				agent.getLevelOf(Abilities.FAERIE_LORE),
				agent.getLevelOf(Abilities.PHILOSOPHIAE),
				agent.getNumberInInventoryOf(new Vis(Arts.VIM)),
				totalVisSupply,
				agent.getMagicAura(),
				agent.getLongevityRitualEffect(),
				agent.getYearsInTwilight(), 
				agent.getCovenant() != null ? agent.getCovenant().getUniqueID() : 0,
				agent.getHermeticHouse() != null ? agent.getHermeticHouse().toString() : ""
				);
	}

	@Override
	public String getTableDeletionSQL(String tableSuffix) {
		return "DROP TABLE IF EXISTS Magi_" + tableSuffix + ";";
	}

}
