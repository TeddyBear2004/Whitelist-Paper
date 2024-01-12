package de.teddy.bansystem.commands;

import de.teddy.bansystem.BanSystem;
import de.teddy.bansystem.tables.BansystemReasons;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.hibernate.SessionFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class KickCommand implements CommandExecutor, TabCompleter {

    private final SessionFactory sessionFactory;

    public KickCommand(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("bansystem.kick") || args.length < 1) return true;

        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            sender.sendMessage(Component.text(String.format("Der Spieler %s wurde nicht gefunden.", args[0]), NamedTextColor.RED));
            return true;
        }

        String reason = (args.length >= 2) ? String.join(" ", args).substring(args[0].length() + 1) : "";
        player.kick(Component.text("Du wurdest gekickt!" + (reason.isEmpty() ? "" : "\n\nGrund: " + reason), NamedTextColor.DARK_RED));

        Component message = Component.text()
                .append(Component.text("Der Spieler ", NamedTextColor.RED))
                .append(Component.text(player.getName() + " wurde gekickt!\nGrund: ", NamedTextColor.GRAY))
                .append(Component.text(reason, NamedTextColor.GRAY))
                .build();

        BanSystem.broadcastMessageWithPermission(message, message);

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .filter(player -> !player.equals(sender))
                    .map(Player::getName)
                    .collect(Collectors.toList());
        }
        if (args.length == 2)
            return getReasons();
        return Collections.emptyList();
    }

    @NotNull
    private List<String> getReasons() {
        return sessionFactory.fromSession(session -> {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<String> query = builder.createQuery(String.class);
            Root<BansystemReasons> root = query.from(BansystemReasons.class);
            query.select(root.get("reason")).where(builder.equal(root.get("type"), "k"));
            return session.createQuery(query).setCacheable(true).getResultList();

        });

    }

}