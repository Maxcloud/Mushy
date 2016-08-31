package handling.handlers;

import java.awt.Point;

import client.MapleClient;
import handling.PacketHandler;
import handling.RecvPacketOpcode;
import server.MaplePortal;
import tools.data.LittleEndianAccessor;

public class UseInnerPortalHandler {

	@PacketHandler(opcode = RecvPacketOpcode.USE_INNER_PORTAL)
	public static void handle(MapleClient c, LittleEndianAccessor lea) {
		lea.skip(1);
		if ((c.getPlayer() == null) || (c.getPlayer().getMap() == null)) {
			return;
		}
		String portalName = lea.readMapleAsciiString();
		MaplePortal portal = c.getPlayer().getMap().getPortal(portalName);
		int toX = lea.readShort();
		int toY = lea.readShort();

		if (portal == null) {
			return;
		}
		if ((portal.getPosition().distanceSq(c.getPlayer().getTruePosition()) > 22500.0D) && (!c.getPlayer().isGM())) {
			return;
		}
		c.getPlayer().getMap().movePlayer(c.getPlayer(), new Point(toX, toY));
		c.getPlayer().checkFollow();
	}
}
