package handling.handlers;

import client.MapleCharacter;
import client.MapleClient;
import constants.ServerConstants.CommandType;
import handling.PacketHandler;
import handling.RecvPacketOpcode;
import server.commands.CommandProcessor;
import tools.data.LittleEndianAccessor;
import tools.packet.CField;
import tools.packet.CWvsContext;

public class GeneralChatHandler {
	
	@PacketHandler(opcode = RecvPacketOpcode.GENERAL_CHAT)
	public static void handle(MapleClient c, LittleEndianAccessor slea){
		if (c.getPlayer() != null && c.getPlayer().getMap() != null) {
			slea.skip(4); // update tick
            String text = slea.readMapleAsciiString();
            byte unk = slea.readByte();
            
            if (text.length() > 0 && c.getPlayer() != null && c.getPlayer().getMap() != null && !CommandProcessor.processCommand(c, text, CommandType.NORMAL)) {
                if (!c.getPlayer().isIntern() && text.length() >= 80) {
                    return;
                }
                if (c.getPlayer().getCanTalk() || c.getPlayer().isStaff()) {
                	
                    //Note: This patch is needed to prevent chat packet from being broadcast to people who might be packet sniffing.
                    if (c.getPlayer().isHidden()) {
                        if (c.getPlayer().isIntern() && !c.getPlayer().isSuperGM() && unk == 0) {
                        	c.getPlayer().getMap().broadcastGMMessage(c.getPlayer(), CField.getChatText(c.getPlayer().getId(), text, c.getPlayer().isSuperGM(), (byte) 1), true);
                            if (unk == 0) {
                            	c.getPlayer().getMap().broadcastGMMessage(c.getPlayer(), CWvsContext.broadcastMsg(2, c.getPlayer().getName() + " : " + text), true);
                            }
                        } else {
                        	c.getPlayer().getMap().broadcastGMMessage(c.getPlayer(), CField.getChatText(c.getPlayer().getId(), text, c.getPlayer().isSuperGM(), unk), true);
                        }
                    } else {
                    	
                        if (c.getPlayer().isIntern() && !c.getPlayer().isSuperGM() && unk == 0) {
                            c.getPlayer().getMap().broadcastMessage(CField.getChatText(c.getPlayer().getId(), text, false, unk));
                        	c.getPlayer().getMap().broadcastMessage(CField.getChatText(c.getPlayer().getId(), text, c.getPlayer().isSuperGM(), 1));
                        } else {
                        	c.getPlayer().getMap().broadcastMessage(CField.getChatText(c.getPlayer().getId(), text, false, unk));
                        	c.getPlayer().getMap().broadcastMessage(CField.getChatText(c.getPlayer().getId(), text, c.getPlayer().isSuperGM(), 1));//was1
                        }
                    }
                } else {
                    c.getSession().write(CWvsContext.broadcastMsg(6, "You have been muted and are therefore unable to talk."));
                }
            }
        }
	}

}
