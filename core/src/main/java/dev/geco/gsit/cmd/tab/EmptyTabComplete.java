package dev.geco.gsit.cmd.tab;

import java.util.*;

import org.jetbrains.annotations.*;

import org.bukkit.command.*;

public class EmptyTabComplete implements TabCompleter {

    @Override
    public List<String> onTabComplete(@NotNull CommandSender Sender, @NotNull Command Command, @NotNull String Label, String[] Args) { return new ArrayList<>(); }

}