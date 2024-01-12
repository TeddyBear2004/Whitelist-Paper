package de.teddy.bansystem.commands;

import de.teddy.bansystem.BanSystem;
import de.teddy.bansystem.database.tables.BansystemPunishment;
import de.teddy.util.HibernateUtil;
import de.teddy.util.TabUtil;
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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record UnPunishCommand(String type, String verb) implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args){
        if(args.length >= 1){
            try{
                UUID uuid = UUIDConverter.getUUIDByName(args[0]);
                if(uuid == null){
                    BanSystem.sendErrorMessage(sender, "Der Spieler konnte nicht gefunden werden.");
                    return true;
                }

                Session session = HibernateUtil.getSession();

                Transaction transaction = session.beginTransaction();

                try{
                    List<BansystemPunishment> punishments = session.createQuery(
                                    "from BansystemPunishment where type = :type and player.uuid = :uuid and (startTime + duration > :date or duration = -1)",
                                    BansystemPunishment.class)
                            .setCacheable(true)
                            .setParameter("type", this.type)
                            .setParameter("uuid", uuid.toString())
                            .setParameter("date", System.currentTimeMillis())
                            .getResultList();
                    punishments.forEach(punishment -> punishment.setActive(false));
                    transaction.commit();
                    session.close();
                    sendSuccessMessage(sender, args[0]);
                    return true;
                }catch(NullPointerException e){
                    e.printStackTrace();
                    transaction.rollback();
                    session.close();
                }

            }catch(IOException e){
                BanSystem.sendErrorMessage(sender, "Der Spieler konnte nicht gefunden werden.");
            }
        }
        return false;
    }

    private void sendSuccessMessage(CommandSender sender, String user){
        BanSystem.sendMessage(sender, String.format("%s wurde erfolgreich " + this.verb + ".", user));
    }

    @Override
    public @Nullable
    List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args){
        if(args.length == 1){
            Session session = HibernateUtil.getSession();
            ;
            Transaction transaction = session.beginTransaction();
            List<String> bannedPlayers = getBannedPlayers(session);
            transaction.commit();
            session.close();
            return TabUtil.reduceEntries(args[0], bannedPlayers);
        }
        return Collections.emptyList();
    }

    @NotNull
    private List<String> getBannedPlayers(Session session){
        return session.createQuery(
                        "select player.username from BansystemPunishment where type = :type and active = true and (startTime + duration > :time or duration = -1)",
                        String.class)
                .setCacheable(true)
                .setParameter("type", this.type)
                .setParameter("time", System.currentTimeMillis())
                .getResultList();
    }

    @Override
    public boolean equals(Object obj){
        if(obj == this)
            return true;
        if(obj == null || obj.getClass() != this.getClass())
            return false;
        var that = (UnPunishCommand)obj;
        return Objects.equals(this.type, that.type) &&
                Objects.equals(this.verb, that.verb);
    }

    @Override
    public int hashCode(){
        return Objects.hash(type, verb);
    }

    @Override
    public String toString(){
        return "UnPunishCommand[" +
                "type=" + type + ", " +
                "verb=" + verb + ']';
    }

}
