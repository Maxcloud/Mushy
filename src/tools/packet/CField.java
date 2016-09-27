package tools.packet;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleKeyLayout;
import client.MonsterFamiliar;
import client.Skill;
import client.SkillMacro;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleAndroid;
import client.inventory.MapleInventoryType;
import client.inventory.MapleRing;
import constants.GameConstants;
import constants.QuickMove.QuickMoveNPC;
import handling.SendPacketOpcode;
import handling.channel.handler.PlayerInteractionHandler;
import handling.world.World;
import handling.world.guild.MapleGuild;
import handling.world.guild.MapleGuildAlliance;
import script.npc.NPCTalk;
import server.MaplePackageActions;
import server.MapleTrade;
import server.events.MapleSnowball;
import server.life.MapleNPC;
import server.maps.MapleDragon;
import server.maps.MapleHaku;
import server.maps.MapleMap;
import server.maps.MapleMapItem;
import server.maps.MapleMist;
import server.maps.MapleNodes;
import server.maps.MapleReactor;
import server.maps.MapleSummon;
import server.maps.MechDoor;
import server.movement.LifeMovementFragment;
import server.quest.MapleQuest;
import server.quest.MapleQuestStatus;
import server.shops.MapleShop;
import tools.AttackPair;
import tools.HexTool;
import tools.Pair;
import tools.Randomizer;
import tools.Triple;
import tools.data.MaplePacketLittleEndianWriter;

public class CField {

	private static int DEFAULT_BUFFMASK = 0;
	
	public static byte[] getPacketFromHexString(String hex) {
		return HexTool.getByteArrayFromHexString(hex);
	}

	/**
	 * 
	 * 68 - Suspicious Activity
	 * 78 - That ID is already logged in.
	 * @param c
	 * @param port
	 * @param characterid
	 * @return
	 */
	public static byte[] getServerIP(MapleClient c, int port, int characterid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.SERVER_IP.getValue());
		mplew.write(0); // request
		mplew.write(0); 
		
		byte[] svr = new byte[] {8, 31, 99, ((byte) 141)};
		byte[] chat = new byte[] {8, 31, 99, ((byte) 133)};
		
		// maple server ip
		mplew.write(svr);
		mplew.writeShort(port);
		
		// chat server ip 
		mplew.write(new byte[4]);
		mplew.writeShort(0);
		
		mplew.writeInt(characterid);
		
		mplew.write(0);
		
		// argument ?
		mplew.writeInt(0);
		mplew.write(0);
		
		 // shutdown ? (timestamp)
		mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
		
		// mplew.write(HexTool.getByteArrayFromHexString("3F 01 00 00 00 C8 00 00"));
		// mplew.write(0);
		
