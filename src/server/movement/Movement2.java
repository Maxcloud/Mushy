package server.movement;

import java.awt.Point;

import tools.data.LittleEndianAccessor;
import tools.data.MaplePacketLittleEndianWriter;

public class Movement2 extends AbstractLifeMovement {
	
	public Movement2(LittleEndianAccessor lea, byte command) {
		super();
		this.command = command;
		
		short x = lea.readShort();
		short y = lea.readShort();
		position = new Point(x, y);
		
		short xv = lea.readShort();
		short xy = lea.readShort();
		vposition = new Point(xv, xy);
		
		fh = lea.readShort();
		
		bMoveAction = lea.readByte();
		tElapse = lea.readShort();
		bForcedStop = lea.readByte();
	}

	@Override
	public void serialize(MaplePacketLittleEndianWriter lew) {
		lew.write(getCommand());
		lew.writePos(getPosition());
		lew.writePos(getVPosition());
		lew.writeShort(getFh());
		lew.write(getMoveAction());
		lew.writeShort(getDuration());
		lew.write(getForcedStop());
	}

}
