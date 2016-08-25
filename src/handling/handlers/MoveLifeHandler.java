package handling.handlers;

import client.MapleClient;
import handling.PacketHandler;
import handling.RecvPacketOpcode;
import handling.channel.handler.MobHandler;
import tools.data.LittleEndianAccessor;

public class MoveLifeHandler {

	@PacketHandler(opcode = RecvPacketOpcode.MOVE_LIFE)
	public static void handle(MapleClient c, LittleEndianAccessor lea) {
		MobHandler.MoveMonster(lea, c, c.getPlayer());
	}
}
