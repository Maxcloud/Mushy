package server;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

import client.SkillFactory;
import client.inventory.MapleInventoryIdentifier;
import constants.GameConstants;
import constants.ServerConfig;
import handling.cashshop.CashShopServer;
import handling.channel.ChannelServer;
import handling.login.LoginInformationProvider;
import handling.login.LoginServer;
import handling.world.World;
import net.DatabaseConnection;
import server.Timer.BuffTimer;
import server.Timer.CloneTimer;
import server.Timer.EtcTimer;
import server.Timer.EventTimer;
import server.Timer.MapTimer;
import server.Timer.PingTimer;
import server.Timer.WorldTimer;
import server.cash.CashItemFactory;
import server.life.MapleLifeFactory;
import server.life.MapleMonsterInformationProvider;
import server.life.MobSkillFactory;
import server.life.PlayerNPC;
import server.maps.MapleMapFactory;
import server.quest.MapleQuest;

public class Start extends Properties {

	private static final long serialVersionUID = 5172629591649728634L;
	
	public static final Start instance = new Start();
    public static long startTime = System.currentTimeMillis();
    
    public void run() throws InterruptedException, IOException {
        long start = System.currentTimeMillis();

        Properties p = new Properties();
        try {
            p.load(new FileInputStream("config.ini"));
        } catch (IOException ex) {
            System.out.println("Failed to load config.ini");
            System.exit(0);
        }
        
        ServerConfig.interface_ = p.getProperty("ip");
        ServerConfig.serverName = p.getProperty("name");
        ServerConfig.eventMessage = p.getProperty("event");
        ServerConfig.scrollingMessage = p.getProperty("message");
        ServerConfig.maxCharacters = getByte(p, "characters");
        ServerConfig.userLimit = getShort(p, "users");
        ServerConfig.channelCount = getByte(p, "channels");
        
        ServerConfig.port = p.getProperty("sql_port");
        ServerConfig.user = p.getProperty("sql_user");
        ServerConfig.pass = p.getProperty("sql_password");
        ServerConfig.database = p.getProperty("sql_db");
        
        System.setProperty("wzpath", p.getProperty("wzpath"));

        try {
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE accounts SET loggedin = 0")) {
                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Runtime Exception - Could not connect to MySql Server.");
        }

        World.init();
        
        System.out.print("Init...\r\n");
        
        // Timers..
        WorldTimer.getInstance().start();
        EtcTimer.getInstance().start();
        MapTimer.getInstance().start();
        CloneTimer.getInstance().start();
        EventTimer.getInstance().start();
        BuffTimer.getInstance().start();
        PingTimer.getInstance().start();
        
        GameConstants.LoadEXP();
        
        // MapleDojoRanking.getInstance().load();
        // MapleGuildRanking.getInstance().load();
        // MapleGuild.loadAll();
        // MapleFamily.loadAll();
        
        MapleLifeFactory.loadQuestCounts();
        
        MapleQuest.initQuests();
        MapleItemInformationProvider.getInstance().runEtc();
        MapleMonsterInformationProvider.getInstance().load();
        
        MapleItemInformationProvider.getInstance().runItems();
        SkillFactory.load();
        LoginInformationProvider.getInstance();
        // RandomRewards.load();
        
        // MapleOxQuizFactory.getInstance();
        // MapleCarnivalFactory.getInstance();
        // CharacterCardFactory.getInstance().initialize();
        MobSkillFactory.getInstance();
        
        // SpeedRunner.loadSpeedRuns();
        MapleInventoryIdentifier.getInstance();
        MapleMapFactory.loadCustomLife();
        
        CashItemFactory.getInstance().initialize();
        LoginServer.run_startup_configurations();
        ChannelServer.startChannel_Main();
        CashShopServer.run_startup_configurations();
        Runtime.getRuntime().addShutdownHook(new Thread(new Shutdown()));
        World.registerRespawn();
        // ShutdownServer.registerMBean();
        PlayerNPC.loadAll();
        MapleMonsterInformationProvider.getInstance().addExtra();
        LoginServer.setOn();
        //System.out.println("Event Script List: " + ServerConfig.getEventList());
        long now = System.currentTimeMillis() - start;
        long seconds = now / 1000;
        long ms = now % 1000;
        System.out.println("Total loading time: " + seconds + "s " + ms + "ms");
    }

    public static class Shutdown implements Runnable {

        @Override
        public void run() {
            ShutdownServer.getInstance().run();
            ShutdownServer.getInstance().run();
        }
    }

    public static void main(final String args[]) throws InterruptedException, IOException {
        instance.run();
    }
    
    /**
     * Retrieves an byte value.
     * @param key The key name.
     * @return The requested value (<code>null</code> if not found).
     */
    public Byte getByte(Properties p, String key)
    {
       Byte value = null;
       String string = p.getProperty(key);
       if (string != null) {
          value = new Byte(string);
       } else {
    	   System.out.println("The byte was null.");
       }
       return value;
    }
    
    /**
     * Retrieves an short value.
     * @param key The key name.
     * @return The requested value (<code>null</code> if not found).
     */
    public Short getShort(Properties p, String key)
    {
       Short value = null;
       String string = p.getProperty(key);
       if (string != null)
          value = new Short(string);
       return value;
    }
}
