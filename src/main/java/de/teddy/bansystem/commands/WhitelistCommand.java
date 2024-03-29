package de.teddy.bansystem.commands;

import com.google.common.collect.Lists;
import de.teddy.bansystem.BanSystem;
import de.teddy.bansystem.tables.BansystemPlayer;
import de.teddy.bansystem.tables.BansystemToken;
import de.teddy.bansystem.tables.BansystemWhitelist;
import de.teddy.util.UUIDConverter;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.hibernate.SessionFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.sql.Date;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WhitelistCommand implements CommandExecutor, TabCompleter {

    private final SessionFactory sessionFactory;
    private final String gamemode;
    private static final Logger LOGGER = Logger.getLogger(WhitelistCommand.class.getName());

    public WhitelistCommand(SessionFactory sessionFactory, String gamemode) {
        this.sessionFactory = sessionFactory;
        this.gamemode = gamemode;
    }

    private void add(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) {
            BanSystem.sendErrorMessage(sender, "Du musst einen Spieler angeben!");
            return;
        }

        try {
            UUID uuid = UUIDConverter.getUUIDByName(args[1]);
            if (uuid == null) {
                BanSystem.sendErrorMessage(sender, "Der Spieler konnte nicht gefunden werden!");
                return;
            }

            sessionFactory.inSession(session -> {
                session.beginTransaction();

                BansystemPlayer bansystemPlayer = session.get(BansystemPlayer.class, uuid.toString());

                if (bansystemPlayer == null) {
                    bansystemPlayer = new BansystemPlayer(uuid.toString(), new Date(System.currentTimeMillis()));
                    session.persist(bansystemPlayer);
                }

                BansystemToken token = new BansystemToken();
                token.setGamemode(gamemode);
                token.setToken(GenerateTokenCommand.generateToken());
                session.persist(token);

                BansystemWhitelist bansystemWhitelist = new BansystemWhitelist();
                bansystemWhitelist.setPlayer(bansystemPlayer);
                bansystemWhitelist.setBansystemToken(token);
                session.persist(bansystemWhitelist);

                session.getTransaction().commit();

                BanSystem.sendMessage(sender, args[1] + " wird zur Whitelist hinzugefügt!");
            });
        } catch (IOException e) {
            BanSystem.sendErrorMessage(sender, "Der Spieler konnte nicht gefunden werden!");
            LOGGER.log(Level.SEVERE, "An exception occurred", e);
        }
    }

    private void remove(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) {
            BanSystem.sendErrorMessage(sender, "Du musst einen Spieler angeben!");
            return;
        }

        try {
            UUID uuid = UUIDConverter.getUUIDByName(args[1]);
            if (uuid != null) {
                sessionFactory.inSession(session -> {
                    List<BansystemWhitelist> bansystemWhitelist = BansystemWhitelist
                            .whitelistQuery(session, uuid).getResultList();

                    bansystemWhitelist.forEach(session::remove);

                    BanSystem.sendMessage(sender, args[1] + " wird von der Whitelist entfernt!");
                });
            } else {
                BanSystem.sendErrorMessage(sender, "Der Spieler konnte nicht gefunden werden!");
            }
        } catch (IOException e) {
            BanSystem.sendErrorMessage(sender, "Der Spieler konnte nicht gefunden werden!");
        }
    }

    private void list(@NotNull CommandSender sender) {
        sessionFactory.inSession(session -> {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<BansystemWhitelist> query = builder.createQuery(BansystemWhitelist.class);
            Root<BansystemWhitelist> root = query.from(BansystemWhitelist.class);
            query.select(root);
            List<BansystemWhitelist> resultList = session.createQuery(query).getResultList();

            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < resultList.size(); i++) {
                BansystemPlayer player = resultList.get(i).getPlayer();
                if (player != null) {
                    stringBuilder.append(resultList.get(i).getPlayer().getUsername());
                    if (i != resultList.size() - 1)
                        stringBuilder.append(", ");
                }
            }
            BanSystem.sendMessage(sender, "Die Whitelist enthält folgende Spieler: " + stringBuilder);
        });
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender.hasPermission("bansystem.whitelist")) {
            if (args.length == 0) {
                BanSystem.sendErrorMessage(sender, "Bitte gebe als erstes Argument add, remove oder list ein.");
                return true;
            }
            if (args[0].equalsIgnoreCase("add"))
                add(sender, args);
            else if (args[0].equalsIgnoreCase("remove"))
                remove(sender, args);
            else if (args[0].equalsIgnoreCase("list"))
                list(sender);
            else
                BanSystem.sendErrorMessage(sender, "Bitte gebe als erstes Argument add, remove oder list ein.");
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1)
            return Lists.newArrayList("add", "remove", "list");
        if (args.length >= 2) {
            if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("list")) {
                return Collections.emptyList();
            } else if (args[0].equalsIgnoreCase("remove")) {
                return sessionFactory.fromSession(session -> {
                    CriteriaBuilder builder = session.getCriteriaBuilder();
                    CriteriaQuery<String> query = builder.createQuery(String.class);
                    Root<BansystemWhitelist> root = query.from(BansystemWhitelist.class);
                    query.select(root.get("player").get("username"));
                    return session.createQuery(query).getResultList();
                });
            }
        }
        return Collections.emptyList();
    }

}