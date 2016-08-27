package tools;

import java.util.Date;
import java.util.SimpleTimeZone;

/**
 * @author Zenn
 */
public class DateUtil {

	private static final int QUEST_UNIXAGE = 27111908;
	public static final long MAX_TIME = 150842304000000000L;
    public static final long ZERO_TIME = 94354848000000000L;
    public static final long PERMANENT = 150841440000000000L;

	public static long getKoreanTimestamp(long realTimestamp) {
		return getTime(realTimestamp);
	}

	public static long getTime(long realTimestamp) {
		if (realTimestamp == -1L) { // 00 80 05 BB 46 E6 17 02, 1/1/2079
			return MAX_TIME;
		}
		if (realTimestamp == -2L) { // 00 40 E0 FD 3B 37 4F 01, 1/1/1900
			return ZERO_TIME;
		}
		if (realTimestamp == -3L) {
			return PERMANENT;
		}
		return realTimestamp * 10000L + 116444592000000000L;
	}

	public static long getFileTimestamp(long timeStampinMillis, boolean roundToMinutes) {
		if (SimpleTimeZone.getDefault().inDaylightTime(new Date())) {
			timeStampinMillis -= 3600000L;
		}
		long time;

		if (roundToMinutes) {
			time = timeStampinMillis / 1000L / 60L * 600000000L;
		} else {
			time = timeStampinMillis * 10000L;
		}
		return time + 116444592000000000L;
	}
	
	public static final int getQuestTimestamp(final long realTimestamp) {
        final int time = (int) (realTimestamp / 1000 / 60); // convert to minutes
        return (int) (time * 0.1396987) + QUEST_UNIXAGE;
    }	
}
