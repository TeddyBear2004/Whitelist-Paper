package de.teddy.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.UUID;

public class UUIDConverter {
	private static final String API_URL = "https://api.mojang.com/users/profiles/minecraft/";

	public static UUID getUUIDByName(String name) throws IOException {
		Player player = Bukkit.getPlayer(name);
		if (player != null) return player.getUniqueId();

		JsonObject jsonObject = fetchJsonObject(API_URL + name);
		if (jsonObject == null) return null;

		String id = jsonObject.get("id").getAsString();
		return UUID.fromString(id.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
	}

	private static JsonObject fetchJsonObject(String url) throws IOException {
		try (InputStreamReader reader = new InputStreamReader(new URL(url).openStream())) {
			return JsonParser.parseReader(reader).getAsJsonObject();
		} catch (ClassCastException e) {
			return null;
		}
	}
}