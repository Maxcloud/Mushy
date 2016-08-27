package handling.channel.handler;

import java.awt.Point;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleStat;
import client.MonsterStatus;
import client.MonsterStatusEffect;
import client.PlayerStats;
import client.Skill;
import client.SkillFactory;
import client.SkillMacro;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import handling.channel.ChannelServer;
import handling.world.World;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MaplePortal;
import server.MapleStatEffect;
import server.Timer;
import server.Timer.CloneTimer;
import server.events.MapleEvent;
import server.events.MapleEventType;
import server.events.MapleSnowball;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MobAttackInfo;
import server.life.MobSkill;
import server.life.MobSkillFactory;
import server.maps.FieldLimitType;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.movement.LifeMovementFragment;
import server.quest.MapleQuest;
import tools.AttackPair;
import tools.FileoutputUtil;
import tools.Pair;
import tools.Randomizer;
import tools.data.LittleEndianAccessor;
import tools.packet.CField;
import tools.packet.CSPacket;
import tools.packet.CWvsContext;
import tools.packet.JobPacket;
import tools.packet.JobPacket.AngelicPacket;
import tools.packet.MobPacket;

public class PlayerHandler {

	public static int isFinisher(int skillid) {
		switch (skillid) {
		case 1111003:
			return 1;
		case 1111005:
			return 2;
		case 11111002:
			return 1;
		case 11111003:
			return 2;
		}
		return 0;
	}

	public static void ChangeSkillMacro(LittleEndianAccessor slea, MapleCharacter chr) {
		int num = slea.readByte();

		for (int i = 0; i < num; i++) {
			String name = slea.readMapleAsciiString();
			int shout = slea.readByte();
			int skill1 = slea.readInt();
			int skill2 = slea.readInt();
			int skill3 = slea.readInt();

			SkillMacro macro = new SkillMacro(skill1, skill2, skill3, name, shout, i);
			chr.updateMacros(i, macro);
		}
	}

	public static void ChangeKeymap(LittleEndianAccessor slea, MapleCharacter chr) {
		if ((slea.available() > 8L) && (chr != null)) {
			slea.skip(4);
			int numChanges = slea.readInt();

			for (int i = 0; i < numChanges; i++) {
				int key = slea.readInt();
				byte type = slea.readByte();
				int action = slea.readInt();
				if ((type == 1) && (action >= 1000)) {
					Skill skil = SkillFactory.getSkill(action);
					if ((skil != null) && (((!skil.isFourthJob()) && (!skil.isBeginnerSkill()) && (skil.isInvisible())
							&& (chr.getSkillLevel(skil) <= 0)) || (GameConstants.isLinkedAttackSkill(action))
							|| (action % 10000 < 1000))) {
						continue;
					}
				}
				chr.changeKeybinding(key, type, action);
			}
		} else if (chr != null) {
			int type = slea.readInt();
			int data = slea.readInt();
			switch (type) {
			case 1:
				if (data <= 0) {
					chr.getQuestRemove(MapleQuest.getInstance(GameConstants.HP_ITEM));
				} else {
					chr.getQuestNAdd(MapleQuest.getInstance(GameConstants.HP_ITEM)).setCustomData(String.valueOf(data));
				}
				break;
			case 2:
				if (data <= 0) {
					chr.getQuestRemove(MapleQuest.getInstance(GameConstants.MP_ITEM));
				} else {
					chr.getQuestNAdd(MapleQuest.getInstance(GameConstants.MP_ITEM)).setCustomData(String.valueOf(data));
				}
				break;
			}
		}
	}

	public static void ChangePetBuff(LittleEndianAccessor slea, MapleCharacter chr) {
		slea.readInt(); // 0
		int skill = slea.readInt();
		slea.readByte(); // 0
		if (skill <= 0) {
			chr.getQuestRemove(MapleQuest.getInstance(GameConstants.BUFF_ITEM));
		} else {
			chr.getQuestNAdd(MapleQuest.getInstance(GameConstants.BUFF_ITEM)).setCustomData(String.valueOf(skill));
		}
	}

	public static void UseTitle(int itemId, MapleClient c, MapleCharacter chr) {
		if ((chr == null) || (chr.getMap() == null)) {
			return;
		}
		Item toUse = chr.getInventory(MapleInventoryType.SETUP).findById(itemId);
		if (toUse == null) {
			return;
		}
		if (itemId <= 0) {
			chr.getQuestRemove(MapleQuest.getInstance(124000));
		} else {
			chr.getQuestNAdd(MapleQuest.getInstance(124000)).setCustomData(String.valueOf(itemId));
		}
		chr.getMap().broadcastMessage(chr, CField.showTitle(chr.getId(), itemId), false);
		c.getSession().write(CWvsContext.enableActions());
	}

	public static void AngelicChange(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
		if ((chr == null) || (chr.getMap() == null)) {
			return;
		}
		int transform = slea.readInt();
		// System.out.println("transform id " + transform);
		if (transform == 5010094) {
			// System.out.println("acvivate");
			chr.getMap().broadcastMessage(chr, CField.showAngelicBuster(chr.getId(), transform), false);
			chr.getMap().broadcastMessage(chr, CField.updateCharLook(chr, transform == 5010094), false);
			c.getSession().write(CWvsContext.enableActions());
			// System.out.println("acvivate done");
		} else {
			// System.out.println("deacvivate");
			// chr.getMap().broadcastMessage(chr,
			// CField.showAngelicBuster(chr.getId(), transform), false);
			// chr.getMap().broadcastMessage(chr, CField.updateCharLook(chr,
			// transform == 5010093), false);
			// c.getSession().write(CWvsContext.enableActions());
		}
	}

	public static void DressUpTime(LittleEndianAccessor slea, final MapleClient c) {
		byte type = slea.readByte();
		// System.out.println("abtype " + type);
		if (type == 1) {
			// PlayerHandler.AngelicChange(slea, c, chr);
			if (GameConstants.isAngelicBuster(c.getPlayer().getJob())) {
				c.getSession().write(JobPacket.AngelicPacket.DressUpTime(type));
				c.getSession().write(JobPacket.AngelicPacket.updateDress(5010094, c.getPlayer()));
				// }
			} else {
				c.getSession().write(CWvsContext.enableActions());
				// return;
			}
		}
	}
	// if (type != 1) {// ||
	// !GameConstants.isAngelicBuster(c.getPlayer().getJob())
	// c.getSession().write(CWvsContext.enableActions());
	// return;
	// }
	// c.getSession().write(JobPacket.AngelicPacket.DressUpTime(type));
	// }

	public static void absorbingDF(LittleEndianAccessor slea, final MapleClient c) {
		int size = slea.readInt();
		int room = 0;
		byte unk = 0;
		int sn;
		for (int i = 0; i < size; i++) {
			room = GameConstants.isDemonAvenger(c.getPlayer().getJob()) || c.getPlayer().getJob() == 212 ? 0
					: slea.readInt();
			unk = slea.readByte();
			sn = slea.readInt();
			if (GameConstants.isDemonSlayer(c.getPlayer().getJob())) {
				// c.getPlayer().addMP(c.getPlayer().getStat().getForce(room));
			}
			if (GameConstants.isAngelicBuster(c.getPlayer().getJob())) {
				boolean rand = Randomizer.isSuccess(80);
				if (sn > 0) {
					if (rand) {
						c.getSession().write(JobPacket.AngelicPacket.SoulSeekerRegen(c.getPlayer(), sn));
					}
				}
			}
			if ((GameConstants.isDemonAvenger(c.getPlayer().getJob())) && slea.available() >= 8) {
				// c.getPlayer().getMap().broadcastMessage(MainPacketCreator.ShieldChacingRe(c.getPlayer().getId(),
				// slea.readInt(), slea.readInt(), unk,
				// c.getPlayer().getKeyValue2("schacing")));
			}
			if (c.getPlayer().getJob() == 212) {
				// c.getPlayer().getMap().broadcastMessage(MainPacketCreator.MegidoFlameRe(c.getPlayer().getId(),
				// unk, slea.readInt()));
			}
		}
	}

	public static void UseChair(final int itemId, final MapleClient c, final MapleCharacter chr) {
		if (chr == null || chr.getMap() == null) {
			return;
		}
		final Item toUse = chr.getInventory(MapleInventoryType.SETUP).findById(itemId);
		if (toUse == null) {
			return;
		}
		if (GameConstants.isFishingMap(chr.getMapId()) && itemId == 3011000) {
			chr.startFishingTask();
		}
		chr.setChair(itemId);
		chr.getMap().broadcastMessage(chr, CField.showChair(chr.getId(), itemId), false);
		c.getSession().write(CWvsContext.enableActions());
	}

	public static void CancelChair(short id, MapleClient c, MapleCharacter chr) {
		if (id == -1) {
			chr.cancelFishingTask();
			chr.setChair(0);
			c.getSession().write(CField.cancelChair(-1));
			if (chr.getMap() != null) {
				chr.getMap().broadcastMessage(chr, CField.showChair(chr.getId(), 0), false);
			}
		} else {
			chr.setChair(id);
			c.getSession().write(CField.cancelChair(id));
		}
	}

	public static void TrockAddMap(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
		byte addrem = slea.readByte();
		byte vip = slea.readByte();

		if (vip == 1) {
			if (addrem == 0) {
				chr.deleteFromRegRocks(slea.readInt());
			} else if (addrem == 1) {
				if (!FieldLimitType.VipRock.check(chr.getMap().getFieldLimit())) {
					chr.addRegRockMap();
				} else {
					chr.dropMessage(1, "This map is not available to enter for the list.");
				}
			}
		} else if (vip == 2) {
			if (addrem == 0) {
				chr.deleteFromRocks(slea.readInt());
			} else if (addrem == 1) {
				if (!FieldLimitType.VipRock.check(chr.getMap().getFieldLimit())) {
					chr.addRockMap();
				} else {
					chr.dropMessage(1, "This map is not available to enter for the list.");
				}
			}
		} else if (vip == 3 || vip == 5) {
			if (addrem == 0) {
				chr.deleteFromHyperRocks(slea.readInt());
			} else if (addrem == 1) {
				if (!FieldLimitType.VipRock.check(chr.getMap().getFieldLimit())) {
					chr.addHyperRockMap();
				} else {
					chr.dropMessage(1, "This map is not available to enter for the list.");
				}
			}
		}
		c.getSession().write(CSPacket.OnMapTransferResult(chr, vip, addrem == 0));
	}

	public static void CharInfoRequest(int objectid, MapleClient c, MapleCharacter chr) {
		if (c.getPlayer() == null || c.getPlayer().getMap() == null) {
			return;
		}
		MapleCharacter player = c.getPlayer().getMap().getCharacterById(objectid);
		c.getSession().write(CWvsContext.enableActions());
		if (player != null/* && (!player.isGM() || c.getPlayer().isGM()) */) {
			c.getSession().write(CWvsContext.charInfo(player, c.getPlayer().getId() == objectid));
		}
	}

