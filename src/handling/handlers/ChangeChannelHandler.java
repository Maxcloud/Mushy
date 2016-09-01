package handling.handlers;

import client.MapleCharacter;
import client.MapleClient;
import constants.MapConstants;
import handling.PacketHandler;
import handling.RecvPacketOpcode;
import handling.world.World;
import server.maps.FieldLimitType;
import tools.data.LittleEndianAccessor;
import tools.packet.CWvsContext;

/**
 * Created by Tim on 9/1/2016.
 */
public class ChangeChannelHandler {

	@PacketHandler(opcode = RecvPacketOpcode.CHANGE_CHANNEL)
	public static void handle(MapleClient c, LittleEndianAccessor lea){
		MapleCharacter chr = c.getPlayer();
		int toChannel = lea.readByte() + 1;
		if (chr == null || chr.hasBlockedInventory() || chr.getEventInstance() != null || chr.getMap() == null || chr.isInBlockedMap() || FieldLimitType.ChannelSwitch.check(chr.getMap().getFieldLimit()) || MapConstants.isFmMap(chr.getMapId()) || c.getChannel() == toChannel) {
			c.getSession().write(CWvsContext.enableActions());
			return;
		}
		if (World.getPendingCharacterSize() >= 10 || !World.isChannelAvailable(toChannel, chr.getWorld())) {
			chr.dropMessage(1, "We could not change your channel at this moment. Please try again soon.");
			c.getSession().write(CWvsContext.enableActions());
			return;
		}
		int tick = lea.readInt();

		chr.changeChannel(toChannel);
	}
}
