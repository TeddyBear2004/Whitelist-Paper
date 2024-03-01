package de.teddy.bansystem.commands;

import de.teddy.bansystem.tables.BansystemToken;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ListTokenCommand implements CommandExecutor {
    private final SessionFactory sessionFactory;

    public ListTokenCommand(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        sessionFactory.inSession(session -> {
            if (!(sender instanceof Player player)) return;

            Query<BansystemToken> query = session
                    .createQuery("from BansystemToken WHERE holder.uuid = :holdername", BansystemToken.class)
                    .setParameter("holdername", player.getUniqueId().toString());
            List<BansystemToken> tokens = query.list();

            Component message = Component.text("Deine unbenutzten Token:\n", NamedTextColor.GREEN);
            for (BansystemToken token : tokens) {
                String tokenStr = token.getToken();
                Component firstFive = Component
                        .text(tokenStr.substring(0, Math.min(tokenStr.length(), 5)))
                        .color(NamedTextColor.YELLOW);
                Component lastFive = Component
                        .text(tokenStr.length() > 5 ? tokenStr.substring(tokenStr.length() - 5) : "")
                        .color(NamedTextColor.YELLOW);
                Component middle = Component
                        .text(tokenStr.length() > 10 ? tokenStr.substring(5, tokenStr.length() - 5) : "")
                        .color(NamedTextColor.YELLOW)
                        .decoration(TextDecoration.OBFUSCATED, true);

                Component copy = Component.text(" [").color(NamedTextColor.GRAY)
                        .append(Component.text("Kopieren").color(NamedTextColor.AQUA))
                        .append(Component.text("]")).color(NamedTextColor.GRAY);

                message = message.append(
                        firstFive.append(middle)
                                .append(lastFive)
                                .append(copy)
                                .append(Component.text("\n"))
                                .clickEvent(ClickEvent.copyToClipboard(token.getToken())));
            }

            sender.sendMessage(message);
        });

        return true;
    }
}