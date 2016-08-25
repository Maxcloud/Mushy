package tools.packet;

import handling.SendPacketOpcode;
import tools.data.MaplePacketLittleEndianWriter;

public class FishingPool {

	/**
	 * <0> close the gui.
	 * <1> open the gui.
	 * <2> show an explanation mark above the user's head.
	 * <3> show a <gotcha> message above the user's head.
	 * <4> show a <got away...> message above the user's head.
	 * <5> does nothing?
	 * @param mode
	 * @return
	 */
	public static byte[] getFishingInformation(int mode) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.FISHING_INFO.getValue());
		mplew.writeInt(mode);
		switch(mode) {
			case 2:
				mplew.writeDouble(50); // distance
				mplew.write(1); // state (1 draw, 2 update)?
				mplew.writeDouble(10); // fDamageMin
				mplew.writeDouble(10); // fDamageMax
				mplew.writeInt(0); // GaugeMove
				
				// FishTemplate::Decode
				mplew.writeDouble(100); // distance max
				mplew.writeInt(0); // move count
				
				// mplew.writeInt(0); // ?
				// mplew.writeInt(0); // ?
				// mplew.writeInt(0); // ?
				// mplew.writeDouble(10.0);
				// mplew.writeDouble(50.0);
				break;
			default: break;
		}
		return mplew.getPacket();
	}
	
	/**
	 * Show the item that was caught, above the player's head.
	 * @param cid
	 * @param itemid 
	 * @return
	 */
	public static byte[] getFishingReward(int cid, int itemid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.FISHING_REWARD.getValue());
		mplew.writeInt(cid);
		mplew.writeInt(itemid);
		return mplew.getPacket();
	}
	
	/**
	 * Sends an error <message> to user.
	 * <message> You can't do that. Someone is already 
	 * fishing there. Max: <code>amount</code> people.
	 * @param amount
	 * @return
	 */
	public static byte[] getFishingZoneInfo(int amount) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendPacketOpcode.FISHING_ZONE_INFO.getValue());
		mplew.writeInt(amount);
		return mplew.getPacket();
	}

}
