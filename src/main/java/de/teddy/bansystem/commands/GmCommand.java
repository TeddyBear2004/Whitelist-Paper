package de.teddy.bansystem.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GmCommand implements CommandExecutor, TabCompleter {
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args){
		if(args.length == 0){
			return false;
		}
        switch(args[0]){
            case "0" -> args[0] = "survival";
            case "1" -> args[0] = "creative";
            case "2" -> args[0] = "adventure";
            case "3" -> args[0] = "spectator";
        }
		return Bukkit.dispatchCommand(sender, "gamemode " + args[0] + " " + (args.length > 1 ? args[1] : ""));
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args){
		if(args.length == 1){
			return Arrays.asList("0", "1", "2", "3", "survival", "creative", "adventure", "spectator");
		}
		if(args.length == 2){
			return Bukkit
					.getOnlinePlayers()
					.stream()
					.map(HumanEntity::getName)
					.filter(player -> player.toLowerCase().startsWith(args[1].toLowerCase()))
					.collect(Collectors.toList());
		}
		return Collections.emptyList();
	}
}
