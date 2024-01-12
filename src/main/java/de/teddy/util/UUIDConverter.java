package de.teddy.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.UUID;

public class UUIDConverter {
	private static final JsonParser JSON_PARSER = new JsonParser();
	private static final String API_URL = "https://api.mojang.com/users/profiles/minecraft/";

	public static UUID getUUIDByName(String name) throws IOException{
		Player player = Bukkit.getPlayer(name);
		if(player != null)
			return player.getUniqueId();
		String url = "https://api.mojang.com/users/profiles/minecraft/" + name;
		BufferedInputStream bufferedInputStream = new BufferedInputStream(new URL(url).openStream());
		JsonObject jsonObject;
		try{
			jsonObject = (JsonObject)JSON_PARSER.parse(new InputStreamReader(bufferedInputStream));
		}catch(ClassCastException e){
			return null;
		}
		StringBuilder builder = new StringBuilder(jsonObject.get("id").toString());
		builder.replace(0, 1, "");
		builder.replace(builder.length() - 1, builder.length(), "");

		return getUniqueIdFromString(builder.toString());
	}

	private static UUID getUniqueIdFromString(String uuid){
		return UUID.fromString(uuid.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
	}

	public static String uuidToName(UUID uuid) throws IOException{
		String url = "https://api.mojang.com/user/profiles/" + uuid.toString().replace("-", "") + "/names";
		BufferedInputStream bufferedInputStream = new BufferedInputStream(new URL(url).openStream());
		JsonObject jsonObject = (JsonObject)JSON_PARSER.parse(new InputStreamReader(bufferedInputStream));
		return jsonObject.get("name").getAsString();
	}
}
