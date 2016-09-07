package tools.packet;

import client.*;
import client.inventory.*;
import constants.GameConstants;
import constants.ServerConstants;
import handling.Buffstat;
import handling.world.MapleCharacterLook;

import java.util.*;
import java.util.Map.Entry;

import server.MapleItemInformationProvider;
import server.cash.CashItem;
import server.shops.MapleShop;
import server.shops.MapleShopItem;
import server.movement.LifeMovementFragment;
import server.quest.MapleQuest;
import server.quest.MapleQuestStatus;
import server.stores.AbstractPlayerStore;
import server.stores.IMaplePlayerShop;
import tools.BitTools;
import tools.HexTool;
import tools.KoreanDateUtil;
import tools.Pair;
import tools.StringUtil;
import tools.Triple;
import tools.data.MaplePacketLittleEndianWriter;

public class PacketHelper {

    public static final long FT_UT_OFFSET = 116444592000000000L;
    public static final long MAX_TIME = 150842304000000000L;
    public static final long ZERO_TIME = 94354848000000000L;
    public static final long PERMANENT = 150841440000000000L;

    public static long getKoreanTimestamp(long realTimestamp) {
        return getTime(realTimestamp);
    }

    public static long getTime(long realTimestamp) {
        if (realTimestamp == -1L) { // 00 80 05 BB 46 E6 17 02, 1/1/2079
            return MAX_TIME;
        }
        if (realTimestamp == -2L) { // 00 40 E0 FD 3B 37 4F 01, 1/1/1900
            return ZERO_TIME;
        }
        if (realTimestamp == -3L) {
            return PERMANENT;
        }
        return realTimestamp * 10000L + 116444592000000000L;
    }

    public static long decodeTime(long fakeTimestamp) {
        if (fakeTimestamp == 150842304000000000L) {
            return -1L;
        }
        if (fakeTimestamp == 94354848000000000L) {
            return -2L;
        }
        if (fakeTimestamp == 150841440000000000L) {
            return -3L;
        }
        return (fakeTimestamp - 116444592000000000L) / 10000L;
    }

    public static long getFileTimestamp(long timeStampinMillis, boolean roundToMinutes) {
        if (SimpleTimeZone.getDefault().inDaylightTime(new Date())) {
            timeStampinMillis -= 3600000L;
        }
        long time;

        if (roundToMinutes) {
            time = timeStampinMillis / 1000L / 60L * 600000000L;
        } else {
            time = timeStampinMillis * 10000L;
        }
        return time + 116444592000000000L;
    }

    public static void addImageInfo(MaplePacketLittleEndianWriter mplew, byte[] image) {
        mplew.writeInt(image.length);
        mplew.write(image);
    }

    public static void addStartedQuestInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.write(1);
        final List<MapleQuestStatus> started = chr.getStartedQuests();
        mplew.writeShort(started.size());
        for (MapleQuestStatus q : started) {
            mplew.writeInt(q.getQuest().getId()); // 174.1 this is an integer now
            if (q.hasMobKills()) {
                StringBuilder sb = new StringBuilder();
                for (Iterator i$ = q.getMobKills().values().iterator(); i$.hasNext();) {
                    int kills = ((Integer) i$.next()).intValue();
                    sb.append(StringUtil.getLeftPaddedStr(String.valueOf(kills), '0', 3));
                }
                mplew.writeMapleAsciiString(sb.toString());
            } else {
                mplew.writeMapleAsciiString(q.getCustomData() == null ? "" : q.getCustomData());
            }
        }
        mplew.writeShort(0);
        /*
         mplew.writeShort(7);
         mplew.writeMapleAsciiString("1NX5211068");
         mplew.writeMapleAsciiString("1");
         mplew.writeMapleAsciiString("SE20130619");
         mplew.writeMapleAsciiString("20130626060823");
         mplew.writeMapleAsciiString("99NX5533018");
         mplew.writeMapleAsciiString("1");
         mplew.writeMapleAsciiString("1NX1003792");
         mplew.writeMapleAsciiString("1");
         mplew.writeMapleAsciiString("1NX1702337");
         mplew.writeMapleAsciiString("1");
         mplew.writeMapleAsciiString("1NX9102857");
         mplew.writeMapleAsciiString("1");
         mplew.writeMapleAsciiString("SE20130116");
         mplew.writeMapleAsciiString("1");
         */
    }

    public static void addCompletedQuestInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.write(1);
        final List<MapleQuestStatus> completed = chr.getCompletedQuests();
        mplew.writeShort(completed.size());
        for (MapleQuestStatus q : completed) {
            mplew.writeInt(q.getQuest().getId()); // 174.1 this is an integer now..
            mplew.writeInt(KoreanDateUtil.getQuestTimestamp(q.getCompletionTime()));
        }
    }

    public static void addSkillInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.write(1);
        mplew.writeShort(0);
        mplew.writeShort(0);
        /*MaplePacketLittleEndianWriter mplew1 =  new MaplePacketLittleEndianWriter();
         final Map<Skill, SkillEntry> skills = chr.getSkills();
         mplew1.write(1);
         int hyper = 0;
         //for (Skill skill : skills.keySet()) {
         //    if (skill.isHyper()) hyper++;
         //}
         mplew1.writeShort(skills.size() - hyper);
         boolean follow = false;
        
         for (Map.Entry<Skill, SkillEntry> skill : skills.entrySet()) {
         //if (((Skill) skill.getKey()).isHyper()) continue;
            
         if (follow) {
         follow = false;
         if (!GameConstants.isHyperSkill((Skill) skill.getKey()))
         mplew1.writeInt(skill.getKey().getId());
         }
         mplew1.writeInt(skill.getKey().getId());
         mplew1.writeInt(((SkillEntry) skill.getValue()).skillevel);
         addExpirationTime(mplew1, ((SkillEntry) skill.getValue()).expiration);

         if (GameConstants.isHyperSkill((Skill) skill.getKey())) {
         // mplew1.writeInt(1110009);
         follow = true;
         } else if (((Skill) skill.getKey()).isFourthJob()) {
         mplew1.writeInt(((SkillEntry) skill.getValue()).masterlevel);
         }
         //  addSingleSkill(mplew, skill.getKey(), skill.getValue());
         }
         mplew.write(mplew1.getPacket());
         System.out.println(HexTool.toString(mplew1.getPacket()));
         */
    }

