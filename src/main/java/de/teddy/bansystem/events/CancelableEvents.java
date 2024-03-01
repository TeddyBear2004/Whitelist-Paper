package de.teddy.bansystem.events;

import com.destroystokyo.paper.profile.PlayerProfile;
import de.teddy.bansystem.BanSystem;
import de.teddy.bansystem.tables.BansystemPlayer;
import de.teddy.bansystem.tables.BansystemPunishment;
import de.teddy.bansystem.tables.BansystemWhitelist;
import de.teddy.util.TimeUtil;
import io.papermc.paper.event.player.AsyncChatEvent;
import io.papermc.paper.event.player.ChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.sql.Date;
import java.util.List;
import java.util.UUID;

public class CancelableEvents implements Listener {
    private static final List<String> forbiddenCommands = List.of("/msg", "/tell");
    private final SessionFactory sessionFactory;
    private final String gamemode;

    public CancelableEvents(SessionFactory sessionFactory, String gamemode) {
        this.sessionFactory = sessionFactory;
        this.gamemode = gamemode;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void asyncPlayerChatEvent(AsyncChatEvent event) {
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());

        if (message.startsWith("/")) {
            boolean b = true;

            for (String forbiddenCommand : forbiddenCommands) {
                if (message.toLowerCase().startsWith(forbiddenCommand)) {
                    b = false;
                    break;
                }
            }

            if (b)
                return;
        }
        Player player = event.getPlayer();

        UUID uniqueId = player.getUniqueId();

        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        List<BansystemPunishment> punishment = session
                .createQuery(
                        "from BansystemPunishment where type = :type and player.uuid = :uuid and active = :active and startTime + duration > :date order by (startTime + duration) desc",
                        BansystemPunishment.class)
                .setParameter("type", "m")
                .setParameter("uuid", uniqueId.toString())
                .setParameter("active", true)
                .setParameter("date", System.currentTimeMillis())
                .getResultList();

        if (!punishment.isEmpty()) {
            event.setCancelled(true);
            long duration = punishment.get(punishment.size() - 1).getDuration();
            BansystemPunishment punishment1;
            if (duration == -1) {
                punishment1 = punishment.get(punishment.size() - 1);
            } else {
                punishment1 = punishment.get(0);
            }

            String durationString = punishment.get(0).getDuration() == -1 ? "Permanent" : TimeUtil.parseMillis(punishment1.getDuration() + punishment1.getStartTime() - System.currentTimeMillis());
            BanSystem.sendErrorMessage(player, "§cDu bist gemuted! Grund: " + punishment1.getReason() + " Dauer: " + durationString);
        }
        transaction.commit();
        session.close();
    }

    @EventHandler
    public void asyncPlayerPreLoginEvent(AsyncPlayerPreLoginEvent event) {
        UUID id = event.getPlayerProfile().getId();
        if (id != null && Bukkit.getOperators().contains(Bukkit.getOfflinePlayer(id))) {
            return;
        }

        sessionFactory.inSession(session -> {
            session.beginTransaction();

            PlayerProfile player = event.getPlayerProfile();

            UUID uniqueId = player.getId();
            if(uniqueId == null){
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, Component.text("§4Deine UUID konnte nicht ermittelt werden!"));
                return;
            }

            List<BansystemWhitelist> bansystemWhitelist = BansystemWhitelist.whitelistQuery(session, uniqueId, gamemode).getResultList();

            if (bansystemWhitelist.isEmpty()) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, Component.text("§4Du stehst nicht auf der Whitelist!"));
                return;
            }

            BansystemPlayer bansystemPlayer = session.get(BansystemPlayer.class, uniqueId.toString());

            if (bansystemPlayer == null) {
                bansystemPlayer = new BansystemPlayer(uniqueId.toString(), new Date(System.currentTimeMillis()));
            }
            bansystemPlayer.setLastLogin(new Date(System.currentTimeMillis()));
            bansystemPlayer.setUsername(player.getName());

            session.persist(bansystemPlayer);

            List<BansystemPunishment> punishments = session
                    .createQuery(
                            "from BansystemPunishment where type = :type and player.uuid = :uuid and active = :active and (startTime + duration > :date or duration = -1) order by (startTime + duration) desc",
                            BansystemPunishment.class)
                    .setCacheable(true)
                    .setParameter("type", "b")
                    .setParameter("uuid", uniqueId.toString())
                    .setParameter("active", true)
                    .setParameter("date", System.currentTimeMillis())
                    .getResultList();
            if (!punishments.isEmpty()) {
                long duration = punishments.get(punishments.size() - 1).getDuration();
                if (duration == -1)
                    event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, Component.text(punishments.get(punishments.size() - 1).getBanScreenMessage()));
                else
                    event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, Component.text(punishments.get(0).getBanScreenMessage()));
                session.getTransaction().commit();
                return;
            }

            session.getTransaction().commit();
        });
    }}