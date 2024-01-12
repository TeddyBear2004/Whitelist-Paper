package de.teddy.bansystem.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public record ChangeGamemodeCommand(String gamemode) implements CommandExecutor, TabCompleter {

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args){
		return Bukkit.dispatchCommand(sender, "gamemode " + gamemode + " " + (args.length > 0 ? args[0] : ""));
	}

	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args){
		if(args.length == 1){
			return Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).filter(player -> player.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}
}
