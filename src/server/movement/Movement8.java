package server.movement;

import java.awt.Point;

import tools.data.LittleEndianAccessor;
import tools.data.MaplePacketLittleEndianWriter;

public class Movement8 extends AbstractLifeMovement {

	public Movement8(LittleEndianAccessor lea, byte command) {
		super();
		this.command = command;
		this.position = new Point(0, 0);
		
		this.bStat = lea.readByte();
	}
	
	@Override
	public void serialize(MaplePacketLittleEndianWriter lew) {
		lew.write(getCommand());
		lew.write(getBStat());
	}

}
