package de.teddy.bansystem.commands;

import de.teddy.bansystem.tables.BansystemPlayer;
import de.teddy.bansystem.tables.BansystemToken;
import de.teddy.util.UUIDConverter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.hibernate.SessionFactory;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Optional;

public class GenerateTokenCommand implements CommandExecutor {
    private final static String[] TOKEN_CHARS = new String[]{
            "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
    private final SessionFactory sessionFactory;
    private final String gameMode;

    public GenerateTokenCommand(SessionFactory sessionFactory, String gameMode) {
        this.sessionFactory = sessionFactory;
        this.gameMode = gameMode;
    }

    @NotNull
    public static String generateToken() {
        StringBuilder token = new StringBuilder();
        for (int i = 0; i < 32; i++)
            token.append(TOKEN_CHARS[(int) (Math.random() * TOKEN_CHARS.length)]);
        return token.toString();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Generate Token
        String token = generateToken();

        sessionFactory.inSession(session -> {
            session.beginTransaction();

            OfflinePlayer p = null;

            BansystemToken bansystemToken = new BansystemToken();
            bansystemToken.setToken(token);
            bansystemToken.setGamemode(this.gameMode);

            if (args.length > 0) {
                String playerName = args[0];
                BansystemPlayer player = session.createQuery("FROM BansystemPlayer WHERE username = :name", BansystemPlayer.class)
                        .setParameter("name", playerName)
                        .uniqueResult();
                if (player == null) {
                    player = new BansystemPlayer();
                    player.setUsername(playerName);
                    try {
                        UUIDConverter.getUUIDByName(playerName);
                        player.setUuid(UUIDConverter.getUUIDByName(playerName).toString());
                    } catch (IOException e) {
                        sender.sendMessage("Der Spieler konnte nicht gefunden werden.");
                        throw new RuntimeException(e);
                    }
                    session.persist(player);
                }

                p = Bukkit.getOfflinePlayer(player.getUsername());

                bansystemToken.setHolder(player);
            }

            session.persist(bansystemToken);

            session.getTransaction().commit();

            Component message = Component.text()
                    .append(Component.text("Du hast den ", NamedTextColor.GRAY))
                    .append(Component.text("Token ", NamedTextColor.YELLOW))
                    .append(Component.text("erfolgreich ", NamedTextColor.GRAY))
                    .append(Component.text("generiert", NamedTextColor.YELLOW))
                    .append(Component.text("! Klicke ", NamedTextColor.GRAY))
                    .append(Component.text("[", NamedTextColor.DARK_BLUE))
                    .append(Component.text("hier", NamedTextColor.YELLOW))
                    .append(Component.text("] ", NamedTextColor.DARK_BLUE))
                    .append(Component.text("um diesen zu kopieren.", NamedTextColor.GRAY))
                    .clickEvent(ClickEvent.copyToClipboard(token))
                    .build();

            if (p != null) {
                Optional.ofNullable(p.getPlayer()).ifPresent(player -> player.sendMessage(message));
            } else
                sender.sendMessage(message);

        });

        return true;
    }


}
