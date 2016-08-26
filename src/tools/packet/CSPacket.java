package tools.packet;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import handling.SendPacketOpcode;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import server.CashCategory;
import server.CashItem;
import server.CashItemFactory;
import server.CashItemInfo;
import server.CashItemInfo.CashModInfo;
import server.CashShop;
import server.Randomizer;
import tools.HexTool;
import tools.Pair;
import tools.data.MaplePacketLittleEndianWriter;

public class CSPacket {

    private static final byte Operation_Code = 0x64;//66

    public static byte[] disableCS() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.TOGGLE_CASHSHOP.getValue());
        mplew.write0(5);

        return mplew.getPacket();
    }

    public static byte[] CS_Top_Items() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CASH_SHOP.getValue());
        mplew.write(5);
        CashItemFactory cif = CashItemFactory.getInstance();
        mplew.write(cif.getMenuItems(301).size() > 0 ? 1 : 3);
        mplew.write(cif.getMenuItems(301).size());
        for (CashItem i : cif.getMenuItems(301)) {
            addCSItemInfo(mplew, i);
        }
        return mplew.getPacket();
    }

    public static byte[] CS_Picture_Item() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CASH_SHOP.getValue());
        mplew.write(4);
        CashItemFactory cif = CashItemFactory.getInstance();
        mplew.write(cif.getMenuItems(409).size() > 0 ? 1 : 3);
        mplew.write(cif.getMenuItems(409).size());
        for (CashItem i : cif.getMenuItems(409)) {
            addCSItemInfo(mplew, i);
        }
        return mplew.getPacket();
    }

    public static byte[] CS_Special_Item() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CASH_SHOP.getValue());
        mplew.write(6);
        CashItemFactory cif = CashItemFactory.getInstance();
        mplew.write(cif.getMenuItems(302).size() > 0 ? 1 : 3);
        mplew.write(cif.getMenuItems(302).size());
        for (CashItem i : cif.getMenuItems(302)) {
            addCSItemInfo(mplew, i);
        }
        return mplew.getPacket();
    }

    public static byte[] CS_Featured_Item() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CASH_SHOP.getValue());
        mplew.write(8);
        CashItemFactory cif = CashItemFactory.getInstance();
        mplew.write(cif.getMenuItems(303).size() > 0 ? 1 : 3);
        mplew.write(cif.getMenuItems(303).size());
        for (CashItem i : cif.getMenuItems(303)) {
            addCSItemInfo(mplew, i);
        }
        return mplew.getPacket();
    }

    public static byte[] changeCategory(int subcategory) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CASH_SHOP_UPDATE.getValue());
        mplew.write(11);
        CashItemFactory cif = CashItemFactory.getInstance();
        mplew.write(cif.getCategoryItems(subcategory).size() > 0 ? 1 : 3);
        mplew.write(cif.getCategoryItems(subcategory).size());
        for (CashItem i : cif.getCategoryItems(subcategory)) {
            addCSItemInfo(mplew, i);
        }

        return mplew.getPacket();
    }
   
    public static byte[] showNXChar(int subcategory) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CASH_SHOP_UPDATE.getValue());
        mplew.write(19);
        mplew.write(1);
        mplew.write(HexTool.getByteArrayFromHexString("24 80 8D 5B 00 90 B4 5B 00 6A 2D 10 00 00 00 68 31 31 01 7B 4F 0F 00 01 00 00 00 00 00 00 00 00 00 00 00 06 00 00 00 8C 0A 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 8C 0A 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 01 01 01 00 01 02 00 00 00 D7 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 07 00 52 61 6C 70 68 69 65 80 0C 16 60 B0 FF E7 7F FE 47 25 B0 83 F3 F9 3F FF C3 FF F0 09 03 00 07 00 00 00 00 80 8D 5B 00 90 B4 5B 00 26 30 10 00 00 00 9F CE 38 01 AB 34 10 00 01 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 60 09 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 60 09 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 01 01 01 00 01 02 00 00 00 43 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 07 00 52 61 6C 70 68 69 65 80 0C 16 60 B0 FF E7 7F FE 47 25 B0 83 F3 F9 3F FF C3 FF F0 09 03 00 07 00 00 00 00 80 8D 5B 00 90 B4 5B 00 04 2D 10 00 00 00 67 62 3D 01 BF F8 19 00 01 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 94 11 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 94 11 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 01 01 01 00 01 02 00 00 00 14 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 07 00 52 61 6C 70 68 69 65 80 0C 16 60 B0 FF E7 7F FE 47 25 B0 83 F3 F9 3F FF C3 FF F0 09 03 00 07 00 00 00 00 80 8D 5B 00 A0 DB 5B 00 30 2E 10 00 00 00 48 3A 34 01 40 98 0F 00 01 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 A0 0F 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 A0 0F 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 01 01 01 00 01 02 00 00 00 0D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0C 00 7A 6F 6D 62 69 65 6B 69 6C 6C 65 72 C5 2E 40 FC 7F FE 07 02 FF 07 04 F2 80 6F F8 3F 9F 78 96 72 25 31 00 07 00 00 00 00 80 8D 5B 00 A0 DB 5B 00 C1 2F 10 00 00 00 A8 47 37 01 60 E6 0F 00 01 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 AC 0D 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 AC 0D 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 01 01 01 00 01 02 00 00 00 05 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0C 00 7A 6F 6D 62 69 65 6B 69 6C 6C 65 72 C5 2E 40 FC 7F FE 07 02 FF 07 04 F2 80 6F F8 3F 9F 78 96 72 25 31 00 07 00 00 00 00 80 8D 5B 00 A0 DB 5B 00 26 30 10 00 00 00 3B CE 38 01 7F 34 10 00 01 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 34 08 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 34 08 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 01 01 01 00 01 02 00 00 00 42 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0C 00 7A 6F 6D 62 69 65 6B 69 6C 6C 65 72 C5 2E 40 FC 7F FE 07 02 FF 07 04 F2 80 6F F8 3F 9F 78 96 72 25 31 00 07 00 00 00 00 80 8D 5B 00 A0 DB 5B 00 88 30 10 00 00 00 C4 54 3A 01 EF 5B 10 00 01 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 8C 0A 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 8C 0A 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 01 01 01 00 01 02 00 00 00 1A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0C 00 7A 6F 6D 62 69 65 6B 69 6C 6C 65 72 C5 2E 40 FC 7F FE 07 02 FF 07 04 F2 80 6F F8 3F 9F 78 96 72 25 31 00 07 00 00 00 00 80 8D 5B 00 A0 DB 5B 00 04 2D 10 00 00 00 59 64 3D 01 9B F9 19 00 01 00 00 00 00 00 00 00 00 00 00 00 0F 00 00 00 A0 0F 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 A0 06 94 EF C4 01 00 80 05 BB 46 E6 17 02 CC 10 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 01 01 01 00 01 02 00 00 00 04 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 43 6C 69 66 66 42 02 32 60 24 FF 67 0A FF E7 0A 02 09 3A F8 3F FF C3 FF 50 05 07 00 07 00 00 00 00 80 8D 5B 00 D0 50 5C 00 CC 2D 10 00 00 00 A4 B3 32 01 3C 71 0F 00 01 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 60 09 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 60 09 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 01 01 01 00 01 02 00 00 00 3A 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 06 00 73 74 72 69 65 6E 03 01 E5 E6 7F 38 70 06 FF E7 54 42 4A 26 F8 3F FF C3 FF 30 1D 03 00 07 00 00 00 00 80 8D 5B 00 D0 50 5C 00 30 2E 10 00 00 00 F4 3A 34 01 63 98 0F 00 01 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 A0 0F 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 A0 0F 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 01 01 01 00 01 02 00 00 00 11 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 06 00 73 74 72 69 65 6E 03 01 E5 E6 7F 38 70 06 FF E7 54 42 4A 26 F8 3F FF C3 FF 30 1D 03 00 07 00 00 00 00 80 8D 5B 00 D0 50 5C 00 5C 2F 10 00 00 00 57 C3 35 01 B3 0E 10 00 01 00 00 00 00 00 00 00 00 00 00 00 02 00 00 00 A0 0F 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 A0 0F 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 01 01 01 00 01 02 00 00 00 1A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 06 00 73 74 72 69 65 6E 03 01 E5 E6 7F 38 70 06 FF E7 54 42 4A 26 F8 3F FF C3 FF 30 1D 03 00 07 00 00 00 00 80 8D 5B 00 D0 50 5C 00 04 2D 10 00 00 00 1D 63 3D 01 59 F9 19 00 01 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 A0 0F 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 A0 0F 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 01 01 01 00 01 02 00 00 00 42 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 06 00 73 74 72 69 65 6E 03 01 E5 E6 7F 38 70 06 FF E7 54 42 4A 26 F8 3F FF C3 FF 30 1D 03 00 07 00 00 00 00 80 8D 5B 00 E0 77 5C 00 6B 2D 10 00 00 00 98 2F 31 01 4C 4E 0F 00 01 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 80 0C 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 80 0C 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 01 01 01 00 01 02 00 00 00 1C 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0B 00 6A 61 63 6B 6F 68 65 61 72 74 73 46 02 4C 8D 8A 69 F1 7F FE 67 1C D0 08 F5 F9 3F FF C3 FF 10 1B 05 00 07 00 00 00 00 80 8D 5B 00 E0 77 5C 00 CC 2D 10 00 00 00 18 B4 32 01 D4 71 0F 00 01 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 60 09 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 60 09 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 01 01 01 00 01 02 00 00 00 14 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0B 00 6A 61 63 6B 6F 68 65 61 72 74 73 46 02 4C 8D 8A 69 F1 7F FE 67 1C D0 08 F5 F9 3F FF C3 FF 10 1B 05 00 07 00 00 00 00 80 8D 5B 00 E0 77 5C 00 5C 2F 10 00 00 00 89 C1 35 01 01 06 10 00 01 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 04 10 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 04 10 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 01 01 01 00 01 02 00 00 00 13 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0B 00 6A 61 63 6B 6F 68 65 61 72 74 73 46 02 4C 8D 8A 69 F1 7F FE 67 1C D0 08 F5 F9 3F FF C3 FF 10 1B 05 00 07 00 00 00 00 80 8D 5B 00 E0 77 5C 00 04 2D 10 00 00 00 6A 64 3D 01 48 F9 19 00 01 00 00 00 00 00 00 00 00 00 00 00 0F 00 00 00 94 11 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 94 11 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 01 01 01 00 01 02 00 00 00 12 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0B 00 6A 61 63 6B 6F 68 65 61 72 74 73 46 02 4C 8D 8A 69 F1 7F FE 67 1C D0 08 F5 F9 3F FF C3 FF 10 1B 05 00 07 00 00 00 00 80 8D 5B 00 F0 9E 5C 00 5C 2F 10 00 00 00 55 C3 35 01 B2 0E 10 00 01 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 1C 0C 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 1C 0C 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 01 01 01 00 01 02 00 00 00 2B 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 08 00 62 61 62 79 64 30 31 31 79 44 71 F0 7F FE E7 7F FE A7 54 F2 3F 02 FA 3F 6F C8 FF F0 25 43 00 07 00 00 00 00 80 8D 5B 00 F0 9E 5C 00 88 30 10 00 00 00 60 56 3A 01 82 5D 10 00 01 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 08 07 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 08 07 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 01 01 01 00 01 02 00 00 00 31 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 08 00 62 61 62 79 64 30 31 31 79 44 71 F0 7F FE E7 7F FE A7 54 F2 3F 02 FA 3F 6F C8 FF F0 25 43 00 07 00 00 00 00 80 8D 5B 00 F0 9E 5C 00 04 2D 10 00 00 00 5C 64 3D 01 9F F9 19 00 01 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 A0 0F 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 A0 0F 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 01 01 01 00 01 02 00 00 00 22 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 08 00 62 61 62 79 64 30 31 31 79 44 71 F0 7F FE E7 7F FE A7 54 F2 3F 02 FA 3F 6F C8 FF F0 25 43 00 07 00 00 00 00 80 8D 5B 00 F0 9E 5C 00 B4 31 10 00 00 00 07 F6 41 01 1F D1 10 00 01 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 1C 0C 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 1C 0C 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 01 01 01 00 01 02 00 00 00 17 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 08 00 62 61 62 79 64 30 31 31 79 44 71 F0 7F FE E7 7F FE A7 54 F2 3F 02 FA 3F 6F C8 FF F0 25 43 00 07 00 00 00 00 80 8D 5B 00 00 C6 5C 00 E0 32 10 00 00 00 2B 2D 31 01 CA 4A 0F 00 01 00 00 00 00 00 00 00 00 00 00 00 06 00 00 00 08 07 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 08 07 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 01 01 01 00 01 02 00 00 00 A5 02 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 4D 65 72 6B 79 79 45 A5 51 17 FF E7 7F FE 27 4A F2 3F 5E 4A 98 FF F3 01 D2 16 67 00 07 00 00 00 00 80 8D 5B 00 00 C6 5C 00 5C 2F 10 00 00 00 36 C3 35 01 88 0E 10 00 01 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 D8 0E 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 D8 0E 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 01 01 01 00 01 02 00 00 00 12 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 4D 65 72 6B 79 79 45 A5 51 17 FF E7 7F FE 27 4A F2 3F 5E 4A 98 FF F3 01 D2 16 67 00 07 00 00 00 00 80 8D 5B 00 00 C6 5C 00 04 2D 10 00 00 00 D4 62 3D 01 26 F9 19 00 01 00 00 00 00 00 00 00 00 00 00 00 0F 00 00 00 50 14 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 50 14 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 01 01 01 00 01 02 00 00 00 25 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 4D 65 72 6B 79 79 45 A5 51 17 FF E7 7F FE 27 4A F2 3F 5E 4A 98 FF F3 01 D2 16 67 00 07 00 00 00 00 80 8D 5B 00 10 ED 5C 00 6B 2D 10 00 00 00 64 2E 31 01 AB 4C 0F 00 01 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 80 0C 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 80 0C 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 01 01 01 00 01 02 00 00 00 64 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 61 64 61 76 69 61 47 D3 70 53 FF E7 7F FE 67 1D F1 3F 01 F9 3F FF 33 7D 32 0A 0F 00 07 00 00 00 00 80 8D 5B 00 10 ED 5C 00 5C 2F 10 00 00 00 60 C1 35 01 ED 09 10 00 01 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 A0 0F 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 A0 0F 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 01 01 01 00 01 02 00 00 00 0C 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 61 64 61 76 69 61 47 D3 70 53 FF E7 7F FE 67 1D F1 3F 01 F9 3F FF 33 7D 32 0A 0F 00 07 00 00 00 00 80 8D 5B 00 10 ED 5C 00 88 30 10 00 00 00 12 55 3A 01 81 5C 10 00 01 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 B0 04 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 B0 04 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 01 01 01 00 01 02 00 00 00 3B 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 61 64 61 76 69 61 47 D3 70 53 FF E7 7F FE 67 1D F1 3F 01 F9 3F FF 33 7D 32 0A 0F 00 07 00 00 00 00 80 8D 5B 00 10 ED 5C 00 04 2D 10 00 00 00 66 62 3D 01 C1 F8 19 00 01 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 94 11 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 94 11 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 01 01 01 00 01 02 00 00 00 18 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 61 64 61 76 69 61 47 D3 70 53 FF E7 7F FE 67 1D F1 3F 01 F9 3F FF 33 7D 32 0A 0F 00 07 00 00 00 00"));

        return mplew.getPacket();
    }
   
    public static byte[] addFavorite(boolean remove, int itemSn) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CASH_SHOP_UPDATE.getValue());
        mplew.write(remove ? 0x10 : 0x0E); //16 remove
        mplew.write(1);
        mplew.writeInt(itemSn);

        return mplew.getPacket();
    }

    public static byte[] Like(int item) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CASH_SHOP_UPDATE.getValue());
        mplew.write(15);
        mplew.write(1);//todo add db row

        return mplew.getPacket();
    }

    public static byte[] Favorite(MapleCharacter chr) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CASH_SHOP_UPDATE.getValue());
        mplew.write(18);
        mplew.write(chr.getWishlistSize() > 0 ? 1 : 3);
        mplew.write(chr.getWishlistSize());
        CashItemFactory cif = CashItemFactory.getInstance();
        mplew.write(chr.getWishlistSize() > 0 ? 1 : 3);
        mplew.write(chr.getWishlistSize());
        for (int i : chr.getWishlist()) {
            CashItem ci = cif.getAllItem(i);
//        for (CashItem i : cif.getMenuItems(301)) {//TODO create and load form favorites?
            addCSItemInfo(mplew, ci);
        }
        return mplew.getPacket();
    }

    public static void addCSItemInfo(MaplePacketLittleEndianWriter mplew, CashItem item) {
        mplew.writeInt(item.getCategory());
        mplew.writeInt(item.getSubCategory()); //4000000 + 10000 + page * 10000
        mplew.writeInt(item.getParent()); //1000000 + 70000 + page * 100 + item on page
        mplew.writeMapleAsciiString(item.getImage()); //jpeg img url
        mplew.writeInt(item.getSN());
        mplew.writeInt(item.getItemId());
        mplew.writeInt(1);
        mplew.writeInt(item.getFlag());//1 =event 2=new = 4=hot
        mplew.writeInt(0);//1 = package?
        mplew.writeInt(0);//changes - type?
        mplew.writeInt(item.getPrice());
        mplew.write(HexTool.getByteArrayFromHexString("00 80 22 D6 94 EF C4 01")); // 1/1/2005
        mplew.writeLong(PacketHelper.MAX_TIME);
        mplew.write(HexTool.getByteArrayFromHexString("00 80 22 D6 94 EF C4 01")); // 1/1/2005
        mplew.writeLong(PacketHelper.MAX_TIME);
        mplew.writeInt(item.getPrice()); //after discount
        mplew.writeInt(0);
        mplew.writeInt(item.getQuantity());
        mplew.writeInt(item.getExpire());
        mplew.write(1); //buy
        mplew.write(1); //gift
        mplew.write(1); //cart
        mplew.write(0);
        mplew.write(1); //favorite
        mplew.writeInt(item.getGender());//gender female 1 male 0 nogender 2
        mplew.writeInt(item.getLikes()); //likes
        mplew.writeInt(0);
//        if(ispack){
//            mplew.writeAsciiString("lol");
//            mplew.writeShort(0);
//        }else{
        mplew.writeInt(0);
//        }
        mplew.writeInt(0);
        mplew.writeInt(0);
        List pack = CashItemFactory.getInstance().getPackageItems(item.getSN());
        if (pack == null) {
            mplew.writeInt(0);
        } else {
            mplew.writeInt(pack.size());
            for (int i = 0; i < pack.size(); i++) {
                mplew.writeInt(100000677);//item.getSN()); //should be pack item sn
                mplew.writeInt(1072443);//((Integer) pack.get(i)).intValue());
                mplew.writeInt(1);//1
                mplew.writeInt(3600); //pack item usual price
                mplew.writeInt(2880); //pack item discounted price
                mplew.writeInt(0);
                mplew.writeInt(1);
                mplew.writeInt(0);
                mplew.writeInt(2);
            }
        }
    }

    public static byte[] warpCS(MapleClient c) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPEN.getValue());
        PacketHelper.addCharacterInfo(mplew, c.getPlayer());
        
        mplew.write(1);
        mplew.writeShort(0);
        
        mplew.writeInt(0);
        
        int[][][] packages = {
            {{5533003}, {20001141, 20001142, 20001143, 20001144, 20001145, 20001146, 20001147}},
            {{5533011}, {100000578, 100000579, 100000580, 100000581, 100000582, 100000583}},
            {{5533013}, {100000424, 100000425, 100000426, 100000427, 100000428, 100000429, 100000430, 100000431, 100000432, 100000433, 100000434, 100000435, 100000436, 100000437, 100000438}},
            {{5533021}, {20800317, 20800318, 20800319, 20800320, 20800321}},
            {{5533006}, {100000172, 100000173, 100000174, 100000175, 100000176}},
            {{5533014}, {20000462, 20000463, 20000464, 20000465, 20000466, 20000467, 20000468, 20000469}},
            {{5533022}, {21100177, 21100178, 21100179}},
            {{5533023}, {20000625, 20000626, 20000627, 20000628, 20000629}},
            {{5533024}, {20000621, 20000622, 20000623, 20000624}},
            {{5533000}, {20000462, 20000463, 20000464, 20000465, 20000466, 20000467, 20000468, 20000469}},
            {{5533001}, {20800259, 20800260, 20800263, 20800264, 20800265, 20800267}},
            {{5533017}, {10002766, 10002767, 10002768, 10002769}},
            {{5533026}, {140100547, 140100548, 140100549, 140100550, 140100551, 140100552}},
            {{5533002}, {20800620, 20800621, 20800622, 20800623, 20800624}},
            {{5533018}, {100000019, 100000004, 100000005, 100000006, 100000007, 100000008, 100000009, 100000010, 100000011, 100000012, 100000013, 100000014, 100000015, 100000016, 100000017, 100000018}}
        };
        
        mplew.writeInt(packages.length);
        for (int[][] package1 : packages) {
            mplew.writeInt(package1[0][0]); // pkg id
            mplew.writeInt(package1[1].length);
            for (int j = 0; j < package1[1].length; j++) {
                mplew.writeInt(package1[1][j]); // SN
            }
        }
        
        // mplew.write(new byte[1080]);
        for(int i = 0; i < 9; i++) {
        	for(int j = 0; j < 2; j++) {
        		for(int k = 0; k < 5; k++) {
        			mplew.writeInt(0); // nCategory
        			mplew.writeInt(0); // nGender
        			mplew.writeInt(0); // nCommoditySN
        		}
        	}
        }
        
        mplew.writeShort(0);
        mplew.writeShort(0);
        mplew.writeShort(0);
        
        mplew.write(0);
        mplew.write(0);
        
        mplew.write(1);
        mplew.writeInt(1);
        
        mplew.write(0);
        mplew.write(0);
        mplew.write(0);
        mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
        
        mplew.write(0);
        mplew.write(0);
        mplew.writeInt(0);
        mplew.write(0);
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] loadCategories() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CASH_SHOP.getValue());

        mplew.write(3);
        mplew.write(1);
        CashItemFactory cif = CashItemFactory.getInstance();
        mplew.write(cif.getCategories().size()); //categories size
        for (CashCategory cat : cif.getCategories()) {
            //id: base = 1000000; favorite = +1000000; category = +10000; subcategory = +100 subsubcategory = +1
            mplew.writeInt(cat.getId());
            mplew.writeMapleAsciiString(cat.getName());
            mplew.writeInt(cat.getParentDirectory());
            mplew.writeInt(cat.getFlag());
            mplew.writeInt(cat.getSold()); //1 = sold out
        }
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] showNXMapleTokens(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_UPDATE.getValue());
        mplew.writeInt(chr.getCSPoints(1)); // NX Credit
        mplew.writeInt(chr.getCSPoints(2)); // MPoint
        mplew.writeInt(chr.getCSPoints(3)); // Maple Rewards (Not in v144.3) but needed to show Nx prepaid for some reason ._.
        mplew.writeInt(chr.getCSPoints(4)); // Nx Prepaid

        return mplew.getPacket();
    }  

    public static byte[] showMesos(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_MESO_UPDATE.getValue());
        mplew.write0(2); // short
        mplew.write(4); // byte
        mplew.write0(5); // int, byte
        mplew.writeLong(chr.getMeso());

        return mplew.getPacket();
    }

    public static byte[] LimitGoodsCountChanged() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code);
        mplew.writeInt(0); // SN
        mplew.writeInt(0); // Count
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] getCSInventory(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code + 5); // 5 = Failed + transfer //was3
        CashShop mci = c.getPlayer().getCashInventory();
        mplew.write(0);
        mplew.writeShort(mci.getItemsSize());
        if (mci.getItemsSize() > 0) {
            int size = 0;
            for (Item itemz : mci.getInventory()) {
                addCashItemInfo(mplew, itemz, c.getAccID(), 0);
                if (GameConstants.isPet(itemz.getItemId()) || GameConstants.getInventoryType(itemz.getItemId()) == MapleInventoryType.EQUIP) {
                    size++;
                }
            }
//            mplew.writeInt(size);
//            for (Item itemz : mci.getInventory()) {
//                if (GameConstants.isPet(itemz.getItemId()) || GameConstants.getInventoryType(itemz.getItemId()) == MapleInventoryType.EQUIP) {
//                    PacketHelper.addItemInfo(mplew, itemz);
//                }
//            }
        }
        if (c.getPlayer().getCashInventory().getInventory().size() > 0) {
            mplew.writeInt(0);
        }
        mplew.writeShort(c.getPlayer().getStorage().getSlots());
        mplew.writeShort(c.getCharacterSlots());
        mplew.writeShort(0);
        mplew.writeShort(c.getPlayer().getStorage().getSlots());
        return mplew.getPacket();
    }

    public static byte[] getCSGifts(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code + 8); // 7 = Failed + transfer//was8
        List<Pair<Item, String>> mci = c.getPlayer().getCashInventory().loadGifts();
        mplew.writeShort(mci.size());
        for (Pair<Item, String> mcz : mci) { // 70 Bytes, need to recheck.
            mplew.writeLong(mcz.getLeft().getUniqueId());
            mplew.writeInt(mcz.getLeft().getItemId());
            mplew.writeAsciiString(mcz.getLeft().getGiftFrom(), 13);
            mplew.writeAsciiString(mcz.getRight(), 73);
        }

        return mplew.getPacket();
    }

    public static byte[] doCSMagic() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code + 6); // 7 = Failed + transfer//6
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] sendWishList(MapleCharacter chr, boolean update) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

