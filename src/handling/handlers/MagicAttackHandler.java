package handling.handlers;

import client.MapleClient;
import handling.PacketHandler;
import handling.RecvPacketOpcode;
import handling.channel.handler.PlayerHandler;
import tools.data.LittleEndianAccessor;

public class MagicAttackHandler {

	@PacketHandler(opcode = RecvPacketOpcode.MAGIC_ATTACK)
	public static void handle(MapleClient c, LittleEndianAccessor lea) {
		PlayerHandler.MagicDamage(lea, c, c.getPlayer());
	}
}
