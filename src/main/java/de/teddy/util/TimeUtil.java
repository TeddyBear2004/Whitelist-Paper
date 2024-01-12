package de.teddy.util;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtil {
	public static String parseMillis(long millis){
		long cache = millis;
		long days = (millis - (millis % 86_400_000)) / 86_400_000;
		millis -= days * 86400000;
		long hours = (millis - (millis % 3_600_000)) / 3_600_000;
		millis -= hours * 3600000;
		long minutes = (millis - (millis % 60_000)) / 60_000;
		millis -= minutes * 60000;
		long seconds = (millis - (millis % 1_000)) / 1_000;

		StringBuilder stringBuilder = new StringBuilder();

		if(days != 0)
			stringBuilder.append(days).append(" Tage ");
		if(hours != 0)
			stringBuilder.append(hours).append(" Stunden ");
		if(minutes != 0)
			stringBuilder.append(minutes).append(" Minuten ");
		if(seconds != 0 && days == 0)
			stringBuilder.append(seconds).append(" Sekunden ");

		return stringBuilder.toString().trim();
	}

	public static String parseMinSmall(long minutes){
		long days = (minutes - (minutes % 1440)) / 1440;
		minutes -= days * 1440;
		long hours = (minutes - (minutes % 60)) / 60;
		minutes -= hours * 60;
		long minutes2 = minutes;

		StringBuilder stringBuilder = new StringBuilder();

		if(days != 0)
			stringBuilder.append(days).append("d");

		if(hours != 0)
			stringBuilder.append(hours).append("h");

		if(minutes2 != 0)
			stringBuilder.append(minutes2).append("m");

		return stringBuilder.toString();
	}


	public static String parseMinutes(long time){
		long days = (time - (time % 1440)) / 1440;
		time -= days * 1440;
		long hours = (time - (time % 60)) / 60;
		time -= hours * 60;
		long minutes = time;

		StringBuilder stringBuilder = new StringBuilder();

		if(days != 0){
			stringBuilder.append(days).append(" Tage ");
		}

		if(hours != 0)
			stringBuilder.append(hours).append(" Stunden ");

		if(minutes != 0)
			stringBuilder.append(minutes).append(" Minuten ");

		return stringBuilder.toString().trim();
	}

	public static long stringToMinutes(@NotNull String s) throws IllegalArgumentException{
		if(!s.matches("[0-9dhm]+")){
			throw new IllegalArgumentException("String is not a valid time format");
		}
		Pattern pattern = Pattern.compile("(\\d+)\\s*(d|h|m)");
		Matcher matcher = pattern.matcher(s);

		long minutes = 0;
		while(matcher.find()){
			int amount = Integer.parseInt(matcher.group(1));
			String unit = matcher.group(2);
			switch(unit){
				case "d" -> minutes += amount * 1440L;
				case "h" -> minutes += amount * 60L;
				case "m" -> minutes += amount;
			}
		}
		return minutes;
	}

	public static List<String> completeTime(String time){
		if(!time.matches("[0-9dhm]+"))
			throw new IllegalArgumentException("String is not a valid time format");

		if(time.isEmpty()){
			return new ArrayList<>(List.of("p"));
		}

		String[] types = {"d", "h", "m"};
		List<String> list = new ArrayList<>();

		for(String type : types){
			if(!time.contains(type)){
				list.add(time + type);
			}
		}

		return list;
	}
}
