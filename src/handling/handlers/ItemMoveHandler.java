package handling.handlers;

import client.MapleClient;
import client.inventory.MapleInventoryType;
import handling.PacketHandler;
import handling.RecvPacketOpcode;
import server.MapleInventoryManipulator;
import tools.data.LittleEndianAccessor;

public class ItemMoveHandler {

    @PacketHandler(opcode = RecvPacketOpcode.ITEM_MOVE)
    public static void handle(MapleClient c, LittleEndianAccessor lea) {
        if(c.getPlayer().hasBlockedInventory()){ //same hack as before :(
            return;
        }
        c.getPlayer().setScrolledPosition((short) 0);
        lea.skip(4); // update tick
        final MapleInventoryType type = MapleInventoryType.getByType(lea.readByte());
        final short src = lea.readShort();
        final short dst = lea.readShort();
        final short quantity = lea.readShort();

        if (src < 0 && dst > 0) {
            MapleInventoryManipulator.unequip(c, src, dst);
        } else if (dst < 0) {
            MapleInventoryManipulator.equip(c, src, dst);
        } else if (dst == 0) {
            MapleInventoryManipulator.drop(c, type, src, quantity);
        } else {
            MapleInventoryManipulator.move(c, type, src, dst);
        }
    }
}