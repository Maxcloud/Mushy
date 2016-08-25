package handling.handlers;

import client.MapleClient;
import handling.PacketHandler;
import handling.RecvPacketOpcode;
import handling.channel.handler.PlayerHandler;
import tools.data.LittleEndianAccessor;

public class RangedAttackHandler {

	@PacketHandler(opcode = RecvPacketOpcode.RANGED_ATTACK)
	public static void handle(MapleClient c, LittleEndianAccessor lea) {
		PlayerHandler.rangedAttack(lea, c, c.getPlayer());
	}
}