//        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        System.out.println("wishlist");
        mplew.write(HexTool.getByteArrayFromHexString("6E 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"));
//        mplew.write(Operation_Code + (/*update ? 15 : */8)); // 9 = Failed + transfer, 16 = Failed.
//        int[] list = chr.getWishlist();
//        for (int i = 0; i < 10; i++) {
//            mplew.writeInt(list[i] != -1 ? list[i] : 0);
//        }

        return mplew.getPacket();
    }

    public static byte[] showBoughtCSItem(Item item, int sn, int accid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code + 17);//17
        addCashItemInfo(mplew, item, accid, sn);
        mplew.write0(5);

        return mplew.getPacket();
    }

    public static byte[] showBoughtCSItem(int itemid, int sn, int uniqueid, int accid, int quantity, String giftFrom, long expire) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code + 17);
        addCashItemInfo(mplew, uniqueid, accid, itemid, sn, quantity, giftFrom, expire);
        mplew.write0(5);
        return mplew.getPacket();
    }

    public static byte[] showBoughtCSItemFailed(final int mode, final int sn) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code + 18);
        mplew.write(mode); // 0/1/2 = transfer, Rest = code
        if (mode == 29 || mode == 30) { // Limit Goods update. this item is out of stock, and therefore not available for sale.
            mplew.writeInt(sn);
        } else if (mode == 69) { // You cannot make any more purchases in %d.\r\nPlease try again in (%d + 1).
            mplew.write(1);	// Hour?	
        } else if (mode == 85) { // %s can only be purchased once a month.
            mplew.writeInt(sn);
            mplew.writeLong(System.currentTimeMillis());
        }

        return mplew.getPacket();
    }

    public static byte[] showBoughtCSPackage(Map<Integer, Item> ccc, int accid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(0xA3); //use to be 7a
        mplew.write(ccc.size());
        int size = 0;
        for (Entry<Integer, Item> sn : ccc.entrySet()) {
            addCashItemInfo(mplew, sn.getValue(), accid, sn.getKey().intValue());
            if (GameConstants.isPet(sn.getValue().getItemId()) || GameConstants.getInventoryType(sn.getValue().getItemId()) == MapleInventoryType.EQUIP) {
                size++;
            }
        }
        if (ccc.size() > 0) {
            mplew.writeInt(size);
            for (Item itemz : ccc.values()) {
                if (GameConstants.isPet(itemz.getItemId()) || GameConstants.getInventoryType(itemz.getItemId()) == MapleInventoryType.EQUIP) {
                    PacketHelper.addItemInfo(mplew, itemz);
                }
            }
        }
        mplew.writeShort(0);

        return mplew.getPacket();
    }

    public static byte[] sendGift(int price, int itemid, int quantity, String receiver, boolean packages) {
        // [ %s ] \r\nwas sent to %s. \r\n%d NX Prepaid \r\nwere spent in the process.
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code + (packages ? 74 : 25)); // 74 = Similar structure to showBoughtCSItemFailed
        mplew.writeMapleAsciiString(receiver);
        mplew.writeInt(itemid);
        mplew.writeShort(quantity);
        if (packages) {
            mplew.writeShort(0); //maplePoints
        }
        mplew.writeInt(price);

        return mplew.getPacket();
    }

    public static byte[] showCouponRedeemedItem(Map<Integer, Item> items, int mesos, int maplePoints, MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code + 19);
        mplew.write(items.size());
        for (Entry<Integer, Item> item : items.entrySet()) {
            addCashItemInfo(mplew, item.getValue(), c.getAccID(), item.getKey().intValue());
        }
        mplew.writeInt(maplePoints);
        mplew.writeInt(0); // Normal items size
        //for (Pair<Integer, Integer> item : items2) {
        //    mplew.writeInt(item.getRight()); // Count
        //    mplew.writeInt(item.getLeft());  // Item ID
        //}
        mplew.writeInt(mesos);

        return mplew.getPacket();
    }

    public static byte[] showCouponGifted(Map<Integer, Item> items, String receiver, MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code + 21); // 22 = Failed. [Mode - 0/2 = transfer, 15 = invalid 3 times]
        mplew.writeMapleAsciiString(receiver); // Split by ;
        mplew.write(items.size());
        for (Entry<Integer, Item> item : items.entrySet()) {
            addCashItemInfo(mplew, item.getValue(), c.getAccID(), item.getKey().intValue());
        }
        mplew.writeInt(0); // (amount of receiver - 1)

        return mplew.getPacket();
    }

    public static byte[] increasedInvSlots(int inv, int slots) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code + 26);
        mplew.write(inv);
        mplew.writeShort(slots);

        return mplew.getPacket();
    }

    public static byte[] increasedStorageSlots(int slots, boolean characterSlots) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code + (characterSlots ? 30 : 28)); // 32 = Buy Character. O.O
        mplew.writeShort(slots);

        return mplew.getPacket();
    }

    public static byte[] increasedPendantSlots() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code + 34); // 35 = Failed
        mplew.writeShort(0); // 0 = Add, 1 = Extend
        mplew.writeShort(100); // Related to time->Low/High fileTime
        // The time limit for the %s slot \r\nhas been extended to %d-%d-%d %d:%d.

        return mplew.getPacket();
    }

    public static byte[] confirmFromCSInventory(Item item, short pos) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code + 38); // 37 = Failed//36 8A
        mplew.write(1);
        mplew.writeShort(pos);
        PacketHelper.addItemInfo(mplew, item);
        mplew.writeInt(0); // For each: 8 bytes(Could be 2 ints or 1 long)

        return mplew.getPacket();
    }

