package constants;

/**
 *
 * @author Itzik
 */
public class ServerConfig {
	
	// Database Connection
	public static String port;
	public static String database;
	public static String user;
	public static String pass;
	
	public static String serverName;
	public static String eventMessage;
	public static String scrollingMessage;
	public static String interface_;
	public static byte maxCharacters;
	public static byte channelCount;
	public static short userLimit;
    
    public static final int STARTER_MAP = 10000; // Maple Island
    public static final int HOME_MAP_ID =  923050005; // xenon lab (mob version), map used for @home
    
    public static final String events = "" + "AutomatedEvent," + "EvolutionLab,PinkZakumEntrance,PVP,CygnusBattle,ScarTarBattle,BossBalrog_EASY,BossBalrog_NORMAL,HorntailBattle,Nibergen,PinkBeanBattle,ZakumBattle,NamelessMagicMonster,Dunas,Dunas2,2095_tokyo,ZakumPQ,LudiPQ,KerningPQ,ProtectTylus,WitchTower_EASY,WitchTower_Med,WitchTower_Hard,Vergamot,ChaosHorntail,ChaosZakum,CoreBlaze,BossQuestEASY,BossQuestMed,BossQuestHARD,BossQuestHELL,BossQuestCHAOS,Ravana_EASY,Ravana_HARD,Ravana_MED,GuildQuest,Aufhaven,Dragonica,Rex,MonsterPark,KentaPQ,ArkariumBattle,AswanOffSeason,HillaBattle,The Dragon Shout,VonLeonBattle,Ghost,OrbisPQ,Romeo,Juliet,Pirate,Amoria,Ellin,CWKPQ,DollHouse,Kenta,Prison,Azwan,HenesysPQ,jett2ndjob,cpq,cpq2,Rex,Trains,Boats,Flight,Visitor,AirPlane,Ghost,PinkBeanBattle,Aswan,AswanOffSeason,Subway";
    // Scripts TODO: Amoria,CWKPQ,BossBalrog_EASY,BossBalrog_NORMAL,ZakumPQ,ProtectTylus,GuildQuest,Ravana_EASY,Ravna_MED,Ravana_HARD (untested or not working)
    
    /*Rates*/
    public static boolean fixedRates = true; //use same rates for all worlds
    public static final int EXP = 8; //exp rate (only works when fixedRates = true)
    public static final int MESO = 5; //meso rate (only works when fixedRates = true)
    public static final int DROP = 2; //drop rate (only works when fixedRates = true)
    public static final int CASH_DROP_RATE = 20; //out of 100
    
    /*Red Events*/
    public static boolean RED_EVENT_10 = false; //Makes cassandra popup when you login at lvl<10 (maple island)
    public static boolean RED_EVENT = false; //Makes red even notification popup (cassandra) When you login at level 11+

    public static enum Events {

        EVENT1("PinkZakumEntrance"),
        EVENT2("PVP"),
        EVENT3("CygnusBattle"),
        EVENT4("ScarTarBattle"),
        EVENT5("BossBalrog_EASY"),
        EVENT6("BossBalrog_NORMAL"),
        EVENT7("HorntailBattle"),
        EVENT8("Nibergen"),
        EVENT9("PinkBeanBattle"),
        EVENT10("ZakumBattle"),
        EVENT11("NamelessMagicMonster"),
        EVENT12("Dunas"),
        EVENT13("Dunas2"),
        EVENT14("2095_tokyo"),
        EVENT15("ZakumPQ"),
        EVENT16("LudiPQ"),
        EVENT17("KerningPQ"),
        EVENT18("ProtectTylus"),
        EVENT19("WitchTower_EASY"),
        EVENT20("WitchTower_Med"),
        EVENT21("WitchTower_Hard"),
        EVENT22("Vergamot"),
        EVENT23("ChaosHorntail"),
        EVENT24("ChaosZakum"),
        EVENT25("CoreBlaze"),
        EVENT26("BossQuestEASY"),
        EVENT27("BossQuestMed"),
        EVENT28("BossQuestHARD"),
        EVENT29("BossQuestHELL"),
        EVENT30("Ravana_EASY"),
        EVENT31("Ravana_HARD"),
        EVENT32("Ravana_MED"),
        EVENT33("GuildQuest"),
        EVENT34("Aufhaven"),
        EVENT35("Dragonica"),
        EVENT36("Rex"),
        EVENT37("MonsterPark"),
        EVENT38("KentaPQ"),
        EVENT39("ArkariumBattle"),
        EVENT40("AswanOffSeason"),
        EVENT41("HillaBattle"),
        EVENT42("The Dragon Shout"),
        EVENT43("VonLeonBattle"),
        EVENT44("Ghost"),
        EVENT45("OrbisPQ"),
        EVENT46("Romeo"),
        EVENT47("Juliet"),
        EVENT48("Pirate"),
        EVENT49("Amoria"),
        EVENT50("Ellin"),
        EVENT51("CWKPQ"),
        EVENT52("DollHouse"),
        EVENT53("Kenta"),
        EVENT54("Prison"),
        EVENT55("Azwan"),
        EVENT56("cpq"),
        EVENT57("cpq2"),
        EVENT58("Rex"),
        EVENT59("Trains"),
        EVENT60("Boats"),
        EVENT61("Flight"),
        EVENT62("Visitor"),
        EVENT63("AirPlane"),
        EVENT64("Ghost"),
        EVENT65("PinkBeanBattle"),
        EVENT66("Aswan"),
        EVENT67("AswanOffSeason"),
        EVENT68("Subway");
        private final String name;

        Events(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static String[] getEvents() {
        String[] eventlist = new String[Events.values().length];
        int arrayLocation = 0;
        for (Events event : Events.values()) {
            eventlist[arrayLocation] += event.getName();
            arrayLocation++;
        }
        return eventlist;
    }

    public static String getEventList() {
        String eventlist = new String();
        for (Events event : Events.values()) {
            eventlist += event.getName();
            eventlist += ", ";
        }
        eventlist += "@";
        eventlist = eventlist.replaceAll(", @", "");
        return eventlist;
    }
}
