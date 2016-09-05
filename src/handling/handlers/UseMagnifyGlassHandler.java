package handling.handlers;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import handling.PacketHandler;
import handling.RecvPacketOpcode;
import server.MapleItemInformationProvider;
import server.StructItemOption;
import tools.Randomizer;
import tools.data.LittleEndianAccessor;
import tools.packet.CField;
import tools.packet.CWvsContext;

import java.util.LinkedList;
import java.util.List;

public class UseMagnifyGlassHandler {

    @PacketHandler(opcode = RecvPacketOpcode.USE_MAGNIFY_GLASS)
    public static void handle(MapleClient c, LittleEndianAccessor lea){
        MapleCharacter chr = c.getPlayer();
        if(chr == null){
            return;
        }
        lea.skip(4); //update tick
        short src = lea.readShort(); // Assumed 0x7F, inventory glass. Don't think you can get the other ones anymore.
        short dst = lea.readShort();
        MapleInventoryType mit = dst < 0 ? MapleInventoryType.EQUIPPED : MapleInventoryType.EQUIP;
        Item item = chr.getInventory(mit).getItem(dst);
        if (item == null || c.getPlayer().hasBlockedInventory()) {
            c.getSession().write(CWvsContext.InventoryPacket.getInventoryFull());
            return;
        }
        Equip equip = (Equip) item;
        long price = GameConstants.getMagnifyPrice(equip);
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        final int reqLevel = ii.getReqLevel(equip.getItemId()) / 10;
        //TODO checks for price
        if(equip.getState() == 1){
            final List<List<StructItemOption>> pots = new LinkedList<>(ii.getAllPotentialInfo().values());
            int newState = -equip.getPotentialByLine(0);
            if(newState > Equip.LEGENDARY){
                newState = Equip.LEGENDARY;
            }else if(newState < Equip.RARE){
                newState = Equip.RARE;
            }

            while (equip.getState() != newState) {
                //TODO:This is brute forcing, could potentially (haha) last forever.
                //31001 = haste, 31002 = door, 31003 = se, 31004 = hb, 41005 = combat orders, 41006 = advanced blessing, 41007 = speed infusion
                for (int i = 0; i < equip.getPotential().length; i++) { // minimum 2 lines, max 5
                    if(equip.getPotentialByLine(i) == 0){
                        break;
                    }
                    boolean rewarded = false;
                    while (!rewarded) {
                        StructItemOption pot = pots.get(Randomizer.nextInt(pots.size())).get(reqLevel);
                        if (pot != null && pot.reqLevel <= reqLevel && GameConstants.optionTypeFits(pot.optionType, equip.getItemId()) && GameConstants.potentialIDFits(pot.opID, newState, i)) { //optionType
                            //have to research optionType before making this truely official-like
                            if (GameConstants.isAllowedPotentialStat(equip, pot.opID)) {
                                equip.setPotentialByLine(i, pot.opID);
                                rewarded = true;
                            }
                        }
                    }
                }
            }
            c.getPlayer().getMap().broadcastMessage(CField.showPotentialReset(c.getPlayer().getId(), true, equip.getItemId()));
            c.getSession().write(CWvsContext.enableActions());
            c.getPlayer().forceReAddItem(equip, MapleInventoryType.EQUIP);
        }else{ //equipState not 1
            chr.dropMessage(5, "This item has no hidden potential to reveal.");
            c.getSession().write(CWvsContext.enableActions());
        }

    }
}