//    public static void addSingleSkill(MaplePacketLittleEndianWriter mplew, Skill skill, SkillEntry ske) {
//        try {
//            // if (skill.getId() != 1001008) return;
//
//            MaplePacketLittleEndianWriter mplew1 = new MaplePacketLittleEndianWriter();
//
//            mplew1.writeInt(skill.getId());
//            mplew1.writeInt(ske.skillevel);
//            addExpirationTime(mplew1, ske.expiration);
//
//            if (GameConstants.isHyperSkill(skill)) {
//                //System.out.println("HYPER: " + ((Skill) skill.getKey()).getId());
//                mplew1.writeInt(0);
//            } else if (((Skill) skill).isFourthJob()) {
//                mplew1.writeInt(((SkillEntry) ske).masterlevel);
//            }
//            if (skill.getId() == 1001008) {
//                System.out.println(HexTool.toString(mplew1.getPacket()));
//            }
//            mplew.write(mplew1.getPacket());
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }
    public static void addCoolDownInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        final List<MapleCoolDownValueHolder> cd = chr.getCooldowns();
        mplew.writeShort(cd.size());
        for (MapleCoolDownValueHolder cooling : cd) {
            mplew.writeInt(cooling.skillId);
            mplew.writeInt((int) (cooling.length + cooling.startTime - System.currentTimeMillis()) / 1000);
        }
    }

    public static void addRocksInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        int[] mapz = chr.getRegRocks();
        for (int i = 0; i < 5; i++) {
            mplew.writeInt(mapz[i]);
        }

        int[] map = chr.getRocks();
        for (int i = 0; i < 10; i++) {
            mplew.writeInt(map[i]);
        }

        int[] maps = chr.getHyperRocks();
        for (int i = 0; i < 13; i++) {
            mplew.writeInt(maps[i]);
        }
        for (int i = 0; i < 13; i++) {
            mplew.writeInt(maps[i]);
        }
    }

    public static void addUnk400Info(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        short size = 0;
        mplew.writeShort(size);
        for (int i = 0; i < size; i++) {
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
    }

    public static void addRingInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        Triple<List<MapleRing>, List<MapleRing>, List<MapleRing>> aRing = chr.getRings(true);
        List<MapleRing> cRing = aRing.getLeft();
        mplew.writeShort(cRing.size());
        for (MapleRing ring : cRing) {
            mplew.writeInt(ring.getPartnerChrId());
            mplew.writeAsciiString(ring.getPartnerName(), 13);
            mplew.writeLong(ring.getRingId());
            mplew.writeLong(ring.getPartnerRingId());
        }
        List<MapleRing> fRing = aRing.getMid();
        mplew.writeShort(fRing.size());
        for (MapleRing ring : fRing) {
            mplew.writeInt(ring.getPartnerChrId());
            mplew.writeAsciiString(ring.getPartnerName(), 13);
            mplew.writeLong(ring.getRingId());
            mplew.writeLong(ring.getPartnerRingId());
            mplew.writeInt(ring.getItemId());
        }
        List<MapleRing> mRing = aRing.getRight();
        mplew.writeShort(mRing.size());
        int marriageId = 30000;
        for (MapleRing ring : mRing) {
            mplew.writeInt(marriageId);
            mplew.writeInt(chr.getId());
            mplew.writeInt(ring.getPartnerChrId());
            mplew.writeShort(3);
            mplew.writeInt(ring.getItemId());
            mplew.writeInt(ring.getItemId());
            mplew.writeAsciiString(chr.getName(), 13);
            mplew.writeAsciiString(ring.getPartnerName(), 13);
        }
    }

    public static void addInventoryInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
    	mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
    	// addPotionPotInfo(mplew, chr);
        mplew.writeInt(chr.getId());

        mplew.writeInt(0); 
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);

        mplew.writeInt(0);

        mplew.write(0);
        mplew.write(0);
        mplew.write(0);

        mplew.write(chr.getInventory(MapleInventoryType.EQUIP).getSlotLimit());
        mplew.write(chr.getInventory(MapleInventoryType.USE).getSlotLimit());
        mplew.write(chr.getInventory(MapleInventoryType.SETUP).getSlotLimit());
        mplew.write(chr.getInventory(MapleInventoryType.ETC).getSlotLimit());
        mplew.write(chr.getInventory(MapleInventoryType.CASH).getSlotLimit());

        MapleQuestStatus stat = chr.getQuestNoAdd(MapleQuest.getInstance(122700));
        if ((stat != null) && (stat.getCustomData() != null) && (Long.parseLong(stat.getCustomData()) > System.currentTimeMillis())) {
            mplew.writeLong(getTime(Long.parseLong(stat.getCustomData())));
        } else {
            mplew.writeLong(getTime(-2L));
        }
        mplew.write(0); // new
        MapleInventory iv = chr.getInventory(MapleInventoryType.EQUIPPED);
        final List<Item> equipped = iv.newList();
        Collections.sort(equipped);
        for (Item item : equipped) {
            if ((item.getPosition() < 0) && (item.getPosition() > -100)) {
                addItemPosition(mplew, item, false, false);
                addItemInfo(mplew, item, chr);
            }
        }
        mplew.writeShort(0);
        for (Item item : equipped) {
            if ((item.getPosition() <= -100) && (item.getPosition() > -1000)) {
                addItemPosition(mplew, item, false, false);
                addItemInfo(mplew, item, chr);
            }
        }
        mplew.writeShort(0);
        iv = chr.getInventory(MapleInventoryType.EQUIP);
        for (Item item : iv.list()) {
            addItemPosition(mplew, item, false, false);
            addItemInfo(mplew, item, chr);
        }
        mplew.writeShort(0);
        for (Item item : equipped) {
            if ((item.getPosition() <= -1000) && (item.getPosition() > -1100)) {
                addItemPosition(mplew, item, false, false);
                addItemInfo(mplew, item, chr);
            }
        }
        mplew.writeShort(0);
        for (Item item : equipped) {
            if ((item.getPosition() <= -1100) && (item.getPosition() > -1200)) {
                addItemPosition(mplew, item, false, false);
                addItemInfo(mplew, item, chr);
            }
        }
        mplew.writeShort(0);
        mplew.writeShort(0);
        for (Item item : equipped) {
            if (item.getPosition() <= -1200) {
                addItemPosition(mplew, item, false, false);
                addItemInfo(mplew, item, chr);
            }
        }
        mplew.writeShort(0);
        mplew.writeShort(0);
        mplew.writeShort(0);
        mplew.writeShort(0);
        for (Item item : equipped) {
            if ((item.getPosition() <= -5000) && (item.getPosition() >= -5003)) {
                addItemPosition(mplew, item, false, false);
                addItemInfo(mplew, item, chr);
            }
        }      
        mplew.writeShort(0);
        mplew.writeShort(0);
        mplew.writeShort(0);
        mplew.writeShort(0);
        mplew.writeShort(0);

        iv = chr.getInventory(MapleInventoryType.USE);
        for (Item item : iv.list()) {
            addItemPosition(mplew, item, false, false);
            addItemInfo(mplew, item, chr);
        }
        mplew.write(0);
        iv = chr.getInventory(MapleInventoryType.SETUP);
        for (Item item : iv.list()) {
            addItemPosition(mplew, item, false, false);
            addItemInfo(mplew, item, chr);
        }
        mplew.write(0);
        iv = chr.getInventory(MapleInventoryType.ETC);
        for (Item item : iv.list()) {
            if (item.getPosition() < 100) {
                addItemPosition(mplew, item, false, false);
                addItemInfo(mplew, item, chr);
            }
        }
        mplew.write(0);
        iv = chr.getInventory(MapleInventoryType.CASH);
        for (Item item : iv.list()) {
            addItemPosition(mplew, item, false, false);
            addItemInfo(mplew, item, chr);
        }
        mplew.write(0);
