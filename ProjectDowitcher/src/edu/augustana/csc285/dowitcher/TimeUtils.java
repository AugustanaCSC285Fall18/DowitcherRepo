package edu.augustana.csc285.dowitcher;

public class TimeUtils {
	public String convertSecondsToMinutes(double seconds) {
		int secondsRounded = (int) Math.round(seconds);
		int minuteNumber = secondsRounded % 60;
		int remainingSeconds = secondsRounded - minuteNumber * 60;
		return minuteNumber + ":" + remainingSeconds;
	}
	
	public int convertMinutesToSeconds(String minutes) {
		int semiColonLocation=minutes.indexOf(":");
		int numMinutes=Integer.parseInt(minutes.substring(0, semiColonLocation));
		int numSecondsRemaining=Integer.parseInt(minutes.substring(semiColonLocation+1));
		return numMinutes*60+numSecondsRemaining;
	}
}
