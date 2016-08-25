package handling.handlers;

import client.MapleClient;
import handling.PacketHandler;
import handling.RecvPacketOpcode;
import handling.channel.handler.NPCHandler;
import tools.data.LittleEndianAccessor;

public class QuestActionHandler {

	@PacketHandler(opcode = RecvPacketOpcode.QUEST_ACTION)
	public static void handle(MapleClient c, LittleEndianAccessor lea) {
		NPCHandler.QuestAction(lea, c, c.getPlayer());
	}
}
