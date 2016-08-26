package handling.handlers.login;

import client.MapleClient;
import constants.WorldConstants.WorldOption;
import handling.PacketHandler;
import handling.RecvPacketOpcode;
import handling.channel.ChannelServer;
import handling.login.LoginServer;
import tools.data.LittleEndianAccessor;
import tools.packet.CField;
import tools.packet.LoginPacket;

public class CreateWithoutSecondPassword {
	
	// @PacketHandler(opcode = RecvPacketOpcode.CHAR_SELECT_NO_PIC)
	public static void handle(MapleClient c, LittleEndianAccessor slea) {
		final int charId = slea.readInt();
		slea.readByte(); // 1? 
        slea.readByte(); // 1?
		
        final String currentpw = c.getSecondPassword();
        
        if (!c.isLoggedIn() || (currentpw != null && (!currentpw.equals(""))) || !c.login_Auth(charId) || ChannelServer.getInstance(c.getChannel()) == null || !WorldOption.isExists(c.getWorld())) {
            System.out.println("The session wants to close");
        	c.getSession().close();
            return;
        }
        c.updateMacs(slea.readMapleAsciiString());
        if (slea.available() != 0) {
            final String setpassword = slea.readMapleAsciiString();

            if (setpassword.length() >= 6 && setpassword.length() <= 16) {
                c.setSecondPassword(setpassword);
                c.updateSecondPassword();
            } else {
                c.getSession().write(LoginPacket.secondPwError((byte) 0x14));
                return;
            }
        } 
        if (c.getIdleTask() != null) {
            c.getIdleTask().cancel(true);
        }
        final String s = c.getSessionIPAddress();
        LoginServer.putLoginAuth(charId, s.substring(s.indexOf('/') + 1, s.length()), c.getTempIP(), c.getChannel());
        c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION, s);
        c.getSession().write(CField.getServerIP(c, Integer.parseInt(ChannelServer.getInstance(c.getChannel()).getIP().split(":")[1]), charId));
    
	}

}
