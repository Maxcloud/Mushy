package handling.handlers;

import client.MapleCharacter;
import client.MapleClient;
import constants.MapConstants;
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
		
		if (chr == null || chr.hasBlockedInventory() || chr.getEventInstance() != null || chr.getMap() == null || chr.isInBlockedMap() || FieldLimitType.ChannelSwitch.check(chr.getMap().getFieldLimit())) {
			c.getSession().write(CWvsContext.enableActions());
			return;
		}

		byte toChannel = (byte) (lea.readByte() + 1); // nChannelID
		int toMap = lea.readInt(); // dwFieldID
		int localTime = lea.readInt(); // update tick

		if(c.getChannel() != toChannel || !MapConstants.isFmMap(chr.getMapId()) || !MapConstants.isFmMap(toMap) || chr.getMapId() == toMap) {
			//wrong channel or not in the fm or trying to teleport to a non-fm map or on map already
			//they don't deserve a warning because none of these scenarios are possible without PE
			c.getSession().write(CWvsContext.enableActions());
			return;
		}
		
		//you can warp them now
		MapleMap map = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(toMap);		
		chr.changeMap(map, map.getPortal("out00"));
	}
}
