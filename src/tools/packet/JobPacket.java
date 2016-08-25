/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tools.packet;

import client.MapleBuffStat;
import client.MapleCharacter;
import handling.SendPacketOpcode;

import java.awt.Point;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import server.Randomizer;
import server.MapleStatEffect;
import tools.HexTool;
import tools.data.MaplePacketLittleEndianWriter;

/**
 *
 * @author Itzik
 */
public class JobPacket {

	public static class WindArcherPacket {
		public static byte[] giveWindArcherBuff(int buffid, int bufflength, Map<MapleBuffStat, Integer> statups,
				MapleStatEffect effect, MapleCharacter chr) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
			PacketHelper.writeBuffMask(mplew, statups);
			try {
				byte count = 0;
				StringBuilder statValue = new StringBuilder();
				Map.Entry[] stat = new Map.Entry[statups.size()];
				for (Map.Entry temp : statups.entrySet()) {
					stat[count] = temp;
					statValue.append((int) stat[count].getValue()).append(" - ");
					count++;
				}
				switch (buffid) {
				case 13001022:
					mplew.writeShort((int) stat[1].getValue());
					mplew.writeInt(buffid);
					mplew.writeInt(bufflength);

					mplew.write0(9);
					mplew.writeInt(1);
					mplew.writeInt(buffid);
					mplew.writeInt((int) stat[0].getValue());
					mplew.write(HexTool.getByteArrayFromHexString("10 00 32 23 00 00 00 00"));
					mplew.writeInt(bufflength);
					mplew.write(HexTool.getByteArrayFromHexString("00 00 00 00 01 00 00 00 00"));
					break;
				case 13101024:// Sylvan Aid now fiex :3
					mplew.writeShort((int) stat[0].getValue());
					mplew.writeInt(buffid);
					mplew.writeInt(bufflength);

					mplew.writeShort((int) stat[2].getValue());
					mplew.writeInt(buffid);
					mplew.writeInt(bufflength);

					mplew.write0(9);
					mplew.writeInt(1);
					mplew.writeInt(buffid);
					mplew.writeInt((int) stat[1].getValue());
					mplew.writeLong(0);
					mplew.writeInt(bufflength);
					mplew.writeInt(0);
					mplew.write(1);
					mplew.writeInt(0);
					break;
				case 13111023:// albatross work
				case 13120008:// max albatross NOW WORK - When albatross
								// finished, you active again skill, else give
								// DC and Error38
					mplew = new MaplePacketLittleEndianWriter();
					mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
					mplew.write(HexTool.getByteArrayFromHexString(
							"00 00 00 00 00 00 00 00 00 40 00 00 30 00 04 00 00 00 00 00 00 01 00 82 00 00 00 00 00 14 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "));
					int skillLevel = chr.getSkillLevel(13111023);
					int skill13120008Level = chr.getSkillLevel(13120008);
					int skill13111023Level = chr.getSkillLevel(13111023);

					mplew.writeShort(skill13120008Level / 2);
					mplew.writeInt(13120008);
					mplew.writeInt(205999);

					mplew.writeShort(skill13120008Level);
					mplew.writeInt(13120008);
					mplew.writeInt(205999);

					mplew.write0(13);
					mplew.writeInt(2);

					mplew.writeInt(13120008);
					mplew.writeInt((skill13120008Level * 5) / 3);
					mplew.write(HexTool.getByteArrayFromHexString("56 9F 33 23 01 00 00 00"));
					mplew.writeInt(bufflength);

					mplew.writeInt(1);
					mplew.writeInt(13120008);
					mplew.writeInt((skillLevel * 300) / 4);
					mplew.write(HexTool.getByteArrayFromHexString("56 9F 33 23 01 00 00 00"));
					mplew.writeInt(bufflength);

					mplew.writeInt(1);
					mplew.writeInt(13120008);
					mplew.writeInt(-2);
					mplew.write(HexTool.getByteArrayFromHexString("56 9F 33 23 01 00 00 00"));
					mplew.writeInt(bufflength);

					mplew.writeInt(2);
					mplew.writeInt(13111023);
					mplew.writeInt(skill13111023Level);
					mplew.write(HexTool.getByteArrayFromHexString("10 00 32 23 47 9F 01 00"));
					mplew.writeInt(bufflength);

					mplew.writeInt(13120008);
					mplew.writeInt((skill13120008Level / 2) + 10);
					mplew.write(HexTool.getByteArrayFromHexString("56 9F 33 23 01 00 00 00"));
					mplew.writeInt(bufflength);

					mplew.writeInt(1);
					mplew.writeInt(13120008);
					mplew.writeInt((skill13120008Level / 2));
					mplew.write(HexTool.getByteArrayFromHexString("56 9F 33 23 01 00 00 00"));
					mplew.writeInt(bufflength);

					mplew.writeInt(1);
					mplew.writeInt(13120008);
					mplew.writeInt((skill13120008Level / 2));
					mplew.write(HexTool.getByteArrayFromHexString("56 9F 33 23 01 00 00 00"));
					mplew.writeInt(bufflength);

					mplew.writeInt(1);
					mplew.writeInt(13120008);
					mplew.writeInt((skill13120008Level / 2) + 10);
					mplew.write(HexTool.getByteArrayFromHexString("56 9F 33 23 01 00 00 00"));
					mplew.writeInt(bufflength);

					mplew.writeInt(0);
					mplew.write(1);
					mplew.writeInt(0);
					break;
				case 13121004:// touch of the wind skill work
					mplew.writeShort((int) stat[0].getValue());
					mplew.writeInt(buffid);
					mplew.writeInt(bufflength);

					mplew.writeShort((int) stat[1].getValue());
					mplew.writeInt(buffid);
					mplew.writeInt(bufflength);

					mplew.writeShort((int) stat[2].getValue());
					mplew.writeInt(buffid);
					mplew.writeInt(bufflength);

					mplew.write0(9);
					mplew.writeInt(1);
					mplew.writeInt(buffid);
					mplew.writeInt((int) stat[3].getValue());
					mplew.write(HexTool.getByteArrayFromHexString("C3 BF BB 33 00 00 00 00"));
					mplew.writeInt(bufflength);
					mplew.writeInt(0);
					mplew.write(1);
					mplew.writeInt(0);
					break;
				}
			} catch (Exception ez) {
				ez.printStackTrace();
			}
			return mplew.getPacket();
		}

