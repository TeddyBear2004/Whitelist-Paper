package de.teddy.bansystem.commands;

import de.teddy.bansystem.BanSystem;
import de.teddy.bansystem.database.tables.BansystemPlayer;
import de.teddy.bansystem.database.tables.BansystemPunishment;
import de.teddy.bansystem.database.tables.BansystemReasons;
import de.teddy.util.HibernateUtil;
import de.teddy.util.TabUtil;
import de.teddy.util.TimeUtil;
import de.teddy.util.UUIDConverter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public record PunishCommand(String normalPermission, String extendedPermission, String type,
                            String verb) implements CommandExecutor, TabCompleter {

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args){
		if(args.length == 0){
			return false;
		}
		if(sender.hasPermission(extendedPermission)){
			StringBuilder timeBuilder = new StringBuilder();
			StringBuilder reasonBuilder = new StringBuilder();

			char actual = 'r';

			for(int i = 1; i < args.length; i++){
				if(args[i].equalsIgnoreCase("-t")){
					actual = 't';
				}else if(args[i].equalsIgnoreCase("-r")){
					actual = 'r';
				}else{
					if(actual == 't'){
						timeBuilder.append(args[i]).append(" ");
					}
					if(actual == 'r'){
						reasonBuilder.append(args[i]).append(" ");
					}
				}
			}

			String time = timeBuilder.toString().trim();
			String reason = reasonBuilder.toString().trim();

			if(reason.isEmpty()){
				BanSystem.sendErrorMessage(sender, "Bitte gebe einen Grund an.");
				return true;
			}

			long minutes = 0;
			if(time.isEmpty()){
				List<BansystemReasons> banSystemReasons = getBanSystemReasons();
				for(BansystemReasons banSystemReason : banSystemReasons){
					if(banSystemReason.getReason().equalsIgnoreCase(reason)){
						minutes = banSystemReason.getDuration();
						if(minutes == 0){
							BanSystem.sendErrorMessage(sender, "Die vorgefertigte Zeit konnte nicht verarbeitet werden.");
							return true;
						}
						break;
					}
				}
			}else{
				try{
					if(time.equals("p")){
						minutes = -1;
					}else if(time.equals("k")){

						String s = args[0] + " " + reason;

						return new KickCommand().onCommand(sender, command, label, s.split(" "));
					}else{
						minutes = TimeUtil.stringToMinutes(time);
						if(minutes == 0){
							BanSystem.sendErrorMessage(sender, "Bitte gib eine gültige Zeit an!");
							return true;
						}
					}

				}catch(IllegalArgumentException e){
					BanSystem.sendErrorMessage(sender, "Bitte gib eine gültige Zeit an!");
					return true;
				}
			}

			if(minutes > 10 * 365 * 1440){
				BanSystem.sendErrorMessage(sender, "Du kannst nicht länger als 10 Jahr bannen!");
				return true;
			}
			System.out.println(reason + " " + minutes);

			UUID uuid = getUuid(args[0]);
			if(uuid == null){
				BanSystem.sendErrorMessage(sender, "Der Spieler konnte nicht gefunden werden!");
				return true;
			}


			punishPlayer(uuid, args[0], reason, minutes, sender);
			sendSuccessMessage(sender, args[0], minutes, reason);
			sendPunishMessage(args[0], minutes, reason);
		}else if(sender.hasPermission(normalPermission)){
			StringBuilder reasonBuilder = new StringBuilder();
			for(int i = args[1].equals("-r") ? 2 : 1; i < args.length; i++)
				reasonBuilder.append(args[i]).append(" ");

			String reason = reasonBuilder.toString().trim();
			System.out.println(reason);
			for(BansystemReasons bansystemReasons : getBanSystemReasons()){
				if(reason.trim().equalsIgnoreCase(bansystemReasons.getReason())){
					UUID uuid = getUuid(args[0]);
					if(uuid == null){
						BanSystem.sendErrorMessage(sender, "Der Spieler konnte nicht gefunden werden!");
						return true;
					}
					punishPlayer(uuid, args[0], reason, bansystemReasons.getDuration(), sender);
					sendSuccessMessage(sender, args[0], bansystemReasons.getDuration(), bansystemReasons.getReason());
					sendPunishMessage(args[0], bansystemReasons.getDuration(), bansystemReasons.getReason());
					return true;
				}
			}
			BanSystem.sendErrorMessage(sender, "Es konnte kein passender Grund gefunden werden.");
		}
		return true;
	}

	private static String buildReason(String[] args){
		StringBuilder reasonBuilder = new StringBuilder();

		char actual = 'r';
		for(int i = 1; i < args.length; i++){
			if(args[i].equalsIgnoreCase("-t")){
				actual = 't';
			}else if(args[i].equalsIgnoreCase("-r")){
				actual = 'r';
			}else{
				if(actual == 'r'){
					reasonBuilder.append(args[i]).append(" ");
				}
			}
		}
		return reasonBuilder.toString();
	}

	@Override
	@NotNull
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args){
		if(args.length == 1){
			List<String> players = Bukkit.getOnlinePlayers()
					.stream()
					.filter(player -> !player.equals(sender))
					.map(HumanEntity::getName)
					.collect(Collectors.toList());
			return TabUtil.reduceEntries(args[0], players);
		}

		if(args.length > 2){
			String actualReason = buildReason(args);
			Optional<BansystemReasons> first = getBanSystemReasons()
					.stream()
					.filter(banSystemReason ->
							banSystemReason
									.getReason()
									.equalsIgnoreCase(actualReason.trim()))
					.findFirst();

			if(args[args.length - 2].equals("-t")){
				if(first.isPresent()){
					if(first.get().getDuration() == -1){
						return Collections.singletonList("p");
					}else{
						return TabUtil.reduceEntries(
								args[args.length - 1].trim(),
								new ArrayList<>(List.of(TimeUtil.parseMinSmall(first.get().getDuration()))));
					}
				}else if(args[args.length - 1].trim().isEmpty() && Objects.equals(type, "b")){
					return List.of("p", "k");
				}else{
					try{
						return TimeUtil.completeTime(args[args.length - 1]);
					}catch(IllegalArgumentException e){
						return Collections.emptyList();
					}
				}
			}else{ //if it is not -t it must be the reason
				if(first.isEmpty()){
					List<String> possibleReasons = TabUtil.reduceEntries(
							actualReason.trim(),
							getReasons());
					List<String> toReturn = new ArrayList<>();
					String[] s1 = actualReason.split(" ", -1);
					for(String possibleReason : possibleReasons){
						String[] s2 = possibleReason.split(" ");
						StringBuilder s = new StringBuilder();


						for(int i = 0; i < s1.length; i++){
							if(i < s2.length){
								if(!s1[i].equalsIgnoreCase(s2[i])){
									s.append(s2[i]).append(" ");
								}
							}
						}
						toReturn.add(s.toString());
					}
					if(toReturn.size() != 0)
						return toReturn;
				}
			}
		}
		if(sender.hasPermission("bansystem.mute.advanced")){
			List<String> possibleArgs = new ArrayList<>(List.of("-t", "-r"));

			if(!args[1].startsWith("-") && !args[1].isEmpty())
				possibleArgs.remove("-r");

			possibleArgs.removeAll(Arrays.asList(args));

			if(args.length == 2)
				return TabUtil.reduceEntries(args[args.length - 1], getReasons());

			return possibleArgs;
		}else if(sender.hasPermission("bansystem.mute"))
			if(args.length == 2)
				return getReasons();

		return Collections.emptyList();
	}

	private static UUID getUuid(String target){
		Player player = Bukkit.getPlayer(target);
		if(player != null)
			return player.getUniqueId();


		try{
			return UUIDConverter.getUUIDByName(target);
		}catch(IOException e){
			return null;
		}
	}

	private void punishPlayer(UUID uuid, String name, String reason, long duration, CommandSender staff){
		Session session = HibernateUtil.getSession();
		;
		Transaction transaction = session.beginTransaction();

		BansystemPlayer banned = session.get(BansystemPlayer.class, uuid.toString());
		BansystemPlayer bansystemStaff = null;
		if(staff instanceof Player player1)
			bansystemStaff = session.get(BansystemPlayer.class, player1.getUniqueId().toString());

		if(banned == null){
			banned = new BansystemPlayer(uuid.toString(), null);
			banned.setUsername(name);
			session.save(banned);
		}

		BansystemPunishment bansystemPunishment = new BansystemPunishment(banned, bansystemStaff, System.currentTimeMillis(), duration == -1 ? duration : duration * 60000, type, reason);
		bansystemPunishment.setActive(true);
		session.save(bansystemPunishment);

		Player player = Bukkit.getPlayer(uuid);
		if(player != null && Objects.equals(type, "b"))
			player.kickPlayer(bansystemPunishment.getBanScreenMessage());

		transaction.commit();
		session.close();
	}

	private void sendPunishMessage(String p, long time, String reason){
		Player sender = Bukkit.getPlayer(p);
		if(sender == null)
			return;

		String timeString = time == -1 ? "unbegrenzte Zeit " : TimeUtil.parseMinutes(time) + " Minuten ";

		BanSystem.sendErrorMessage(sender, ChatColor.GRAY + "Du wurdest " + verb + ".");
		BanSystem.sendErrorMessage(sender, ChatColor.GRAY + "Grund: " + ChatColor.GOLD + "" + reason);
		BanSystem.sendErrorMessage(sender, ChatColor.GRAY + "Dauer: " + ChatColor.GOLD + "" + timeString);
	}

	private void sendSuccessMessage(CommandSender sender, String player, long time, String reason){
		//opfer verb Gold, Grund grau Gold Dauer
		String message = String.format("%s wurde %s",
				player.trim(),
				verb);
		String secondMessage = String.format("Grund: %s%s%s Dauer: %s%s",
				ChatColor.GOLD,
				reason.trim(),
				ChatColor.GRAY,
				ChatColor.GOLD,
				time == -1 ? "Permanent" : TimeUtil.parseMinutes(time));

		String advancedMessage = String.format("%s wurde von %s%s%s %s.",
				player.trim(),
				ChatColor.AQUA,
				sender.getName(),
				ChatColor.GRAY,
				verb);

		String advancedSecondMessage = String.format("Grund: %s%s%s Dauer: %s%s",
				ChatColor.GOLD,
				reason.trim(),
				ChatColor.GRAY,
				ChatColor.GOLD,
				time == -1 ? "Permanent" : TimeUtil.parseMinutes(time));

		Bukkit.getOnlinePlayers()
				.forEach(p -> {
					if(p.hasPermission("bansystem.bansystem.receive.history.advanced")){
						BanSystem.sendMessage(p, advancedMessage);
						p.sendMessage(ChatColor.GRAY + advancedSecondMessage);
					}else if(p.hasPermission("bansystem.bansystem.receive.history")){
						BanSystem.sendMessage(p, message);
						p.sendMessage(ChatColor.GRAY + secondMessage);
					}
				});
	}

	@NotNull
	private List<String> getReasons(){
		Session session = HibernateUtil.getSession();
		;
		Transaction transaction = session.beginTransaction();
		List<String> resultList = session
				.createQuery("select reason from BansystemReasons WHERE type = :type", String.class)
				.setCacheable(true)
				.setParameter("type", type)
				.getResultList();
		transaction.commit();
		session.close();
		return resultList;
	}

	@NotNull
	private List<BansystemReasons> getBanSystemReasons(){
		Session session = HibernateUtil.getSession();
		;
		Transaction transaction = session.beginTransaction();
		List<BansystemReasons> resultList = session
				.createQuery("from BansystemReasons WHERE type = :type", BansystemReasons.class)
				.setCacheable(true)
				.setParameter("type", type)
				.getResultList();
		transaction.commit();
		session.close();
		return resultList;
	}
}
