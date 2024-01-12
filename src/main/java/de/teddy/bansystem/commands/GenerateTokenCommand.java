package de.teddy.bansystem.commands;

import de.teddy.bansystem.tables.BansystemToken;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.jetbrains.annotations.NotNull;

public class GenerateTokenCommand implements CommandExecutor {
	private final static String[] TOKEN_CHARS = new String[]{
			"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",
			"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
			"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
	private final SessionFactory sessionFactory;

	public GenerateTokenCommand(SessionFactory sessionFactory){
		this.sessionFactory = sessionFactory;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args){
		if(sender.hasPermission("bansystem.generatetoken")){
			// Generate Token
			StringBuilder token = new StringBuilder();
			for(int i = 0; i < 32; i++)
				token.append(TOKEN_CHARS[(int)(Math.random() * TOKEN_CHARS.length)]);

			sessionFactory.inSession(session -> {
				Transaction transaction = session.beginTransaction();
				BansystemToken bansystemToken = new BansystemToken();
				bansystemToken.setToken(token.toString());
				session.persist(bansystemToken);
				transaction.commit();

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
						.clickEvent(ClickEvent.copyToClipboard(token.toString()))
						.build();

				sender.sendMessage(message);

			});


		}
		return true;
	}

}
