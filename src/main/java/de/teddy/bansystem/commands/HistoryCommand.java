package de.teddy.bansystem.commands;

import de.teddy.bansystem.tables.BansystemPunishment;
import de.teddy.util.TimeUtil;
import de.teddy.util.UUIDConverter;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.hibernate.SessionFactory;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class HistoryCommand implements CommandExecutor, TabCompleter {
	private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm");
	private final SessionFactory sessionFactory;

	public HistoryCommand(SessionFactory sessionFactory){
		this.sessionFactory = sessionFactory;
	}
	public boolean onCommand(CommandSender sender, @NotNull Command cmd, @NotNull String commandLabel, String[] args) {
		if (!sender.hasPermission("bansystem.history")) {
			sender.sendMessage(Component.text("Du hast keine Berechtigung!", NamedTextColor.RED));
			return true;
		}

		if (args.length < 1) return false;

		try {
			UUID uuidByName = UUIDConverter.getUUIDByName(args[0]);
			if (uuidByName == null) {
				sender.sendMessage(Component.text(args[0] + " wurde nicht gefunden.", NamedTextColor.RED));
				return true;
			}

			List<BansystemPunishment> punishments = getPunishments(uuidByName.toString());
			if (punishments.isEmpty()) {
				sender.sendMessage(Component.text(args[0] + " hat keine Bestrafungen!", NamedTextColor.GOLD));
				return true;
			}

			sender.sendMessage(Component.text("History von " + args[0] + ":", NamedTextColor.GOLD));
			for (BansystemPunishment punishment : punishments) {
				displayPunishment(sender, punishment);
			}
		} catch (IOException e) {
			sender.sendMessage(Component.text("Der Spieler konnte nicht gefunden werden!", NamedTextColor.RED));
		}

		return true;
	}

	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String commandLabel, String[] args) {
		return null;
	}

	private List<BansystemPunishment> getPunishments(String uuid) {
		return sessionFactory.fromSession(session -> {
			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaQuery<BansystemPunishment> query = builder.createQuery(BansystemPunishment.class);
			Root<BansystemPunishment> root = query.from(BansystemPunishment.class);
			query.select(root).where(builder.equal(root.get("player").get("uuid"), uuid)).orderBy(builder.asc(root.get("startTime")));
			return session.createQuery(query).getResultList();
		});
	}

	private void displayPunishment(CommandSender sender, BansystemPunishment punishment) {
		String punishmentType = Objects.equals(punishment.getType(), "b") ? "Ban" : punishment.getType().equals("m") ? "Mute" : "";
		Component firstMessage = Component.text()
				.append(Component.text(punishmentType + " ", NamedTextColor.DARK_RED))
				.append(Component.text(SIMPLE_DATE_FORMAT.format(new Date(punishment.getStartTime())) + " - ", NamedTextColor.YELLOW))
				.append(Component.text(TimeUtil.parseMillis(punishment.getDuration()) + " ", NamedTextColor.YELLOW))
				.append(Component.text("von ", NamedTextColor.GRAY))
				.append(Component.text(punishment.getStaff().getUsername(), NamedTextColor.AQUA))
				.build();

		TextComponent reason = Component.text(punishment.getReason(), NamedTextColor.YELLOW);
		if(!punishment.isActive())
			reason = reason.decorate(TextDecoration.STRIKETHROUGH);

		Component secondMessage = Component.text()
				.append(Component.text("(", NamedTextColor.GRAY))
				.append(reason)
				.append(Component.text(")", NamedTextColor.GRAY))
				.build();

		sender.sendMessage(firstMessage);
		sender.sendMessage(secondMessage);
	}
}