	public static void AdminCommand(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
		if (!c.getPlayer().isGM()) {
			return;
		}
		byte mode = slea.readByte();
		String victim;
		MapleCharacter target;
		switch (mode) {
		case 0x00: // Level1~Level8 & Package1~Package2
			int[][] toSpawn = MapleItemInformationProvider.getInstance().getSummonMobs(slea.readInt());
			for (int[] toSpawnChild : toSpawn) {
				if (Randomizer.nextInt(101) <= toSpawnChild[1]) {
					c.getPlayer().getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(toSpawnChild[0]),
							c.getPlayer().getPosition());
				}
			}
			c.getSession().write(CWvsContext.enableActions());
			break;
		case 0x01: { // /d (inv)
			byte type = slea.readByte();
			MapleInventory in = c.getPlayer().getInventory(MapleInventoryType.getByType(type));
			for (byte i = 0; i < in.getSlotLimit(); i++) {
				if (in.getItem(i) != null) {
					MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.getByType(type), i,
							in.getItem(i).getQuantity(), false);
				}
				return;
			}
			break;
		}
		case 0x02: // Exp
			c.getPlayer().setExp(slea.readInt());
			break;
		case 0x03: // /ban <name>
			victim = slea.readMapleAsciiString();
			String reason = victim + " permanent banned by " + c.getPlayer().getName();
			target = c.getChannelServer().getPlayerStorage().getCharacterByName(victim);
			if (target != null) {
				String readableTargetName = MapleCharacter.makeMapleReadable(target.getName());
				String ip = target.getClient().getSession().getRemoteAddress().toString().split(":")[0];
				reason += readableTargetName + " (IP: " + ip + ")";
				target.ban(reason, false, false);
				target.sendPolice("You have been blocked by #bMapleGM #kfor the HACK reason.");
				c.getSession().write(CField.getGMEffect(4, (byte) 0));
			} else if (MapleCharacter.ban(victim, reason, false)) {
				c.getSession().write(CField.getGMEffect(4, (byte) 0));
			} else {
				c.getSession().write(CField.getGMEffect(6, (byte) 1));
			}
			break;
		case 0x04: // /block <name> <duration (in days)>
					// <HACK/BOT/AD/HARASS/CURSE/SCAM/MISCONDUCT/SELL/ICASH/TEMP/GM/IPROGRAM/MEGAPHONE>
			victim = slea.readMapleAsciiString();
			int type = slea.readByte(); // reason
			int duration = slea.readInt();
			String description = slea.readMapleAsciiString();
			reason = c.getPlayer().getName() + " used /ban to ban";
			target = c.getChannelServer().getPlayerStorage().getCharacterByName(victim);
			if (target != null) {
				String readableTargetName = MapleCharacter.makeMapleReadable(target.getName());
				String ip = target.getClient().getSession().getRemoteAddress().toString().split(":")[0];
				reason += readableTargetName + " (IP: " + ip + ")";
				if (duration == -1) {
					target.ban(description + " " + reason, true);
				} else {
					Calendar cal = Calendar.getInstance();
					cal.add(Calendar.DATE, duration);
					target.tempban(description, cal, type, false);
					target.sendPolice(duration, reason, 6000);
				}
				c.getSession().write(CField.getGMEffect(4, (byte) 0));
			} else if (MapleCharacter.ban(victim, reason, false)) {
				c.getSession().write(CField.getGMEffect(4, (byte) 0));
			} else {
				c.getSession().write(CField.getGMEffect(6, (byte) 1));
			}
			break;
		case 0x10: // /h, information by vana (and tele mode f1) ... hide
					// ofcourse
			if (slea.readByte() > 0) {
				SkillFactory.getSkill(9101004).getEffect(1).applyTo(c.getPlayer());
			} else {
				c.getPlayer().dispelBuff(9101004);
			}
			break;
		case 0x11: // Entering a map
			switch (slea.readByte()) {
			case 0:// /u
				StringBuilder sb = new StringBuilder("USERS ON THIS MAP: ");
				for (MapleCharacter mc : c.getPlayer().getMap().getCharacters()) {
					sb.append(mc.getName());
					sb.append(" ");
				}
				c.getPlayer().dropMessage(5, sb.toString());
				break;
			case 12:// /uclip and entering a map
				break;
			}
			break;
		case 0x12: // Send
			victim = slea.readMapleAsciiString();
			int mapId = slea.readInt();
			c.getChannelServer().getPlayerStorage().getCharacterByName(victim)
					.changeMap(c.getChannelServer().getMapFactory().getMap(mapId));
			break;
		case 0x15: // Kill
			int mobToKill = slea.readInt();
			int amount = slea.readInt();
			List<MapleMapObject> monsterx = c.getPlayer().getMap().getMapObjectsInRange(c.getPlayer().getPosition(),
					Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER));
			for (int x = 0; x < amount; x++) {
				MapleMonster monster = (MapleMonster) monsterx.get(x);
				if (monster.getId() == mobToKill) {
					c.getPlayer().getMap().killMonster(monster, c.getPlayer(), false, false, (byte) 1);
				}
			}
			break;
		case 0x16: // Questreset
			MapleQuest.getInstance(slea.readShort()).forfeit(c.getPlayer());
			break;
		case 0x17: // Summon
			int mobId = slea.readInt();
			int quantity = slea.readInt();
			for (int i = 0; i < quantity; i++) {
				c.getPlayer().getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(mobId),
						c.getPlayer().getPosition());
			}
			break;
		case 0x18: // Maple & Mobhp
			int mobHp = slea.readInt();
			c.getPlayer().dropMessage(5, "Monsters HP");
			List<MapleMapObject> monsters = c.getPlayer().getMap().getMapObjectsInRange(c.getPlayer().getPosition(),
					Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER));
			for (MapleMapObject mobs : monsters) {
				MapleMonster monster = (MapleMonster) mobs;
				if (monster.getId() == mobHp) {
					c.getPlayer().dropMessage(5, monster.getName() + ": " + monster.getHp());
				}
			}
			break;
		case 0x1E: // Warn
			victim = slea.readMapleAsciiString();
			String message = slea.readMapleAsciiString();
			target = c.getChannelServer().getPlayerStorage().getCharacterByName(victim);
			if (target != null) {
				target.getClient().getSession().write(CWvsContext.broadcastMsg(1, message));
				c.getSession().write(CField.getGMEffect(0x1E, (byte) 1));
			} else {
				c.getSession().write(CField.getGMEffect(0x1E, (byte) 0));
			}
			break;
		case 0x24:// /Artifact Ranking
			break;
		case 0x77: // Testing purpose
			if (slea.available() == 4) {
				System.out.println(slea.readInt());
			} else if (slea.available() == 2) {
				System.out.println(slea.readShort());
			}
			break;
		default:
			System.out.println("New GM packet encountered (MODE : " + mode + ": " + slea.toString());
			break;
		}
	}

	public static void TakeDamage(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
		slea.skip(4);
		slea.skip(4); // update tick
		byte type = slea.readByte();
		slea.skip(1);
		int damage = slea.readInt();
		slea.skip(2);
		boolean isDeadlyAttack = false;
		boolean pPhysical = false;
		int oid = 0;
		int monsteridfrom = 0;
		int fake = 0;
		int mpattack = 0;
		int skillid = 0;
		int pID = 0;
		int pDMG = 0;
		byte direction = 0;
		byte pType = 0;
		Point pPos = new Point(0, 0);
		MapleMonster attacker = null;

		if (GameConstants.isXenon(chr.getJob())) {
			if (chr.getSkillLevel(36110004) > 0) {
				chr.getMap().broadcastMessage(JobPacket.XenonPacket.EazisSystem(chr.getId(), oid));
			}
		}

		if ((chr == null) || (chr.isHidden()) || (chr.getMap() == null)) {
			c.getSession().write(CWvsContext.enableActions());
			return;
		}
		if ((chr.isGM()) && (chr.isInvincible())) {
			c.getSession().write(CWvsContext.enableActions());
			return;
		}
		PlayerStats stats = chr.getStat();
		if ((type != -2) && (type != -3) && (type != -4)) {
			monsteridfrom = slea.readInt();
			oid = slea.readInt();
			attacker = chr.getMap().getMonsterByOid(oid);
			direction = slea.readByte();
			if ((attacker == null) || (attacker.getId() != monsteridfrom) || (attacker.getLinkCID() > 0)
					|| (attacker.isFake()) || (attacker.getStats().isFriendly())) {
				return;
			}
			if (chr.getMapId() == 915000300) {
				MapleMap to = chr.getClient().getChannelServer().getMapFactory().getMap(915000200);
				chr.dropMessage(5, "You've been found out! Retreat!");
				chr.changeMap(to, to.getPortal(1));
				return;
			}
			if (attacker.getId() == 9300166 && chr.getMapId() == 910025200) {
				int rocksLost = Randomizer.rand(1, 5);
				while (chr.itemQuantity(4031469) < rocksLost) {
					rocksLost--;
				}
				if (rocksLost > 0) {
					chr.gainItem(4031469, -rocksLost);
					Item toDrop = MapleItemInformationProvider.getInstance().getEquipById(4031469);
					for (int i = 0; i < rocksLost; i++) {
						chr.getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), toDrop, c.getPlayer().getPosition(),
								true, true);
					}
				}
			}
			if ((type != -1) && (damage > 0)) {
				MobAttackInfo attackInfo = attacker.getStats().getMobAttack(type);
				if (attackInfo != null) {
					if ((attackInfo.isElement) && (stats.TER > 0) && (Randomizer.nextInt(100) < stats.TER)) {
						System.out.println(new StringBuilder().append("Avoided ER from mob id: ").append(monsteridfrom)
								.toString());
						return;
					}
					if (attackInfo.isDeadlyAttack()) {
						isDeadlyAttack = true;
						mpattack = stats.getMp() - 1;
					} else {
						mpattack += attackInfo.getMpBurn();
					}
					MobSkill skill = MobSkillFactory.getMobSkill(attackInfo.getDiseaseSkill(),
							attackInfo.getDiseaseLevel());
					if ((skill != null) && ((damage == -1) || (damage > 0))) {
						skill.applyEffect(chr, attacker, false);
					}
					attacker.setMp(attacker.getMp() - attackInfo.getMpCon());
				}
			}
			skillid = slea.readInt();
			pDMG = slea.readInt();
			byte defType = slea.readByte();
			slea.skip(1);
			if (defType == 1) {
				Skill bx = SkillFactory.getSkill(31110008);
				int bof = chr.getTotalSkillLevel(bx);
				if (bof > 0) {
					MapleStatEffect eff = bx.getEffect(bof);
					if (Randomizer.nextInt(100) <= eff.getX()) {
						chr.handleForceGain(oid, 31110008, eff.getZ());
					}
				}
			}
			if (skillid != 0) {
				pPhysical = slea.readByte() > 0;
				pID = slea.readInt();
				pType = slea.readByte();
				slea.skip(4);
				pPos = slea.readPos();
			}
		}
		if (damage == -1) {
			fake = 4020002 + (chr.getJob() / 10 - 40) * 100000;
			if ((fake != 4120002) && (fake != 4220002)) {
				fake = 4120002;
			}
			if ((type == -1) && (chr.getJob() == 122) && (attacker != null)
					&& (chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -10) != null)
					&& (chr.getTotalSkillLevel(1220006) > 0)) {
				MapleStatEffect eff = SkillFactory.getSkill(1220006).getEffect(chr.getTotalSkillLevel(1220006));
				attacker.applyStatus(chr,
						new MonsterStatusEffect(MonsterStatus.STUN, Integer.valueOf(1), 1220006, null, false), false,
						eff.getDuration(), true, eff);
				fake = 1220006;
			}

			if (chr.getTotalSkillLevel(fake) <= 0) {
				return;
			}
		} else if ((damage < -1) || (damage > 200000)) {
			c.getSession().write(CWvsContext.enableActions());
			return;
		}
		if ((chr.getStat().dodgeChance > 0) && (Randomizer.nextInt(100) < chr.getStat().dodgeChance)) {
			c.getSession().write(CField.EffectPacket.showForeignEffect(20));
			return;
		}
		if ((pPhysical) && (skillid == 1201007) && (chr.getTotalSkillLevel(1201007) > 0)) {
			damage -= pDMG;
			if (damage > 0) {
				MapleStatEffect eff = SkillFactory.getSkill(1201007).getEffect(chr.getTotalSkillLevel(1201007));
				long enemyDMG = Math.min(damage * (eff.getY() / 100), attacker.getMobMaxHp() / 2L);
				if (enemyDMG > pDMG) {
					enemyDMG = pDMG;
				}
				if (enemyDMG > 1000L) {
					enemyDMG = 1000L;
				}
				attacker.damage(chr, enemyDMG, true, 1201007);
			} else {
				damage = 1;
			}
		}
		Pair modify = chr.modifyDamageTaken(damage, attacker);
		damage = ((Double) modify.left).intValue();
		if (damage > 0) {

			if (chr.getBuffedValue(MapleBuffStat.MORPH) != null) {
				chr.cancelMorphs();
			}

			boolean mpAttack = (chr.getBuffedValue(MapleBuffStat.MECH_CHANGE) != null)
					&& (chr.getBuffSource(MapleBuffStat.MECH_CHANGE) != 35121005);
			if (chr.getBuffedValue(MapleBuffStat.MAGIC_GUARD) != null) {
				int hploss = 0;
				int mploss = 0;
				if (isDeadlyAttack) {
					if (stats.getHp() > 1) {
						hploss = stats.getHp() - 1;
					}
					if (stats.getMp() > 1) {
						mploss = stats.getMp() - 1;
					}
					if (chr.getBuffedValue(MapleBuffStat.INFINITY) != null) {
						mploss = 0;
					}
					chr.addMPHP(-hploss, -mploss);
				} else {
					mploss = (int) (damage * (chr.getBuffedValue(MapleBuffStat.MAGIC_GUARD).doubleValue() / 100.0D))
							+ mpattack;
					hploss = damage - mploss;
					if (chr.getBuffedValue(MapleBuffStat.INFINITY) != null) {
						mploss = 0;
					} else if (mploss > stats.getMp()) {
						mploss = stats.getMp();
						hploss = damage - mploss + mpattack;
					}
					chr.addMPHP(-hploss, -mploss);
				}
			} else if (chr.getStat().mesoGuardMeso > 0.0D) {
				int mesoloss = (int) (damage * (chr.getStat().mesoGuardMeso / 100.0D));
				if (chr.getMeso() < mesoloss) {
					chr.gainMeso(-chr.getMeso(), false);
					chr.cancelBuffStats(new MapleBuffStat[] { MapleBuffStat.MESOGUARD });
				} else {
					chr.gainMeso(-mesoloss, false);
				}
				if ((isDeadlyAttack) && (stats.getMp() > 1)) {
					mpattack = stats.getMp() - 1;
				}
				chr.addMPHP(-damage, -mpattack);
			} else if (isDeadlyAttack) {
				chr.addMPHP(stats.getHp() > 1 ? -(stats.getHp() - 1) : 0,
						(stats.getMp() > 1) && (!mpAttack) ? -(stats.getMp() - 1) : 0);
			} else {
				chr.addMPHP(-damage, mpAttack ? 0 : -mpattack);
			}
			if ((chr.inPVP()) && (chr.getStat().getHPPercent() <= 20)) {
				chr.getStat();
				SkillFactory.getSkill(PlayerStats.getSkillByJob(93, chr.getJob())).getEffect(1).applyTo(chr);
			}
		}
		byte offset = 0;
		int offset_d = 0;
		if (slea.available() == 1L) {
			offset = slea.readByte();
			if ((offset == 1) && (slea.available() >= 4L)) {
				offset_d = slea.readInt();
			}
			if ((offset < 0) || (offset > 2)) {
				offset = 0;
			}
		}

		chr.getMap().broadcastMessage(chr, CField.damagePlayer(chr.getId(), type, damage, monsteridfrom, direction,
				skillid, pDMG, pPhysical, pID, pType, pPos, offset, offset_d, fake), false);
	}

	public static void AranCombo(MapleClient c, MapleCharacter chr, int toAdd) {
		if ((chr != null) && (chr.getJob() >= 2000) && (chr.getJob() <= 2112)) {
			short combo = chr.getCombo();
			long curr = System.currentTimeMillis();

			if ((combo > 0) && (curr - chr.getLastCombo() > 7000L)) {
				combo = 0;
			}
			combo = (short) Math.min(30000, combo + toAdd);
			chr.setLastCombo(curr);
			chr.setCombo(combo);

			c.getSession().write(CField.updateCombo(combo));

			switch (combo) {
			case 10:
			case 20:
			case 30:
			case 40:
			case 50:
			case 60:
			case 70:
			case 80:
			case 90:
			case 100:
				if (chr.getSkillLevel(21000000) < combo / 10) {
					break;
				}
				SkillFactory.getSkill(21000000).getEffect(combo / 10).applyComboBuff(chr, combo);
				break;
			}
		}
	}

	public static void UseItemEffect(int itemId, MapleClient c, MapleCharacter chr) {
		Item toUse = chr
				.getInventory(
						(itemId == 4290001) || (itemId == 4290000) ? MapleInventoryType.ETC : MapleInventoryType.CASH)
				.findById(itemId);
		if ((toUse == null) || (toUse.getItemId() != itemId) || (toUse.getQuantity() < 1)) {
			c.getSession().write(CWvsContext.enableActions());
			return;
		}
		if (itemId != 5510000) {
			chr.setItemEffect(itemId);
		}
		chr.getMap().broadcastMessage(chr, CField.itemEffect(chr.getId(), itemId), false);
	}

	public static void CancelItemEffect(int id, MapleCharacter chr) {
		chr.cancelEffect(MapleItemInformationProvider.getInstance().getItemEffect(-id), false, -1L);
	}

	public static void CancelBuffHandler(int sourceid, MapleCharacter chr) {
		if ((chr == null) || (chr.getMap() == null)) {
			return;
		}
		Skill skill = SkillFactory.getSkill(sourceid);
		switch (sourceid) {
		// // case 33001001: //��Ծ� ���̵�
		// // chr.send(CWvsContext.cancelJaguarRiding());
		// // break;
		// case 13101024:
		// chr.getMap().broadcastMessage(chr, CField.skillCancel(chr, sourceid),
		// false);
		// break;
		// case 13111023:
		// case 13120008:
		// chr.cancelAllBuffs();
		// chr.cancelBuffStats(new MapleBuffStat[]{MapleBuffStat.ALBATROSS});
		// chr.cancelBuffStats(new MapleBuffStat[]{MapleBuffStat.INDIE_PAD});
		// chr.cancelBuffStats(new MapleBuffStat[]{MapleBuffStat.HP_BOOST});
		// chr.cancelBuffStats(new MapleBuffStat[]{MapleBuffStat.ATTACK_SPEED});
		// chr.cancelBuffStats(new
		// MapleBuffStat[]{MapleBuffStat.CRITICAL_PERCENT_UP});
		// chr.getMap().broadcastMessage(chr, CField.skillCancel(chr, sourceid),
		// false);

		// break;
		case 4341052:
			chr.getStat().setHp(0, chr);
			chr.updateSingleStat(MapleStat.HP, 0);
			chr.getClient().getSession().write(CWvsContext.enableActions());
			break;
		}

		if (skill.isChargeSkill()) {
			chr.setKeyDownSkill_Time(0L);
			chr.getMap().broadcastMessage(chr, CField.skillCancel(chr, sourceid), false);
		} else {
			chr.cancelEffect(skill.getEffect(1), false, -1L);
		}
	}

	public static void CancelMech(LittleEndianAccessor slea, MapleCharacter chr) {
		if (chr == null) {
			return;
		}
		int sourceid = slea.readInt();
		if ((sourceid % 10000 < 1000) && (SkillFactory.getSkill(sourceid) == null)) {
			sourceid += 1000;
		}
		Skill skill = SkillFactory.getSkill(sourceid);
		if (skill == null) {
			return;
		}
		if (skill.isChargeSkill()) {
			chr.setKeyDownSkill_Time(0L);
			chr.getMap().broadcastMessage(chr, CField.skillCancel(chr, sourceid), false);
		} else {

			chr.cancelEffect(skill.getEffect(slea.readByte()), false, -1L);
		}
	}

	public static void QuickSlot(LittleEndianAccessor slea, MapleCharacter chr) {
		if ((slea.available() == 32L) && (chr != null)) {
			StringBuilder ret = new StringBuilder();
			for (int i = 0; i < 8; i++) {
				ret.append(slea.readInt()).append(",");
			}
			ret.deleteCharAt(ret.length() - 1);
			chr.getQuestNAdd(MapleQuest.getInstance(123000)).setCustomData(ret.toString());
		}
	}

	public static void SkillEffect(LittleEndianAccessor slea, MapleCharacter chr) {
		int skillId = slea.readInt();
		if (skillId >= 91000000 && skillId < 100000000) {
			chr.getClient().getSession().write(CWvsContext.enableActions());
			return;
		}
		byte level = slea.readByte();
		short direction = slea.readShort();
		byte unk = slea.readByte();

		Skill skill = SkillFactory.getSkill(GameConstants.getLinkedAttackSkill(skillId));
		if ((chr == null) || (skill == null) || (chr.getMap() == null)) {
			return;
		}
		int skilllevel_serv = chr.getTotalSkillLevel(skill);

		if ((skilllevel_serv > 0) && (skilllevel_serv == level) && ((skillId == 33101005) || (skill.isChargeSkill()))) {
			chr.setKeyDownSkill_Time(System.currentTimeMillis());
			if (skillId == 33101005) {
				chr.setLinkMid(slea.readInt(), 0);
			}
			chr.getMap().broadcastMessage(chr, CField.skillEffect(chr, skillId, level, direction, unk), false);
		}
	}

	public static void SpecialMove(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
		if ((chr == null) || (chr.hasBlockedInventory()) || (chr.getMap() == null) || (slea.available() < 9L)) {
			c.getSession().write(CWvsContext.enableActions());
			return;
		}
		slea.skip(4);
		int skillid = slea.readInt();
		if (skillid >= 91000000 && skillid < 100000000) {
			c.getSession().write(CWvsContext.enableActions());
			return;
		}
		if (skillid == 23111008) {
			skillid += Randomizer.nextInt(2);
		}
		int xy1 = 0;
		int xy2 = 0;
		if (skillid == 65111100) {
			xy1 = slea.readShort();
			xy2 = slea.readShort();
			int soulnum = slea.readByte();
			int scheck = 0;
			int scheck2 = 0;
			if (soulnum == 1) {
				scheck = slea.readInt();
			} else if (soulnum == 2) {
				scheck = slea.readInt();
				scheck2 = slea.readInt();
			}
			c.getSession().write(JobPacket.AngelicPacket.SoulSeeker(chr, skillid, soulnum, scheck, scheck2));
			c.getSession().write(JobPacket.AngelicPacket.unlockSkill());
			c.getSession().write(JobPacket.AngelicPacket.RechargeEffect());
			c.getSession().write(CWvsContext.enableActions());
			return;
		}
		if (skillid >= 100000000) {
			slea.readByte(); // zero
		}
		int skillLevel = slea.readByte();
		// System.err.println(skillLevel);
		Skill skill = SkillFactory.getSkill(skillid);
		if ((skill == null)
				|| ((GameConstants.isAngel(skillid)) && (chr.getStat().equippedSummon % 10000 != skillid % 10000))
				|| ((chr.inPVP()) && (skill.isPVPDisabled()))) {
			c.getSession().write(CWvsContext.enableActions());
			return;
		}
		int levelCheckSkill = 0;
		if ((GameConstants.isPhantom(chr.getJob())) && (!GameConstants.isPhantom(skillid / 10000))) {
			int skillJob = skillid / 10000;
			if (skillJob % 100 == 0) {
				levelCheckSkill = 24001001;
			} else if (skillJob % 10 == 0) {
				levelCheckSkill = 24101001;
			} else if (skillJob % 10 == 1) {
				levelCheckSkill = 24111001;
			} else {
				levelCheckSkill = 24121001;
			}
		}
		if ((levelCheckSkill == 0) && ((chr.getTotalSkillLevel(GameConstants.getLinkedAttackSkill(skillid)) <= 0)
				|| (chr.getTotalSkillLevel(GameConstants.getLinkedAttackSkill(skillid)) != skillLevel))) {
			if ((!GameConstants.isMulungSkill(skillid)) && (!GameConstants.isPyramidSkill(skillid))
					&& (chr.getTotalSkillLevel(GameConstants.getLinkedAttackSkill(skillid)) <= 0)
					&& !GameConstants.isAngel(skillid)) {
				c.getSession().write(CWvsContext.enableActions());
				return;
			}
			if (GameConstants.isMulungSkill(skillid)) {
				if (chr.getMapId() / 10000 != 92502) {
					return;
				}
				if (chr.getMulungEnergy() < 10000) {
					return;
				}
				chr.mulung_EnergyModify(false);
			} else if ((GameConstants.isPyramidSkill(skillid)) && (chr.getMapId() / 10000 != 92602)
					&& (chr.getMapId() / 10000 != 92601)) {
				return;
			}
		}
		if (GameConstants.isEventMap(chr.getMapId())) {
			for (MapleEventType t : MapleEventType.values()) {
				MapleEvent e = ChannelServer.getInstance(chr.getClient().getChannel()).getEvent(t);
				if ((e.isRunning()) && (!chr.isGM())) {
					for (int i : e.getType().mapids) {
						if (chr.getMapId() == i) {
							chr.dropMessage(5, "You may not use that here.");
							return;
						}
					}
				}
			}
		}
		skillLevel = chr.getTotalSkillLevel(GameConstants.getLinkedAttackSkill(skillid));
		MapleStatEffect effect = chr.inPVP() ? skill.getPVPEffect(skillLevel) : skill.getEffect(skillLevel);
		if ((effect.isMPRecovery()) && (chr.getStat().getHp() < chr.getStat().getMaxHp() / 100 * 10)) {
			c.getPlayer().dropMessage(5, "You do not have the HP to use this skill.");
			c.getSession().write(CWvsContext.enableActions());
			return;
		}
		if ((effect.getCooldown(chr) > 0) && (!chr.isGM())) {
			if (chr.skillisCooling(skillid) && skillid != 24121005) {
				c.getSession().write(CWvsContext.enableActions());
				return;
			}
			if ((skillid != 5221006) && (skillid != 35111002)) {
				c.getSession().write(CField.skillCooldown(skillid, effect.getCooldown(chr)));
				chr.addCooldown(skillid, System.currentTimeMillis(), effect.getCooldown(chr) * 1000);
			}
		}
		int mobID;
		MapleMonster mob;
		switch (skillid) {
		case 1121001:
		case 1221001:
		case 1321001:
		case 9001020:
		case 9101020:
		case 31111003:
			byte number_of_mobs = slea.readByte();
			slea.skip(3);
			for (int i = 0; i < number_of_mobs; i++) {
				int mobId = slea.readInt();

				mob = chr.getMap().getMonsterByOid(mobId);
				if (mob == null) {
					continue;
				}
				mob.switchController(chr, mob.isControllerHasAggro());
				mob.applyStatus(chr,
						new MonsterStatusEffect(MonsterStatus.STUN, Integer.valueOf(1), skillid, null, false), false,
						effect.getDuration(), true, effect);
			}

			chr.getMap().broadcastMessage(chr, CField.EffectPacket.showBuffeffect(chr.getId(), skillid, 1,
					chr.getLevel(), skillLevel, slea.readByte()), chr.getTruePosition());
			c.getSession().write(CWvsContext.enableActions());
			break;
		case 5201008: { // infinite blast Handler
			int itemid = slea.readInt();
			MapleStatEffect effectp = SkillFactory.getSkill(skillid).getEffect(chr.getSkillLevel(skillid));
			MapleInventoryManipulator.removeById(c, GameConstants.getInventoryType(itemid), itemid,
					effectp.getBulletConsume(), true, false);

			break;
		}

		case 30001061:
			mobID = slea.readInt();
			mob = chr.getMap().getMonsterByOid(mobID);
			if (mob != null) {
				boolean success = (mob.getHp() <= mob.getMobMaxHp() / 2L) && (mob.getId() >= 9304000)
						&& (mob.getId() < 9305000);
				chr.getMap().broadcastMessage(chr, CField.EffectPacket.showBuffeffect(chr.getId(), skillid, 1,
						chr.getLevel(), skillLevel, (byte) (success ? 1 : 0)), chr.getTruePosition());
				if (success) {
					chr.getQuestNAdd(MapleQuest.getInstance(GameConstants.JAGUAR))
							.setCustomData(String.valueOf((mob.getId() - 9303999) * 10));
					chr.getMap().killMonster(mob, chr, true, false, (byte) 1);
					chr.cancelEffectFromBuffStat(MapleBuffStat.MONSTER_RIDING);
					c.getSession().write(CWvsContext.updateJaguar(chr));
				} else {
					chr.dropMessage(5, "The monster has too much physical strength, so you cannot catch it.");
				}
			}
			c.getSession().write(CWvsContext.enableActions());
			break;
		case 30001062:
			chr.dropMessage(5, "No monsters can be summoned. Capture a monster first.");
			c.getSession().write(CWvsContext.enableActions());
			break;
		case 31221001:
			// case 36100010:
			// case 36110012:
			// case 36120015:
		case 36001005:
		case 2121052: {
			List<Integer> moblist = new ArrayList<Integer>();
			byte count = slea.readByte();
			for (byte i = 1; i <= count; i++) {
				moblist.add(slea.readInt());
			}
			if (skillid == 31221001) {
				c.getSession().write(JobPacket.XenonPacket.ShieldChacing(chr.getId(), moblist, 31221014));
			} else if (skillid == 36001005) {
				c.getSession().write(JobPacket.XenonPacket.PinPointRocket(chr.getId(), moblist));
			} else if (skillid == 2121052) {
				c.getSession().write(JobPacket.XenonPacket.MegidoFlameRe(chr.getId(), moblist.get(0)));
			}
			break;
		}
		case 33101005:
			mobID = chr.getFirstLinkMid();
			mob = chr.getMap().getMonsterByOid(mobID);
			chr.setKeyDownSkill_Time(0L);
			chr.getMap().broadcastMessage(chr, CField.skillCancel(chr, skillid), false);
			if (mob != null) {
				boolean success = (mob.getStats().getLevel() < chr.getLevel()) && (mob.getId() < 9000000)
						&& (!mob.getStats().isBoss());
				if (success) {
					chr.getMap().broadcastMessage(MobPacket.suckMonster(mob.getObjectId(), chr.getId()));
					chr.getMap().killMonster(mob, chr, false, false, (byte) -1);
				} else {
					chr.dropMessage(5, "The monster has too much physical strength, so you cannot catch it.");
				}
			} else {
				chr.dropMessage(5, "No monster was sucked. The skill failed.");
			}
			c.getSession().write(CWvsContext.enableActions());
			break;
		case 20040216:
		case 20040217:
		case 20040220:
			// case 20041239:
			chr.changeLuminousMode(skillid);
			// chr.HandleOtherluminous();
			c.getSession().write(CWvsContext.enableActions());
			break;
		case 27121100:
			World.Broadcast.broadcastMessage(CField.getGameMessage("PlayerHandler.", (short) 8));
			c.getSession().write(JobPacket.LuminousPacket.giveLuminousState(20040216, chr.getLightGauge(),
					chr.getDarkGauge(), 2000000000));
			// chr.HandleOtherluminous();
			c.getSession().write(CWvsContext.enableActions());
			break;
		case 11101022:
		case 11111022:
		case 11121005:
		case 11121011:
		case 11121012:
			chr.changeWarriorStance(skillid);
			c.getSession().write(CWvsContext.enableActions());
			break;
		case 36121054:
			// chr.setXenonSurplus((short) 20);
			c.getSession().write(CWvsContext.enableActions());
			c.getSession().write(JobPacket.XenonPacket.giveAmaranthGenerator());
			break;
		case 4341003:
			chr.setKeyDownSkill_Time(0L);
			chr.getMap().broadcastMessage(chr, CField.skillCancel(chr, skillid), false);
		default:

			Point pos = null;
			if ((slea.available() == 5L) || (slea.available() == 7L)) {
				pos = slea.readPos();
			}

			if (effect.isMagicDoor()) {
				if (!FieldLimitType.MysticDoor.check(chr.getMap().getFieldLimit())) {
					effect.applyTo(c.getPlayer(), pos);
				} else {
					c.getSession().write(CWvsContext.enableActions());
				}
			} else {
				int mountid = MapleStatEffect.parseMountInfo(c.getPlayer(), skill.getId());
				if ((mountid != 0) && (mountid != GameConstants.getMountItem(skill.getId(), c.getPlayer()))
						&& (!c.getPlayer().isIntern())
						&& (c.getPlayer().getBuffedValue(MapleBuffStat.MONSTER_RIDING) == null)
						&& (c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -122) == null)
						&& (!GameConstants.isMountItemAvailable(mountid, c.getPlayer().getJob()))) {
					c.getSession().write(CWvsContext.enableActions());
					return;
				}
				// System.err.println("pos " + pos);
				// System.err.println("effect " + effect.getSourceId());
				effect.applyTo(c.getPlayer(), pos);
			}
		}
		if (GameConstants.isAngelicBuster(chr.getJob())) {

			int Recharge = effect.getOnActive();
			if (Recharge > -1) {
				if (Randomizer.isSuccess(Recharge)) {
					c.getSession().write(AngelicPacket.unlockSkill());
					c.getSession().write(AngelicPacket.showRechargeEffect());
					if (c.getPlayer().isGM()) {
						c.getSession().write(AngelicPacket.unlockSkill());
						// c.getSession().write(AngelicPacket.showRechargeEffect());
					} else {
						c.getSession().write(AngelicPacket.lockSkill(skillid));
					}
				}
				c.getSession().write(CWvsContext.enableActions());
			} else {
				if (c.getPlayer().isGM()) {
					c.getSession().write(AngelicPacket.unlockSkill());
					// c.getSession().write(AngelicPacket.showRechargeEffect());
				} else {
					c.getSession().write(AngelicPacket.lockSkill(skillid));
				}
			}
		}

	}

	public static void closeRangeAttack(LittleEndianAccessor slea, MapleClient c, final MapleCharacter chr, final boolean energy) {
		
		if ((chr == null) || ((energy) && (chr.getBuffedValue(MapleBuffStat.ENERGY_CHARGE) == null)
				&& (chr.getBuffedValue(MapleBuffStat.BODY_PRESSURE) == null)
				&& (chr.getBuffedValue(MapleBuffStat.DARK_AURA) == null)
				&& (chr.getBuffedValue(MapleBuffStat.TORNADO) == null)
				&& (chr.getBuffedValue(MapleBuffStat.SUMMON) == null)
				&& (chr.getBuffedValue(MapleBuffStat.RAINING_MINES) == null)
				&& (chr.getBuffedValue(MapleBuffStat.ASURA) == null)
				&& (chr.getBuffedValue(MapleBuffStat.TELEPORT_MASTERY) == null))) {
			return;
		}
		
		if ((chr.hasBlockedInventory()) || (chr.getMap() == null)) {
			return;
		}

		AttackInfo attack = DamageParse.parseDmgM(chr, slea);
		if (attack == null) {
			c.getSession().write(CWvsContext.enableActions());
			return;
		}
		
		final boolean mirror = chr.getBuffedValue(MapleBuffStat.SHADOWPARTNER) != null;
		double maxdamage = chr.getStat().getCurrentMaxBaseDamage();
		Item shield = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -10);
		int attackCount = (shield != null) && (shield.getItemId() / 10000 == 134) ? 2 : 1;
		int skillLevel = 0;
		MapleStatEffect effect = null;
		Skill skill = null;

		String dmg = "";
		for (AttackPair ae : attack.allDamage) {
			for (Pair att : ae.attack) {
				dmg += att.getLeft();
				dmg += ",";
			}
		}

		if (attack.skill != 0) {
			
			skill = SkillFactory.getSkill(GameConstants.getLinkedAttackSkill(attack.skill));
			if ((skill == null) || ((GameConstants.isAngel(attack.skill))
					&& (chr.getStat().equippedSummon % 10000 != attack.skill % 10000))) {
				c.getSession().write(CWvsContext.enableActions());
				return;
			}
			
			if (GameConstants.isDemonAvenger(chr.getJob())) {
				int exceedMax = chr.getSkillLevel(31220044) > 0 ? 18 : 20;
				// chr.showInfo("Info", false, "exceedMax;" + exceedMax);
				if (chr.getExceed() + 1 > exceedMax) {
					chr.setExceed((short) exceedMax);
				} else {
					chr.gainExceed((short) 1);
				}
			}
			
			if (GameConstants.isExceedAttack(skill.getId())) {
				chr.handleExceedAttack(skill.getId());
			}
			
			switch (attack.skill) {
			case 101001100:
			case 101101100:
			case 101111100:
			case 101121100:
				chr.zeroChange(false);
				break;
			case 101001200:
			case 101101200:
			case 101111200:
			case 101121200:
				chr.zeroChange(true);
				break;
			}
			
			skillLevel = chr.getTotalSkillLevel(skill);
			effect = attack.getAttackEffect(chr, skillLevel, skill);
			
			if (effect == null) {
				return;
			}
			
			if (GameConstants.isEventMap(chr.getMapId())) {
				for (MapleEventType t : MapleEventType.values()) {
					MapleEvent e = ChannelServer.getInstance(chr.getClient().getChannel()).getEvent(t);
					if ((e.isRunning()) && (!chr.isGM())) {
						for (int i : e.getType().mapids) {
							if (chr.getMapId() == i) {
								chr.dropMessage(5, "You may not use that here.");
								return;
							}
						}
					}
				}
			}

			if (GameConstants.isAngelicBuster(chr.getJob())) {
				int Recharge = effect.getOnActive();
				if (Recharge > -1) {
					if (Randomizer.isSuccess(Recharge)) {
						c.getSession().write(AngelicPacket.unlockSkill());
						c.getSession().write(AngelicPacket.showRechargeEffect());
					} else {
						if (c.getPlayer().isGM()) {
							c.getSession().write(AngelicPacket.unlockSkill());
							// c.getSession().write(AngelicPacket.showRechargeEffect());
						} else {
							c.getSession().write(AngelicPacket.lockSkill(attack.skill));
						}
					}
				} else {
					if (c.getPlayer().isGM()) {
						c.getSession().write(AngelicPacket.unlockSkill());
						// c.getSession().write(AngelicPacket.showRechargeEffect());
					} else {
						c.getSession().write(AngelicPacket.lockSkill(attack.skill));
					}
				}
			}
			
			maxdamage *= (effect.getDamage() + chr.getStat().getDamageIncrease(attack.skill)) / 100.0D;
			attackCount = effect.getAttackCount();

			if ((effect.getCooldown(chr) > 0) && (!chr.isGM()) && (!energy)) {
				if (chr.skillisCooling(attack.skill)) {
					c.getSession().write(CWvsContext.enableActions());
					return;
				}
				c.getSession().write(CField.skillCooldown(attack.skill, effect.getCooldown(chr)));
				chr.addCooldown(attack.skill, System.currentTimeMillis(), effect.getCooldown(chr) * 1000);
			}
			
		}
		
		attack = DamageParse.Modify_AttackCrit(attack, chr, 1, effect);
		attackCount *= (mirror ? 2 : 1);
		
		if (!energy) {
			if (((chr.getMapId() == 109060000) || (chr.getMapId() == 109060002) || (chr.getMapId() == 109060004))
					&& (attack.skill == 0)) {
				MapleSnowball.MapleSnowballs.hitSnowball(chr);
			}

			int numFinisherOrbs = 0;
			Integer comboBuff = chr.getBuffedValue(MapleBuffStat.COMBO);

			if (isFinisher(attack.skill) > 0) {
				if (comboBuff != null) {
					numFinisherOrbs = comboBuff.intValue() - 1;
				}
				if (numFinisherOrbs <= 0) {
					return;
				}
				chr.handleOrbconsume(isFinisher(attack.skill));
			}
		}
		
		chr.checkFollow();
		
		if (!chr.isHidden()) {
			chr.getMap().broadcastMessage(chr, CField.closeRangeAttack(chr.getId(), attack.tbyte, attack.skill, skillLevel, attack.display, attack.speed, attack.allDamage, energy, chr.getLevel(), chr.getStat().passive_mastery(), attack.unk, attack.charge), chr.getTruePosition());
		} else {
			chr.getMap().broadcastGMMessage(chr, CField.closeRangeAttack(chr.getId(), attack.tbyte, attack.skill, skillLevel, attack.display, attack.speed, attack.allDamage, energy, chr.getLevel(), chr.getStat().passive_mastery(), attack.unk, attack.charge),false);
		}
		
		DamageParse.applyAttack(attack, skill, c.getPlayer(), attackCount, maxdamage, effect, mirror ? AttackType.NON_RANGED_WITH_MIRROR : AttackType.NON_RANGED);
		
		WeakReference<MapleCharacter>[] clones = chr.getClones();
		for (int i = 0; i < clones.length; i++) {
			if (clones[i].get() != null) {
				final MapleCharacter clone = clones[i].get();
				final Skill skil2 = skill;
				final int skillLevel2 = skillLevel;
				final int attackCount2 = attackCount;
				final double maxdamage2 = maxdamage;
				final MapleStatEffect eff2 = effect;
				final AttackInfo attack2 = DamageParse.DivideAttack(attack, chr.isGM() ? 1 : 4);
				Timer.CloneTimer.getInstance().schedule(new Runnable() {
					@Override
					public void run() {
						if (!clone.isHidden()) {
							clone.getMap()
									.broadcastMessage(CField.closeRangeAttack(clone.getId(), attack2.tbyte,
											attack2.skill, skillLevel2, attack2.display, attack2.speed,
											attack2.allDamage, energy, clone.getLevel(),
											clone.getStat().passive_mastery(), attack2.unk, attack2.charge));
						} else {
							clone.getMap().broadcastGMMessage(clone,
									CField.closeRangeAttack(clone.getId(), attack2.tbyte, attack2.skill, skillLevel2,
											attack2.display, attack2.speed, attack2.allDamage, energy, clone.getLevel(),
											clone.getStat().passive_mastery(), attack2.unk, attack2.charge),
									false);
						}
						DamageParse.applyAttack(attack2, skil2, chr, attackCount2, maxdamage2, eff2,
								mirror ? AttackType.NON_RANGED_WITH_MIRROR : AttackType.NON_RANGED);
					}
				}, 500 * i + 500);
			}
		}
		
		int bulletCount = 1;
		switch (attack.skill) {
		case 1201011:
			bulletCount = effect.getAttackCount();
			DamageParse.applyAttack(attack, skill, chr, skillLevel, maxdamage, effect, AttackType.NON_RANGED); // applyAttack(attack, skill, chr, bulletCount, effect, AttackType.RANGED);
			break;
		default:
			DamageParse.applyAttackMagic(attack, skill, chr, effect, maxdamage); // applyAttackMagic(attack, skill, c.getPlayer(), effect);
			break;
		}
	}

	public static void rangedAttack(LittleEndianAccessor slea, MapleClient c, final MapleCharacter chr) {
		if (chr == null) {
			return;
		}
		if ((chr.hasBlockedInventory()) || (chr.getMap() == null)) {
			return;
		}
		AttackInfo attack = DamageParse.parseDmgR(slea, chr);
		if (attack == null) {
			c.getSession().write(CWvsContext.enableActions());
			return;
		}
		int bulletCount = 1;
		int skillLevel = 0;
		MapleStatEffect effect = null;
		Skill skill = null;
		boolean AOE = attack.skill == 4111004;
		boolean noBullet = (chr.getJob() >= 300 && chr.getJob() <= 322)
				|| (chr.getJob() >= 3500 && chr.getJob() <= 3512) || GameConstants.isCannon(chr.getJob())
				|| GameConstants.isXenon(chr.getJob()) || GameConstants.isJett(chr.getJob())
				|| GameConstants.isPhantom(chr.getJob()) || GameConstants.isMercedes(chr.getJob())
				|| GameConstants.isZero(chr.getJob());
		if (attack.skill != 0) {
			skill = SkillFactory.getSkill(GameConstants.getLinkedAttackSkill(attack.skill));
			if ((skill == null) || ((GameConstants.isAngel(attack.skill))
					&& (chr.getStat().equippedSummon % 10000 != attack.skill % 10000))) {
				c.getSession().write(CWvsContext.enableActions());
				return;
			}
			skillLevel = chr.getTotalSkillLevel(skill);
			effect = attack.getAttackEffect(chr, skillLevel, skill);
			if (effect == null) {
				return;
			}
			if (GameConstants.isEventMap(chr.getMapId())) {
				for (MapleEventType t : MapleEventType.values()) {
					MapleEvent e = ChannelServer.getInstance(chr.getClient().getChannel()).getEvent(t);
					if ((e.isRunning()) && (!chr.isGM())) {
						for (int i : e.getType().mapids) {
							if (chr.getMapId() == i) {
								chr.dropMessage(5, "You may not use that here.");
								return;
							}
						}
					}
				}
			}
			if (GameConstants.isAngelicBuster(chr.getJob())) {
				int Recharge = effect.getOnActive();
				if (Recharge > -1) {
					if (Randomizer.isSuccess(Recharge)) {
						c.getSession().write(AngelicPacket.unlockSkill());
						c.getSession().write(AngelicPacket.showRechargeEffect());
					} else {
						if (c.getPlayer().isGM()) {
							c.getSession().write(AngelicPacket.unlockSkill());
							// c.getSession().write(AngelicPacket.showRechargeEffect());
						} else {
							c.getSession().write(AngelicPacket.lockSkill(attack.skill));
						}
					}
				} else {
					if (c.getPlayer().isGM()) {
						c.getSession().write(AngelicPacket.unlockSkill());
						// c.getSession().write(AngelicPacket.showRechargeEffect());
					} else {
						c.getSession().write(AngelicPacket.lockSkill(attack.skill));
					}
				}
			}
			if (GameConstants.isWindArcher(chr.getJob())) {
				int percent = 0, count = 0, skillid = 0, type = 0;
				if (c.getPlayer().getSkillLevel(SkillFactory.getSkill(13120003)) > 0) {
					if (Randomizer.nextInt(100) < 85) {
						skillid = 13120003;
						type = 1;
					} else {
						skillid = 13120010;
						type = 1;
					}
					count = Randomizer.rand(1, 5);
					percent = 20;
				} else if (c.getPlayer().getSkillLevel(SkillFactory.getSkill(13110022)) > 0) {
					if (Randomizer.nextInt(100) < 90) {
						skillid = 13110022;
						type = 1;
					} else {
						skillid = 13110027;
						type = 1;
					}
					count = Randomizer.rand(1, 4);
					percent = 10;
				} else if (c.getPlayer().getSkillLevel(SkillFactory.getSkill(13100022)) > 0) {
					if (Randomizer.nextInt(100) < 95) {
						skillid = 13100022;
						type = 1;
					} else {
						skillid = 13100027;
						type = 1;
					}
					count = Randomizer.rand(1, 3);
					percent = 5;
				}
				for (AttackPair at : attack.allDamage) {

					MapleMonster mob = chr.getMap().getMonsterByOid(at.objectid);
					if (Randomizer.nextInt(100) < percent) {
						if (mob != null) {
							c.getPlayer().getMap().broadcastMessage(c.getPlayer(), JobPacket.WindArcherPacket
									.TrifleWind(c.getPlayer().getId(), skillid, count, mob.getObjectId(), type), false);
							c.getSession().write(JobPacket.WindArcherPacket.TrifleWind(c.getPlayer().getId(), skillid,
									count, mob.getObjectId(), type));
						}
					}
				}
			}
			switch (attack.skill) {
			case 13101005:
			case 21110004: // Ranged but uses attackcount instead
			case 14101006: // Vampure
			case 21120006:
			case 11101004:
				// MIHILE
			case 51001004: // Soul Blade
			case 51111007:
			case 51121008:
				// END MIHILE
			case 1077:
			case 1078:
			case 1079:
			case 11077:
			case 11078:
			case 11079:
			case 15111007:
			case 13111007: // Wind Shot
			case 33101007:
			case 13101020:// Fary Spiral
			case 33101002:
			case 33121002:
			case 33121001:
			case 21100004:
			case 21110011:
			case 21100007:
			case 21000004:
			case 5121002:
			case 5921002:
			case 4121003:
			case 4221003:
			case 5221017:
			case 5721007:
			case 5221016:
			case 5721006:
			case 5211008:
			case 5201001:
			case 5721003:
			case 5711000:
			case 4111013:
			case 5121016:
			case 5121013:
			case 5221013:
			case 5721004:
			case 5721001:
			case 5321001:
			case 14111008:
			case 60011216:// Soul Buster
			case 65001100:// Star Bubble
				// case 2321054:
				AOE = true;
				bulletCount = effect.getAttackCount();
				break;

			case 35121005:
			case 35111004:
			case 35121013:
				AOE = true;
				bulletCount = 6;
				break;
			default:
				bulletCount = effect.getBulletCount();
				break;
			}
			if (noBullet && effect.getBulletCount() < effect.getAttackCount()) {
				bulletCount = effect.getAttackCount();
			}
			if ((noBullet) && (effect.getBulletCount() < effect.getAttackCount())) {
				bulletCount = effect.getAttackCount();
			}
			if ((effect.getCooldown(chr) > 0) && (!chr.isGM())
					&& (((attack.skill != 35111004) && (attack.skill != 35121013))
							|| (chr.getBuffSource(MapleBuffStat.MECH_CHANGE) != attack.skill))) {
				if (chr.skillisCooling(attack.skill)) {
					c.getSession().write(CWvsContext.enableActions());
					return;
				}
				c.getSession().write(CField.skillCooldown(attack.skill, effect.getCooldown(chr)));
				chr.addCooldown(attack.skill, System.currentTimeMillis(), effect.getCooldown(chr) * 1000);
			}
		}
		attack = DamageParse.Modify_AttackCrit(attack, chr, 2, effect);
		Integer ShadowPartner = chr.getBuffedValue(MapleBuffStat.SHADOWPARTNER);
		if (ShadowPartner != null) {
			bulletCount *= 2;
		}
		int projectile = 0;
		int visProjectile = 0;
		if ((!AOE) && (chr.getBuffedValue(MapleBuffStat.SOULARROW) == null) && (!noBullet)) {
			Item ipp = chr.getInventory(MapleInventoryType.USE).getItem((short) attack.slot);
			if (ipp == null) {
				return;
			}
			projectile = ipp.getItemId();

			if (attack.csstar > 0) {
				if (chr.getInventory(MapleInventoryType.CASH).getItem((short) attack.csstar) == null) {
					return;
				}
				visProjectile = chr.getInventory(MapleInventoryType.CASH).getItem((short) attack.csstar).getItemId();
			} else {
				visProjectile = projectile;
			}

			if (chr.getBuffedValue(MapleBuffStat.SPIRIT_CLAW) == null) {
				int bulletConsume = bulletCount;
				if ((effect != null) && (effect.getBulletConsume() != 0)) {
					bulletConsume = effect.getBulletConsume() * (ShadowPartner != null ? 2 : 1);
				}
				if ((chr.getJob() == 412) && (bulletConsume > 0)
						&& (ipp.getQuantity() < MapleItemInformationProvider.getInstance().getSlotMax(projectile))) {
					Skill expert = SkillFactory.getSkill(4120010);
					if (chr.getTotalSkillLevel(expert) > 0) {
						MapleStatEffect eff = expert.getEffect(chr.getTotalSkillLevel(expert));
						if (eff.makeChanceResult()) {
							ipp.setQuantity((short) (ipp.getQuantity() + 1));
							c.getSession().write(CWvsContext.InventoryPacket.updateInventorySlot(MapleInventoryType.USE,
									ipp, false));
							bulletConsume = 0;
							c.getSession().write(CWvsContext.InventoryPacket.getInventoryStatus());
						}
					}
				}
				if ((bulletConsume > 0) && (!MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, projectile,
						bulletConsume, false, true))) {
					chr.dropMessage(5, "You do not have enough arrows/bullets/stars.");
					return;
				}
			}
		} else if ((chr.getJob() >= 3500) && (chr.getJob() <= 3512)) {
			visProjectile = 2333000;
		} else if (GameConstants.isCannon(chr.getJob())) {
			visProjectile = 2333001;
		}

		int projectileWatk = 0;
		if (projectile != 0) {
			projectileWatk = MapleItemInformationProvider.getInstance().getWatkForProjectile(projectile);
		}
		PlayerStats statst = chr.getStat();
		double basedamage;
		switch (attack.skill) {
		case 4001344:
		case 4121007:
		case 14001004:
		case 14111005:
			basedamage = Math.max(statst.getCurrentMaxBaseDamage(),
					statst.getTotalLuk() * 5.0F * (statst.getTotalWatk() + projectileWatk) / 100.0F);
			break;
		case 4111004:
			basedamage = 53000.0D;
			break;
		default:
			basedamage = statst.getCurrentMaxBaseDamage();
			switch (attack.skill) {
			case 3101005:
				basedamage *= effect.getX() / 100.0D;
				break;
			}
		}

		if (effect != null) {
			basedamage *= (effect.getDamage() + statst.getDamageIncrease(attack.skill)) / 100.0D;

			long money = effect.getMoneyCon();
			if (money != 0) {
				if (money > chr.getMeso()) {
					money = chr.getMeso();
				}
				chr.gainMeso(-money, false);
			}
		}
		chr.checkFollow();
		if (!chr.isHidden()) {
			if (attack.skill == 3211006) {
				chr.getMap().broadcastMessage(chr,
						CField.strafeAttack(chr.getId(), attack.tbyte, attack.skill, skillLevel, attack.display,
								attack.speed, visProjectile, attack.allDamage, attack.position, chr.getLevel(),
								chr.getStat().passive_mastery(), attack.unk, chr.getTotalSkillLevel(3220010)),
						chr.getTruePosition());
			} else {
				chr.getMap().broadcastMessage(chr,
						CField.rangedAttack(chr.getId(), attack.tbyte, attack.skill, skillLevel, attack.display,
								attack.speed, visProjectile, attack.allDamage, attack.position, chr.getLevel(),
								chr.getStat().passive_mastery(), attack.unk),
						chr.getTruePosition());
			}
		} else if (attack.skill == 3211006) {
			chr.getMap().broadcastGMMessage(chr,
					CField.strafeAttack(chr.getId(), attack.tbyte, attack.skill, skillLevel, attack.display,
							attack.speed, visProjectile, attack.allDamage, attack.position, chr.getLevel(),
							chr.getStat().passive_mastery(), attack.unk, chr.getTotalSkillLevel(3220010)),
					false);
		} else {
			chr.getMap().broadcastGMMessage(chr,
					CField.rangedAttack(chr.getId(), attack.tbyte, attack.skill, skillLevel, attack.display,
							attack.speed, visProjectile, attack.allDamage, attack.position, chr.getLevel(),
							chr.getStat().passive_mastery(), attack.unk),
					false);
		}

		DamageParse.applyAttack(attack, skill, chr, bulletCount, basedamage, effect,
				ShadowPartner != null ? AttackType.RANGED_WITH_SHADOWPARTNER : AttackType.RANGED);
		WeakReference<MapleCharacter>[] clones = chr.getClones();
		for (int i = 0; i < clones.length; i++) {
			if (clones[i].get() != null) {
				final MapleCharacter clone = clones[i].get();
				final Skill skil2 = skill;
				final MapleStatEffect eff2 = effect;
				final double basedamage2 = basedamage;
				final int bulletCount2 = bulletCount;
				final int visProjectile2 = visProjectile;
				final int skillLevel2 = skillLevel;
				final AttackInfo attack2 = DamageParse.DivideAttack(attack, chr.isGM() ? 1 : 4);
				Timer.CloneTimer.getInstance().schedule(new Runnable() {
					@Override
					public void run() {
						if (!clone.isHidden()) {
							if (attack2.skill == 3211006) {
								clone.getMap()
										.broadcastMessage(CField.strafeAttack(clone.getId(), attack2.tbyte,
												attack2.skill, skillLevel2, attack2.display, attack2.speed,
												visProjectile2, attack2.allDamage, attack2.position, clone.getLevel(),
												clone.getStat().passive_mastery(), attack2.unk,
												chr.getTotalSkillLevel(3220010)));
							} else {
								clone.getMap()
										.broadcastMessage(CField.rangedAttack(clone.getId(), attack2.tbyte,
												attack2.skill, skillLevel2, attack2.display, attack2.speed,
												visProjectile2, attack2.allDamage, attack2.position, clone.getLevel(),
												clone.getStat().passive_mastery(), attack2.unk));
							}
						} else {
							if (attack2.skill == 3211006) {
								clone.getMap().broadcastGMMessage(clone,
										CField.strafeAttack(clone.getId(), attack2.tbyte, attack2.skill, skillLevel2,
												attack2.display, attack2.speed, visProjectile2, attack2.allDamage,
												attack2.position, clone.getLevel(), clone.getStat().passive_mastery(),
												attack2.unk, chr.getTotalSkillLevel(3220010)),
										false);
							} else {
								clone.getMap().broadcastGMMessage(clone,
										CField.rangedAttack(clone.getId(), attack2.tbyte, attack2.skill, skillLevel2,
												attack2.display, attack2.speed, visProjectile2, attack2.allDamage,
												attack2.position, clone.getLevel(), clone.getStat().passive_mastery(),
												attack2.unk),
										false);
							}
						}
						DamageParse.applyAttack(attack2, skil2, chr, bulletCount2, basedamage2, eff2,
								AttackType.RANGED);
					}
				}, 500 * i + 500);
			}
		}
	}

	public static void MagicDamage(LittleEndianAccessor slea, MapleClient c, final MapleCharacter chr) {
		
		if ((chr == null) || (chr.hasBlockedInventory()) || (chr.getMap() == null)) {
			return;
		}
		
		AttackInfo attack = DamageParse.parseDmgMa(slea, chr);
		if (attack == null) {
			c.getSession().write(CWvsContext.enableActions());
			return;
		}
		
		Skill skill = SkillFactory.getSkill(GameConstants.getLinkedAttackSkill(attack.skill));
		
		if ((skill == null) || ((GameConstants.isAngel(attack.skill))
				&& (chr.getStat().equippedSummon % 10000 != attack.skill % 10000))) {
			c.getSession().write(CWvsContext.enableActions());
			return;
		}
		
		int skillLevel = chr.getTotalSkillLevel(skill);
		
		MapleStatEffect effect = attack.getAttackEffect(chr, skillLevel, skill);
		if (effect == null) {
			return;
		}
		
		if (skill.getId() >= 27100000 && skill.getId() < 27120400 && attack.targets > 0
				&& chr.getLuminousState() < 20040000) {
			// chr.changeSkillLevel(SkillFactory.getSkill(20040216), (byte) 1,
			// (byte) 1);
			// chr.changeSkillLevel(SkillFactory.getSkill(20040217), (byte) 1,
			// (byte) 1);
			// chr.changeSkillLevel(SkillFactory.getSkill(20040220), (byte) 1,
			// (byte) 1);
			// chr.changeSkillLevel(SkillFactory.getSkill(20041239), (byte) 1,
			// (byte) 1);
			chr.setLuminousState(GameConstants.getLuminousSkillMode(skill.getId()));
			c.getSession().write(JobPacket.LuminousPacket.giveLuminousState(
					GameConstants.getLuminousSkillMode(skill.getId()), chr.getLightGauge(), chr.getDarkGauge(), 10000));
			SkillFactory.getSkill(GameConstants.getLuminousSkillMode(skill.getId())).getEffect(1).applyTo(chr);
		}
		
		attack = DamageParse.Modify_AttackCrit(attack, chr, 3, effect);
		
		if (GameConstants.isEventMap(chr.getMapId())) {
			for (MapleEventType t : MapleEventType.values()) {
				MapleEvent e = ChannelServer.getInstance(chr.getClient().getChannel()).getEvent(t);
				if ((e.isRunning()) && (!chr.isGM())) {
					for (int i : e.getType().mapids) {
						if (chr.getMapId() == i) {
							chr.dropMessage(5, "You may not use that here.");
							return;
						}
					}
				}
			}
		}
		
		double maxdamage = chr.getStat().getCurrentMaxBaseDamage() * (effect.getDamage() + chr.getStat().getDamageIncrease(attack.skill)) / 100.0D;
		
		if (GameConstants.isPyramidSkill(attack.skill)) {
			maxdamage = 1.0D;
		} else if ((GameConstants.isBeginnerJob(skill.getId() / 10000)) && (skill.getId() % 10000 == 1000)) {
			maxdamage = 40.0D;
		}
		
		if ((effect.getCooldown(chr) > 0) && (!chr.isGM())) {
			if (chr.skillisCooling(attack.skill)) {
				c.getSession().write(CWvsContext.enableActions());
				return;
			}
			c.getSession().write(CField.skillCooldown(attack.skill, effect.getCooldown(chr)));
			chr.addCooldown(attack.skill, System.currentTimeMillis(), effect.getCooldown(chr) * 1000);
		}
		
		chr.checkFollow();
		
		if (!chr.isHidden()) {
			chr.getMap().broadcastMessage(chr,
					CField.magicAttack(chr.getId(), attack.tbyte, attack.skill, skillLevel, attack.display,
							attack.speed, attack.allDamage, attack.charge, chr.getLevel(), attack.unk),
					chr.getTruePosition());
		} else {
			chr.getMap().broadcastGMMessage(chr, CField.magicAttack(chr.getId(), attack.tbyte, attack.skill, skillLevel, attack.display, attack.speed, attack.allDamage, attack.charge, chr.getLevel(), attack.unk), false);
		}
		
		DamageParse.applyAttackMagic(attack, skill, c.getPlayer(), effect, maxdamage);
		
		WeakReference<MapleCharacter>[] clones = chr.getClones();
		for (int i = 0; i < clones.length; i++) {
			if (clones[i].get() != null) {
				final MapleCharacter clone = clones[i].get();
				final Skill skil2 = skill;
				final MapleStatEffect eff2 = effect;
				final double maxd = maxdamage;
				final int skillLevel2 = skillLevel;
				final AttackInfo attack2 = DamageParse.DivideAttack(attack, chr.isGM() ? 1 : 4);
				Timer.CloneTimer.getInstance().schedule(new Runnable() {
					@Override
					public void run() {
						if (!clone.isHidden()) {
							clone.getMap()
									.broadcastMessage(CField.magicAttack(clone.getId(), attack2.tbyte, attack2.skill,
											skillLevel2, attack2.display, attack2.speed, attack2.allDamage,
											attack2.charge, clone.getLevel(), attack2.unk));
						} else {
							clone.getMap().broadcastGMMessage(clone,
									CField.magicAttack(clone.getId(), attack2.tbyte, attack2.skill, skillLevel2,
											attack2.display, attack2.speed, attack2.allDamage, attack2.charge,
											clone.getLevel(), attack2.unk),
									false);
						}
						DamageParse.applyAttackMagic(attack2, skil2, chr, eff2, maxd);
					}
				}, 500 * i + 500);
			}
		}
		
		int bulletCount = 1;
		switch (attack.skill) {
		case 27101100: // Sylvan Lance
		case 27101202: // Pressure Void
		case 27111100: // Spectral Light
		case 27111202: // Moonlight Spear
		case 27121100: // Reflection
		case 27001100:
		case 27121202: // Apocalypse
		case 2121006: // Paralyze
		case 2221003: //
		case 2221006: // Chain Lightning
		case 2221007: // Blizzard
		case 2221012: // Frozen Orb
		case 2111003: // Poison Mist
		case 2121003: // Myst Eruption
		case 22181002: // Dark Fog
		case 2321054:
		case 27121303:
		case 27111303:
		case 36121013:
			// case 36101009:
			// case 36111010:
			bulletCount = effect.getAttackCount();
			DamageParse.applyAttack(attack, skill, chr, skillLevel, maxdamage, effect, AttackType.RANGED);
			break;
		default:
			DamageParse.applyAttackMagic(attack, skill, chr, effect, maxdamage);
			break;
		}
	}

	public static void DropMeso(int meso, MapleCharacter chr) {
		if ((!chr.isAlive()) || (meso < 10) || (meso > 50000) || (meso > chr.getMeso())) {
			chr.getClient().getSession().write(CWvsContext.enableActions());
			return;
		}
		chr.gainMeso(-meso, false, true);
		chr.getMap().spawnMesoDrop(meso, chr.getTruePosition(), chr, chr, true, (byte) 0);
	}

	public static void ChangeAndroidEmotion(int emote, MapleCharacter chr) {// MIXTAMAL6

		if ((emote > 0) && (chr != null) && (chr.getMap() != null) && (!chr.isHidden()) && (emote <= 17)
				&& (chr.getAndroid() != null)) {

			chr.getMap().broadcastMessage(CField.showAndroidEmotion(chr.getId(), (byte) emote));

		}

	}

	public static void MoveAndroid(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {// MIXTAMAL6
		slea.skip(12);// MIXTAMAL6
		final List<LifeMovementFragment> res = MovementParse.parseMovement(slea, 3);// MIXTAMAL6

		if ((res != null) && (chr != null) && (!res.isEmpty()) && (chr.getMap() != null)
				&& (chr.getAndroid() != null)) {
			Point pos = new Point(chr.getAndroid().getPos());
			chr.getAndroid().updatePosition(res);
			chr.getMap().broadcastMessage(chr, CField.moveAndroid(chr.getId(), pos, res), false);
		}
	}

	public static void MoveHaku(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
		slea.skip(17);
		List res = MovementParse.parseMovement(slea, 6);

		if ((res != null) && (chr != null) && (!res.isEmpty()) && (chr.getMap() != null) && (chr.getHaku() != null)) {
			Point pos = new Point(chr.getHaku().getPosition());
			chr.getHaku().updatePosition(res);
			chr.getMap().broadcastMessage(chr, CField.moveHaku(chr.getId(), chr.getHaku().getObjectId(), pos, res),
					false);
		}
	}

	public static void ChangeHaku(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
		int oid = slea.readInt();
		if (chr.getHaku() != null) {
			chr.getHaku().sendStats();
			chr.getMap().broadcastMessage(chr, CField.spawnHaku_change0(chr.getId()), true);
			chr.getMap().broadcastMessage(chr, CField.spawnHaku_change1(chr.getHaku()), true);
			chr.getMap().broadcastMessage(chr, CField.spawnHaku_bianshen(chr.getId(), oid, chr.getHaku().getStats()),
					true);
		}
	}

	public static void ChangeEmotion(final int emote, final MapleCharacter chr) {
		if (emote > 7) {
			final int emoteid = 5159992 + emote;
			final MapleInventoryType type = GameConstants.getInventoryType(emoteid);
			if (chr.getInventory(type).findById(emoteid) == null) {
				return;
			}
		}
		if (emote > 0 && chr != null && chr.getMap() != null && !chr.isHidden()) { // O_o
			chr.getMap().broadcastMessage(chr, CField.facialExpression(chr, emote), false);
			WeakReference<MapleCharacter>[] clones = chr.getClones();
			for (int i = 0; i < clones.length; i++) {
				if (clones[i].get() != null) {
					final MapleCharacter clone = clones[i].get();
					CloneTimer.getInstance().schedule(new Runnable() {
						@Override
						public void run() {
							clone.getMap().broadcastMessage(CField.facialExpression(clone, emote));
						}
					}, 500 * i + 500);
				}
			}
		}
	}

	public static void Heal(LittleEndianAccessor slea, MapleCharacter chr) {
		if (chr == null) {
			return;
		}
		slea.skip(4); // update tick
		if (slea.available() >= 8L) {
			slea.skip(slea.available() >= 12L ? 8 : 4);
		}
		int healHP = slea.readShort();
		int healMP = slea.readShort();

		PlayerStats stats = chr.getStat();

		if (stats.getHp() <= 0) {
			return;
		}
		long now = System.currentTimeMillis();
		if ((healHP != 0) && (chr.canHP(now + 1000L))) {
			if (healHP > stats.getHealHP()) {
				healHP = (int) stats.getHealHP();
			}
			chr.addHP(healHP);
		}
		if ((healMP != 0) && (!GameConstants.isDemonSlayer(chr.getJob())) && (chr.canMP(now + 1000L))) {
			if (healMP > stats.getHealMP()) {
				healMP = (int) stats.getHealMP();
			}
			chr.addMP(healMP);
		}
	}

	public static void ChangeMapSpecial(String portal_name, MapleClient c, MapleCharacter chr) {
		if ((chr == null) || (chr.getMap() == null)) {
			return;
		}
		MaplePortal portal = chr.getMap().getPortal(portal_name);

		// if (chr.getGMLevel() > ServerConstants.PlayerGMRank.GM.getLevel()) {
		// chr.dropMessage(6, new
		// StringBuilder().append(portal.getScriptName()).append("
		// accessed").toString());
		// }
		if ((portal != null) && (!chr.hasBlockedInventory())) {
			portal.enterPortal(c);
		} else {
			c.getSession().write(CWvsContext.enableActions());
		}
	}

	public static void ChangeMap(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
		if ((chr == null) || (chr.getMap() == null)) {
			return;
		}
		if (slea.available() != 0L) {
			slea.readByte();
			int targetid = slea.readInt();
			slea.readInt();
			MaplePortal portal = chr.getMap().getPortal(slea.readMapleAsciiString());
			if (slea.available() >= 7L) {
				slea.skip(4); // update tick
			}
			slea.skip(1);
			boolean wheel = (slea.readShort() > 0) && (!GameConstants.isEventMap(chr.getMapId()))
					&& (chr.haveItem(5510000, 1, false, true)) && (chr.getMapId() / 1000000 != 925);

			if ((targetid != -1) && (!chr.isAlive())) {
				chr.setStance(0);
				if ((chr.getEventInstance() != null) && (chr.getEventInstance().revivePlayer(chr)) && (chr.isAlive())) {
					return;
				}
				if (chr.getPyramidSubway() != null) {
					chr.getStat().setHp(50, chr);
					chr.getPyramidSubway().fail(chr);
					return;
				}

				if (chr.getMapId() == 105200111) {
					chr.getStat().setHp(500000, chr);
					chr.getStat().setMp(500000, chr);
				}

				if (!wheel) {
					chr.getStat().setHp(50, chr);

					MapleMap to = chr.getMap().getReturnMap();
					chr.changeMap(to, to.getPortal(0));
				} else {
					c.getSession().write(CField.EffectPacket.useWheel((byte) (chr.getInventory(MapleInventoryType.CASH).countById(5510000) - 1)));
					chr.getStat().setHp(chr.getStat().getMaxHp() / 100 * 40, chr);
					MapleInventoryManipulator.removeById(c, MapleInventoryType.CASH, 5510000, 1, true, false);

					MapleMap to = chr.getMap();
					chr.changeMap(to, to.getPortal(0));
				}
			} else if ((targetid != -1) && (chr.isIntern())) {
				MapleMap to = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(targetid);
				if (to != null) {
					chr.changeMap(to, to.getPortal(0));
				} else {
					chr.dropMessage(5, "Map is NULL. Use !warp <mapid> instead.");
				}
			} else if ((targetid != -1) && (!chr.isIntern())) {
				int divi = chr.getMapId() / 100;
				boolean unlock = false;
				boolean warp = false;
				if (divi == 9130401) {
					warp = (targetid / 100 == 9130400) || (targetid / 100 == 9130401);
					if (targetid / 10000 != 91304) {
						warp = true;
						unlock = true;
						targetid = 130030000;
					}
				} else if (divi == 9130400) {
					warp = (targetid / 100 == 9130400) || (targetid / 100 == 9130401);
					if (targetid / 10000 != 91304) {
						warp = true;
						unlock = true;
						targetid = 130030000;
					}
				} else if (divi == 9140900) {
					warp = (targetid == 914090011) || (targetid == 914090012) || (targetid == 914090013)
							|| (targetid == 140090000);
				} else if ((divi == 9120601) || (divi == 9140602) || (divi == 9140603) || (divi == 9140604)
						|| (divi == 9140605)) {
					warp = (targetid == 912060100) || (targetid == 912060200) || (targetid == 912060300)
							|| (targetid == 912060400) || (targetid == 912060500) || (targetid == 3000100);
					unlock = true;
				} else if (divi == 9101500) {
					warp = (targetid == 910150006) || (targetid == 101050010);
					unlock = true;
				} else if ((divi == 9140901) && (targetid == 140000000)) {
					unlock = true;
					warp = true;
				} else if ((divi == 9240200) && (targetid == 924020000)) {
					unlock = true;
					warp = true;
				} else if ((targetid == 980040000) && (divi >= 9800410) && (divi <= 9800450)) {
					warp = true;
				} else if ((divi == 9140902) && ((targetid == 140030000) || (targetid == 140000000))) {
					unlock = true;
					warp = true;
				} else if ((divi == 9000900) && (targetid / 100 == 9000900) && (targetid > chr.getMapId())) {
					warp = true;
				} else if ((divi / 1000 == 9000) && (targetid / 100000 == 9000)) {
					unlock = (targetid < 900090000) || (targetid > 900090004);
					warp = true;
				} else if ((divi / 10 == 1020) && (targetid == 1020000)) {
					unlock = true;
					warp = true;
				} else if ((chr.getMapId() == 900090101) && (targetid == 100030100)) {
					unlock = true;
					warp = true;
				} else if ((chr.getMapId() == 2010000) && (targetid == 104000000)) {
					unlock = true;
					warp = true;
				} else if ((chr.getMapId() == 106020001) || (chr.getMapId() == 106020502)) {
					if (targetid == chr.getMapId() - 1) {
						unlock = true;
						warp = true;
					}
				} else if ((chr.getMapId() == 0) && (targetid == 10000)) {
					unlock = true;
					warp = true;
				} else if ((chr.getMapId() == 931000011) && (targetid == 931000012)) {
					unlock = true;
					warp = true;
				} else if ((chr.getMapId() == 931000021) && (targetid == 931000030)) {
					unlock = true;
					warp = true;
				} else if ((chr.getMapId() == 105040300) && (targetid == 105040000)) {
					unlock = true;
					warp = true;
				}
				if (unlock) {
					c.getSession().write(CField.UIPacket.IntroDisableUI(false));
					c.getSession().write(CField.UIPacket.IntroLock(false));
					c.getSession().write(CWvsContext.enableActions());
				}
				if (warp) {
					MapleMap to = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(targetid);
					chr.changeMap(to, to.getPortal(0));
				}
			} else if ((portal != null) && (!chr.hasBlockedInventory())) {
				portal.enterPortal(c);
			} else {
				
				c.getSession().write(CWvsContext.enableActions());
			}
		}
	}

	public static void InnerPortal(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
		if ((chr == null) || (chr.getMap() == null)) {
			return;
		}
		MaplePortal portal = chr.getMap().getPortal(slea.readMapleAsciiString());
		int toX = slea.readShort();
		int toY = slea.readShort();

		if (portal == null) {
			return;
		}
		if ((portal.getPosition().distanceSq(chr.getTruePosition()) > 22500.0D) && (!chr.isGM())) {
			return;
		}
		chr.getMap().movePlayer(chr, new Point(toX, toY));
		chr.checkFollow();
	}

	public static void snowBall(LittleEndianAccessor slea, MapleClient c) {
		c.getSession().write(CWvsContext.enableActions());
	}

	public static void leftKnockBack(LittleEndianAccessor slea, MapleClient c) {
		if (c.getPlayer().getMapId() / 10000 == 10906) {
			c.getSession().write(CField.leftKnockBack());
			c.getSession().write(CWvsContext.enableActions());
		}
	}

	public static void ReIssueMedal(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
		MapleQuest q = MapleQuest.getInstance(slea.readShort());
		int itemid = q.getMedalItem();
		if ((itemid != slea.readInt()) || (itemid <= 0) || (q == null) || (chr.getQuestStatus(q.getId()) != 2)) {
			c.getSession().write(CField.UIPacket.reissueMedal(itemid, 4));
			return;
		}
		if (chr.haveItem(itemid, 1, true, true)) {
			c.getSession().write(CField.UIPacket.reissueMedal(itemid, 3));
			return;
		}
		if (!MapleInventoryManipulator.checkSpace(c, itemid, 1, "")) {
			c.getSession().write(CField.UIPacket.reissueMedal(itemid, 2));
			return;
		}
		if (chr.getMeso() < 100) {
			c.getSession().write(CField.UIPacket.reissueMedal(itemid, 1));
			return;
		}
		chr.gainMeso(-100, true, true);
		MapleInventoryManipulator.addById(c, itemid, (byte) 1,
				new StringBuilder().append("Redeemed item through medal quest ").append(q.getId()).append(" on ")
						.append(FileoutputUtil.CurrentReadable_Date()).toString());
		c.getSession().write(CField.UIPacket.reissueMedal(itemid, 0));
	}

	public static void MessengerRanking(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
		if (chr == null) {
			return;
		}
		c.getSession().write(CField.messengerOpen(slea.readByte(), null));
	}
}
