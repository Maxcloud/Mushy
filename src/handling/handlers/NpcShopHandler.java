package handling.handlers;

import client.MapleClient;
import constants.GameConstants;
import handling.PacketHandler;
import handling.RecvPacketOpcode;
import server.shops.MapleShop;
import tools.data.LittleEndianAccessor;

public class NpcShopHandler {

	@PacketHandler(opcode = RecvPacketOpcode.NPC_SHOP)
	public static void handle(MapleClient c, LittleEndianAccessor lea) {
		System.out.println(lea.toString());
		
		byte bmode = lea.readByte();
        if (c.getPlayer() == null) {
            return;
        }

        switch (bmode) {
            case 0: {
                MapleShop shop = c.getPlayer().getShop();
                if (shop == null) {
                    return;
                }
                short slot = lea.readShort();
                slot++;
                int itemId = lea.readInt();
                short quantity = lea.readShort();
                // int unitprice = slea.readInt();
                shop.buy(c, slot, itemId, quantity);
                break;
            }
            case 1: {
                MapleShop shop = c.getPlayer().getShop();
                if (shop == null) {
                    return;
                }
                byte slot = (byte) lea.readShort();
                int itemId = lea.readInt();
                short quantity = lea.readShort();
                shop.sell(c, GameConstants.getInventoryType(itemId), slot, quantity);
                break;
            }
            case 2: {
                MapleShop shop = c.getPlayer().getShop();
                if (shop == null) {
                    return;
                }
                byte slot = (byte) lea.readShort();
                shop.recharge(c, slot);
                break;
            }
            default:
                c.getPlayer().setConversation(0);
        }
	}
}
