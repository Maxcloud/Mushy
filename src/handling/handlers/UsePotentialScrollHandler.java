package handling.handlers;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.ItemFlag;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import handling.PacketHandler;
import handling.RecvPacketOpcode;
import server.MapleItemInformationProvider;
import tools.data.LittleEndianAccessor;
import tools.packet.CField;
import tools.packet.CWvsContext;

import java.util.Map;

public class UsePotentialScrollHandler {

    @PacketHandler(opcode = RecvPacketOpcode.USE_POTENTIAL_SCROLL)
    public static void handle(MapleClient c, LittleEndianAccessor lea){
        MapleCharacter chr = c.getPlayer();
        if(chr == null){
            return;
        }
        lea.skip(4); //update tick
        short scrollSlot = lea.readShort();
        short equipSlot = lea.readShort();
        // the rest is whatever
        Item scroll = chr.getInventory(MapleInventoryType.USE).getItem(scrollSlot);
        if (scroll == null) {
            scroll = chr.getInventory(MapleInventoryType.CASH).getItem(scrollSlot);
        }
        Equip equip;
        if (equipSlot < 0) {
            equip = (Equip) chr.getInventory(MapleInventoryType.EQUIPPED).getItem(equipSlot);
        } else{
            equip = (Equip) chr.getInventory(MapleInventoryType.EQUIP).getItem(equipSlot);
        }
        if(scroll == null || equip == null){
            c.getSession().write(CWvsContext.InventoryPacket.getInventoryFull());
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        Equip.ScrollResult scrollSuccess;
        int oldState = equip.getState();
        if(oldState == Equip.HIDDEN){
            chr.dropMessage(5, "Please unveil the potential before using a potential scroll on this item.");
            c.getSession().write(CWvsContext.InventoryPacket.getInventoryFull());
            c.getSession().write(CWvsContext.enableActions());
            return;
        }else{
            Map<String, Integer> scrollInfo = MapleItemInformationProvider.getInstance().getEquipStats(scroll.getItemId());
            final boolean cursed = !scrollInfo.containsKey("cursed") ? false : scrollInfo.get("cursed") == 1;
            if(equip.usePotentialScroll(scroll.getItemId())){ //check for boom
                if(!cursed || (cursed && ItemFlag.SHIELD_WARD.check(equip.getFlag()))){
                    equip.setFlag((short) (equip.getFlag() - ItemFlag.SHIELD_WARD.getValue()));
                    scrollSuccess = Equip.ScrollResult.FAIL;
                }else {
                    scrollSuccess = Equip.ScrollResult.CURSE;
                }
            }else{
                scrollSuccess = Equip.ScrollResult.SUCCESS;
            }

            // Update
            chr.getInventory(GameConstants.getInventoryType(scroll.getItemId())).removeItem(scroll.getPosition(), (short) 1, false);

            if (scrollSuccess == Equip.ScrollResult.CURSE) {
                c.getSession().write(CWvsContext.InventoryPacket.scrolledItem(scroll, MapleInventoryType.EQUIP, equip, true, false, false));
                if (equipSlot < 0) {
                    chr.getInventory(MapleInventoryType.EQUIPPED).removeItem(equip.getPosition());
                } else {
                    chr.getInventory(MapleInventoryType.EQUIP).removeItem(equip.getPosition());
                }
            }

            c.getSession().write(CField.enchantResult(scrollSuccess == Equip.ScrollResult.SUCCESS ? 1 : scrollSuccess == Equip.ScrollResult.CURSE ? 2 : 0));
            chr.getMap().broadcastMessage(CField.showPotentialReset(c.getPlayer().getId(), true, equip.getItemId()));
            chr.forceReAddItem_NoUpdate(equip, MapleInventoryType.EQUIP);
            chr.getMap().broadcastMessage(chr, CField.getScrollEffect(c.getPlayer().getId(), scrollSuccess, false, scroll.getItemId(), scroll.getItemId()), false);

            if (equipSlot < 0 && (scrollSuccess == Equip.ScrollResult.SUCCESS || scrollSuccess == Equip.ScrollResult.CURSE)) {
                chr.equipChanged();
            }

        }
        c.getSession().write(CWvsContext.enableActions());
    }
}
