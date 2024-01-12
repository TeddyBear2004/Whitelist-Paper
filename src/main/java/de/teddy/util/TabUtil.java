package de.teddy.util;

import java.util.List;

public class TabUtil {
	public static List<String> reduceEntries(String prefix, List<String> list){
		if(prefix == null){
			return list;
		}
		list.removeIf(s -> !s.toLowerCase().startsWith(prefix.toLowerCase()));
		return list;
	}
}
