package de.teddy.bansystem.commands;

import de.teddy.bansystem.database.tables.BansystemToken;
import de.teddy.util.HibernateUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jetbrains.annotations.NotNull;

public class GenerateTokenCommand implements CommandExecutor {
	private final static String[] TOKEN_CHARS = new String[]{
			"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",
			"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
			"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args){
		if(sender.hasPermission("bansystem.generatetoken")){
			// Generate Token
			StringBuilder token = new StringBuilder();
			for(int i = 0; i < 32; i++)
				token.append(TOKEN_CHARS[(int)(Math.random() * TOKEN_CHARS.length)]);

			Session session = HibernateUtil.getSession();
			Transaction transaction = session.beginTransaction();
			BansystemToken bansystemToken = new BansystemToken();
			bansystemToken.setToken(token.toString());
			session.save(bansystemToken);
			transaction.commit();
			session.close();
			BaseComponent[] message
					= new ComponentBuilder("§7Du hast den §eToken §7erfolgreich §egeneriert§7! Klicke §1[§ehier§1] §7um diesen zu kopieren.")
					.event(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, token.toString()))
					.create();

			sender.sendMessage(message);
		}
		return true;
	}

}
