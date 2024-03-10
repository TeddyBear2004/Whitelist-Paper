package de.teddy.bansystem.commands;

import de.teddy.bansystem.BanSystem;
import de.teddy.bansystem.tables.BansystemPunishment;
import de.teddy.util.TabUtil;
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

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class UnPunishCommand implements CommandExecutor, TabCompleter {

    private final String type;
    private final String verb;
    private final SessionFactory sessionFactory;

    public UnPunishCommand(String type, String verb, SessionFactory sessionFactory) {
        this.type = type;
        this.verb = verb;
        this.sessionFactory = sessionFactory;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) return false;

        try{
            UUID uuid = UUIDConverter.getUUIDByName(args[0]);
            if (uuid == null) {
                BanSystem.sendErrorMessage(sender, "Der Spieler konnte nicht gefunden werden.");
                return true;
            }

            sessionFactory.inSession(session -> {
                session.beginTransaction();
                String hql = "FROM BansystemPunishment p " +
                             "WHERE p.type = :type " +
                             "AND p.player.uuid = :uuid " +
                             "AND ((p.startTime + p.duration) > :currentTimeMillis OR p.duration = -1)";

                List<BansystemPunishment> punishments = session.createQuery(hql, BansystemPunishment.class)
                        .setParameter("type", this.type)
                        .setParameter("uuid", uuid.toString())
                        .setParameter("currentTimeMillis", System.currentTimeMillis())
                        .getResultList();

                System.out.println(punishments);

                punishments.forEach(punishment -> punishment.setActive(false));

                if(!punishments.isEmpty()){
                    sendSuccessMessage(sender, args[0]);
                }else {
                    sendNoPunishFound(sender, args[0]);
                }
                session.getTransaction().commit();
            });

        }catch(IOException e){
            BanSystem.sendErrorMessage(sender, "Der Spieler konnte nicht gefunden werden.");
        }

        return true;
    }

    private void sendSuccessMessage(CommandSender sender, String user) {
        BanSystem.sendMessage(sender, String.format("%s wurde erfolgreich " + this.verb + ".", user));
    }

    private void sendNoPunishFound(CommandSender sender, String user) {
        BanSystem.sendMessage(sender, String.format("%s hatte keine Bestrafungen.", user));
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return TabUtil.reduceEntries(args[0], getBannedPlayers());
        }
        return Collections.emptyList();
    }

    @NotNull
    private List<String> getBannedPlayers() {
        return sessionFactory.fromSession(session -> {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<String> query = builder.createQuery(String.class);
            Root<BansystemPunishment> root = query.from(BansystemPunishment.class);
            query.select(root.get("player").get("username"))
                    .where(builder.and(
                            builder.equal(root.get("type"), this.type),
                            builder.isTrue(root.get("active")),
                            builder.or(
                                    builder.greaterThan(root.get("startTime").as(Long.class), System.currentTimeMillis()),
                                    builder.equal(root.get("duration"), -1)
                            )
                    ));
            return session.createQuery(query).getResultList();
        });
    }

}