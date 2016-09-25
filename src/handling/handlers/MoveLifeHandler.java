package handling.handlers;

import java.awt.Point;
import java.util.List;

import client.MapleClient;
import constants.GameConstants;
import handling.PacketHandler;
import handling.RecvPacketOpcode;
import handling.channel.handler.MovementParse;
import server.life.MapleMonster;
import server.life.MobAttackInfo;
import server.life.MobSkill;
import server.life.MobSkillFactory;
import server.maps.MapleMap;
import server.maps.MapleMapObjectType;
import server.movement.LifeMovementFragment;
import tools.Pair;
import tools.Randomizer;
import tools.data.LittleEndianAccessor;
import tools.packet.MobPacket;

public class MoveLifeHandler {

	@PacketHandler(opcode = RecvPacketOpcode.MOVE_LIFE)
	public static void handle(MapleClient c, LittleEndianAccessor lea) {
		
		int objectId = lea.readInt();
        MapleMonster monster = c.getPlayer().getMap().getMonsterByOid(objectId);
        if (monster == null || monster.getType() != MapleMapObjectType.MONSTER) {
            return;
        }
        lea.skip(1); // ?
        short moveId = lea.readShort();
        
        byte pNibbles = lea.readByte();
		byte rawActivity = lea.readByte();
		int useSkillId = lea.readByte();
		int useSkillLevel = lea.readByte();
           
		if (rawActivity >= 0) {
			rawActivity = (byte) (rawActivity & 0xFF >> 1);
		}

		boolean isAttack = inRangeInclusive(rawActivity, 12, 20);
		boolean isSkill = inRangeInclusive(rawActivity, 21, 25);

		byte attackId = (byte) (isAttack ? rawActivity - 12 : -1);
		boolean nextMovementCouldBeSkill = (pNibbles & 0x0F) != 0;

		
		MobSkill toUse = null;
		int percHpLeft = (int) ((monster.getHp() / monster.getMobMaxHp()) * 100);

		if (nextMovementCouldBeSkill) {
			int Random = Randomizer.nextInt(monster.getNoSkills());
			Pair<Integer, Integer> skillToUse = monster.getSkills().get(Random);
			useSkillId = skillToUse.getLeft();
			useSkillLevel = skillToUse.getRight();
			toUse = MobSkillFactory.getMobSkill(useSkillId, useSkillLevel);
			
			if (isSkill || isAttack) {
				if (useSkillId != toUse.getSkillId() || useSkillLevel != toUse.getSkillLevel()) {
					useSkillId = 0;
					useSkillLevel = 0;
					return;
				} else if (toUse.getHP() < percHpLeft) {
					toUse = null;
				} else {
					toUse.applyEffect(c.getPlayer(), monster, true);
				}
			} else {
				MobAttackInfo mobAttack = monster.getStats().getMobAttack(attackId);
			}
		}
		
		lea.readByte();
        // for: short, short
        
        lea.readByte();
        // for: short
        
        // ?
        lea.readByte();
        
        // ?
        lea.readInt();
        
        // ?
        lea.readInt();
        lea.readInt();
        
        // ?
        lea.skip(5);
        
        lea.readInt(); // tEncodedGatherDuration
        short x = lea.readShort();
        short y = lea.readShort();
        short vx = lea.readShort();
        short vy = lea.readShort();

        Point startPos = monster.getPosition();
        List<LifeMovementFragment> res = MovementParse.parseMovement(lea, 2);

        if (monster != null && c != null) {
            c.getSession().write(MobPacket.moveMonsterResponse(objectId, moveId, monster.getMp(), monster.isControllerHasAggro(), useSkillId, useSkillLevel, attackId));
        }
        if (res != null) {
            final MapleMap map = c.getPlayer().getMap();
            MovementParse.updatePosition(res, monster, -1);
            map.moveMonster(monster, monster.getPosition());
            map.broadcastMessage(c.getPlayer(), MobPacket.moveMonster(pNibbles, rawActivity, useSkillId, useSkillLevel, startPos, res), monster.getPosition());
        }
    }
	
	public static boolean inRangeInclusive(Byte pVal, Integer pMin, Integer pMax) {
		return !(pVal < pMin) || (pVal > pMax);
	}
}
