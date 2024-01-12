package de.teddy.bansystem.commands;

import de.teddy.bansystem.BanSystem;
import de.teddy.util.HibernateUtil;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class KickCommand implements CommandExecutor, TabCompleter {
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args){
		if(sender.hasPermission("bansystem.kick")){
			if(args.length >= 1){
				Player player = Bukkit.getPlayer(args[0]);
				if(player != null){
					StringBuilder reason = new StringBuilder();
					if(args.length >= 2)
						for(int i = args[1].equals("-r") ? 2 : 1; i < args.length; i++)
							reason.append(args[i]).append(" ");

					player.kick(Component.text(
							ChatColor.DARK_RED + "Du wurdest gekickt!"
									+ (reason.isEmpty() ? ""
									: "\n\n" + ChatColor.RED + "Grund: " + ChatColor.GRAY + reason)));

					BanSystem.broadcastMessageWithPermission(
							ChatColor.RED +
									"Der Spieler " + ChatColor.GRAY + player.getName() + ChatColor.RED + " wurde gekickt!\n" + ChatColor.RED + "Grund: " + ChatColor.GRAY + reason,
							ChatColor.RED + "Der Spieler " + ChatColor.GRAY + player.getName() + ChatColor.RED + " wurde von " + ChatColor.GRAY + sender.getName() + " gekickt!\n" + ChatColor.RED + "Grund: " + ChatColor.GRAY + reason);

				}else{
					BanSystem.sendErrorMessage(sender, String.format("Der Spieler %s wurde nicht gefunden.", args[0]));
				}
			}
		}

		return true;
	}

	@Nullable
	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args){
		if(args.length == 1){
			List<String> collect = Bukkit.getOnlinePlayers()
					.stream()
					.filter(player -> !player.equals(sender))
					.map(HumanEntity::getName)
					.collect(Collectors.toList());
			collect.remove(sender.getName());
			return collect;
		}
		if(args.length == 2 || (args[1].equals("-r") && args.length == 3))
			return getReasons();
		return Collections.emptyList();
	}

	@NotNull
	private List<String> getReasons(){
		Session session = HibernateUtil.getSession();
		;
		Transaction transaction = session.beginTransaction();
		List<String> resultList = session
				.createQuery("select reason from BansystemReasons WHERE type = 'k'", String.class)
				.setCacheable(true)
				.getResultList();
		transaction.commit();
		session.close();
		return resultList;
	}
}
