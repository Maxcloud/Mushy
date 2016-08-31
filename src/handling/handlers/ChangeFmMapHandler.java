package handling.handlers;

import client.MapleCharacter;
import client.MapleClient;
import handling.PacketHandler;
import handling.RecvPacketOpcode;
import handling.channel.ChannelServer;
import server.maps.FieldLimitType;
import server.maps.MapleMap;
import tools.data.LittleEndianAccessor;
import tools.packet.CWvsContext;

/**
 * Created by Tim on 9/1/2016.
 */
public class ChangeFmMapHandler {

    @PacketHandler(opcode = RecvPacketOpcode.CHANGE_FM_MAP)
    public static void handle(MapleClient c, LittleEndianAccessor lea){
        MapleCharacter chr = c.getPlayer();
        if (chr == null || chr.hasBlockedInventory() || chr.getEventInstance() != null || chr.getMap() == null
                || chr.isInBlockedMap() || FieldLimitType.ChannelSwitch.check(chr.getMap().getFieldLimit())) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        }

        byte toChannel = (byte) (lea.readByte() + 1); // nChannelID
        int toMap = lea.readInt(); // dwFieldID
//        int localTime = lea.readInt(); // update tick, uncomment if you see a nice use for it
        if(!chr.isInFmRoom() || toMap < 910000001 || toMap > 910000022) {
            //somehow got outside of the map range
            c.getPlayer().dropMessage(1, "Request denied due to trying to teleport to an unknown map.");
            c.getSession().write(CWvsContext.enableActions());
        }else{
            if (chr.getMapId() == toMap) {
                if (c.getChannel() == toChannel) {
                    chr.dropMessage(1, "You are already in " + chr.getMap().getMapName() + ".");
                    c.getSession().write(CWvsContext.enableActions());
                } else { // diff channel
                    chr.changeChannel(toChannel);
                }
            } else { // diff map
                if (c.getChannel() != toChannel) {
                    chr.changeChannel(toChannel);
                }
                final MapleMap warpMap = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(toMap);
                if (warpMap != null) {
                    chr.changeMap(warpMap, warpMap.getPortal("out00"));
                } else {
                    chr.dropMessage(1, "Request denied due to an unknown error.");
                    c.getSession().write(CWvsContext.enableActions());
                }
            }
        }
    }
}
