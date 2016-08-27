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

	/* Rates */
	public static final int EXP = 9; // exp rate (only works when fixedRates true)
	public static final int MESO = 5; // meso rate (only works when fixedRates = true)
	public static final int DROP = 3; // drop rate (only works when fixedRates =  true)
	
	public static final int CASH_DROP_RATE = 20; // out of 100
	/* Red Events */
	public static boolean RED_EVENT_10 = false; // Makes cassandra popup when you login at lvl<10 (maple island)
	public static boolean RED_EVENT = false; // Makes red even notification popup (cassandra) When you login at level 11+
}
