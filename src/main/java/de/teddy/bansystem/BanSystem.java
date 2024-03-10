package de.teddy.bansystem;

import de.teddy.bansystem.commands.*;
import de.teddy.bansystem.events.CancelableEvents;
import de.teddybear2004.library.TeddyLibrary;
import net.kyori.adventure.text.Component;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.hibernate.SessionFactory;

import java.util.Objects;

public final class BanSystem extends JavaPlugin {
	public static Location spawn;

	private LuckPerms api;

	@Override
	public void onEnable() {
		saveDefaultConfig();

		TeddyLibrary plugin = getPlugin(TeddyLibrary.class);
		SessionFactory sessionFactory = plugin.getSessionFactory();

		spawn = getConfig().getLocation("spawn");

		registerCommands(sessionFactory);
		Bukkit.getServer().getPluginManager().registerEvents(new CancelableEvents(sessionFactory, getConfig().getString("gamemode")), this);

		RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
		if (provider != null) {
			api = provider.getProvider();
		}

	}

	private void registerCommands(SessionFactory sessionFactory) {
		registerCommand("ban", new PunishCommand("bansystem.ban", "bansystem.ban.advanced", "b", "gebannt", sessionFactory, api));
		registerCommand("mute", new PunishCommand("bansystem.mute", "bansystem.mute.advanced", "m", "gemutet", sessionFactory, api));
		registerCommand("unban", new UnPunishCommand("b", "entbannt", sessionFactory));
		registerCommand("unmute", new UnPunishCommand("m", "entmutet", sessionFactory));
		registerCommand("kick", new KickCommand(sessionFactory));
		registerCommand("whitelist", new WhitelistCommand(sessionFactory, getConfig().getString("gamemode")));
		registerCommand("history", new HistoryCommand(sessionFactory));
		registerCommand("generateToken", new GenerateTokenCommand(sessionFactory, getConfig().getString("gamemode")));
		registerCommand("gm", new GmCommand());
		registerCommand("survival", new ChangeGamemodeCommand("survival"));
		registerCommand("creative", new ChangeGamemodeCommand("creative"));
		registerCommand("adventure", new ChangeGamemodeCommand("adventure"));
		registerCommand("spectator", new ChangeGamemodeCommand("spectator"));
		registerCommand("spawn", new SpawnCommand());
		registerCommand("listtoken", new ListTokenCommand(sessionFactory));
	}

	private void registerCommand(String commandName, CommandExecutor executor) {
		Objects.requireNonNull(this.getCommand(commandName)).setExecutor(executor);
	}

	public static void sendErrorMessage(CommandSender sender, String message) {
		sendMessage(sender, String.format("§4%s", message));
	}

	public static void sendMessage(CommandSender sender, String message) {
		sender.sendMessage(String.format("§6[§cBanSystem§6] §7%s", message));
	}

	public static void broadcastMessageWithPermission(Component message, Component advancedMessage) {
		Bukkit.getOnlinePlayers()
				.forEach(player -> {
					if (player.hasPermission("bansystem.bansystem.receive.history.advanced")) {
						player.sendMessage(advancedMessage);
					} else if (player.hasPermission("bansystem.bansystem.receive.history")) {
						player.sendMessage(message);
					}
				});
	}
}