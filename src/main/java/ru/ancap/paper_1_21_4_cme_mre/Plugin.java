package ru.ancap.paper_1_21_4_cme_mre;

import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public final class Plugin extends JavaPlugin implements Listener {
    
    public static final String TEST_COMMAND_ID = "test-command";
    public static final List<String> sources = List.of(TEST_COMMAND_ID, "test-command-alias");
    public static final CommandExecutor exampleCommandExecutor = new ExampleCommandExecutor();
    
    private volatile boolean registered = false;
    private final AtomicInteger calls = new AtomicInteger(0);
    
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        this.getCommand("run-register-unregister-by-one").setExecutor((sender, command, label, args) -> {
            final BukkitTask[] task_link = new BukkitTask[1];
            task_link[0] = Bukkit.getScheduler().runTaskTimer(this, () -> {
                if (calls.get() == 99) {
                    task_link[0].cancel();
                    calls.set(0);
                }
                calls.incrementAndGet();
                registrationStep();
            }, 0, 1);
            return true;
        });
        this.getCommand("run-register-unregister-at-once").setExecutor((sender, command, label, args) -> {
            for (int i = 0; i != 100; i++) registrationStep();
            return true;
        });
        String reregisterThis = "reregister-this";
        CommandExecutor[] reregisterThisExecutorLink = new CommandExecutor[1];
        reregisterThisExecutorLink[0] = (sender, command, label, args) -> {
            unregisterCommand(reregisterThis);
            registerCommand(reregisterThis, Plugin.this, List.of(reregisterThis, reregisterThis+"-alias"), reregisterThisExecutorLink[0]);
            return true;
        };
        this.getCommand(reregisterThis).setExecutor(reregisterThisExecutorLink[0]);
    }
    
    public void registrationStep() {
        if (!registered) {
            registerCommand(TEST_COMMAND_ID, Plugin.this, sources, exampleCommandExecutor);
            System.out.println("registering");
            registered = true;
        } else {
            System.out.println("unregistering");
            unregisterCommand(TEST_COMMAND_ID);
            System.out.println("unregistered successfully");
            registered = false;
        }
    }
    
    public static void registerCommand(
        @NotNull String id,
        @NotNull JavaPlugin owner,
        @NotNull List<String> sources,
        @NonNull CommandExecutor executor
    ) {
        var command = registerCommand(id, owner, sources);
        command.setExecutor(executor);
    }
    
    @SneakyThrows
    public static PluginCommand registerCommand(
        @NonNull String id,
        @NonNull JavaPlugin owner,
        @NonNull List<String> sources
    ) {
        CommandMap map = (CommandMap) FieldUtils.readField(Bukkit.getServer(), "commandMap", true);
        Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, org.bukkit.plugin.Plugin.class);
        constructor.setAccessible(true);
        PluginCommand command = constructor.newInstance(id, owner);
        command.setAliases(sources);
        
        map.register(owner.getName(), command);
        syncCommands();
        return command;
    }
    
    @SneakyThrows
    public static void unregisterCommand(
        @NonNull String commandName
    ) {
        SimpleCommandMap map = (SimpleCommandMap) FieldUtils.readField(Bukkit.getServer(), "commandMap", true);
        @SuppressWarnings("unchecked")
        Map<String, Command> knownCommands = (Map<String, Command>) FieldUtils.readField(map, "knownCommands", true);
        //noinspection DataFlowIssue
        for (String alias : Bukkit.getPluginCommand(commandName).getAliases()) knownCommands.remove(alias);
        knownCommands.remove(commandName);
        syncCommands();
    }
    
    @SneakyThrows
    public static void syncCommands() {
        Class<?> server = Class.forName(craftBukkitPackage()+".CraftServer");
        Method syncCommands = server.getDeclaredMethod("syncCommands");
        syncCommands.setAccessible(true);
        syncCommands.invoke(Bukkit.getServer());
    }
    
    public static String craftBukkitPackage() {
        return Bukkit.getServer().getClass().getPackage().getName();
    }
    
}