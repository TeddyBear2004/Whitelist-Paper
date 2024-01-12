package de.teddy.bansystem.commands;

import com.google.common.collect.Lists;
import de.teddy.bansystem.BanSystem;
import de.teddy.bansystem.database.tables.BansystemPlayer;
import de.teddy.bansystem.database.tables.BansystemToken;
import de.teddy.bansystem.database.tables.BansystemWhitelist;
import de.teddy.util.HibernateUtil;
import de.teddy.util.UUIDConverter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.sql.Date;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class WhitelistCommand implements CommandExecutor, TabCompleter {
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args){
		if(sender.hasPermission("bansystem.whitelist")){
			if(args.length == 0){
				BanSystem.sendErrorMessage(sender, "Bitte gebe als erstes Argument add, remove oder list ein.");
				return true;
			}
			if(args[0].equalsIgnoreCase("add"))
				add(sender, args);
			else if(args[0].equalsIgnoreCase("remove"))
				remove(sender, args);
			else if(args[0].equalsIgnoreCase("list"))
				list(sender);
			else
				BanSystem.sendErrorMessage(sender, "Bitte gebe als erstes Argument add, remove oder list ein.");
		}
		return true;
	}

	private static void add(@NotNull CommandSender sender, @NotNull String[] args){
		if(args.length == 1){
			BanSystem.sendErrorMessage(sender, "Du musst einen Spieler angeben!");
			return;
		}

		try{
			UUID uuid = UUIDConverter.getUUIDByName(args[1]);
			if(uuid != null){
				Session session = HibernateUtil.getSession();
				Transaction transaction = session.beginTransaction();
				BansystemPlayer bansystemPlayer = session.get(BansystemPlayer.class, uuid.toString());

				if(bansystemPlayer == null){
					bansystemPlayer = new BansystemPlayer(uuid.toString(), new Date(System.currentTimeMillis()));
					session.save(bansystemPlayer);
				}

				BansystemWhitelist bansystemWhitelist = new BansystemWhitelist();
				bansystemWhitelist.setBansystemPlayer(bansystemPlayer);
				bansystemWhitelist.setBansystemToken(session.get(BansystemToken.class, 0));
				session.save(bansystemWhitelist);

				transaction.commit();
				session.close();
				BanSystem.sendMessage(sender, args[1] + " wird zur Whitelist hinzugefügt!");
			}else{
				BanSystem.sendErrorMessage(sender, "Der Spieler konnte nicht gefunden werden!");
			}
		}catch(IOException e){
			BanSystem.sendErrorMessage(sender, "Der Spieler konnte nicht gefunden werden!");
		}
	}

	private static void remove(@NotNull CommandSender sender, @NotNull String[] args){
		if(args.length == 1){
			BanSystem.sendErrorMessage(sender, "Du musst einen Spieler angeben!");
			return;
		}

		try{
			UUID uuid = UUIDConverter.getUUIDByName(args[1]);
			if(uuid != null){
				Session session = HibernateUtil.getSession();
				Transaction transaction = session.beginTransaction();

				List<BansystemWhitelist> resultList = session.createQuery(
								"from BansystemWhitelist where bansystemPlayer.uuid = '" + uuid + "'",
								BansystemWhitelist.class)
						.setCacheable(true)
						.getResultList();

				session.remove(resultList.get(0));
				transaction.commit();
				session.close();

				BanSystem.sendMessage(sender, args[1] + " wird von der Whitelist entfernt!");
			}else{
				BanSystem.sendErrorMessage(sender, "Der Spieler konnte nicht gefunden werden!");
			}
		}catch(IOException e){
			BanSystem.sendErrorMessage(sender, "Der Spieler konnte nicht gefunden werden!");
		}
	}

	private static void list(@NotNull CommandSender sender){
		Session session = HibernateUtil.getSession();
		Transaction transaction = session.beginTransaction();

		List<BansystemWhitelist> resultList = session
				.createQuery(
						"from BansystemWhitelist",
						BansystemWhitelist.class)
				.getResultList();


		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < resultList.size(); i++){
			BansystemPlayer player = resultList.get(i).getPlayer();
			if(player != null){
				builder.append(resultList.get(i).getPlayer().getUsername());
				if(i != resultList.size() - 1)
					builder.append(", ");
			}
		}
		BanSystem.sendMessage(sender, "Die Whitelist enthält folgende Spieler: " + builder);
		transaction.commit();
		session.close();
	}


	@Nullable
	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args){
		if(args.length == 1)
			return Lists.newArrayList("add", "remove", "list");
		if(args.length >= 2){
			if(args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("list")){
				return Collections.emptyList();
			}else if(args[0].equalsIgnoreCase("remove")){
				Session session = HibernateUtil.getSession();
				Transaction transaction = session.beginTransaction();

				List<String> resultList = session.createQuery(
								"from BansystemWhitelist",
								BansystemWhitelist.class)
						.setCacheable(true)
						.getResultList()
						.stream()
						.map(bansystemWhitelist -> bansystemWhitelist.getPlayer().getUsername())
						.toList();

				transaction.commit();
				session.close();
				return resultList;
			}
		}
		return Collections.emptyList();
	}
}
