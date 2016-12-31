package tools.packet.twostate;

import client.MapleBuffStat;
import client.MapleCharacter;
import tools.data.PacketWriter;

public class EnergyCharged extends TemporaryStat {

	private final MapleBuffStat nBuff = MapleBuffStat.EnergyCharged;
	
	@Override
	public void encode(PacketWriter pw, MapleCharacter chr) {
		
		int nValue = 0;
		int nReason = 0;
		
		if (chr.getBuffedValue(nBuff) != null)
			nValue = chr.getBuffedValue(nBuff);
		
		if (chr.getBuffSource(nBuff) > -1)
			nReason = chr.getBuffSource(nBuff);
		
		encode(pw, chr, nValue, nReason);
		time(pw, true, Integer.MAX_VALUE);
	}
	
}
