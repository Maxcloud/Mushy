package handling.handlers.login;

import java.util.List;

import client.MapleCharacter;
import client.MapleClient;
import constants.WorldConstants.WorldOption;
import handling.PacketHandler;
import handling.RecvPacketOpcode;
import handling.channel.ChannelServer;
import handling.world.World;
import tools.data.LittleEndianAccessor;
import tools.packet.LoginPacket;

public class CharacterListRequest {

	@PacketHandler(opcode = RecvPacketOpcode.CHARLIST_REQUEST)
	public static void handle(MapleClient c, LittleEndianAccessor slea) {
		if (!c.isLoggedIn()) {
            c.getSession().close();
            return;
        }
        slea.readByte();
        final int server = slea.readByte();
        final int channel = slea.readByte() + 1;
        if (!World.isChannelAvailable(channel, server) || !WorldOption.isExists(server)) {
            c.getSession().write(LoginPacket.getLoginFailed(10)); // cannot process so many
            return;
        }
        
        final List<MapleCharacter> chars = c.loadCharacters(server);
        if (chars != null && ChannelServer.getInstance(channel) != null) {
            c.setWorld(server);
            c.setChannel(channel);
            c.getSession().write(LoginPacket.getSecondAuthSuccess(c));
            c.getSession().write(LoginPacket.getAccountName(c.getAccountName()));
            c.getSession().write(LoginPacket.getCharList(c.getSecondPassword(), chars, c.getCharacterSlots()));
            c.getSession().write(LoginPacket.sendAllowedCreation());
        } else {
            c.getSession().close();
        }
	}

}
