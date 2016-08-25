package handling.handlers.login;

import client.MapleClient;
import handling.PacketHandler;
import handling.RecvPacketOpcode;
import tools.data.LittleEndianAccessor;
import tools.packet.LoginPacket;

public class PongHandler {

	@PacketHandler(opcode = RecvPacketOpcode.PONG)
	public static void handle(MapleClient c, LittleEndianAccessor slea) {
		
		if (slea.available() > 0) {
			
			if(slea.readInt() == 28)
				c.getSession().write(LoginPacket.getPing());
			
		}
		// c.pongReceived();
	}
}
