/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools.packet;

import client.MapleCharacter;
import handling.SendPacketOpcode;
import server.maps.MapleMap;
import tools.data.MaplePacketLittleEndianWriter;

/**
 *
 * @author LEL
 */
public class EvolvingPacket {


    public static byte[] showEvolvingMessage(int action) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        //24 00 1B 01 00
        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.writeShort(284);
        mplew.write(action);
        return mplew.getPacket();
    }

    public static byte[] partyCoreInfo(int[] core) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        //AF 00 /00 /48 EF 36 00 /D3 FB 36 00 /00 00 00 00 /00 00 00 00/ 00 00 00 00/ 00 00 00 00 /00 00 00 00 /32 F3 36 00 /00 00 00 00 /00 00 00 00
        mplew.writeShort(SendPacketOpcode.EVOLVING_ACTION.getValue());//
        mplew.write(0);
        for (int i = 0; i < 10; i++) {
            mplew.writeInt(core[i]);
        }
        return mplew.getPacket();
    }

    public static byte[] showPartyConnect(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.EVOLVING_ACTION.getValue());//
        mplew.write(1);
        mplew.write(1);
        mplew.write(chr.getParty().getLeader().getId() == chr.getId() ? 1 : 0);
        return mplew.getPacket();
    }

    public static byte[] connectCancel() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.EVOLVING_ACTION.getValue());//
        mplew.writeShort(1);
        return mplew.getPacket();
    }

    public static byte[] rewardCore(int itemid, int position) {
        //AF 00 02 01 00 00 00 00 00 D0 F2 36 00 01 00 00 00
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.EVOLVING_ACTION.getValue());//
        mplew.write(2); //슬롯?
        mplew.write(1);
        mplew.writeInt(0);
        mplew.write(position);
        mplew.writeInt(itemid);
        mplew.writeInt(1);
        return mplew.getPacket();
    }

    public static byte[] showRewardCore(int itemid) {
        //24 00 1D 16 D0 F2 36 00 01 00 00 00
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.writeShort(5662);
        mplew.writeInt(itemid);
        mplew.writeInt(1);
        return mplew.getPacket();
    }

    public static byte[] moveCore(byte equip, byte slot, byte move, byte to) {
        //AF 00 03 00 01 02 01 03
        //AF 00 03 00 01 03 01 04
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.EVOLVING_ACTION.getValue());//
        mplew.write(3);
        mplew.write(0);
        mplew.write(equip);//무브, 장착해제 : 1, 장착 : 0
        mplew.write(slot);
        mplew.write(move);//장착, 무브 : 1, 장착해제 : 0
        mplew.write(to);
        return mplew.getPacket();
    }

    public static byte[] dropCore(byte position, short quantity) {
        //AF 00 04 01 /00 /01 00 /00 00
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.EVOLVING_ACTION.getValue());//
        mplew.write(4);
        mplew.write(1);
        mplew.write(position);
        mplew.writeShort(quantity);//1
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    public static byte[] EvolvingWarpToMap(MapleMap to, int spawnPoint, MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.WARP_TO_MAP.getValue());
        EvolvingWarpToMapInfo(mplew, chr, to, spawnPoint);
        mplew.writeInt(100);
        mplew.write(1);
        mplew.writeInt(0);
        mplew.writeMapleAsciiString("bgm");
        mplew.writeInt(0);
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    public static void EvolvingWarpToMapInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter player, MapleMap map, int sp) {
        mplew.writeLong(player.getClient().getChannel());
        mplew.write(0);
        mplew.write(2);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(map.getId()); //957010001
        mplew.write(sp);
        mplew.writeInt(player.getStat().getHp());
        mplew.writeShort(0);
        mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
    }

    public static byte[] spawnEvolvingMonster() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        return mplew.getPacket();
    }
}

