package constants;

/**
*
* @author SharpAceX (Alan)
*/

public class MapConstants {

	public static boolean isFmMap(int map){
		return map >= 910000001 && map < 910000022;
	}

	public static boolean isAzwanMap(int mapId) {
		return mapId >= 262020000 && mapId < 262023000;
	}

	public static boolean isHyperTeleMap(int mapId) {
		for (int i : GameConstants.hyperTele) {
			if (i == mapId) {
				return true;
			}
		}
		return false;
	}

	public static boolean isFishingMap(int mapid) {
		return mapid == 749050500 || mapid == 749050501 || mapid == 749050502 || mapid == 970020000 || mapid == 970020005;
	}

	public static boolean isStartingEventMap(final int mapid) {
		switch (mapid) {
		case 109010000:
		case 109020001:
		case 109030001:
		case 109030101:
		case 109030201:
		case 109030301:
		case 109030401:
		case 109040000:
		case 109060001:
		case 109060002:
		case 109060003:
		case 109060004:
		case 109060005:
		case 109060006:
		case 109080000:
		case 109080001:
		case 109080002:
		case 109080003:
			return true;
		}
		return false;
	}

	public static boolean isEventMap(final int mapid) {
		return (mapid >= 109010000 && mapid < 109050000) || (mapid > 109050001 && mapid < 109090000) || (mapid >= 809040000 && mapid <= 809040100);
	}

	public static boolean isCoconutMap(final int mapid) {
		return mapid == 109080000 || mapid == 109080001 || mapid == 109080002 || mapid == 109080003 || mapid == 109080010 || mapid == 109080011 || mapid == 109080012 || mapid == 109090300 || mapid == 109090301 || mapid == 109090302 || mapid == 109090303 || mapid == 109090304 || mapid == 910040100;
	}

	public static boolean isTeamMap(final int mapid) {
		return mapid == 109080000 || mapid == 109080001 || mapid == 109080002 || mapid == 109080003 || mapid == 109080010 || mapid == 109080011 || mapid == 109080012 || mapid == 109090300 || mapid == 109090301 || mapid == 109090302 || mapid == 109090303 || mapid == 109090304 || mapid == 910040100 || mapid == 960020100 || mapid == 960020101 || mapid == 960020102 || mapid == 960020103 || mapid == 960030100 || mapid == 689000000 || mapid == 689000010;
	}

	public static boolean isAnyDropMap(int mapId) {
		switch (mapId) {
		case 180000000:
		case 180000001:
			return true;
		}
		return false;
	}

	public static boolean isNoExpireMap(int mapId) {
		switch (mapId) {
		case 180000000:
		case 180000001:
			return true;
		}
		return false;
	}

	public static boolean isTutorialMap(int mapid) {
		if (mapid < 100000000) { //Explorer & Cannoneer
			return true;
		} else if (mapid / 100 == 1030509) { //Dual Blade
			return true;
		} else if (mapid / 10000 == 13003) { //Cygnus
			return true;
		} else if (mapid / 100000 == 9000) { //Evan
			return true;
		} else if (mapid / 10000 == 91015) { //Mercedes
			return true;
		} else if (mapid / 10000 == 91307) { //Mihile
			return true;
		} else if (mapid / 10000 == 91400) { //Aran
			return true;
		} else if (mapid / 10000 == 91500) { //Phantom
			return true;
		} else if (mapid / 10000 == 93100) { //Resistance
			return true;
		} else if (mapid / 10000 == 93105) { //Demon Slayer
			return true;
		}
		return false;
		//There might be included other maps like main town or job advancements,
		//But we don't care since you don't get much exp here and you're locked on teasers.
	}

}
