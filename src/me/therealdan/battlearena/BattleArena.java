package me.therealdan.battlearena;

import me.therealdan.battlearena.commands.BattleArenaCommand;
import me.therealdan.battlearena.events.BattleLeaveEvent;
import me.therealdan.battlearena.mechanics.arena.Arena;
import me.therealdan.battlearena.mechanics.battle.Battle;
import me.therealdan.battlearena.mechanics.battle.BattleHandler;
import me.therealdan.battlearena.mechanics.killcounter.KillCounter;
import me.therealdan.battlearena.mechanics.lobby.BattleCreator;
import me.therealdan.battlearena.mechanics.lobby.Lobby;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class BattleArena extends JavaPlugin {

    private static BattleArena battleArena;
    public static String MAIN, SECOND;

    @Override
    public void onEnable() {
        battleArena = this;

        saveDefaultConfig();
        MAIN = ChatColor.translateAlternateColorCodes('&', getConfig().getString("Color.Main"));
        SECOND = ChatColor.translateAlternateColorCodes('&', getConfig().getString("Color.Secondary"));

        Arena.load();

        getServer().getPluginManager().registerEvents(Lobby.getInstance(), this);
        getServer().getPluginManager().registerEvents(BattleCreator.getInstance(), this);
        getServer().getPluginManager().registerEvents(BattleHandler.getInstance(), this);

        BattleArenaCommand battleArenaCommand = new BattleArenaCommand();
        getCommand("BattleArena").setExecutor(battleArenaCommand);
        getCommand("BA").setExecutor(battleArenaCommand);
    }

    @Override
    public void onDisable() {
        for (Battle battle : Battle.values())
            battle.end(BattleLeaveEvent.Reason.SERVER_SHUTDOWN);

        Lobby.getInstance().unload();

        Arena.unload();
        KillCounter.unload();
    }

    public static BattleArena getInstance() {
        return battleArena;
    }
}