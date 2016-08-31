package handling.handlers;

import client.MapleCharacter;
import client.MapleClient;
import handling.PacketHandler;
import handling.RecvPacketOpcode;
import handling.channel.ChannelServer;
import handling.world.World;
import server.maps.FieldLimitType;
import server.maps.MapleMap;
import tools.data.LittleEndianAccessor;
import tools.packet.CWvsContext;

/**
 * Created by Tim on 9/1/2016.
 */
public class ChangeChannelHandler {

    @PacketHandler(opcode = RecvPacketOpcode.CHANGE_CHANNEL)
    public static void handle(MapleClient c, LittleEndianAccessor lea){
        MapleCharacter chr = c.getPlayer();
        boolean isInFmRoom = chr.isInFmRoom();
        if (chr == null || chr.hasBlockedInventory() || chr.getEventInstance() != null || chr.getMap() == null
                || chr.isInBlockedMap() || FieldLimitType.ChannelSwitch.check(chr.getMap().getFieldLimit())) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        if (World.getPendingCharacterSize() >= 10) {
            chr.dropMessage(1, "The server is busy at the moment. Please try again in less than a minute.");
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        final int toChannel = lea.readByte() + 1;
        int mapid = 0;
        if (isInFmRoom) {
            mapid = lea.readInt();
        }
        lea.skip(4); // update tick
        if (!World.isChannelAvailable(toChannel, chr.getWorld())) {
            chr.dropMessage(1, "Request denied due to an unknown error.");
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        // this stuff is for FM map changing, which SHOULD only be called with 0x13C.
        // so it's just here in case some magic happens.
        if (isInFmRoom && (mapid < 910000001 || mapid > 910000022)) {
            // yes I know, it's only been like 20 lines since this was checked
            chr.dropMessage(1, "Request denied due to an unknown error.");
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        if (isInFmRoom) {
            if (chr.getMapId() == mapid) {
                if (c.getChannel() == toChannel) {
                    chr.dropMessage(1, "You are already in " + chr.getMap().getMapName());
                    c.getSession().write(CWvsContext.enableActions());
                } else { // diff channel
                    chr.changeChannel(toChannel);
                }
            } else { // diff map
                if (c.getChannel() != toChannel) {
                    chr.changeChannel(toChannel);
                }
                final MapleMap warpMap = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(mapid);
                if (warpMap != null) {
                    chr.changeMap(warpMap, warpMap.getPortal("out00"));
                } else {
                    chr.dropMessage(1, "Request denied due to an unknown error.");
                    c.getSession().write(CWvsContext.enableActions());
                }
            }
        } else {
            chr.changeChannel(toChannel);
        }

    }
}
