package tools.packet;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import client.MapleBuffStat;
import client.MapleBuffStatValueHolder;
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
import tools.data.PacketWriter;
import tools.packet.twostate.TSIndex;

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
		PacketWriter pw = new PacketWriter();
		pw.writeShort(SendPacketOpcode.SERVER_IP.getValue());
		pw.write(0); // request
		pw.write(0); 
		
		byte[] svr = new byte[] {8, 31, 99, ((byte) 141)};
		byte[] chat = new byte[] {8, 31, 99, ((byte) 133)};
		
		// maple server ip
		pw.write(svr);
		pw.writeShort(port);
		
		// chat server ip 
		pw.write(new byte[4]);
		pw.writeShort(0);
		
		pw.writeInt(characterid);
		
		pw.write(0);
		
		// argument ?
		pw.writeInt(0);
		pw.write(0);
		
		 // shutdown ? (timestamp)
		pw.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
		
		// pw.write(HexTool.getByteArrayFromHexString("3F 01 00 00 00 C8 00 00"));
		// pw.write(0);
		
		return pw.getPacket();
	}

	public static byte[] getChannelChange(MapleClient c, int port) {
		PacketWriter pw = new PacketWriter();
		byte[] svr = new byte[] {8, 31, 99, ((byte) 141)};
		
		pw.writeShort(SendPacketOpcode.CHANGE_CHANNEL.getValue());
		pw.write(1);
		pw.write(svr);
		pw.writeShort(port);
		pw.writeInt(0);
		return pw.getPacket();
	}

	public static byte[] getPVPType(int type, List<Pair<Integer, String>> players1, int team, boolean enabled,
			int lvl) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.ENTER_PVP.getValue());
		pw.write(type);
		pw.write(lvl);
		pw.write(enabled ? 1 : 0);
		pw.write(0);
		if (type > 0) {
			pw.write(team);
			pw.writeInt(players1.size());
			for (Pair pl : players1) {
				pw.writeInt(((Integer) pl.left).intValue());
				pw.writeMapleAsciiString((String) pl.right);
				pw.writeShort(2660);
			}
		}

		return pw.getPacket();
	}

	public static byte[] getPVPTransform(int type) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.CHANGE_TEAM.getValue());
		pw.write(type);

		return pw.getPacket();
	}

	public static byte[] getPVPDetails(List<Pair<Integer, Integer>> players) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.CHANGE_MODE.getValue());
		pw.write(1);
		pw.write(0);
		pw.writeInt(players.size());
		for (Pair pl : players) {
			pw.writeInt(((Integer) pl.left).intValue());
			pw.write(((Integer) pl.right).intValue());
		}

		return pw.getPacket();
	}

	public static byte[] enablePVP(boolean enabled) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.CHANGE_STATE.getValue());
		pw.write(enabled ? 1 : 2);

		return pw.getPacket();
	}

	public static byte[] getPVPScore(int score, boolean kill) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.UPDATE_COUNT.getValue());
		pw.writeInt(score);
		pw.write(kill ? 1 : 0);

		return pw.getPacket();
	}

	public static byte[] getPVPResult(List<Pair<Integer, MapleCharacter>> flags, int exp, int winningTeam,
			int playerTeam) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.SHOW_MODE_RESULT.getValue());
		pw.writeInt(flags.size());
		for (Pair f : flags) {
			pw.writeInt(((MapleCharacter) f.right).getId());
			pw.writeMapleAsciiString(((MapleCharacter) f.right).getName());
			pw.writeInt(((Integer) f.left).intValue());
			pw.writeShort(((MapleCharacter) f.right).getTeam() + 1); // byte, byte
			pw.writeInt(0);
			pw.writeInt(0);
		}
		pw.write(new byte[24]);
		pw.writeInt(exp);
		pw.write(0);
		pw.writeShort(100);
		pw.writeInt(0);
		pw.writeInt(0);
		pw.write(winningTeam);
		pw.write(playerTeam);

		return pw.getPacket();
	}

	public static byte[] getPVPTeam(List<Pair<Integer, String>> players) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.UPDATE_TEAM_INFO.getValue());
		pw.writeInt(players.size());
		for (Pair pl : players) {
			pw.writeInt(((Integer) pl.left).intValue());
			pw.writeMapleAsciiString((String) pl.right);
			pw.writeShort(2660); // byte, byte
		}

		return pw.getPacket();
	}

	public static byte[] getPVPScoreboard(List<Pair<Integer, MapleCharacter>> flags, int type) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.UPDATE_RANK_INFO.getValue());
		pw.writeShort(flags.size());
		for (Pair f : flags) {
			pw.writeInt(((MapleCharacter) f.right).getId());
			pw.writeMapleAsciiString(((MapleCharacter) f.right).getName());
			pw.writeInt(((Integer) f.left).intValue());
			pw.write(type == 0 ? 0 : ((MapleCharacter) f.right).getTeam() + 1);
		}

		return pw.getPacket();
	}

	public static byte[] getPVPPoints(int p1, int p2) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.UPDATE_TEAM_SCORE.getValue());
		pw.writeInt(p1);
		pw.writeInt(p2);

		return pw.getPacket();
	}

	public static byte[] getPVPKilled(String lastWords) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.REVIVE_MESSAGE.getValue());
		pw.writeMapleAsciiString(lastWords);

		return pw.getPacket();
	}

	public static byte[] getPVPMode(int mode) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.SCREEN_EFFECT.getValue());
		pw.write(mode);

		return pw.getPacket();
	}

	public static byte[] getPVPIceHPBar(int hp, int maxHp) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.ICEKNIGHT_HP_CHANGE.getValue());
		pw.writeInt(hp);
		pw.writeInt(maxHp);

		return pw.getPacket();
	}

	public static byte[] getCaptureFlags(MapleMap map) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.CAPTURE_FLAGS.getValue());
		pw.writeRect(map.getArea(0));
		pw.writeInt(((Point) ((Pair) map.getGuardians().get(0)).left).x);
		pw.writeInt(((Point) ((Pair) map.getGuardians().get(0)).left).y);
		pw.writeRect(map.getArea(1));
		pw.writeInt(((Point) ((Pair) map.getGuardians().get(1)).left).x);
		pw.writeInt(((Point) ((Pair) map.getGuardians().get(1)).left).y);

		return pw.getPacket();
	}

	public static byte[] getCapturePosition(MapleMap map) {
		PacketWriter pw = new PacketWriter();

		Point p1 = map.getPointOfItem(2910000);
		Point p2 = map.getPointOfItem(2910001);
		pw.writeShort(SendPacketOpcode.CAPTURE_POSITION.getValue());
		pw.write(p1 == null ? 0 : 1);
		if (p1 != null) {
			pw.writeInt(p1.x);
			pw.writeInt(p1.y);
		}
		pw.write(p2 == null ? 0 : 1);
		if (p2 != null) {
			pw.writeInt(p2.x);
			pw.writeInt(p2.y);
		}

		return pw.getPacket();
	}

	public static byte[] resetCapture() {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.CAPTURE_RESET.getValue());

		return pw.getPacket();
	}

	public static byte[] getMacros(SkillMacro[] macros) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.SKILL_MACRO.getValue());
		int count = 0;
		for (int i = 0; i < 5; i++) {
			if (macros[i] != null) {
				count++;
			}
		}
		pw.write(count);
		for (int i = 0; i < 5; i++) {
			SkillMacro macro = macros[i];
			if (macro != null) {
				pw.writeMapleAsciiString(macro.getName());
				pw.write(macro.getShout());
				pw.writeInt(macro.getSkill1());
				pw.writeInt(macro.getSkill2());
				pw.writeInt(macro.getSkill3());
			}
		}

		return pw.getPacket();
	}

	public static byte[] gameMsg(String msg) {
		PacketWriter pw = new PacketWriter();
		pw.writeShort(SendPacketOpcode.GAME_MSG.getValue());
		pw.writeAsciiString(msg);
		pw.write(1);

		return pw.getPacket();
	}

	public static byte[] innerPotentialMsg(String msg) {
		PacketWriter pw = new PacketWriter();
		pw.writeShort(SendPacketOpcode.INNER_ABILITY_MSG.getValue());
		pw.writeMapleAsciiString(msg);

		return pw.getPacket();
	}

	public static byte[] updateInnerPotential(byte ability, int skill, int level, int rank) {
		PacketWriter pw = new PacketWriter();
		pw.writeShort(SendPacketOpcode.ENABLE_INNER_ABILITY.getValue());
		pw.write(1); // unlock
		pw.write(1); // 0 = no update
		pw.writeShort(ability); // 1-3
		pw.writeInt(skill); // skill id (7000000+)
		pw.writeShort(level); // level, 0 = blank inner ability
		pw.writeShort(rank); // rank
		pw.write(1); // 0 = no update

		return pw.getPacket();
	}

	public static byte[] innerPotentialResetMessage() {
		PacketWriter pw = new PacketWriter();
		pw.writeShort(SendPacketOpcode.INNER_ABILITY_RESET_MSG.getValue());
		pw.writeMapleAsciiString("Ability reconfigured.");
		pw.write(1);
		return pw.getPacket();
	}

	public static byte[] updateHonour(int honourLevel, int honourExp, boolean levelup) {
		/*
		 * data: 03 00 00 00 69 00 00 00 01
		 */
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.UPDATE_HONOUR.getValue());

		pw.writeInt(honourLevel);
		pw.writeInt(honourExp);
		pw.write(levelup ? 1 : 0); // shows level up effect

		return pw.getPacket();
	}
	
	public static byte[] getCharInfo(MapleCharacter mc) {
		return getWarpToMap(mc, null, 0, false);
	}
	
	public static byte[] getWarpToMap(MapleCharacter mc, MapleMap to, int spawnPoint, boolean bCharacterData) {
		PacketWriter pw = new PacketWriter();
		pw.writeShort(SendPacketOpcode.WARP_TO_MAP.getValue());
		
		// size (int + int)
		pw.writeShort(0);
		
		pw.writeInt(mc.getClient().getChannel() - 1);
		
		// bDev
		pw.write(0);
		
		// wOldDriverID
		pw.writeInt(0);
		
		// Are you logging into the handling? (1), or changing the map? (2)
		// bPopupDlg
		pw.write(bCharacterData ? 1 : 2);
		
		// ?
		pw.writeInt(0);
		
		// nFieldWidth
		pw.writeInt(800);
		
		// nFieldHeight
		pw.writeInt(600);
		
		// Are you logging into the handling? (1), or changing the map? (0)
		pw.write(bCharacterData);
		
		// size (string (size->string))
		pw.writeShort(0);
		
		if(bCharacterData) {
			mc.CRand().connectData(pw);
			
			PacketHelper.addCharacterInfo(pw, mc);
			pw.writeInt(0); // log out event
		} else {
			
			// bUsingBuffProtector (this will call the revive function, upon death.)
			pw.write(0);
			
			pw.writeInt(to.getId());
			pw.write(spawnPoint);
			pw.writeInt(mc.getStat().getHp());
			
			// (bool (int + int))
			pw.write(0); 
		}
		
		// set white fade in-and-out
		pw.write(0);
		
		// set overlapping screen animation
		pw.write(0);
		
		// some sort of korean event fame-up
		pw.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
		
		// ?
		pw.writeInt(0x64);
		
		// party map experience.
		// bool (int + string(bgm) + int(fieldid))
		pw.write(0);
		
		// bool
		pw.write(0);
		
		// ?
		pw.write(1);
		
		// bool (int)
		pw.write(0);
		
		// bool ((int + byte(size))->(int, int, int))->(long, int, int)
		boolean starplanet = false;
		pw.write(0);
		if (starplanet) {
			
			// nRoundID
			pw.writeInt(0); 
			
			// the size, cannot exceed the count of 10
			pw.write(0); 
			
				// anPoint
				pw.writeInt(0);
				
				// anRanking
				pw.writeInt(0);
				
				// atLastCheckRank (timeGetTime - 300000)
				pw.writeInt(0);
				
			// ftShiningStarExpiredTime
			pw.writeLong(0);
			
			//nShiningStarPickedCount
			pw.writeInt(0);
			
			//nRoundStarPoint
			pw.writeInt(0);
		}			
		
		// bool (int + byte + long)
		boolean aStarPlanetRoundInfo = false;
		pw.write(aStarPlanetRoundInfo);
		if(aStarPlanetRoundInfo) {
			
			// nStarPlanetRoundID
			pw.writeInt(0);
			
			// nStarPlanetRoundState
			pw.write(0);
			
			// ftStarPlanetRoundEndDate
			pw.writeLong(0);
		}
		
		// int(size)->(int, string)
		pw.writeInt(0);
		
		// FreezeHotEventInfo
		
		// nAccountType
		pw.write(0);
		
		// dwAccountID
		pw.writeInt(0);
		
		// EventBestFriendInfo
		
		// dwEventBestFriendAID
		pw.writeInt(0);
		
		pw.writeInt(0);
		
		return pw.getPacket();
	}

	public static byte[] serverBlocked(int type) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.SERVER_BLOCKED.getValue());
		pw.write(type);

		return pw.getPacket();
	}

	public static byte[] pvpBlocked(int type) {
		PacketWriter pw = new PacketWriter();

		pw.write(type);

		return pw.getPacket();
	}

	public static byte[] showEquipEffect() {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.SHOW_EQUIP_EFFECT.getValue());

		return pw.getPacket();
	}

	public static byte[] showEquipEffect(int team) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.SHOW_EQUIP_EFFECT.getValue());
		pw.writeShort(team);

		return pw.getPacket();
	}

	public static byte[] multiChat(String name, String chattext, int mode) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.MULTICHAT.getValue());
		pw.write(mode);
		pw.writeMapleAsciiString(name);
		pw.writeMapleAsciiString(chattext);

		return pw.getPacket();
	}

	public static byte[] getFindReplyWithCS(String target, boolean buddy) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.WHISPER.getValue());
		pw.write(buddy ? 72 : 9);
		pw.writeMapleAsciiString(target);
		pw.write(2);
		pw.writeInt(-1);

		return pw.getPacket();
	}

	public static byte[] getWhisper(String sender, int channel, String text) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.WHISPER.getValue());
		pw.write(18);
		pw.writeMapleAsciiString(sender);
		pw.writeShort(channel - 1);
		pw.writeMapleAsciiString(text);

		return pw.getPacket();
	}

	public static byte[] getWhisperReply(String target, byte reply) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.WHISPER.getValue());
		pw.write(10);
		pw.writeMapleAsciiString(target);
		pw.write(reply);

		return pw.getPacket();
	}

	public static byte[] getFindReplyWithMap(String target, int mapid, boolean buddy) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.WHISPER.getValue());
		pw.write(buddy ? 72 : 9);
		pw.writeMapleAsciiString(target);
		pw.write(3);// was1
		pw.writeInt(mapid);// mapid);
		// pw.writeZeroBytes(8);

		return pw.getPacket();
	}

	public static byte[] getFindReply(String target, int channel, boolean buddy) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.WHISPER.getValue());
		pw.write(buddy ? 72 : 9);
		pw.writeMapleAsciiString(target);
		pw.write(3);
		pw.writeInt(channel - 1);

		return pw.getPacket();
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
		PacketWriter pw = new PacketWriter();
		pw.writeShort(SendPacketOpcode.BOSS_ENV.getValue());
		pw.write(mode);
		switch(mode) {
			case 1: // tremble effect
				pw.write(type);
				pw.writeInt(delay);
				pw.writeShort(30);
				break;
			case 2:
				pw.writeMapleAsciiString(text);
				break;
			case 4:
				pw.writeMapleAsciiString(text);
				break;
			case 5:
				pw.writeMapleAsciiString(text);
				pw.writeInt(0);
				break;
			case 12:
				pw.writeMapleAsciiString(text);
				pw.writeInt(0); 
				break;
			default: throw new UnsupportedOperationException("That mode has not been implemented.");
		}
		
		return pw.getPacket();
	}

	public static byte[] environmentMove(String env, int mode) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.MOVE_ENV.getValue());
		pw.writeMapleAsciiString(env);
		pw.writeInt(mode);

		return pw.getPacket();
	}

	public static byte[] getUpdateEnvironment(MapleMap map) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.UPDATE_ENV.getValue());
		pw.writeInt(map.getEnvironment().size());
		for (Map.Entry mp : map.getEnvironment().entrySet()) {
			pw.writeMapleAsciiString((String) mp.getKey());
			pw.writeInt(((Integer) mp.getValue()).intValue());
		}

		return pw.getPacket();
	}

	public static byte[] startMapEffect(String msg, int itemid, boolean active) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.MAP_EFFECT.getValue());
		pw.write(active ? 0 : 1);

		pw.writeInt(itemid);
		if (active) {
			pw.writeMapleAsciiString(msg);
		}
		return pw.getPacket();
	}

	public static byte[] removeMapEffect() {
		return startMapEffect(null, 0, false);
	}

	public static byte[] getGMEffect(int value, int mode) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.GM_EFFECT.getValue());
		pw.write(value);
		pw.write(new byte[17]);

		return pw.getPacket();
	}

	public static byte[] showOXQuiz(int questionSet, int questionId, boolean askQuestion) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.OX_QUIZ.getValue());
		pw.write(askQuestion ? 1 : 0);
		pw.write(questionSet);
		pw.writeShort(questionId);

		return pw.getPacket();
	}

	public static byte[] showEventInstructions() {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.GMEVENT_INSTRUCTIONS.getValue());
		pw.write(0);

		return pw.getPacket();
	}

	public static byte[] getPVPClock(int type, int time) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.CLOCK.getValue());
		pw.write(3);
		pw.write(type);
		pw.writeInt(time);

		return pw.getPacket();
	}

	public static byte[] getBanBanClock(int time, int direction) {
		PacketWriter packet = new PacketWriter();
		packet.writeShort(SendPacketOpcode.CLOCK.getValue());
		packet.write(5);
		packet.write(direction); // 0:?????? 1:????
		packet.writeInt(time);
		return packet.getPacket();
	}

	public static byte[] getClock(int time) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.CLOCK.getValue());
		pw.write(2);
		pw.writeInt(time);

		return pw.getPacket();
	}

	public static byte[] getClockTime(int hour, int min, int sec) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.CLOCK.getValue());
		pw.write(1);
		pw.write(hour);
		pw.write(min);
		pw.write(sec);

		return pw.getPacket();
	}

	public static byte[] boatPacket(int effect, int mode) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.BOAT_MOVE.getValue());
		pw.write(effect);
		pw.write(mode);

		return pw.getPacket();
	}

	public static byte[] setBoatState(int effect) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.BOAT_STATE.getValue());
		pw.write(effect);
		pw.write(1);

		return pw.getPacket();
	}

	public static byte[] stopClock() {
		return getPacketFromHexString(Integer.toHexString(SendPacketOpcode.STOP_CLOCK.getValue()) + " 00");
	}

	public static byte[] showAriantScoreBoard() {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.ARIANT_SCOREBOARD.getValue());

		return pw.getPacket();
	}

	public static byte[] sendPyramidUpdate(int amount) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.PYRAMID_UPDATE.getValue());
		pw.writeInt(amount);

		return pw.getPacket();
	}

	public static byte[] sendPyramidResult(byte rank, int amount) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.PYRAMID_RESULT.getValue());
		pw.write(rank);
		pw.writeInt(amount);

		return pw.getPacket();
	}

	public static byte[] quickSlot(String skil) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.QUICK_SLOT.getValue());
		pw.write(skil == null ? 0 : 1);
		if (skil != null) {
			String[] slots = skil.split(",");
			for (int i = 0; i < 8; i++) {
				pw.writeInt(Integer.parseInt(slots[i]));
			}
		}

		return pw.getPacket();
	}

	public static byte[] getMovingPlatforms(MapleMap map) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.MOVE_PLATFORM.getValue());
		pw.writeInt(map.getPlatforms().size());
		for (MapleNodes.MaplePlatform mp : map.getPlatforms()) {
			pw.writeMapleAsciiString(mp.name);
			pw.writeInt(mp.start);
			pw.writeInt(mp.SN.size());
			for (int x = 0; x < mp.SN.size(); x++) {
				pw.writeInt((mp.SN.get(x)).intValue());
			}
			pw.writeInt(mp.speed);
			pw.writeInt(mp.x1);
			pw.writeInt(mp.x2);
			pw.writeInt(mp.y1);
			pw.writeInt(mp.y2);
			pw.writeInt(mp.x1);
			pw.writeInt(mp.y1);
			pw.writeShort(mp.r);
		}

		return pw.getPacket();
	}

	public static byte[] sendPyramidKills(int amount) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.PYRAMID_KILL_COUNT.getValue());
		pw.writeInt(amount);

		return pw.getPacket();
	}

	public static byte[] sendPVPMaps() {
		final PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.PVP_INFO.getValue());
		pw.write(3); // max amount of players
		for (int i = 0; i < 20; i++) {
			pw.writeInt(10); // how many peoples in each map
		}
		pw.write(new byte[124]);
		pw.writeShort(150); //// PVP 1.5 EVENT!
		pw.write(0);

		return pw.getPacket();
	}

	public static byte[] gainForce(int oid, int count, int color) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.GAIN_FORCE.getValue());
		pw.write(1); // 0 = remote user?
		pw.writeInt(oid);
		byte newcheck = 0;
		pw.writeInt(newcheck); // unk
		if (newcheck > 0) {
			pw.writeInt(0); // unk
			pw.writeInt(0); // unk
		}
		pw.write(0);
		pw.writeInt(4); // size, for each below
		pw.writeInt(count); // count
		pw.writeInt(color); // color, 1-10 for demon, 1-2 for phantom
		pw.writeInt(0); // unk
		pw.writeInt(0); // unk
		return pw.getPacket();
	}

	public static byte[] getAndroidTalkStyle(int npc, String talk, int... args) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.NPC_TALK.getValue());
		pw.write(4);
		pw.writeInt(npc);
		pw.writeShort(10);
		pw.writeMapleAsciiString(talk);
		pw.write(args.length);

		for (int i = 0; i < args.length; i++) {
			pw.writeInt(args[i]);
		}
		return pw.getPacket();
	}

	public static byte[] achievementRatio(int amount) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.ACHIEVEMENT_RATIO.getValue());
		pw.writeInt(amount);

		return pw.getPacket();
	}

	public static byte[] getQuickMoveInfo(boolean show, List<QuickMoveNPC> qm) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.QUICK_MOVE.getValue());
		pw.write(qm.size() <= 0 ? 0 : show ? qm.size() : 0);
		if (show && qm.size() > 0) {
			for (QuickMoveNPC qmn : qm) {
				pw.writeInt(0);
				pw.writeInt(qmn.getId());
				pw.writeInt(qmn.getType());
				pw.writeInt(qmn.getLevel());
				pw.writeMapleAsciiString(qmn.getDescription());
				pw.writeLong(PacketHelper.getTime(-2));
				pw.writeLong(PacketHelper.getTime(-1));
			}
		}

		return pw.getPacket();
	}

	public static byte[] spawnPlayerMapobject(MapleCharacter chr) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.SPAWN_PLAYER.getValue());
		pw.writeInt(chr.getId()); // dwCharacterID
		
		// CUserRemote::Init
		pw.write(chr.getLevel());
		pw.writeMapleAsciiString(chr.getName());
		MapleQuestStatus ultExplorer = chr.getQuestNoAdd(MapleQuest.getInstance(111111));
		if ((ultExplorer != null) && (ultExplorer.getCustomData() != null)) {
			pw.writeMapleAsciiString(ultExplorer.getCustomData());
		} else {
			pw.writeMapleAsciiString("");
		}
		if (chr.getGuildId() <= 0) {
			pw.write(new byte[8]);
		} else {
			MapleGuild gs = World.Guild.getGuild(chr.getGuildId());
			if (gs != null) {
				pw.writeMapleAsciiString(gs.getName());
				pw.writeShort(gs.getLogoBG());
				pw.write(gs.getLogoBGColor());
				pw.writeShort(gs.getLogo());
				pw.write(gs.getLogoColor());
			} else {
				pw.write(new byte[8]);
			}
		}
		pw.write(chr.getGender());
		pw.writeInt(chr.getFame());
		pw.writeInt(1); // farmLevel
		pw.writeInt(0); // NameTagMark
		
		Map<MapleBuffStat, MapleBuffStatValueHolder> statups = chr.getBuffValues();
		List<MapleBuffStat> stats = Arrays.asList(
				MapleBuffStat.PyramidEffect,
				MapleBuffStat.KillingPoint,
				MapleBuffStat.PinkbeanRollingGrade,
				MapleBuffStat.ZeroAuraStr,
				MapleBuffStat.ZeroAuraSpd,
				MapleBuffStat.BMageAura,
				MapleBuffStat.BattlePvP_Helena_Mark,
				MapleBuffStat.BattlePvP_LangE_Protection,
				MapleBuffStat.AdrenalinBoost,
				MapleBuffStat.RWBarrier);
		
		for(MapleBuffStat stat : stats) {
			statups.putIfAbsent(stat, new MapleBuffStatValueHolder(null, 0, null, 0, 0, chr.getId()));
		}
		
		int[] mask = new int[18];
		
		List<MapleBuffStat> temporaryStat = Arrays.asList(
				MapleBuffStat.EnergyCharged,
				MapleBuffStat.Dash_Speed,
				MapleBuffStat.Dash_Jump,
				MapleBuffStat.RideVehicle,
				MapleBuffStat.PartyBooster,
				MapleBuffStat.GuidedBullet,
				MapleBuffStat.Undead,
				MapleBuffStat.RideVehicleExpire);
		
		// TwoStateTemporaryStat
		for(MapleBuffStat statup : temporaryStat) {
			mask[statup.getPosition()] |= statup.getValue();
		}
		
		for(MapleBuffStat statup : statups.keySet()) {
			mask[statup.getPosition()] |= statup.getValue();
		}
		
		for(int i = mask.length; i >= 2; i--) {
			pw.writeInt(mask[i - 1]);
		}
		
		PacketHelper.decodeForRemote(pw, chr, statups);
		
		pw.writeShort(chr.getJob());
		pw.writeShort(chr.getSubcategory());
		pw.writeInt(0); // nTotalCHUC (star force enchantment)
		
		PacketHelper.addCharLook(pw, chr, true, false);
		if (GameConstants.isZero(chr.getJob())) {
			PacketHelper.addCharLook(pw, chr, true, false);
		}

		pw.writeInt(0); // dwDriverID
		pw.writeInt(0); // dwPassengerID
		pw.writeInt(0);
		pw.writeInt(0);
		pw.writeInt(0);
		
		// nChocoCount
		pw.writeInt(Math.min(250, chr.getInventory(MapleInventoryType.CASH).countById(5110000)));
		
		// chr.getItemEffect()
		pw.writeInt(0); // nActiveEffectItemID
		pw.writeInt(0); // nMonkeyEffectItemID
		
		// MapleQuestStatus status = chr.getQuestNoAdd(MapleQuest.getInstance(124000));
		// status != null && status.getCustomData() != null ? Integer.parseInt(status.getCustomData()) : 
		pw.writeInt(0);
		
		pw.writeInt(0); // nDamageSkin
		pw.writeInt(0); // ptPos
		pw.writeInt(0); // nDemonWing
		pw.writeInt(0); // nKaiserWingID
		pw.writeInt(0); // nKaiserTailID
		pw.writeInt(0); // nCompletedSetItemID
		pw.writeShort(-1); // nFieldSeatID
		
		// nPortableChairID

		pw.writeInt(GameConstants.getInventoryType(chr.getChair()) == 
				MapleInventoryType.SETUP ? chr.getChair() : 0);
		
		pw.writeInt(0);
		pw.writeInt(0); // lTowerChairIDList
		pw.writeInt(0); // head title? chr.getHeadTitle()
		pw.writeInt(0);
		pw.writeShort(chr.getTruePosition().x);
        pw.writeShort(chr.getTruePosition().y);
		pw.write(chr.getStance());
		pw.writeShort(chr.getFH());
		
		pw.write(0);
		pw.write(0);
		pw.write(0);
		pw.write(1);
		pw.write(0);

		pw.writeInt(chr.getMount().getLevel()); 
		pw.writeInt(chr.getMount().getExp());
		pw.writeInt(chr.getMount().getFatigue());

		PacketHelper.addAnnounceBox(pw, chr);
		
		pw.write((chr.getChalkboard() != null) && (chr.getChalkboard().length() > 0) ? 1 : 0);
		if ((chr.getChalkboard() != null) && (chr.getChalkboard().length() > 0)) {
			pw.writeMapleAsciiString(chr.getChalkboard());
		}

		Triple<List<MapleRing>, List<MapleRing>, List<MapleRing>> rings = chr.getRings(false);
		addRingInfo(pw, rings.getLeft());
		addRingInfo(pw, rings.getMid());
		addMRingInfo(pw, rings.getRight(), chr);

		pw.write(0); // mask
		pw.writeInt(0); // nEvanDragonGlide_Riding

		if (GameConstants.isKaiser(chr.getJob())) {
			String x = chr.getOneInfo(12860, "extern");
			pw.writeInt(x == null ? 0 : Integer.parseInt(x));
			x = chr.getOneInfo(12860, "inner");
			pw.writeInt(x == null ? 0 : Integer.parseInt(x));
			x = chr.getOneInfo(12860, "primium");
			pw.write(x == null ? 0 : Integer.parseInt(x));
		}
		
		pw.writeInt(0); // nSkillID (CUser::SetMakingMeisterSkillEff(..., nSkillID)
		
		PacketHelper.addFarmInfo(pw, chr.getClient(), (byte) 0);
		for (int i = 0; i < 5; i++) {
			pw.write(-1);
		}

		pw.writeInt(0); // nItemID
		pw.write(0); // bSoulEffect
		pw.write(0); // ?
		
		// StarPlanetRank::Decode
		pw.write(0);
		
		// DecodeStarPlanetTrendShopLook
		pw.writeInt(0);
		
		// DecodeTextEquipInfo
		pw.writeInt(0);
		
		// DecodeFreezeHotEventInfo
		 pw.write(0); // nAccountType
		 pw.writeInt(0); // dwAccountID
		 
		 // DecodeEventBestFriendInfo
		 pw.writeInt(0); // dwEventBestFriendAID
		 
		 pw.write(0); // bOnOff (OnKinesisPsychicEnergyShieldEffect)
		 pw.write(1); // bBeastFormWingOnOff
		 pw.writeInt(0); // nMeso
		 
		 pw.writeInt(1);
		 pw.writeInt(0);
		 pw.writeMapleAsciiString("");
		 pw.writeInt(0);
		 pw.write(0);
		 pw.writeInt(0);
		 pw.writeInt(0);
		 pw.writeInt(0);

		return pw.getPacket();
	}

	public static byte[] removePlayerFromMap(int cid) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.REMOVE_PLAYER_FROM_MAP.getValue());
		pw.writeInt(cid);

		return pw.getPacket();
	}

	public static byte[] getChatText(int cidfrom, String text, boolean whiteBG, int show) {
		PacketWriter pw = new PacketWriter();
		pw.writeShort(SendPacketOpcode.CHATTEXT.getValue());
		pw.writeInt(cidfrom);
		pw.write(whiteBG ? 1 : 0);
		pw.writeMapleAsciiString(text);
		pw.write(show);
		pw.write(0);
		pw.write(-1);
		// pw.writeMapleAsciiString("[fuck]"); // new
		return pw.getPacket();
	}

	public static byte[] getScrollEffect(int chr, Equip.ScrollResult scrollSuccess, boolean legendarySpirit, int item,
			int scroll) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.SHOW_SCROLL_EFFECT.getValue());
		pw.writeInt(chr);
		pw.write(
				scrollSuccess == Equip.ScrollResult.SUCCESS ? 1 : scrollSuccess == Equip.ScrollResult.CURSE ? 2 : 0);
		pw.write(legendarySpirit ? 1 : 0);
		pw.writeInt(scroll); // scroll
		pw.writeInt(item); // item
		pw.writeInt(0);
		pw.write(0);
		pw.write(0);

		return pw.getPacket();
	}

	public static byte[] showMagnifyingEffect(int chr, short pos) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.SHOW_MAGNIFYING_EFFECT.getValue());
		pw.writeInt(chr);
		pw.writeShort(pos);
		pw.write(0);// new 143 is in ida?

		return pw.getPacket();
	}

	public static byte[] showPotentialReset(int chr, boolean success, int itemid) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.SHOW_POTENTIAL_RESET.getValue());
		pw.writeInt(chr);
		pw.writeBoolean(success);
		pw.writeInt(itemid);

		return pw.getPacket();
	}


	public static byte[] showBlackCubePotentialReset(int chr, boolean success, int itemId) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.SHOW_POTENTIAL_BLACK_CUBE.getValue());
		pw.writeInt(chr);
		pw.writeBoolean(success);
		pw.writeInt(itemId);

		return pw.getPacket();
	}

	public static byte[] showNebuliteEffect(int chr, boolean success) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.SHOW_NEBULITE_EFFECT.getValue());
		pw.writeInt(chr);
		pw.write(success ? 1 : 0);
		pw.writeMapleAsciiString(success ? "Successfully mounted Nebulite." : "Failed to mount Nebulite.");

		return pw.getPacket();
	}

	public static byte[] useNebuliteFusion(int cid, int itemId, boolean success) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.SHOW_FUSION_EFFECT.getValue());
		pw.writeInt(cid);
		pw.write(success ? 1 : 0);
		pw.writeInt(itemId);

		return pw.getPacket();
	}

	public static byte[] pvpAttack(int cid, int playerLevel, int skill, int skillLevel, int speed, int mastery,
			int projectile, int attackCount, int chargeTime, int stance, int direction, int range, int linkSkill,
			int linkSkillLevel, boolean movementSkill, boolean pushTarget, boolean pullTarget,
			List<AttackPair> attack) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.PVP_ATTACK.getValue());
		pw.writeInt(cid);
		pw.write(playerLevel);
		pw.writeInt(skill);
		pw.write(skillLevel);
		pw.writeInt(linkSkill != skill ? linkSkill : 0);
		pw.write(linkSkillLevel != skillLevel ? linkSkillLevel : 0);
		pw.write(direction);
		pw.write(movementSkill ? 1 : 0);
		pw.write(pushTarget ? 1 : 0);
		pw.write(pullTarget ? 1 : 0);
		pw.write(0);
		pw.writeShort(stance);
		pw.write(speed);
		pw.write(mastery);
		pw.writeInt(projectile);
		pw.writeInt(chargeTime);
		pw.writeInt(range);
		pw.write(attack.size());
		pw.write(0);
		pw.writeInt(0);
		pw.write(attackCount);
		pw.write(0);
		for (AttackPair p : attack) {
			pw.writeInt(p.objectid);
			pw.writeInt(0);
			pw.writePos(p.point);
			pw.write(0);
			pw.writeInt(0);
			for (Pair atk : p.attack) {
				pw.writeInt(((Integer) atk.left).intValue());
				pw.writeInt(0);
				pw.write(((Boolean) atk.right).booleanValue() ? 1 : 0);
				pw.writeShort(0);
			}
		}

		return pw.getPacket();
	}

	public static byte[] getPVPMist(int cid, int mistSkill, int mistLevel, int damage) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.PVP_MIST.getValue());
		pw.writeInt(cid);
		pw.writeInt(mistSkill);
		pw.write(mistLevel);
		pw.writeInt(damage);
		pw.write(8);
		pw.writeInt(1000);

		return pw.getPacket();
	}

	public static byte[] pvpCool(int cid, List<Integer> attack) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.PVP_COOL.getValue());
		pw.writeInt(cid);
		pw.write(attack.size());
		for (Iterator<Integer> i$ = attack.iterator(); i$.hasNext();) {
			int b = i$.next().intValue();
			pw.writeInt(b);
		}

		return pw.getPacket();
	}

	public static byte[] teslaTriangle(int cid, int sum1, int sum2, int sum3) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.TESLA_TRIANGLE.getValue());
		pw.writeInt(cid);
		pw.writeInt(sum1);
		pw.writeInt(sum2);
		pw.writeInt(sum3);

		pw.write(new byte[69]);// test

		return pw.getPacket();
	}

	public static byte[] followEffect(int initiator, int replier, Point toMap) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.FOLLOW_EFFECT.getValue());
		pw.writeInt(initiator);
		pw.writeInt(replier);
		pw.writeLong(0);
		if (replier == 0) {
			pw.write(toMap == null ? 0 : 1);
			if (toMap != null) {
				pw.writeInt(toMap.x);
				pw.writeInt(toMap.y);
			}
		}

		return pw.getPacket();
	}

	public static byte[] showPQReward(int cid) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.SHOW_PQ_REWARD.getValue());
		pw.writeInt(cid);
		for (int i = 0; i < 6; i++) {
			pw.write(0);
		}

		return pw.getPacket();
	}

	public static byte[] craftMake(int cid, int something, int time) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.CRAFT_EFFECT.getValue());
		pw.writeInt(cid);
		pw.writeInt(something);
		pw.writeInt(time);

		return pw.getPacket();
	}

	public static byte[] craftFinished(int cid, int craftID, int ranking, int itemId, int quantity, int exp) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.CRAFT_COMPLETE.getValue());
		pw.writeInt(cid);
		pw.writeInt(craftID);
		pw.writeInt(ranking);
		pw.writeInt(itemId);
		pw.writeInt(quantity);
		pw.writeInt(exp);

		return pw.getPacket();
	}

	public static byte[] harvestResult(int cid, boolean success) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.HARVESTED.getValue());
		pw.writeInt(cid);
		pw.write(success ? 1 : 0);

		return pw.getPacket();
	}

	public static byte[] playerDamaged(int cid, int dmg) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.PLAYER_DAMAGED.getValue());
		pw.writeInt(cid);
		pw.writeInt(dmg);

		return pw.getPacket();
	}

	public static byte[] showPyramidEffect(int chr) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.NETT_PYRAMID.getValue());
		pw.writeInt(chr);
		pw.write(1);
		pw.writeInt(0);
		pw.writeInt(0);

		return pw.getPacket();
	}

	public static byte[] pamsSongEffect(int cid) {
		PacketWriter pw = new PacketWriter();
		pw.writeShort(SendPacketOpcode.PAMS_SONG.getValue());
		pw.writeInt(cid);
		return pw.getPacket();
	}

	public static byte[] spawnHaku_change0(int cid) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.HAKU_CHANGE_0.getValue());
		pw.writeInt(cid);

		return pw.getPacket();
	}

	public static byte[] spawnHaku_change1(MapleHaku d) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.HAKU_CHANGE_1.getValue());
		pw.writeInt(d.getOwner());
		pw.writePos(d.getPosition());
		pw.write(d.getStance());
		pw.writeShort(0);
		pw.write(0);
		pw.writeInt(0);

		return pw.getPacket();
	}

	public static byte[] spawnHaku_bianshen(int cid, int oid, boolean change) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.HAKU_CHANGE.getValue());
		pw.writeInt(cid);
		pw.writeInt(oid);
		pw.write(change ? 2 : 1);

		return pw.getPacket();
	}

	public static byte[] hakuUnk(int cid, int oid, boolean change) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.HAKU_CHANGE.getValue());
		pw.writeInt(cid);
		pw.writeInt(oid);
		pw.write(0);
		pw.write(0);
		pw.writeMapleAsciiString("lol");

		return pw.getPacket();
	}

	public static byte[] spawnHaku(MapleHaku d) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.SPAWN_HAKU.getValue());
		pw.writeInt(d.getOwner());
		pw.writeInt(d.getObjectId());
		pw.writeInt(40020109);
		pw.write(1);
		pw.writePos(d.getPosition());
		pw.write(0);
		pw.writeShort(d.getStance());

		return pw.getPacket();
	}

	public static byte[] moveHaku(int cid, int oid, Point pos, List<LifeMovementFragment> res) {
		PacketWriter pw = new PacketWriter();
		pw.writeShort(SendPacketOpcode.HAKU_MOVE.getValue());
		pw.writeInt(cid);
		pw.writeInt(oid);
		pw.writeInt(0);
		pw.writePos(pos);
		pw.writeInt(0);
		PacketHelper.serializeMovementList(pw, res);
		return pw.getPacket();
	}

	public static byte[] spawnDragon(MapleDragon d) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.DRAGON_SPAWN.getValue());
		pw.writeInt(d.getOwner());
		pw.writeInt(d.getPosition().x);
		pw.writeInt(d.getPosition().y);
		pw.write(d.getStance());
		pw.writeShort(0);
		pw.writeShort(d.getJobId());

		return pw.getPacket();
	}

	public static byte[] removeDragon(int chrid) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.DRAGON_REMOVE.getValue());
		pw.writeInt(chrid);

		return pw.getPacket();
	}

	public static byte[] moveDragon(MapleDragon d, Point startPos, List<LifeMovementFragment> moves) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.DRAGON_MOVE.getValue());
		pw.writeInt(d.getOwner());
		pw.writeInt(0);
		pw.writePos(startPos);
		pw.writeInt(0);
		PacketHelper.serializeMovementList(pw, moves);

		return pw.getPacket();
	}

	public static byte[] spawnAndroid(MapleCharacter cid, MapleAndroid android) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.ANDROID_SPAWN.getValue());
		pw.writeInt(cid.getId());
		pw.write(android.getItemId() == 1662006 ? 5 : android.getItemId() - 1661999);
		pw.writePos(android.getPos());
		pw.write(android.getStance());
		pw.writeShort(0);
		pw.writeShort(0);
		pw.writeShort(android.getHair() - 30000);
		pw.writeShort(android.getFace() - 20000);
		pw.writeMapleAsciiString(android.getName());
		for (short i = -1200; i > -1207; i = (short) (i - 1)) {
			Item item = cid.getInventory(MapleInventoryType.EQUIPPED).getItem(i);
			pw.writeInt(item != null ? item.getItemId() : 0);
		}

		return pw.getPacket();
	}

	public static byte[] moveAndroid(int cid, Point pos, List<LifeMovementFragment> res) {
		PacketWriter pw = new PacketWriter();
		pw.writeShort(SendPacketOpcode.ANDROID_MOVE.getValue());
		pw.writeInt(cid);
		pw.writeInt(0);
		pw.writePos(pos);
		pw.writeInt(2147483647);
		PacketHelper.serializeMovementList(pw, res);
		return pw.getPacket();
	}

	public static byte[] showAndroidEmotion(int cid, byte emo1/* , byte emo2 */) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.ANDROID_EMOTION.getValue());
		pw.writeInt(cid);
		pw.write(0);// new
		pw.write(emo1);

		return pw.getPacket();
	}

	public static byte[] updateAndroidLook(boolean itemOnly, MapleCharacter cid, MapleAndroid android) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.ANDROID_UPDATE.getValue());
		pw.writeInt(cid.getId());
		pw.write(itemOnly ? 1 : 0);
		if (itemOnly) {
			for (short i = -1200; i > -1207; i = (short) (i - 1)) {
				Item item = cid.getInventory(MapleInventoryType.EQUIPPED).getItem(i);
				pw.writeInt(item != null ? item.getItemId() : 0);
			}
		} else {
			pw.writeShort(0);
			pw.writeShort(android.getHair() - 30000);
			pw.writeShort(android.getFace() - 20000);
			pw.writeMapleAsciiString(android.getName());
		}

		return pw.getPacket();
	}

	public static byte[] deactivateAndroid(int cid) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.ANDROID_DEACTIVATED.getValue());
		pw.writeInt(cid);

		return pw.getPacket();
	}

	public static byte[] removeFamiliar(int cid) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.SPAWN_FAMILIAR.getValue());
		pw.writeInt(cid);
		pw.writeShort(0);
		pw.write(0);

		return pw.getPacket();
	}

	public static byte[] spawnFamiliar(MonsterFamiliar mf, boolean spawn, boolean respawn) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(respawn ? SendPacketOpcode.SPAWN_FAMILIAR_2.getValue() : SendPacketOpcode.SPAWN_FAMILIAR.getValue());
		pw.writeInt(mf.getCharacterId());
		pw.write(spawn ? 1 : 0);
		pw.write(respawn ? 1 : 0);
		pw.write(0);
		if (spawn) {
			pw.writeInt(mf.getFamiliar());
			pw.writeInt(mf.getFatigue());
			pw.writeInt(mf.getVitality() * 300); // max fatigue
			pw.writeMapleAsciiString(mf.getName());
			pw.writePos(mf.getTruePosition());
			pw.write(mf.getStance());
			pw.writeShort(mf.getFh());
		}

		return pw.getPacket();
	}

	public static byte[] moveFamiliar(int cid, Point startPos, List<LifeMovementFragment> moves) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.MOVE_FAMILIAR.getValue());
		pw.writeInt(cid);
		pw.write(0);
		pw.writePos(startPos);
		pw.writeInt(0);
		PacketHelper.serializeMovementList(pw, moves);

		return pw.getPacket();
	}

	public static byte[] touchFamiliar(int cid, byte unk, int objectid, int type, int delay, int damage) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.TOUCH_FAMILIAR.getValue());
		pw.writeInt(cid);
		pw.write(0);
		pw.write(unk);
		pw.writeInt(objectid);
		pw.writeInt(type);
		pw.writeInt(delay);
		pw.writeInt(damage);

		return pw.getPacket();
	}

	public static byte[] familiarAttack(int cid, byte unk, List<Triple<Integer, Integer, List<Integer>>> attackPair) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.ATTACK_FAMILIAR.getValue());
		pw.writeInt(cid);
		pw.write(0);// familiar id?
		pw.write(unk);
		pw.write(attackPair.size());
		for (Triple<Integer, Integer, List<Integer>> s : attackPair) {
			pw.writeInt(s.left);
			pw.write(s.mid);
			pw.write(s.right.size());
			for (int damage : s.right) {
				pw.writeInt(damage);
			}
		}

		return pw.getPacket();
	}

	public static byte[] renameFamiliar(MonsterFamiliar mf) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.RENAME_FAMILIAR.getValue());
		pw.writeInt(mf.getCharacterId());
		pw.write(0);
		pw.writeInt(mf.getFamiliar());
		pw.writeMapleAsciiString(mf.getName());

		return pw.getPacket();
	}

	public static byte[] updateFamiliar(MonsterFamiliar mf) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.UPDATE_FAMILIAR.getValue());
		pw.writeInt(mf.getCharacterId());
		pw.writeInt(mf.getFamiliar());
		pw.writeInt(mf.getFatigue());
		pw.writeLong(PacketHelper.getTime(mf.getVitality() >= 3 ? System.currentTimeMillis() : -2L));

		return pw.getPacket();
	}

	public static byte[] movePlayer(int cid, List<LifeMovementFragment> moves, Point startPos) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.MOVE_PLAYER.getValue());
		pw.writeInt(cid);
		pw.writeInt(0);
		pw.writePos(startPos);
		pw.writeShort(0);
		pw.writeShort(0);
		PacketHelper.serializeMovementList(pw, moves);

		return pw.getPacket();
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
		PacketWriter pw = new PacketWriter();

		if (type == 0) {
			pw.writeShort(SendPacketOpcode.CLOSE_RANGE_ATTACK.getValue());
		} else if (type == 1 || type == 2) {
			pw.writeShort(SendPacketOpcode.RANGED_ATTACK.getValue());
		} else if (type == 3) {
			pw.writeShort(SendPacketOpcode.MAGIC_ATTACK.getValue());
		} else {
			pw.writeShort(SendPacketOpcode.ENERGY_ATTACK.getValue());
		}

		pw.writeInt(cid);
		pw.write(tbyte);
		// System.out.println(nMobCount + " - nMobCount");
		pw.write(lvl);
		if ((skill > 0) || (type == 3)) {
			pw.write(level);
			if (level > 0) {
				pw.writeInt(skill);
			}
		} else if (type != 2 && type != 3) {
			pw.write(0);
		}

		if (GameConstants.isZero(skill / 10000) && skill != 100001283) {
			short zero1 = 0;
			short zero2 = 0;
			pw.write(zero1 > 0 || zero2 > 0); // boolean
			if (zero1 > 0 || zero2 > 0) {
				pw.writeShort(zero1);
				pw.writeShort(zero2);
				// there is a full handler so better not write zero
			}
		}

		if (type == 2) {
			pw.write(ultLevel);
			if (ultLevel > 0) {
				pw.writeInt(3220010);
			}
		}
		if (skill == 40021185 || skill == 42001006) {
			pw.write(0); // boolean if true then int
		}
		if (type == 0 || type == 1) {
			pw.write(0);
		}
		pw.write(unk);// always 0?
		if ((unk & 2) != 0) {
			pw.writeInt(0);
			pw.writeInt(0);
		}
		pw.writeShort(display);
		pw.write(speed);
		pw.write(mastery);
		pw.writeInt(charge);
		for (AttackPair oned : damage) {
			if (oned.attack != null) {
				pw.writeInt(oned.objectid);
				pw.write(7);
				pw.write(0);
				pw.write(0);
				if (skill == 42111002) {
					pw.write(oned.attack.size());
					for (Pair eachd : oned.attack) {
						pw.writeInt(((Integer) eachd.left).intValue());
					}
				} else {
					for (Pair eachd : oned.attack) {
						pw.write(((Boolean) eachd.right).booleanValue() ? 1 : 0);
						pw.writeInt(((Integer) eachd.left).intValue());
					}
				}
			}
		}
		if (skill == 2321001 || skill == 2221052 || skill == 11121052) {
			pw.writeInt(0);
		} else if (skill == 65121052 || skill == 101000202 || skill == 101000102) {
			pw.writeInt(0);
			pw.writeInt(0);
		}
		if (skill == 42100007) {
			pw.writeShort(0);
			pw.write(0);
		}
		if (type == 1 || type == 2) {
			pw.writePos(pos);
		} else if (type == 3 && charge > 0) {
			pw.writeInt(charge);
		}
		if (skill == 5321000 || skill == 5311001 || skill == 5321001 || skill == 5011002 || skill == 5311002
				|| skill == 5221013 || skill == 5221017 || skill == 3120019 || skill == 3121015 || skill == 4121017) {
			pw.writePos(pos);
		}
		pw.write(new byte[30]);// test

		return pw.getPacket();
	}

	public static byte[] skillEffect(MapleCharacter from, int skillId, byte level, short display, byte unk) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.SKILL_EFFECT.getValue());
		pw.writeInt(from.getId());
		pw.writeInt(skillId);
		pw.write(level);
		pw.writeShort(display);
		pw.write(unk);
		if (skillId == 13111020) {
			pw.writePos(from.getPosition()); // Position
		}

		return pw.getPacket();
	}

	public static byte[] skillCancel(MapleCharacter from, int skillId) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.CANCEL_SKILL_EFFECT.getValue());
		pw.writeInt(from.getId());
		pw.writeInt(skillId);

		return pw.getPacket();
	}

	public static byte[] damagePlayer(int cid, int type, int damage, int monsteridfrom, byte direction, int skillid,
			int pDMG, boolean pPhysical, int pID, byte pType, Point pPos, byte offset, int offset_d, int fake) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.DAMAGE_PLAYER.getValue());
		pw.writeInt(cid);
		pw.write(type);
		pw.writeInt(damage);
		pw.write(0);
		if (type >= -1) {
			pw.writeInt(monsteridfrom);
			pw.write(direction);
			pw.writeInt(skillid);
			pw.writeInt(pDMG);
			pw.write(0);
			if (pDMG > 0) {
				pw.write(pPhysical ? 1 : 0);
				pw.writeInt(pID);
				pw.write(pType);
				pw.writePos(pPos);
			}
			pw.write(offset);
			if (offset == 1) {
				pw.writeInt(offset_d);
			}
		}
		pw.writeInt(damage);
		if ((damage <= 0) || (fake > 0)) {
			pw.writeInt(fake);
		}

		return pw.getPacket();
	}

	public static byte[] facialExpression(MapleCharacter from, int expression) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.FACIAL_EXPRESSION.getValue());
		pw.writeInt(from.getId());
		pw.writeInt(expression);
		pw.writeInt(-1);
		pw.write(0);

		return pw.getPacket();
	}

	public static byte[] directionFacialExpression(int expression, int duration) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.DIRECTION_FACIAL_EXPRESSION.getValue());
		pw.writeInt(expression);
		pw.writeInt(duration);
		pw.write(0);

		/*
		 * Facial Expressions: 0 - Normal 1 - F1 2 - F2 3 - F3 4 - F4 5 - F5 6 -
		 * F6 7 - F7 8 - Vomit 9 - Panic 10 - Sweetness 11 - Kiss 12 - Wink 13 -
		 * Ouch! 14 - Goo goo eyes 15 - Blaze 16 - Star 17 - Love 18 - Ghost 19
		 * - Constant Sigh 20 - Sleepy 21 - Flaming hot 22 - Bleh 23 - No Face
		 */
		return pw.getPacket();
	}

	public static byte[] itemEffect(int characterid, int itemid) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.SHOW_EFFECT.getValue());
		pw.writeInt(characterid);
		pw.writeInt(itemid);

		return pw.getPacket();
	}

	public static byte[] showTitle(int characterid, int itemid) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.SHOW_TITLE.getValue());
		pw.writeInt(characterid);
		pw.writeInt(itemid);

		return pw.getPacket();
	}

	public static byte[] showAngelicBuster(int characterid, int tempid) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.ANGELIC_CHANGE.getValue());
		pw.writeInt(characterid);
		pw.writeInt(tempid);

		return pw.getPacket();
	}

	public static byte[] showChair(int characterid, int itemid) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.SHOW_CHAIR.getValue());
		pw.writeInt(characterid);
		pw.writeInt(itemid);
		pw.writeInt(0);

		return pw.getPacket();
	}

	public static byte[] updateCharLook(MapleCharacter chr, boolean second) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.UPDATE_CHAR_LOOK.getValue());
		pw.writeInt(chr.getId());
		pw.write(1);
		PacketHelper.addCharLook(pw, chr, false, second);
		Triple<List<MapleRing>, List<MapleRing>, List<MapleRing>> rings = chr.getRings(false);
		addRingInfo(pw, rings.getLeft());
		addRingInfo(pw, rings.getMid());
		addMRingInfo(pw, rings.getRight(), chr);
		pw.writeInt(0); // -> charid to follow (4)
		pw.writeInt(0);
		return pw.getPacket();
	}

	public static byte[] updatePartyMemberHP(int cid, int curhp, int maxhp) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.UPDATE_PARTYMEMBER_HP.getValue());
		pw.writeInt(cid);
		pw.writeInt(curhp);
		pw.writeInt(maxhp);

		return pw.getPacket();
	}

	public static byte[] loadGuildName(MapleCharacter chr) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.LOAD_GUILD_NAME.getValue());
		pw.writeInt(chr.getId());
		if (chr.getGuildId() <= 0) {
			pw.writeShort(0);
		} else {
			MapleGuild gs = World.Guild.getGuild(chr.getGuildId());
			if (gs != null) {
				pw.writeMapleAsciiString(gs.getName());
			} else {
				pw.writeShort(0);
			}
		}

		return pw.getPacket();
	}

	public static byte[] loadGuildIcon(MapleCharacter chr) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.LOAD_GUILD_ICON.getValue());
		pw.writeInt(chr.getId());
		if (chr.getGuildId() <= 0) {
			pw.write(new byte[6]);
		} else {
			MapleGuild gs = World.Guild.getGuild(chr.getGuildId());
			if (gs != null) {
				pw.writeShort(gs.getLogoBG());
				pw.write(gs.getLogoBGColor());
				pw.writeShort(gs.getLogo());
				pw.write(gs.getLogoColor());
			} else {
				pw.write(new byte[6]);
			}
		}

		return pw.getPacket();
	}

	public static byte[] changeTeam(int cid, int type) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.LOAD_TEAM.getValue());
		pw.writeInt(cid);
		pw.write(type);

		return pw.getPacket();
	}

	public static byte[] showHarvesting(int cid, int tool) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.SHOW_HARVEST.getValue());
		pw.writeInt(cid);
		if (tool > 0) {
			pw.write(1);
			pw.write(0);
			pw.writeShort(0);
			pw.writeInt(tool);
			pw.write(new byte[30]);
		} else {
			pw.write(0);
			pw.write(new byte[33]);
		}

		return pw.getPacket();
	}

	public static byte[] getPVPHPBar(int cid, int hp, int maxHp) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.PVP_HP.getValue());
		pw.writeInt(cid);
		pw.writeInt(hp);
		pw.writeInt(maxHp);

		return pw.getPacket();
	}

	
	public static byte[] cancelChair(int id, int characterId) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.CANCEL_CHAIR.getValue());
		pw.writeInt(characterId);
		if (id == -1) {
			pw.write(0);
		} else {
			pw.write(1);
			pw.writeShort(id);
		}

		return pw.getPacket();
	}

	public static byte[] instantMapWarp(byte portal) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.CURRENT_MAP_WARP.getValue());
		pw.write(0);
		pw.write(portal); // nUserCallingType
		
		if (portal <= 0) {
			pw.writeInt(0); // nIdx
		} else {
			pw.writeInt(0); // dwCallerID
			pw.writeShort(0); // x
			pw.writeShort(0); // y
		}

		return pw.getPacket();
	}

	public static byte[] updateQuestInfo(MapleCharacter c, int quest, int npc, byte progress) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.UPDATE_QUEST_INFO.getValue());
		pw.write(progress);
		pw.writeInt(quest);
		pw.writeInt(npc);
		pw.writeInt(0);
		pw.write(0); // new

		return pw.getPacket();
	}

	public static byte[] updateQuestFinish(int quest, int npc, int nextquest) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.UPDATE_QUEST_INFO.getValue());
		pw.write(11);// was 10
		pw.writeInt(quest);
		pw.writeInt(npc);
		pw.writeInt(nextquest);
		pw.write(0); // new

		return pw.getPacket();
	}

	public static byte[] sendHint(String hint, int width, int height) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.PLAYER_HINT.getValue());
		pw.writeMapleAsciiString(hint);
		pw.writeShort(width < 1 ? Math.max(hint.length() * 10, 40) : width);
		pw.writeShort(Math.max(height, 5));
		pw.write(1);

		return pw.getPacket();
	}

	public static byte[] updateCombo(int value) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.ARAN_COMBO.getValue());
		pw.writeInt(value);

		return pw.getPacket();
	}

	public static byte[] rechargeCombo(int value) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.ARAN_COMBO_RECHARGE.getValue());
		pw.writeInt(value);

		return pw.getPacket();
	}

	public static byte[] getFollowMessage(String msg) {
		return getGameMessage(msg, (short) 11);
	}

	public static byte[] getGameMessage(String msg, short colour) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.GAME_MESSAGE.getValue());
		pw.writeShort(colour);
		pw.writeMapleAsciiString(msg);

		return pw.getPacket();
	}

	public static byte[] getBuffZoneEffect(int itemId) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.BUFF_ZONE_EFFECT.getValue());
		pw.writeInt(itemId);

		return pw.getPacket();
	}

	public static byte[] getTimeBombAttack() {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.TIME_BOMB_ATTACK.getValue());
		pw.writeInt(0);
		pw.writeInt(0);
		pw.writeInt(0);
		pw.writeInt(10);
		pw.writeInt(6);

		return pw.getPacket();
	}

	public static byte[] moveFollow(Point otherStart, Point myStart, Point otherEnd, List<LifeMovementFragment> moves) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.FOLLOW_MOVE.getValue());
		pw.writeInt(0);
		pw.writePos(otherStart);
		pw.writePos(myStart);
		PacketHelper.serializeMovementList(pw, moves);
		pw.write(17);
		for (int i = 0; i < 8; i++) {
			pw.write(0);
		}
		pw.write(0);
		pw.writePos(otherEnd);
		pw.writePos(otherStart);
		pw.write(new byte[100]);

		return pw.getPacket();
	}

	public static byte[] getFollowMsg(int opcode) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.FOLLOW_MSG.getValue());
		pw.writeInt(opcode);
		pw.writeInt(0);

		return pw.getPacket();
	}

	public static byte[] registerFamiliar(MonsterFamiliar mf) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.REGISTER_FAMILIAR.getValue());
		pw.writeLong(mf.getId());
		mf.writeRegisterPacket(pw, false);
		pw.write(mf.getVitality() >= 3 ? 1 : 0);
		pw.write(0);

		return pw.getPacket();
	}

	public static byte[] createUltimate(int amount) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.CREATE_ULTIMATE.getValue());
		pw.writeInt(amount);

		return pw.getPacket();
	}

	public static byte[] harvestMessage(int oid, int msg) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.HARVEST_MESSAGE.getValue());
		pw.writeInt(oid);
		pw.writeInt(msg);

		return pw.getPacket();
	}

	public static byte[] openBag(int index, int itemId, boolean firstTime) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.OPEN_BAG.getValue());
		pw.writeInt(index);
		pw.writeInt(itemId);
		pw.write(firstTime ? 1 : 0);
		pw.write(0);

		return pw.getPacket();
	}

	public static byte[] dragonBlink(int portalId) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.DRAGON_BLINK.getValue());
		pw.write(portalId);

		return pw.getPacket();
	}

	public static byte[] getPVPIceGage(int score) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.PVP_ICEGAGE.getValue());
		pw.writeInt(score);

		return pw.getPacket();
	}

	public static byte[] skillCooldown(int sid, int time) {
		PacketWriter pw = new PacketWriter();
		
		pw.writeShort(SendPacketOpcode.COOLDOWN.getValue());
		pw.writeInt(1);
		pw.writeInt(sid);
		pw.writeInt(time * 1000);

		return pw.getPacket();
	}

	public static byte[] dropItemFromMapObject(MapleMapItem drop, Point dropfrom, Point dropto, byte mod) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.DROP_ITEM_FROM_MAPOBJECT.getValue());
		pw.write(0); // eDropType
		pw.write(mod); // nEnterType
		pw.writeInt(drop.getObjectId()); // m_mDrop
		pw.write(drop.getMeso() > 0 ? 1 : 0); // bIsMoney
		pw.writeInt(0); // nDropMotionType
		pw.writeInt(0); // nDropSpeed
		pw.writeInt(0); // bNoMove
		pw.writeInt(drop.getItemId()); // fRand
		pw.writeInt(drop.getOwner()); // nInfo
		pw.write(drop.getDropType()); // dwOwnType
		pw.writePos(dropto); //ptDrop x, y
		pw.writeInt(0); // dwSourceID
		if (mod != 2) {
			pw.writePos(dropfrom);
			pw.writeInt(0); // tDelay
		}
		pw.write(0); // bExplosiveDrop
		
		if (drop.getMeso() == 0) {
			PacketHelper.addExpirationTime(pw, drop.getItem().getExpiration());
		}
		
		pw.write(drop.isPlayerDrop() ? 0 : 1); // bByPet
		pw.write(0); // ?
		pw.writeShort(0); // nFallingVY
		pw.write(0); // nFadeInEffect
		pw.write(0); // nMakeType
		pw.writeInt(0); // bCollisionPickup
		pw.write(0); // nItemGrade
		pw.write(0); // bPrepareCollisionPickUp
		return pw.getPacket();
	}

	public static byte[] explodeDrop(int oid) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.REMOVE_ITEM_FROM_MAP.getValue());
		pw.write(4);
		pw.writeInt(oid);
		pw.writeShort(655);

		return pw.getPacket();
	}

	public static byte[] removeItemFromMap(int oid, int animation, int cid) {
		return removeItemFromMap(oid, animation, cid, 0);
	}

	public static byte[] removeItemFromMap(int oid, int animation, int cid, int slot) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.REMOVE_ITEM_FROM_MAP.getValue());
		pw.write(animation);
		pw.writeInt(oid);
		if (animation >= 2) {
			pw.writeInt(cid);
			if (animation == 5) {
				pw.writeInt(slot);
			}
		}
		return pw.getPacket();
	}

	public static byte[] spawnMist(MapleMist mist) {
        PacketWriter pw = new PacketWriter();
        
        pw.writeShort(SendPacketOpcode.SPAWN_MIST.getValue());
        pw.writeInt(mist.getObjectId());

        //pw.write(mist.isMobMist() ? 0 : mist.isPoisonMist());
        pw.write(0);
        pw.writeInt(mist.getOwnerId());
        if (mist.getMobSkill() == null) {
            pw.writeInt(mist.getSourceSkill().getId());
        } else {
            pw.writeInt(mist.getMobSkill().getSkillId());
        }
        pw.write(mist.getSkillLevel());
        pw.writeShort(mist.getSkillDelay());
        pw.writeRect(mist.getBox());
        pw.writeInt(0); 
        pw.writeInt(mist.isShelter() ? 1 : 0);
        pw.writePos(mist.getPosition());
        pw.writeInt(0);
        pw.writeInt(0);
        pw.write(0);
        pw.writeInt(0);
        if (Skill.isFlipAffectedAreaSkill(mist.getSourceSkill().getId()))
            pw.write(0);
        pw.writeInt(0);
        
        return pw.getPacket();
    }

	public static byte[] removeMist(int oid, boolean eruption) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.REMOVE_MIST.getValue());
		pw.writeInt(oid);
		pw.write(eruption ? 1 : 0);

		return pw.getPacket();
	}

	public static byte[] spawnDoor(int oid, Point pos, boolean animation) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.SPAWN_DOOR.getValue());
		pw.write(animation ? 0 : 1);
		pw.writeInt(oid);
		pw.writePos(pos);

		return pw.getPacket();
	}

	public static byte[] removeDoor(int oid, boolean animation) {
		PacketWriter pw = new PacketWriter();
		pw.writeShort(SendPacketOpcode.REMOVE_DOOR.getValue());
		pw.write(animation ? 0 : 1);
		pw.writeInt(oid);

		return pw.getPacket();
	}

	public static byte[] spawnKiteError() {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.SPAWN_KITE_ERROR.getValue());

		return pw.getPacket();
	}

	public static byte[] spawnKite(int oid, int id, Point pos) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.SPAWN_KITE.getValue());
		pw.writeInt(oid);
		pw.writeInt(0);
		pw.writeMapleAsciiString("");
		pw.writeMapleAsciiString("");
		pw.writePos(pos);

		return pw.getPacket();
	}

	public static byte[] destroyKite(int oid, int id, boolean animation) {
		PacketWriter pw = new PacketWriter();
		pw.writeShort(SendPacketOpcode.DESTROY_KITE.getValue());
		pw.write(animation ? 0 : 1);
		pw.writeInt(oid);

		return pw.getPacket();
	}

	public static byte[] spawnMechDoor(MechDoor md, boolean animated) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.MECH_DOOR_SPAWN.getValue());
		pw.write(animated ? 0 : 1);
		pw.writeInt(md.getOwnerId());
		pw.writePos(md.getTruePosition());
		pw.write(md.getId());
		pw.writeInt(md.getPartyId());
		return pw.getPacket();
	}

	public static byte[] removeMechDoor(MechDoor md, boolean animated) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.MECH_DOOR_REMOVE.getValue());
		pw.write(animated ? 0 : 1);
		pw.writeInt(md.getOwnerId());
		pw.write(md.getId());

		return pw.getPacket();
	}

	public static byte[] triggerReactor(MapleReactor reactor, short stance, int dwOwnerID) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.REACTOR_HIT.getValue());
        pw.writeInt(reactor.getObjectId()); // m_mReactor
        pw.write(reactor.getState()); // nState
        pw.writePos(reactor.getTruePosition()); // ptPos.x, ptPos.y
        pw.writeShort(stance); // Should be short, KMST IDA && 176.1 IDA
        pw.write(0); // nProperEventIdx
        pw.write(4); // tStateEnd (time + 100 * value)
        pw.writeInt(dwOwnerID); // KMST && 176.1 IDA stated another Int here, dwOwnerID. 
        
		return pw.getPacket();
	}

	public static byte[] spawnReactor(MapleReactor reactor) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.REACTOR_SPAWN.getValue());
		pw.writeInt(reactor.getObjectId());
		pw.writeInt(reactor.getReactorId());
		pw.write(reactor.getState());
		pw.writePos(reactor.getTruePosition());
		pw.write(reactor.getFacingDirection());
		pw.writeMapleAsciiString(reactor.getName());

		return pw.getPacket();
	}

	public static byte[] destroyReactor(MapleReactor reactor) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.REACTOR_DESTROY.getValue());
		pw.writeInt(reactor.getObjectId());
		pw.write(reactor.getState());
		pw.writePos(reactor.getPosition());

		return pw.getPacket();
	}

	public static byte[] makeExtractor(int cid, String cname, Point pos, int timeLeft, int itemId, int fee) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.SPAWN_EXTRACTOR.getValue());
		pw.writeInt(cid);
		pw.writeMapleAsciiString(cname);
		pw.writeInt(pos.x);
		pw.writeInt(pos.y);
		pw.writeShort(timeLeft);
		pw.writeInt(itemId);
		pw.writeInt(fee);

		return pw.getPacket();
	}

	public static byte[] removeExtractor(int cid) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.REMOVE_EXTRACTOR.getValue());
		pw.writeInt(cid);
		pw.writeInt(1);

		return pw.getPacket();
	}

	public static byte[] rollSnowball(int type, MapleSnowball.MapleSnowballs ball1,
			MapleSnowball.MapleSnowballs ball2) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.ROLL_SNOWBALL.getValue());
		pw.write(type);
		pw.writeInt(ball1 == null ? 0 : ball1.getSnowmanHP() / 75);
		pw.writeInt(ball2 == null ? 0 : ball2.getSnowmanHP() / 75);
		pw.writeShort(ball1 == null ? 0 : ball1.getPosition());
		pw.write(0);
		pw.writeShort(ball2 == null ? 0 : ball2.getPosition());
		pw.write(new byte[11]);

		return pw.getPacket();
	}

	public static byte[] enterSnowBall() {
		return rollSnowball(0, null, null);
	}

	public static byte[] hitSnowBall(int team, int damage, int distance, int delay) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.HIT_SNOWBALL.getValue());
		pw.write(team);
		pw.writeShort(damage);
		pw.write(distance);
		pw.write(delay);

		return pw.getPacket();
	}

	public static byte[] snowballMessage(int team, int message) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.SNOWBALL_MESSAGE.getValue());
		pw.write(team);
		pw.writeInt(message);

		return pw.getPacket();
	}

	public static byte[] leftKnockBack() {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.LEFT_KNOCK_BACK.getValue());

		return pw.getPacket();
	}

	public static byte[] hitCoconut(boolean spawn, int id, int type) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.HIT_COCONUT.getValue());
		pw.writeInt(spawn ? 32768 : id);
		pw.write(spawn ? 0 : type);

		return pw.getPacket();
	}

	public static byte[] coconutScore(int[] coconutscore) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.COCONUT_SCORE.getValue());
		pw.writeShort(coconutscore[0]);
		pw.writeShort(coconutscore[1]);

		return pw.getPacket();
	}

	public static byte[] updateWitchTowerKeys(int keys) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.WITCH_TOWER.getValue());
		pw.write(keys);

		return pw.getPacket();
	}

	public static byte[] showChaosZakumShrine(boolean spawned, int time) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.CHAOS_ZAKUM_SHRINE.getValue());
		pw.write(spawned ? 1 : 0);
		pw.writeInt(time);

		return pw.getPacket();
	}

	public static byte[] showChaosHorntailShrine(boolean spawned, int time) {
		return showHorntailShrine(spawned, time);
	}

	public static byte[] showHorntailShrine(boolean spawned, int time) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.HORNTAIL_SHRINE.getValue());
		pw.write(spawned ? 1 : 0);
		pw.writeInt(time);

		return pw.getPacket();
	}

	public static byte[] getRPSMode(byte mode, int mesos, int selection, int answer) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.RPS_GAME.getValue());
		pw.write(mode);
		switch (mode) {
		case 6:
			if (mesos == -1) {
				break;
			}
			pw.writeInt(mesos);
			break;
		case 8:
			pw.writeInt(9000019);
			break;
		case 11:
			pw.write(selection);
			pw.write(answer);
		}

		return pw.getPacket();
	}

	public static byte[] messengerInvite(String from, int messengerid) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.MESSENGER.getValue());
		pw.write(3);
		pw.writeMapleAsciiString(from);
		pw.write(1);// channel?
		pw.writeInt(messengerid);
		pw.write(0);

		return pw.getPacket();
	}

	public static byte[] addMessengerPlayer(String from, MapleCharacter chr, int position, int channel) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.MESSENGER.getValue());
		pw.write(0);
		pw.write(position);
		PacketHelper.addCharLook(pw, chr, true, false);
		pw.writeMapleAsciiString(from);
		pw.write(channel);
		pw.write(1); // v140
		pw.writeInt(chr.getJob());

		return pw.getPacket();
	}

	public static byte[] removeMessengerPlayer(int position) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.MESSENGER.getValue());
		pw.write(2);
		pw.write(position);

		return pw.getPacket();
	}

	public static byte[] updateMessengerPlayer(String from, MapleCharacter chr, int position, int channel) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.MESSENGER.getValue());
		pw.write(0); // v140.
		pw.write(position);
		PacketHelper.addCharLook(pw, chr, true, false);
		pw.writeMapleAsciiString(from);
		pw.write(channel);
		pw.write(0); // v140.
		pw.writeInt(chr.getJob()); // doubt it's the job, lol. v140.

		return pw.getPacket();
	}

	public static byte[] joinMessenger(int position) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.MESSENGER.getValue());
		pw.write(1);
		pw.write(position);

		return pw.getPacket();
	}

	public static byte[] messengerChat(String charname, String text) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.MESSENGER.getValue());
		pw.write(6);
		pw.writeMapleAsciiString(charname);
		pw.writeMapleAsciiString(text);

		return pw.getPacket();
	}

	public static byte[] messengerNote(String text, int mode, int mode2) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.MESSENGER.getValue());
		pw.write(mode);
		pw.writeMapleAsciiString(text);
		pw.write(mode2);

		return pw.getPacket();
	}

	public static byte[] messengerOpen(byte type, List<MapleCharacter> chars) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.MESSENGER_OPEN.getValue());
		pw.write(type); // 7 in messenger open ui 8 new ui
		if (chars.isEmpty()) {
			pw.writeShort(0);
		}
		for (MapleCharacter chr : chars) {
			pw.write(1);
			pw.writeInt(chr.getId());
			pw.writeInt(0); // likes
			pw.writeLong(0); // some time
			pw.writeMapleAsciiString(chr.getName());
			PacketHelper.addCharLook(pw, chr, true, false);
		}

		return pw.getPacket();
	}

	public static byte[] messengerCharInfo(MapleCharacter chr) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.MESSENGER.getValue());
		pw.write(0x0B);
		pw.writeMapleAsciiString(chr.getName());
		pw.writeInt(chr.getJob());
		pw.writeInt(chr.getFame());
		pw.writeInt(0); // likes
		MapleGuild gs = World.Guild.getGuild(chr.getGuildId());
		pw.writeMapleAsciiString(gs != null ? gs.getName() : "-");
		MapleGuildAlliance alliance = World.Alliance.getAlliance(gs.getAllianceId());
		pw.writeMapleAsciiString(alliance != null ? alliance.getName() : "");
		pw.write(2);

		return pw.getPacket();
	}

	public static byte[] removeFromPackageList(boolean remove, int Package) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.PACKAGE_OPERATION.getValue());
		pw.write(24);
		pw.writeInt(Package);
		pw.write(remove ? 3 : 4);

		return pw.getPacket();
	}

	public static byte[] sendPackageMSG(byte operation, List<MaplePackageActions> packages) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.PACKAGE_OPERATION.getValue());
		pw.write(operation);

		switch (operation) {
		case 9:
			pw.write(1);
			break;
		case 10:
			pw.write(0);
			pw.write(packages.size());

			for (MaplePackageActions dp : packages) {
				pw.writeInt(dp.getPackageId());
				pw.writeAsciiString(dp.getSender(), 13);
				pw.writeInt(dp.getMesos());
				pw.writeLong(PacketHelper.getTime(dp.getSentTime()));
				pw.write(new byte[205]);

				if (dp.getItem() != null) {
					pw.write(1);
					PacketHelper.addItemInfo(pw, dp.getItem());
				} else {
					pw.write(0);
				}
			}
			pw.write(0);
		}

		return pw.getPacket();
	}

	public static byte[] getKeymap(MapleKeyLayout layout) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.KEYMAP.getValue());
		layout.writeData(pw);

		return pw.getPacket();
	}

	public static byte[] petAutoHP(int itemId) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.PET_AUTO_HP.getValue());
		pw.writeInt(itemId);

		return pw.getPacket();
	}

	public static byte[] petAutoMP(int itemId) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.PET_AUTO_MP.getValue());
		pw.writeInt(itemId);

		return pw.getPacket();
	}

	public static byte[] petAutoCure(int itemId) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.PET_AUTO_CURE.getValue());
		pw.writeInt(itemId);

		return pw.getPacket();
	}

	public static byte[] petAutoBuff(int skillId) {
		PacketWriter pw = new PacketWriter();

		// pw.writeShort(SendPacketOpcode.PET_AUTO_BUFF.getValue());
		pw.writeInt(skillId);

		return pw.getPacket();
	}

	public static void addRingInfo(PacketWriter pw, List<MapleRing> rings) {
		pw.write(rings.size());
		for (MapleRing ring : rings) {
			pw.writeInt(1);
			pw.writeLong(ring.getRingId());
			pw.writeLong(ring.getPartnerRingId());
			pw.writeInt(ring.getItemId());
		}
	}

	public static void addMRingInfo(PacketWriter pw, List<MapleRing> rings, MapleCharacter chr) {
		pw.write(rings.size());
		for (MapleRing ring : rings) {
			pw.writeInt(1);
			pw.writeInt(chr.getId());
			pw.writeInt(ring.getPartnerChrId());
			pw.writeInt(ring.getItemId());
		}
	}

	public static byte[] getBuffBar(long millis) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.BUFF_BAR.getValue());
		pw.writeLong(millis);

		return pw.getPacket();
	}

	public static byte[] getBoosterFamiliar(int cid, int familiar, int id) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.BOOSTER_FAMILIAR.getValue());
		pw.writeInt(cid);
		pw.writeInt(familiar);
		pw.writeLong(id);
		pw.write(0);

		return pw.getPacket();
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
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.TARGET_SKILL.getValue());
		List<Integer> skillz = new ArrayList<Integer>();
		for (Skill sk : chr.getSkills().keySet()) {
			if ((sk.canBeLearnedBy(chr.getJob())) && (GameConstants.canSteal(sk))
					&& (!skillz.contains(Integer.valueOf(sk.getId())))) {
						 skillz.add(Integer.valueOf(sk.getId()));
			}
		}
		pw.write(1);
		pw.writeInt(chr.getId());
		pw.writeInt(skillz.isEmpty() ? 2 : 4);
		pw.writeInt(chr.getJob());
		pw.writeInt(skillz.size());
		for (Iterator<Integer> i$ = skillz.iterator(); i$.hasNext();) {
			int i = i$.next().intValue();
			pw.writeInt(i);
		}

		return pw.getPacket();
	}

	public static class InteractionPacket {

		public static byte[] getTradeInvite(MapleCharacter c) {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
			pw.write(PlayerInteractionHandler.Interaction.INVITE_TRADE.action);
			pw.write(4);// was 3
			pw.writeMapleAsciiString(c.getName());
			// pw.writeInt(c.getLevel());
			pw.writeInt(c.getJob());
			return pw.getPacket();
		}

		public static byte[] getTradeMesoSet(byte number, long meso) {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
			pw.write(PlayerInteractionHandler.Interaction.UPDATE_MESO.action);
			pw.write(number);
			pw.writeLong(meso);
			return pw.getPacket();
		}

		public static byte[] getTradeItemAdd(byte number, Item item) {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
			pw.write(PlayerInteractionHandler.Interaction.SET_ITEMS.action);
			pw.write(number);
			pw.write(item.getPosition());
			PacketHelper.addItemInfo(pw, item);

			return pw.getPacket();
		}

		public static byte[] getTradeStart(MapleClient c, MapleTrade trade, byte number) {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
			// pw.write(PlayerInteractionHandler.Interaction.START_TRADE.action);
			// if (number != 0){//13 a0
			//// pw.write(HexTool.getByteArrayFromHexString("13 01 01 03 FE
			// 53 00 00 40 08 00 00 00 E2 7B 00 00 01 E9 50 0F 00 03 62 98 0F 00
			// 04 56 BF 0F 00 05 2A E7 0F 00 07 B7 5B 10 00 08 3D 83 10 00 09 D3
			// D1 10 00 0B 13 01 16 00 11 8C 1F 11 00 12 BF 05 1D 00 13 CB 2C 1D
			// 00 31 40 6F 11 00 32 6B 46 11 00 35 32 5C 19 00 37 20 E2 11 00 FF
			// 03 B6 98 0F 00 05 AE 0A 10 00 09 CC D0 10 00 FF FF 00 00 00 00 13
			// 01 16 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0B 00
			// 4D 6F 6D 6F 6C 6F 76 65 73 4B 48 40 08"));
			// pw.write(19);
			// pw.write(1);
			// PacketHelper.addCharLook(pw, trade.getPartner().getChr(),
			// false);
			// pw.writeMapleAsciiString(trade.getPartner().getChr().getName());
			// pw.writeShort(trade.getPartner().getChr().getJob());
			// }else{
			pw.write(20);
			pw.write(4);
			pw.write(2);
			pw.write(number);

			if (number == 1) {
				pw.write(0);
				PacketHelper.addCharLook(pw, trade.getPartner().getChr(), false, false);
				pw.writeMapleAsciiString(trade.getPartner().getChr().getName());
				pw.writeShort(trade.getPartner().getChr().getJob());
			}
			pw.write(number);
			PacketHelper.addCharLook(pw, c.getPlayer(), false, false);
			pw.writeMapleAsciiString(c.getPlayer().getName());
			pw.writeShort(c.getPlayer().getJob());
			pw.write(255);
			// }
			return pw.getPacket();
		}

		public static byte[] getTradeConfirmation() {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
			pw.write(PlayerInteractionHandler.Interaction.CONFIRM_TRADE.action);

			return pw.getPacket();
		}

		public static byte[] TradeMessage(byte UserSlot, byte message) {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
			pw.write(PlayerInteractionHandler.Interaction.EXIT.action);
			// pw.write(25);//new v141
			pw.write(UserSlot);
			pw.write(message);

			return pw.getPacket();
		}

		public static byte[] getTradeCancel(byte UserSlot, int unsuccessful) {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
			pw.write(PlayerInteractionHandler.Interaction.EXIT.action);
			pw.write(UserSlot);
			pw.write(7);// was2 

			return pw.getPacket();
		}
	}

	public static class NPCPacket {

		public static byte[] spawnNPC(MapleNPC life, boolean show) {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.SPAWN_NPC.getValue());
			pw.writeInt(life.getObjectId());
			pw.writeInt(life.getId());
			getNpcInit(pw, life, show);
			
			return pw.getPacket();
		}
		
		public static byte[] removeNPC(int objectid) {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.REMOVE_NPC.getValue());
			pw.writeInt(objectid);

			return pw.getPacket();
		}
		
		public static byte[] spawnNPCRequestController(MapleNPC life, boolean show) {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.SPAWN_NPC_REQUEST_CONTROLLER.getValue());
			pw.write(1);
			pw.writeInt(life.getObjectId());
			pw.writeInt(life.getId());
			getNpcInit(pw, life, show);

			return pw.getPacket();
		}
		
		public static byte[] removeNPCController(int objectid) {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.SPAWN_NPC_REQUEST_CONTROLLER.getValue());
			pw.write(0);
			pw.writeInt(objectid);

			return pw.getPacket();
		}
		
		private static void getNpcInit(PacketWriter pw, MapleNPC life, boolean show) {
			pw.writeShort(life.getPosition().x);
			pw.writeShort(life.getCy());
			
			pw.write(0); // bMove
			pw.write(life.getF() == 1 ? 0 : 1); // nMoveAction
			pw.writeShort(life.getFh());
			pw.writeShort(life.getRx0());
			pw.writeShort(life.getRx1());
			pw.write(show ? 1 : 0); // bEnabled
			
			pw.writeInt(0); // CNpc::SetPresentItem
			
			pw.write(0); // nPresentTimeState
			pw.writeInt(-1); // tPresent
			pw.writeInt(0); // nNoticeBoardType
			
			/*
			 * if (nNoticeBoardType == 1)		
			 * 	pw.writeInt(0); // nNoticeBoardType
			 *
			 */
			
			pw.writeInt(0);
			pw.writeInt(0);
			
			pw.writeMapleAsciiString("");
			pw.write(0);
		}

		public static byte[] getMapSelection(final int npcid, final String sel) {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.NPC_TALK.getValue());
			pw.write(4);
			pw.writeInt(npcid);
			pw.writeShort(0x11);
			pw.writeInt(npcid == 2083006 ? 1 : 0); // neo city
			pw.writeInt(npcid == 9010022 ? 1 : 0); // dimensional
			pw.writeMapleAsciiString(sel);

			return pw.getPacket();
		}

		public static byte[] toggleNPCShow(int oid, boolean hide) {
			PacketWriter pw = new PacketWriter();
			pw.writeShort(SendPacketOpcode.NPC_TOGGLE_VISIBLE.getValue());
			pw.writeInt(oid);
			pw.write(hide ? 0 : 1);
			return pw.getPacket();
		}

		public static byte[] setNPCSpecialAction(int oid, String action) {
			PacketWriter pw = new PacketWriter();
			pw.writeShort(SendPacketOpcode.NPC_SET_SPECIAL_ACTION.getValue());
			pw.writeInt(oid);
			pw.writeMapleAsciiString(action);
			pw.writeInt(0); // unknown yet
			pw.write(0); // unknown yet
			return pw.getPacket();
		}

		public static byte[] NPCSpecialAction(int oid, int x, int y) {
			PacketWriter pw = new PacketWriter();
			pw.writeShort(SendPacketOpcode.NPC_UPDATE_LIMITED_INFO.getValue());
			pw.writeInt(oid);
			pw.writeInt(x);
			pw.writeInt(y);
			return pw.getPacket();
		}

		public static byte[] setNPCScriptable() {
			PacketWriter pw = new PacketWriter();
			pw.writeShort(SendPacketOpcode.NPC_SCRIPTABLE.getValue());

			List<Pair<Integer, String>> npcs = new LinkedList<Pair<Integer, String>>();
			npcs.add(new Pair<>(9070006,
					"Why...why has this happened to me? My knightly honor... My knightly pride..."));
			npcs.add(new Pair<>(9000021, "Are you enjoying the event?"));

			pw.write(npcs.size());
			for (Pair<Integer, String> s : npcs) {
				pw.writeInt(s.getLeft());
				pw.writeMapleAsciiString(s.getRight());
				pw.writeInt(0);
				// pw.writeInt(Integer.MAX_VALUE);
				pw.write(0);
			}
			return pw.getPacket();
		}
		
		/**
		 * 
		 * @param talk
		 * @return
		 */
		public static byte[] getNPCTalk(NPCTalk talk) {
			PacketWriter pw = new PacketWriter();
			
			pw.writeShort(SendPacketOpcode.NPC_TALK.getValue());
			pw.write(talk.getType());
			pw.writeInt(talk.getNpcID());
			pw.write(0); // bool?
			// pw.writeInt(0);
			
			pw.write(talk.getMsg());
			pw.write(talk.getParam());
			pw.write(talk.getColor()); // 0 = blue; 1 = brown
			switch(talk.getMsg()) {
				case 0: // OnSay
					if ((talk.getParam() & 4) != 0)
						pw.writeInt(talk.getNpcIDD());
					pw.writeMapleAsciiString(talk.getText());
					pw.write(talk.getPrev());
					pw.write(talk.getNext());
					pw.writeInt(talk.getSeconds());
					break;
				case 1: // OnSayImage
					pw.write(talk.getArgs().length);
					for(Object obj : talk.getArgs()) {
						pw.writeMapleAsciiString((String) obj);
					}
					break;
				case 2: // OnAskYesNo
					if((talk.getParam() & 4) != 0)
						pw.writeInt(talk.getNpcIDD());
					pw.writeMapleAsciiString(talk.getText());
					break;
				case 3: // OnAskText
					if((talk.getParam() & 4) != 0)
						pw.writeInt(talk.getNpcIDD());
					pw.writeMapleAsciiString(talk.getText());
					pw.writeMapleAsciiString(talk.getDef());
					pw.writeShort(talk.getMin());
					pw.writeShort(talk.getMax());
					break;
				case 4: // OnAskNumber
					pw.writeMapleAsciiString(talk.getText());
					pw.writeInt(talk.getAmount());
					pw.writeInt(talk.getMin());
					pw.writeInt(talk.getMax());
					break;
				case 5: // OnAskMenu
					if ((talk.getParam() & 4) != 0)
						pw.writeInt(talk.getNpcIDD());
					pw.writeMapleAsciiString(talk.getText());
					break;
				case 6: // OnInitialQuiz
					pw.write(0); // setting this to 1 will close the window.
					pw.writeMapleAsciiString(talk.getText());
					pw.writeMapleAsciiString(talk.getDef());
					pw.writeMapleAsciiString(talk.getHint());
					pw.writeInt(talk.getMin());
					pw.writeInt(talk.getMax());
					pw.writeInt(talk.getSeconds());
				case 7: // OnInitialSpeedQuiz
				case 8: // OnICQuiz
				case 9: // OnAskAvatar
					pw.write(0); // bAngelicBuster
					pw.write(0); // bZeroBeta
					pw.writeMapleAsciiString(talk.getText());
					pw.write(talk.getArgs().length);
					for(Object i : talk.getArgs()) {
						pw.writeInt((int) i);
					}
					break;
				case 10: // OnAskAndroid
				case 12: // OnAskPet
				case 13: // OnAskPetAll
				case 14: // OnAskActionPetEvolution
				case 16: // OnAskYesNo
					if((talk.getParam() & 4) != 0)
						pw.writeInt(talk.getNpcIDD());
					pw.writeMapleAsciiString(talk.getText());
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
					pw.writeShort(0); // background index
					pw.writeMapleAsciiString("");
					pw.writeMapleAsciiString("");
					pw.writeShort(0); // column
					pw.writeShort(0); // line
					pw.writeShort(0); // font size
					pw.writeShort(0); // top font margin
					break;
				case 40: // OnAskUserSurvey
					pw.writeInt(0); // talk type
					pw.write(1); // show exit button
					pw.writeMapleAsciiString(talk.getText());
					break;
				case 42: // OnAskMixHair
				case 43: // OnAskMixHairExZero
				case 44: // OnAskCustomMixHair
				case 45: // OnAskCustomMixHairAndProb
					pw.write(0); // bAngelicBuster
					pw.writeInt(0); // left percentage
					pw.writeInt(0); // right percentage
					pw.writeMapleAsciiString(talk.getText());
					break;
				case 46: // OnAskMixHairNew
				case 47: // OnAskMixHairNewExZero
				case 49: // OnAskScreenShinningStarMsg
					break;
				case 51: // OnAskNumberUseKeyPad
					pw.writeInt(0); // result
					break;
				case 52: // OnSpinOffGuitarRhythmGame
				case 53: // OnGhostParkEnter
					pw.writeInt(0); // size
					break;
				default: throw new UnsupportedOperationException("This message id has not been implemented.");
			}
			
			return pw.getPacket();
		}

		public static byte[] getEnglishQuiz(int npc, byte type, int diffNPC, String talk, String endBytes) {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.NPC_TALK.getValue());
			pw.write(4);
			pw.writeInt(npc);
			pw.write(10); // not sure
			pw.write(type);
			if ((type & 0x4) != 0) {
				pw.writeInt(diffNPC);
			}
			pw.writeMapleAsciiString(talk);
			pw.write(HexTool.getByteArrayFromHexString(endBytes));

			return pw.getPacket();
		}

		public static byte[] getSlideMenu(int npcid, int type, int lasticon, String sel) {
			// Types: 0 - map selection 1 - neo city map selection 2 - korean
			// map selection 3 - tele rock map selection 4 - dojo buff selection
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.NPC_TALK.getValue());
			pw.write(4); // slide menu
			pw.writeInt(npcid);
			pw.write(0);
			pw.write(18);
			pw.write(0);
			pw.write(0);
			
			pw.writeInt(type); // menu type
			pw.writeInt(type == 0 ? lasticon : 0); // last icon on menu
			pw.writeMapleAsciiString(sel);

			return pw.getPacket();
		}

		public static byte[] getSelfTalkText(String text) {
			PacketWriter pw = new PacketWriter();
			pw.writeShort(SendPacketOpcode.NPC_TALK.getValue());
			pw.write(3); // nSpeakerTypeID
			pw.writeInt(0); //nSpeakerTemplateID
			pw.write(1); 
                        pw.writeInt(0);
			pw.write(0);//nMsgType
			pw.write(0x11); //bParam (0x11 is NO_ESC [0x1] and SMP_NPC_REPLACED_BY_USER_LEFT [0x10])
			pw.write(0); //eColor
			pw.writeMapleAsciiString(text);
			pw.write(0);//bPrev
			pw.write(1); //bNext
                        pw.writeInt(0); //tWait
			return pw.getPacket();
		}

		public static byte[] getNPCTutoEffect(String effect) {
			PacketWriter pw = new PacketWriter();
			pw.writeShort(SendPacketOpcode.NPC_TALK.getValue());
			pw.write(3);
			pw.writeInt(0);
			pw.write(0);
			pw.write(1);
			pw.write(257);
			pw.write(0);
			pw.writeMapleAsciiString(effect);
			return pw.getPacket();
		}

		public static byte[] getCutSceneSkip() {
			PacketWriter pw = new PacketWriter();
			pw.writeShort(SendPacketOpcode.NPC_TALK.getValue());
			pw.write(3);
			pw.writeInt(0);
			pw.write(1);
			pw.writeInt(0);
			
			pw.write(2);
			pw.write(5);
			pw.writeInt(9010000); // Maple administrator
			pw.writeMapleAsciiString("Would you like to skip the tutorial cutscenes?");
			return pw.getPacket();
		}

		public static byte[] getDemonSelection() {
			PacketWriter pw = new PacketWriter();
			pw.writeShort(SendPacketOpcode.NPC_TALK.getValue());
			pw.write(3);
			pw.writeInt(0);
			
			pw.write(1);			
			pw.writeInt(2159311); // npc
			
			pw.write(0x17);
			pw.write(1);
			pw.write(1);
			pw.write(0);
			
			pw.write(new byte[8]);
			return pw.getPacket();
		}

		public static byte[] getEvanTutorial(String data) {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.NPC_TALK.getValue());

			pw.write(8);
			pw.writeInt(0);
			pw.write(1);
			pw.write(1);
			pw.write(1);
			pw.writeMapleAsciiString(data);

			return pw.getPacket();
		}

		public static byte[] getNPCShop(int sid, MapleShop shop, MapleClient c) {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.OPEN_NPC_SHOP.getValue());
			
			pw.write(0);
			/*
			 * if ( CInPacket::Decode1(iPacket) )
      		 * 	v62 = CInPacket::Decode4(v68);
			 */
			pw.writeInt(0); // m_nSelectNpcItemID
			pw.writeInt(shop.getNpcId()); // m_dwNpcTemplateID
			pw.writeInt(0); // m_nStarCoin
			pw.writeInt(0); // m_nShopVerNo
			PacketHelper.addShopInfo(pw, shop, c);

			return pw.getPacket();
		}

		public static byte[] confirmShopTransaction(byte code, MapleShop shop, MapleClient c, int indexBought) {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.CONFIRM_SHOP_TRANSACTION.getValue());
			pw.write(code);
			if (code == 5) {
				pw.writeInt(0); // m_nSelectNpcItemID
				pw.writeInt(shop.getNpcId()); // m_dwNpcTemplateID
				pw.writeInt(0); // m_nStarCoin
				pw.writeInt(0); // m_nShopVerNo
				PacketHelper.addShopInfo(pw, shop, c);
			} else {
				pw.write(indexBought >= 0 ? 1 : 0);
				if (indexBought >= 0) {
					pw.writeInt(indexBought);
				} else {
					pw.write(0);
					pw.writeInt(0);
				}
			}

			return pw.getPacket();
		}

		public static byte[] getStorage(int npcId, byte slots, Collection<Item> items, long meso) {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
			pw.write(22);
			pw.writeInt(npcId);
			pw.write(slots);
			pw.writeShort(126);
			pw.writeShort(0);
			pw.writeInt(0);
			pw.writeLong(meso);
			PacketHelper.addStorageItems(pw, items);
			pw.write(new byte[4]);// Might be too much

			return pw.getPacket();
		}

		public static byte[] getStorageFull() {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
			pw.write(17);

			return pw.getPacket();
		}

		public static byte[] mesoStorage(byte slots, long meso) {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
			pw.write(19);
			pw.write(slots);
			pw.writeShort(2);
			pw.writeShort(0);
			pw.writeInt(0);
			pw.writeLong(meso);

			return pw.getPacket();
		}

		public static byte[] arrangeStorage(byte slots, Collection<Item> items, boolean changed) {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
			pw.write(15);
			pw.write(slots);
			pw.write(124);
			pw.write(new byte[10]);
			pw.write(items.size());
			for (Item item : items) {
				PacketHelper.addItemInfo(pw, item);
			}
			pw.write(0);
			return pw.getPacket();
		}

		public static byte[] storeStorage(byte slots, MapleInventoryType type, Collection<Item> items) {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
			pw.write(13);
			pw.write(slots);
			pw.writeShort(type.getBitfieldEncoding());
			pw.writeShort(0);
			pw.writeInt(0);
			pw.write(items.size());
			for (Item item : items) {
				PacketHelper.addItemInfo(pw, item);
			}
			return pw.getPacket();
		}

		public static byte[] takeOutStorage(byte slots, MapleInventoryType type, Collection<Item> items) {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
			pw.write(9);
			pw.write(slots);
			pw.writeShort(type.getBitfieldEncoding());
			pw.writeShort(0);
			pw.writeInt(0);
			pw.write(items.size());
			for (Item item : items) {
				PacketHelper.addItemInfo(pw, item);
			}
			return pw.getPacket();
		}
	}

	public static class SummonPacket {

		public static byte[] spawnSummon(MapleSummon summon, boolean animated) {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.SPAWN_SUMMON.getValue());
			pw.writeInt(summon.getOwnerId());
			pw.writeInt(summon.getObjectId());
			pw.writeInt(summon.getSkill()); // nSkillID
			pw.write(summon.getOwnerLevel() - 1); // nCharLevel
			pw.write(summon.getSkillLevel()); // nSLV (skill level)
			pw.writePos(summon.getPosition()); // nX, nY
			pw.write((summon.getSkill() == 32111006) || (summon.getSkill() == 33101005) ? 5 : 4); // nMoveAction
			
			if ((summon.getSkill() == 35121003) && (summon.getOwner().getMap() != null)) { // Giant Robot SG-88
				pw.writeShort(summon.getOwner().getMap().getFootholds().findBelow(summon.getPosition()).getId());
			} else {
				pw.writeShort(0); // nCurFoothold
			}
			
			pw.write(summon.getMovementType().getValue()); // nMoveAbility
			pw.write(summon.getSummonType()); // nAssistType
			pw.write(animated ? 1 : 0); // nEnterType
			pw.writeInt(0); // dwMobID
			pw.write(1); // bFlyMob
			pw.write(0); // bBeforeFirstAttack
			pw.writeInt(0); // nLookID
			pw.writeInt(0); // nBulletID
			
			boolean mirroredTarget = summon.getSkill() == 4341006 && summon.getOwner() != null;
			pw.write(mirroredTarget);
			if (mirroredTarget) {
				PacketHelper.addCharLook(pw, summon.getOwner(), true, false);
			} else if (summon.getSkill() == 35111002) { // Rock 'n Shock
				pw.write(0); // nTeslaCoilState
			} else if (summon.getSkill() == 42111003) { // Kishin Shoukan
				pw.writeShort(0);
				pw.writeShort(0);
				pw.writeShort(0);
				pw.writeShort(0);
			}
			
			pw.write(0); // bJaguarActive
			pw.writeInt(0); // tSummonTerm
			pw.write(0); // bAttackActive

			return pw.getPacket();
		}

		public static byte[] removeSummon(int ownerId, int objId) {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.REMOVE_SUMMON.getValue());
			pw.writeInt(ownerId);
			
			pw.writeInt(objId); // dwSummonedID
			pw.write(10); // nLeaveType

			return pw.getPacket();
		}

		public static byte[] removeSummon(MapleSummon summon, boolean animated) {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.REMOVE_SUMMON.getValue());
			pw.writeInt(summon.getOwnerId());
			pw.writeInt(summon.getObjectId());
			if (animated) {
				switch (summon.getSkill()) {
				case 35121003:
					pw.write(10);
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
					pw.write(5);
					break;
				default:
					pw.write(4);
					break;
				}
			} else {
				pw.write(1);
			}

			return pw.getPacket();
		}
		
		public static byte[] moveSummon(int cid, int oid, Point startPos, List<LifeMovementFragment> moves) {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.MOVE_SUMMON.getValue());
			pw.writeInt(cid);
			pw.writeInt(oid);
			pw.writeInt(0);
			pw.writePos(startPos);
			pw.writeInt(0);
			PacketHelper.serializeMovementList(pw, moves);

			return pw.getPacket();
		}

		public static byte[] summonAttack(int cid, int summonSkillId, byte animation,
				List<Pair<Integer, Integer>> allDamage, int level, boolean darkFlare) {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.SUMMON_ATTACK.getValue());
			pw.writeInt(cid);
			
			pw.writeInt(summonSkillId); // pSummoned
			pw.write(level - 1); // nCharLevel
			pw.write(animation); // bLeft
			pw.write(allDamage.size()); // nMobCount
			for (Pair attackEntry : allDamage) {
				pw.writeInt(((Integer) attackEntry.left).intValue());
				pw.write(7); // nAttackCount
				pw.writeInt(((Integer) attackEntry.right).intValue());
			}
			pw.write(darkFlare ? 1 : 0); // bCounterAttack
			pw.write(0); // bNoAction
			pw.writeShort(0); // pMob
			pw.writeShort(0); // (tCur + this) delay per attack?

			return pw.getPacket();
		}

		public static byte[] pvpSummonAttack(int cid, int playerLevel, int oid, int animation, Point pos,
				List<AttackPair> attack) {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.PVP_SUMMON.getValue());
			pw.writeInt(cid);
			pw.writeInt(oid);
			pw.write(playerLevel);
			pw.write(animation);
			pw.writePos(pos);
			pw.writeInt(0);
			pw.write(attack.size());
			for (AttackPair p : attack) {
				pw.writeInt(p.objectid);
				pw.writePos(p.point);
				pw.write(p.attack.size());
				pw.write(0);
				for (Pair atk : p.attack) {
					pw.writeInt(((Integer) atk.left).intValue());
				}
			}

			return pw.getPacket();
		}

		public static byte[] summonSkill(int cid, int summonSkillId, int newStance) {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.SUMMON_SKILL.getValue());
			pw.writeInt(cid);
			pw.writeInt(summonSkillId);
			pw.write(newStance);

			return pw.getPacket();
		}

		public static byte[] damageSummon(int cid, int summonSkillId, int damage, int unkByte, int monsterIdFrom) {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.DAMAGE_SUMMON.getValue());
			pw.writeInt(cid);
			
			pw.writeInt(summonSkillId);
			pw.write(unkByte); // nAttackIdx
			pw.writeInt(damage); // nDamage
			pw.writeInt(monsterIdFrom); // dwMobTemplateID
			pw.write(0); // bLeft

			return pw.getPacket();
		}
	}

	public static class UIPacket {

		public static byte[] getDirectionStatus(boolean enable) {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.DIRECTION_STATUS.getValue());
			pw.write(enable ? 1 : 0);

			return pw.getPacket();
		}

		public static byte[] openUI(int type) {
			PacketWriter pw = new PacketWriter(3);

			pw.writeShort(SendPacketOpcode.OPEN_UI.getValue());
			pw.writeInt(type); // 175.1

			return pw.getPacket();
		}

		public static byte[] sendRepairWindow(int npc) {
			PacketWriter pw = new PacketWriter(10);

			pw.writeShort(SendPacketOpcode.OPEN_UI_OPTION.getValue());
			pw.writeInt(33);
			pw.writeInt(npc);
			pw.writeInt(0);// new143

			return pw.getPacket();
		}

		public static byte[] sendJewelCraftWindow(int npc) {
			PacketWriter pw = new PacketWriter(10);

			pw.writeShort(SendPacketOpcode.OPEN_UI_OPTION.getValue());
			pw.writeInt(104);
			pw.writeInt(npc);
			pw.writeInt(0);// new143

			return pw.getPacket();
		}

		public static byte[] startAzwan(int npc) {
			PacketWriter pw = new PacketWriter(10);
			pw.writeShort(SendPacketOpcode.OPEN_UI_OPTION.getValue());
			pw.writeInt(70);
			pw.writeInt(npc);
			pw.writeInt(0);// new143
			return pw.getPacket();
		}

		public static byte[] openUIOption(int type, int npc) {
			PacketWriter pw = new PacketWriter(10);
			pw.writeShort(SendPacketOpcode.OPEN_UI_OPTION.getValue());
			pw.writeInt(type);
			pw.writeInt(npc);
			return pw.getPacket();
		}

		public static byte[] sendDojoResult(int points) {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.OPEN_UI_OPTION.getValue());
			pw.writeInt(0x48);
			pw.writeInt(points);

			return pw.getPacket();
		}

		public static byte[] sendAzwanResult() {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.OPEN_UI_OPTION.getValue());
			pw.writeInt(0x45);
			pw.writeInt(0);

			return pw.getPacket();
		}

		public static byte[] DublStart(boolean dark) {
			PacketWriter pw = new PacketWriter();
			pw.writeShort(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
			pw.write(0x28);
			pw.write(dark ? 1 : 0);

			return pw.getPacket();
		}

		public static byte[] DublStartAutoMove() {
			PacketWriter pw = new PacketWriter();
			pw.writeShort(SendPacketOpcode.MOVE_SCREEN.getValue());
			pw.write(3);
			pw.writeInt(2);

			return pw.getPacket();
		}

		public static byte[] IntroLock(boolean enable) {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.INTRO_LOCK.getValue());
			pw.write(enable ? 1 : 0);
			pw.writeInt(0);

			return pw.getPacket();
		}

		public static byte[] IntroEnableUI(int enable) {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.INTRO_ENABLE_UI.getValue());
			pw.write(enable > 0 ? 1 : 0);
			if (enable > 0) {
				pw.write(enable);
				pw.writeShort(0);
			}
			pw.write(0);
			return pw.getPacket();
		}

		public static byte[] IntroDisableUI(boolean enable) {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.INTRO_DISABLE_UI.getValue());
			pw.write(enable ? 1 : 0);

			return pw.getPacket();
		}

		public static byte[] summonHelper(boolean summon) {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.SUMMON_HINT.getValue());
			pw.write(summon ? 1 : 0);

			return pw.getPacket();
		}

		public static byte[] summonMessage(int type) {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.SUMMON_HINT_MSG.getValue());
			pw.write(1);
			pw.writeInt(type);
			pw.writeInt(7000);

			return pw.getPacket();
		}

		public static byte[] summonMessage(String message) {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.SUMMON_HINT_MSG.getValue());
			pw.write(0);
			pw.writeMapleAsciiString(message);
			pw.writeInt(200);
			pw.writeShort(0);
			pw.writeInt(10000);

			return pw.getPacket();
		}

		public static byte[] getDirectionInfo(int type, int value) {
			PacketWriter pw = new PacketWriter();
			pw.writeShort(SendPacketOpcode.DIRECTION_INFO.getValue());
			pw.write((byte) type);
			pw.writeInt(value);
			return pw.getPacket();
		}

		public static byte[] getDirectionInfo(String data, int value, int x, int y, int a, int b) {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.DIRECTION_INFO.getValue());
			pw.write(2);
			pw.writeMapleAsciiString(data);
			pw.writeInt(value);
			pw.writeInt(x);
			pw.writeInt(y);
			pw.write(a);
			if (a > 0) {
				pw.writeInt(0);
			}
			pw.write(b);
			if (b > 1) {
				pw.writeInt(0);
				pw.write(a);
				pw.write(b);
			}

			return pw.getPacket();
		}

		public static byte[] getDirectionEffect(String data, int value, int x, int y) {
			return getDirectionEffect(data, value, x, y, 0);
		}

		public static byte[] getDirectionEffect(String data, int value, int x, int y, int npc) {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.DIRECTION_INFO.getValue());
			pw.write(2);
			pw.writeMapleAsciiString(data);
			pw.writeInt(value);
			pw.writeInt(x);
			pw.writeInt(y);
			pw.write(1);
			pw.writeInt(0);
			pw.write(1);
			pw.writeInt(npc);
			pw.write(1);
			pw.write(0);

			return pw.getPacket();
		}

		public static byte[] getDirectionInfoNew(byte x, int value) {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.DIRECTION_INFO.getValue());
			pw.write(5);
			pw.write(x);
			pw.writeInt(value);
			if (x == 0) {
				pw.writeInt(value);
				pw.writeInt(value);
			}

			return pw.getPacket();
		}

		public static byte[] moveScreen(int x) {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.MOVE_SCREEN_X.getValue());
			pw.writeInt(x);
			pw.writeInt(0);
			pw.writeInt(0);

			return pw.getPacket();
		}

		public static byte[] screenDown() {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.MOVE_SCREEN_DOWN.getValue());

			return pw.getPacket();
		}

		public static byte[] reissueMedal(int itemId, int type) {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.REISSUE_MEDAL.getValue());
			pw.write(type);
			pw.writeInt(itemId);

			return pw.getPacket();
		}

		public static byte[] playMovie(String data, boolean show) {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.PLAY_MOVIE.getValue());
			pw.writeMapleAsciiString(data);
			pw.write(show ? 1 : 0);

			return pw.getPacket();
		}

		public static byte[] setRedLeafStatus(int joejoe, int hermoninny, int littledragon, int ika) {
			// packet made to set status
			// should remove it and make a handler for it, it's a recv opcode
			/*
			 * slea: E2 9F 72 00 5D 0A 73 01 E2 9F 72 00 04 00 00 00 00 00 00 00
			 * 75 96 8F 00 55 01 00 00 76 96 8F 00 00 00 00 00 77 96 8F 00 00 00
			 * 00 00 78 96 8F 00 00 00 00 00
			 */
			PacketWriter pw = new PacketWriter();

			// pw.writeShort();
			pw.writeInt(7512034); // no idea
			pw.writeInt(24316509); // no idea
			pw.writeInt(7512034); // no idea
			pw.writeInt(4); // no idea
			pw.writeInt(0); // no idea
			pw.writeInt(9410165); // joe joe
			pw.writeInt(joejoe); // amount points added
			pw.writeInt(9410166); // hermoninny
			pw.writeInt(hermoninny); // amount points added
			pw.writeInt(9410167); // little dragon
			pw.writeInt(littledragon); // amount points added
			pw.writeInt(9410168); // ika
			pw.writeInt(ika); // amount points added

			return pw.getPacket();
		}

		public static byte[] sendRedLeaf(int points, boolean viewonly) {
			/*
			 * slea: 73 00 00 00 0A 00 00 00 01
			 */
			PacketWriter pw = new PacketWriter(10);

			pw.writeShort(SendPacketOpcode.OPEN_UI_OPTION.getValue());
			pw.writeInt(0x73);
			pw.writeInt(points);
			pw.write(viewonly ? 1 : 0); // if view only, then complete button
											// is disabled

			return pw.getPacket();
		}
	}

	public static class EffectPacket {

		public static byte[] showForeignEffect(int effect) {
			return showForeignEffect(-1, effect);
		}

		public static byte[] showForeignEffect(int cid, int effect) {
			PacketWriter pw = new PacketWriter();

			if (cid == -1) {
				pw.writeShort(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
			} else {
				pw.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
				pw.writeInt(cid);
			}
			pw.write(effect);

			return pw.getPacket();
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
			PacketWriter pw = new PacketWriter();

			if (cid == -1) {
				pw.writeShort(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
			} else {
				pw.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
				pw.writeInt(cid);
			}
			pw.write(3);
			pw.writeInt(effectid);
			pw.writeInt(effectid2);
			pw.writeInt(skillid);
			pw.write(level);
			pw.write(0);
			pw.write(new byte[100]);

			return pw.getPacket();
		}

		public static byte[] useCharm(byte charmsleft, byte daysleft, boolean safetyCharm) {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
			pw.write(8);
			pw.write(safetyCharm ? 1 : 0);
			pw.write(charmsleft);
			pw.write(daysleft);
			if (!safetyCharm) {
				pw.writeInt(0);
			}

			return pw.getPacket();
		}

		public static byte[] Mulung_DojoUp2() {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
			pw.write(10);

			return pw.getPacket();
		}

		public static byte[] showOwnHpHealed(int amount) {
			return showHpHealed(-1, amount);
		}

		public static byte[] showHpHealed(int cid, int amount) {
			PacketWriter pw = new PacketWriter();

			if (cid == -1) {
				pw.writeShort(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
			} else {
				pw.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
				pw.writeInt(cid);
			}
			pw.write(30);
			pw.writeInt(amount);

			return pw.getPacket();
		}

		public static byte[] showRewardItemAnimation(int itemId, String effect) {
			return showRewardItemAnimation(itemId, effect, -1);
		}

		public static byte[] showRewardItemAnimation(int itemId, String effect, int from_playerid) {
			PacketWriter pw = new PacketWriter();

			if (from_playerid == -1) {
				pw.writeShort(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
			} else {
				pw.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
				pw.writeInt(from_playerid);
			}
			pw.write(17);
			pw.writeInt(itemId);
			pw.write((effect != null) && (effect.length() > 0) ? 1 : 0);
			if ((effect != null) && (effect.length() > 0)) {
				pw.writeMapleAsciiString(effect);
			}

			return pw.getPacket();
		}

		public static byte[] showCashItemEffect(int itemId) {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
			pw.write(23);
			pw.writeInt(itemId);

			return pw.getPacket();
		}

		public static byte[] ItemMaker_Success() {
			return ItemMaker_Success_3rdParty(-1);
		}

		public static byte[] ItemMaker_Success_3rdParty(int from_playerid) {
			PacketWriter pw = new PacketWriter();

			if (from_playerid == -1) {
				pw.writeShort(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
			} else {
				pw.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
				pw.writeInt(from_playerid);
			}
			pw.write(19);
			pw.writeInt(0);

			return pw.getPacket();
		}

		public static byte[] useWheel(byte charmsleft) {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
			pw.write(24);
			pw.write(charmsleft);

			return pw.getPacket();
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
			PacketWriter pw = new PacketWriter();

			if (cid == -1) {
				pw.writeShort(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
			} else {
				pw.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
				pw.writeInt(cid);
			}
			pw.write(effectid);
			pw.writeInt(skillid);
			pw.write(playerLevel - 1);
			if ((effectid == 2) && (skillid == 31111003)) {
				pw.writeInt(0);
			}
			pw.write(skillLevel);
			if ((direction != 3) || (skillid == 1320006) || (skillid == 30001062) || (skillid == 30001061)) {
				pw.write(direction);
			}

			if (skillid == 30001062) {
				pw.writeInt(0);
			}
			pw.write(new byte[10]);

			return pw.getPacket();
		}

		public static byte[] ShowWZEffect(String data) {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
			pw.write(0x26); // updated.
			pw.writeMapleAsciiString(data);
			pw.write(0); // bool
			pw.writeInt(0); // bUpgrade
			pw.writeInt(4); // nRet

			return pw.getPacket();
		}

		public static byte[] showOwnCraftingEffect(String effect, byte direction, int time, int mode) {
			return showCraftingEffect(-1, effect, direction, time, mode);
		}

		public static byte[] showCraftingEffect(int cid, String effect, byte direction, int time, int mode) {
			PacketWriter pw = new PacketWriter();

			if (cid == -1) {
				pw.writeShort(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
			} else {
				pw.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
				pw.writeInt(cid);
			}
			pw.write(34); // v140
			pw.writeMapleAsciiString(effect);
			pw.write(direction);
			pw.writeInt(time);
			pw.writeInt(mode);
			if (mode == 2) {
				pw.writeInt(0);
			}

			return pw.getPacket();
		}

		public static byte[] TutInstructionalBalloon(String data) {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
			pw.write(25);// was 26 in v140
			pw.writeMapleAsciiString(data);
			pw.writeInt(1);

			return pw.getPacket();
		}

		public static byte[] showOwnPetLevelUp(byte index) {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
			pw.write(6);
			pw.write(0);
			pw.write(index);

			return pw.getPacket();
		}

		public static byte[] showOwnChampionEffect() {
			return showChampionEffect(-1);
		}

		public static byte[] showChampionEffect(int from_playerid) {
			PacketWriter pw = new PacketWriter();

			if (from_playerid == -1) {
				pw.writeShort(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
			} else {
				pw.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
				pw.writeInt(from_playerid);
			}
			pw.write(34);
			pw.writeInt(30000);

			return pw.getPacket();
		}

		public static byte[] updateDeathCount(int deathCount) {
			PacketWriter pw = new PacketWriter();

			pw.writeShort(SendPacketOpcode.DEATH_COUNT.getValue());
			pw.writeInt(deathCount);

			return pw.getPacket();
		}
	}

	public static byte[] showWeirdEffect(String effect, int itemId) {
		final PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
		pw.write(0x20);
		pw.writeMapleAsciiString(effect);
		pw.write(1);
		pw.writeInt(0);// weird high number is it will keep showing it lol
		pw.writeInt(2);
		pw.writeInt(itemId);
		return pw.getPacket();
	}

	public static byte[] showWeirdEffect(int chrId, String effect, int itemId) {
		final PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
		pw.writeInt(chrId);
		pw.write(0x20);
		pw.writeMapleAsciiString(effect);
		pw.write(1);
		pw.writeInt(0);// weird high number is it will keep showing it lol
		pw.writeInt(2);// this makes it read the itemId
		pw.writeInt(itemId);
		return pw.getPacket();
	}

	public static byte[] enchantResult(int result) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.STRENGTHEN_UI.getValue());
		pw.writeInt(result);// 0=fail/1=sucess/2=idk/3=shows stats
		return pw.getPacket();
	}

	public static byte[] sendSealedBox(short slot, int itemId, List<Integer> items) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.SEALED_BOX.getValue());
		pw.writeShort(slot);
		pw.writeInt(itemId);
		pw.writeInt(items.size());
		for (int item : items) {
			pw.writeInt(item);
		}

		return pw.getPacket();
	}

	public static byte[] unsealBox(int reward) {
		PacketWriter pw = new PacketWriter();

		pw.writeShort(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
		pw.write(0x31);
		pw.write(1);
		pw.writeInt(reward);
		pw.writeInt(1);

		return pw.getPacket();
	}
	
	public static byte[] finalAttack(int skill, int finalattack, int wepType, int oid) {
        PacketWriter pw = new PacketWriter();
        
        pw.writeShort(SendPacketOpcode.FINAL_ATTACK.getValue()); 
        pw.writeInt(skill);
        pw.writeInt(finalattack);
        pw.writeInt(wepType); 
        pw.writeInt(0); //delay
        pw.writeInt(oid);
        pw.writeInt(0); 
        if (finalattack == 101000102) {
            pw.write(0); //bLeft
            pw.writeShort(0);
            pw.writeShort(0);
        }
        return pw.getPacket();
    }
    
    public static byte[] swordEnergy(short energy) {
        
        PacketWriter pw = new PacketWriter();
        
        pw.writeShort(SendPacketOpcode.SWORD_ENERGY.getValue());
        pw.writeShort(energy);
        
        return pw.getPacket();
    }
    
    public static byte[] ignition(int skillid, int mobid, Point pos) {
        
        PacketWriter pw = new PacketWriter();
        
        pw.writeShort(SendPacketOpcode.IGNITION.getValue());
        pw.writeInt(skillid);
        pw.writeInt(pos.x);
        pw.writeInt(pos.y);
        pw.writeInt(mobid);
        pw.writeInt(5);// unk
        
        return pw.getPacket();
    }
    
    public static byte[] openURL(String url) {
        
        PacketWriter pw = new PacketWriter();
        
        pw.writeShort(SendPacketOpcode.OPEN_URL.getValue());
        pw.write(1); // if 0, open set nexon url
        pw.writeMapleAsciiString(url);
        
        return pw.getPacket();
    }
    
    public static byte[] elementFlame() {
        
        PacketWriter pw = new PacketWriter();
        
        pw.writeShort(SendPacketOpcode.ELEMENT_FLAME.getValue());
        
        return pw.getPacket();
    }
}