//        for (int i = 0; i < chr.getExtendedSlots().size(); i++) {
//            mplew.writeInt(i);
//            mplew.writeInt(chr.getExtendedSlot(i));
//            for (Item item : chr.getInventory(MapleInventoryType.ETC).list()) {
//                if ((item.getPosition() > i * 100 + 100) && (item.getPosition() < i * 100 + 200)) {
//                    addItemPosition(mplew, item, false, true);
//                    addItemInfo(mplew, item, chr);
//                }
//            }
//            mplew.writeInt(-1);
//        }
        mplew.write0(21); // new
    }

    public static void addPotionPotInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        if (chr.getPotionPots() == null) {
            mplew.writeInt(0);
            return;
        }
        mplew.writeInt(chr.getPotionPots().size());
        for (MaplePotionPot p : chr.getPotionPots()) {
            mplew.writeInt(p.getId());
            mplew.writeInt(p.getMaxValue());
            mplew.writeInt(p.getHp());
            mplew.writeInt(0);
            mplew.writeInt(p.getMp());

            mplew.writeLong(PacketHelper.getTime(p.getStartDate()));
            mplew.writeLong(PacketHelper.getTime(p.getEndDate()));
        }
    }

    public static void addCharStats(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {

        mplew.writeInt(chr.getId());
   
        // dwCharacterIDForLog
        mplew.writeInt(chr.getId()); 
        
        // dwWorldIDForLog
        mplew.writeInt(chr.getClient().getWorld()); 
        
        mplew.writeAsciiString(chr.getName(), 13);
        mplew.write(chr.getGender());
        mplew.write(chr.getSkinColor());
        mplew.writeInt(chr.getFace());
        mplew.writeInt(chr.getHair());

        mplew.write(-1); // nMixBaseHairColor
		mplew.write(0); // nMixAddHairColor
		mplew.write(0); // nMixHairBaseProb

        mplew.write(chr.getLevel());
        mplew.writeShort(chr.getJob());
        
        chr.getStat().connectData(mplew);
        
        mplew.writeShort(chr.getRemainingAp());
        if (GameConstants.isSeparatedSp(chr.getJob())) {
            int size = chr.getRemainingSpSize();
            mplew.write(size);
            for (int i = 0; i < chr.getRemainingSps().length; i++) {
                if (chr.getRemainingSp(i) > 0) {
                    mplew.write(i + 1);
                    mplew.writeInt(chr.getRemainingSp(i));
                }
            }
        } else {
            mplew.writeShort(chr.getRemainingSp());
        }
        mplew.writeLong(chr.getExp());
        mplew.writeInt(chr.getFame());
        mplew.writeInt(0); // Waru points
        mplew.writeInt(chr.getGachExp());
        mplew.writeInt(chr.getMapId());
        mplew.write(chr.getInitialSpawnpoint());
        mplew.writeInt(0); // ?
        mplew.writeShort(chr.getSubcategory());
        
        if (GameConstants.isDemonSlayer(chr.getJob()) || GameConstants.isXenon(chr.getJob()) || GameConstants.isDemonAvenger(chr.getJob())) {
            mplew.writeInt(chr.getFaceMarking());
        }
        
        mplew.write(chr.getFatigue());
        mplew.writeInt(GameConstants.getCurrentDate());
        for (MapleTrait.MapleTraitType t : MapleTrait.MapleTraitType.values()) {
            mplew.writeInt(chr.getTrait(t).getTotalExp());
        }
        //for (MapleTrait.MapleTraitType t : MapleTrait.MapleTraitType.values()) {
        //    mplew.writeShort(0); //today's stats
        //}
        //mplew.write(0);
        //mplew.writeLong(getTime(System.currentTimeMillis()));
        mplew.write0(21);
        
        mplew.writeInt(chr.getStat().pvpExp);
        mplew.write(chr.getStat().pvpRank);
        mplew.writeInt(chr.getBattlePoints());
        mplew.write(5); // pvp mode level
        mplew.write(6); // pvp mode type
        mplew.writeInt(0); // event points
        
        addPartTimeJob(mplew, MapleCharacter.getPartTime(chr.getId()));
        
        /*
         * Character Card
         * 
         */
        for (int i = 0; i < 9; i++) {
            mplew.writeInt(0);
            mplew.write(0);
            mplew.writeInt(0);
        }
        
        mplew.writeReversedLong(getTime(System.currentTimeMillis())); // account last login
        
        // is this character burning
        mplew.write(0); 
    }

    public static void addCharLook(MaplePacketLittleEndianWriter mplew, MapleCharacterLook chr, boolean mega, boolean second) {
        mplew.write(second ? chr.getSecondGender() : chr.getGender());
        mplew.write(second ? chr.getSecondSkinColor() : chr.getSkinColor());
        mplew.writeInt(second ? chr.getSecondFace() : chr.getFace());
        mplew.writeInt(chr.getJob());
        mplew.write(mega ? 0 : 1);
        mplew.writeInt(second ? chr.getSecondHair() : chr.getHair());

        final Map<Byte, Integer> myEquip = new LinkedHashMap<>();
        final Map<Byte, Integer> maskedEquip = new LinkedHashMap<>();
        final Map<Byte, Integer> totemEquip = new LinkedHashMap<>();
        final Map<Byte, Integer> equip = second ? chr.getSecondEquips(true) : chr.getEquips(true);
        
        // System.out.println("There are " + equip.entrySet().size() + " equips.");
        for (final Entry<Byte, Integer> item : equip.entrySet()) {
            if ((item.getKey()).byteValue() < -127) {
	            continue;
	        }
		    
            byte pos = (byte) ((item.getKey()).byteValue() * -1);
		
		    if ((pos < 100) && (myEquip.get(Byte.valueOf(pos)) == null)) {
		        myEquip.put(Byte.valueOf(pos), item.getValue());
		    } else if ((pos > 100) && (pos != 111)) {
		        pos = (byte) (pos - 100);
		            if (myEquip.get(Byte.valueOf(pos)) != null) {
		                maskedEquip.put(Byte.valueOf(pos), myEquip.get(Byte.valueOf(pos)));
		                totemEquip.put(Byte.valueOf(pos), item.getValue());
		            }
		        myEquip.put(Byte.valueOf(pos), item.getValue());
		        totemEquip.put(Byte.valueOf(pos), item.getValue());
		    } else if (myEquip.get(Byte.valueOf(pos)) != null) {
		    	
		        maskedEquip.put(Byte.valueOf(pos), item.getValue());
		        totemEquip.put(Byte.valueOf(pos), item.getValue());
		    }
        }
        
        // System.out.println("There are " + chr.getTotems().entrySet().size() + " totems.");
    	for (final Entry<Byte, Integer> totem : chr.getTotems().entrySet()) {
	        byte pos = (byte) ((totem.getKey()).byteValue() * -1);
	            if (pos < 0 || pos > 2) { //3 totem slots
	            continue;
	        }
		    if (totem.getValue() < 1200000 || totem.getValue() >= 1210000) {
		        continue;
		    }
		    totemEquip.put(Byte.valueOf(pos), totem.getValue());
	    }
	
    	// System.out.println("There are " + myEquip.entrySet().size() + " pieces of equipment.");
	    for (Map.Entry entry : myEquip.entrySet()) {
	        int weapon = ((Integer) entry.getValue()).intValue();
		    if (GameConstants.getWeaponType(weapon) == (second ? MapleWeaponType.LONG_SWORD : MapleWeaponType.BIG_SWORD)) {
		        continue;
		    }
		    mplew.write(((Byte) entry.getKey()).byteValue());
		    mplew.writeInt(((Integer) entry.getValue()).intValue());
	    }
	    
	    mplew.write(255);
	
	    for (Map.Entry entry : maskedEquip.entrySet()) {
	        mplew.write(((Byte) entry.getKey()).byteValue());
	        mplew.writeInt(((Integer) entry.getValue()).intValue());
	    }
	    
	    mplew.write(255);
	
	    for (Map.Entry entry : totemEquip.entrySet()) {
	        mplew.write(((Byte) entry.getKey()).byteValue());
	        mplew.writeInt(((Integer) entry.getValue()).intValue());
	    }
	    
	    mplew.write(255); // new v140

        Integer cWeapon = equip.get(Byte.valueOf((byte) -111));
        mplew.writeInt(cWeapon != null ? cWeapon.intValue() : 0);
        
        Integer Weapon = equip.get(Byte.valueOf((byte) -11));
        mplew.writeInt(Weapon != null ? Weapon.intValue() : 0); //new v139
        
        boolean zero = GameConstants.isZero(chr.getJob());
        Integer Shield = equip.get(Byte.valueOf((byte) -10));
        mplew.writeInt(!zero && Shield != null ? Shield.intValue() : 0); //new v139
        
        mplew.write(0); // mercedes elf ears
        mplew.write0(12); // all 3 pets unique id
        
	    if (GameConstants.isDemonSlayer(chr.getJob()) || GameConstants.isXenon(chr.getJob()) || GameConstants.isDemonAvenger(chr.getJob())) {
	        mplew.writeInt(chr.getFaceMarking());
	    } else if (GameConstants.isZero(chr.getJob())) {
	        mplew.write(1);
	    }
	    
	    /*
	     if (JobConstants.isBeastTamer(chr.getJob())) { // tale and ears
			mplew.write(1);
			mplew.writeInt(5010116);
			mplew.write(1);
			mplew.writeInt(5010119);
		 }
	     */
	    mplew.write(0); // mixed hair color
	    mplew.write(0); // mixed hair percent
	}

    public static void addExpirationTime(MaplePacketLittleEndianWriter mplew, long time) {
        mplew.writeLong(getTime(time));
    }

    public static void addItemPosition(MaplePacketLittleEndianWriter mplew, Item item, boolean trade, boolean bagSlot) {
        if (item == null) {
            mplew.write(0);
            return;
        }
        short pos = item.getPosition();
        if (pos <= -1) {
            pos = (short) (pos * -1);
            if ((pos > 100) && (pos < 1000)) {
                pos = (short) (pos - 100);
            }
        }
        if (bagSlot) {
            mplew.writeInt(pos % 100 - 1);
        } else if ((!trade) && (item.getType() == 1)) {
            mplew.writeShort(pos);
        } else {
            mplew.write(pos);
        }
    }

    public static void addItemInfo(MaplePacketLittleEndianWriter mplew, Item item) {
        addItemInfo(mplew, item, null);
    }

    public static void addItemInfo(final MaplePacketLittleEndianWriter mplew, final Item item, final MapleCharacter chr) {
        mplew.write(item.getPet() != null ? 3 : item.getType());
        mplew.writeInt(item.getItemId());
        boolean hasUniqueId = item.getUniqueId() > 0 && !GameConstants.isMarriageRing(item.getItemId()) && item.getItemId() / 10000 != 166;
        //marriage rings arent cash items so dont have uniqueids, but we assign them anyway for the sake of rings
        mplew.write(hasUniqueId ? 1 : 0);
        if (hasUniqueId) {
            mplew.writeLong(item.getUniqueId());
        }
        if (item.getPet() != null) { // Pet
            addPetItemInfo(mplew, item, item.getPet(), true);
        } else {
            addExpirationTime(mplew, item.getExpiration());
            mplew.writeInt(chr == null ? -1 : chr.getExtendedSlots().indexOf(item.getItemId()));
            if (item.getType() == 1) {
                final Equip equip = Equip.calculateEquipStats((Equip) item);
                //final Equip equip = Equip.calculateEquipStatsTest((Equip) item);
                addEquipStats(mplew, equip);
                //addEquipStatsTest(mplew, equip);
                addEquipBonusStats(mplew, equip, hasUniqueId);
            } else {
                mplew.writeShort(item.getQuantity());
                mplew.writeMapleAsciiString(item.getOwner());
                mplew.writeShort(item.getFlag());
                if (GameConstants.isThrowingStar(item.getItemId()) || GameConstants.isBullet(item.getItemId()) || item.getItemId() / 10000 == 287) {
                    mplew.writeLong(item.getInventoryId() <= 0 ? -1 : item.getInventoryId());
                }
            }
        }
    }

    public static void addEquipStatsTest(MaplePacketLittleEndianWriter mplew, Equip equip) {
        int mask;
        int masklength = 2;
        for (int i = 1; i <= masklength; i++) {
            mask = 0;
            if (equip.getStatsTest().size() > 0) {
                for (EquipStat stat : equip.getStatsTest().keySet()) {
                    if (stat.getPosition() == i) {
                        mask += stat.getValue();
                    }
                }
            }
            mplew.writeInt(mask);
            if (mask != 0) {
                for (EquipStat stat : equip.getStatsTest().keySet()) {
                    if (stat.getDatatype() == 8) {
                        mplew.writeLong(equip.getStatsTest().get(stat));
                    } else if (stat.getDatatype() == 4) {
                        mplew.writeInt(equip.getStatsTest().get(stat).intValue());
                    } else if (stat.getDatatype() == 2) {
                        mplew.writeShort(equip.getStatsTest().get(stat).shortValue());
                    } else if (stat.getDatatype() == 1) {
                        mplew.write(equip.getStatsTest().get(stat).byteValue());
                    }
                }
            }
        }
    }

    public static void addEquipStats(MaplePacketLittleEndianWriter mplew, Equip equip) {
        int head = 0;
        if (equip.getStats().size() > 0) {
            for (EquipStat stat : equip.getStats()) {
                head |= stat.getValue();
            }
        }
        mplew.writeInt(head);
        if (head != 0) {
            if (equip.getStats().contains(EquipStat.SLOTS)) {
                mplew.write(equip.getUpgradeSlots());
            }
            if (equip.getStats().contains(EquipStat.LEVEL)) {
                mplew.write(equip.getLevel());
            }
            if (equip.getStats().contains(EquipStat.STR)) {
                mplew.writeShort(equip.getStr());
            }
            if (equip.getStats().contains(EquipStat.DEX)) {
                mplew.writeShort(equip.getDex());
            }
            if (equip.getStats().contains(EquipStat.INT)) {
                mplew.writeShort(equip.getInt());
            }
            if (equip.getStats().contains(EquipStat.LUK)) {
                mplew.writeShort(equip.getLuk());
            }
            if (equip.getStats().contains(EquipStat.MHP)) {
                mplew.writeShort(equip.getHp());
            }
            if (equip.getStats().contains(EquipStat.MMP)) {
                mplew.writeShort(equip.getMp());
            }
            if (equip.getStats().contains(EquipStat.WATK)) {
                mplew.writeShort(equip.getWatk());
            }
            if (equip.getStats().contains(EquipStat.MATK)) {
                mplew.writeShort(equip.getMatk());
            }
            if (equip.getStats().contains(EquipStat.WDEF)) {
                mplew.writeShort(equip.getWdef());
            }
            if (equip.getStats().contains(EquipStat.MDEF)) {
                mplew.writeShort(equip.getMdef());
            }
            if (equip.getStats().contains(EquipStat.ACC)) {
                mplew.writeShort(equip.getAcc());
            }
            if (equip.getStats().contains(EquipStat.AVOID)) {
                mplew.writeShort(equip.getAvoid());
            }
            if (equip.getStats().contains(EquipStat.HANDS)) {
                mplew.writeShort(equip.getHands());
            }
            if (equip.getStats().contains(EquipStat.SPEED)) {
                mplew.writeShort(equip.getSpeed());
            }
            if (equip.getStats().contains(EquipStat.JUMP)) {
                mplew.writeShort(equip.getJump());
            }
            if (equip.getStats().contains(EquipStat.FLAG)) {
                mplew.writeShort(equip.getFlag());
            }
            if (equip.getStats().contains(EquipStat.INC_SKILL)) {
                mplew.write(equip.getIncSkill() > 0 ? 1 : 0);
            }
            if (equip.getStats().contains(EquipStat.ITEM_LEVEL)) {
                mplew.write(Math.max(equip.getBaseLevel(), equip.getEquipLevel())); // Item level
            }
            if (equip.getStats().contains(EquipStat.ITEM_EXP)) {
                mplew.writeLong(equip.getExpPercentage() * 100000); // Item Exp... 10000000 = 100%
            }
            if (equip.getStats().contains(EquipStat.DURABILITY)) {
                mplew.writeInt(equip.getDurability());
            }
            if (equip.getStats().contains(EquipStat.VICIOUS_HAMMER)) {
                mplew.writeInt(equip.getViciousHammer());
            }
            if (equip.getStats().contains(EquipStat.PVP_DAMAGE)) {
                mplew.writeShort(equip.getPVPDamage());
            }
            if (equip.getStats().contains(EquipStat.ENHANCT_BUFF)) {
                mplew.writeShort(equip.getEnhanctBuff());
            }
            if (equip.getStats().contains(EquipStat.DURABILITY_SPECIAL)) {
                mplew.writeInt(equip.getDurability());
            }
            if (equip.getStats().contains(EquipStat.REQUIRED_LEVEL)) {
                mplew.write(equip.getReqLevel());
            }
            if (equip.getStats().contains(EquipStat.YGGDRASIL_WISDOM)) {
                mplew.write(equip.getYggdrasilWisdom());
            }
            if (equip.getStats().contains(EquipStat.FINAL_STRIKE)) {
                mplew.write(equip.getFinalStrike());
            }
            if (equip.getStats().contains(EquipStat.BOSS_DAMAGE)) {
                mplew.write(equip.getBossDamage());
            }
            if (equip.getStats().contains(EquipStat.IGNORE_PDR)) {
                mplew.write(equip.getIgnorePDR());
            }
        } else {
            /*
             *   if ( v3 >= 0 )
             *     v36 = 0;
             *   else
             *     v36 = (unsigned __int8)CInPacket::Decode1(a2);
             */
//            mplew.write(0); //unknown
        }
        addEquipSpecialStats(mplew, equip);
    }

    public static void addEquipSpecialStats(MaplePacketLittleEndianWriter mplew, Equip equip) {
        int head = 0;
        if (equip.getSpecialStats().size() > 0) {
            for (EquipSpecialStat stat : equip.getSpecialStats()) {
                head |= stat.getValue();
            }
        }
        mplew.writeInt(head);
//        System.out.println("mask " + head);

        if (head != 0) {
            if (equip.getSpecialStats().contains(EquipSpecialStat.TOTAL_DAMAGE)) {
//                System.out.println("TOTAL_DAMAGE " + equip.getTotalDamage());
                mplew.write(equip.getTotalDamage());
            }
            if (equip.getSpecialStats().contains(EquipSpecialStat.ALL_STAT)) {
//                System.out.println("ALL_STAT " + equip.getAllStat());
                mplew.write(equip.getAllStat());
            }
            if (equip.getSpecialStats().contains(EquipSpecialStat.KARMA_COUNT)) {
//                System.out.println("KARMA_COUNT " + equip.getKarmaCount());
                mplew.write(equip.getKarmaCount());
            }
            if (equip.getSpecialStats().contains(EquipSpecialStat.UNK8)) {
//                System.out.println("unk8 " + System.currentTimeMillis());
                mplew.writeLong(System.currentTimeMillis());
            }
            if (equip.getSpecialStats().contains(EquipSpecialStat.UNK10)) {
//                System.out.println("unk10 " + 1);
                mplew.writeInt(0);
            }
        }
    }

//    public static void addEquipBonusStats(MaplePacketLittleEndianWriter mplew, Equip equip, boolean hasUniqueId) {
//        mplew.writeMapleAsciiString(equip.getOwner());
//        mplew.write(equip.getState()); // 17 = rare, 18 = epic, 19 = unique, 20 = legendary, potential flags. special grade is 14 but it crashes
//        mplew.write(equip.getEnhance());
//        mplew.writeShort(equip.getPotential1());
//        mplew.writeShort(equip.getPotential2());
//        mplew.writeShort(equip.getPotential3());
//        mplew.writeShort(equip.getBonusPotential1());
//        mplew.writeShort(equip.getBonusPotential2());
//        mplew.writeShort(equip.getBonusPotential3());
//        mplew.writeShort(equip.getFusionAnvil() % 100000);
//        mplew.writeShort(equip.getSocketState());
//        mplew.writeShort(equip.getSocket1() % 10000); // > 0 = mounted, 0 = empty, -1 = none.
//        mplew.writeShort(equip.getSocket2() % 10000);
//        mplew.writeShort(equip.getSocket3() % 10000);
//        if (!hasUniqueId) {
//            mplew.writeLong(equip.getInventoryId() <= 0 ? -1 : equip.getInventoryId()); //some tracking ID
//        }
//        mplew.writeLong(getTime(-2));
//        mplew.writeInt(-1); //?
//        
//    }
    public static void addEquipBonusStats(MaplePacketLittleEndianWriter mplew, Equip equip, boolean hasUniqueId) {
        mplew.writeMapleAsciiString(equip.getOwner());
        mplew.write(equip.getState()); // 17 = rare, 18 = epic, 19 = unique, 20 = legendary, potential flags. special grade is 14 but it crashes
        mplew.write(equip.getEnhance());
        mplew.writeShort(equip.getPotential1());
        mplew.writeShort(equip.getPotential2());
        mplew.writeShort(equip.getPotential3());
        mplew.writeShort(equip.getBonusPotential1());
        mplew.writeShort(equip.getBonusPotential2());
        mplew.writeShort(equip.getBonusPotential3());
        mplew.writeShort(equip.getFusionAnvil() % 100000);
        mplew.writeShort(equip.getSocketState());
        mplew.writeShort(equip.getSocket1() % 10000); // > 0 = mounted, 0 = empty, -1 = none.
        mplew.writeShort(equip.getSocket2() % 10000);
        mplew.writeShort(equip.getSocket3() % 10000);
        if (!hasUniqueId) {
            mplew.writeLong(equip.getInventoryId() <= 0 ? -1 : equip.getInventoryId()); //some tracking ID
        }
        mplew.writeLong(getTime(-2));
        mplew.writeInt(-1); //?
        // new 142
        mplew.writeLong(0);
        mplew.writeLong(getTime(-2));
        mplew.writeLong(0);
        mplew.writeLong(0);
        mplew.writeShort(0); // new
        mplew.writeShort(0); // new
        mplew.writeShort(0); // new
    }

    public static void serializeMovementList(MaplePacketLittleEndianWriter lew, List<LifeMovementFragment> moves) {
        lew.write(moves.size());
        for (LifeMovementFragment move : moves) {
            move.serialize(lew);
        }
    }

    public static void addAnnounceBox(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        if ((chr.getPlayerShop() != null) && (chr.getPlayerShop().isOwner(chr)) && (chr.getPlayerShop().getShopType() != 1) && (chr.getPlayerShop().isAvailable())) {
            addInteraction(mplew, chr.getPlayerShop());
        } else {
            mplew.write(0);
        }
    }

    public static void addInteraction(MaplePacketLittleEndianWriter mplew, IMaplePlayerShop shop) {
        mplew.write(shop.getGameType());
        mplew.writeInt(((AbstractPlayerStore) shop).getObjectId());
        mplew.writeMapleAsciiString(shop.getDescription());
        if (shop.getShopType() != 1) {
            mplew.write(shop.getPassword().length() > 0 ? 1 : 0);
        }
        mplew.write(shop.getItemId() % 10);
        mplew.write(shop.getSize());
        mplew.write(shop.getMaxSize());
        if (shop.getShopType() != 1) {
            mplew.write(shop.isOpen() ? 0 : 1);
        }
    }

    public static void addCharacterInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        long mask = 0xFF_FF_FF_FF_FF_FF_FF_FFL;
        mplew.writeLong(mask);

        // combat orders
        mplew.write(0);
        
        // pet active skill cool time
        for (int i = 0; i < 3; i++) {
            mplew.writeInt(0);
        }
        
        mplew.write(0);
        
        mplew.write(0);
        
        mplew.writeInt(0);
        
        // ?
        mplew.write(0);
        
        if ((mask & 1) != 0) {
            addCharStats(mplew, chr);

            mplew.write(chr.getBuddylist().getCapacity());

            mplew.write(chr.getBlessOfFairyOrigin() != null);
            if (chr.getBlessOfFairyOrigin() != null) {
                mplew.writeMapleAsciiString(chr.getBlessOfFairyOrigin());
            }

            mplew.write(chr.getBlessOfEmpressOrigin() != null);
            if (chr.getBlessOfEmpressOrigin() != null) {
                mplew.writeMapleAsciiString(chr.getBlessOfEmpressOrigin());
            }

            MapleQuestStatus ultExplorer = chr.getQuestNoAdd(MapleQuest.getInstance(GameConstants.ULT_EXPLORER));
            mplew.write((ultExplorer != null) && (ultExplorer.getCustomData() != null));
            if ((ultExplorer != null) && (ultExplorer.getCustomData() != null)) {
                mplew.writeMapleAsciiString(ultExplorer.getCustomData());
            }
        }
        
        if ((mask & 2) != 0) {
        	mplew.writeLong(chr.getMeso());
        }
        
        if ((mask & 8) != 0) {
            addInventoryInfo(mplew, chr);
        }
        
        if ((mask & 0x100) != 0) {
            addSkillInfo(mplew, chr);
        }
        
        if ((mask & 0x8000) != 0) {
            addCoolDownInfo(mplew, chr);
        }
        
        if ((mask & 0x200) != 0) {
            addStartedQuestInfo(mplew, chr);
        }
        
        if ((mask & 0x4000) != 0) {
            addCompletedQuestInfo(mplew, chr);
        }
        
        if ((mask & 0x400) != 0) {
            mplew.writeShort(0);
        }
        
        if ((mask & 0x800) != 0) {
            addRingInfo(mplew, chr);
        }
        
        if ((mask & 0x1000) != 0) {
            addRocksInfo(mplew, chr);
        }
        
        if ((mask & 0x20000) != 0) {
            mplew.writeInt(0);
        }
        
        if ((mask & 0x10000) != 0) {
            addMonsterBookInfo(mplew, chr);
        }

        // ?
        mplew.writeShort(0);
        
        // ?
        mplew.writeInt(0);
        
        
        if ((mask & 0x80000) != 0) {
        	mplew.writeShort(0);
        }
        
        if ((mask & 0x40000) != 0) {
            chr.QuestInfoPacket(mplew);
        }
        
        if ((mask & 0x2000) != 0) {
        	mplew.writeShort(0);
        }

        if ((mask & 0x1000) != 0) {
        	mplew.writeInt(0);
        }
        
        if ((mask & 0x200000) != 0) {
            if ((chr.getJob() >= 3300) && (chr.getJob() <= 3312)) {
                addJaguarInfo(mplew, chr);
            }
        }
        
        // 0x800
        if (GameConstants.isZero(chr.getJob())) {
            addZeroInfo(mplew, chr);
            //chr.getStat().zeroData(mplew, chr);
        }

        if ((mask & 0x4000000) != 0) {
        	mplew.writeShort(0);
        }

        if ((mask & 0x10000000) != 0) {
            addStealSkills(mplew, chr);
        }
        
        if ((mask & 0x80000000) != 0) {
            addAbilityInfo(mplew, chr);
        }
        
        if ((mask & 0x10000) != 0) {
        	mplew.writeShort(0);
        }

        // ...
        mplew.writeInt(0);
        
        // ...
        mplew.write(0);

        if ((mask & 0x1) != 0) {
        	mplew.writeInt(chr.getHonorLevel()); // honor level
            mplew.writeInt(chr.getHonourExp()); // honor xp
        }
        
        if ((mask & 0x2) != 0) {
        	mplew.write(1);
        	mplew.writeShort(0);
        }
        
        if ((mask & 0x4) != 0) {
        	mplew.write(0);
        }
        
        // 0x08
        if (GameConstants.isAngelicBuster(chr.getJob())) {
	        mplew.writeInt(1);
	        mplew.writeInt(21173); //face
	        mplew.writeInt(37141); //hair
	        mplew.write(0);
	        mplew.writeInt(1051291);
	        mplew.writeInt(0);
	        mplew.writeInt(0);
	        // mplew.write(0);
        } else {
        	mplew.writeInt(0);
        	mplew.writeInt(0);
        	mplew.writeInt(0);
        	mplew.write(0);
        	mplew.writeInt(-1);
        	mplew.writeInt(0);
        	mplew.writeInt(0);
        }
        
        if ((mask & 0x40000) != 0) {
        	mplew.writeInt(1);
            mplew.writeInt(0);
            mplew.writeLong(0);
            mplew.writeMapleAsciiString("");
            mplew.writeInt(0);
        }

        if ((mask & 0x10) != 0) {
        	mplew.writeShort(0);
            mplew.writeShort(0);
        }
        
        if ((mask & 0x20) != 0) {
        	mplew.writeInt(0); // farm monsters length (if length > 1 for each monster int id and long expire)
        }

        if ((mask & 0x40) != 0) {
        	addFarmInfo(mplew, chr.getClient(), (byte) 2);
            mplew.writeInt(0);
            mplew.writeInt(0);
        }

        if ((mask & 0x80) != 0) {
        	mplew.write(0);
        }

        if ((mask & 0x400) != 0) {
        	mplew.writeInt(0);
            mplew.writeLong(getTime(-2));
            mplew.writeInt(0);
        }

        if ((mask & 0x20000) != 0) {
        	mplew.writeInt(chr.getId());
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeLong(getTime(-2));
            mplew.writeInt(0);
        }
        
        // ...
        mplew.writeShort(0);
        mplew.writeShort(0);
        
        // ...
        mplew.write(0);
        
        // DecodeTextEquipInfo
        mplew.writeInt(0);
        
        if ((mask & 0x8000000) != 0) {
        	mplew.write(1);
        	mplew.write(0);
        	mplew.writeInt(1);
        	mplew.writeInt(0);
        	mplew.writeInt(100);
        	mplew.writeLong(getTime(-1));
        	mplew.writeShort(0);
            mplew.writeShort(0);
        }
        
        if ((mask & 0x10000000) != 0) {
        	mplew.write(0);
        }
        
        if ((mask & 0x20000000) != 0) {
        	mplew.writeInt(0);
        	mplew.writeInt(0);
        }

        if ((mask & 0x2000) != 0) {
            addCoreAura(mplew, chr); //84 bytes + boolean (85 total)
            mplew.write(1);
        }
        
        if ((mask & 0x100000) != 0) {
        	mplew.writeShort(0); //for <short> length write 2 shorts
        }

        // red leaf information
        mplew.writeInt(chr.getAccountID());
        mplew.writeInt(chr.getId());
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.write(new byte[32]);
        
        // addRedLeafInfo(mplew, chr);
    }

    public static int getSkillBook(final int i) {
        switch (i) {
            case 1:
            case 2:
                return 4;
            case 3:
                return 3;
            case 4:
                return 2;
        }
        return 0;
    }

    public static void addAbilityInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        final List<InnerSkillValueHolder> skills = chr.getInnerSkills();
        mplew.writeShort(skills.size());
        for (int i = 0; i < skills.size(); ++i) {
            mplew.write(i + 1); // key
            mplew.writeInt(skills.get(i).getSkillId()); //d 7000000 id ++, 71 = char cards
            mplew.write(skills.get(i).getSkillLevel()); // level
            mplew.write(skills.get(i).getRank()); //rank, C, B, A, and S
        }

    }

    public static void addCoreAura(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        /*MapleCoreAura aura = chr.getCoreAura();
        mplew.writeInt(aura.getId()); // nvr change
        mplew.writeInt(chr.getId());
        int level = chr.getSkillLevel(80001151) > 0 ? chr.getSkillLevel(80001151) : chr.getSkillLevel(1214);
        mplew.writeInt(level);
        
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(aura.getExpire());//timer
        mplew.writeInt(0);
        
        mplew.writeInt(aura.getAtt());//wep att
        mplew.writeInt(aura.getDex());//dex
        mplew.writeInt(aura.getLuk());//luk
        mplew.writeInt(aura.getMagic());//magic att
        mplew.writeInt(aura.getInt());//int
        mplew.writeInt(aura.getStr());//str
        
        mplew.writeInt(0);
        mplew.writeInt(aura.getTotal());//max
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeLong(getTime(System.currentTimeMillis() + 86400000L));
        mplew.writeInt(0);
        mplew.write(GameConstants.isJett(chr.getJob()) ? 1 : 0);*/
    	
    	mplew.writeInt(0);
    	mplew.writeInt(0);
    	mplew.writeInt(0);
    	mplew.writeInt(0);
    	mplew.writeInt(0);
    	
    	mplew.writeInt(0);
    	mplew.writeInt(0);
    	mplew.writeInt(0);
    	mplew.writeInt(0);
    	mplew.writeInt(0);
    	mplew.writeInt(0);
    	
    	mplew.writeInt(0);
    	mplew.writeInt(0);
    	mplew.writeInt(0);
    	mplew.writeInt(0);

    	mplew.writeLong(getTime(-2));
    	mplew.write(0);
    }

    public static void addStolenSkills(MaplePacketLittleEndianWriter mplew, MapleCharacter chr, int jobNum, boolean writeJob) {
        if (writeJob) {
            mplew.writeInt(jobNum);
        }
        int count = 0;
        if (chr.getStolenSkills() != null) {
            for (Pair<Integer, Boolean> sk : chr.getStolenSkills()) {
                if (GameConstants.getJobNumber(sk.left / 10000) == jobNum) {
                    mplew.writeInt(sk.left);
                    count++;
                    if (count >= GameConstants.getNumSteal(jobNum)) {
                        break;
                    }
                }
            }
        }
        while (count < GameConstants.getNumSteal(jobNum)) { //for now?
            mplew.writeInt(0);
            count++;
        }
    }

    public static void addChosenSkills(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        for (int i = 1; i <= 5; i++) {
            boolean found = false;
            if (chr.getStolenSkills() != null) {
                for (Pair<Integer, Boolean> sk : chr.getStolenSkills()) {
                    if (GameConstants.getJobNumber(sk.left / 10000) == i && sk.right) {
                        mplew.writeInt(sk.left);
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                mplew.writeInt(0);
            }
        }
    }

    public static void addStealSkills(final MaplePacketLittleEndianWriter mplew, final MapleCharacter chr) {
        /*for (int i = 1; i <= 5; i++) {
            addStolenSkills(mplew, chr, i, false); // 52
        }
        addChosenSkills(mplew, chr); // 16*/
    	
    	mplew.writeInt(0);
    	mplew.writeInt(0);
    	mplew.writeInt(0);
    	mplew.writeInt(0);
    	
    	mplew.writeInt(0);
    	mplew.writeInt(0);
    	mplew.writeInt(0);
    	mplew.writeInt(0);
    	
    	mplew.writeInt(0);
    	mplew.writeInt(0);
    	mplew.writeInt(0);
    	
    	mplew.writeInt(0);
    	mplew.writeInt(0);
    	
    	mplew.writeInt(0);
    	mplew.writeInt(0);
    	
    	mplew.writeInt(0);
    	mplew.writeInt(0);
    	mplew.writeInt(0);
    	mplew.writeInt(0);
    	mplew.writeInt(0);
    	
    }

    public static void addMonsterBookInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        if (chr.getMonsterBook().getSetScore() > 0) {
            chr.getMonsterBook().writeFinished(mplew);
        } else {
            chr.getMonsterBook().writeUnfinished(mplew);
        }

        mplew.writeInt(chr.getMonsterBook().getSet());
    }

    public static void addPetItemInfo(MaplePacketLittleEndianWriter mplew, Item item, MaplePet pet, boolean active) {
        if (item == null) {
            mplew.writeLong(PacketHelper.getKoreanTimestamp((long) (System.currentTimeMillis() * 1.5)));
        } else {
            addExpirationTime(mplew, item.getExpiration() <= System.currentTimeMillis() ? -1L : item.getExpiration());
        }
        mplew.writeInt(-1);
        mplew.writeAsciiString(pet.getName(), 13);
        mplew.write(pet.getLevel());
        mplew.writeShort(pet.getCloseness());
        mplew.write(pet.getFullness());
        if (item == null) {
            mplew.writeLong(PacketHelper.getKoreanTimestamp((long) (System.currentTimeMillis() * 1.5)));
        } else {
            addExpirationTime(mplew, item.getExpiration() <= System.currentTimeMillis() ? -1L : item.getExpiration());
        }
        mplew.writeShort(0);
        mplew.writeShort(pet.getFlags());
        mplew.writeInt((pet.getPetItemId() == 5000054) && (pet.getSecondsLeft() > 0) ? pet.getSecondsLeft() : 0);
        mplew.writeShort(0);
        mplew.write(active ? 0 : pet.getSummoned() ? pet.getSummonedValue() : 0);
        for (int i = 0; i < 4; i++) {
            mplew.write(0);
        }
        mplew.writeInt(-1); //new v140
        mplew.writeShort(100); //new v140
    }

    public static void addShopInfo(MaplePacketLittleEndianWriter mplew, MapleShop shop, MapleClient c) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        mplew.write(shop.getRanks().size() > 0 ? 1 : 0);

        if (shop.getRanks().size() > 0) {
            mplew.write(shop.getRanks().size());
            for (Pair s : shop.getRanks()) {
                mplew.writeInt(((Integer) s.left).intValue());
                mplew.writeMapleAsciiString((String) s.right);
            }
        }
        mplew.writeShort(shop.getItems().size() + c.getPlayer().getRebuy().size());
        for (MapleShopItem item : shop.getItems()) {
            addShopItemInfo(mplew, item, shop, ii, null, c.getPlayer());
        }
        for (Item i : c.getPlayer().getRebuy()) {
            addShopItemInfo(mplew, new MapleShopItem(i.getItemId(), (int) ii.getPrice(i.getItemId()), i.getQuantity(), i.getPosition()), shop, ii, i, c.getPlayer());
        }
    }

    /*
     * Categories:
     * 0 - No Tab
     * 1 - Equip
     * 2 - Use
     * 3 - Setup
     * 4 - Etc
     * 5 - Recipe
     * 6 - Scroll
     * 7 - Special
     * 8 - 8th Anniversary
     * 9 - Button
     * 10 - Invitation Ticket
     * 11 - Materials
     * 12 - Maple
     * 13 - Homecoming
     * 14 - Cores
     * 80 - JoeJoe
     * 81 - Hermoninny
     * 82 - Little Dragon
     * 83 - Ika
     */
    public static void addShopItemInfo(MaplePacketLittleEndianWriter mplew, MapleShopItem item, MapleShop shop, MapleItemInformationProvider ii, Item i, MapleCharacter chr) {
        mplew.writeInt(item.getItemId()); // nItemID
        mplew.writeInt(item.getPrice());  // nPrice
        mplew.writeInt(0); // nTokenItemID
        mplew.writeInt(0); // nTokenPrice
        mplew.writeInt(0); // nPointQuestID
        mplew.writeInt(0); // nPointPrice
        mplew.writeInt(0); // nStarCoin
        mplew.writeInt(0); // nQuestExID 
        mplew.writeMapleAsciiString(""); // sQuestExKey
        mplew.writeInt(0); // nQuestExValue
        mplew.writeInt(1440 * item.getExpiration()); // nItemPeriod
        mplew.writeInt(item.getMinLevel()); // nLevelLimited
        mplew.writeShort(0); // nShowLevMin
        mplew.writeShort(0); // nShowLevMax
        mplew.writeInt(0); // nQuestID
        mplew.write(0);
        mplew.writeLong(getTime(-2L)); // ftSellStart
        mplew.writeLong(getTime(-1L)); // ftSellEnd
        mplew.writeInt(item.getCategory()); // nTabIndex
        
        mplew.write(0); // bWorldBlock
        /*if (GameConstants.isEquip(item.getItemId())) { // // bWorldBlock
            mplew.write(item.hasPotential() ? 1 : 0);
        } else {
            mplew.write(0);
        }*/
        
        mplew.writeInt(0); // nPotentialGrade
        mplew.writeInt(item.getExpiration() > 0 ? 1 : 0);
        mplew.write(0);
        if ((!GameConstants.isThrowingStar(item.getItemId())) && (!GameConstants.isBullet(item.getItemId()))) {
            mplew.writeShort(item.getQuantity()); // nQuantity
            mplew.writeShort(item.getBuyable()); // nMaxPerSlot
        } else {
            mplew.writeAsciiString("333333");
            mplew.writeShort(BitTools.doubleToShortBits(ii.getPrice(item.getItemId())));
            mplew.writeShort(ii.getSlotMax(item.getItemId()));
        }

        mplew.write(i == null ? 0 : 1);
        if (i != null) {
            addItemInfo(mplew, i);
        }
        if (shop.getRanks().size() > 0) {
            mplew.write(item.getRank() >= 0 ? 1 : 0);
            if (item.getRank() >= 0) {
                mplew.write(item.getRank());
            }
        }
        for (int j = 0; j < 4; j++) {
            mplew.writeInt(0); // red leaf high price probably
        }
        addRedLeafInfo(mplew, chr);
    }

    public static void addJaguarInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.write(chr.getIntNoRecord(GameConstants.JAGUAR));
        for (int i = 0; i < 5; i++) {
            mplew.writeInt(0);
        }
    }

    public static void addZeroInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        short mask = 0;
        mplew.writeShort(mask);
        if ((mask & 1) != 0) {
            mplew.write(0); //bool
        }
        if ((mask & 2) != 0) {
            mplew.writeInt(0);
        }
        if ((mask & 4) != 0) {
            mplew.writeInt(0);
        }
        if ((mask & 8) != 0) {
            mplew.write(0);
        }
        if ((mask & 10) != 0) {
            mplew.writeInt(0);
        }
        if ((mask & 20) != 0) {
            mplew.writeInt(0);
        }
        if ((mask & 40) != 0) {
            mplew.writeInt(0);
        }
        if (mask < 0) {
            mplew.writeInt(0);
        }
        if ((mask & 100) != 0) {
            mplew.writeInt(0);
        }
        if ((mask & 200) != 0) {
        	mplew.writeInt(0);
        	mplew.writeInt(0);
        	mplew.writeInt(0);
        }
    }

    public static void addFarmInfo(MaplePacketLittleEndianWriter mplew, MapleClient c, byte gender) {
        mplew.writeMapleAsciiString(""); // c.getFarm().getName()
        mplew.writeInt(0); // c.getFarm().getWaru()
        mplew.writeInt(0); // c.getFarm().getLevel()
        mplew.writeInt(0); // c.getFarm().getExp()
        mplew.writeInt(0); // c.getFarm().getAestheticPoints()
        mplew.writeInt(0); // cash (gems)
        mplew.write(gender); // gender
        mplew.writeInt(0); // theme
        mplew.writeInt(0); // slot extend
        mplew.writeInt(1); // locker slot count
    }

    public static void addRedLeafInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        for (int i = 0; i < 4; i++) {
            mplew.writeInt(9410165 + i);
            mplew.writeInt(chr.getFriendShipPoints()[i]);
        }
    }

    public static void addLuckyLogoutInfo(MaplePacketLittleEndianWriter mplew, boolean enable, CashItem item0, CashItem item1, CashItem item2) {
        mplew.writeInt(enable ? 1 : 0);
        if (enable) {
            CSPacket.addCSItemInfo(mplew, item0);
            CSPacket.addCSItemInfo(mplew, item1);
            CSPacket.addCSItemInfo(mplew, item2);
        }
    }

    public static void addPartTimeJob(MaplePacketLittleEndianWriter mplew, PartTimeJob parttime) {
        mplew.write(parttime.getJob());
        if (parttime.getJob() > 0 && parttime.getJob() <= 5) {
            mplew.writeReversedLong(parttime.getTime());
        } else {
            mplew.writeReversedLong(-2);
        }
        mplew.writeInt(parttime.getReward()); // duration
        mplew.write(parttime.getReward() > 0);
    }

    public static <E extends Buffstat> void writeSingleMask(MaplePacketLittleEndianWriter mplew, E statup) {
        for (int i = GameConstants.MAX_BUFFSTAT; i >= 1; i--) {
            mplew.writeInt(i == statup.getPosition() ? statup.getValue() : 0);
        }
    }

   public static <E extends Buffstat> void writeMask(MaplePacketLittleEndianWriter mplew, Collection<E> statups) {
        int[] mask = new int[10];
        if (!statups.contains(MapleBuffStat.MONSTER_RIDING)) {
            mask = new int[12];
        }
        for (Buffstat statup : statups) {
            mask[(statup.getPosition() - 1)] |= statup.getValue();
        }
        for (int i = mask.length; i >= 1; i--) {
            mplew.writeInt(mask[(i - 1)]);
        }
    }

    public static <E extends Buffstat> void writeBuffMask(MaplePacketLittleEndianWriter mplew, Collection<Pair<E, Integer>> statups) {
        int[] mask = new int[10];
        if (!statups.contains(MapleBuffStat.MONSTER_RIDING)) {
            mask = new int[12];
        }
        for (Pair statup : statups) {
            mask[(((Buffstat) statup.left).getPosition() - 1)] |= ((Buffstat) statup.left).getValue();
        }
        for (int i = mask.length; i >= 1; i--) {
            mplew.writeInt(mask[(i - 1)]);
        }
    }

    public static <E extends Buffstat> void writeBuffMask(MaplePacketLittleEndianWriter mplew, Map<E, Integer> statups) {
        int[] mask = new int[10];
        if (!statups.containsKey(MapleBuffStat.MONSTER_RIDING)) {
            mask = new int[12];
        }
        for (Buffstat statup : statups.keySet()) {
            mask[(statup.getPosition() - 1)] |= statup.getValue();
        }
        for (int i = mask.length; i >= 1; i--) {
            mplew.writeInt(mask[(i - 1)]);
        }
    }
}
