package utils;

public class TimeUtils {
	public static String convertSecondsToMinutes(double seconds) {
		int secondsRounded = (int) Math.round(seconds);
		int minuteNumber = secondsRounded % 60;
		int remainingSeconds = secondsRounded - minuteNumber * 60;
		return minuteNumber + ":" + remainingSeconds;
	}

	public static int convertMinutesToSeconds(String time) {
		int semiColonLocation = time.indexOf(":");
		System.out.println(semiColonLocation);
		if (semiColonLocation != -1) {
			int numMinutes = Integer.parseInt(time.substring(0, semiColonLocation));
			int numSecondsRemaining = Integer.parseInt(time.substring(semiColonLocation + 1));
			return numMinutes * 60 + numSecondsRemaining;
		} else {
			return Integer.parseInt(time);
		}
	}
}
