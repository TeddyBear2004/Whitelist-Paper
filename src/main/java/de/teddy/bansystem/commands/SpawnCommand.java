package de.teddy.bansystem.commands;

import de.teddy.bansystem.BanSystem;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SpawnCommand implements CommandExecutor, TabCompleter {
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args){
		if(args.length == 0){
			if(sender instanceof Player player){
				player.teleport(BanSystem.spawn);
				BanSystem.sendMessage(player, "Du wurdest zum Spawn teleportiert!");
			}
		}else{
			Player p = Bukkit.getPlayer(args[0]);
			if(p != null){
				p.teleport(BanSystem.spawn);
				BanSystem.sendMessage(p, "Du wurdest zum Spawn teleportiert!");
				BanSystem.sendMessage(sender, "Du hast " + p.getName() + " zum Spawn teleportiert!");
			}else{
				BanSystem.sendErrorMessage(sender, "Der Spieler ist nicht online oder wurde nicht gefunden!");
			}
		}
		return true;
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args){
		if(args.length == 1)
			return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
		return Collections.emptyList();
	}
}
