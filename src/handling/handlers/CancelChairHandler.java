package handling.handlers;

import client.MapleClient;
import handling.PacketHandler;
import handling.RecvPacketOpcode;
import tools.data.LittleEndianAccessor;
import tools.packet.CField;

public class CancelChairHandler {

	@PacketHandler(opcode = RecvPacketOpcode.CANCEL_CHAIR)
	public static void handle(MapleClient c, LittleEndianAccessor lea) {
		short id = lea.readShort();

		if (id == -1) {
			c.getPlayer().cancelFishingTask();
			c.getPlayer().setChair(0);
			c.getSession().write(CField.cancelChair(-1));
			if (c.getPlayer().getMap() != null) {
				c.getPlayer().getMap().broadcastMessage(c.getPlayer(), CField.showChair(c.getPlayer().getId(), 0), false);
			}
		} else {
			c.getPlayer().setChair(id);
			c.getSession().write(CField.cancelChair(id));
		}
	}
}