		public static byte[] TrifleWind(int cid, int skillid, int ga, int oid, int gu) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendPacketOpcode.GAIN_FORCE.getValue());
			mplew.write(0);
			mplew.writeInt(cid);
			mplew.writeInt(7);
			mplew.write(1);
			mplew.writeInt(gu);
			mplew.writeInt(oid);
			mplew.writeInt(skillid);
			for (int i = 1; i < ga; i++) {
				mplew.write(1);
				mplew.writeInt(2 + i);
				mplew.writeInt(1);
				mplew.writeInt(Randomizer.rand(0x2A, 0x2F));
				mplew.writeInt(7 + i);
				mplew.writeInt(Randomizer.rand(5, 0xAB));
				mplew.writeInt(Randomizer.rand(0, 0x37));
				mplew.writeLong(0);
				mplew.writeInt(Randomizer.nextInt());
				mplew.writeInt(0);
			}
			mplew.write(0);

			mplew.write0(69); // for no dc goodluck charm! >:D xD

			return mplew.getPacket();
		}
	}

	public static class PhantomPacket {

		public static byte[] addStolenSkill(int jobNum, int index, int skill, int level) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.UPDATE_STOLEN_SKILLS.getValue());
			mplew.write(1);
			mplew.write(0);
			mplew.writeInt(jobNum);
			mplew.writeInt(index);
			mplew.writeInt(skill);
			mplew.writeInt(level);
			mplew.writeInt(0);
			mplew.write(0);

			return mplew.getPacket();
		}

		public static byte[] removeStolenSkill(int jobNum, int index) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.UPDATE_STOLEN_SKILLS.getValue());
			mplew.write(1);
			mplew.write(3);
			mplew.writeInt(jobNum);
			mplew.writeInt(index);
			mplew.write(0);

			return mplew.getPacket();
		}

		public static byte[] replaceStolenSkill(int base, int skill) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.REPLACE_SKILLS.getValue());
			mplew.write(1);
			mplew.write(skill > 0 ? 1 : 0);
			mplew.writeInt(base);
			mplew.writeInt(skill);

			return mplew.getPacket();
		}

		public static byte[] gainCardStack(int oid, int runningId, int color, int skillid, int damage, int times) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.GAIN_FORCE.getValue());
			mplew.write(0);
			mplew.writeInt(oid);
			mplew.writeInt(1);
			mplew.writeInt(damage);
			mplew.writeInt(skillid);
			for (int i = 0; i < times; i++) {
				mplew.write(1);
				mplew.writeInt(damage == 0 ? runningId + i : runningId);
				mplew.writeInt(color);
				mplew.writeInt(Randomizer.rand(15, 29));
				mplew.writeInt(Randomizer.rand(7, 11));
				mplew.writeInt(Randomizer.rand(0, 9));
			}
			mplew.write(0);

			mplew.write0(69); // for no DC it requires this do not
										// remove

			return mplew.getPacket();
		}

		public static byte[] updateCardStack(final int total) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.PHANTOM_CARD.getValue());
			mplew.write(total);

			return mplew.getPacket();
		}

		public static byte[] getCarteAnimation(int cid, int oid, int job, int total, int numDisplay) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.GAIN_FORCE.getValue());
			mplew.write(0);
			mplew.writeInt(cid);
			mplew.writeInt(1);

			mplew.writeInt(oid);
			mplew.writeInt(job == 2412 ? 24120002 : 24100003);
			mplew.write(1);
			for (int i = 1; i <= numDisplay; i++) {
				mplew.writeInt(total - (numDisplay - i));
				mplew.writeInt(job == 2412 ? 2 : 0);

				mplew.writeInt(15 + Randomizer.nextInt(15));
				mplew.writeInt(7 + Randomizer.nextInt(5));
				mplew.writeInt(Randomizer.nextInt(4));

				mplew.write(i == numDisplay ? 0 : 1);
			}

			return mplew.getPacket();
		}

		public static byte[] giveAriaBuff(int bufflevel, int buffid, int bufflength) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
			Map<MapleBuffStat, Integer> statups = new EnumMap<>(MapleBuffStat.class);
			statups.put(MapleBuffStat.DAMAGE_RATE, 0);
			statups.put(MapleBuffStat.DAMAGE_PERCENT, 0);
			PacketHelper.writeBuffMask(mplew, statups);
			for (int i = 0; i < 2; i++) {
				mplew.writeShort(bufflevel);
				mplew.writeInt(buffid);
				mplew.writeInt(bufflength);
			}
			mplew.write0(3);
			mplew.writeShort(0);
			mplew.write(0);
			return mplew.getPacket();
		}
	}

	public static class AngelicPacket {

		public static byte[] showRechargeEffect() {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
			mplew.write(0x2D);
			return mplew.getPacket();
		}

		public static byte[] RechargeEffect() {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
			mplew.write(0x2D);
			return mplew.getPacket();
		}

		public static byte[] DressUpTime(byte type) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
			mplew.write(type);
			mplew.writeShort(7707);
			mplew.write(2);
			mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
			return mplew.getPacket();
		}

		public static byte[] updateDress(int transform, MapleCharacter chr) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendPacketOpcode.ANGELIC_CHANGE.getValue());
			mplew.writeInt(chr.getId());
			mplew.writeInt(transform);
			return mplew.getPacket();
		}

		public static byte[] lockSkill(int skillid) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendPacketOpcode.LOCK_CHARGE_SKILL.getValue());
			mplew.writeInt(skillid);
			return mplew.getPacket();
		}

		public static byte[] unlockSkill() {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendPacketOpcode.UNLOCK_CHARGE_SKILL.getValue());
			mplew.writeInt(0);
			return mplew.getPacket();
		}

		public static byte[] absorbingSoulSeeker(int characterid, int size, Point essence1, Point essence2, int skillid,
				boolean creation) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.GAIN_FORCE.getValue());
			mplew.write(!creation ? 0 : 1);
			mplew.writeInt(characterid);
			if (!creation) {
				// false
				mplew.writeInt(3);
				mplew.write(1);
				mplew.write(size);
				mplew.write0(3);
				mplew.writeShort(essence1.x);
				mplew.writeShort(essence1.y);
				mplew.writeShort(essence2.y);
				mplew.writeShort(essence2.x);
			} else {
				// true
				mplew.writeShort(essence1.x);
				mplew.writeShort(essence1.y);
				mplew.writeInt(4);
				mplew.write(1);
				mplew.writeShort(essence1.y);
				mplew.writeShort(essence1.x);
			}
			mplew.writeInt(skillid);
			if (!creation) {
				for (int i = 0; i < 2; i++) {
					mplew.write(1);
					mplew.writeInt(Randomizer.rand(19, 20));
					mplew.writeInt(1);
					mplew.writeInt(Randomizer.rand(18, 19));
					mplew.writeInt(Randomizer.rand(20, 23));
					mplew.writeInt(Randomizer.rand(36, 55));
					mplew.writeInt(540);
					mplew.writeShort(0);// new 142
					mplew.write0(6);// new 143
				}
			} else {
				mplew.write(1);
				mplew.writeInt(Randomizer.rand(6, 21));
				mplew.writeInt(1);
				mplew.writeInt(Randomizer.rand(42, 45));
				mplew.writeInt(Randomizer.rand(4, 7));
				mplew.writeInt(Randomizer.rand(267, 100));
				mplew.writeInt(0);// 540
				mplew.writeInt(0);
				mplew.writeInt(0);
			}
			mplew.write(0);
			return mplew.getPacket();
		}

		public static byte[] SoulSeekerRegen(MapleCharacter chr, int sn) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendPacketOpcode.GAIN_FORCE.getValue());
			mplew.write(1);
			mplew.writeInt(chr.getId());
			mplew.writeInt(sn);
			mplew.writeInt(4);
			mplew.write(1);
			mplew.writeInt(sn);
			mplew.writeInt(65111007); // hide skills
			mplew.write(1);
			mplew.writeInt(Randomizer.rand(0x06, 0x10));
			mplew.writeInt(1);
			mplew.writeInt(Randomizer.rand(0x28, 0x2B));
			mplew.writeInt(Randomizer.rand(0x03, 0x04));
			mplew.writeInt(Randomizer.rand(0xFA, 0x49));
			mplew.writeInt(0);
			mplew.writeLong(0);
			mplew.write(0);
			return mplew.getPacket();
		}

		public static byte[] SoulSeeker(MapleCharacter chr, int skillid, int sn, int sc1, int sc2) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendPacketOpcode.GAIN_FORCE.getValue());
			mplew.write(0);
			mplew.writeInt(chr.getId());
			mplew.writeInt(3);
			mplew.write(1);
			mplew.writeInt(sn);
			if (sn >= 1) {
				mplew.writeInt(sc1);// SHOW_ITEM_GAIN_INCHAT
				if (sn == 2) {
					mplew.writeInt(sc2);
				}
			}
			mplew.writeInt(65111007); // hide skills
			for (int i = 0; i < 2; i++) {
				mplew.write(1);
				mplew.writeInt(i + 2);
				mplew.writeInt(1);
				mplew.writeInt(Randomizer.rand(0x0F, 0x10));
				mplew.writeInt(Randomizer.rand(0x1B, 0x22));
				mplew.writeInt(Randomizer.rand(0x1F, 0x24));
				mplew.writeInt(540);
				mplew.writeInt(0);// wasshort new143
				mplew.writeInt(0);// new143
			}
			mplew.write(0);
			return mplew.getPacket();
		}
	}

	public static class LuminousPacket {

		public static byte[] updateLuminousGauge(int darktotal, int lighttotal, int darktype, int lighttype) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.LUMINOUS_COMBO.getValue());
			mplew.writeInt(darktotal);
			mplew.writeInt(lighttotal);
			mplew.writeInt(darktype);
			mplew.writeInt(lighttype);
			mplew.writeInt(281874974);// 1210382225

			mplew.write0(69); // for no dc

			return mplew.getPacket();
		}

		public static byte[] giveLuminousState(int skill, int light, int dark, int duration) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());

			PacketHelper.writeSingleMask(mplew, MapleBuffStat.LUMINOUS_GAUGE);

			mplew.writeShort(1);
			mplew.writeInt(skill); // 20040217
			mplew.writeInt(duration);
			mplew.write0(5);
			mplew.writeInt(skill); // 20040217
			mplew.writeInt(483195070);
			mplew.write0(8);
			mplew.writeInt(Math.max(light, -1)); // light gauge
			mplew.writeInt(Math.max(dark, -1)); // dark gauge
			mplew.writeInt(1);
			mplew.writeInt(1);// was2
			mplew.writeInt(283183599);
			mplew.writeInt(0);
			mplew.writeInt(0);// new143
			mplew.writeInt(1);
			mplew.write(0);

			mplew.write0(69); // for no dc

			return mplew.getPacket();
		}
	}

	public static class XenonPacket {

		public static byte[] giveXenonSupply(short amount) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
			PacketHelper.writeSingleMask(mplew, MapleBuffStat.SUPPLY_SURPLUS);

			mplew.writeShort(amount);
			mplew.writeInt(30020232); // skill id
			mplew.writeInt(-1); // duration
			mplew.write0(18);

			return mplew.getPacket();
		}

		public static byte[] giveAmaranthGenerator() {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
			Map<MapleBuffStat, Integer> statups = new EnumMap<>(MapleBuffStat.class);
			statups.put(MapleBuffStat.SUPPLY_SURPLUS, 0);
			statups.put(MapleBuffStat.AMARANTH_GENERATOR, 0);
			PacketHelper.writeBuffMask(mplew, statups);

			mplew.writeShort(20); // gauge fill
			mplew.writeInt(30020232); // skill id
			mplew.writeInt(-1); // duration

			mplew.writeShort(1);
			mplew.writeInt(36121054); // skill id
			mplew.writeInt(10000); // duration

			mplew.write0(5);
			mplew.writeInt(1000);
			mplew.writeInt(1);
			mplew.write0(1);

			mplew.write0(69); // for no dc

			return mplew.getPacket();
		}

		public static byte[] PinPointRocket(int cid, List<Integer> moblist) {
			MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
			packet.writeShort(SendPacketOpcode.GAIN_FORCE.getValue());
			packet.write(0);
			packet.writeInt(cid);
			packet.writeInt(6);
			packet.write(1);
			packet.writeInt(moblist.size());
			for (int i = 0; i < moblist.size(); i++) {
				packet.writeInt(moblist.get(i));
			}
			packet.writeInt(36001005);
			for (int i = 1; i <= moblist.size(); i++) {
				packet.write(1);
				packet.writeInt(i + 7);
				packet.writeInt(0);
				packet.writeInt(Randomizer.rand(10, 20));
				packet.writeInt(Randomizer.rand(20, 40));
				packet.writeInt(Randomizer.rand(40, 200));
				packet.writeInt(Randomizer.rand(500, 2000));
				packet.writeLong(0); // v196
				packet.writeInt(Randomizer.nextInt());
				packet.writeInt(0);
			}
			packet.write(0);

			packet.write0(69); // for no dc
			return packet.getPacket();
		}

		public static byte[] MegidoFlameRe(int cid, int oid) {
			MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
			packet.writeShort(SendPacketOpcode.GAIN_FORCE.getValue());
			packet.write(0);
			packet.writeInt(cid);
			packet.writeInt(3);
			packet.write(1);
			packet.writeInt(1);
			packet.writeInt(oid);
			packet.writeInt(2121055);
			packet.write(1);
			packet.writeInt(2);
			packet.writeInt(2);
			packet.writeInt(Randomizer.rand(10, 17));
			packet.writeInt(Randomizer.rand(10, 16));
			packet.writeInt(Randomizer.rand(40, 52));
			packet.writeInt(20);
			packet.writeLong(0);
			packet.writeLong(0);
			packet.write(0);
			packet.write0(69); // for no dc
			return packet.getPacket();
		}

		public static byte[] ShieldChacingRe(int cid, int unkwoun, int oid, int unkwoun2, int unkwoun3) {
			MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
			packet.writeShort(SendPacketOpcode.GAIN_FORCE.getValue());
			packet.write(1);
			packet.writeInt(cid);
			packet.writeInt(unkwoun);
			packet.writeInt(4);
			packet.write(1);
			packet.writeInt(oid);
			packet.writeInt(31221014);

			packet.write(1);
			packet.writeInt(unkwoun2 + 1);
			packet.writeInt(3);
			packet.writeInt(unkwoun3);
			packet.writeInt(3);
			packet.writeInt(Randomizer.rand(36, 205));
			packet.writeInt(0);
			packet.writeLong(0);
			packet.writeInt(Randomizer.nextInt());
			packet.writeInt(0);
			packet.write(0);
			packet.write0(69); // for no dc
			return packet.getPacket();
		}

		public static byte[] ShieldChacing(int cid, List<Integer> moblist, int skillid) {
			MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
			packet.writeShort(SendPacketOpcode.GAIN_FORCE.getValue());
			packet.write(0);
			packet.writeInt(cid);
			packet.writeInt(3);
			packet.write(1);
			packet.writeInt(moblist.size());
			for (int i = 0; i < moblist.size(); i++) {
				packet.writeInt(((Integer) moblist.get(i)).intValue());
			}
			packet.writeInt(skillid);
			for (int i = 1; i <= moblist.size(); i++) {
				packet.write(1);
				packet.writeInt(1 + i);
				packet.writeInt(3);
				packet.writeInt(Randomizer.rand(1, 20));
				packet.writeInt(Randomizer.rand(20, 50));
				packet.writeInt(Randomizer.rand(50, 200));
				packet.writeInt(skillid == 2121055 ? 720 : 660);
				packet.writeLong(0);
				packet.writeInt(Randomizer.nextInt());
				packet.writeInt(0);
			}
			packet.write(0);
			packet.write0(69); // for no dc
			return packet.getPacket();
		}

		public static byte[] EazisSystem(int cid, int oid) {
			MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
			packet.writeShort(SendPacketOpcode.GAIN_FORCE.getValue());
			packet.write(0);
			packet.writeInt(cid);
			packet.writeInt(5);
			packet.write(1);
			packet.writeInt(oid);
			packet.writeInt(36110004);
			for (int i = 0; i < 3; i++) {
				packet.write(1);
				packet.writeInt(i + 2);
				packet.writeInt(0);
				packet.writeInt(0x23);
				packet.writeInt(5);
				packet.writeInt(Randomizer.rand(80, 100));
				packet.writeInt(Randomizer.rand(200, 300));
				packet.writeLong(0); // v196
				packet.writeInt(Randomizer.nextInt());
				packet.writeInt(0);
			}
			packet.write(0);
			packet.write0(69); // for no dc
			return packet.getPacket();
		}
	}

	public static class AvengerPacket {

		public static byte[] giveAvengerHpBuff(int hp) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());

			PacketHelper.writeSingleMask(mplew, MapleBuffStat.LUNAR_TIDE);
			mplew.writeShort(3);
			mplew.writeInt(0);
			mplew.writeInt(2100000000);
			mplew.write0(5);
			mplew.writeInt(hp);
			mplew.write0(9);

			mplew.write0(69); // for no dc

			return mplew.getPacket();
		}

		public static byte[] giveExceed(short amount) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
			PacketHelper.writeSingleMask(mplew, MapleBuffStat.EXCEED);

			mplew.writeShort(amount);
			mplew.writeInt(30010230); // skill id
			mplew.writeInt(-1); // duration
			mplew.write0(14);

			mplew.write0(69); // for no dc

			return mplew.getPacket();
		}

		public static byte[] giveExceedAttack(int skill, short amount) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
			PacketHelper.writeSingleMask(mplew, MapleBuffStat.EXCEED_ATTACK);

			mplew.writeShort(amount);
			mplew.writeInt(skill); // skill id
			mplew.writeInt(15000); // duration
			mplew.write0(14);

			mplew.write0(69); // for no dc

			return mplew.getPacket();
		}

		public static byte[] cancelExceed() {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.CANCEL_BUFF.getValue());

			Map<MapleBuffStat, Integer> statups = new EnumMap<>(MapleBuffStat.class);
			statups.put(MapleBuffStat.EXCEED, 0);
			statups.put(MapleBuffStat.EXCEED_ATTACK, 0);
			PacketHelper.writeBuffMask(mplew, statups);

			return mplew.getPacket();
		}
	}

	public static class DawnWarriorPacket {

		public static byte[] giveMoonfallStance(int level) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
			Map<MapleBuffStat, Integer> statups = new EnumMap<>(MapleBuffStat.class);
			statups.put(MapleBuffStat.CRITICAL_PERCENT_UP, 0);
			statups.put(MapleBuffStat.MOON_STANCE2, 0);
			statups.put(MapleBuffStat.WARRIOR_STANCE, 0);
			PacketHelper.writeBuffMask(mplew, statups);

			mplew.writeShort(level);
			mplew.writeInt(11101022);
			mplew.writeInt(Integer.MAX_VALUE);
			mplew.writeShort(1);
			mplew.writeInt(11101022);
			mplew.writeInt(Integer.MAX_VALUE);
			mplew.writeInt(0);
			mplew.write(5);
			mplew.write(1);
			mplew.writeInt(1);
			mplew.writeInt(11101022);
			mplew.writeInt(level);
			mplew.writeInt(Integer.MAX_VALUE);
			mplew.writeInt(0);
			mplew.writeInt(0);
			mplew.write(1);
			mplew.writeInt(0);

			mplew.write0(69); // for no dc

			return mplew.getPacket();
		}

		public static byte[] giveSunriseStance(int level) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
			Map<MapleBuffStat, Integer> statups = new EnumMap<>(MapleBuffStat.class);
			statups.put(MapleBuffStat.BOOSTER, 0);
			statups.put(MapleBuffStat.DAMAGE_PERCENT, 0);
			statups.put(MapleBuffStat.WARRIOR_STANCE, 0);
			PacketHelper.writeBuffMask(mplew, statups);

			mplew.writeShort(level);
			mplew.writeInt(11111022);
			mplew.writeInt(Integer.MAX_VALUE);
			mplew.writeInt(0);
			mplew.write(5);
			mplew.write(1);
			mplew.writeInt(1);
			mplew.writeInt(11111022);
			mplew.writeInt(-1);
			mplew.writeInt(Integer.MAX_VALUE);
			mplew.writeInt(0);
			mplew.writeInt(1);
			mplew.writeInt(11111022);
			mplew.writeInt(0x19);
			mplew.writeInt(Integer.MAX_VALUE);
			mplew.writeInt(0);
			mplew.writeInt(0);
			mplew.write(1);
			mplew.writeInt(0);

			mplew.write0(69); // for no dc

			return mplew.getPacket();
		}

		public static byte[] giveEquinox_Moon(int level, int duration) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
			Map<MapleBuffStat, Integer> statups = new EnumMap<>(MapleBuffStat.class);
			statups.put(MapleBuffStat.CRITICAL_PERCENT_UP, 0);
			statups.put(MapleBuffStat.MOON_STANCE2, 0);
			statups.put(MapleBuffStat.EQUINOX_STANCE, 0);
			PacketHelper.writeBuffMask(mplew, statups);

			mplew.writeShort(level);
			mplew.writeInt(11121005);
			mplew.writeInt(duration);
			mplew.writeShort(1);
			mplew.writeInt(11121005);
			mplew.writeInt(duration);
			mplew.writeInt(0);
			mplew.write(5);
			mplew.writeInt(1);
			mplew.writeInt(11121005);
			mplew.writeInt(level);
			mplew.writeInt(duration);
			mplew.writeInt(duration);
			mplew.writeInt(0);
			mplew.write(1);
			mplew.writeInt(0);

			mplew.write0(69); // for no dc

			return mplew.getPacket();
		}

		public static byte[] giveEquinox_Sun(int level, int duration) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

			mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
			Map<MapleBuffStat, Integer> statups = new EnumMap<>(MapleBuffStat.class);
			statups.put(MapleBuffStat.BOOSTER, 0);
			statups.put(MapleBuffStat.DAMAGE_PERCENT, 0);
			statups.put(MapleBuffStat.EQUINOX_STANCE, 0);
			PacketHelper.writeBuffMask(mplew, statups);

			mplew.writeShort(level);
			mplew.writeInt(11121005);
			mplew.writeInt(duration);
			mplew.writeInt(0);
			mplew.write(5);
			mplew.writeInt(1);
			mplew.writeInt(11121005);
			mplew.writeInt(-1);
			mplew.writeInt(duration);
			mplew.writeInt(duration);
			mplew.writeInt(1);
			mplew.writeInt(11121005);
			mplew.writeInt(0x19);
			mplew.writeInt(duration);
			mplew.writeInt(duration);
			mplew.writeInt(0);
			mplew.write(1);
			mplew.writeInt(0);

			mplew.write0(69); // for no dc

			return mplew.getPacket();
		}
	}
}