//    public static byte[] confirmToCSInventory(Item item, int accId, int sn) {
//        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
//
//        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
//        mplew.write(Operation_Code + 40); // 39 = Failed//38
//        addCashItemInfo(mplew, item, accId, sn, false);
//        System.out.println("string " + mplew.toString());
//        return mplew.getPacket();
//    }
    public static byte[] confirmToCSInventory(Item item, int accId, int sn) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code + 40);
        addCashItemInfo(mplew, item, accId, sn, false);
//        System.out.println("string " + mplew.toString());
        return mplew.getPacket();
    }

    public static byte[] cashItemDelete(int uniqueid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code + 40); // 41 = Failed //42 is delete
        mplew.writeLong(uniqueid); // or SN?

        return mplew.getPacket();
    }

    public static byte[] rebateCashItem() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code + 67); // 41 = Failed
        mplew.writeLong(0); // UniqueID
        mplew.writeInt(0); // MaplePoints accumulated
        mplew.writeInt(0); // For each: 8 bytes.

        return mplew.getPacket();
    }

    public static byte[] sendBoughtRings(boolean couple, Item item, int sn, int accid, String receiver) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code + (couple ? 69 : 79));
        addCashItemInfo(mplew, item, accid, sn);
        mplew.writeMapleAsciiString(receiver);
        mplew.writeInt(item.getItemId());
        mplew.writeShort(1); // Count

        return mplew.getPacket();
    }

    public static byte[] receiveFreeCSItem(Item item, int sn, int accid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code + 87); // 105 = Buy Name Change, 107 = Transfer world
        addCashItemInfo(mplew, item, accid, sn);

        return mplew.getPacket();
    }

    public static byte[] cashItemExpired(int uniqueid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code + 42);
        mplew.writeLong(uniqueid);

        return mplew.getPacket();
    }

    public static byte[] showBoughtCSQuestItem(int price, short quantity, byte position, int itemid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code + 75); // 76 = Failed.
        mplew.writeInt(1); // size. below gets repeated for each.
        mplew.writeInt(quantity);
        mplew.writeInt(itemid);

        return mplew.getPacket();
    }

    public static byte[] updatePurchaseRecord() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code + 94); // 95 = Failed. //94
        mplew.writeInt(0);
        mplew.write(1); // boolean

        return mplew.getPacket();
    }

    public static byte[] sendCashRefund(final int cash) {
        // Your refund has been processed. \r\n(%d NX Refund)
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code + 97);
        mplew.writeInt(0); // Item Size.->For each 8 bytes.
        mplew.writeInt(cash); // NX

        return mplew.getPacket();
    }

    public static byte[] sendRandomBox(int uniqueid, Item item, short pos) { // have to revise this
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code + 99); // 100 = Failed
        mplew.writeLong(uniqueid);
        mplew.writeInt(1302000);
        PacketHelper.addItemInfo(mplew, item);
        mplew.writeShort(0);
        mplew.writeInt(0); // Item Size.->For each 8 bytes.

        return mplew.getPacket();
    }

    public static byte[] sendCashGachapon(final boolean cashItem, int idFirst, Item item, int accid) { // Xmas Surprise, Cash Shop Surprise
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code + 109); // 110 = Failed.		
        mplew.writeLong(idFirst); //uniqueid of the xmas surprise itself
        mplew.writeInt(0);
        mplew.write(cashItem ? 1 : 0);
        if (cashItem) {
            addCashItemInfo(mplew, item, accid, 0); //info of the new item, but packet shows 0 for sn?
        }
        mplew.writeInt(item.getItemId());
        mplew.write(1);

        return mplew.getPacket();
    }

    public static byte[] sendTwinDragonEgg(boolean test1, boolean test2, int idFirst, Item item, int accid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code + 111); // 112 = Failed.		
        mplew.write(test1 ? 1 : 0);
        mplew.write(test2 ? 1 : 0);
        mplew.writeInt(1);
        mplew.writeInt(2);
        mplew.writeInt(3);
        mplew.writeInt(4);
        if (test1 && test2) {
            addCashItemInfo(mplew, item, accid, 0); //info of the new item, but packet shows 0 for sn?
        }

        return mplew.getPacket();
    }

    public static byte[] sendBoughtMaplePoints(final int maplePoints) {
        // You've received %d Maple Points.
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code + 113);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(maplePoints);

        return mplew.getPacket();
    }

    public static byte[] receiveGachaStamps(final boolean invfull, final int amount) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GACHAPON_STAMPS.getValue());
        mplew.write(invfull ? 0 : 1);
        if (!invfull) {
            mplew.writeInt(amount);
        }

        return mplew.getPacket();
    }

    public static byte[] freeCashItem(final int itemId) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.FREE_CASH_ITEM.getValue());
        mplew.writeInt(itemId);

        return mplew.getPacket();
    }

    public static byte[] showXmasSurprise(boolean full, int idFirst, Item item, int accid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.XMAS_SURPRISE.getValue());
        mplew.write(full ? 212 : 213);
        if (!full) {
            mplew.writeLong(idFirst); //uniqueid of the xmas surprise itself
            mplew.writeInt(0);
            addCashItemInfo(mplew, item, accid, 0); //info of the new item, but packet shows 0 for sn?
            mplew.writeInt(item.getItemId());
            mplew.write(1);
            mplew.write(1);
        }

        return mplew.getPacket();
    }

    public static byte[] showOneADayInfo(boolean show, int sn) { // hmmph->Buy regular item causes invalid pointer
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ONE_A_DAY.getValue());
        mplew.writeInt(100); //idk-related to main page
        mplew.writeInt(100000); // idk-related to main page
        mplew.writeInt(1); // size of items to buy, for each, repeat 3 ints below.
        mplew.writeInt(20121231); // yyyy-mm-dd
        mplew.writeInt(sn);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] playCashSong(int itemid, String name) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CASH_SONG.getValue());
        mplew.writeInt(itemid);
        mplew.writeMapleAsciiString(name);
        return mplew.getPacket();
    }

    public static byte[] useAlienSocket(boolean start) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ALIEN_SOCKET_CREATOR.getValue());
        mplew.write(start ? 0 : 2);

        return mplew.getPacket();
    }

    public static byte[] ViciousHammer(boolean start, int hammered) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.VICIOUS_HAMMER.getValue());
        mplew.write(start ? 0x42 : 0x46);
        mplew.writeInt(0);
        if (start) {
            mplew.writeInt(hammered);
        }

        return mplew.getPacket();
    }

    public static byte[] getLogoutGift() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.LOGOUT_GIFT.getValue());

        return mplew.getPacket();
    }

    public static byte[] GoldenHammer(byte mode, int success) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GOLDEN_HAMMER.getValue());

        mplew.write(mode);
        mplew.writeInt(success);

        /*
         * success = 1:
         * mode:
         * 3 - 2 upgrade increases\r\nhave been used already.
         */
        return mplew.getPacket();
    }

    public static byte[] changePetFlag(int uniqueId, boolean added, int flagAdded) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PET_FLAG_CHANGE.getValue());

        mplew.writeLong(uniqueId);
        mplew.write(added ? 1 : 0);
        mplew.writeShort(flagAdded);

        return mplew.getPacket();
    }

    public static byte[] changePetName(MapleCharacter chr, String newname, int slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PET_NAMECHANGE.getValue());

        mplew.writeInt(chr.getId());
        mplew.write(0);
        mplew.writeMapleAsciiString(newname);
        mplew.write(slot);

        return mplew.getPacket();
    }

    public static byte[] OnMemoResult(final byte act, final byte mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        //04 // The note has successfully been sent 
        //05 00 // The other character is online now. Please use the whisper function. 
        //05 01 // Please check the name of the receiving character. 
        //05 02 // The receiver's inbox is full. Please try again. 
        mplew.writeShort(SendPacketOpcode.SHOW_NOTES.getValue());
        mplew.write(act);
        if (act == 5) {
            mplew.write(mode);
        }

        return mplew.getPacket();
    }

    public static byte[] showNotes(final ResultSet notes, final int count) throws SQLException {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_NOTES.getValue());
        mplew.write(3);
        mplew.write(count);
        for (int i = 0; i < count; i++) {
            mplew.writeInt(notes.getInt("id"));
            mplew.writeMapleAsciiString(notes.getString("from"));
            mplew.writeMapleAsciiString(notes.getString("message"));
            mplew.writeLong(PacketHelper.getKoreanTimestamp(notes.getLong("timestamp")));
            mplew.write(notes.getInt("gift"));
            notes.next();
        }

        return mplew.getPacket();
    }

    public static byte[] useChalkboard(final int charid, final String msg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CHALKBOARD.getValue());

        mplew.writeInt(charid);
        if (msg == null || msg.length() <= 0) {
            mplew.write(0);
        } else {
            mplew.write(1);
            mplew.writeMapleAsciiString(msg);
        }

        return mplew.getPacket();
    }

    public static byte[] OnMapTransferResult(MapleCharacter chr, byte vip, boolean delete) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        // 31 00 05/08 00 // You cannot go to that place.
        // 31 00 06 00 // (null) is currently difficult to locate, so the teleport will not take place.
        // 31 00 09 00 // It's the map you're currently on.
        // 31 00 0A 00 // This map is not available to enter for the list.
        // 31 00 0B 00 // Users below level 7 are not allowed to go out from Maple Island.
        mplew.writeShort(SendPacketOpcode.TROCK_LOCATIONS.getValue());
        mplew.write(delete ? 2 : 3);
        mplew.write(vip);
        if (vip == 1) {
            int[] map = chr.getRegRocks();
            for (int i = 0; i < 5; i++) {
                mplew.writeInt(map[i]);
            }
        } else if (vip == 2) {
            int[] map = chr.getRocks();
            for (int i = 0; i < 10; i++) {
                mplew.writeInt(map[i]);
            }
        } else if (vip == 3 || vip == 5) {
            int[] map = chr.getHyperRocks();
            for (int i = 0; i < 13; i++) {
                mplew.writeInt(map[i]);
            }
        }

        return mplew.getPacket();
    }

    public static void addCashItemInfo(MaplePacketLittleEndianWriter mplew, Item item, int accId, int sn) {
        addCashItemInfo(mplew, item, accId, sn, true);
    }

    public static void addCashItemInfo(MaplePacketLittleEndianWriter mplew, Item item, int accId, int sn, boolean isFirst) {
        addCashItemInfo(mplew, item.getUniqueId(), accId, item.getItemId(), sn, item.getQuantity(), item.getGiftFrom(), item.getExpiration(), isFirst); //owner for the lulz
    }

    public static void addCashItemInfo(MaplePacketLittleEndianWriter mplew, int uniqueid, int accId, int itemid, int sn, int quantity, String sender, long expire) {
        addCashItemInfo(mplew, uniqueid, accId, itemid, sn, quantity, sender, expire, true);
    }

    public static void addCashItemInfo(MaplePacketLittleEndianWriter mplew, int uniqueid, int accId, int itemid, int sn, int quantity, String sender, long expire, boolean isFirst) {
        mplew.writeLong(uniqueid > 0 ? uniqueid : 0);
        mplew.writeLong(accId);
        mplew.writeInt(itemid);
        mplew.writeInt(sn);
        mplew.writeShort(1);//quantity
        mplew.writeAsciiString(sender, 13); //owner for the lulzlzlzl
        mplew.write(HexTool.getByteArrayFromHexString("00 80 05 BB 46 E6 17 02 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"));
//        mplew.write(HexTool.getByteArrayFromHexString("00 80 05 BB 46 E6 17 02"));
//        mplew.writeLong(isFirst ?  0 : sn);
//        mplew.writeZeroBytes(10);
//        // new: this part, or part of this, may be outside of the item loop.
//        mplew.writeLong(0);
//        mplew.write(HexTool.getByteArrayFromHexString("00 40 E0 FD 3B 37 4F 01"));
////        mplew.writeLong(PacketHelper.getTime(-2L));
//        // i suspect these are outside of the loop, no way to confirm though
//        mplew.writeLong(0);
//        mplew.writeLong(0);
//        mplew.writeZeroBytes(5); //new v142

        //additional 4 bytes for some stuff?
        //if (isFirst && uniqueid > 0 && GameConstants.isEffectRing(itemid)) {
        //	MapleRing ring = MapleRing.loadFromDb(uniqueid);
        //	if (ring != null) { //or is this only for friendship rings, i wonder. and does isFirst even matter
        //		mplew.writeMapleAsciiString(ring.getPartnerName());
        //		mplew.writeInt(itemid);
        //		mplew.writeShort(quantity);
        //	}
        //}
    }

    public static byte[] sendCSFail(int err) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code + 22);
        mplew.write(err);
        // 1: Request timed out.\r\nPlease try again.
        // 3: You don't have enough cash.
        // 4: You can't buy someone a cash item gift if you're under 14.
        // 5: You have exceeded the allotted limit of price\r\nfor gifts.
        // 10: Please check and see if you have exceeded\r\nthe number of cash items you can have.
        // 11: Please check and see\r\nif the name of the character is wrong,\r\nor if the item has gender restrictions.
        // 44/69: You have reached the daily maximum \r\npurchase limit for the Cash Shop.
        // 22: Due to gender restrictions, the coupon \r\nis unavailable for use.
        // 17: This coupon was already used.
        // 16: This coupon has expired.
        // 18: This coupon can only be used at\r\nNexon-affiliated Internet Cafe's.\r\nPlease use the Nexon-affiliated Internet Cafe's.
        // 19: This coupon is a Nexon-affiliated Internet Cafe-only coupon,\r\nand it had already been used.
        // 20: This coupon is a Nexon-affiliated Internet Cafe-only coupon,\r\nand it had already been expired.
        // 14: Please check and see if \r\nthe coupon number is right.
        // 23: This coupon is only for regular items, and \r\nit's unavailable to give away as a gift.
        // 24: This coupon is only for MapleStory, and\r\nit cannot be gifted to others.
        // 25: Please check if your inventory is full or not.
        // 26: This item is only available for purchase by a user at the premium service internet cafe.
        // 27: You are sending a gift to an invalid recipient.\r\nPlease check the character name and gender.
        // 28: Please check the name of the receiver.
        // 29: Items are not available for purchase\r\n at this hour.
        // 30: The item is out of stock, and therefore\r\nnot available for sale.
        // 31: You have exceeded the spending limit of NX.
        // 32: You do not have enough mesos.
        // 33: The Cash Shop is unavailable\r\nduring the beta-test phase.\r\nWe apologize for your inconvenience.
        // 34: Check your PIC password and\r\nplease try again.
        // 37: Please verify your 2nd password and\r\ntry again.
        // 21: This is the NX coupon number.\r\nRegister your coupon at www.nexon.net.
        // 38: This coupon is only available to the users buying cash item for the first time.
        // 39: You have already applied for this.
        // 47: You have exceeded the maximum number\r\nof usage per account\for this account.\r\nPlease check the coupon for detail.
        // 49: The coupon system will be available soon.
        // 50: This item can only be used 15 days \r\nafter the account's registration.
        // 51: You do not have enough Gift Tokens \r\nin your account. Please charge your account \r\nwith Nexon Game Cards to receive \r\nGift Tokens to gift this item.
        // 52: Due to technical difficulties,\r\nthis item cannot be sent at this time.\r\nPlease try again.
        // 53: You may not gift items for \r\nit has been less than two weeks \r\nsince you first charged your account.
        // 54: Users with history of illegal activities\r\n may not gift items to others. Please make sure \r\nyour account is neither previously blocked, \r\nnor illegally charged with NX.
        // 55: Due to limitations, \r\nyou may not gift this item as this time. \r\nPlease try again later.
        // 56: You have exceeded the amount of time \r\nyou can gift items to other characters.
        // 57: This item cannot be gifted \r\ndue to technical difficulties. \r\nPlease try again later.
        // 58: You cannot transfer \r\na character under level 20.
        // 59: You cannot transfer a character \r\nto the same world it is currently in.
        // 60: You cannot transfer a character \r\ninto the new server world.
        // 61: You may not transfer out of this \r\nworld at this time.
        // 62: You cannot transfer a character into \r\na world that has no empty character slots.
        // 63: The event has either ended or\r\nthis item is not available for free testing.
        // 6: You cannot send a gift to your own account.\r\nPlease purchase it after logging\r\nin with the related character.
        // 7: That character could not be found in this world.\r\nGifts can only be sent to character\r\nin the same world.
        // 8: This item has a gender restriction.\r\nPlease confirm the gender of the recipient.
        // 9: The gift cannot be sent because\r\nthe recipient's Inventory is full.
        // 64: This item cannot be purchased \r\nwith MaplePoints.
        // 65: Sorry for inconvinence. \r\nplease try again.
        // 67: This item cannot be\r\npurchased by anyone under 7.
        // 68: This item cannot be\r\nreceived by anyone under 7.
        // 66: You can no longer purchase or gift that Item of the Day.
        // 70: NX use is restricted.\r\nPlease change your settings in the NX Security Settings menu\r\nin the Nexon Portal My Info section.
        // 74: This item is not currently for sale.
        // 81: You have too many Cash Items.\r\nPlease clear 1 Cash slot and try again.
        // 90: You have exceeded the purchase limit for this item.\r\nYou cannot buy anymore.
        // 88: This item is non-refundable.
        // 87: Items cannot be refunded if\r\n7 days have passed from purchase.
        // 89: Refund cannot be processed, as some of the items in this\r\npackage have been used.
        // 86: Refund is currently unavailable.
        // 91: You cannot name change.\r\na character under level 10.
        // default: Due to an unknown error,\r\nthe request for Cash Shop has failed.

        return mplew.getPacket();
    }

    public static byte[] enableCSUse() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.TOGGLE_CASHSHOP.getValue());
        mplew.write(1);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] getBoosterPack(int f1, int f2, int f3) { //item IDs
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.BOOSTER_PACK.getValue());
        mplew.write(0xD7);
        mplew.writeInt(f1);
        mplew.writeInt(f2);
        mplew.writeInt(f3);

        return mplew.getPacket();
    }

    public static byte[] getBoosterPackClick() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.BOOSTER_PACK.getValue());
        mplew.write(0xD5);

        return mplew.getPacket();
    }

    public static byte[] getBoosterPackReveal() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.BOOSTER_PACK.getValue());
        mplew.write(0xD6);

        return mplew.getPacket();
    }

    public static byte[] sendMesobagFailed(final boolean random) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(random ? SendPacketOpcode.R_MESOBAG_FAILURE.getValue() : SendPacketOpcode.MESOBAG_FAILURE.getValue());

        return mplew.getPacket();
    }

    public static byte[] sendMesobagSuccess(int mesos) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MESOBAG_SUCCESS.getValue());
        mplew.writeInt(mesos);
        return mplew.getPacket();
    }

    public static byte[] sendRandomMesobagSuccess(int size, int mesos) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.R_MESOBAG_SUCCESS.getValue());
        mplew.write(size); // 1 = small, 2 = adequete, 3 = large, 4 = huge
        mplew.writeInt(mesos);

        return mplew.getPacket();
    }
}
