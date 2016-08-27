package handling.handlers.login;

import client.MapleCharacter;
import client.MapleCharacterUtil;
import client.MapleClient;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import constants.JobConstants;
import handling.PacketHandler;
import handling.RecvPacketOpcode;
import handling.login.LoginInformationProvider;
import handling.login.LoginInformationProvider.JobType;
import server.MapleItemInformationProvider;
import tools.data.LittleEndianAccessor;
import tools.packet.LoginPacket;

public class CreateNewCharacter {

	@PacketHandler(opcode = RecvPacketOpcode.CREATE_CHAR)
	public static void handle(MapleClient c, LittleEndianAccessor slea) {
		
		String name;
		byte gender, skin;
		short subcategory;
		int face, hair, hairColor = -1, hat = -1, top, bottom = -1, shoes, weapon, cape = -1, faceMark = -1, shield = -1;
		
		name = slea.readMapleAsciiString();

		if (!MapleCharacterUtil.canCreateChar(name, false)) {
			System.out.println("char name hack: " + name);
			return;
		}

		slea.skip(4); // key type setting
		slea.skip(4); // -1
		
		int job_type = slea.readInt();
		JobType job = JobType.getByType(job_type);
		
		if (job == null) {
			System.out.println("New job type found: " + job_type);
			return;
		}

		for (JobConstants.LoginJob j : JobConstants.LoginJob.values()) {
			if (j.getJobType() == job_type) {
				if (j.getFlag() != JobConstants.LoginJob.JobFlag.ENABLED.getFlag()) {
					System.out.println("job was tried to be created while not enabled");
					return;
				}
			}
		}

		subcategory = slea.readShort();
		gender = slea.readByte();
		skin = slea.readByte();
		
		slea.skip(1);
		
		face = slea.readInt();
		hair = slea.readInt();
		
		if (job.hairColor) {
			hairColor = slea.readInt();
		}
		
		if (job.skinColor) {
			slea.readInt();
		}
		
		if (job.faceMark) {
			faceMark = slea.readInt();
		}
		
		if (job.hat) {
			hat = slea.readInt();
		}
		
		top = slea.readInt();
		
		if (job.bottom) {
			bottom = slea.readInt();
		}
		
		if (job.cape) {
			cape = slea.readInt();
		}

		shoes = slea.readInt();
		weapon = slea.readInt();

		if (slea.available() >= 4) {
			shield = slea.readInt();
		}

		MapleCharacter newchar = MapleCharacter.getDefault(c, job);
		newchar.setWorld((byte) c.getWorld());
		newchar.setFace(face);
		newchar.setSecondFace(face);
		
		if (hairColor < 0) {
			hairColor = 0;
		}
		
		if (job != JobType.Mihile) {
			hair += hairColor;
		}
		
		newchar.setHair(hair);
		newchar.setSecondHair(hair);
		
		if (job == JobType.AngelicBuster) {
			newchar.setSecondFace(21173);
			newchar.setSecondHair(37141);
			newchar.setLevel((short) 10);
			newchar.getStat().int_ = 4;
			newchar.getStat().dex = 57;
			newchar.getStat().maxhp = 1500;
			newchar.getStat().hp = 1500;
			newchar.getStat().maxmp = 1500;
			newchar.getStat().mp = 1500;
			newchar.setRemainingSp(3);
		} else if (job == JobType.Zero) {
			newchar.setSecondFace(21290);
			newchar.setSecondHair(37623);
		}
		
		newchar.setGender(gender);
		newchar.setName(name);
		newchar.setSkinColor(skin);
		
		if (faceMark < 0) {
			faceMark = 0;
		}
		
		newchar.setFaceMarking(faceMark);
		final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
		final MapleInventory equip = newchar.getInventory(MapleInventoryType.EQUIPPED);
		Item item;
		
		// TODO: Check zero's beta weapon slot
		
		/**
		 * -1 Hat | -2 Face | -3 Eye acc | -4 Ear acc | -5 Topwear
		 * -6 Bottom | -7 Shoes | -9 Cape | -10 Shield | -11 Weapon
		 * 
		 */
		int[][] equips = new int[][] { { hat, -1 }, { top, -5 }, { bottom, -6 }, { cape, -9 }, { shoes, -7 }, { weapon, -11 }, { shield, -10 } };
		for (int[] i : equips) {
			if (i[0] > 0) {
				item = ii.getEquipById(i[0]);
				item.setPosition((byte) i[1]);
				item.setGMLog("Character Creation");
				equip.addFromDB(item);
			}
		}
		
		int[][] guidebooks = new int[][] { { 4161001, 0 }, { 4161047, 1 }, { 4161048, 2000 }, { 4161052, 2001 },
				{ 4161054, 3 }, { 4161079, 2002 } };
		int guidebook = 0;
		for (int[] i : guidebooks) {
			if (newchar.getJob() == i[1]) {
				guidebook = i[0];
			} else if (newchar.getJob() / 1000 == i[1]) {
				guidebook = i[0];
			}
		}

		if (guidebook > 0) {
			newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(guidebook, (byte) 0, (short) 1, (byte) 0));
		}

		if (job == JobType.Zero) {
			newchar.setLevel((short) 100);
			newchar.getStat().str = 518;
			newchar.getStat().maxhp = 6910;
			newchar.getStat().hp = 6910;
			newchar.getStat().maxmp = 100;
			newchar.getStat().mp = 100;
			newchar.setRemainingSp(3, 0); // alpha
			newchar.setRemainingSp(3, 1); // beta
		}
		
		if (job == JobType.Luminous) {
			newchar.setJob((short) 2700);
			newchar.setLevel((short) 10);
			newchar.getStat().str = 4;
			newchar.getStat().int_ = 57;
			newchar.getStat().maxhp = 500;
			newchar.getStat().hp = 500;
			newchar.getStat().maxmp = 1000;
			newchar.getStat().mp = 1000;
			newchar.setRemainingSp(3);
		}

		if (MapleCharacterUtil.canCreateChar(name, c.isGm())
				&& (!LoginInformationProvider.getInstance().isForbiddenName(name) || c.isGm())
				&& (c.isGm() || c.canMakeCharacter(c.getWorld()))) {
			MapleCharacter.saveNewCharToDB(newchar, job, subcategory);
			c.getSession().write(LoginPacket.addNewCharEntry(newchar, true));
			c.createdChar(newchar.getId());
		} else {
			c.getSession().write(LoginPacket.addNewCharEntry(newchar, false));
		}
		
	}
}
