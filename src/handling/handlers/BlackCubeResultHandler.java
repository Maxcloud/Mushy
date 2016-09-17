package handling.handlers;

import client.MapleClient;
import client.inventory.Equip;
import client.inventory.MapleInventoryType;
import handling.PacketHandler;
import handling.RecvPacketOpcode;
import tools.data.LittleEndianAccessor;
import tools.packet.CWvsContext;

public class BlackCubeResultHandler {

    static public int[] pots = new int[3];
    static public Equip equip;
    static public MapleInventoryType mit;

    @PacketHandler(opcode = RecvPacketOpcode.BLACK_CUBE_RESULT)
    public static void handle(MapleClient c, LittleEndianAccessor lea){
        c.getPlayer().setHasBlackCubed(false);
        lea.skip(4); // update tick
        short choice = lea.readShort();
        long uniqueId = lea.readLong();
        boolean choseOld = choice == 7; //old choice = 7, new choice = 6
        if(choseOld){
            equip.setPotential(pots);
        }

        c.getPlayer().forceReAddItem(equip, mit);
        c.getSession().write(CWvsContext.enableActions());

    }
}
