package tools.packet;

import java.awt.Point;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import client.MonsterStatus;
import client.MonsterStatusEffect;
import handling.SendPacketOpcode;
import server.life.MapleMonster;
import server.life.MobSkill;
import server.maps.MapleMap;
import server.maps.MapleNodes;
import server.movement.LifeMovementFragment;
import tools.HexTool;
import tools.Pair;
import tools.data.MaplePacketLittleEndianWriter;

public class MobPacket {

	public static byte[] damageMonster(int oid, long damage) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.DAMAGE_MONSTER.getValue());
		mplew.writeInt(oid);
		mplew.write(0);
		mplew.writeLong(damage);

		return mplew.getPacket();
	}

	public static byte[] damageFriendlyMob(MapleMonster mob, long damage, boolean display) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.DAMAGE_MONSTER.getValue());
		mplew.writeInt(mob.getObjectId());
		mplew.write(display ? 1 : 2);
		if (damage > 2147483647L) {
			mplew.writeInt(2147483647);
		} else {
			mplew.writeInt((int) damage);
		}
		if (mob.getHp() > 2147483647L) {
			mplew.writeInt((int) (mob.getHp() / mob.getMobMaxHp() * 2147483647.0D));
		} else {
			mplew.writeInt((int) mob.getHp());
		}
		if (mob.getMobMaxHp() > 2147483647L) {
			mplew.writeInt(2147483647);
		} else {
			mplew.writeInt((int) mob.getMobMaxHp());
		}

		return mplew.getPacket();
	}

	public static byte[] killMonster(int oid, int animation, boolean azwan) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		if (azwan) {
			mplew.writeShort(SendPacketOpcode.AZWAN_KILL_MONSTER.getValue());
		} else {
			mplew.writeShort(SendPacketOpcode.KILL_MONSTER.getValue());
		}
		boolean a = false; // idk
		boolean b = false; // idk
		if (azwan) {
			mplew.write(a ? 1 : 0);
			mplew.write(b ? 1 : 0);
		}
		mplew.writeInt(oid);
		if (azwan) {
			if (a) {
				mplew.write(0);
				if (b) {
					// set mob temporary stat
				} else {
					// set mob temporary stat
				}
			} else {
				if (b) {
					// idk
				} else {
					// idk
				}
			}
			return mplew.getPacket();
		}
		mplew.write(animation);
		if (animation == 4) {
			mplew.writeInt(-1);
		}

		return mplew.getPacket();
	}

	public static byte[] suckMonster(int oid, int chr) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.KILL_MONSTER.getValue());
		mplew.writeInt(oid);
		mplew.write(4);
		mplew.writeInt(chr);

		return mplew.getPacket();
	}

	public static byte[] healMonster(int oid, int heal) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.DAMAGE_MONSTER.getValue());
		mplew.writeInt(oid);
		mplew.write(0);
		mplew.writeInt(-heal);

		return mplew.getPacket();
	}

	public static byte[] MobToMobDamage(int oid, int dmg, int mobid, boolean azwan) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		if (azwan) {
			mplew.writeShort(SendPacketOpcode.AZWAN_MOB_TO_MOB_DAMAGE.getValue());
		} else {
			mplew.writeShort(SendPacketOpcode.MOB_TO_MOB_DAMAGE.getValue());
		}
		mplew.writeInt(oid);
		mplew.write(0);
		mplew.writeInt(dmg);
		mplew.writeInt(mobid);
		mplew.write(1);

		return mplew.getPacket();
	}

	public static byte[] getMobSkillEffect(int oid, int skillid, int cid, int skilllevel) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SKILL_EFFECT_MOB.getValue());
		mplew.writeInt(oid);
		mplew.writeInt(skillid);
		mplew.writeInt(cid);
		mplew.writeShort(skilllevel);

		return mplew.getPacket();
	}

	public static byte[] getMobCoolEffect(int oid, int itemid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.ITEM_EFFECT_MOB.getValue());
		mplew.writeInt(oid);
		mplew.writeInt(itemid);

		return mplew.getPacket();
	}

	public static byte[] showMonsterHP(int oid, int remhppercentage) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SHOW_MONSTER_HP.getValue());
		mplew.writeInt(oid);
		mplew.write(remhppercentage);

		return mplew.getPacket();
	}

	public static byte[] showCygnusAttack(int oid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.CYGNUS_ATTACK.getValue());
		mplew.writeInt(oid);

		return mplew.getPacket();
	}

	public static byte[] showMonsterResist(int oid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.MONSTER_RESIST.getValue());
		mplew.writeInt(oid);
		mplew.writeInt(0);
		mplew.writeInt(0); // new
		mplew.writeShort(1);
		mplew.writeInt(0);

		return mplew.getPacket();
	}

	public static byte[] showBossHP(MapleMonster mob) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.BOSS_ENV.getValue());
		mplew.write(6);
		mplew.writeInt(mob.getId() == 9400589 ? 9300184 : mob.getId());
		if (mob.getHp() > 2147483647L) {
			mplew.writeInt((int) (mob.getHp() / mob.getMobMaxHp() * 2147483647.0D));
		} else {
			mplew.writeInt((int) mob.getHp());
		}
		if (mob.getMobMaxHp() > 2147483647L) {
			mplew.writeInt(2147483647);
		} else {
			mplew.writeInt((int) mob.getMobMaxHp());
		}
		mplew.write(mob.getStats().getTagColor());
		mplew.write(mob.getStats().getTagBgColor());

		return mplew.getPacket();
	}

	public static byte[] showBossHP(int monsterId, long currentHp, long maxHp) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.BOSS_ENV.getValue());
		mplew.write(6);
		mplew.writeInt(monsterId);
		if (currentHp > 2147483647L) {
			mplew.writeInt((int) (currentHp / maxHp * 2147483647.0D));
		} else {
			mplew.writeInt((int) (currentHp <= 0L ? -1L : currentHp));
		}
		if (maxHp > 2147483647L) {
			mplew.writeInt(2147483647);
		} else {
			mplew.writeInt((int) maxHp);
		}
		mplew.write(6);
		mplew.write(5);

		return mplew.getPacket();
	}

	public static byte[] moveMonster(boolean useskill, int skill, int unk, int oid, Point startPos,
			List<LifeMovementFragment> moves) {
		return moveMonster(useskill, skill, unk, oid, startPos, moves, null, null);
	}

	public static byte[] moveMonster(boolean useskill, int skill, int unk, int oid, Point startPos,
			List<LifeMovementFragment> moves, List<Integer> unk2, List<Pair<Integer, Integer>> unk3) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.MOVE_MONSTER.getValue());
		mplew.writeInt(oid);
		mplew.write(useskill ? 1 : 0);
		mplew.write(skill);
		mplew.writeInt(unk);
		mplew.write(unk3 == null ? 0 : unk3.size());
		if (unk3 != null) {
			for (Pair i : unk3) {
				mplew.writeShort(((Integer) i.left).intValue());
				mplew.writeShort(((Integer) i.right).intValue());
			}
		}
		mplew.write(unk2 == null ? 0 : unk2.size());
		if (unk2 != null) {
			for (Integer i : unk2) {
				mplew.writeShort(i.intValue());
			}
		}

		mplew.writeInt(0);
		mplew.writePos(startPos);
		mplew.writeInt(0);
		PacketHelper.serializeMovementList(mplew, moves);
		mplew.write(0); // ...
		return mplew.getPacket();
	}

	public static byte[] spawnMonster(MapleMonster life, int spawnType, int link, boolean azwan) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.SPAWN_MONSTER.getValue());

		mplew.write(0);
		mplew.writeInt(life.getObjectId());

		mplew.write(1);
		mplew.writeInt(life.getId());

		addMonsterStatus(mplew, life);
		addMonsterInformation(mplew, life, true, (byte) spawnType, link, false);

		/*mplew.writePos(life.getTruePosition());
		mplew.write(0);// ...
		mplew.write(life.getStance());
		mplew.writeShort(life.getFh());
		mplew.writeShort(life.getFh());
		mplew.writeShort(spawnType); // ...
		if ((spawnType == -3) || (spawnType >= 0)) {
			mplew.writeInt(link);
		}
		mplew.write(life.getCarnivalTeam());
		mplew.writeInt(life.getHp() > 2147483647 ? 2147483647 : (int) life.getHp());
		mplew.writeInt(0);

		mplew.writeInt(0);
		mplew.writeInt(0);
		mplew.writeInt(0);

		mplew.writeInt(0);
		mplew.write(0);

		mplew.writeInt(-1);
		mplew.writeInt(-1);
		mplew.write(0);
		mplew.writeInt(0);
		mplew.writeInt(0); // 0x64
		mplew.writeInt(-1);
		mplew.write(0);
		mplew.writeInt(0);
		mplew.writeInt(0);
		mplew.writeShort(0); */
		return mplew.getPacket();
	}

	private static void addMonsterStatus(MaplePacketLittleEndianWriter mplew, MapleMonster life) {

		// ForcedMobStat::Decode
		mplew.write(life.getChangedStats() != null);
		if (life.getChangedStats() != null) {
			mplew.writeInt(life.getChangedStats().hp > 2147483647L ? 2147483647 : (int) life.getChangedStats().hp);
			mplew.writeInt(life.getChangedStats().mp);
			mplew.writeInt(life.getChangedStats().exp);
			mplew.writeInt(life.getChangedStats().watk);
			mplew.writeInt(life.getChangedStats().matk);
			mplew.writeInt(life.getChangedStats().PDRate);
			mplew.writeInt(life.getChangedStats().MDRate);
			mplew.writeInt(life.getChangedStats().acc);
			mplew.writeInt(life.getChangedStats().eva);
			mplew.writeInt(life.getChangedStats().pushed);
			mplew.writeInt(life.getChangedStats().speed);
			mplew.writeInt(life.getChangedStats().level);
			mplew.writeInt(0); // nUserCount
		}

		// CMob::SetTemporaryStat
		mplew.write(HexTool.getByteArrayFromHexString("00 00 00 00 00 00 00 60 80 FF A7 00"));

		// MobStat::DecodeTemporary
		mplew.write(new byte[158]);
	}

	public static byte[] controlMonster(MapleMonster life, boolean newSpawn, boolean aggro, boolean azwan) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		if (azwan) {
			mplew.writeShort(SendPacketOpcode.AZWAN_SPAWN_MONSTER_CONTROL.getValue());
		} else {
			mplew.writeShort(SendPacketOpcode.SPAWN_MONSTER_CONTROL.getValue());
		}

		if (!azwan) {
			mplew.write(aggro ? 2 : 1);
		}
		mplew.writeInt(life.getObjectId());

		mplew.write(1); // 1 = Control normal, 5 = Control none?
		mplew.writeInt(life.getId());

		addMonsterStatus(mplew, life);
		addMonsterInformation(mplew, life, false, (byte) 0, 0, newSpawn);
		
		/*mplew.writePos(life.getTruePosition());
		mplew.write(life.getStance());
		mplew.writeShort(life.getFh());// was0
		mplew.writeShort(life.getFh());
		mplew.write(newSpawn ? -2 : life.isFake() ? -4 : -1);
		mplew.write(life.getCarnivalTeam());
		mplew.writeInt(125);
		mplew.writeInt(0);
		mplew.writeInt(0);
		mplew.writeInt(0);
		mplew.writeInt(0);
		mplew.writeInt(0);
		mplew.write(0);
		mplew.writeLong(-1);
		mplew.writeInt(0);
		mplew.writeInt(0);
		mplew.write(0);
		mplew.write(-1);*/
		return mplew.getPacket();
	}

	public static void addMonsterInformation(MaplePacketLittleEndianWriter mplew, MapleMonster life, boolean summon,
			byte spawnType, int link, boolean newSpawn) {
		
		mplew.writePos(life.getTruePosition());
		mplew.write(0); // ...
		mplew.write(life.getStance());
		mplew.writeShort(life.getFh());
		mplew.writeShort(life.getFh());

		if (summon) {

			mplew.writeShort(spawnType); // ...
			if ((spawnType == -3) || (spawnType >= 0)) {
				mplew.writeInt(link); // link
			}

		} else {
			
			// mplew.write(newSpawn ? -2 : life.isFake() ? -4 : -1);
			if(newSpawn) {
				mplew.writeShort(-2); // ...
			} else if (life.isFake()) {
				mplew.writeShort(-4); // ...
			} else {
				mplew.writeShort(-1); // ...
			}

		}
		mplew.write(life.getCarnivalTeam());
		
		int value = Integer.MAX_VALUE;
		if(life.getHp() > value) {
			mplew.writeInt(value);
		} else {
			mplew.writeInt((int) life.getHp());
		}
		
		mplew.writeInt(0);
		
		mplew.writeInt(0);
		mplew.writeInt(0);
		mplew.writeInt(0);
		
		mplew.writeInt(0);
		mplew.write(0);
		
		mplew.writeInt(-1);
		mplew.writeInt(-1);
		mplew.write(0);
		mplew.writeInt(0);
		mplew.writeInt(0); // 0x64
		mplew.writeInt(-1);
		mplew.write(0);
		mplew.writeInt(0);
		mplew.writeInt(0);
		mplew.writeShort(0);
	}

	public static byte[] stopControllingMonster(MapleMonster life, boolean azwan) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		if (azwan) {
			mplew.writeShort(SendPacketOpcode.AZWAN_SPAWN_MONSTER_CONTROL.getValue());
		} else {
			mplew.writeShort(SendPacketOpcode.SPAWN_MONSTER_CONTROL.getValue());
		}
		if (!azwan) {
			mplew.write(0);
		}
		mplew.writeInt(life.getObjectId());
		if (azwan) {
			mplew.write(0);
			mplew.writeInt(0);
			
			mplew.write(0);
			addMonsterStatus(mplew, life);
			addMonsterInformation(mplew, life, false, (byte) 0, 0, false);

			/*mplew.writePos(life.getTruePosition());
			mplew.write(life.getStance());
			mplew.writeShort(0);
			mplew.writeShort(life.getFh());
			mplew.write(life.isFake() ? -4 : -1);
			mplew.write(life.getCarnivalTeam());
			mplew.writeInt(63000);
			mplew.writeInt(0);
			mplew.writeInt(0);
			mplew.write(-1);*/
		}

		return mplew.getPacket();
	}

	public static byte[] makeMonsterReal(MapleMonster life, boolean azwan) {
		return spawnMonster(life, -1, 0, azwan);
	}

	public static byte[] makeMonsterFake(MapleMonster life, boolean azwan) {
		return spawnMonster(life, -4, 0, azwan);
	}

	public static byte[] makeMonsterEffect(MapleMonster life, int effect, boolean azwan) {
		return spawnMonster(life, effect, 0, azwan);
	}

	public static byte[] moveMonsterResponse(int objectid, short moveid, int currentMp, boolean useSkills, int skillId,
			int skillLevel) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.MOVE_MONSTER_RESPONSE.getValue());
		mplew.writeInt(objectid);
		
		mplew.writeShort(moveid);
		mplew.write(useSkills ? 1 : 0);
		mplew.writeInt(currentMp); // ...
		mplew.writeInt(skillId); // ...
		mplew.write(skillLevel);
		mplew.writeInt(0); // // attack id
		return mplew.getPacket();
	}

	public static byte[] getMonsterSkill(int objectid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.MONSTER_SKILL.getValue());
		mplew.writeInt(objectid);
		mplew.writeLong(0);

		return mplew.getPacket();
	}

	public static byte[] getMonsterTeleport(int objectid, int x, int y) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.TELE_MONSTER.getValue());
		mplew.writeInt(objectid);
		mplew.writeInt(x);
		mplew.writeInt(y);

		return mplew.getPacket();
	}

	private static void getLongMask_NoRef(MaplePacketLittleEndianWriter mplew, Collection<MonsterStatusEffect> ss,
			boolean ignore_imm) {
		int[] mask = new int[12];
		for (MonsterStatusEffect statup : ss) {
			if ((statup != null) && (statup.getStati() != MonsterStatus.WEAPON_DAMAGE_REFLECT)
					&& (statup.getStati() != MonsterStatus.MAGIC_DAMAGE_REFLECT)
					&& ((!ignore_imm) || ((statup.getStati() != MonsterStatus.WEAPON_IMMUNITY)
							&& (statup.getStati() != MonsterStatus.MAGIC_IMMUNITY)
							&& (statup.getStati() != MonsterStatus.DAMAGE_IMMUNITY)))) {
				mask[(statup.getStati().getPosition() - 1)] |= statup.getStati().getValue();
			}
		}
		for (int i = mask.length; i >= 1; i--) {
			mplew.writeInt(mask[(i - 1)]);
		}
	}

	public static byte[] applyMonsterStatus(int oid, MonsterStatus mse, int x, MobSkill skil) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.APPLY_MONSTER_STATUS.getValue());
		mplew.writeInt(oid);
		PacketHelper.writeSingleMask(mplew, mse);

		mplew.writeInt(x);
		mplew.writeShort(skil.getSkillId());
		mplew.writeShort(skil.getSkillLevel());
		mplew.writeShort(mse.isEmpty() ? 1 : 0);

		mplew.writeShort(0);
		mplew.write(2);// was 1
		mplew.write0(30);

		return mplew.getPacket();
	}

	public static byte[] applyMonsterStatus(MapleMonster mons, MonsterStatusEffect ms) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.APPLY_MONSTER_STATUS.getValue());
		mplew.writeInt(mons.getObjectId());
		PacketHelper.writeSingleMask(mplew, ms.getStati());

		mplew.writeInt(ms.getX().intValue());
		if (ms.isMonsterSkill()) {
			mplew.writeShort(ms.getMobSkill().getSkillId());
			mplew.writeShort(ms.getMobSkill().getSkillLevel());
		} else if (ms.getSkill() > 0) {
			mplew.writeInt(ms.getSkill());
		}
		mplew.writeShort((short) ((ms.getCancelTask() - System.currentTimeMillis()) / 1000));

		mplew.writeLong(0L);
		mplew.writeShort(0);
		mplew.write(1);

		return mplew.getPacket();
	}

	public static byte[] applyMonsterStatus(MapleMonster mons, List<MonsterStatusEffect> mse) {
		if ((mse.size() <= 0) || (mse.get(0) == null)) {
			return CWvsContext.enableActions();
		}
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.APPLY_MONSTER_STATUS.getValue());
		mplew.writeInt(mons.getObjectId());
		MonsterStatusEffect ms = (MonsterStatusEffect) mse.get(0);
		if (ms.getStati() == MonsterStatus.POISON) {
			PacketHelper.writeSingleMask(mplew, MonsterStatus.EMPTY);
			mplew.write(mse.size());
			for (MonsterStatusEffect m : mse) {
				mplew.writeInt(m.getFromID());
				if (m.isMonsterSkill()) {
					mplew.writeShort(m.getMobSkill().getSkillId());
					mplew.writeShort(m.getMobSkill().getSkillLevel());
				} else if (m.getSkill() > 0) {
					mplew.writeInt(m.getSkill());
				}
				mplew.writeInt(m.getX().intValue());
				mplew.writeInt(1000);
				mplew.writeInt(0);
				mplew.writeInt(8000);// new v141
				mplew.writeInt(6);
				mplew.writeInt(0);
			}
			mplew.writeShort(1000);// was 300
			mplew.write(2);// was 1
			// mplew.write(1);
		} else {
			PacketHelper.writeSingleMask(mplew, ms.getStati());

			mplew.writeInt(ms.getX().intValue());
			if (ms.isMonsterSkill()) {
				mplew.writeShort(ms.getMobSkill().getSkillId());
				mplew.writeShort(ms.getMobSkill().getSkillLevel());
			} else if (ms.getSkill() > 0) {
				mplew.writeInt(ms.getSkill());
			}
			mplew.writeShort((short) ((ms.getCancelTask() - System.currentTimeMillis()) / 1000));
			mplew.writeLong(0L);
			mplew.writeShort(0);
			mplew.write(1);
		}
		// System.out.println("Monsterstatus3");
		return mplew.getPacket();
	}

	public static byte[] applyMonsterStatus(int oid, Map<MonsterStatus, Integer> stati, List<Integer> reflection,
			MobSkill skil) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.APPLY_MONSTER_STATUS.getValue());
		mplew.writeInt(oid);
		PacketHelper.writeMask(mplew, stati.keySet());

		for (Map.Entry mse : stati.entrySet()) {
			mplew.writeInt(((Integer) mse.getValue()).intValue());
			mplew.writeInt(skil.getSkillId());
			mplew.writeShort((short) skil.getDuration());
		}

		for (Integer ref : reflection) {
			mplew.writeInt(ref.intValue());
		}
		mplew.writeLong(0L);
		mplew.writeShort(0);

		int size = stati.size();
		if (reflection.size() > 0) {
			size /= 2;
		}
		mplew.write(size);
		return mplew.getPacket();
	}

	public static byte[] applyPoison(MapleMonster mons, List<MonsterStatusEffect> mse) {
		if ((mse.size() <= 0) || (mse.get(0) == null)) {
			return CWvsContext.enableActions();
		}
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.APPLY_MONSTER_STATUS.getValue());
		mplew.writeInt(mons.getObjectId());
		PacketHelper.writeSingleMask(mplew, MonsterStatus.EMPTY);
		mplew.write(mse.size());
		for (MonsterStatusEffect m : mse) {
			mplew.writeInt(m.getFromID());
			if (m.isMonsterSkill()) {
				mplew.writeShort(m.getMobSkill().getSkillId());
				mplew.writeShort(m.getMobSkill().getSkillLevel());
			} else if (m.getSkill() > 0) {
				mplew.writeInt(m.getSkill());
			}
			mplew.writeInt(m.getX().intValue());
			mplew.writeInt(1000);
			mplew.writeInt(0);// 600574518?
			mplew.writeInt(8000);// war 7000
			mplew.writeInt(6);// was 5
			mplew.writeInt(0);
		}
		mplew.writeShort(1000);// was 300
		mplew.write(2);// was 1
		// mplew.write(1);
		// System.out.println("Monsterstatus5");
		return mplew.getPacket();
	}

	public static byte[] cancelMonsterStatus(int oid, MonsterStatus stat) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.CANCEL_MONSTER_STATUS.getValue());
		mplew.writeInt(oid);
		PacketHelper.writeSingleMask(mplew, stat);
		mplew.write(4);// was1
		mplew.write(2);

		return mplew.getPacket();
	}

	public static byte[] cancelPoison(int oid, MonsterStatusEffect m) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.CANCEL_MONSTER_STATUS.getValue());
		mplew.writeInt(oid);
		PacketHelper.writeSingleMask(mplew, MonsterStatus.EMPTY);
		mplew.writeInt(0);
		mplew.writeInt(1);
		mplew.writeInt(m.getFromID());
		if (m.isMonsterSkill()) {
			mplew.writeShort(m.getMobSkill().getSkillId());
			mplew.writeShort(m.getMobSkill().getSkillLevel());
		} else if (m.getSkill() > 0) {
			mplew.writeInt(m.getSkill());
		}
		mplew.write(3);

		return mplew.getPacket();
	}

	public static byte[] talkMonster(int oid, int itemId, String msg) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.TALK_MONSTER.getValue());
		mplew.writeInt(oid);
		mplew.writeInt(500);
		mplew.writeInt(itemId);
		mplew.write(itemId <= 0 ? 0 : 1);
		mplew.write((msg == null) || (msg.length() <= 0) ? 0 : 1);
		if ((msg != null) && (msg.length() > 0)) {
			mplew.writeMapleAsciiString(msg);
		}
		mplew.writeInt(1);

		return mplew.getPacket();
	}

	public static byte[] removeTalkMonster(int oid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.REMOVE_TALK_MONSTER.getValue());
		mplew.writeInt(oid);

		return mplew.getPacket();
	}

	public static final byte[] getNodeProperties(MapleMonster objectid, MapleMap map) {
		if (objectid.getNodePacket() != null) {
			return objectid.getNodePacket();
		}
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.MONSTER_PROPERTIES.getValue());
		mplew.writeInt(objectid.getObjectId());
		mplew.writeInt(map.getNodes().size());
		mplew.writeInt(objectid.getPosition().x);
		mplew.writeInt(objectid.getPosition().y);
		for (MapleNodes.MapleNodeInfo mni : map.getNodes()) {
			mplew.writeInt(mni.x);
			mplew.writeInt(mni.y);
			mplew.writeInt(mni.attr);
			if (mni.attr == 2) {
				mplew.writeInt(500);
			}
		}
		mplew.writeInt(0);
		mplew.write(0);
		mplew.write(0);

		objectid.setNodePacket(mplew.getPacket());
		return objectid.getNodePacket();
	}

	public static byte[] showMagnet(int mobid, boolean success) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.SHOW_MAGNET.getValue());
		mplew.writeInt(mobid);
		mplew.write(success ? 1 : 0);
		mplew.write(0);

		return mplew.getPacket();
	}

	public static byte[] catchMonster(int mobid, int itemid, byte success) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

		mplew.writeShort(SendPacketOpcode.CATCH_MONSTER.getValue());
		mplew.writeInt(mobid);
		mplew.writeInt(itemid);
		mplew.write(success);

		return mplew.getPacket();
	}
}
