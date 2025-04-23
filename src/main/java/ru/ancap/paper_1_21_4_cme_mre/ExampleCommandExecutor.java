package ru.ancap.paper_1_21_4_cme_mre;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public class ExampleCommandExecutor implements CommandExecutor {
    
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        commandSender.sendMessage(ThreadLocalRandom.current().nextInt(0, 1000)+"");
        return true;
    }
    
}