package tools.packet;

import java.util.List;
import java.util.Map;
import java.util.Set;

import client.MapleCharacter;
import client.MapleClient;
import client.PartTimeJob;
import constants.GameConstants;
import constants.JobConstants;
import constants.JobConstants.LoginJob;
import constants.ServerConstants;
import constants.WorldConstants.WorldOption;
import handling.SendPacketOpcode;
import handling.login.LoginServer;
import tools.DateUtil;
import tools.Randomizer;
import tools.Triple;
import tools.data.MaplePacketLittleEndianWriter;

public class LoginPacket {

    public static byte[] getHello(short mapleVersion, byte[] sendIv, byte[] recvIv) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(15);
        mplew.writeShort(mapleVersion);
        mplew.writeMapleAsciiString(ServerConstants.MAPLE_PATCH);
        mplew.write(recvIv);
        mplew.write(sendIv);
        mplew.write(8);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static final byte[] getPing() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(2);
        mplew.writeShort(SendPacketOpcode.PING.getValue());
        return mplew.getPacket();
    }
    
    public static final byte[] getStart() {
    	MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(2);
    	mplew.writeShort(0x24);
    	mplew.write(1);
    	return mplew.getPacket();
    }
    
    public static byte[] useAuthSever() {
    	MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    	mplew.writeShort(SendPacketOpcode.AUTHSERVER.getValue());
    	mplew.write(0); // disable the auth server check
    	return mplew.getPacket();
    }

    public static byte[] getAuthSuccessRequest(MapleClient client) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.LOGIN_STATUS.getValue());
        mplew.write(0);
        mplew.write(0);
        mplew.writeInt(0);
        mplew.writeMapleAsciiString(client.getAccountName()); // 174.1
        mplew.writeInt(client.getAccID());
        mplew.write(0);
        mplew.write(0);
        mplew.writeInt(2);
        mplew.writeInt(0);
        mplew.write(0);
        // mplew.write0(7); //gm stuff and new int
        mplew.writeMapleAsciiString(client.getAccountName());
        mplew.write(3);
        mplew.write(0);
        mplew.writeLong(0);
        mplew.writeLong(0);

        mplew.writeInt(0x1A); // The amount of characters available in total - 3?

        getAvailableJobs(mplew);
        
        mplew.write(0); // 174.1
        mplew.writeInt(-1); // 174.1
        mplew.write(1);
        mplew.write(1);

        // The date the account was created.
        mplew.writeLong(0);

        return mplew.getPacket();
    }
    
    public static final byte[] sendAllowedCreation() {
    	MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    	mplew.writeShort(0x2D);
    	getAvailableJobs(mplew);
    	return mplew.getPacket();
    }
    
    private static void getAvailableJobs(MaplePacketLittleEndianWriter mplew) {
    	mplew.write(JobConstants.enableJobs ? 1 : 0); //toggle
        mplew.write(JobConstants.jobOrder); // Job Order (orders are located in lib)
        for (LoginJob j : LoginJob.values()) {
            mplew.write(j.getFlag());
            mplew.writeShort(j.getFlag());
        }
    }
    
    /**
     * Send a packet to confirm authentication was successful.
     * @param c
     * @return
     */
    public static final byte[] getSecondAuthSuccess(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.LOGIN_SECOND.getValue());
        mplew.write(0); // request
        mplew.writeInt(c.getAccID());
        mplew.write(0);
        mplew.write(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.write(0);
        mplew.writeMapleAsciiString(c.getAccountName());
        mplew.write(0);
        mplew.write(0);
        mplew.writeLong(0);
        mplew.writeMapleAsciiString(c.getAccountName());
        mplew.writeLong(Randomizer.nextLong());
        mplew.writeInt(28);
        mplew.writeLong(Randomizer.nextLong());
        /*for(byte b = 0; b < 3; b++) {
        	if(b == 1)
        		mplew.writeInt(28);
        	mplew.writeLong(Randomizer.nextLong());
        }*/
        mplew.writeMapleAsciiString("");
        getAvailableJobs(mplew); 
        mplew.write(0);
        mplew.writeInt(-1);
        return mplew.getPacket();
    }

    public static final byte[] getLoginFailed(int reason) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(16);

        mplew.writeShort(SendPacketOpcode.LOGIN_STATUS.getValue());
        mplew.write(reason);
        mplew.write(0);
        mplew.writeInt(0);

        return mplew.getPacket();
    }
    /*
     * location: UI.wz/Login.img/Notice/text
     * reasons:
     * useful:
     * 32 - server under maintenance check site for updates
     * 35 - your computer is running thirdy part programs close them and play again
     * 36 - due to high population char creation has been disabled
     * 43 - revision needed your ip is temporary blocked
     * 75-78 are cool for auto register
     
     */

    public static byte[] getTempBan(long timestampTill, byte reason) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(17);

        mplew.writeShort(SendPacketOpcode.LOGIN_STATUS.getValue());
        mplew.write(2);
        mplew.write(0);
        mplew.writeInt(0);
        mplew.write(reason);
        mplew.writeLong(timestampTill);

        return mplew.getPacket();
    }

    public static final byte[] deleteCharResponse(int cid, int state) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.DELETE_CHAR_RESPONSE.getValue());
        mplew.writeInt(cid);
        mplew.write(state);

        return mplew.getPacket();
    }

    public static byte[] secondPwError(byte mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);

        mplew.writeShort(SendPacketOpcode.SECONDPW_ERROR.getValue());
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] sendAuthResponse(int response) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.AUTH_RESPONSE.getValue());
        mplew.writeInt(response);
        return mplew.getPacket();
    }

    public static byte[] enableRecommended(int world) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.ENABLE_RECOMMENDED.getValue());
        mplew.writeInt(world);
        return mplew.getPacket();
    }

    public static byte[] sendRecommended(int world, String message) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SEND_RECOMMENDED.getValue());
        mplew.write(message != null ? 1 : 0);
        if (message != null) {
            mplew.writeInt(world);
            mplew.writeMapleAsciiString(message);
        }
        return mplew.getPacket();
    }

    public static byte[] getServerList(int serverId, Map<Integer, Integer> channelLoad) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SERVERLIST.getValue());
        mplew.write(serverId);
        String worldName = LoginServer.getTrueServerName();
        mplew.writeMapleAsciiString(worldName);
        mplew.write(WorldOption.getById(serverId).getFlag());
        mplew.writeMapleAsciiString(LoginServer.getEventMessage());
        mplew.writeShort(100);
        mplew.writeShort(100);
        mplew.write(0);
        int lastChannel = 1;
        Set<Integer> channels = channelLoad.keySet();
        for (int i = 30; i > 0; i--) {
            if (channels.contains(Integer.valueOf(i))) {
                lastChannel = i;
                break;
            }
        }
        mplew.write(lastChannel);

        for (int i = 1; i <= lastChannel; i++) {
            int load;

            if (channels.contains(i)) {
                load = channelLoad.get(i);
            } else {
                load = 1200;
            }
            mplew.writeMapleAsciiString(worldName + "-" + i);
            mplew.writeInt(load);
            mplew.write(serverId);
            mplew.writeShort(i - 1);
        }
        mplew.writeShort(0);
        mplew.writeInt(0);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] getEndOfServerList() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SERVERLIST.getValue());
        mplew.write(0xFF);
        mplew.write(0); // boolean disable cash shop and trade msg
        mplew.write(0); // 174.1
        mplew.write(0); // 174.1

        return mplew.getPacket();
    }

    public static byte[] getServerStatus(int status) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SERVERSTATUS.getValue());
        mplew.write(status);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] changeBackground(List<Triple<String, Integer, Boolean>> backgrounds) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CHANGE_BACKGROUND.getValue());
        mplew.write(0); // backgrounds.size() (number of bgs)
        for (Triple<String, Integer, Boolean> background : backgrounds) {
            mplew.writeMapleAsciiString(background.getLeft());
            mplew.write(background.getRight() ? Randomizer.nextInt(2) : background.getMid());
            mplew.write(0); // 174.1
            mplew.write(0); // 174.1
        }
        /* Map.wz/Obj/login.img/WorldSelect/background/background number
         Backgrounds ids sometime have more than one background anumation */
        /* Background are like layers, backgrounds in the packets are
         removed, so the background which was hiden by the last one
         is shown.
         */

        return mplew.getPacket();
    }

    public static byte[] getCharList(String secondpw, List<MapleCharacter> chars, int charslots) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CHARLIST.getValue());
        mplew.write(0); // nDay
        mplew.writeMapleAsciiString("normal"); // 174.1
        mplew.writeInt(0); // 174.1 ref count?
       
        mplew.write(1); // burning event block?
        
        // character locations
        mplew.writeInt(0); // ?
        
        // timestamp?
        mplew.writeLong(0); // 174.1
        
        // has the list of characters been edited
        mplew.write(0);
        
        mplew.writeInt(0); // ? (nSecond)
        
        mplew.write(chars.size());
        for (MapleCharacter chr : chars) {
            addCharEntry(mplew, chr, (!chr.isGM()) && (chr.getLevel() >= 30), false);
        }
        mplew.write((secondpw != null) && (secondpw.length() <= 0) ? 2 : (secondpw != null) && (secondpw.length() > 0) ? 1 : 0); 
        mplew.write(0);
        mplew.writeInt(charslots);
        
        mplew.writeInt(0); // buy character count?
        mplew.writeInt(-1); // event new char job
        
        /*
        mplew.writeInt(6111);
        
        // Something to do with time.
        mplew.writeInt(0); // hidword
        mplew.writeInt(0); // lodword
        
        mplew.write(0); // rename count?
        
        mplew.write(0); // ?
        */
        
        mplew.writeReversedLong(DateUtil.getTime(System.currentTimeMillis()));
        mplew.write(0); // the amount of allowed name changes
        mplew.write0(5);
        return mplew.getPacket();
    }
    
    public static byte[] getAccountName(String name) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(0x10F);
        mplew.writeLong(0);
        mplew.writeMapleAsciiString(name);
        return mplew.getPacket();
    }

    public static byte[] addNewCharEntry(MapleCharacter chr, boolean worked) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ADD_NEW_CHAR_ENTRY.getValue());
        mplew.write(worked ? 0 : 1);
        addCharEntry(mplew, chr, false, false);

        return mplew.getPacket();
    }

    public static byte[] charNameResponse(String charname, boolean nameUsed) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CHAR_NAME_RESPONSE.getValue());
        mplew.writeMapleAsciiString(charname);
        mplew.write(nameUsed ? 1 : 0);

        return mplew.getPacket();
    }

    private static void addCharEntry(MaplePacketLittleEndianWriter mplew, MapleCharacter chr, boolean ranking, boolean viewAll) {
        PacketHelper.addCharStats(mplew, chr);
        PacketHelper.addCharLook(mplew, chr, true, false);
        if (GameConstants.isZero(chr.getJob())) {
            PacketHelper.addCharLook(mplew, chr, true, true);
        }
        if (!viewAll) {
            mplew.write(0);
        }
        
        mplew.write(ranking ? 1 : 0);
        if (ranking) {
            mplew.writeInt(chr.getRank());
            mplew.writeInt(chr.getRankMove());
            mplew.writeInt(chr.getJobRank());
            mplew.writeInt(chr.getJobRankMove());
        }
    }

    public static byte[] enableSpecialCreation(int accid, boolean enable) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SPECIAL_CREATION.getValue());
        mplew.writeInt(accid);
        mplew.write(enable ? 0 : 1);
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] partTimeJob(int cid, short type, long time) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PART_TIME.getValue());
        mplew.writeInt(cid);
        mplew.write(0);
        mplew.write(type);
        //1) 0A D2 CD 01 70 59 9F EA
        //2) 0B D2 CD 01 B0 6B 9C 18
        mplew.writeReversedLong(DateUtil.getTime(time));
        mplew.writeInt(0);
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] updatePartTimeJob(PartTimeJob partTime) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(21);
        mplew.writeShort(SendPacketOpcode.PART_TIME.getValue());
        mplew.writeInt(partTime.getCharacterId());
        mplew.write(0);
        PacketHelper.addPartTimeJob(mplew, partTime);
        return mplew.getPacket();
    }
}