		return mplew.getPacket();
	}

	public static byte[] getChannelChange(MapleClient c, int port) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		byte[] svr = new byte[] {8, 31, 99, ((byte) 141)};
		
		mplew.writeShort(SendPacketOpcode.CHANGE_CHANNEL.getValue());
		mplew.write(1);
		mplew.write(svr);
		mplew.writeShort(port);
		mplew.writeInt(0);
		return mplew.getPacket();
	}

	public static byte[] getPVPType(int type, List<Pair<Integer, String>> players1, int team, boolean enabled,
			int lvl) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.ENTER_PVP.getValue());
		mplew.write(type);
		mplew.write(lvl);
		mplew.write(enabled ? 1 : 0);
		mplew.write(0);
		if (type > 0) {
			mplew.write(team);
			mplew.writeInt(players1.size());
			for (Pair pl : players1) {
				mplew.writeInt(((Integer) pl.left).intValue());
				mplew.writeMapleAsciiString((String) pl.right);
				mplew.writeShort(2660);
			}
		}

		return mplew.getPacket();
	}

	public static byte[] getPVPTransform(int type) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.CHANGE_TEAM.getValue());
		mplew.write(type);

		return mplew.getPacket();
	}

	public static byte[] getPVPDetails(List<Pair<Integer, Integer>> players) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.CHANGE_MODE.getValue());
		mplew.write(1);
		mplew.write(0);
		mplew.writeInt(players.size());
		for (Pair pl : players) {
			mplew.writeInt(((Integer) pl.left).intValue());
			mplew.write(((Integer) pl.right).intValue());
		}

		return mplew.getPacket();
	}

	public static byte[] enablePVP(boolean enabled) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.CHANGE_STATE.getValue());
		mplew.write(enabled ? 1 : 2);

		return mplew.getPacket();
	}

	public static byte[] getPVPScore(int score, boolean kill) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.UPDATE_COUNT.getValue());
		mplew.writeInt(score);
		mplew.write(kill ? 1 : 0);

		return mplew.getPacket();
	}

	public static byte[] getPVPResult(List<Pair<Integer, MapleCharacter>> flags, int exp, int winningTeam,
			int playerTeam) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SHOW_MODE_RESULT.getValue());
		mplew.writeInt(flags.size());
		for (Pair f : flags) {
			mplew.writeInt(((MapleCharacter) f.right).getId());
			mplew.writeMapleAsciiString(((MapleCharacter) f.right).getName());
			mplew.writeInt(((Integer) f.left).intValue());
			mplew.writeShort(((MapleCharacter) f.right).getTeam() + 1); // byte, byte
			mplew.writeInt(0);
			mplew.writeInt(0);
		}
		mplew.write0(24);
		mplew.writeInt(exp);
		mplew.write(0);
		mplew.writeShort(100);
		mplew.writeInt(0);
		mplew.writeInt(0);
		mplew.write(winningTeam);
		mplew.write(playerTeam);

		return mplew.getPacket();
	}

	public static byte[] getPVPTeam(List<Pair<Integer, String>> players) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.UPDATE_TEAM_INFO.getValue());
		mplew.writeInt(players.size());
		for (Pair pl : players) {
			mplew.writeInt(((Integer) pl.left).intValue());
			mplew.writeMapleAsciiString((String) pl.right);
			mplew.writeShort(2660); // byte, byte
		}

		return mplew.getPacket();
	}

	public static byte[] getPVPScoreboard(List<Pair<Integer, MapleCharacter>> flags, int type) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.UPDATE_RANK_INFO.getValue());
		mplew.writeShort(flags.size());
		for (Pair f : flags) {
			mplew.writeInt(((MapleCharacter) f.right).getId());
			mplew.writeMapleAsciiString(((MapleCharacter) f.right).getName());
			mplew.writeInt(((Integer) f.left).intValue());
			mplew.write(type == 0 ? 0 : ((MapleCharacter) f.right).getTeam() + 1);
		}

		return mplew.getPacket();
	}

	public static byte[] getPVPPoints(int p1, int p2) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.UPDATE_TEAM_SCORE.getValue());
		mplew.writeInt(p1);
		mplew.writeInt(p2);

		return mplew.getPacket();
	}

	public static byte[] getPVPKilled(String lastWords) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.REVIVE_MESSAGE.getValue());
		mplew.writeMapleAsciiString(lastWords);

		return mplew.getPacket();
	}

	public static byte[] getPVPMode(int mode) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SCREEN_EFFECT.getValue());
		mplew.write(mode);

		return mplew.getPacket();
	}

	public static byte[] getPVPIceHPBar(int hp, int maxHp) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.ICEKNIGHT_HP_CHANGE.getValue());
		mplew.writeInt(hp);
		mplew.writeInt(maxHp);

		return mplew.getPacket();
	}

	public static byte[] getCaptureFlags(MapleMap map) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.CAPTURE_FLAGS.getValue());
		mplew.writeRect(map.getArea(0));
		mplew.writeInt(((Point) ((Pair) map.getGuardians().get(0)).left).x);
		mplew.writeInt(((Point) ((Pair) map.getGuardians().get(0)).left).y);
		mplew.writeRect(map.getArea(1));
		mplew.writeInt(((Point) ((Pair) map.getGuardians().get(1)).left).x);
		mplew.writeInt(((Point) ((Pair) map.getGuardians().get(1)).left).y);

		return mplew.getPacket();
	}

	public static byte[] getCapturePosition(MapleMap map) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		Point p1 = map.getPointOfItem(2910000);
		Point p2 = map.getPointOfItem(2910001);
		mplew.writeShort(SendPacketOpcode.CAPTURE_POSITION.getValue());
		mplew.write(p1 == null ? 0 : 1);
		if (p1 != null) {
			mplew.writeInt(p1.x);
			mplew.writeInt(p1.y);
		}
		mplew.write(p2 == null ? 0 : 1);
		if (p2 != null) {
			mplew.writeInt(p2.x);
			mplew.writeInt(p2.y);
		}

		return mplew.getPacket();
	}

	public static byte[] resetCapture() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.CAPTURE_RESET.getValue());

		return mplew.getPacket();
	}

	public static byte[] getMacros(SkillMacro[] macros) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SKILL_MACRO.getValue());
		int count = 0;
		for (int i = 0; i < 5; i++) {
			if (macros[i] != null) {
				count++;
			}
		}
		mplew.write(count);
		for (int i = 0; i < 5; i++) {
			SkillMacro macro = macros[i];
			if (macro != null) {
				mplew.writeMapleAsciiString(macro.getName());
				mplew.write(macro.getShout());
				mplew.writeInt(macro.getSkill1());
				mplew.writeInt(macro.getSkill2());
				mplew.writeInt(macro.getSkill3());
			}
		}

		return mplew.getPacket();
	}

	public static byte[] gameMsg(String msg) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.GAME_MSG.getValue());
		mplew.writeAsciiString(msg);
		mplew.write(1);

		return mplew.getPacket();
	}

	public static byte[] innerPotentialMsg(String msg) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.INNER_ABILITY_MSG.getValue());
		mplew.writeMapleAsciiString(msg);

		return mplew.getPacket();
	}

	public static byte[] updateInnerPotential(byte ability, int skill, int level, int rank) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.ENABLE_INNER_ABILITY.getValue());
		mplew.write(1); // unlock
		mplew.write(1); // 0 = no update
		mplew.writeShort(ability); // 1-3
		mplew.writeInt(skill); // skill id (7000000+)
		mplew.writeShort(level); // level, 0 = blank inner ability
		mplew.writeShort(rank); // rank
		mplew.write(1); // 0 = no update

		return mplew.getPacket();
	}

	public static byte[] innerPotentialResetMessage() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.INNER_ABILITY_RESET_MSG.getValue());
		mplew.writeMapleAsciiString("Ability reconfigured.");
		mplew.write(1);
		return mplew.getPacket();
	}

	public static byte[] updateHonour(int honourLevel, int honourExp, boolean levelup) {
		/*
		 * data: 03 00 00 00 69 00 00 00 01
		 */
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.UPDATE_HONOUR.getValue());

		mplew.writeInt(honourLevel);
		mplew.writeInt(honourExp);
		mplew.write(levelup ? 1 : 0); // shows level up effect

		return mplew.getPacket();
	}
	
	public static byte[] getCharInfo(MapleCharacter mc) {
		return getWarpToMap(mc, null, 0, false);
	}
	
	public static byte[] getWarpToMap(MapleCharacter mc, MapleMap to, int spawnPoint, boolean bCharacterData) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.WARP_TO_MAP.getValue());
		
		// size (int + int)
		mplew.writeShort(0);
		
		mplew.writeInt(mc.getClient().getChannel() - 1);
		
		// bDev
		mplew.write(0);
		
		// wOldDriverID
		mplew.writeInt(0);
		
		// Are you logging into the handling? (1), or changing the map? (2)
		// bPopupDlg
		mplew.write(bCharacterData ? 1 : 2);
		
		// ?
		mplew.writeInt(0);
		
		// nFieldWidth
		mplew.writeInt(800);
		
		// nFieldHeight
		mplew.writeInt(600);
		
		// Are you logging into the handling? (1), or changing the map? (0)
		mplew.write(bCharacterData);
		
		// size (string (size->string))
		mplew.writeShort(0);
		
		if(bCharacterData) {
			mc.CRand().connectData(mplew);
			
			PacketHelper.addCharacterInfo(mplew, mc);
			mplew.writeInt(0); // log out event
		} else {
			
			// bUsingBuffProtector (this will call the revive function, upon death.)
			mplew.write(0);
			
			mplew.writeInt(to.getId());
			mplew.write(spawnPoint);
			mplew.writeInt(mc.getStat().getHp());
			
			// (bool (int + int))
			mplew.write(0); 
		}
		
		// set white fade in-and-out
		mplew.write(0);
		
		// set overlapping screen animation
		mplew.write(0);
		
		// some sort of korean event fame-up
		mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
		
		// ?
		mplew.writeInt(0x64);
		
		// party map experience.
		// bool (int + string(bgm) + int(fieldid))
		mplew.write(0);
		
		// bool
		mplew.write(0);
		
		// ?
		mplew.write(1);
		
		// bool (int)
		mplew.write(0);
		
		// bool ((int + byte(size))->(int, int, int))->(long, int, int)
		boolean starplanet = false;
		mplew.write(0);
		if (starplanet) {
			
			// nRoundID
			mplew.writeInt(0); 
			
			// the size, cannot exceed the count of 10
			mplew.write(0); 
			
				// anPoint
				mplew.writeInt(0);
				
				// anRanking
				mplew.writeInt(0);
				
				// atLastCheckRank (timeGetTime - 300000)
				mplew.writeInt(0);
				
			// ftShiningStarExpiredTime
			mplew.writeLong(0);
			
			//nShiningStarPickedCount
			mplew.writeInt(0);
			
			//nRoundStarPoint
			mplew.writeInt(0);
		}			
		
		// bool (int + byte + long)
		boolean aStarPlanetRoundInfo = false;
		mplew.write(aStarPlanetRoundInfo);
		if(aStarPlanetRoundInfo) {
			
			// nStarPlanetRoundID
			mplew.writeInt(0);
			
			// nStarPlanetRoundState
			mplew.write(0);
			
			// ftStarPlanetRoundEndDate
			mplew.writeLong(0);
		}
		
		// int(size)->(int, string)
		mplew.writeInt(0);
		
		// FreezeHotEventInfo
		
		// nAccountType
		mplew.write(0);
		
		// dwAccountID
		mplew.writeInt(0);
		
		// EventBestFriendInfo
		
		// dwEventBestFriendAID
		mplew.writeInt(0);
		
		mplew.writeInt(0);
		
		return mplew.getPacket();
	}

	public static byte[] serverBlocked(int type) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SERVER_BLOCKED.getValue());
		mplew.write(type);

		return mplew.getPacket();
	}

	public static byte[] pvpBlocked(int type) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.write(type);

		return mplew.getPacket();
	}

	public static byte[] showEquipEffect() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SHOW_EQUIP_EFFECT.getValue());

		return mplew.getPacket();
	}

	public static byte[] showEquipEffect(int team) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SHOW_EQUIP_EFFECT.getValue());
		mplew.writeShort(team);

		return mplew.getPacket();
	}

	public static byte[] multiChat(String name, String chattext, int mode) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.MULTICHAT.getValue());
		mplew.write(mode);
		mplew.writeMapleAsciiString(name);
		mplew.writeMapleAsciiString(chattext);

		return mplew.getPacket();
	}

	public static byte[] getFindReplyWithCS(String target, boolean buddy) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
		mplew.write(buddy ? 72 : 9);
		mplew.writeMapleAsciiString(target);
		mplew.write(2);
		mplew.writeInt(-1);

		return mplew.getPacket();
	}

	public static byte[] getWhisper(String sender, int channel, String text) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
		mplew.write(18);
		mplew.writeMapleAsciiString(sender);
		mplew.writeShort(channel - 1);
		mplew.writeMapleAsciiString(text);

		return mplew.getPacket();
	}

	public static byte[] getWhisperReply(String target, byte reply) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
		mplew.write(10);
		mplew.writeMapleAsciiString(target);
		mplew.write(reply);

		return mplew.getPacket();
	}

	public static byte[] getFindReplyWithMap(String target, int mapid, boolean buddy) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
		mplew.write(buddy ? 72 : 9);
		mplew.writeMapleAsciiString(target);
		mplew.write(3);// was1
		mplew.writeInt(mapid);// mapid);
		// mplew.writeZeroBytes(8);

		return mplew.getPacket();
	}

	public static byte[] getFindReply(String target, int channel, boolean buddy) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
		mplew.write(buddy ? 72 : 9);
		mplew.writeMapleAsciiString(target);
		mplew.write(3);
		mplew.writeInt(channel - 1);

		return mplew.getPacket();
	}
	
	public static byte[] trembleEffect(int type, int delay) {
		return environmentChange(null, 1, type, delay);
	}
	
	public static byte[] environmentChange(String text, int mode) {
		return environmentChange(text, mode, 0, 0);
	}
	
	public static byte[] showMapEffect(String effect) {
		return environmentChange(effect, 4, 0, 0);
	}
	
	public static byte[] playSound(String sound) {
		return environmentChange(sound, 5, 0, 0);
	}
	
	public static byte[] musicChange(String song) {
		return environmentChange(song, 7, 0, 0);
	}
	
	public static byte[] showEnterEffect(String text) {
		return environmentChange(text, 12, 0, 0);
	}
	
	public static byte[] environmentChange(String text, int mode, int type, int delay) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.BOSS_ENV.getValue());
		mplew.write(mode);
		switch(mode) {
			case 1: // tremble effect
				mplew.write(type);
				mplew.writeInt(delay);
				mplew.writeShort(30);
				break;
			case 2:
				mplew.writeMapleAsciiString(text);
				break;
			case 4:
				mplew.writeMapleAsciiString(text);
				break;
			case 5:
				mplew.writeMapleAsciiString(text);
				mplew.writeInt(0);
				break;
			case 12:
				mplew.writeMapleAsciiString(text);
				mplew.writeInt(0); 
				break;
			default: throw new UnsupportedOperationException("That mode has not been implemented.");
		}
		
		return mplew.getPacket();
	}

	public static byte[] environmentMove(String env, int mode) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.MOVE_ENV.getValue());
		mplew.writeMapleAsciiString(env);
		mplew.writeInt(mode);

		return mplew.getPacket();
	}

	public static byte[] getUpdateEnvironment(MapleMap map) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.UPDATE_ENV.getValue());
		mplew.writeInt(map.getEnvironment().size());
		for (Map.Entry mp : map.getEnvironment().entrySet()) {
			mplew.writeMapleAsciiString((String) mp.getKey());
			mplew.writeInt(((Integer) mp.getValue()).intValue());
		}

		return mplew.getPacket();
	}

	public static byte[] startMapEffect(String msg, int itemid, boolean active) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.MAP_EFFECT.getValue());
		mplew.write(active ? 0 : 1);

		mplew.writeInt(itemid);
		if (active) {
			mplew.writeMapleAsciiString(msg);
		}
		return mplew.getPacket();
	}

	public static byte[] removeMapEffect() {
		return startMapEffect(null, 0, false);
	}

	public static byte[] getGMEffect(int value, int mode) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.GM_EFFECT.getValue());
		mplew.write(value);
		mplew.write0(17);

		return mplew.getPacket();
	}

	public static byte[] showOXQuiz(int questionSet, int questionId, boolean askQuestion) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.OX_QUIZ.getValue());
		mplew.write(askQuestion ? 1 : 0);
		mplew.write(questionSet);
		mplew.writeShort(questionId);

		return mplew.getPacket();
	}

	public static byte[] showEventInstructions() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.GMEVENT_INSTRUCTIONS.getValue());
		mplew.write(0);

		return mplew.getPacket();
	}

	public static byte[] getPVPClock(int type, int time) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.CLOCK.getValue());
		mplew.write(3);
		mplew.write(type);
		mplew.writeInt(time);

		return mplew.getPacket();
	}

	public static byte[] getBanBanClock(int time, int direction) {
		MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
		packet.writeShort(SendPacketOpcode.CLOCK.getValue());
		packet.write(5);
		packet.write(direction); // 0:?????? 1:????
		packet.writeInt(time);
		return packet.getPacket();
	}

	public static byte[] getClock(int time) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.CLOCK.getValue());
		mplew.write(2);
		mplew.writeInt(time);

		return mplew.getPacket();
	}

	public static byte[] getClockTime(int hour, int min, int sec) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.CLOCK.getValue());
		mplew.write(1);
		mplew.write(hour);
		mplew.write(min);
		mplew.write(sec);

		return mplew.getPacket();
	}

	public static byte[] boatPacket(int effect, int mode) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.BOAT_MOVE.getValue());
		mplew.write(effect);
		mplew.write(mode);

		return mplew.getPacket();
	}

	public static byte[] setBoatState(int effect) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.BOAT_STATE.getValue());
		mplew.write(effect);
		mplew.write(1);

		return mplew.getPacket();
	}

	public static byte[] stopClock() {
		return getPacketFromHexString(Integer.toHexString(SendPacketOpcode.STOP_CLOCK.getValue()) + " 00");
	}

	public static byte[] showAriantScoreBoard() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.ARIANT_SCOREBOARD.getValue());

		return mplew.getPacket();
	}

	public static byte[] sendPyramidUpdate(int amount) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.PYRAMID_UPDATE.getValue());
		mplew.writeInt(amount);

		return mplew.getPacket();
	}

	public static byte[] sendPyramidResult(byte rank, int amount) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.PYRAMID_RESULT.getValue());
		mplew.write(rank);
		mplew.writeInt(amount);

		return mplew.getPacket();
	}

	public static byte[] quickSlot(String skil) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.QUICK_SLOT.getValue());
		mplew.write(skil == null ? 0 : 1);
		if (skil != null) {
			String[] slots = skil.split(",");
			for (int i = 0; i < 8; i++) {
				mplew.writeInt(Integer.parseInt(slots[i]));
			}
		}

		return mplew.getPacket();
	}

	public static byte[] getMovingPlatforms(MapleMap map) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.MOVE_PLATFORM.getValue());
		mplew.writeInt(map.getPlatforms().size());
		for (MapleNodes.MaplePlatform mp : map.getPlatforms()) {
			mplew.writeMapleAsciiString(mp.name);
			mplew.writeInt(mp.start);
			mplew.writeInt(mp.SN.size());
			for (int x = 0; x < mp.SN.size(); x++) {
				mplew.writeInt((mp.SN.get(x)).intValue());
			}
			mplew.writeInt(mp.speed);
			mplew.writeInt(mp.x1);
			mplew.writeInt(mp.x2);
			mplew.writeInt(mp.y1);
			mplew.writeInt(mp.y2);
			mplew.writeInt(mp.x1);
			mplew.writeInt(mp.y1);
			mplew.writeShort(mp.r);
		}

		return mplew.getPacket();
	}

	public static byte[] sendPyramidKills(int amount) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.PYRAMID_KILL_COUNT.getValue());
		mplew.writeInt(amount);

		return mplew.getPacket();
	}

	public static byte[] sendPVPMaps() {
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.PVP_INFO.getValue());
		mplew.write(3); // max amount of players
		for (int i = 0; i < 20; i++) {
			mplew.writeInt(10); // how many peoples in each map
		}
		mplew.write0(124);
		mplew.writeShort(150); //// PVP 1.5 EVENT!
		mplew.write(0);

		return mplew.getPacket();
	}

	public static byte[] gainForce(int oid, int count, int color) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.GAIN_FORCE.getValue());
		mplew.write(1); // 0 = remote user?
		mplew.writeInt(oid);
		byte newcheck = 0;
		mplew.writeInt(newcheck); // unk
		if (newcheck > 0) {
			mplew.writeInt(0); // unk
			mplew.writeInt(0); // unk
		}
		mplew.write(0);
		mplew.writeInt(4); // size, for each below
		mplew.writeInt(count); // count
		mplew.writeInt(color); // color, 1-10 for demon, 1-2 for phantom
		mplew.writeInt(0); // unk
		mplew.writeInt(0); // unk
		return mplew.getPacket();
	}

	public static byte[] getAndroidTalkStyle(int npc, String talk, int... args) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
		mplew.write(4);
		mplew.writeInt(npc);
		mplew.writeShort(10);
		mplew.writeMapleAsciiString(talk);
		mplew.write(args.length);

		for (int i = 0; i < args.length; i++) {
			mplew.writeInt(args[i]);
		}
		return mplew.getPacket();
	}

	public static byte[] achievementRatio(int amount) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.ACHIEVEMENT_RATIO.getValue());
		mplew.writeInt(amount);

		return mplew.getPacket();
	}

	public static byte[] getQuickMoveInfo(boolean show, List<QuickMoveNPC> qm) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.QUICK_MOVE.getValue());
		mplew.write(qm.size() <= 0 ? 0 : show ? qm.size() : 0);
		if (show && qm.size() > 0) {
			for (QuickMoveNPC qmn : qm) {
				mplew.writeInt(0);
				mplew.writeInt(qmn.getId());
				mplew.writeInt(qmn.getType());
				mplew.writeInt(qmn.getLevel());
				mplew.writeMapleAsciiString(qmn.getDescription());
				mplew.writeLong(PacketHelper.getTime(-2));
				mplew.writeLong(PacketHelper.getTime(-1));
			}
		}

		return mplew.getPacket();
	}

	public static byte[] spawnPlayerMapobject(MapleCharacter chr) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SPAWN_PLAYER.getValue());
		mplew.writeInt(chr.getId());
		mplew.write(chr.getLevel());
		mplew.writeMapleAsciiString(chr.getName());
		MapleQuestStatus ultExplorer = chr.getQuestNoAdd(MapleQuest.getInstance(111111));
		if ((ultExplorer != null) && (ultExplorer.getCustomData() != null)) {
			mplew.writeMapleAsciiString(ultExplorer.getCustomData());
		} else {
			mplew.writeMapleAsciiString("");
		}
		if (chr.getGuildId() <= 0) {
			mplew.write0(8);
		} else {
			MapleGuild gs = World.Guild.getGuild(chr.getGuildId());
			if (gs != null) {
				mplew.writeMapleAsciiString(gs.getName());
				mplew.writeShort(gs.getLogoBG());
				mplew.write(gs.getLogoBGColor());
				mplew.writeShort(gs.getLogo());
				mplew.write(gs.getLogoColor());
			} else {
				mplew.write0(8);
			}
		}
		mplew.write(0);

		final List<Pair<Integer, Integer>> buffvalue = new ArrayList<>();
		final List<Pair<Integer, Integer>> buffvaluenew = new ArrayList<>();
		int[] mask = new int[GameConstants.MAX_BUFFSTAT];
		mask[0] |= -33554432; // -0x2000000
		mask[1] |= 0x2000;
		mask[1] |= 0x1000;
		mask[1] |= 0x200;
		mask[5] |= 0x20000;
		mask[5] |= 0x8000;
		if ((chr.getBuffedValue(MapleBuffStat.DarkSight) != null) || (chr.isHidden())) {
		//	mask[MapleBuffStat.DarkSight.getPosition(true)] |= MapleBuffStat.DarkSight.getValue();
		}
		if (chr.getBuffedValue(MapleBuffStat.SoulArrow) != null) {
		//	mask[MapleBuffStat.SoulArrow.getPosition(true)] |= MapleBuffStat.SoulArrow.getValue();
		}
		if (chr.getBuffedValue(MapleBuffStat.DamAbsorbShield) != null) {
			// mask[MapleBuffStat.DamAbsorbShield.getPosition(true)] |= MapleBuffStat.DamAbsorbShield.getValue();
			buffvaluenew.add(new Pair(Integer.valueOf(1000), Integer.valueOf(2)));
			buffvaluenew.add(new Pair(Integer.valueOf(chr.getTrueBuffSource(MapleBuffStat.DamAbsorbShield)),
					Integer.valueOf(4)));
			buffvaluenew.add(new Pair(Integer.valueOf(9), Integer.valueOf(0)));
		}
		if (chr.getBuffedValue(MapleBuffStat.StopForceAtomInfo) != null) {
			// mask[MapleBuffStat.StopForceAtomInfo.getPosition(true)] |= MapleBuffStat.StopForceAtomInfo.getValue();
			buffvaluenew.add(new Pair(
					Integer.valueOf(chr.getTotalSkillLevel(chr.getTrueBuffSource(MapleBuffStat.StopForceAtomInfo))),
					Integer.valueOf(2)));
			buffvaluenew.add(
					new Pair(Integer.valueOf(chr.getTrueBuffSource(MapleBuffStat.StopForceAtomInfo)), Integer.valueOf(4)));
			buffvaluenew.add(new Pair(Integer.valueOf(5), Integer.valueOf(0)));
			buffvaluenew.add(
					new Pair(Integer.valueOf(chr.getTrueBuffSource(MapleBuffStat.StopForceAtomInfo) == 61101002 ? 1 : 2),
							Integer.valueOf(4)));
			buffvaluenew.add(
					new Pair(Integer.valueOf(chr.getTrueBuffSource(MapleBuffStat.StopForceAtomInfo) == 61101002 ? 3 : 5),
							Integer.valueOf(4)));
			buffvaluenew.add(new Pair(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.StopForceAtomInfo).intValue()),
					Integer.valueOf(4)));
			buffvaluenew.add(
					new Pair(Integer.valueOf(chr.getTrueBuffSource(MapleBuffStat.StopForceAtomInfo) == 61101002 ? 3 : 5),
							Integer.valueOf(4)));
			if (chr.getTrueBuffSource(MapleBuffStat.StopForceAtomInfo) != 61101002) {
				buffvaluenew.add(new Pair(Integer.valueOf(8), Integer.valueOf(0)));
			}
		}
		if ((chr.getBuffedValue(MapleBuffStat.ComboCounter) != null)
				&& (chr.getBuffedValue(MapleBuffStat.StopForceAtomInfo) == null)) {
			// mask[MapleBuffStat.ComboCounter.getPosition(true)] |= MapleBuffStat.ComboCounter.getValue();
			buffvalue.add(
					new Pair(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.ComboCounter).intValue()), Integer.valueOf(1)));
		}
		if (chr.getBuffedValue(MapleBuffStat.WeaponCharge) != null) {
			// mask[MapleBuffStat.WeaponCharge.getPosition(true)] |= MapleBuffStat.WeaponCharge.getValue();
			buffvalue.add(new Pair(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.WeaponCharge).intValue()),
					Integer.valueOf(2)));
			buffvalue.add(new Pair(Integer.valueOf(chr.getBuffSource(MapleBuffStat.WeaponCharge)), Integer.valueOf(3)));
		}
		if ((chr.getBuffedValue(MapleBuffStat.ShadowPartner) != null)
				&& (chr.getBuffedValue(MapleBuffStat.StopForceAtomInfo) == null)) {
			// mask[MapleBuffStat.ShadowPartner.getPosition(true)] |= MapleBuffStat.ShadowPartner.getValue();
			buffvalue.add(new Pair(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.ShadowPartner).intValue()),
					Integer.valueOf(2)));
			buffvalue
					.add(new Pair(Integer.valueOf(chr.getBuffSource(MapleBuffStat.ShadowPartner)), Integer.valueOf(3)));
		}
		// if ((chr.getBuffedValue(MapleBuffStat.Morph) != null) &&
		// (chr.getBuffedValue(MapleBuffStat.StopForceAtomInfo) == null)) {//TODO
		// mask[MapleBuffStat.Morph.getPosition(true)] |=
		// MapleBuffStat.Morph.getValue();
		// buffvalue.add(new
		// Pair(Integer.valueOf(chr.getStatForBuff(MapleBuffStat.Morph).getMorph(chr)),
		// Integer.valueOf(2)));
		// buffvalue.add(new
		// Pair(Integer.valueOf(chr.getBuffSource(MapleBuffStat.Morph)),
		// Integer.valueOf(3)));
		// }
		if (chr.getBuffedValue(MapleBuffStat.BERSERK_FURY) != null) {// works
			// mask[MapleBuffStat.BERSERK_FURY.getPosition(true)] |= MapleBuffStat.BERSERK_FURY.getValue();
		}
		if (chr.getBuffedValue(MapleBuffStat.DIVINE_BODY) != null) {
			// mask[MapleBuffStat.DIVINE_BODY.getPosition(true)] |= MapleBuffStat.DIVINE_BODY.getValue();
		}
		if (chr.getBuffedValue(MapleBuffStat.WIND_WALK) != null) {// TODO better
			// mask[MapleBuffStat.WIND_WALK.getPosition(true)] |= MapleBuffStat.WIND_WALK.getValue();
			buffvalue.add(new Pair(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.WIND_WALK).intValue()),
					Integer.valueOf(2)));
			buffvalue
					.add(new Pair(Integer.valueOf(chr.getTrueBuffSource(MapleBuffStat.WIND_WALK)), Integer.valueOf(3)));
		}
		if (chr.getBuffedValue(MapleBuffStat.PYRAMID_PQ) != null) {// TODO
			// mask[MapleBuffStat.PYRAMID_PQ.getPosition(true)] |= MapleBuffStat.PYRAMID_PQ.getValue();
			buffvalue.add(new Pair(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.PYRAMID_PQ).intValue()),
					Integer.valueOf(2)));
			buffvalue.add(
					new Pair(Integer.valueOf(chr.getTrueBuffSource(MapleBuffStat.PYRAMID_PQ)), Integer.valueOf(3)));
		}
		if (chr.getBuffedValue(MapleBuffStat.Flying) != null) {// TODO
			// mask[MapleBuffStat.Flying.getPosition(true)] |= MapleBuffStat.Flying.getValue();
			buffvalue.add(new Pair(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.Flying).intValue()),
					Integer.valueOf(1)));
		}
		// if (chr.getBuffedValue(MapleBuffStat.OWL_SPIRIT) != null) {//TODO
		// mask[MapleBuffStat.OWL_SPIRIT.getPosition(true)] |=
		// MapleBuffStat.OWL_SPIRIT.getValue();
		// buffvalue.add(new
		// Pair(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.OWL_SPIRIT).intValue()),
		// Integer.valueOf(2)));
		// buffvalue.add(new
		// Pair(Integer.valueOf(chr.getTrueBuffSource(MapleBuffStat.OWL_SPIRIT)),
		// Integer.valueOf(3)));
		// }
		if (chr.getBuffedValue(MapleBuffStat.FinalCut) != null) {
			// mask[MapleBuffStat.FinalCut.getPosition(true)] |= MapleBuffStat.FinalCut.getValue();
			buffvalue.add(new Pair(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.FinalCut).intValue()),
					Integer.valueOf(2)));
			buffvalue
					.add(new Pair(Integer.valueOf(chr.getTrueBuffSource(MapleBuffStat.FinalCut)), Integer.valueOf(3)));
		}

		if (chr.getBuffedValue(MapleBuffStat.TORNADO) != null) {
			// Smask[MapleBuffStat.TORNADO.getPosition(true)] |= MapleBuffStat.TORNADO.getValue();
			buffvalue.add(new Pair(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.TORNADO).intValue()),
					Integer.valueOf(2)));
			buffvalue.add(new Pair(Integer.valueOf(chr.getTrueBuffSource(MapleBuffStat.TORNADO)), Integer.valueOf(3)));
		}
		if (chr.getBuffedValue(MapleBuffStat.INFILTRATE) != null) {
			// mask[MapleBuffStat.INFILTRATE.getPosition(true)] |= MapleBuffStat.INFILTRATE.getValue();
		}
		if (chr.getBuffedValue(MapleBuffStat.Mechanic) != null) {
			// mask[MapleBuffStat.Mechanic.getPosition(true)] |= MapleBuffStat.Mechanic.getValue();
			buffvalue.add(new Pair(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.Mechanic).intValue()),
					Integer.valueOf(2)));
			buffvalue.add(
					new Pair(Integer.valueOf(chr.getTrueBuffSource(MapleBuffStat.Mechanic)), Integer.valueOf(3)));
		}
		if (chr.getBuffedValue(MapleBuffStat.BMageAura) != null) {
			// Smask[MapleBuffStat.BMageAura.getPosition(true)] |= MapleBuffStat.BMageAura.getValue();
			buffvalue.add(new Pair(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.BMageAura).intValue()),
					Integer.valueOf(2)));
			buffvalue
					.add(new Pair(Integer.valueOf(chr.getTrueBuffSource(MapleBuffStat.BMageAura)), Integer.valueOf(3)));
		}
		if (chr.getBuffedValue(MapleBuffStat.BMageAura) != null) {
			// mask[MapleBuffStat.BLUE_AURA.getPosition(true)] |= MapleBuffStat.BLUE_AURA.getValue();
			buffvalue.add(new Pair(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.BMageAura).intValue()),
					Integer.valueOf(2)));
			buffvalue
					.add(new Pair(Integer.valueOf(chr.getTrueBuffSource(MapleBuffStat.BMageAura)), Integer.valueOf(3)));
		}
		if (chr.getBuffedValue(MapleBuffStat.BMageAura) != null) {
			// mask[MapleBuffStat.YELLOW_AURA.getPosition(true)] |= MapleBuffStat.YELLOW_AURA.getValue();
			buffvalue.add(new Pair(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.BMageAura).intValue()),
					Integer.valueOf(2)));
			buffvalue.add(
					new Pair(Integer.valueOf(chr.getTrueBuffSource(MapleBuffStat.BMageAura)), Integer.valueOf(3)));
		}
		if ((chr.getBuffedValue(MapleBuffStat.DamAbsorbShield) != null)
				&& (chr.getBuffedValue(MapleBuffStat.StopForceAtomInfo) == null)) {
			// mask[MapleBuffStat.WATER_SHIELD.getPosition(true)] |= MapleBuffStat.WATER_SHIELD.getValue();
			buffvaluenew.add(
					new Pair(Integer.valueOf(chr.getTotalSkillLevel(chr.getTrueBuffSource(MapleBuffStat.DamAbsorbShield))),
							Integer.valueOf(2)));
			buffvaluenew.add(
					new Pair(Integer.valueOf(chr.getTrueBuffSource(MapleBuffStat.DamAbsorbShield)), Integer.valueOf(4)));
			buffvaluenew.add(new Pair(Integer.valueOf(9), Integer.valueOf(0)));
		}
		if (chr.getBuffedValue(MapleBuffStat.Inflation) != null) {
			// mask[MapleBuffStat.Inflation.getPosition(true)] |= MapleBuffStat.Inflation.getValue();
			buffvalue.add(new Pair(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.Inflation).intValue()),
					Integer.valueOf(2)));
			buffvalue.add(
					new Pair(Integer.valueOf(chr.getTrueBuffSource(MapleBuffStat.Inflation)), Integer.valueOf(3)));
		}

		for (int i = 0; i < mask.length; i++) {
			mplew.writeInt(mask[i]);
		}
		for (Pair i : buffvalue) {
			if (((Integer) i.right).intValue() == 3) {
				mplew.writeInt(((Integer) i.left).intValue());
			} else if (((Integer) i.right).intValue() == 2) {
				mplew.writeShort(((Integer) i.left).shortValue());
			} else if (((Integer) i.right).intValue() == 1) {
				mplew.write(((Integer) i.left).byteValue());
			}
		}
		mplew.writeInt(-1);
		if (buffvaluenew.isEmpty()) {
			mplew.write0(10);
		} else {
			mplew.write(0);
			for (Pair i : buffvaluenew) {
				if (((Integer) i.right).intValue() == 4) {
					mplew.writeInt(((Integer) i.left).intValue());
				} else if (((Integer) i.right).intValue() == 2) {
					mplew.writeShort(((Integer) i.left).shortValue());
				} else if (((Integer) i.right).intValue() == 1) {
					mplew.write(((Integer) i.left).byteValue());
				} else if (((Integer) i.right).intValue() == 0) {
					mplew.write0(((Integer) i.left).intValue());
				}
			}
		}
		mplew.write0(38); // v143 20->38 ty hawt

		int CHAR_MAGIC_SPAWN = Randomizer.nextInt();
		mplew.write(1);
		mplew.writeInt(CHAR_MAGIC_SPAWN);
		mplew.write0(8); // v143 10->8
		mplew.write(1);
		mplew.writeInt(CHAR_MAGIC_SPAWN);
		mplew.write0(10);
		mplew.write(1);
		mplew.writeInt(CHAR_MAGIC_SPAWN);
		mplew.writeShort(0);
		int buffSrc = chr.getBuffSource(MapleBuffStat.RideVehicle);
		if (buffSrc > 0) {
			Item c_mount = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -118);
			Item mount = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -18);
			if ((GameConstants.getMountItem(buffSrc, chr) == 0) && (c_mount != null)
					&& (chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -119) != null)) {
				mplew.writeInt(c_mount.getItemId());
			} else if ((GameConstants.getMountItem(buffSrc, chr) == 0) && (mount != null)
					&& (chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -19) != null)) {
				mplew.writeInt(mount.getItemId());
			} else {
				mplew.writeInt(GameConstants.getMountItem(buffSrc, chr));
			}
			mplew.writeInt(buffSrc);
		} else {
			mplew.writeLong(0L);
		}
		mplew.write(1);
		mplew.writeInt(CHAR_MAGIC_SPAWN);
		mplew.writeLong(0L);
		mplew.write(1);
		mplew.writeInt(CHAR_MAGIC_SPAWN);
		mplew.write0(15);
		mplew.write(1);
		mplew.writeInt(CHAR_MAGIC_SPAWN);
		mplew.write0(16);
		mplew.write(1);
		mplew.writeInt(CHAR_MAGIC_SPAWN);
		mplew.writeShort(0);

		mplew.writeShort(chr.getJob());
		mplew.writeShort(chr.getSubcategory());
		PacketHelper.addCharLook(mplew, chr, true, false);
		if (GameConstants.isZero(chr.getJob())) {
			PacketHelper.addCharLook(mplew, chr, true, false);
		}

		mplew.writeInt(0);
		mplew.writeInt(0);

		mplew.writeInt(Math.min(250, chr.getInventory(MapleInventoryType.CASH).countById(5110000))); // Valentine
																										// Effect
		mplew.writeInt(0);
		mplew.writeInt(0);

		mplew.writeInt(0);
		mplew.writeInt(0);
		mplew.writeInt(0);
		MapleQuestStatus stat = chr.getQuestNoAdd(MapleQuest.getInstance(124000));
		mplew.writeInt(stat != null && stat.getCustomData() != null ? Integer.parseInt(stat.getCustomData()) : 0); // title
		mplew.writeInt(0);
		mplew.writeInt(0);
		mplew.writeInt(0);// head title? chr.getHeadTitle()
		mplew.writeInt(chr.getItemEffect());
		// mplew.writeInt(chr.getDamageSkin()); // this aint working yet brah
		mplew.writeInt(GameConstants.getInventoryType(chr.getChair()) == MapleInventoryType.SETUP ? chr.getChair() : 0);
		mplew.writeInt(0);
		mplew.writeInt(0); // new v143
		mplew.writePos(chr.getTruePosition());
		mplew.write(chr.getStance());
		mplew.writeShort(chr.getFH());
		mplew.write(0);
		mplew.write(0);
		mplew.write(0);

		mplew.write(1);
		mplew.write(0);

		mplew.writeInt(chr.getMount().getLevel());
		mplew.writeInt(chr.getMount().getExp());
		mplew.writeInt(chr.getMount().getFatigue());

		PacketHelper.addAnnounceBox(mplew, chr);
		mplew.write((chr.getChalkboard() != null) && (chr.getChalkboard().length() > 0) ? 1 : 0);

		/*
		 * if (GameConstants.isKaiser(chr.getJob())) { //doesn't do shit?
		 * mplew.writeShort(0); mplew.write(0); mplew.writeInt(1);
		 * mplew.writeShort(0); }
		 */

		if ((chr.getChalkboard() != null) && (chr.getChalkboard().length() > 0)) {
			mplew.writeMapleAsciiString(chr.getChalkboard());
		}

		Triple rings = chr.getRings(false);
		addRingInfo(mplew, (List) rings.getLeft());
		addRingInfo(mplew, (List) rings.getMid());
		addMRingInfo(mplew, (List) rings.getRight(), chr);

		mplew.write(chr.getStat().Berserk ? 1 : 0); // mask
		mplew.writeInt(0);

		if (GameConstants.isKaiser(chr.getJob())) {
			String x = chr.getOneInfo(12860, "extern");
			mplew.writeInt(x == null ? 0 : Integer.parseInt(x));
			x = chr.getOneInfo(12860, "inner");
			mplew.writeInt(x == null ? 0 : Integer.parseInt(x));
			x = chr.getOneInfo(12860, "primium");
			mplew.write(x == null ? 0 : Integer.parseInt(x));
		}

		mplew.write(0); // new v142->v143
		mplew.writeInt(0); // new v142->v143

		PacketHelper.addFarmInfo(mplew, chr.getClient(), (byte) 0);
		for (int i = 0; i < 5; i++) {
			mplew.write(-1);
		}

		mplew.writeInt(0);
		mplew.write(0);
		mplew.writeInt(0);

		return mplew.getPacket();
	}

	public static byte[] removePlayerFromMap(int cid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.REMOVE_PLAYER_FROM_MAP.getValue());
		mplew.writeInt(cid);

		return mplew.getPacket();
	}

	public static byte[] getChatText(int cidfrom, String text, boolean whiteBG, int show) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.CHATTEXT.getValue());
		mplew.writeInt(cidfrom);
		mplew.write(whiteBG ? 1 : 0);
		mplew.writeMapleAsciiString(text);
		mplew.write(show);
		mplew.write(0);
		mplew.write(-1);
		// mplew.writeMapleAsciiString("[fuck]"); // new
		return mplew.getPacket();
	}

	public static byte[] getScrollEffect(int chr, Equip.ScrollResult scrollSuccess, boolean legendarySpirit, int item,
			int scroll) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SHOW_SCROLL_EFFECT.getValue());
		mplew.writeInt(chr);
		mplew.write(
				scrollSuccess == Equip.ScrollResult.SUCCESS ? 1 : scrollSuccess == Equip.ScrollResult.CURSE ? 2 : 0);
		mplew.write(legendarySpirit ? 1 : 0);
		mplew.writeInt(scroll); // scroll
		mplew.writeInt(item); // item
		mplew.writeInt(0);
		mplew.write(0);
		mplew.write(0);

		return mplew.getPacket();
	}

	public static byte[] showMagnifyingEffect(int chr, short pos) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SHOW_MAGNIFYING_EFFECT.getValue());
		mplew.writeInt(chr);
		mplew.writeShort(pos);
		mplew.write(0);// new 143 is in ida?

		return mplew.getPacket();
	}

	public static byte[] showPotentialReset(int chr, boolean success, int itemid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SHOW_POTENTIAL_RESET.getValue());
		mplew.writeInt(chr);
		mplew.writeBoolean(success);
		mplew.writeInt(itemid);

		return mplew.getPacket();
	}


	public static byte[] showBlackCubePotentialReset(int chr, boolean success, int itemId) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SHOW_POTENTIAL_BLACK_CUBE.getValue());
		mplew.writeInt(chr);
		mplew.writeBoolean(success);
		mplew.writeInt(itemId);

		return mplew.getPacket();
	}

	public static byte[] showNebuliteEffect(int chr, boolean success) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SHOW_NEBULITE_EFFECT.getValue());
		mplew.writeInt(chr);
		mplew.write(success ? 1 : 0);
		mplew.writeMapleAsciiString(success ? "Successfully mounted Nebulite." : "Failed to mount Nebulite.");

		return mplew.getPacket();
	}

	public static byte[] useNebuliteFusion(int cid, int itemId, boolean success) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SHOW_FUSION_EFFECT.getValue());
		mplew.writeInt(cid);
		mplew.write(success ? 1 : 0);
		mplew.writeInt(itemId);

		return mplew.getPacket();
	}

	public static byte[] pvpAttack(int cid, int playerLevel, int skill, int skillLevel, int speed, int mastery,
			int projectile, int attackCount, int chargeTime, int stance, int direction, int range, int linkSkill,
			int linkSkillLevel, boolean movementSkill, boolean pushTarget, boolean pullTarget,
			List<AttackPair> attack) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.PVP_ATTACK.getValue());
		mplew.writeInt(cid);
		mplew.write(playerLevel);
		mplew.writeInt(skill);
		mplew.write(skillLevel);
		mplew.writeInt(linkSkill != skill ? linkSkill : 0);
		mplew.write(linkSkillLevel != skillLevel ? linkSkillLevel : 0);
		mplew.write(direction);
		mplew.write(movementSkill ? 1 : 0);
		mplew.write(pushTarget ? 1 : 0);
		mplew.write(pullTarget ? 1 : 0);
		mplew.write(0);
		mplew.writeShort(stance);
		mplew.write(speed);
		mplew.write(mastery);
		mplew.writeInt(projectile);
		mplew.writeInt(chargeTime);
		mplew.writeInt(range);
		mplew.write(attack.size());
		mplew.write(0);
		mplew.writeInt(0);
		mplew.write(attackCount);
		mplew.write(0);
		for (AttackPair p : attack) {
			mplew.writeInt(p.objectid);
			mplew.writeInt(0);
			mplew.writePos(p.point);
			mplew.write(0);
			mplew.writeInt(0);
			for (Pair atk : p.attack) {
				mplew.writeInt(((Integer) atk.left).intValue());
				mplew.writeInt(0);
				mplew.write(((Boolean) atk.right).booleanValue() ? 1 : 0);
				mplew.writeShort(0);
			}
		}

		return mplew.getPacket();
	}

	public static byte[] getPVPMist(int cid, int mistSkill, int mistLevel, int damage) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.PVP_MIST.getValue());
		mplew.writeInt(cid);
		mplew.writeInt(mistSkill);
		mplew.write(mistLevel);
		mplew.writeInt(damage);
		mplew.write(8);
		mplew.writeInt(1000);

		return mplew.getPacket();
	}

	public static byte[] pvpCool(int cid, List<Integer> attack) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.PVP_COOL.getValue());
		mplew.writeInt(cid);
		mplew.write(attack.size());
		for (Iterator i$ = attack.iterator(); i$.hasNext();) {
			int b = ((Integer) i$.next()).intValue();
			mplew.writeInt(b);
		}

		return mplew.getPacket();
	}

	public static byte[] teslaTriangle(int cid, int sum1, int sum2, int sum3) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.TESLA_TRIANGLE.getValue());
		mplew.writeInt(cid);
		mplew.writeInt(sum1);
		mplew.writeInt(sum2);
		mplew.writeInt(sum3);

		mplew.write0(69);// test

		return mplew.getPacket();
	}

	public static byte[] followEffect(int initiator, int replier, Point toMap) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.FOLLOW_EFFECT.getValue());
		mplew.writeInt(initiator);
		mplew.writeInt(replier);
		mplew.writeLong(0);
		if (replier == 0) {
			mplew.write(toMap == null ? 0 : 1);
			if (toMap != null) {
				mplew.writeInt(toMap.x);
				mplew.writeInt(toMap.y);
			}
		}

		return mplew.getPacket();
	}

	public static byte[] showPQReward(int cid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SHOW_PQ_REWARD.getValue());
		mplew.writeInt(cid);
		for (int i = 0; i < 6; i++) {
			mplew.write(0);
		}

		return mplew.getPacket();
	}

	public static byte[] craftMake(int cid, int something, int time) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.CRAFT_EFFECT.getValue());
		mplew.writeInt(cid);
		mplew.writeInt(something);
		mplew.writeInt(time);

		return mplew.getPacket();
	}

	public static byte[] craftFinished(int cid, int craftID, int ranking, int itemId, int quantity, int exp) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.CRAFT_COMPLETE.getValue());
		mplew.writeInt(cid);
		mplew.writeInt(craftID);
		mplew.writeInt(ranking);
		mplew.writeInt(itemId);
		mplew.writeInt(quantity);
		mplew.writeInt(exp);

		return mplew.getPacket();
	}

	public static byte[] harvestResult(int cid, boolean success) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.HARVESTED.getValue());
		mplew.writeInt(cid);
		mplew.write(success ? 1 : 0);

		return mplew.getPacket();
	}

	public static byte[] playerDamaged(int cid, int dmg) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.PLAYER_DAMAGED.getValue());
		mplew.writeInt(cid);
		mplew.writeInt(dmg);

		return mplew.getPacket();
	}

	public static byte[] showPyramidEffect(int chr) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.NETT_PYRAMID.getValue());
		mplew.writeInt(chr);
		mplew.write(1);
		mplew.writeInt(0);
		mplew.writeInt(0);

		return mplew.getPacket();
	}

	public static byte[] pamsSongEffect(int cid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.PAMS_SONG.getValue());
		mplew.writeInt(cid);
		return mplew.getPacket();
	}

	public static byte[] spawnHaku_change0(int cid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.HAKU_CHANGE_0.getValue());
		mplew.writeInt(cid);

		return mplew.getPacket();
	}

	public static byte[] spawnHaku_change1(MapleHaku d) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.HAKU_CHANGE_1.getValue());
		mplew.writeInt(d.getOwner());
		mplew.writePos(d.getPosition());
		mplew.write(d.getStance());
		mplew.writeShort(0);
		mplew.write(0);
		mplew.writeInt(0);

		return mplew.getPacket();
	}

	public static byte[] spawnHaku_bianshen(int cid, int oid, boolean change) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.HAKU_CHANGE.getValue());
		mplew.writeInt(cid);
		mplew.writeInt(oid);
		mplew.write(change ? 2 : 1);

		return mplew.getPacket();
	}

	public static byte[] hakuUnk(int cid, int oid, boolean change) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.HAKU_CHANGE.getValue());
		mplew.writeInt(cid);
		mplew.writeInt(oid);
		mplew.write(0);
		mplew.write(0);
		mplew.writeMapleAsciiString("lol");

		return mplew.getPacket();
	}

	public static byte[] spawnHaku(MapleHaku d) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SPAWN_HAKU.getValue());
		mplew.writeInt(d.getOwner());
		mplew.writeInt(d.getObjectId());
		mplew.writeInt(40020109);
		mplew.write(1);
		mplew.writePos(d.getPosition());
		mplew.write(0);
		mplew.writeShort(d.getStance());

		return mplew.getPacket();
	}

	public static byte[] moveHaku(int cid, int oid, Point pos, List<LifeMovementFragment> res) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.HAKU_MOVE.getValue());
		mplew.writeInt(cid);
		mplew.writeInt(oid);
		mplew.writeInt(0);
		mplew.writePos(pos);
		mplew.writeInt(0);
		PacketHelper.serializeMovementList(mplew, res);
		return mplew.getPacket();
	}

	public static byte[] spawnDragon(MapleDragon d) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.DRAGON_SPAWN.getValue());
		mplew.writeInt(d.getOwner());
		mplew.writeInt(d.getPosition().x);
		mplew.writeInt(d.getPosition().y);
		mplew.write(d.getStance());
		mplew.writeShort(0);
		mplew.writeShort(d.getJobId());

		return mplew.getPacket();
	}

	public static byte[] removeDragon(int chrid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.DRAGON_REMOVE.getValue());
		mplew.writeInt(chrid);

		return mplew.getPacket();
	}

	public static byte[] moveDragon(MapleDragon d, Point startPos, List<LifeMovementFragment> moves) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.DRAGON_MOVE.getValue());
		mplew.writeInt(d.getOwner());
		mplew.writeInt(0);
		mplew.writePos(startPos);
		mplew.writeInt(0);
		PacketHelper.serializeMovementList(mplew, moves);

		return mplew.getPacket();
	}

	public static byte[] spawnAndroid(MapleCharacter cid, MapleAndroid android) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.ANDROID_SPAWN.getValue());
		mplew.writeInt(cid.getId());
		mplew.write(android.getItemId() == 1662006 ? 5 : android.getItemId() - 1661999);
		mplew.writePos(android.getPos());
		mplew.write(android.getStance());
		mplew.writeShort(0);
		mplew.writeShort(0);
		mplew.writeShort(android.getHair() - 30000);
		mplew.writeShort(android.getFace() - 20000);
		mplew.writeMapleAsciiString(android.getName());
		for (short i = -1200; i > -1207; i = (short) (i - 1)) {
			Item item = cid.getInventory(MapleInventoryType.EQUIPPED).getItem(i);
			mplew.writeInt(item != null ? item.getItemId() : 0);
		}

		return mplew.getPacket();
	}

	public static byte[] moveAndroid(int cid, Point pos, List<LifeMovementFragment> res) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.ANDROID_MOVE.getValue());
		mplew.writeInt(cid);
		mplew.writeInt(0);
		mplew.writePos(pos);
		mplew.writeInt(2147483647);
		PacketHelper.serializeMovementList(mplew, res);
		return mplew.getPacket();
	}

	public static byte[] showAndroidEmotion(int cid, byte emo1/* , byte emo2 */) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.ANDROID_EMOTION.getValue());
		mplew.writeInt(cid);
		mplew.write(0);// new
		mplew.write(emo1);

		return mplew.getPacket();
	}

	public static byte[] updateAndroidLook(boolean itemOnly, MapleCharacter cid, MapleAndroid android) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.ANDROID_UPDATE.getValue());
		mplew.writeInt(cid.getId());
		mplew.write(itemOnly ? 1 : 0);
		if (itemOnly) {
			for (short i = -1200; i > -1207; i = (short) (i - 1)) {
				Item item = cid.getInventory(MapleInventoryType.EQUIPPED).getItem(i);
				mplew.writeInt(item != null ? item.getItemId() : 0);
			}
		} else {
			mplew.writeShort(0);
			mplew.writeShort(android.getHair() - 30000);
			mplew.writeShort(android.getFace() - 20000);
			mplew.writeMapleAsciiString(android.getName());
		}

		return mplew.getPacket();
	}

	public static byte[] deactivateAndroid(int cid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.ANDROID_DEACTIVATED.getValue());
		mplew.writeInt(cid);

		return mplew.getPacket();
	}

	public static byte[] removeFamiliar(int cid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SPAWN_FAMILIAR.getValue());
		mplew.writeInt(cid);
		mplew.writeShort(0);
		mplew.write(0);

		return mplew.getPacket();
	}

	public static byte[] spawnFamiliar(MonsterFamiliar mf, boolean spawn, boolean respawn) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(respawn ? SendPacketOpcode.SPAWN_FAMILIAR_2.getValue() : SendPacketOpcode.SPAWN_FAMILIAR.getValue());
		mplew.writeInt(mf.getCharacterId());
		mplew.write(spawn ? 1 : 0);
		mplew.write(respawn ? 1 : 0);
		mplew.write(0);
		if (spawn) {
			mplew.writeInt(mf.getFamiliar());
			mplew.writeInt(mf.getFatigue());
			mplew.writeInt(mf.getVitality() * 300); // max fatigue
			mplew.writeMapleAsciiString(mf.getName());
			mplew.writePos(mf.getTruePosition());
			mplew.write(mf.getStance());
			mplew.writeShort(mf.getFh());
		}

		return mplew.getPacket();
	}

	public static byte[] moveFamiliar(int cid, Point startPos, List<LifeMovementFragment> moves) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.MOVE_FAMILIAR.getValue());
		mplew.writeInt(cid);
		mplew.write(0);
		mplew.writePos(startPos);
		mplew.writeInt(0);
		PacketHelper.serializeMovementList(mplew, moves);

		return mplew.getPacket();
	}

	public static byte[] touchFamiliar(int cid, byte unk, int objectid, int type, int delay, int damage) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.TOUCH_FAMILIAR.getValue());
		mplew.writeInt(cid);
		mplew.write(0);
		mplew.write(unk);
		mplew.writeInt(objectid);
		mplew.writeInt(type);
		mplew.writeInt(delay);
		mplew.writeInt(damage);

		return mplew.getPacket();
	}

	public static byte[] familiarAttack(int cid, byte unk, List<Triple<Integer, Integer, List<Integer>>> attackPair) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.ATTACK_FAMILIAR.getValue());
		mplew.writeInt(cid);
		mplew.write(0);// familiar id?
		mplew.write(unk);
		mplew.write(attackPair.size());
		for (Triple<Integer, Integer, List<Integer>> s : attackPair) {
			mplew.writeInt(s.left);
			mplew.write(s.mid);
			mplew.write(s.right.size());
			for (int damage : s.right) {
				mplew.writeInt(damage);
			}
		}

		return mplew.getPacket();
	}

	public static byte[] renameFamiliar(MonsterFamiliar mf) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.RENAME_FAMILIAR.getValue());
		mplew.writeInt(mf.getCharacterId());
		mplew.write(0);
		mplew.writeInt(mf.getFamiliar());
		mplew.writeMapleAsciiString(mf.getName());

		return mplew.getPacket();
	}

	public static byte[] updateFamiliar(MonsterFamiliar mf) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.UPDATE_FAMILIAR.getValue());
		mplew.writeInt(mf.getCharacterId());
		mplew.writeInt(mf.getFamiliar());
		mplew.writeInt(mf.getFatigue());
		mplew.writeLong(PacketHelper.getTime(mf.getVitality() >= 3 ? System.currentTimeMillis() : -2L));

		return mplew.getPacket();
	}

	public static byte[] movePlayer(int cid, List<LifeMovementFragment> moves, Point startPos) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.MOVE_PLAYER.getValue());
		mplew.writeInt(cid);
		mplew.writeInt(0);
		mplew.writePos(startPos);
		mplew.writeShort(0);
		mplew.writeShort(0);
		PacketHelper.serializeMovementList(mplew, moves);

		return mplew.getPacket();
	}

	public static byte[] closeRangeAttack(int cid, int tbyte, int skill, int level, int display, byte speed,
			List<AttackPair> damage, boolean energy, int lvl, byte mastery, byte unk, int charge) {
		return addAttackInfo(energy ? 4 : 0, cid, tbyte, skill, level, display, speed, damage, lvl, mastery, unk, 0,
				null, 0);
	}

	public static byte[] rangedAttack(int cid, int tbyte, int skill, int level, int display, byte speed, int itemid,
			List<AttackPair> damage, Point pos, int lvl, byte mastery, byte unk) {
		return addAttackInfo(1, cid, tbyte, skill, level, display, speed, damage, lvl, mastery, unk, itemid, pos, 0);
	}

	public static byte[] strafeAttack(int cid, int tbyte, int skill, int level, int display, byte speed, int itemid,
			List<AttackPair> damage, Point pos, int lvl, byte mastery, byte unk, int ultLevel) {
		return addAttackInfo(2, cid, tbyte, skill, level, display, speed, damage, lvl, mastery, unk, itemid, pos,
				ultLevel);
	}

	public static byte[] magicAttack(int cid, int tbyte, int skill, int level, int display, byte speed,
			List<AttackPair> damage, int charge, int lvl, byte unk) {
		return addAttackInfo(3, cid, tbyte, skill, level, display, speed, damage, lvl, (byte) 0, unk, charge, null, 0);
	}

	public static byte[] addAttackInfo(int type, int cid, int tbyte, int skill, int level, int display, byte speed,
			List<AttackPair> damage, int lvl, byte mastery, byte unk, int charge, Point pos, int ultLevel) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		if (type == 0) {
			mplew.writeShort(SendPacketOpcode.CLOSE_RANGE_ATTACK.getValue());
		} else if (type == 1 || type == 2) {
			mplew.writeShort(SendPacketOpcode.RANGED_ATTACK.getValue());
		} else if (type == 3) {
			mplew.writeShort(SendPacketOpcode.MAGIC_ATTACK.getValue());
		} else {
			mplew.writeShort(SendPacketOpcode.ENERGY_ATTACK.getValue());
		}

		mplew.writeInt(cid);
		mplew.write(tbyte);
		// System.out.println(nMobCount + " - nMobCount");
		mplew.write(lvl);
		if ((skill > 0) || (type == 3)) {
			mplew.write(level);
			if (level > 0) {
				mplew.writeInt(skill);
			}
		} else if (type != 2 && type != 3) {
			mplew.write(0);
		}

		if (GameConstants.isZero(skill / 10000) && skill != 100001283) {
			short zero1 = 0;
			short zero2 = 0;
			mplew.write(zero1 > 0 || zero2 > 0); // boolean
			if (zero1 > 0 || zero2 > 0) {
				mplew.writeShort(zero1);
				mplew.writeShort(zero2);
				// there is a full handler so better not write zero
			}
		}

		if (type == 2) {
			mplew.write(ultLevel);
			if (ultLevel > 0) {
				mplew.writeInt(3220010);
			}
		}
		if (skill == 40021185 || skill == 42001006) {
			mplew.write(0); // boolean if true then int
		}
		if (type == 0 || type == 1) {
			mplew.write(0);
		}
		mplew.write(unk);// always 0?
		if ((unk & 2) != 0) {
			mplew.writeInt(0);
			mplew.writeInt(0);
		}
		mplew.writeShort(display);
		mplew.write(speed);
		mplew.write(mastery);
		mplew.writeInt(charge);
		for (AttackPair oned : damage) {
			if (oned.attack != null) {
				mplew.writeInt(oned.objectid);
				mplew.write(7);
				mplew.write(0);
				mplew.write(0);
				if (skill == 42111002) {
					mplew.write(oned.attack.size());
					for (Pair eachd : oned.attack) {
						mplew.writeInt(((Integer) eachd.left).intValue());
					}
				} else {
					for (Pair eachd : oned.attack) {
						mplew.write(((Boolean) eachd.right).booleanValue() ? 1 : 0);
						mplew.writeInt(((Integer) eachd.left).intValue());
					}
				}
			}
		}
		if (skill == 2321001 || skill == 2221052 || skill == 11121052) {
			mplew.writeInt(0);
		} else if (skill == 65121052 || skill == 101000202 || skill == 101000102) {
			mplew.writeInt(0);
			mplew.writeInt(0);
		}
		if (skill == 42100007) {
			mplew.writeShort(0);
			mplew.write(0);
		}
		if (type == 1 || type == 2) {
			mplew.writePos(pos);
		} else if (type == 3 && charge > 0) {
			mplew.writeInt(charge);
		}
		if (skill == 5321000 || skill == 5311001 || skill == 5321001 || skill == 5011002 || skill == 5311002
				|| skill == 5221013 || skill == 5221017 || skill == 3120019 || skill == 3121015 || skill == 4121017) {
			mplew.writePos(pos);
		}
		mplew.write0(30);// test

		return mplew.getPacket();
	}

	public static byte[] skillEffect(MapleCharacter from, int skillId, byte level, short display, byte unk) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SKILL_EFFECT.getValue());
		mplew.writeInt(from.getId());
		mplew.writeInt(skillId);
		mplew.write(level);
		mplew.writeShort(display);
		mplew.write(unk);
		if (skillId == 13111020) {
			mplew.writePos(from.getPosition()); // Position
		}

		return mplew.getPacket();
	}

	public static byte[] skillCancel(MapleCharacter from, int skillId) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.CANCEL_SKILL_EFFECT.getValue());
		mplew.writeInt(from.getId());
		mplew.writeInt(skillId);

		return mplew.getPacket();
	}

	public static byte[] damagePlayer(int cid, int type, int damage, int monsteridfrom, byte direction, int skillid,
			int pDMG, boolean pPhysical, int pID, byte pType, Point pPos, byte offset, int offset_d, int fake) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.DAMAGE_PLAYER.getValue());
		mplew.writeInt(cid);
		mplew.write(type);
		mplew.writeInt(damage);
		mplew.write(0);
		if (type >= -1) {
			mplew.writeInt(monsteridfrom);
			mplew.write(direction);
			mplew.writeInt(skillid);
			mplew.writeInt(pDMG);
			mplew.write(0);
			if (pDMG > 0) {
				mplew.write(pPhysical ? 1 : 0);
				mplew.writeInt(pID);
				mplew.write(pType);
				mplew.writePos(pPos);
			}
			mplew.write(offset);
			if (offset == 1) {
				mplew.writeInt(offset_d);
			}
		}
		mplew.writeInt(damage);
		if ((damage <= 0) || (fake > 0)) {
			mplew.writeInt(fake);
		}

		return mplew.getPacket();
	}

	public static byte[] facialExpression(MapleCharacter from, int expression) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.FACIAL_EXPRESSION.getValue());
		mplew.writeInt(from.getId());
		mplew.writeInt(expression);
		mplew.writeInt(-1);
		mplew.write(0);

		return mplew.getPacket();
	}

	public static byte[] directionFacialExpression(int expression, int duration) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.DIRECTION_FACIAL_EXPRESSION.getValue());
		mplew.writeInt(expression);
		mplew.writeInt(duration);
		mplew.write(0);

		/*
		 * Facial Expressions: 0 - Normal 1 - F1 2 - F2 3 - F3 4 - F4 5 - F5 6 -
		 * F6 7 - F7 8 - Vomit 9 - Panic 10 - Sweetness 11 - Kiss 12 - Wink 13 -
		 * Ouch! 14 - Goo goo eyes 15 - Blaze 16 - Star 17 - Love 18 - Ghost 19
		 * - Constant Sigh 20 - Sleepy 21 - Flaming hot 22 - Bleh 23 - No Face
		 */
		return mplew.getPacket();
	}

	public static byte[] itemEffect(int characterid, int itemid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SHOW_EFFECT.getValue());
		mplew.writeInt(characterid);
		mplew.writeInt(itemid);

		return mplew.getPacket();
	}

	public static byte[] showTitle(int characterid, int itemid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SHOW_TITLE.getValue());
		mplew.writeInt(characterid);
		mplew.writeInt(itemid);

		return mplew.getPacket();
	}

	public static byte[] showAngelicBuster(int characterid, int tempid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.ANGELIC_CHANGE.getValue());
		mplew.writeInt(characterid);
		mplew.writeInt(tempid);

		return mplew.getPacket();
	}

	public static byte[] showChair(int characterid, int itemid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SHOW_CHAIR.getValue());
		mplew.writeInt(characterid);
		mplew.writeInt(itemid);
		mplew.writeInt(0);

		return mplew.getPacket();
	}

	public static byte[] updateCharLook(MapleCharacter chr, boolean second) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.UPDATE_CHAR_LOOK.getValue());
		mplew.writeInt(chr.getId());
		mplew.write(1);
		PacketHelper.addCharLook(mplew, chr, false, second);
		Triple<List<MapleRing>, List<MapleRing>, List<MapleRing>> rings = chr.getRings(false);
		addRingInfo(mplew, rings.getLeft());
		addRingInfo(mplew, rings.getMid());
		addMRingInfo(mplew, rings.getRight(), chr);
		mplew.writeInt(0); // -> charid to follow (4)
		mplew.writeInt(0);
		return mplew.getPacket();
	}

	public static byte[] updatePartyMemberHP(int cid, int curhp, int maxhp) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.UPDATE_PARTYMEMBER_HP.getValue());
		mplew.writeInt(cid);
		mplew.writeInt(curhp);
		mplew.writeInt(maxhp);

		return mplew.getPacket();
	}

	public static byte[] loadGuildName(MapleCharacter chr) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.LOAD_GUILD_NAME.getValue());
		mplew.writeInt(chr.getId());
		if (chr.getGuildId() <= 0) {
			mplew.writeShort(0);
		} else {
			MapleGuild gs = World.Guild.getGuild(chr.getGuildId());
			if (gs != null) {
				mplew.writeMapleAsciiString(gs.getName());
			} else {
				mplew.writeShort(0);
			}
		}

		return mplew.getPacket();
	}

	public static byte[] loadGuildIcon(MapleCharacter chr) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.LOAD_GUILD_ICON.getValue());
		mplew.writeInt(chr.getId());
		if (chr.getGuildId() <= 0) {
			mplew.write0(6);
		} else {
			MapleGuild gs = World.Guild.getGuild(chr.getGuildId());
			if (gs != null) {
				mplew.writeShort(gs.getLogoBG());
				mplew.write(gs.getLogoBGColor());
				mplew.writeShort(gs.getLogo());
				mplew.write(gs.getLogoColor());
			} else {
				mplew.write0(6);
			}
		}

		return mplew.getPacket();
	}

	public static byte[] changeTeam(int cid, int type) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.LOAD_TEAM.getValue());
		mplew.writeInt(cid);
		mplew.write(type);

		return mplew.getPacket();
	}

	public static byte[] showHarvesting(int cid, int tool) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SHOW_HARVEST.getValue());
		mplew.writeInt(cid);
		if (tool > 0) {
			mplew.write(1);
			mplew.write(0);
			mplew.writeShort(0);
			mplew.writeInt(tool);
			mplew.write0(30);
		} else {
			mplew.write(0);
			mplew.write0(33);
		}

		return mplew.getPacket();
	}

	public static byte[] getPVPHPBar(int cid, int hp, int maxHp) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.PVP_HP.getValue());
		mplew.writeInt(cid);
		mplew.writeInt(hp);
		mplew.writeInt(maxHp);

		return mplew.getPacket();
	}

	public static byte[] cancelChair(int id) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.CANCEL_CHAIR.getValue());
		if (id == -1) {
			mplew.write(0);
		} else {
			mplew.write(1);
			mplew.writeShort(id);
		}

		return mplew.getPacket();
	}

	public static byte[] instantMapWarp(byte portal) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.CURRENT_MAP_WARP.getValue());
		mplew.write(0);
		mplew.write(portal); // nUserCallingType
		
		if (portal <= 0) {
			mplew.writeInt(0); // nIdx
		} else {
			mplew.writeInt(0); // dwCallerID
			mplew.writeShort(0); // x
			mplew.writeShort(0); // y
		}

		return mplew.getPacket();
	}

	public static byte[] updateQuestInfo(MapleCharacter c, int quest, int npc, byte progress) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.UPDATE_QUEST_INFO.getValue());
		mplew.write(progress);
		mplew.writeInt(quest);
		mplew.writeInt(npc);
		mplew.writeInt(0);
		mplew.write(0); // new

		return mplew.getPacket();
	}

	public static byte[] updateQuestFinish(int quest, int npc, int nextquest) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.UPDATE_QUEST_INFO.getValue());
		mplew.write(11);// was 10
		mplew.writeInt(quest);
		mplew.writeInt(npc);
		mplew.writeInt(nextquest);
		mplew.write(0); // new

		return mplew.getPacket();
	}

	public static byte[] sendHint(String hint, int width, int height) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.PLAYER_HINT.getValue());
		mplew.writeMapleAsciiString(hint);
		mplew.writeShort(width < 1 ? Math.max(hint.length() * 10, 40) : width);
		mplew.writeShort(Math.max(height, 5));
		mplew.write(1);

		return mplew.getPacket();
	}

	public static byte[] updateCombo(int value) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.ARAN_COMBO.getValue());
		mplew.writeInt(value);

		return mplew.getPacket();
	}

	public static byte[] rechargeCombo(int value) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.ARAN_COMBO_RECHARGE.getValue());
		mplew.writeInt(value);

		return mplew.getPacket();
	}

	public static byte[] getFollowMessage(String msg) {
		return getGameMessage(msg, (short) 11);
	}

	public static byte[] getGameMessage(String msg, short colour) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.GAME_MESSAGE.getValue());
		mplew.writeShort(colour);
		mplew.writeMapleAsciiString(msg);

		return mplew.getPacket();
	}

	public static byte[] getBuffZoneEffect(int itemId) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.BUFF_ZONE_EFFECT.getValue());
		mplew.writeInt(itemId);

		return mplew.getPacket();
	}

	public static byte[] getTimeBombAttack() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.TIME_BOMB_ATTACK.getValue());
		mplew.writeInt(0);
		mplew.writeInt(0);
		mplew.writeInt(0);
		mplew.writeInt(10);
		mplew.writeInt(6);

		return mplew.getPacket();
	}

	public static byte[] moveFollow(Point otherStart, Point myStart, Point otherEnd, List<LifeMovementFragment> moves) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.FOLLOW_MOVE.getValue());
		mplew.writeInt(0);
		mplew.writePos(otherStart);
		mplew.writePos(myStart);
		PacketHelper.serializeMovementList(mplew, moves);
		mplew.write(17);
		for (int i = 0; i < 8; i++) {
			mplew.write(0);
		}
		mplew.write(0);
		mplew.writePos(otherEnd);
		mplew.writePos(otherStart);
		mplew.write0(100);

		return mplew.getPacket();
	}

	public static byte[] getFollowMsg(int opcode) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.FOLLOW_MSG.getValue());
		mplew.writeInt(opcode);
		mplew.writeInt(0);

		return mplew.getPacket();
	}

	public static byte[] registerFamiliar(MonsterFamiliar mf) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.REGISTER_FAMILIAR.getValue());
		mplew.writeLong(mf.getId());
		mf.writeRegisterPacket(mplew, false);
		mplew.write(mf.getVitality() >= 3 ? 1 : 0);
		mplew.write(0);

		return mplew.getPacket();
	}

	public static byte[] createUltimate(int amount) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.CREATE_ULTIMATE.getValue());
		mplew.writeInt(amount);

		return mplew.getPacket();
	}

	public static byte[] harvestMessage(int oid, int msg) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.HARVEST_MESSAGE.getValue());
		mplew.writeInt(oid);
		mplew.writeInt(msg);

		return mplew.getPacket();
	}

	public static byte[] openBag(int index, int itemId, boolean firstTime) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.OPEN_BAG.getValue());
		mplew.writeInt(index);
		mplew.writeInt(itemId);
		mplew.write(firstTime ? 1 : 0);
		mplew.write(0);

		return mplew.getPacket();
	}

	public static byte[] dragonBlink(int portalId) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.DRAGON_BLINK.getValue());
		mplew.write(portalId);

		return mplew.getPacket();
	}

	public static byte[] getPVPIceGage(int score) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.PVP_ICEGAGE.getValue());
		mplew.writeInt(score);

		return mplew.getPacket();
	}

	public static byte[] skillCooldown(int sid, int time) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		
		mplew.writeShort(SendPacketOpcode.COOLDOWN.getValue());
		mplew.writeInt(1);
		mplew.writeInt(sid);
		mplew.writeInt(time);

		return mplew.getPacket();
	}

	public static byte[] dropItemFromMapObject(MapleMapItem drop, Point dropfrom, Point dropto, byte mod) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.DROP_ITEM_FROM_MAPOBJECT.getValue());
		mplew.write(0); // eDropType
		mplew.write(mod); // nEnterType
		mplew.writeInt(drop.getObjectId()); // m_mDrop
		mplew.write(drop.getMeso() > 0 ? 1 : 0); // bIsMoney
		mplew.writeInt(0); // nDropMotionType
		mplew.writeInt(0); // nDropSpeed
		mplew.writeInt(0); // bNoMove
		mplew.writeInt(drop.getItemId()); // fRand
		mplew.writeInt(drop.getOwner()); // nInfo
		mplew.write(drop.getDropType()); // dwOwnType
		mplew.writePos(dropto); //ptDrop x, y
		mplew.writeInt(0); // dwSourceID
		if (mod != 2) {
			mplew.writePos(dropfrom);
			mplew.writeInt(0); // tDelay
		}
		mplew.write(0); // bExplosiveDrop
		
		if (drop.getMeso() == 0) {
			PacketHelper.addExpirationTime(mplew, drop.getItem().getExpiration());
		}
		
		mplew.write(drop.isPlayerDrop() ? 0 : 1); // bByPet
		mplew.write(0); // ?
		mplew.writeShort(0); // nFallingVY
		mplew.write(0); // nFadeInEffect
		mplew.write(0); // nMakeType
		mplew.writeInt(0); // bCollisionPickup
		mplew.write(0); // nItemGrade
		mplew.write(0); // bPrepareCollisionPickUp
		return mplew.getPacket();
	}

	public static byte[] explodeDrop(int oid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.REMOVE_ITEM_FROM_MAP.getValue());
		mplew.write(4);
		mplew.writeInt(oid);
		mplew.writeShort(655);

		return mplew.getPacket();
	}

	public static byte[] removeItemFromMap(int oid, int animation, int cid) {
		return removeItemFromMap(oid, animation, cid, 0);
	}

	public static byte[] removeItemFromMap(int oid, int animation, int cid, int slot) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.REMOVE_ITEM_FROM_MAP.getValue());
		mplew.write(animation);
		mplew.writeInt(oid);
		if (animation >= 2) {
			mplew.writeInt(cid);
			if (animation == 5) {
				mplew.writeInt(slot);
			}
		}
		return mplew.getPacket();
	}

	public static byte[] spawnMist(MapleMist mist) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SPAWN_MIST.getValue());
		mplew.writeInt(mist.getObjectId());

		// mplew.write(mist.isMobMist() ? 0 : mist.isPoisonMist());
		mplew.write(0);
		mplew.writeInt(mist.getOwnerId());
		if (mist.getMobSkill() == null) {
			mplew.writeInt(mist.getSourceSkill().getId());
		} else {
			mplew.writeInt(mist.getMobSkill().getSkillId());
		}
		mplew.write(mist.getSkillLevel());
		mplew.writeShort(mist.getSkillDelay());
		mplew.writeRect(mist.getBox());
		mplew.writeInt(mist.isShelter() ? 1 : 0);
		mplew.writeInt(0);
		mplew.writePos(mist.getPosition());
		mplew.writeInt(0);
		mplew.writeInt(0);

		return mplew.getPacket();
	}

	public static byte[] removeMist(int oid, boolean eruption) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.REMOVE_MIST.getValue());
		mplew.writeInt(oid);
		mplew.write(eruption ? 1 : 0);

		return mplew.getPacket();
	}

	public static byte[] spawnDoor(int oid, Point pos, boolean animation) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SPAWN_DOOR.getValue());
		mplew.write(animation ? 0 : 1);
		mplew.writeInt(oid);
		mplew.writePos(pos);

		return mplew.getPacket();
	}

	public static byte[] removeDoor(int oid, boolean animation) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.REMOVE_DOOR.getValue());
		mplew.write(animation ? 0 : 1);
		mplew.writeInt(oid);

		return mplew.getPacket();
	}

	public static byte[] spawnKiteError() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SPAWN_KITE_ERROR.getValue());

		return mplew.getPacket();
	}

	public static byte[] spawnKite(int oid, int id, Point pos) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SPAWN_KITE.getValue());
		mplew.writeInt(oid);
		mplew.writeInt(0);
		mplew.writeMapleAsciiString("");
		mplew.writeMapleAsciiString("");
		mplew.writePos(pos);

		return mplew.getPacket();
	}

	public static byte[] destroyKite(int oid, int id, boolean animation) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.DESTROY_KITE.getValue());
		mplew.write(animation ? 0 : 1);
		mplew.writeInt(oid);

		return mplew.getPacket();
	}

	public static byte[] spawnMechDoor(MechDoor md, boolean animated) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.MECH_DOOR_SPAWN.getValue());
		mplew.write(animated ? 0 : 1);
		mplew.writeInt(md.getOwnerId());
		mplew.writePos(md.getTruePosition());
		mplew.write(md.getId());
		mplew.writeInt(md.getPartyId());
		return mplew.getPacket();
	}

	public static byte[] removeMechDoor(MechDoor md, boolean animated) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.MECH_DOOR_REMOVE.getValue());
		mplew.write(animated ? 0 : 1);
		mplew.writeInt(md.getOwnerId());
		mplew.write(md.getId());

		return mplew.getPacket();
	}

	public static byte[] triggerReactor(MapleReactor reactor, short stance, int dwOwnerID) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.REACTOR_HIT.getValue());
                mplew.writeInt(reactor.getObjectId()); //m_mReactor
                mplew.write(reactor.getState()); //nState
                mplew.writePos(reactor.getTruePosition()); //ptPos.x, ptPos.y
                mplew.writeShort(stance); //Should be short, KMST IDA && 176.1 IDA
                mplew.write(0); //nProperEventIdx
                mplew.write(4); //tStateEnd (time + 100 * value)
                mplew.writeInt(dwOwnerID); //KMST && 176.1 IDA stated another Int here, dwOwnerID. 
		return mplew.getPacket();
	}

	public static byte[] spawnReactor(MapleReactor reactor) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.REACTOR_SPAWN.getValue());
		mplew.writeInt(reactor.getObjectId());
		mplew.writeInt(reactor.getReactorId());
		mplew.write(reactor.getState());
		mplew.writePos(reactor.getTruePosition());
		mplew.write(reactor.getFacingDirection());
		mplew.writeMapleAsciiString(reactor.getName());

		return mplew.getPacket();
	}

	public static byte[] destroyReactor(MapleReactor reactor) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.REACTOR_DESTROY.getValue());
		mplew.writeInt(reactor.getObjectId());
		mplew.write(reactor.getState());
		mplew.writePos(reactor.getPosition());

		return mplew.getPacket();
	}

	public static byte[] makeExtractor(int cid, String cname, Point pos, int timeLeft, int itemId, int fee) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SPAWN_EXTRACTOR.getValue());
		mplew.writeInt(cid);
		mplew.writeMapleAsciiString(cname);
		mplew.writeInt(pos.x);
		mplew.writeInt(pos.y);
		mplew.writeShort(timeLeft);
		mplew.writeInt(itemId);
		mplew.writeInt(fee);

		return mplew.getPacket();
	}

	public static byte[] removeExtractor(int cid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.REMOVE_EXTRACTOR.getValue());
		mplew.writeInt(cid);
		mplew.writeInt(1);

		return mplew.getPacket();
	}

	public static byte[] rollSnowball(int type, MapleSnowball.MapleSnowballs ball1,
			MapleSnowball.MapleSnowballs ball2) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.ROLL_SNOWBALL.getValue());
		mplew.write(type);
		mplew.writeInt(ball1 == null ? 0 : ball1.getSnowmanHP() / 75);
		mplew.writeInt(ball2 == null ? 0 : ball2.getSnowmanHP() / 75);
		mplew.writeShort(ball1 == null ? 0 : ball1.getPosition());
		mplew.write(0);
		mplew.writeShort(ball2 == null ? 0 : ball2.getPosition());
		mplew.write0(11);

		return mplew.getPacket();
	}

	public static byte[] enterSnowBall() {
		return rollSnowball(0, null, null);
	}

	public static byte[] hitSnowBall(int team, int damage, int distance, int delay) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.HIT_SNOWBALL.getValue());
		mplew.write(team);
		mplew.writeShort(damage);
		mplew.write(distance);
		mplew.write(delay);

		return mplew.getPacket();
	}

	public static byte[] snowballMessage(int team, int message) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SNOWBALL_MESSAGE.getValue());
		mplew.write(team);
		mplew.writeInt(message);

		return mplew.getPacket();
	}

	public static byte[] leftKnockBack() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.LEFT_KNOCK_BACK.getValue());

		return mplew.getPacket();
	}

	public static byte[] hitCoconut(boolean spawn, int id, int type) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.HIT_COCONUT.getValue());
		mplew.writeInt(spawn ? 32768 : id);
		mplew.write(spawn ? 0 : type);

		return mplew.getPacket();
	}

	public static byte[] coconutScore(int[] coconutscore) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.COCONUT_SCORE.getValue());
		mplew.writeShort(coconutscore[0]);
		mplew.writeShort(coconutscore[1]);

		return mplew.getPacket();
	}

	public static byte[] updateWitchTowerKeys(int keys) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.WITCH_TOWER.getValue());
		mplew.write(keys);

		return mplew.getPacket();
	}

	public static byte[] showChaosZakumShrine(boolean spawned, int time) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.CHAOS_ZAKUM_SHRINE.getValue());
		mplew.write(spawned ? 1 : 0);
		mplew.writeInt(time);

		return mplew.getPacket();
	}

	public static byte[] showChaosHorntailShrine(boolean spawned, int time) {
		return showHorntailShrine(spawned, time);
	}

	public static byte[] showHorntailShrine(boolean spawned, int time) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.HORNTAIL_SHRINE.getValue());
		mplew.write(spawned ? 1 : 0);
		mplew.writeInt(time);

		return mplew.getPacket();
	}

	public static byte[] getRPSMode(byte mode, int mesos, int selection, int answer) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.RPS_GAME.getValue());
		mplew.write(mode);
		switch (mode) {
		case 6:
			if (mesos == -1) {
				break;
			}
			mplew.writeInt(mesos);
			break;
		case 8:
			mplew.writeInt(9000019);
			break;
		case 11:
			mplew.write(selection);
			mplew.write(answer);
		}

		return mplew.getPacket();
	}

	public static byte[] messengerInvite(String from, int messengerid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
		mplew.write(3);
		mplew.writeMapleAsciiString(from);
		mplew.write(1);// channel?
		mplew.writeInt(messengerid);
		mplew.write(0);

		return mplew.getPacket();
	}

	public static byte[] addMessengerPlayer(String from, MapleCharacter chr, int position, int channel) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
		mplew.write(0);
		mplew.write(position);
		PacketHelper.addCharLook(mplew, chr, true, false);
		mplew.writeMapleAsciiString(from);
		mplew.write(channel);
		mplew.write(1); // v140
		mplew.writeInt(chr.getJob());

		return mplew.getPacket();
	}

	public static byte[] removeMessengerPlayer(int position) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
		mplew.write(2);
		mplew.write(position);

		return mplew.getPacket();
	}

	public static byte[] updateMessengerPlayer(String from, MapleCharacter chr, int position, int channel) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
		mplew.write(0); // v140.
		mplew.write(position);
		PacketHelper.addCharLook(mplew, chr, true, false);
		mplew.writeMapleAsciiString(from);
		mplew.write(channel);
		mplew.write(0); // v140.
		mplew.writeInt(chr.getJob()); // doubt it's the job, lol. v140.

		return mplew.getPacket();
	}

	public static byte[] joinMessenger(int position) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
		mplew.write(1);
		mplew.write(position);

		return mplew.getPacket();
	}

	public static byte[] messengerChat(String charname, String text) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
		mplew.write(6);
		mplew.writeMapleAsciiString(charname);
		mplew.writeMapleAsciiString(text);

		return mplew.getPacket();
	}

	public static byte[] messengerNote(String text, int mode, int mode2) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
		mplew.write(mode);
		mplew.writeMapleAsciiString(text);
		mplew.write(mode2);

		return mplew.getPacket();
	}

	public static byte[] messengerOpen(byte type, List<MapleCharacter> chars) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.MESSENGER_OPEN.getValue());
		mplew.write(type); // 7 in messenger open ui 8 new ui
		if (chars.isEmpty()) {
			mplew.writeShort(0);
		}
		for (MapleCharacter chr : chars) {
			mplew.write(1);
			mplew.writeInt(chr.getId());
			mplew.writeInt(0); // likes
			mplew.writeLong(0); // some time
			mplew.writeMapleAsciiString(chr.getName());
			PacketHelper.addCharLook(mplew, chr, true, false);
		}

		return mplew.getPacket();
	}

	public static byte[] messengerCharInfo(MapleCharacter chr) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
		mplew.write(0x0B);
		mplew.writeMapleAsciiString(chr.getName());
		mplew.writeInt(chr.getJob());
		mplew.writeInt(chr.getFame());
		mplew.writeInt(0); // likes
		MapleGuild gs = World.Guild.getGuild(chr.getGuildId());
		mplew.writeMapleAsciiString(gs != null ? gs.getName() : "-");
		MapleGuildAlliance alliance = World.Alliance.getAlliance(gs.getAllianceId());
		mplew.writeMapleAsciiString(alliance != null ? alliance.getName() : "");
		mplew.write(2);

		return mplew.getPacket();
	}

	public static byte[] removeFromPackageList(boolean remove, int Package) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.PACKAGE_OPERATION.getValue());
		mplew.write(24);
		mplew.writeInt(Package);
		mplew.write(remove ? 3 : 4);

		return mplew.getPacket();
	}

	public static byte[] sendPackageMSG(byte operation, List<MaplePackageActions> packages) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.PACKAGE_OPERATION.getValue());
		mplew.write(operation);

		switch (operation) {
		case 9:
			mplew.write(1);
			break;
		case 10:
			mplew.write(0);
			mplew.write(packages.size());

			for (MaplePackageActions dp : packages) {
				mplew.writeInt(dp.getPackageId());
				mplew.writeAsciiString(dp.getSender(), 13);
				mplew.writeInt(dp.getMesos());
				mplew.writeLong(PacketHelper.getTime(dp.getSentTime()));
				mplew.write0(205);

				if (dp.getItem() != null) {
					mplew.write(1);
					PacketHelper.addItemInfo(mplew, dp.getItem());
				} else {
					mplew.write(0);
				}
			}
			mplew.write(0);
		}

		return mplew.getPacket();
	}

	public static byte[] getKeymap(MapleKeyLayout layout) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.KEYMAP.getValue());
		layout.writeData(mplew);

		return mplew.getPacket();
	}

	public static byte[] petAutoHP(int itemId) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.PET_AUTO_HP.getValue());
		mplew.writeInt(itemId);

		return mplew.getPacket();
	}

	public static byte[] petAutoMP(int itemId) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.PET_AUTO_MP.getValue());
		mplew.writeInt(itemId);

		return mplew.getPacket();
	}

	public static byte[] petAutoCure(int itemId) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.PET_AUTO_CURE.getValue());
		mplew.writeInt(itemId);

		return mplew.getPacket();
	}

	public static byte[] petAutoBuff(int skillId) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		// mplew.writeShort(SendPacketOpcode.PET_AUTO_BUFF.getValue());
		mplew.writeInt(skillId);

		return mplew.getPacket();
	}

	public static void addRingInfo(MaplePacketLittleEndianWriter mplew, List<MapleRing> rings) {
		mplew.write(rings.size());
		for (MapleRing ring : rings) {
			mplew.writeInt(1);
			mplew.writeLong(ring.getRingId());
			mplew.writeLong(ring.getPartnerRingId());
			mplew.writeInt(ring.getItemId());
		}
	}

	public static void addMRingInfo(MaplePacketLittleEndianWriter mplew, List<MapleRing> rings, MapleCharacter chr) {
		mplew.write(rings.size());
		for (MapleRing ring : rings) {
			mplew.writeInt(1);
			mplew.writeInt(chr.getId());
			mplew.writeInt(ring.getPartnerChrId());
			mplew.writeInt(ring.getItemId());
		}
	}

	public static byte[] getBuffBar(long millis) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.BUFF_BAR.getValue());
		mplew.writeLong(millis);

		return mplew.getPacket();
	}

	public static byte[] getBoosterFamiliar(int cid, int familiar, int id) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.BOOSTER_FAMILIAR.getValue());
		mplew.writeInt(cid);
		mplew.writeInt(familiar);
		mplew.writeLong(id);
		mplew.write(0);

		return mplew.getPacket();
	}

	static {
		DEFAULT_BUFFMASK |= MapleBuffStat.EnergyCharged.getValue();
		DEFAULT_BUFFMASK |= MapleBuffStat.Dash_Speed.getValue();
		DEFAULT_BUFFMASK |= MapleBuffStat.Dash_Jump.getValue();
		DEFAULT_BUFFMASK |= MapleBuffStat.RideVehicle.getValue();
		DEFAULT_BUFFMASK |= MapleBuffStat.Speed.getValue();
		DEFAULT_BUFFMASK |= MapleBuffStat.StopForceAtomInfo.getValue();
		DEFAULT_BUFFMASK |= MapleBuffStat.DEFAULT_BUFFSTAT.getValue();
	}

	public static byte[] viewSkills(MapleCharacter chr) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.TARGET_SKILL.getValue());
		List skillz = new ArrayList();
		for (Skill sk : chr.getSkills().keySet()) {
			if ((sk.canBeLearnedBy(chr.getJob())) && (GameConstants.canSteal(sk))
					&& (!skillz.contains(Integer.valueOf(sk.getId())))) {
						 skillz.add(Integer.valueOf(sk.getId()));
			}
		}
		mplew.write(1);
		mplew.writeInt(chr.getId());
		mplew.writeInt(skillz.isEmpty() ? 2 : 4);
		mplew.writeInt(chr.getJob());
		mplew.writeInt(skillz.size());
		for (Iterator i$ = skillz.iterator(); i$.hasNext();) {
			int i = ((Integer) i$.next()).intValue();
			mplew.writeInt(i);
		}

		return mplew.getPacket();
	}

	public static class InteractionPacket {

		public static byte[] getTradeInvite(MapleCharacter c) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
			mplew.write(PlayerInteractionHandler.Interaction.INVITE_TRADE.action);
			mplew.write(4);// was 3
			mplew.writeMapleAsciiString(c.getName());
			// mplew.writeInt(c.getLevel());
			mplew.writeInt(c.getJob());
			return mplew.getPacket();
		}

		public static byte[] getTradeMesoSet(byte number, long meso) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
			mplew.write(PlayerInteractionHandler.Interaction.UPDATE_MESO.action);
			mplew.write(number);
			mplew.writeLong(meso);
			return mplew.getPacket();
		}

		public static byte[] getTradeItemAdd(byte number, Item item) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
			mplew.write(PlayerInteractionHandler.Interaction.SET_ITEMS.action);
			mplew.write(number);
			mplew.write(item.getPosition());
			PacketHelper.addItemInfo(mplew, item);

			return mplew.getPacket();
		}

		public static byte[] getTradeStart(MapleClient c, MapleTrade trade, byte number) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
			// mplew.write(PlayerInteractionHandler.Interaction.START_TRADE.action);
			// if (number != 0){//13 a0
			//// mplew.write(HexTool.getByteArrayFromHexString("13 01 01 03 FE
			// 53 00 00 40 08 00 00 00 E2 7B 00 00 01 E9 50 0F 00 03 62 98 0F 00
			// 04 56 BF 0F 00 05 2A E7 0F 00 07 B7 5B 10 00 08 3D 83 10 00 09 D3
			// D1 10 00 0B 13 01 16 00 11 8C 1F 11 00 12 BF 05 1D 00 13 CB 2C 1D
			// 00 31 40 6F 11 00 32 6B 46 11 00 35 32 5C 19 00 37 20 E2 11 00 FF
			// 03 B6 98 0F 00 05 AE 0A 10 00 09 CC D0 10 00 FF FF 00 00 00 00 13
			// 01 16 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0B 00
			// 4D 6F 6D 6F 6C 6F 76 65 73 4B 48 40 08"));
			// mplew.write(19);
			// mplew.write(1);
			// PacketHelper.addCharLook(mplew, trade.getPartner().getChr(),
			// false);
			// mplew.writeMapleAsciiString(trade.getPartner().getChr().getName());
			// mplew.writeShort(trade.getPartner().getChr().getJob());
			// }else{
			mplew.write(20);
			mplew.write(4);
			mplew.write(2);
			mplew.write(number);

			if (number == 1) {
				mplew.write(0);
				PacketHelper.addCharLook(mplew, trade.getPartner().getChr(), false, false);
				mplew.writeMapleAsciiString(trade.getPartner().getChr().getName());
				mplew.writeShort(trade.getPartner().getChr().getJob());
			}
			mplew.write(number);
			PacketHelper.addCharLook(mplew, c.getPlayer(), false, false);
			mplew.writeMapleAsciiString(c.getPlayer().getName());
			mplew.writeShort(c.getPlayer().getJob());
			mplew.write(255);
			// }
			return mplew.getPacket();
		}

		public static byte[] getTradeConfirmation() {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
			mplew.write(PlayerInteractionHandler.Interaction.CONFIRM_TRADE.action);

			return mplew.getPacket();
		}

		public static byte[] TradeMessage(byte UserSlot, byte message) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
			mplew.write(PlayerInteractionHandler.Interaction.EXIT.action);
			// mplew.write(25);//new v141
			mplew.write(UserSlot);
			mplew.write(message);

			return mplew.getPacket();
		}

		public static byte[] getTradeCancel(byte UserSlot, int unsuccessful) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
			mplew.write(PlayerInteractionHandler.Interaction.EXIT.action);
			mplew.write(UserSlot);
			mplew.write(7);// was2 

			return mplew.getPacket();
		}
	}

	public static class NPCPacket {

		public static byte[] spawnNPC(MapleNPC life, boolean show) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.SPAWN_NPC.getValue());
			mplew.writeInt(life.getObjectId());
			mplew.writeInt(life.getId());
			getNpcInit(mplew, life, show);
			
			return mplew.getPacket();
		}
		
		public static byte[] removeNPC(int objectid) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.REMOVE_NPC.getValue());
			mplew.writeInt(objectid);

			return mplew.getPacket();
		}
		
		public static byte[] spawnNPCRequestController(MapleNPC life, boolean show) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.SPAWN_NPC_REQUEST_CONTROLLER.getValue());
			mplew.write(1);
			mplew.writeInt(life.getObjectId());
			mplew.writeInt(life.getId());
			getNpcInit(mplew, life, show);

			return mplew.getPacket();
		}
		
		public static byte[] removeNPCController(int objectid) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.SPAWN_NPC_REQUEST_CONTROLLER.getValue());
			mplew.write(0);
			mplew.writeInt(objectid);

			return mplew.getPacket();
		}
		
		private static void getNpcInit(MaplePacketLittleEndianWriter mplew, MapleNPC life, boolean show) {
			mplew.writeShort(life.getPosition().x);
			mplew.writeShort(life.getCy());
			
			mplew.write(0); // bMove
			mplew.write(life.getF() == 1 ? 0 : 1); // nMoveAction
			mplew.writeShort(life.getFh());
			mplew.writeShort(life.getRx0());
			mplew.writeShort(life.getRx1());
			mplew.write(show ? 1 : 0); // bEnabled
			
			mplew.writeInt(0); // CNpc::SetPresentItem
			
			mplew.write(0); // nPresentTimeState
			mplew.writeInt(-1); // tPresent
			mplew.writeInt(0); // nNoticeBoardType
			
			/*
			 * if (nNoticeBoardType == 1)		
			 * 	mplew.writeInt(0); // nNoticeBoardType
			 *
			 */
			
			mplew.writeInt(0);
			mplew.writeInt(0);
			
			mplew.writeMapleAsciiString("");
			mplew.write(0);
		}

		public static byte[] getMapSelection(final int npcid, final String sel) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
			mplew.write(4);
			mplew.writeInt(npcid);
			mplew.writeShort(0x11);
			mplew.writeInt(npcid == 2083006 ? 1 : 0); // neo city
			mplew.writeInt(npcid == 9010022 ? 1 : 0); // dimensional
			mplew.writeMapleAsciiString(sel);

			return mplew.getPacket();
		}

		public static byte[] toggleNPCShow(int oid, boolean hide) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendPacketOpcode.NPC_TOGGLE_VISIBLE.getValue());
			mplew.writeInt(oid);
			mplew.write(hide ? 0 : 1);
			return mplew.getPacket();
		}

		public static byte[] setNPCSpecialAction(int oid, String action) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendPacketOpcode.NPC_SET_SPECIAL_ACTION.getValue());
			mplew.writeInt(oid);
			mplew.writeMapleAsciiString(action);
			mplew.writeInt(0); // unknown yet
			mplew.write(0); // unknown yet
			return mplew.getPacket();
		}

		public static byte[] NPCSpecialAction(int oid, int x, int y) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendPacketOpcode.NPC_UPDATE_LIMITED_INFO.getValue());
			mplew.writeInt(oid);
			mplew.writeInt(x);
			mplew.writeInt(y);
			return mplew.getPacket();
		}

		public static byte[] setNPCScriptable() {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendPacketOpcode.NPC_SCRIPTABLE.getValue());

			List<Pair<Integer, String>> npcs = new LinkedList();
			npcs.add(new Pair<>(9070006,
					"Why...why has this happened to me? My knightly honor... My knightly pride..."));
			npcs.add(new Pair<>(9000021, "Are you enjoying the event?"));

			mplew.write(npcs.size());
			for (Pair<Integer, String> s : npcs) {
				mplew.writeInt(s.getLeft());
				mplew.writeMapleAsciiString(s.getRight());
				mplew.writeInt(0);
				// mplew.writeInt(Integer.MAX_VALUE);
				mplew.write(0);
			}
			return mplew.getPacket();
		}
		
		/**
		 * 
		 * @param talk
		 * @return
		 */
		public static byte[] getNPCTalk(NPCTalk talk) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			
			mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
			mplew.write(talk.getType());
			mplew.writeInt(talk.getNpcID());
			mplew.write(0); // bool?
			// mplew.writeInt(0);
			
			mplew.write(talk.getMsg());
			mplew.write(talk.getParam());
			mplew.write(talk.getColor()); // 0 = blue; 1 = brown
			switch(talk.getMsg()) {
				case 0: // OnSay
					if ((talk.getParam() & 4) != 0)
						mplew.writeInt(talk.getNpcIDD());
					mplew.writeMapleAsciiString(talk.getText());
					mplew.write(talk.getPrev());
					mplew.write(talk.getNext());
					mplew.writeInt(talk.getSeconds());
					break;
				case 1: // OnSayImage
					mplew.write(talk.getArgs().length);
					for(Object obj : talk.getArgs()) {
						mplew.writeMapleAsciiString((String) obj);
					}
					break;
				case 2: // OnAskYesNo
					if((talk.getParam() & 4) != 0)
						mplew.writeInt(talk.getNpcIDD());
					mplew.writeMapleAsciiString(talk.getText());
					break;
				case 3: // OnAskText
					if((talk.getParam() & 4) != 0)
						mplew.writeInt(talk.getNpcIDD());
					mplew.writeMapleAsciiString(talk.getText());
					mplew.writeMapleAsciiString(talk.getDef());
					mplew.writeShort(talk.getMin());
					mplew.writeShort(talk.getMax());
					break;
				case 4: // OnAskNumber
					mplew.writeMapleAsciiString(talk.getText());
					mplew.writeInt(talk.getAmount());
					mplew.writeInt(talk.getMin());
					mplew.writeInt(talk.getMax());
					break;
				case 5: // OnAskMenu
					if ((talk.getParam() & 4) != 0)
						mplew.writeInt(talk.getNpcIDD());
					mplew.writeMapleAsciiString(talk.getText());
					break;
				case 6: // OnInitialQuiz
					mplew.write(0); // setting this to 1 will close the window.
					mplew.writeMapleAsciiString(talk.getText());
					mplew.writeMapleAsciiString(talk.getDef());
					mplew.writeMapleAsciiString(talk.getHint());
					mplew.writeInt(talk.getMin());
					mplew.writeInt(talk.getMax());
					mplew.writeInt(talk.getSeconds());
				case 7: // OnInitialSpeedQuiz
				case 8: // OnICQuiz
				case 9: // OnAskAvatar
					mplew.write(0); // bAngelicBuster
					mplew.write(0); // bZeroBeta
					mplew.writeMapleAsciiString(talk.getText());
					mplew.write(talk.getArgs().length);
					for(Object i : talk.getArgs()) {
						mplew.writeInt((int) i);
					}
					break;
				case 10: // OnAskAndroid
				case 12: // OnAskPet
				case 13: // OnAskPetAll
				case 14: // OnAskActionPetEvolution
				case 16: // OnAskYesNo
					if((talk.getParam() & 4) != 0)
						mplew.writeInt(talk.getNpcIDD());
					mplew.writeMapleAsciiString(talk.getText());
					break;
				case 17: // OnAskBoxText
				case 18: // OnAskSlideMenu
				case 22: // OnAskAvatar
				case 23: // OnAskSelectMenu
				case 24: // OnAskAngelicBuster
					break;
				case 25: // OnSayIllustration
				case 26: // OnSayIllustration
				case 27: // OnAskYesNoIllustration
				case 28: // OnAskYesNoIllustration
				case 30: // OnAskMenuIllustration
				case 31: // OnAskYesNoIllustration
				case 32: // OnAskMenuIllustration
				case 34: // OnAskAvatarZero
				case 38: // OnAskWeaponBox
				case 39: // OnAskBoxText_BgImg
					mplew.writeShort(0); // background index
					mplew.writeMapleAsciiString("");
					mplew.writeMapleAsciiString("");
					mplew.writeShort(0); // column
					mplew.writeShort(0); // line
					mplew.writeShort(0); // font size
					mplew.writeShort(0); // top font margin
					break;
				case 40: // OnAskUserSurvey
					mplew.writeInt(0); // talk type
					mplew.write(1); // show exit button
					mplew.writeMapleAsciiString(talk.getText());
					break;
				case 42: // OnAskMixHair
				case 43: // OnAskMixHairExZero
				case 44: // OnAskCustomMixHair
				case 45: // OnAskCustomMixHairAndProb
					mplew.write(0); // bAngelicBuster
					mplew.writeInt(0); // left percentage
					mplew.writeInt(0); // right percentage
					mplew.writeMapleAsciiString(talk.getText());
					break;
				case 46: // OnAskMixHairNew
				case 47: // OnAskMixHairNewExZero
				case 49: // OnAskScreenShinningStarMsg
					break;
				case 51: // OnAskNumberUseKeyPad
					mplew.writeInt(0); // result
					break;
				case 52: // OnSpinOffGuitarRhythmGame
				case 53: // OnGhostParkEnter
					mplew.writeInt(0); // size
					break;
				default: throw new UnsupportedOperationException("This message id has not been implemented.");
			}
			
			return mplew.getPacket();
		}

		public static byte[] getEnglishQuiz(int npc, byte type, int diffNPC, String talk, String endBytes) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
			mplew.write(4);
			mplew.writeInt(npc);
			mplew.write(10); // not sure
			mplew.write(type);
			if ((type & 0x4) != 0) {
				mplew.writeInt(diffNPC);
			}
			mplew.writeMapleAsciiString(talk);
			mplew.write(HexTool.getByteArrayFromHexString(endBytes));

			return mplew.getPacket();
		}

		public static byte[] getSlideMenu(int npcid, int type, int lasticon, String sel) {
			// Types: 0 - map selection 1 - neo city map selection 2 - korean
			// map selection 3 - tele rock map selection 4 - dojo buff selection
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
			mplew.write(4); // slide menu
			mplew.writeInt(npcid);
			mplew.write(0);
			mplew.write(18);
			mplew.write(0);
			mplew.write(0);
			
			mplew.writeInt(type); // menu type
			mplew.writeInt(type == 0 ? lasticon : 0); // last icon on menu
			mplew.writeMapleAsciiString(sel);

			return mplew.getPacket();
		}

		public static byte[] getSelfTalkText(String text) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
			mplew.write(3); // nSpeakerTypeID
			mplew.writeInt(0); //nSpeakerTemplateID
			mplew.write(1); 
                        mplew.writeInt(0);
			mplew.write(0);//nMsgType
			mplew.write(0x11); //bParam (0x11 is NO_ESC [0x1] and SMP_NPC_REPLACED_BY_USER_LEFT [0x10])
			mplew.write(0); //eColor
			mplew.writeMapleAsciiString(text);
			mplew.write(0);//bPrev
			mplew.write(1); //bNext
                        mplew.writeInt(0); //tWait
			return mplew.getPacket();
		}

		public static byte[] getNPCTutoEffect(String effect) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
			mplew.write(3);
			mplew.writeInt(0);
			mplew.write(0);
			mplew.write(1);
			mplew.write(257);
			mplew.write(0);
			mplew.writeMapleAsciiString(effect);
			return mplew.getPacket();
		}

		public static byte[] getCutSceneSkip() {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
			mplew.write(3);
			mplew.writeInt(0);
			mplew.write(1);
			mplew.writeInt(0);
			
			mplew.write(2);
			mplew.write(5);
			mplew.writeInt(9010000); // Maple administrator
			mplew.writeMapleAsciiString("Would you like to skip the tutorial cutscenes?");
			return mplew.getPacket();
		}

		public static byte[] getDemonSelection() {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
			mplew.write(3);
			mplew.writeInt(0);
			
			mplew.write(1);			
			mplew.writeInt(2159311); // npc
			
			mplew.write(0x17);
			mplew.write(1);
			mplew.write(1);
			mplew.write(0);
			
			mplew.write0(8);
			return mplew.getPacket();
		}

		public static byte[] getEvanTutorial(String data) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());

			mplew.write(8);
			mplew.writeInt(0);
			mplew.write(1);
			mplew.write(1);
			mplew.write(1);
			mplew.writeMapleAsciiString(data);

			return mplew.getPacket();
		}

		public static byte[] getNPCShop(int sid, MapleShop shop, MapleClient c) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.OPEN_NPC_SHOP.getValue());
			
			mplew.write(0);
			/*
			 * if ( CInPacket::Decode1(iPacket) )
      		 * 	v62 = CInPacket::Decode4(v68);
			 */
			mplew.writeInt(0); // m_nSelectNpcItemID
			mplew.writeInt(shop.getNpcId()); // m_dwNpcTemplateID
			mplew.writeInt(0); // m_nStarCoin
			mplew.writeInt(0); // m_nShopVerNo
			PacketHelper.addShopInfo(mplew, shop, c);

			return mplew.getPacket();
		}

		public static byte[] confirmShopTransaction(byte code, MapleShop shop, MapleClient c, int indexBought) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.CONFIRM_SHOP_TRANSACTION.getValue());
			mplew.write(code);
			if (code == 5) {
				mplew.writeInt(0); // m_nSelectNpcItemID
				mplew.writeInt(shop.getNpcId()); // m_dwNpcTemplateID
				mplew.writeInt(0); // m_nStarCoin
				mplew.writeInt(0); // m_nShopVerNo
				PacketHelper.addShopInfo(mplew, shop, c);
			} else {
				mplew.write(indexBought >= 0 ? 1 : 0);
				if (indexBought >= 0) {
					mplew.writeInt(indexBought);
				} else {
					mplew.write(0);
					mplew.writeInt(0);
				}
			}

			return mplew.getPacket();
		}

		public static byte[] getStorage(int npcId, byte slots, Collection<Item> items, long meso) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
			mplew.write(22);
			mplew.writeInt(npcId);
			mplew.write(slots);
			mplew.writeShort(126);
			mplew.writeShort(0);
			mplew.writeInt(0);
			mplew.writeLong(meso);
			mplew.writeShort(0);
			mplew.write((byte) items.size());
			for (Item item : items) {
				PacketHelper.addItemInfo(mplew, item);
			}
			mplew.write0(2);// 4

			return mplew.getPacket();
		}

		public static byte[] getStorageFull() {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
			mplew.write(17);

			return mplew.getPacket();
		}

		public static byte[] mesoStorage(byte slots, long meso) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
			mplew.write(19);
			mplew.write(slots);
			mplew.writeShort(2);
			mplew.writeShort(0);
			mplew.writeInt(0);
			mplew.writeLong(meso);

			return mplew.getPacket();
		}

		public static byte[] arrangeStorage(byte slots, Collection<Item> items, boolean changed) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
			mplew.write(15);
			mplew.write(slots);
			mplew.write(124);
			mplew.write0(10);
			mplew.write(items.size());
			for (Item item : items) {
				PacketHelper.addItemInfo(mplew, item);
			}
			mplew.write(0);
			return mplew.getPacket();
		}

		public static byte[] storeStorage(byte slots, MapleInventoryType type, Collection<Item> items) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
			mplew.write(13);
			mplew.write(slots);
			mplew.writeShort(type.getBitfieldEncoding());
			mplew.writeShort(0);
			mplew.writeInt(0);
			mplew.write(items.size());
			for (Item item : items) {
				PacketHelper.addItemInfo(mplew, item);
			}
			return mplew.getPacket();
		}

		public static byte[] takeOutStorage(byte slots, MapleInventoryType type, Collection<Item> items) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
			mplew.write(9);
			mplew.write(slots);
			mplew.writeShort(type.getBitfieldEncoding());
			mplew.writeShort(0);
			mplew.writeInt(0);
			mplew.write(items.size());
			for (Item item : items) {
				PacketHelper.addItemInfo(mplew, item);
			}
			return mplew.getPacket();
		}
	}

	public static class SummonPacket {

		public static byte[] spawnSummon(MapleSummon summon, boolean animated) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.SPAWN_SUMMON.getValue());
			mplew.writeInt(summon.getOwnerId());
			mplew.writeInt(summon.getObjectId());
			mplew.writeInt(summon.getSkill()); // nSkillID
			mplew.write(summon.getOwnerLevel() - 1); // nCharLevel
			mplew.write(summon.getSkillLevel()); // nSLV (skill level)
			mplew.writePos(summon.getPosition()); // nX, nY
			mplew.write((summon.getSkill() == 32111006) || (summon.getSkill() == 33101005) ? 5 : 4); // nMoveAction
			
			if ((summon.getSkill() == 35121003) && (summon.getOwner().getMap() != null)) { // Giant Robot SG-88
				mplew.writeShort(summon.getOwner().getMap().getFootholds().findBelow(summon.getPosition()).getId());
			} else {
				mplew.writeShort(0); // nCurFoothold
			}
			
			mplew.write(summon.getMovementType().getValue()); // nMoveAbility
			mplew.write(summon.getSummonType()); // nAssistType
			mplew.write(animated ? 1 : 0); // nEnterType
			mplew.writeInt(0); // dwMobID
			mplew.write(1); // bFlyMob
			mplew.write(0); // bBeforeFirstAttack
			mplew.writeInt(0); // nLookID
			mplew.writeInt(0); // nBulletID
			
			boolean mirroredTarget = summon.getSkill() == 4341006 && summon.getOwner() != null;
			mplew.write(mirroredTarget);
			if (mirroredTarget) {
				PacketHelper.addCharLook(mplew, summon.getOwner(), true, false);
			} else if (summon.getSkill() == 35111002) { // Rock 'n Shock
				mplew.write(0); // boolean for TeslaCoilState
			} else if (summon.getSkill() == 42111003) { // Kishin Shoukan
				mplew.writeShort(0);
				mplew.writeShort(0);
				mplew.writeShort(0);
				mplew.writeShort(0);
			}
			
			mplew.write(0); // bJaguarActive
			mplew.writeInt(0); // tSummonTerm
			mplew.write(0); // bAttackActive

			return mplew.getPacket();
		}

		public static byte[] removeSummon(int ownerId, int objId) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.REMOVE_SUMMON.getValue());
			mplew.writeInt(ownerId);
			
			mplew.writeInt(objId); // dwSummonedID
			mplew.write(10); // nLeaveType

			return mplew.getPacket();
		}

		public static byte[] removeSummon(MapleSummon summon, boolean animated) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.REMOVE_SUMMON.getValue());
			mplew.writeInt(summon.getOwnerId());
			mplew.writeInt(summon.getObjectId());
			if (animated) {
				switch (summon.getSkill()) {
				case 35121003:
					mplew.write(10);
					break;
				case 33101008:
				case 35111001:
				case 35111002:
				case 35111005:
				case 35111009:
				case 35111010:
				case 35111011:
				case 35121009:
				case 35121010:
				case 35121011:
					mplew.write(5);
					break;
				default:
					mplew.write(4);
					break;
				}
			} else {
				mplew.write(1);
			}

			return mplew.getPacket();
		}
		
		public static byte[] moveSummon(int cid, int oid, Point startPos, List<LifeMovementFragment> moves) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.MOVE_SUMMON.getValue());
			mplew.writeInt(cid);
			mplew.writeInt(oid);
			mplew.writeInt(0);
			mplew.writePos(startPos);
			mplew.writeInt(0);
			PacketHelper.serializeMovementList(mplew, moves);

			return mplew.getPacket();
		}

		public static byte[] summonAttack(int cid, int summonSkillId, byte animation,
				List<Pair<Integer, Integer>> allDamage, int level, boolean darkFlare) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.SUMMON_ATTACK.getValue());
			mplew.writeInt(cid);
			
			mplew.writeInt(summonSkillId); // pSummoned
			mplew.write(level - 1); // nCharLevel
			mplew.write(animation); // bLeft
			mplew.write(allDamage.size()); // nMobCount
			for (Pair attackEntry : allDamage) {
				mplew.writeInt(((Integer) attackEntry.left).intValue());
				mplew.write(7); // nAttackCount
				mplew.writeInt(((Integer) attackEntry.right).intValue());
			}
			mplew.write(darkFlare ? 1 : 0); // bCounterAttack
			mplew.write(0); // bNoAction
			mplew.writeShort(0); // pMob
			mplew.writeShort(0); // (tCur + this) delay per attack?

			return mplew.getPacket();
		}

		public static byte[] pvpSummonAttack(int cid, int playerLevel, int oid, int animation, Point pos,
				List<AttackPair> attack) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.PVP_SUMMON.getValue());
			mplew.writeInt(cid);
			mplew.writeInt(oid);
			mplew.write(playerLevel);
			mplew.write(animation);
			mplew.writePos(pos);
			mplew.writeInt(0);
			mplew.write(attack.size());
			for (AttackPair p : attack) {
				mplew.writeInt(p.objectid);
				mplew.writePos(p.point);
				mplew.write(p.attack.size());
				mplew.write(0);
				for (Pair atk : p.attack) {
					mplew.writeInt(((Integer) atk.left).intValue());
				}
			}

			return mplew.getPacket();
		}

		public static byte[] summonSkill(int cid, int summonSkillId, int newStance) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.SUMMON_SKILL.getValue());
			mplew.writeInt(cid);
			mplew.writeInt(summonSkillId);
			mplew.write(newStance);

			return mplew.getPacket();
		}

		public static byte[] damageSummon(int cid, int summonSkillId, int damage, int unkByte, int monsterIdFrom) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.DAMAGE_SUMMON.getValue());
			mplew.writeInt(cid);
			
			mplew.writeInt(summonSkillId);
			mplew.write(unkByte); // nAttackIdx
			mplew.writeInt(damage); // nDamage
			mplew.writeInt(monsterIdFrom); // dwMobTemplateID
			mplew.write(0); // bLeft

			return mplew.getPacket();
		}
	}

	public static class UIPacket {

		public static byte[] getDirectionStatus(boolean enable) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.DIRECTION_STATUS.getValue());
			mplew.write(enable ? 1 : 0);

			return mplew.getPacket();
		}

		public static byte[] openUI(int type) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);

			mplew.writeShort(SendPacketOpcode.OPEN_UI.getValue());
			mplew.writeInt(type); // 175.1

			return mplew.getPacket();
		}

		public static byte[] sendRepairWindow(int npc) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(10);

			mplew.writeShort(SendPacketOpcode.OPEN_UI_OPTION.getValue());
			mplew.writeInt(33);
			mplew.writeInt(npc);
			mplew.writeInt(0);// new143

			return mplew.getPacket();
		}

		public static byte[] sendJewelCraftWindow(int npc) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(10);

			mplew.writeShort(SendPacketOpcode.OPEN_UI_OPTION.getValue());
			mplew.writeInt(104);
			mplew.writeInt(npc);
			mplew.writeInt(0);// new143

			return mplew.getPacket();
		}

		public static byte[] startAzwan(int npc) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(10);
			mplew.writeShort(SendPacketOpcode.OPEN_UI_OPTION.getValue());
			mplew.writeInt(70);
			mplew.writeInt(npc);
			mplew.writeInt(0);// new143
			return mplew.getPacket();
		}

		public static byte[] openUIOption(int type, int npc) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(10);
			mplew.writeShort(SendPacketOpcode.OPEN_UI_OPTION.getValue());
			mplew.writeInt(type);
			mplew.writeInt(npc);
			return mplew.getPacket();
		}

		public static byte[] sendDojoResult(int points) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.OPEN_UI_OPTION.getValue());
			mplew.writeInt(0x48);
			mplew.writeInt(points);

			return mplew.getPacket();
		}

		public static byte[] sendAzwanResult() {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.OPEN_UI_OPTION.getValue());
			mplew.writeInt(0x45);
			mplew.writeInt(0);

			return mplew.getPacket();
		}

		public static byte[] DublStart(boolean dark) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
			mplew.write(0x28);
			mplew.write(dark ? 1 : 0);

			return mplew.getPacket();
		}

		public static byte[] DublStartAutoMove() {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendPacketOpcode.MOVE_SCREEN.getValue());
			mplew.write(3);
			mplew.writeInt(2);

			return mplew.getPacket();
		}

		public static byte[] IntroLock(boolean enable) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.INTRO_LOCK.getValue());
			mplew.write(enable ? 1 : 0);
			mplew.writeInt(0);

			return mplew.getPacket();
		}

		public static byte[] IntroEnableUI(int enable) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.INTRO_ENABLE_UI.getValue());
			mplew.write(enable > 0 ? 1 : 0);
			if (enable > 0) {
				mplew.write(enable);
				mplew.writeShort(0);
			}
			mplew.write(0);
			return mplew.getPacket();
		}

		public static byte[] IntroDisableUI(boolean enable) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.INTRO_DISABLE_UI.getValue());
			mplew.write(enable ? 1 : 0);

			return mplew.getPacket();
		}

		public static byte[] summonHelper(boolean summon) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.SUMMON_HINT.getValue());
			mplew.write(summon ? 1 : 0);

			return mplew.getPacket();
		}

		public static byte[] summonMessage(int type) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.SUMMON_HINT_MSG.getValue());
			mplew.write(1);
			mplew.writeInt(type);
			mplew.writeInt(7000);

			return mplew.getPacket();
		}

		public static byte[] summonMessage(String message) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.SUMMON_HINT_MSG.getValue());
			mplew.write(0);
			mplew.writeMapleAsciiString(message);
			mplew.writeInt(200);
			mplew.writeShort(0);
			mplew.writeInt(10000);

			return mplew.getPacket();
		}

		public static byte[] getDirectionInfo(int type, int value) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendPacketOpcode.DIRECTION_INFO.getValue());
			mplew.write((byte) type);
			mplew.writeInt(value);
			return mplew.getPacket();
		}

		public static byte[] getDirectionInfo(String data, int value, int x, int y, int a, int b) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.DIRECTION_INFO.getValue());
			mplew.write(2);
			mplew.writeMapleAsciiString(data);
			mplew.writeInt(value);
			mplew.writeInt(x);
			mplew.writeInt(y);
			mplew.write(a);
			if (a > 0) {
				mplew.writeInt(0);
			}
			mplew.write(b);
			if (b > 1) {
				mplew.writeInt(0);
				mplew.write(a);
				mplew.write(b);
			}

			return mplew.getPacket();
		}

		public static byte[] getDirectionEffect(String data, int value, int x, int y) {
			return getDirectionEffect(data, value, x, y, 0);
		}

		public static byte[] getDirectionEffect(String data, int value, int x, int y, int npc) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.DIRECTION_INFO.getValue());
			mplew.write(2);
			mplew.writeMapleAsciiString(data);
			mplew.writeInt(value);
			mplew.writeInt(x);
			mplew.writeInt(y);
			mplew.write(1);
			mplew.writeInt(0);
			mplew.write(1);
			mplew.writeInt(npc);
			mplew.write(1);
			mplew.write(0);

			return mplew.getPacket();
		}

		public static byte[] getDirectionInfoNew(byte x, int value) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.DIRECTION_INFO.getValue());
			mplew.write(5);
			mplew.write(x);
			mplew.writeInt(value);
			if (x == 0) {
				mplew.writeInt(value);
				mplew.writeInt(value);
			}

			return mplew.getPacket();
		}

		public static byte[] moveScreen(int x) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.MOVE_SCREEN_X.getValue());
			mplew.writeInt(x);
			mplew.writeInt(0);
			mplew.writeInt(0);

			return mplew.getPacket();
		}

		public static byte[] screenDown() {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.MOVE_SCREEN_DOWN.getValue());

			return mplew.getPacket();
		}

		public static byte[] reissueMedal(int itemId, int type) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.REISSUE_MEDAL.getValue());
			mplew.write(type);
			mplew.writeInt(itemId);

			return mplew.getPacket();
		}

		public static byte[] playMovie(String data, boolean show) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.PLAY_MOVIE.getValue());
			mplew.writeMapleAsciiString(data);
			mplew.write(show ? 1 : 0);

			return mplew.getPacket();
		}

		public static byte[] setRedLeafStatus(int joejoe, int hermoninny, int littledragon, int ika) {
			// packet made to set status
			// should remove it and make a handler for it, it's a recv opcode
			/*
			 * slea: E2 9F 72 00 5D 0A 73 01 E2 9F 72 00 04 00 00 00 00 00 00 00
			 * 75 96 8F 00 55 01 00 00 76 96 8F 00 00 00 00 00 77 96 8F 00 00 00
			 * 00 00 78 96 8F 00 00 00 00 00
			 */
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			// mplew.writeShort();
			mplew.writeInt(7512034); // no idea
			mplew.writeInt(24316509); // no idea
			mplew.writeInt(7512034); // no idea
			mplew.writeInt(4); // no idea
			mplew.writeInt(0); // no idea
			mplew.writeInt(9410165); // joe joe
			mplew.writeInt(joejoe); // amount points added
			mplew.writeInt(9410166); // hermoninny
			mplew.writeInt(hermoninny); // amount points added
			mplew.writeInt(9410167); // little dragon
			mplew.writeInt(littledragon); // amount points added
			mplew.writeInt(9410168); // ika
			mplew.writeInt(ika); // amount points added

			return mplew.getPacket();
		}

		public static byte[] sendRedLeaf(int points, boolean viewonly) {
			/*
			 * slea: 73 00 00 00 0A 00 00 00 01
			 */
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(10);

			mplew.writeShort(SendPacketOpcode.OPEN_UI_OPTION.getValue());
			mplew.writeInt(0x73);
			mplew.writeInt(points);
			mplew.write(viewonly ? 1 : 0); // if view only, then complete button
											// is disabled

			return mplew.getPacket();
		}
	}

	public static class EffectPacket {

		public static byte[] showForeignEffect(int effect) {
			return showForeignEffect(-1, effect);
		}

		public static byte[] showForeignEffect(int cid, int effect) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			if (cid == -1) {
				mplew.writeShort(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
			} else {
				mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
				mplew.writeInt(cid);
			}
			mplew.write(effect);

			return mplew.getPacket();
		}

		public static byte[] showItemLevelupEffect() {
			return showForeignEffect(18);
		}

		public static byte[] showForeignItemLevelupEffect(int cid) {
			return showForeignEffect(cid, 18);
		}

		public static byte[] showOwnDiceEffect(int skillid, int effectid, int effectid2, int level) {
			return showDiceEffect(-1, skillid, effectid, effectid2, level);
		}

		public static byte[] showDiceEffect(int cid, int skillid, int effectid, int effectid2, int level) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			if (cid == -1) {
				mplew.writeShort(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
			} else {
				mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
				mplew.writeInt(cid);
			}
			mplew.write(3);
			mplew.writeInt(effectid);
			mplew.writeInt(effectid2);
			mplew.writeInt(skillid);
			mplew.write(level);
			mplew.write(0);
			mplew.write0(100);

			return mplew.getPacket();
		}

		public static byte[] useCharm(byte charmsleft, byte daysleft, boolean safetyCharm) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
			mplew.write(8);
			mplew.write(safetyCharm ? 1 : 0);
			mplew.write(charmsleft);
			mplew.write(daysleft);
			if (!safetyCharm) {
				mplew.writeInt(0);
			}

			return mplew.getPacket();
		}

		public static byte[] Mulung_DojoUp2() {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
			mplew.write(10);

			return mplew.getPacket();
		}

		public static byte[] showOwnHpHealed(int amount) {
			return showHpHealed(-1, amount);
		}

		public static byte[] showHpHealed(int cid, int amount) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			if (cid == -1) {
				mplew.writeShort(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
			} else {
				mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
				mplew.writeInt(cid);
			}
			mplew.write(30);
			mplew.writeInt(amount);

			return mplew.getPacket();
		}

		public static byte[] showRewardItemAnimation(int itemId, String effect) {
			return showRewardItemAnimation(itemId, effect, -1);
		}

		public static byte[] showRewardItemAnimation(int itemId, String effect, int from_playerid) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			if (from_playerid == -1) {
				mplew.writeShort(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
			} else {
				mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
				mplew.writeInt(from_playerid);
			}
			mplew.write(17);
			mplew.writeInt(itemId);
			mplew.write((effect != null) && (effect.length() > 0) ? 1 : 0);
			if ((effect != null) && (effect.length() > 0)) {
				mplew.writeMapleAsciiString(effect);
			}

			return mplew.getPacket();
		}

		public static byte[] showCashItemEffect(int itemId) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
			mplew.write(23);
			mplew.writeInt(itemId);

			return mplew.getPacket();
		}

		public static byte[] ItemMaker_Success() {
			return ItemMaker_Success_3rdParty(-1);
		}

		public static byte[] ItemMaker_Success_3rdParty(int from_playerid) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			if (from_playerid == -1) {
				mplew.writeShort(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
			} else {
				mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
				mplew.writeInt(from_playerid);
			}
			mplew.write(19);
			mplew.writeInt(0);

			return mplew.getPacket();
		}

		public static byte[] useWheel(byte charmsleft) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
			mplew.write(24);
			mplew.write(charmsleft);

			return mplew.getPacket();
		}

		public static byte[] showOwnBuffEffect(int skillid, int effectid, int playerLevel, int skillLevel) {
			return showBuffeffect(-1, skillid, effectid, playerLevel, skillLevel, (byte) 3);
		}

		public static byte[] showOwnBuffEffect(int skillid, int effectid, int playerLevel, int skillLevel,
				byte direction) {
			return showBuffeffect(-1, skillid, effectid, playerLevel, skillLevel, direction);
		}

		public static byte[] showBuffeffect(int cid, int skillid, int effectid, int playerLevel, int skillLevel) {
			return showBuffeffect(cid, skillid, effectid, playerLevel, skillLevel, (byte) 3);
		}

		public static byte[] showBuffeffect(int cid, int skillid, int effectid, int playerLevel, int skillLevel,
				byte direction) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			if (cid == -1) {
				mplew.writeShort(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
			} else {
				mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
				mplew.writeInt(cid);
			}
			mplew.write(effectid);
			mplew.writeInt(skillid);
			mplew.write(playerLevel - 1);
			if ((effectid == 2) && (skillid == 31111003)) {
				mplew.writeInt(0);
			}
			mplew.write(skillLevel);
			if ((direction != 3) || (skillid == 1320006) || (skillid == 30001062) || (skillid == 30001061)) {
				mplew.write(direction);
			}

			if (skillid == 30001062) {
				mplew.writeInt(0);
			}
			mplew.write0(10);

			return mplew.getPacket();
		}

		public static byte[] ShowWZEffect(String data) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
			mplew.write(0x26); // updated.
			mplew.writeMapleAsciiString(data);
			mplew.write(0); // bool
			mplew.writeInt(0); // bUpgrade
			mplew.writeInt(4); // nRet

			return mplew.getPacket();
		}

		public static byte[] showOwnCraftingEffect(String effect, byte direction, int time, int mode) {
			return showCraftingEffect(-1, effect, direction, time, mode);
		}

		public static byte[] showCraftingEffect(int cid, String effect, byte direction, int time, int mode) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			if (cid == -1) {
				mplew.writeShort(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
			} else {
				mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
				mplew.writeInt(cid);
			}
			mplew.write(34); // v140
			mplew.writeMapleAsciiString(effect);
			mplew.write(direction);
			mplew.writeInt(time);
			mplew.writeInt(mode);
			if (mode == 2) {
				mplew.writeInt(0);
			}

			return mplew.getPacket();
		}

		public static byte[] TutInstructionalBalloon(String data) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
			mplew.write(25);// was 26 in v140
			mplew.writeMapleAsciiString(data);
			mplew.writeInt(1);

			return mplew.getPacket();
		}

		public static byte[] showOwnPetLevelUp(byte index) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
			mplew.write(6);
			mplew.write(0);
			mplew.write(index);

			return mplew.getPacket();
		}

		public static byte[] showOwnChampionEffect() {
			return showChampionEffect(-1);
		}

		public static byte[] showChampionEffect(int from_playerid) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			if (from_playerid == -1) {
				mplew.writeShort(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
			} else {
				mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
				mplew.writeInt(from_playerid);
			}
			mplew.write(34);
			mplew.writeInt(30000);

			return mplew.getPacket();
		}

		public static byte[] updateDeathCount(int deathCount) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.DEATH_COUNT.getValue());
			mplew.writeInt(deathCount);

			return mplew.getPacket();
		}
	}

	public static byte[] showWeirdEffect(String effect, int itemId) {
		final tools.data.output.MaplePacketLittleEndianWriter mplew = new tools.data.output.MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
		mplew.write(0x20);
		mplew.writeMapleAsciiString(effect);
		mplew.write(1);
		mplew.writeInt(0);// weird high number is it will keep showing it lol
		mplew.writeInt(2);
		mplew.writeInt(itemId);
		return mplew.getPacket();
	}

	public static byte[] showWeirdEffect(int chrId, String effect, int itemId) {
		final tools.data.output.MaplePacketLittleEndianWriter mplew = new tools.data.output.MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
		mplew.writeInt(chrId);
		mplew.write(0x20);
		mplew.writeMapleAsciiString(effect);
		mplew.write(1);
		mplew.writeInt(0);// weird high number is it will keep showing it lol
		mplew.writeInt(2);// this makes it read the itemId
		mplew.writeInt(itemId);
		return mplew.getPacket();
	}

	public static byte[] enchantResult(int result) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.STRENGTHEN_UI.getValue());
		mplew.writeInt(result);// 0=fail/1=sucess/2=idk/3=shows stats
		return mplew.getPacket();
	}

	public static byte[] sendSealedBox(short slot, int itemId, List<Integer> items) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SEALED_BOX.getValue());
		mplew.writeShort(slot);
		mplew.writeInt(itemId);
		mplew.writeInt(items.size());
		for (int item : items) {
			mplew.writeInt(item);
		}

		return mplew.getPacket();
	}

	public static byte[] unsealBox(int reward) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
		mplew.write(0x31);
		mplew.write(1);
		mplew.writeInt(reward);
		mplew.writeInt(1);

		return mplew.getPacket();
	}
}
