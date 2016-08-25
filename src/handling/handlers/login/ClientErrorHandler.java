package handling.handlers.login;

import client.MapleClient;
import handling.PacketHandler;
import handling.RecvPacketOpcode;
import handling.SendPacketOpcode;
import tools.FileoutputUtil;
import tools.HexTool;
import tools.data.LittleEndianAccessor;

public class ClientErrorHandler {
	
	@PacketHandler(opcode = RecvPacketOpcode.CLIENT_ERROR)
	public static void handle(MapleClient c, LittleEndianAccessor slea) {
		if (slea.available() < 8) {
            System.out.println(slea.toString());
            return;
        }
        short type = slea.readShort();
        String type_str = "Unknown?!";
        if (type == 0x01) {
            type_str = "SendBackupPacket";
        } else if (type == 0x02) {
            type_str = "Crash Report";
        } else if (type == 0x03) {
            type_str = "Exception";
        }
        int errortype = slea.readInt(); // example error 38
        //if (errortype == 0) { // i don't wanna log error code 0 stuffs, (usually some bounceback to login)
        //    return;
        //}
        short data_length = slea.readShort();
        
        slea.skip(4);
        
        short opcode = slea.readShort();
	}

}
