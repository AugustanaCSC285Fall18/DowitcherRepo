package utils;

public class TimeUtils {
	/**
	 * Converts the number of seconds to a minute and second format. Returns a string in MM:SS
	 * @param seconds
	 * @return String
	 */
	public static String convertSecondsToMinutes(double seconds) {
		int secondsRounded = (int) Math.round(seconds);
		int minuteNumber = secondsRounded % 60;
		int remainingSeconds = secondsRounded - minuteNumber * 60;
		return minuteNumber + ":" + remainingSeconds;
	}
	
	/**
	 * Converts a string in MM:SS format back into number of seconds. Also can take in an integer value as a string and converts it to an int if its not in MM:SS format.
	 * @param time
	 * @return int
	 */
	public static int convertMinutesToSeconds(String time){
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
