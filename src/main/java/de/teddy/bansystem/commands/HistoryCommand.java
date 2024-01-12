package de.teddy.bansystem.commands;

import de.teddy.bansystem.BanSystem;
import de.teddy.bansystem.database.tables.BansystemPunishment;
import de.teddy.util.TimeUtil;
import de.teddy.util.UUIDConverter;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class HistoryCommand implements CommandExecutor, TabCompleter {
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		if(sender.hasPermission("bansystem.history")){
			if(args.length >= 1){
				try{
					UUID uuidByName = UUIDConverter.getUUIDByName(args[0]);
					if(uuidByName != null){
						List<BansystemPunishment> punishments = getPunishments(uuidByName.toString());
						if(punishments.size() > 0){
							BanSystem.sendMessage(sender, "History von " + ChatColor.GOLD + args[0] + ChatColor.GRAY + ":");
							SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
							for(BansystemPunishment punishment : punishments){
								//hellrotTYPE goldDatum Uhrzeit gray- golddauer abMod:grauvon hellblauStaff
								//gelb/wennentbanntDurchGestrichenGrund
								String firstMessage =
										String.format("%s%s %s%s %s- %s%s",
												ChatColor.DARK_RED,
												Objects.equals(punishment.getType(), "b") ? "Ban" : punishment.getType().equals("m") ? "Mute" : "",
												ChatColor.YELLOW,
												sdf.format(new Date(punishment.getStartTime())),
												ChatColor.DARK_GRAY,
												ChatColor.YELLOW,
												TimeUtil.parseMillis(punishment.getDuration()))
												+ ChatColor.GRAY + " von " + ChatColor.AQUA + punishment.getStaff().getUsername();

								String secondMessage =
										ChatColor.GRAY + "(" + ChatColor.YELLOW + (punishment.isActive() ? "" : ChatColor.STRIKETHROUGH.toString()) + punishment.getReason() + ChatColor.RESET + ChatColor.GRAY + ")";

								sender.sendMessage(ChatColor.DARK_GRAY + ">> §7" + firstMessage);
								sender.sendMessage(ChatColor.DARK_GRAY + ">> §7" + secondMessage);
							}
						}else{
							BanSystem.sendMessage(sender, ChatColor.GOLD + args[0] + ChatColor.GRAY + " hat keine Bestrafungen!");
						}
					}else{
						BanSystem.sendMessage(sender, "§c" + args[0] + " wurde nicht gefunden.");
					}
					return true;
				}catch(IOException e){
					BanSystem.sendErrorMessage(sender, "Der Spieler konnte nicht gefunden werden!");
					return true;
				}
			}
		}else{
			BanSystem.sendErrorMessage(sender, "Du hast keine Berechtigung!");
			return true;
		}
		return false;
	}

	public java.util.List<String> onTabComplete(CommandSender sender, Command cmd, String commandLabel, String[] args){
		return null;
	}

	private List<BansystemPunishment> getPunishments(String uuid){
		return BanSystem.INSTANCE.session.createQuery("from BansystemPunishment where player.uuid = :uuid order by startTime", BansystemPunishment.class).setParameter("uuid", uuid).list();
	}
}
