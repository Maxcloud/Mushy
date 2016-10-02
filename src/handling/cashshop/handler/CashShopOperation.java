package handling.cashshop.handler;

import client.MapleCharacter;
import client.MapleCharacterUtil;
import client.MapleClient;
import client.inventory.Item;
import client.inventory.MapleInventoryIdentifier;
import client.inventory.MapleInventoryType;
import client.inventory.MapleRing;
import constants.GameConstants;
import handling.cashshop.CashShopServer;
import handling.channel.ChannelServer;
import handling.login.LoginServer;
import handling.world.CharacterTransfer;
import handling.world.World;
import net.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.cash.CashItem;
import server.cash.CashItemFactory;
import server.cash.CashItemInfo;
import server.quest.MapleQuest;
import server.quest.MapleQuestStatus;
import tools.FileoutputUtil;
import tools.Triple;
import tools.data.LittleEndianAccessor;
import tools.packet.CField;
import tools.packet.CWvsContext;
import tools.packet.CSPacket;

public class CashShopOperation {

    public static void LeaveCS(final LittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
        CashShopServer.getPlayerStorage().deregisterPlayer(chr);
        c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION, c.getSessionIPAddress());

        try {

            World.ChannelChange_Data(new CharacterTransfer(chr), chr.getId(), c.getChannel());
            c.getSession().write(CField.getChannelChange(c, Integer.parseInt(ChannelServer.getInstance(c.getChannel()).getIP().split(":")[1])));
        } finally {
            final String s = c.getSessionIPAddress();
            LoginServer.addIPAuth(s.substring(s.indexOf('/') + 1, s.length()));
            chr.saveToDB(false, true);
            c.setPlayer(null);
            c.setReceiving(false);
        }
    }

    public static void EnterCS(final CharacterTransfer transfer, final MapleClient c) {
        if (transfer == null) {
            c.getSession().close();
            return;
        }
        MapleCharacter chr = MapleCharacter.ReconstructChr(transfer, c, false);

        c.setPlayer(chr);
        c.setAccID(chr.getAccountID());

        if (!c.CheckIPAddress()) { // Remote hack
            c.getSession().close();
            return;
        }

        final int state = c.getLoginState();
        boolean allowLogin = false;
        if (state == MapleClient.LOGIN_SERVER_TRANSITION || state == MapleClient.CHANGE_CHANNEL) {
            if (!World.isCharacterListConnected(c.loadCharacterNames(c.getWorld()))) {
                allowLogin = true;
            }
        }
        if (!allowLogin) {
            c.setPlayer(null);
            c.getSession().close();
            return;
        }
        c.updateLoginState(MapleClient.LOGIN_LOGGEDIN, c.getSessionIPAddress());
        CashShopServer.getPlayerStorage().registerPlayer(chr);
        c.getSession().write(CSPacket.warpCS(c));
        c.getSession().write(CSPacket.loadCategories());
        c.getSession().write(CSPacket.CS_Picture_Item());
        c.getSession().write(CSPacket.CS_Top_Items());
        c.getSession().write(CSPacket.CS_Special_Item());
        c.getSession().write(CSPacket.CS_Featured_Item());
        c.getSession().write(CSPacket.showNXMapleTokens(c.getPlayer()));
        //doCSPackets(c);
        // loadCashShop(c);
        //doCSPackets(c);
    }

    public static void loadCashShop(MapleClient c) {
        c.getSession().write(CSPacket.loadCategories());
        String head = "55 05";
        c.getSession().write(CField.getPacketFromHexString(head + " 03 01 68 80 84 1E 00 08 00 46 61 76 6F 72 69 74 65 01 00 00 00 00 00 00 00 00 00 00 00 50 69 0F 00 12 00 53 70 65 63 69 61 6C 20 50 72 6F 6D 6F 74 69 6F 6E 73 01 00 00 00 02 00 00 00 00 00 00 00 B4 69 0F 00 0C 00 4E 65 77 20 41 72 72 69 76 61 6C 73 02 00 00 00 02 00 00 00 01 00 00 00 7C 6A 0F 00 0C 00 4C 69 6D 69 74 65 64 20 54 69 6D 65 02 00 00 00 00 00 00 00 01 00 00 00 E0 6A 0F 00 10 00 4C 69 6D 69 74 65 64 20 51 75 61 6E 74 69 74 79 02 00 00 00 00 00 00 00 00 00 00 00 F4 6F 0F 00 12 00 4D 61 70 6C 65 20 52 65 77 61 72 64 73 20 53 68 6F 70 02 00 00 00 00 00 00 00 00 00 00 00 60 90 0F 00 0B 00 54 69 6D 65 20 53 61 76 65 72 73 01 00 00 00 00 00 00 00 00 00 00 00 C4 90 0F 00 0E 00 54 65 6C 65 70 6F 72 74 20 52 6F 63 6B 73 02 00 00 00 00 00 00 00 00 00 00 00 28 91 0F 00 0B 00 49 74 65 6D 20 53 74 6F 72 65 73 02 00 00 00 00 00 00 00 00 00 00 00 8C 91 0F 00 0D 00 51 75 65 73 74 20 48 65 6C 70 65 72 73 02 00 00 00 00 00 00 00 00 00 00 00 F0 91 0F 00 0E 00 44 75 6E 67 65 6F 6E 20 50 61 73 73 65 73 02 00 00 00 00 00 00 00 00 00 00 00 70 B7 0F 00 0E 00 52 61 6E 64 6F 6D 20 52 65 77 61 72 64 73 01 00 00 00 02 00 00 00 00 00 00 00 D4 B7 0F 00 10 00 47 61 63 68 61 70 6F 6E 20 54 69 63 6B 65 74 73 02 00 00 00 00 00 00 00 01 00 00 00 38 B8 0F 00 0E 00 53 75 72 70 72 69 73 65 20 42 6F 78 65 73 02 00 00 00 00 00 00 00 00 00 00 00 00 B9 0F 00 0A 00 4D 65 73 6F 20 53 61 63 6B 73 02 00 00 00 00 00 00 00 00 00 00 00 9C B8 0F 00 0D 00 53 70 65 63 69 61 6C 20 49 74 65 6D 73 02 00 00 00 00 00 00 00 00 00 00 00 80 DE 0F 00 17 00 45 71 75 69 70 6D 65 6E 74 20 4D 6F 64 69 66 69 63 61 74 69 6F 6E 73 01 00 00 00 00 00 00 00 00 00 00 00 E4 DE 0F 00 0D 00 4D 69 72 61 63 6C 65 20 43 75 62 65 73 02 00 00 00 00 00 00 00 00 00 00 00 CC E2 0F 00 0D 00 46 75 73 69 6F 6E 20 41 6E 76 69 6C 73 02 00 00 00 00 00 00 00 00 00 00 00 AC DF 0F 00 07 00 53 63 72 6F 6C 6C 73 02 00 00 00 00 00 00 00 00 00 00 00 10 E0 0F 00 0D 00 55 70 67 72 61 64 65 20 53 6C 6F 74 73 02 00 00 00 00 00 00 00 00 00 00 00 74 E0 0F 00 05 00 54 72 61 64 65 02 00 00 00 00 00 00 00 00 00 00 00 A0 E1 0F 00 08 00 50 61 63 6B 61 67 65 73 02 00 00 00 00 00 00 00 00 00 00 00 04 E2 0F 00 08 00 44 75 72 61 74 69 6F 6E 02 00 00 00 00 00 00 00 00 00 00 00 68 E2 0F 00 0B 00 42 79 70 61 73 73 20 4B 65 79 73 02 00 00 00 00 00 00 00 00 00 00 00 3C E1 0F 00 05 00 4F 74 68 65 72 02 00 00 00 00 00 00 00 00 00 00 00 3D E1 0F 00 08 00 49 74 65 6D 20 54 61 67 03 00 00 00 00 00 00 00 00 00 00 00 3E E1 0F 00 0B 00 49 74 65 6D 20 47 75 61 72 64 73 03 00 00 00 00 00 00 00 00 00 00 00 90 05 10 00 17 00 43 68 61 72 61 63 74 65 72 20 4D 6F 64 69 66 69 63 61 74 69 6F 6E 73 01 00 00 00 00 00 00 00 00 00 00 00 F4 05 10 00 13 00 53 50 2F 41 50 20 6D 6F 64 69 66 69 63 61 74 69 6F 6E 73 02 00 00 00 00 00 00 00 00 00 00 00 58 06 10 00 0B 00 45 58 50 20 43 6F 75 70 6F 6E 73 02 00 00 00 00 00 00 00 00 00 00 00 BC 06 10 00 0C 00 44 72 6F 70 20 43 6F 75 70 6F 6E 73 02 00 00 00 00 00 00 00 00 00 00 00 20 07 10 00 0F 00 49 6E 76 65 6E 74 6F 72 79 20 73 6C 6F 74 73 02 00 00 00 00 00 00 00 00 00 00 00 84 07 10 00 13 00 53 6B 69 6C 6C 20 4D 6F 64 69 66 69 63 61 74 69 6F 6E 73 02 00 00 00 00 00 00 00 00 00 00 00 E8 07 10 00 0A 00 50 72 6F 74 65 63 74 69 6F 6E 02 00 00 00 00 00 00 00 00 00 00 00 4C 08 10 00 07 00 57 65 64 64 69 6E 67 02 00 00 00 00 00 00 00 00 00 00 00 B0 08 10 00 05 00 4F 74 68 65 72 02 00 00 00 00 00 00 00 00 00 00 00 14 09 10 00 08 00 50 61 63 6B 61 67 65 73 02 00 00 00 00 00 00 00 00 00 00 00 78 09 10 00 05 00 4D 6F 75 6E 74 02 00 00 00 00 00 00 00 00 00 00 00 A0 2C 10 00 09 00 45 71 75 69 70 6D 65 6E 74 01 00 00 00 00 00 00 00 00 00 00 00 04 2D 10 00 06 00 57 65 61 70 6F 6E 02 00 00 00 00 00 00 00 00 00 00 00 05 2D 10 00 07 00 53 68 69 65 6C 64 73 03 00 00 00 00 00 00 00 00 00 00 00 06 2D 10 00 13 00 54 68 72 6F 77 69 6E 67 20 53 74 61 72 20 43 6F 76 65 72 03 00 00 00 00 00 00 00 00 00 00 00 68 2D 10 00 03 00 48 61 74 02 00 00 00 00 00 00 00 00 00 00 00 69 2D 10 00 0F 00 46 75 6C 6C 20 48 65 61 64 20 43 6F 76 65 72 03 00 00 00 00 00 00 00 00 00 00 00 6A 2D 10 00 07 00 42 65 61 6E 69 65 73 03 00 00 00 00 00 00 00 00 00 00 00 6B 2D 10 00 07 00 48 61 69 72 70 69 6E 03 00 00 00 00 00 00 00 00 00 00 00 6C 2D 10 00 08 00 48 61 69 72 62 61 6E 64 03 00 00 00 00 00 00 00 00 00 00 00 6D 2D 10 00 0D 00 46 75 6C 6C 20 42 72 69 6D 20 48 61 74 03 00 00 00 00 00 00 00 00 00 00 00 6E 2D 10 00 04 00 43 61 70 73 03 00 00 00 00 00 00 00 00 00 00 00 73 2D 10 00 05 00 4F 74 68 65 72 03 00 00 00 00 00 00 00 00 00 00 00 CC 2D 10 00 04 00 46 61 63 65 02 00 00 00 00 00 00 00 00 00 00 00 30 2E 10 00 03 00 45 79 65 02 00 00 00 00 00 00 00 00 00 00 00 94 2E 10 00 09 00 41 63 63 65 73 73 6F 72 79 02 00 00 00 00 00 00 00 00 00 00 00 95 2E 10 00 05 00 53 74 61 74 73 03 00 00 00 00 00 00 00 00 00 00 00 F8 2E 10 00 08 00 45 61 72 72 69 6E 67 73 02 00 00 00 00 00 00 00 00 00 00 00 5C 2F 10 00 07 00 4F 76 65 72 61 6C 6C 02 00 00 00 00 00 00 00 00 00 00 00 C0 2F 10 00 03 00 54 6F 70 02 00 00 00 00 00 00 00 00 00 00 00 C1 2F 10 00 0C 00 4C 6F 6E 67 20 53 6C 65 65 76 65 73 03 00 00 00 00 00 00 00 00 00 00 00 C2 2F 10 00 0D 00 53 68 6F 72 74 20 53 6C 65 65 76 65 73 03 00 00 00 00 00 00 00 00 00 00 00 24 30 10 00 06 00 42 6F 74 74 6F 6D 02 00 00 00 00 00 00 00 00 00 00 00 25 30 10 00 06 00 53 68 6F 72 74 73 03 00 00 00 00 00 00 00 00 00 00 00 26 30 10 00 05 00 50 61 6E 74 73 03 00 00 00 00 00 00 00 00 00 00 00 27 30 10 00 06 00 53 6B 69 72 74 73 03 00 00 00 00 00 00 00 00 00 00 00 88 30 10 00 05 00 53 68 6F 65 73 02 00 00 00 00 00 00 00 00 00 00 00 EC 30 10 00 05 00 47 6C 6F 76 65 02 00 00 00 00 00 00 00 00 00 00 00 50 31 10 00 04 00 52 69 6E 67 02 00 00 00 00 00 00 00 00 00 00 00 51 31 10 00 05 00 53 74 61 74 73 03 00 00 00 00 00 00 00 00 00 00 00 52 31 10 00 0A 00 46 72 69 65 6E 64 73 68 69 70 03 00 00 00 00 00 00 00 00 00 00 00 53 31 10 00 05 00 4C 61 62 65 6C 03 00 00 00 00 00 00 00 00 00 00 00 54 31 10 00 05 00 51 75 6F 74 65 03 00 00 00 00 00 00 00 00 00 00 00 56 31 10 00 04 00 53 6F 6C 6F 03 00 00 00 00 00 00 00 00 00 00 00 B4 31 10 00 04 00 43 61 70 65 02 00 00 00 00 00 00 00 00 00 00 00 7C 32 10 00 08 00 50 61 63 6B 61 67 65 73 02 00 00 00 00 00 00 00 01 00 00 00 E0 32 10 00 0B 00 54 72 61 6E 73 70 61 72 65 6E 74 02 00 00 00 00 00 00 00 00 00 00 00 44 33 10 00 0C 00 44 61 6D 61 67 65 20 53 6B 69 6E 73 02 00 00 00 00 00 00 00 00 00 00 00 B0 53 10 00 0A 00 41 70 70 65 61 72 61 6E 63 65 01 00 00 00 00 00 00 00 00 00 00 00 14 54 10 00 0D 00 42 65 61 75 74 79 20 50 61 72 6C 6F 72 02 00 00 00 00 00 00 00 00 00 00 00 15 54 10 00 04 00 48 61 69 72 03 00 00 00 00 00 00 00 00 00 00 00 16 54 10 00 04 00 46 61 63 65 03 00 00 00 00 00 00 00 00 00 00 00 17 54 10 00 04 00 53 6B 69 6E 03 00 00 00 00 00 00 00 00 00 00 00 78 54 10 00 12 00 46 61 63 69 61 6C 20 45 78 70 72 65 73 73 69 6F 6E 73 02 00 00 00 00 00 00 00 00 00 00 00 DC 54 10 00 06 00 45 66 66 65 63 74 02 00 00 00 00 00 00 00 00 00 00 00 40 55 10 00 0F 00 54 72 61 6E 73 66 6F 72 6D 61 74 69 6F 6E 73 02 00 00 00 00 00 00 00 00 00 00 00 A4 55 10 00 07 00 53 70 65 63 69 61 6C 02 00 00 00 00 00 00 00 00 00 00 00 C0 7A 10 00 03 00 50 65 74 01 00 00 00 00 00 00 00 00 00 00 00 24 7B 10 00 04 00 50 65 74 73 02 00 00 00 00 00 00 00 00 00 00 00 88 7B 10 00 0E 00 50 65 74 20 41 70 70 65 61 72 61 6E 63 65 02 00 00 00 00 00 00 00 00 00 00 00 EC 7B 10 00 07 00 50 65 74 20 55 73 65 02 00 00 00 00 00 00 00 00 00 00 00 50 7C 10 00 08 00 50 65 74 20 46 6F 6F 64 02 00 00 00 00 00 00 00 00 00 00 00 B4 7C 10 00 08 00 50 61 63 6B 61 67 65 73 02 00 00 00 00 00 00 00 00 00 00 00 18 7D 10 00 0A 00 50 65 74 20 53 6B 69 6C 6C 73 02 00 00 00 00 00 00 00 00 00 00 00 D0 A1 10 00 0B 00 46 72 65 65 20 4D 61 72 6B 65 74 01 00 00 00 00 00 00 00 00 00 00 00 34 A2 10 00 0C 00 53 68 6F 70 20 50 65 72 6D 69 74 73 02 00 00 00 00 00 00 00 00 00 00 00 E0 C8 10 00 14 00 4D 65 73 73 65 6E 67 65 72 20 61 6E 64 20 53 6F 63 69 61 6C 01 00 00 00 00 00 00 00 00 00 00 00 44 C9 10 00 0A 00 4D 65 67 61 70 68 6F 6E 65 73 02 00 00 00 00 00 00 00 00 00 00 00 A8 C9 10 00 0A 00 4D 65 73 73 65 6E 67 65 72 73 02 00 00 00 00 00 00 00 00 00 00 00 70 CA 10 00 0F 00 57 65 61 74 68 65 72 20 45 66 66 65 63 74 73 02 00 00 00 00 00 00 00 00 00 00 00 71 CA 10 00 05 00 53 74 61 74 73 03 00 00 00 00 00 00 00 00 00 00 00 72 CA 10 00 09 00 4E 6F 6E 2D 53 74 61 74 73 03 00 00 00 00 00 00 00 00 00 00 00 D4 CA 10 00 05 00 4F 74 68 65 72 02 00 00 00 00 00 00 00 00 00 00 00 20 D6 13 00 0C 00 4D 6F 6E 73 74 65 72 20 4C 69 66 65 01 00 00 00 00 00 00 00 00 00 00 00 84 D6 13 00 0A 00 49 6E 63 75 62 61 74 6F 72 73 02 00 00 00 00 00 00 00 00 00 00 00 E8 D6 13 00 04 00 47 65 6D 73 02 00 00 00 00 00 00 00 00 00 00 00"));
        c.getSession().write(CField.getPacketFromHexString(head + " 04 01 04 00 09 3D 00 10 30 3D 00 38 B8 0F 00 34 00 68 74 74 70 3A 2F 2F 6E 78 63 61 63 68 65 2E 6E 65 78 6F 6E 2E 6E 65 74 2F 75 6D 62 72 61 63 6F 2F 37 39 34 31 2F 68 6D 6E 74 79 34 69 6F 37 38 2E 6A 70 67 88 2C 9A 00 AC AE 4F 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 D0 84 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 D0 84 00 00 00 00 00 00 0B 00 00 00 5A 00 00 00 3C 00 3C 00 01 00 00 00 00 00 3C 00 02 00 00 00 A6 02 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 09 3D 00 10 30 3D 00 D4 B7 0F 00 34 00 68 74 74 70 3A 2F 2F 6E 78 63 61 63 68 65 2E 6E 65 78 6F 6E 2E 6E 65 74 2F 75 6D 62 72 61 63 6F 2F 37 39 34 31 2F 68 6D 6E 74 79 34 69 6F 37 38 2E 6A 70 67 AA EE F5 05 0C AF 4F 00 01 00 00 00 03 00 00 00 00 00 00 00 00 00 00 00 64 96 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 72 41 1B 19 D2 01 00 80 05 BB 46 E6 17 02 B8 88 00 00 00 00 00 00 0B 00 00 00 5A 00 00 00 3C 00 3C 00 01 00 00 00 00 00 3C 00 02 00 00 00 02 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 09 3D 00 10 30 3D 00 7C 32 10 00 34 00 68 74 74 70 3A 2F 2F 6E 78 63 61 63 68 65 2E 6E 65 78 6F 6E 2E 6E 65 74 2F 75 6D 62 72 61 63 6F 2F 37 39 34 31 2F 68 6D 6E 74 79 34 69 6F 37 38 2E 6A 70 67 A6 ED F5 05 4B E0 8A 00 01 00 00 00 03 00 00 00 01 00 00 00 00 00 00 00 F0 6E 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 00 DE 44 13 06 D1 01 00 80 05 BB 46 E6 17 02 24 13 00 00 00 00 00 00 01 00 00 00 00 00 00 00 01 00 01 00 01 00 00 00 00 00 01 00 02 00 00 00 03 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 18 EC F5 05 5F 42 0F 00 01 00 00 00 88 13 00 00 84 03 00 00 00 00 00 00 01 00 00 00 00 00 00 00 02 00 00 00 19 EC F5 05 BC 0D 10 00 01 00 00 00 70 17 00 00 E8 03 00 00 00 00 00 00 01 00 00 00 00 00 00 00 02 00 00 00 1A EC F5 05 E4 D1 10 00 01 00 00 00 88 13 00 00 4C 04 00 00 00 00 00 00 01 00 00 00 00 00 00 00 02 00 00 00 A8 ED F5 05 9A 5C 10 00 01 00 00 00 48 0D 00 00 2C 01 00 00 00 00 00 00 01 00 00 00 00 00 00 00 02 00 00 00 4F A3 98 00 E8 F8 19 00 01 00 00 00 28 23 00 00 40 06 00 00 00 00 00 00 01 00 00 00 00 00 00 00 02 00 00 00 00 09 3D 00 10 30 3D 00 7C 32 10 00 34 00 68 74 74 70 3A 2F 2F 6E 78 63 61 63 68 65 2E 6E 65 78 6F 6E 2E 6E 65 74 2F 75 6D 62 72 61 63 6F 2F 37 39 34 31 2F 68 6D 6E 74 79 34 69 6F 37 38 2E 6A 70 67 A7 ED F5 05 4E E0 8A 00 01 00 00 00 03 00 00 00 01 00 00 00 00 00 00 00 F0 6E 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 00 DE 44 13 06 D1 01 00 80 05 BB 46 E6 17 02 24 13 00 00 00 00 00 00 01 00 00 00 00 00 00 00 01 00 01 00 01 00 00 00 00 00 01 00 02 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 22 EC F5 05 56 46 0F 00 01 00 00 00 88 13 00 00 84 03 00 00 00 00 00 00 01 00 00 00 00 00 00 00 02 00 00 00 19 EC F5 05 BC 0D 10 00 01 00 00 00 70 17 00 00 E8 03 00 00 00 00 00 00 01 00 00 00 00 00 00 00 02 00 00 00 1A EC F5 05 E4 D1 10 00 01 00 00 00 88 13 00 00 4C 04 00 00 00 00 00 00 01 00 00 00 00 00 00 00 02 00 00 00 A8 ED F5 05 9A 5C 10 00 01 00 00 00 48 0D 00 00 2C 01 00 00 00 00 00 00 01 00 00 00 00 00 00 00 02 00 00 00 4F A3 98 00 E8 F8 19 00 01 00 00 00 28 23 00 00 40 06 00 00 00 00 00 00 01 00 00 00 00 00 00 00 02 00 00 00"));
        c.getSession().write(CField.getPacketFromHexString(head + " 05 01 04 C0 C6 2D 00 D0 ED 2D 00 E4 DE 0F 00 00 00 09 E4 F5 05 79 3D 4D 00 01 00 00 00 04 00 00 00 00 00 00 00 00 00 00 00 B0 04 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 B0 04 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 3C 00 3C 00 01 00 01 00 00 00 3C 00 02 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 C0 C6 2D 00 D0 ED 2D 00 E4 DE 0F 00 00 00 0B E4 F5 05 7A 3D 4D 00 01 00 00 00 04 00 00 00 00 00 00 00 00 00 00 00 98 08 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 98 08 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 3C 00 3C 00 01 00 01 00 00 00 3C 00 02 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 C0 C6 2D 00 D0 ED 2D 00 74 E0 0F 00 00 00 7C FE FD 02 81 3A 54 00 01 00 00 00 04 00 00 00 00 00 00 00 00 00 00 00 A0 0F 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 A0 0F 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 3C 00 3C 00 01 00 00 00 00 00 3C 00 02 00 00 00 B2 01 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 C0 C6 2D 00 D0 ED 2D 00 10 E0 0F 00 00 00 3D FE FD 02 D0 FD 54 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 24 13 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 24 13 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 3C 00 3C 00 01 00 01 00 00 00 3C 00 02 00 00 00 77 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"));
        c.getSession().write(CField.getPacketFromHexString(head + " 06 01 04 C0 C6 2D 00 E0 14 2E 00 BC 06 10 00 00 00 06 FE FD 02 AA C9 51 00 01 00 00 00 04 00 00 00 00 00 00 00 00 00 00 00 DC 05 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 DC 05 00 00 00 00 00 00 01 00 00 00 01 00 00 00 01 00 01 00 01 00 00 00 00 00 01 00 02 00 00 00 A9 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 C0 C6 2D 00 E0 14 2E 00 B0 08 10 00 00 00 D4 E2 F5 05 90 40 4D 00 01 00 00 00 04 00 00 00 00 00 00 00 00 00 00 00 6C 07 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 6C 07 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 01 00 01 00 01 00 01 00 00 00 01 00 02 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 C0 C6 2D 00 E0 14 2E 00 58 06 10 00 00 00 CE A1 98 00 B0 20 57 00 01 00 00 00 04 00 00 00 00 00 00 00 00 00 00 00 C4 09 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 C4 09 00 00 00 00 00 00 01 00 00 00 0A 00 00 00 01 00 01 00 01 00 00 00 00 00 01 00 02 00 00 00 59 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 C0 C6 2D 00 E0 14 2E 00 C4 90 0F 00 00 00 BF C3 C9 01 84 E7 4C 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 AC 26 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 AC 26 00 00 00 00 00 00 01 00 00 00 1E 00 00 00 01 00 01 00 01 00 01 00 00 00 01 00 02 00 00 00 1D 03 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"));
        c.getSession().write(CField.getPacketFromHexString(head + " 08 01 04 C0 C6 2D 00 F0 3B 2E 00 CC E2 0F 00 00 00 9F A4 98 00 00 3F 4D 00 01 00 00 00 04 00 00 00 00 00 00 00 00 00 00 00 8C 0A 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 8C 0A 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 3C 00 3C 00 01 00 01 00 00 00 01 00 02 00 00 00 C3 02 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 C0 C6 2D 00 F0 3B 2E 00 20 07 10 00 00 00 13 FE FD 02 F0 DA 52 00 01 00 00 00 04 00 00 00 00 00 00 00 00 00 00 00 F4 1A 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 F4 1A 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 01 00 01 00 01 00 01 00 00 00 01 00 02 00 00 00 1B 03 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 C0 C6 2D 00 F0 3B 2E 00 E8 07 10 00 00 00 1A E6 F5 05 14 47 4E 00 01 00 00 00 04 00 00 00 00 00 00 00 00 00 00 00 E8 03 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 E8 03 00 00 00 00 00 00 01 00 00 00 01 00 00 00 01 00 01 00 01 00 01 00 00 00 01 00 02 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 C0 C6 2D 00 F0 3B 2E 00 E8 07 10 00 00 00 1B E6 F5 05 40 1B 54 00 01 00 00 00 04 00 00 00 00 00 00 00 00 00 00 00 E8 03 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 E8 03 00 00 00 00 00 00 01 00 00 00 01 00 00 00 01 00 01 00 01 00 01 00 00 00 01 00 02 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"));
    }

    public static void CSUpdate(final MapleClient c) {
        doCSPackets(c);
    }

    private static boolean CouponCodeAttempt(final MapleClient c) {
        c.couponAttempt++;
        return c.couponAttempt > 5;
    }

    public static void CouponCode(final String code, final MapleClient c) {
        if (code.length() <= 0) {
            return;
        }
        Triple<Boolean, Integer, Integer> info = null;
        try {
            info = MapleCharacterUtil.getNXCodeInfo(code);
        } catch (SQLException e) {
        }
        if (info != null && info.left) {
            if (!CouponCodeAttempt(c)) {
                int type = info.mid, item = info.right;
                try {
                    MapleCharacterUtil.setNXCodeUsed(c.getPlayer().getName(), code);
                } catch (SQLException e) {
                }
                /*
                 * Explanation of type!
                 * Basically, this makes coupon codes do
                 * different things!
                 *
                 * Type 1: A-Cash,
                 * Type 2: Maple Points
                 * Type 3: Item.. use SN
                 * Type 4: Mesos
                 */
                Map<Integer, Item> itemz = new HashMap<>();
                int maplePoints = 0, mesos = 0;
                switch (type) {
                    case 1:
                    case 2:
                        c.getPlayer().modifyCSPoints(type, item, false);
                        maplePoints = item;
                        break;
                    case 3:
                        CashItemInfo itez = CashItemFactory.getInstance().getItem(item);
                        if (itez == null) {
                            c.getSession().write(CSPacket.sendCSFail(0));
                            return;
                        }
                        byte slot = MapleInventoryManipulator.addId(c, itez.getId(), (short) 1, "", "Cash shop: coupon code" + " on " + FileoutputUtil.CurrentReadable_Date());
                        if (slot < 0) {
                            c.getSession().write(CSPacket.sendCSFail(0));
                            return;
                        } else {
                            itemz.put(item, c.getPlayer().getInventory(GameConstants.getInventoryType(item)).getItem(slot));
                        }
                        break;
                    case 4:
                        c.getPlayer().gainMeso(item, false);
                        mesos = item;
                        break;
                }
                c.getSession().write(CSPacket.showCouponRedeemedItem(itemz, mesos, maplePoints, c));
                doCSPackets(c);
            }
        } else {
            if (CouponCodeAttempt(c) == true) {
                c.getSession().write(CSPacket.sendCSFail(48)); //A1, 9F
            } else {
                c.getSession().write(CSPacket.sendCSFail(info == null ? 14 : 17)); //A1, 9F
            }
        }
    }

    public static void BuyCashItem(final LittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
        final int action = slea.readByte();
//        System.out.println("action " + action);
        if (action == 0) {
            slea.skip(2);
            CouponCode(slea.readMapleAsciiString(), c);
        } else if (action == 2) {
            boolean bUsingMaplePoints = slea.readByte() > 0; //If type is two (MapleCash being used), set to true. 
            int dwPurchaseOption = slea.readInt();
            
            slea.skip(1); //Unsure, new 176
            int sn = slea.readInt();
            
            final CashItem item = CashItemFactory.getInstance().getAllItem(sn);
            final int toCharge = slea.readInt();
            int nChkQuantity = slea.readInt(); //Quantity of the item, unused it seems. 
            //slea.readInt(); //Not sure what the final three Ints are, GMS has them zero'd out.
            //slea.readInt(); Commenting them out to prevent any type of error 38 just incase, since these are unused.
            //slea.readInt(); 
            
            if (item == null) {
                //Maybe add a check to compare quantity from packet to quantity of item retrieved from SN?
                c.getSession().write(CSPacket.sendCSFail(0));
                doCSPackets(c);
                return;
            }
            chr.modifyCSPoints(dwPurchaseOption, -toCharge, true);
            Item itemz = chr.getCashInventory().toItem(item);
            if (itemz != null) {
                chr.getCashInventory().addToInventory(itemz);
                c.getSession().write(CSPacket.showBoughtCSItem(itemz, item.getSN(), c.getAccID()));
            } else {
                c.getSession().write(CSPacket.sendCSFail(0));
                doCSPackets(c);
                return;
            }
        } else if (action == 101) {//TODO BETTER idk what it is
//            System.out.println("action 101");//might be farm mesos? RITE NOW IS FREEH
            slea.skip(1);
            int type = slea.readInt();//type
            int sn = slea.readInt();
            final CashItem item = CashItemFactory.getInstance().getAllItem(sn);
            if (item == null) {
                c.getSession().write(CSPacket.sendCSFail(0));
            }
//            chr.modifyCSPoints(type, -toCharge, true);
            Item itemz = chr.getCashInventory().toItem(item);
            if (itemz != null) {
                chr.getCashInventory().addToInventory(itemz);
                c.getSession().write(CSPacket.showBoughtCSItem(itemz, item.getSN(), c.getAccID()));
            } else {
                c.getSession().write(CSPacket.sendCSFail(0));
            }
        } else if (action == 3) {
            slea.skip(1);
            final int toCharge = slea.readInt();
            final CashItemInfo item = CashItemFactory.getInstance().getItem(slea.readInt());

            if (item != null && chr.getCSPoints(toCharge) >= item.getPrice()) {
                if (!item.genderEquals(c.getPlayer().getGender())/* && c.getPlayer().getAndroid() == null*/) {
                    c.getSession().write(CSPacket.sendCSFail(0xA7));
                    doCSPackets(c);
                    return;
                } else if (item.getId() == 5211046 || item.getId() == 5211047 || item.getId() == 5211048 || item.getId() == 5050100 || item.getId() == 5051001) {
                    c.getSession().write(CWvsContext.broadcastMsg(1, "You cannot purchase this item through cash shop."));
                    c.getSession().write(CWvsContext.enableActions());
                    doCSPackets(c);
                    return;
                } else if (c.getPlayer().getCashInventory().getItemsSize() >= 100) {
                    c.getSession().write(CSPacket.sendCSFail(0xB2));
                    doCSPackets(c);
                    return;
                }
                for (int id : GameConstants.cashBlock) {
                    if (item.getId() == id) {
                        c.getSession().write(CWvsContext.broadcastMsg(1, "You cannot purchase this item through cash shop."));
                        c.getSession().write(CWvsContext.enableActions());
                        doCSPackets(c);
                        return;
                    }
                }
                chr.modifyCSPoints(toCharge, -item.getPrice(), false);
                Item itemz = chr.getCashInventory().toItem(item);
                if (itemz != null && itemz.getUniqueId() > 0 && itemz.getItemId() == item.getId() && itemz.getQuantity() == item.getCount()) {
                    chr.getCashInventory().addToInventory(itemz);
                    //c.getSession().write(CSPacket.confirmToCSInventory(itemz, c.getAccID(), item.getSN()));
                    c.getSession().write(CSPacket.showBoughtCSItem(itemz, item.getSN(), c.getAccID()));

                } else {
                    c.getSession().write(CSPacket.sendCSFail(0));
                }
            } else {
                c.getSession().write(CSPacket.sendCSFail(0));
            }
        } else if (action == 4 /*|| action == 34*/) { //gift, package
            slea.readMapleAsciiString(); // pic
            final CashItemInfo item = CashItemFactory.getInstance().getItem(slea.readInt());
            if (action == 4) {
                slea.skip(1);
            }
            String partnerName = slea.readMapleAsciiString();
            String msg = slea.readMapleAsciiString();
            if (item == null || c.getPlayer().getCSPoints(1) < item.getPrice() || msg.length() > 73 || msg.length() < 1) { //dont want packet editors gifting random stuff =P
                c.getSession().write(CSPacket.sendCSFail(0));
                doCSPackets(c);
                return;
            }
            Triple<Integer, Integer, Integer> info = MapleCharacterUtil.getInfoByName(partnerName, c.getPlayer().getWorld());
            if (info == null || info.getLeft().intValue() <= 0 || info.getLeft().intValue() == c.getPlayer().getId() || info.getMid().intValue() == c.getAccID()) {
                c.getSession().write(CSPacket.sendCSFail(0xA2)); //9E v75
                doCSPackets(c);
                return;
            } else if (!item.genderEquals(info.getRight().intValue())) {
                c.getSession().write(CSPacket.sendCSFail(0xA3));
                doCSPackets(c);
                return;
            } else {
                //get the packets for that
                c.getPlayer().getCashInventory().gift(info.getLeft().intValue(), c.getPlayer().getName(), msg, item.getSN(), MapleInventoryIdentifier.getInstance());
                c.getPlayer().modifyCSPoints(1, -item.getPrice(), false);
                c.getSession().write(CSPacket.sendGift(item.getPrice(), item.getId(), item.getCount(), partnerName, action == 34));
            }
        } else if (action == 5) { //Increase Inventory Slot
            slea.skip(1);
            final int toCharge = slea.readInt();
            final boolean coupon = slea.readByte() > 0;
            if (coupon) {
                final MapleInventoryType type = getInventoryType(slea.readInt());
                if (chr.getCSPoints(toCharge) >= 6000 && chr.getInventory(type).getSlotLimit() < 89) {
                    chr.modifyCSPoints(toCharge, -6000, false);
                    chr.getInventory(type).addSlot((byte) 8);
                    chr.dropMessage(1, "Slots has been increased to " + chr.getInventory(type).getSlotLimit());
                    c.getSession().write(CField.getCharInfo(chr));
                } else {
                    c.getSession().write(CSPacket.sendCSFail(0xA4));
                }
            } else {
                slea.skip(1); //176
                byte bType = slea.readByte();
                final MapleInventoryType type = MapleInventoryType.getByType(bType);
                if (chr.getCSPoints(toCharge) >= 2500 && chr.getInventory(type).getSlotLimit() < 93) {
                    chr.modifyCSPoints(toCharge, -2500, false);
                    chr.getInventory(type).addSlot((byte) 4);
                    String slotName = (type == MapleInventoryType.EQUIP ? "Equip" : type == MapleInventoryType.ETC ? "Etc" : type == MapleInventoryType.SETUP ? "Setup" : type == MapleInventoryType.USE ? "Use" : "Undefined " + type);
                    chr.dropMessage(1,  slotName + " slots have been increased to " + chr.getInventory(type).getSlotLimit());
                } else {
                    c.getSession().write(CSPacket.sendCSFail(0xA4));
                }
            }
        } else if (action == 6) { // Increase inv
            slea.skip(1);
            final int toCharge = slea.readInt();
            final boolean coupon = slea.readByte() > 0;
            if (coupon) {
                final MapleInventoryType type = getInventoryType(slea.readInt());
                if (chr.getCSPoints(toCharge) >= 6000 && chr.getInventory(type).getSlotLimit() < 89) {
                    chr.modifyCSPoints(toCharge, -6000, false);
                    chr.getInventory(type).addSlot((byte) 8);
                    chr.dropMessage(1, "Slots have been increased to " + chr.getInventory(type).getSlotLimit());
                    c.getSession().write(CField.getCharInfo(chr));
                } else {
                    c.getSession().write(CSPacket.sendCSFail(0xA4));
                }
            } else {
                final MapleInventoryType type = MapleInventoryType.getByType(slea.readByte());
                if (chr.getCSPoints(toCharge) >= 2500 && chr.getInventory(type).getSlotLimit() < 93) {
                    chr.modifyCSPoints(toCharge, -2500, false);
                    chr.getInventory(type).addSlot((byte) 4);
                    chr.dropMessage(1, "Slots have been increased to " + chr.getInventory(type).getSlotLimit());
                    c.getSession().write(CField.getCharInfo(chr));
                } else {
                    c.getSession().write(CSPacket.sendCSFail(0xA4));
                }
            }

        } else if (action == 7) { // Increase slot space
            slea.skip(1);
            final int toCharge = slea.readInt();
            final int coupon = slea.readByte() > 0 ? 2 : 1;
            if (chr.getCSPoints(toCharge) >= 4000 * coupon && chr.getStorage().getSlots() < (49 - (4 * coupon))) {
                chr.modifyCSPoints(toCharge, -4000 * coupon, false);
                chr.getStorage().increaseSlots((byte) (4 * coupon));
                chr.getStorage().saveToDB();
                chr.dropMessage(1, "Storage slots increased to: " + chr.getStorage().getSlots());
                c.getSession().write(CField.getCharInfo(chr));
            } else {
                c.getSession().write(CSPacket.sendCSFail(0xA4));
            }
        } else if (action == 8) { //...10 = pendant slot expansion
            slea.skip(1);
            final int toCharge = slea.readInt();
            CashItemInfo item = CashItemFactory.getInstance().getItem(slea.readInt());
            int slots = c.getCharacterSlots();
            if (item == null || c.getPlayer().getCSPoints(toCharge) < item.getPrice() || slots > 15 || item.getId() != 5430000) {
                c.getSession().write(CSPacket.sendCSFail(0));
                doCSPackets(c);
                return;
            }
            if (c.gainCharacterSlot()) {
                c.getPlayer().modifyCSPoints(toCharge, -item.getPrice(), false);
                chr.dropMessage(1, "Character slots increased to: " + (slots + 1));
            } else {
                c.getSession().write(CSPacket.sendCSFail(0));
            }
        } else if (action == 10) { //...10 = pendant slot expansion
            //Data: 00 01 00 00 00 DC FE FD 02
            slea.readByte(); //Action is short?
            slea.readInt(); //always 1 - No Idea
            final int sn = slea.readInt();
            CashItemInfo item = CashItemFactory.getInstance().getItem(sn);
            if (item == null || c.getPlayer().getCSPoints(1) < item.getPrice() || item.getId() / 10000 != 555) {
                c.getSession().write(CSPacket.sendCSFail(0));
                doCSPackets(c);
                return;
            }
            MapleQuestStatus marr = c.getPlayer().getQuestNoAdd(MapleQuest.getInstance(GameConstants.PENDANT_SLOT));
            if (marr != null && marr.getCustomData() != null && Long.parseLong(marr.getCustomData()) >= System.currentTimeMillis()) {
                c.getSession().write(CSPacket.sendCSFail(0));
            } else {
                c.getPlayer().getQuestNAdd(MapleQuest.getInstance(GameConstants.PENDANT_SLOT)).setCustomData(String.valueOf(System.currentTimeMillis() + ((long) item.getPeriod() * 24 * 60 * 60000)));
                c.getPlayer().modifyCSPoints(1, -item.getPrice(), false);
                chr.dropMessage(1, "Additional pendant slot gained.");
            }
        } else if (action == 15) { //get item from csinventory
            //uniqueid, 00 01 01 00, type->position(short)
            Item item = c.getPlayer().getCashInventory().findByCashId((int) slea.readLong());
//            Item item = MapleItemInformationProvider.getInstance().getEquipById(item);
            if (item != null && item.getQuantity() > 0 && MapleInventoryManipulator.checkSpace(c, item.getItemId(), item.getQuantity(), item.getOwner())) {
                Item item_ = item.copy();
                short pos = MapleInventoryManipulator.addbyItem(c, item_, true);
                if (pos >= 0) {
                    if (item_.getPet() != null) {
                        item_.getPet().setInventoryPosition(pos);
                        c.getPlayer().addPet(item_.getPet());
                    }
                    c.getPlayer().getCashInventory().removeFromInventory(item);
                    c.getSession().write(CSPacket.confirmFromCSInventory(item_, pos));
                } else {
                    c.getSession().write(CSPacket.sendCSFail(0xB1));
                }
            } else {
                c.getSession().write(CSPacket.sendCSFail(0xB1));
            }
        } else if (action == 16) { //put item in cash inventory  
//            System.out.println("action 15");
            int uniqueid = (int) slea.readLong();
            MapleInventoryType type = MapleInventoryType.getByType(slea.readByte());
            Item item = c.getPlayer().getInventory(type).findByUniqueId(uniqueid);
            if (item != null && item.getQuantity() > 0 && item.getUniqueId() > 0 && c.getPlayer().getCashInventory().getItemsSize() < 100) {
                Item item_ = item.copy();
                MapleInventoryManipulator.removeFromSlot(c, type, item.getPosition(), item.getQuantity(), false);
                if (item_.getPet() != null) {
                    c.getPlayer().removePetCS(item_.getPet());
                }
                item_.setPosition((byte) 0);
                c.getPlayer().getCashInventory().addToInventory(item_);
                c.getSession().write(CSPacket.showBoughtCSItem(item, item.getUniqueId(), c.getAccID()));
                //warning: this d/cs
//                c.getSession().write(CSPacket.confirmToCSInventory(item, c.getAccID(), itemz.getSN()));
            } else {
                c.getSession().write(CSPacket.sendCSFail(0xB1));
            }
        } else if ((action == 33) || (action == 39)) {
          //  slea.skip(5);
            slea.readMapleAsciiString();
            int toCharge = slea.readInt();
            CashItemInfo item = CashItemFactory.getInstance().getItem(slea.readInt());
     //       slea.skip(1);
            String partnerName = slea.readMapleAsciiString();
            String msg = slea.readMapleAsciiString();
            if ((item == null) || (!GameConstants.isEffectRing(item.getId())) || (c.getPlayer().getCSPoints(toCharge) < item.getPrice()) || (msg.length() > 73) || (msg.length() < 1)) {
                 c.getSession().write(CSPacket.sendCSFail(0));
                doCSPackets(c);
                return;
            }
            if (!item.genderEquals(c.getPlayer().getGender())) {
                // c.getSession().write(MTSCSPacket.sendCSFail(166));
                doCSPackets(c);
                return;
            }
            if (c.getPlayer().getCashInventory().getItemsSize() >= 100) {
                // c.getSession().write(MTSCSPacket.sendCSFail(177));
                doCSPackets(c);
                return;
            }

            Triple info = MapleCharacterUtil.getInfoByName(partnerName, c.getPlayer().getWorld());
            if ((info == null) || (((Integer) info.getLeft()).intValue() <= 0) || (((Integer) info.getLeft()).intValue() == c.getPlayer().getId())) {
                //   c.getSession().write(MTSCSPacket.sendCSFail(180));
                doCSPackets(c);
                return;
            }
            if (((Integer) info.getMid()).intValue() == c.getAccID()) {
                //    c.getSession().write(MTSCSPacket.sendCSFail(163));
                doCSPackets(c);
                return;
            }
            if ((((Integer) info.getRight()).intValue() == c.getPlayer().getGender()) && (action == 30)) {
                //  c.getSession().write(MTSCSPacket.sendCSFail(161));
                doCSPackets(c);
                return;
            }

            int err = MapleRing.createRing(item.getId(), c.getPlayer(), partnerName, msg, ((Integer) info.getLeft()).intValue(), item.getSN());

            if (err != 1) {
                //  c.getSession().write(MTSCSPacket.sendCSFail(0));
                doCSPackets(c);
                return;
            }
         //   c.getPlayer().modifyCSPoints(toCharge, -amount, false);
            c.getPlayer().dropMessage(1, "Purchase successful.");
            /*   Item itemz = chr.getCashInventory().toItem(item);
             if ((itemz != null) && (itemz.getUniqueId() > 0) && (itemz.getItemId() == item.getId()) && (itemz.getQuantity() == item.getCount())) {
             chr.getCashInventory().addToInventory(itemz);
             c.getSession().write(MTSCSPacket.showBoughtCSItem(itemz, item.getSN(), c.getAccID()));
             } else {
             c.getSession().write(MTSCSPacket.sendCSFail(0));
             }*/
        } else if (action == 36) {//Packages
            slea.skip(1);
            int unk = slea.readInt();//is1
            final CashItemInfo item = CashItemFactory.getInstance().getItem(slea.readInt());
            final int toCharge = slea.readInt();
            List<Integer> ccc = null;
            if (item != null) {
                ccc = CashItemFactory.getInstance().getPackageItems(item.getId());
            }
            if (item == null || ccc == null || c.getPlayer().getCSPoints(toCharge) < item.getPrice()) {
                c.getSession().write(CSPacket.sendCSFail(0));
                doCSPackets(c);
                return;
            } else if (!item.genderEquals(c.getPlayer().getGender())) {
                c.getSession().write(CSPacket.sendCSFail(0xA6));
                doCSPackets(c);
                return;
            } else if (c.getPlayer().getCashInventory().getItemsSize() >= (100 - ccc.size())) {
                c.getSession().write(CSPacket.sendCSFail(0xB1));
                doCSPackets(c);
                return;
            }

            Map<Integer, Item> ccz = new HashMap<>();
            for (int i : ccc) {
                final CashItemInfo cii = CashItemFactory.getInstance().getSimpleItem(i);
                if (cii == null) {
                    continue;
                }
                Item itemz = c.getPlayer().getCashInventory().toItem(cii);
                if (itemz == null || itemz.getUniqueId() <= 0) {
                    continue;
                }
                for (int iz : GameConstants.cashBlock) {
                    if (itemz.getItemId() == iz) {
                    }
                }
                ccz.put(i, itemz);
                c.getPlayer().getCashInventory().addToInventory(itemz);
            }
            chr.modifyCSPoints(toCharge, -item.getPrice(), false);
            c.getSession().write(CSPacket.showBoughtCSPackage(ccz, c.getAccID()));

        } else if (action == 35 || action == 99) { //99 buy with mesos
            final CashItemInfo item = CashItemFactory.getInstance().getItem(slea.readInt());
            if (item == null || !MapleItemInformationProvider.getInstance().isQuestItem(item.getId())) {
                c.getSession().write(CSPacket.sendCSFail(0));
                doCSPackets(c);
                return;
            } else if (c.getPlayer().getMeso() < item.getPrice()) {
                c.getSession().write(CSPacket.sendCSFail(0xB8));
                doCSPackets(c);
                return;
            } else if (c.getPlayer().getInventory(GameConstants.getInventoryType(item.getId())).getNextFreeSlot() < 0) {
                c.getSession().write(CSPacket.sendCSFail(0xB1));
                doCSPackets(c);
                return;
            }
            byte pos = MapleInventoryManipulator.addId(c, item.getId(), (short) item.getCount(), null, "Cash shop: quest item" + " on " + FileoutputUtil.CurrentReadable_Date());
            if (pos < 0) {
                c.getSession().write(CSPacket.sendCSFail(0xB1));
                doCSPackets(c);
                return;
            }
            chr.gainMeso(-item.getPrice(), false);
            c.getSession().write(CSPacket.showBoughtCSQuestItem(item.getPrice(), (short) item.getCount(), pos, item.getId()));
        } else if (action == 48) {
            c.getSession().write(CSPacket.updatePurchaseRecord());
        } else if (action == 91) { // Open random box.
            final int uniqueid = (int) slea.readLong();

            //c.getSession().write(CSPacket.sendRandomBox(uniqueid, new Item(1302000, (short) 1, (short) 1, (short) 0, 10), (short) 0));
            //} else if (action == 99) { //buy with mesos
            //    int sn = slea.readInt();
            //    int price = slea.readInt();
        } else {
            System.out.println("New Action: " + action + " Remaining: " + slea.toString());
            c.getSession().write(CSPacket.sendCSFail(0));
        }
        doCSPackets(c);
    }

    public static void SwitchCategory(final LittleEndianAccessor slea, final MapleClient c) {
        int Scategory = slea.readByte();
//        System.out.println("Scategory " + Scategory);
        if (Scategory == 103) {
            slea.skip(1);
            int itemSn = slea.readInt();
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO `wishlist` VALUES (?, ?)")) {
                ps.setInt(1, c.getPlayer().getId());
                ps.setInt(2, itemSn);
                ps.executeUpdate();
                ps.close();
            } catch (SQLException ex) {
                System.out.println("error");
            }
            c.getSession().write(CSPacket.addFavorite(false, itemSn));
        } else if (Scategory == 105) {
            int item = slea.readInt();
            try {
                Connection con = DatabaseConnection.getConnection();
                try (PreparedStatement ps = con.prepareStatement("UPDATE cashshop_items SET likes = likes+" + 1 + " WHERE sn = ?")) {
                    ps.setInt(1, item);
                    ps.executeUpdate();
                }
            } catch (SQLException ex) {
            }
            c.getSession().write(CSPacket.Like(item));
        } else if (Scategory == 107) {//search results
            int resultSize = slea.readInt();
            List<Integer> itemList = new ArrayList<>();
            for(int i = 0; i<resultSize;i++){
                itemList.add(slea.readInt());
            }
            c.getSession().write(CSPacket.showSearchResults(itemList));
        } else if (Scategory == 109) {
            c.getSession().write(CSPacket.Favorite(c.getPlayer()));
        } else if (Scategory == 112) {//click on special item TODO
            //int C8 - C9 - CA
        } else if (Scategory == 113) {//buy from cart inventory TODO
            //byte buy = 1 or gift = 0
            //byte amount
            //for each SN
        } else {
            int category = slea.readInt();
            if (category == 4000000) {
                c.getSession().write(CSPacket.CS_Top_Items());
                c.getSession().write(CSPacket.CS_Picture_Item());
            } else if (category == 1060100) {
                c.getSession().write(CSPacket.showNXChar(category));
                c.getSession().write(CSPacket.changeCategory(category));
            } else {
//                System.err.println(category);
                c.getSession().write(CSPacket.changeCategory(category));
            }
        }
    }

    private static MapleInventoryType getInventoryType(final int id) {
        switch (id) {
            case 50200093:
                return MapleInventoryType.EQUIP;
            case 50200094:
                return MapleInventoryType.USE;
            case 50200197:
                return MapleInventoryType.SETUP;
            case 50200095:
                return MapleInventoryType.ETC;
            default:
                return MapleInventoryType.UNDEFINED;
        }
    }

    public static void doCSPackets(MapleClient c) {
        //c.getSession().write(CSPacket.getCSInventory(c));
        //c.getSession().write(CSPacket.doCSMagic());
        //c.getSession().write(CSPacket.getCSGifts(c));
        //c.getSession().write(CWvsContext.BuddylistPacket.updateBuddylist(c.getPlayer().getBuddylist().getBuddies()));
        //c.getSession().write(CSPacket.showNXMapleTokens(c.getPlayer()));
        //c.getSession().write(CSPacket.sendWishList(c.getPlayer(), false));
        c.getSession().write(CSPacket.showNXMapleTokens(c.getPlayer()));
        c.getSession().write(CSPacket.enableCSUse());
        c.getPlayer().getCashInventory().checkExpire(c);
    }
}
