package constants;

public class ServerConstants {
	
	// Protocol Version
    public static final short MAPLE_VERSION = (short) 175;
    public static final String MAPLE_PATCH = "1";
    
    // Database Connection
 	public static String PORT;
 	public static String DATABASE;
 	public static String USER;
 	public static String PASS;

 	public static String SERVERNAME;
 	public static String EVENTMESSAGE;
 	public static String SCROLLINGMESSAGE;
 	public static String HOST;
 	public static byte MAXCHARACTERS;
 	public static byte CHANNELCOUNT;
 	public static short USERLIMIT;

 	// Rates
 	public static final int EXP = 8;
 	public static final int MESO = 2;
 	public static final int DROP = 2;
 	
 	public static final int CASH_DROP_RATE = 20; // out of 100

    public static enum PlayerGMRank {

        NORMAL('@', 0),
        INTERN('!', 1),
        GM('!', 2),
        SUPERGM('!', 3),
        ADMIN('!', 4);
        private final char commandPrefix;
        private final int level;

        PlayerGMRank(char ch, int level) {
            commandPrefix = ch;
            this.level = level;
        }

        public String getCommandPrefix() {
            return String.valueOf(commandPrefix);
        }

        public int getLevel() {
            return level;
        }
    }

    public static enum CommandType {

        NORMAL(0),
        TRADE(1);
        private final int level;

        CommandType(int level) {
            this.level = level;
        }

        public int getType() {
            return level;
        }
    }
}
