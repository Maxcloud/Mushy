package handling.handlers;

import client.MapleClient;
import handling.PacketHandler;
import handling.RecvPacketOpcode;
import handling.channel.handler.InventoryHandler;
import tools.data.LittleEndianAccessor;

public class ItemPickupHandler {

    @PacketHandler(opcode = RecvPacketOpcode.ITEM_PICKUP)
    public static void handle(MapleClient c, LittleEndianAccessor slea){
    	InventoryHandler.Pickup_Player(slea, c, c.getPlayer());
    }
}
