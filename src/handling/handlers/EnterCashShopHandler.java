package handling.handlers;

import client.MapleClient;
import handling.PacketHandler;
import handling.RecvPacketOpcode;
import handling.channel.handler.InterServerHandler;
import tools.data.LittleEndianAccessor;

public class EnterCashShopHandler {

	@PacketHandler(opcode = RecvPacketOpcode.ENTER_CASH_SHOP)
	public static void handle(MapleClient c , LittleEndianAccessor lea) {
		InterServerHandler.EnterCS(c, c.getPlayer());
	}
}
