package handling.handlers;

import client.MapleClient;
import client.Skill;
import client.SkillFactory;
import constants.GameConstants;
import handling.PacketHandler;
import handling.RecvPacketOpcode;
import server.quest.MapleQuest;
import tools.data.LittleEndianAccessor;

public class ChangeKeymapHandler {
	
	@PacketHandler(opcode = RecvPacketOpcode.CHANGE_KEYMAP)
	public static void handle(MapleClient c, LittleEndianAccessor lea) {
		if ((lea.available() > 8L) && (c.getPlayer() != null)) {
			lea.skip(4);
			int numChanges = lea.readInt();

			for (int i = 0; i < numChanges; i++) {
				int key = lea.readInt();
				byte type = lea.readByte();
				int action = lea.readInt();
				if ((type == 1) && (action >= 1000)) {
					Skill skil = SkillFactory.getSkill(action);
					if ((skil != null) && (((!skil.isFourthJob()) && (!skil.isBeginnerSkill()) && (skil.isInvisible())
							&& (c.getPlayer().getSkillLevel(skil) <= 0)) || (GameConstants.isLinkedAttackSkill(action))
							|| (action % 10000 < 1000))) {
						continue;
					}
				}
				c.getPlayer().changeKeybinding(key, type, action);
			}
		} else if (c.getPlayer() != null) {
			int type = lea.readInt();
			int data = lea.readInt();
			switch (type) {
			case 1:
				if (data <= 0) {
					c.getPlayer().getQuestRemove(MapleQuest.getInstance(GameConstants.HP_ITEM));
				} else {
					c.getPlayer().getQuestNAdd(MapleQuest.getInstance(GameConstants.HP_ITEM)).setCustomData(String.valueOf(data));
				}
				break;
			case 2:
				if (data <= 0) {
					c.getPlayer().getQuestRemove(MapleQuest.getInstance(GameConstants.MP_ITEM));
				} else {
					c.getPlayer().getQuestNAdd(MapleQuest.getInstance(GameConstants.MP_ITEM)).setCustomData(String.valueOf(data));
				}
				break;
			}
		}
	}

}
