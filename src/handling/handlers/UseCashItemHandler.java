package handling.handlers;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import handling.PacketHandler;
import handling.RecvPacketOpcode;
import tools.data.LittleEndianAccessor;
import tools.packet.CField;
import tools.packet.CWvsContext;

public class UseCashItemHandler {

    //[Recv] (0x00FE) Data: FA 28 65 00 04 00 79 3D 4D 00 01 00 00 00
    //[Recv] (0x00FE) Data: [0C 53 65 00] [04 00] [79 3D 4D 00] [01 00] [00 00]
    // 0xFE int:updateTick short:src(nUPos) int:itemId short:dst short:?

    @PacketHandler(opcode = RecvPacketOpcode.USE_CASH_ITEM)
    public static void handle(MapleClient c, LittleEndianAccessor lea){
        MapleCharacter chr = c.getPlayer();
        if(chr == null) {
            return;
        }
        lea.skip(4); //update tick
        short src = lea.readShort();
        int itemId = lea.readInt();

        if(GameConstants.getCashCubeByItemId(itemId) != null) {
            short dst = lea.readShort();
            MapleInventoryType mit = dst < 0 ? MapleInventoryType.EQUIPPED : MapleInventoryType.EQUIP;

            Item item = chr.getInventory(MapleInventoryType.CASH).getItem(src);
            if (item == null) {
                item = chr.getInventory(MapleInventoryType.USE).getItem(src); //just in case
            }
            Equip equip;
            equip = (Equip) chr.getInventory(mit).getItem(dst);
            if (item == null || equip == null) {
                c.getSession().write(CWvsContext.InventoryPacket.getInventoryFull());
                c.getSession().write(CWvsContext.enableActions());
                return;
            }

            int oldState = equip.getState();
            equip.renewPotential(GameConstants.getCashCubeByItemId(itemId));
            equip.revealHiddenPotential();
            boolean hasRankedUp = oldState != equip.getState();

            // Update
            chr.getInventory(GameConstants.getInventoryType(item.getItemId())).removeItem(item.getPosition(), (short) 1, false);

            chr.getMap().broadcastMessage(chr, CField.getScrollEffect(c.getPlayer().getId(), Equip.ScrollResult.SUCCESS, false, equip.getItemId(), item.getItemId()), false);
            c.getSession().write(CField.enchantResult(1)); //success
            if (dst < 0) {
                chr.equipChanged();
            }
            chr.forceReAddItem(equip, mit);
            c.getSession().write(CWvsContext.enableActions());
            c.getSession().write(CField.showPotentialReset(chr.getId(), true, itemId));
            c.getSession().write(CWvsContext.sendRedCubeRequest(chr.getId(), hasRankedUp, itemId, dst, equip));
        }else{
            chr.dropMessage(5, "You have used a cash item currently unknown by the server.");
            c.getSession().write(CWvsContext.enableActions());
        }
    }
}
