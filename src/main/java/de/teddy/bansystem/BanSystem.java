package de.teddy.bansystem;

import de.teddy.bansystem.commands.*;
import de.teddy.bansystem.events.CreativeInventory;
import de.teddy.bansystem.events.VanishEvent;
import de.teddy.util.HibernateUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.hibernate.Session;

import java.util.Objects;

public final class BanSystem extends JavaPlugin {
	public static BanSystem INSTANCE;
	public final Session session;
	public static Location spawn;

	public BanSystem(){
		INSTANCE = this;
		session = HibernateUtil.getSession();
	}

	@Override
	public void onEnable(){
		saveConfig();
		spawn = getConfig().getLocation("spawn");

		PluginCommand ban = Objects.requireNonNull(this.getCommand("ban"));
		PunishCommand banCommand = new PunishCommand("bansystem.ban", "bansystem.ban.advanced", "b", "gebannt");
		ban.setExecutor(banCommand);
		ban.setTabCompleter(banCommand);

		PluginCommand mute = Objects.requireNonNull(this.getCommand("mute"));
		PunishCommand muteCommand = new PunishCommand("bansystem.mute", "bansystem.mute.advanced", "m", "gemutet");
		mute.setExecutor(muteCommand);
		mute.setTabCompleter(muteCommand);

		PluginCommand unban = Objects.requireNonNull(this.getCommand("unban"));
		UnPunishCommand unbanCommand = new UnPunishCommand("b", "entbannt");
		unban.setExecutor(unbanCommand);
		unban.setTabCompleter(unbanCommand);

		PluginCommand unmute = Objects.requireNonNull(this.getCommand("unmute"));
		UnPunishCommand unmuteCommand = new UnPunishCommand("m", "entmutet");
		unmute.setExecutor(unmuteCommand);
		unmute.setTabCompleter(unmuteCommand);

		PluginCommand kick = Objects.requireNonNull(this.getCommand("kick"));
		kick.setExecutor(new KickCommand());
		kick.setTabCompleter(new KickCommand());

		PluginCommand whitelist = Objects.requireNonNull(this.getCommand("whitelist"));
		whitelist.setExecutor(new WhitelistCommand());
		whitelist.setTabCompleter(new WhitelistCommand());

		PluginCommand history = Objects.requireNonNull(this.getCommand("history"));
		history.setExecutor(new HistoryCommand());
		history.setTabCompleter(new HistoryCommand());

		PluginCommand generateToken = Objects.requireNonNull(this.getCommand("generateToken"));
		generateToken.setExecutor(new GenerateTokenCommand());

		PluginCommand gm = Objects.requireNonNull(this.getCommand("gm"));
		gm.setExecutor(new GmCommand());
		gm.setTabCompleter(new GmCommand());

		PluginCommand survivalCommand = Objects.requireNonNull(this.getCommand("survival"));
		ChangeGamemodeCommand survival = new ChangeGamemodeCommand("survival");
		survivalCommand.setExecutor(survival);
		survivalCommand.setTabCompleter(survival);

		PluginCommand creativeCommand = Objects.requireNonNull(this.getCommand("creative"));
		ChangeGamemodeCommand creative = new ChangeGamemodeCommand("creative");
		creativeCommand.setExecutor(creative);
		creativeCommand.setTabCompleter(creative);

		PluginCommand adventureCommand = Objects.requireNonNull(this.getCommand("adventure"));
		ChangeGamemodeCommand adventure = new ChangeGamemodeCommand("adventure");
		adventureCommand.setExecutor(adventure);
		adventureCommand.setTabCompleter(adventure);

		PluginCommand spectatorCommand = Objects.requireNonNull(this.getCommand("spectator"));
		ChangeGamemodeCommand spectator = new ChangeGamemodeCommand("spectator");
		spectatorCommand.setExecutor(spectator);
		spectatorCommand.setTabCompleter(spectator);

		PluginCommand spawn = Objects.requireNonNull(this.getCommand("spawn"));
		spawn.setExecutor(new SpawnCommand());
		spawn.setTabCompleter(new SpawnCommand());

		PluginCommand vanish = Objects.requireNonNull(this.getCommand("vanish"));
		vanish.setExecutor(new VanishCommand());

		getServer().getPluginManager().registerEvents(new CreativeInventory(), this);
		getServer().getPluginManager().registerEvents(new VanishEvent(), this);
	}

	@Override
	public void onDisable(){
		HibernateUtil.shutdown();
	}

	public static void sendErrorMessage(CommandSender sender, String message){
		sendMessage(sender, String.format("§4%s", message));
	}

	public static void sendMessage(CommandSender sender, String message){
		sender.sendMessage(String.format("§6[§cBanSystem§6] §7%s", message));
	}

	public static void broadcastMessageWithPermission(String message, String advancedMessage){
		Bukkit.getOnlinePlayers()
				.forEach(player -> {
					if(player.hasPermission("bansystem.bansystem.receive.history.advanced")){
						BanSystem.sendMessage(player, advancedMessage);
					}else if(player.hasPermission("bansystem.bansystem.receive.history")){
						BanSystem.sendMessage(player, message);
					}
				});
	}
}
