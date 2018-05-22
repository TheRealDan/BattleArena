package me.therealdan.galacticwarfront.events;

import me.therealdan.galacticwarfront.mechanics.battle.battle.Battle;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BattleDeathEvent extends Event {

    private static HandlerList handlerList = new HandlerList();

    private Battle battle;
    private Player player, killer;

    public BattleDeathEvent(Battle battle, Player player, Player killer) {
        this.battle = battle;
        this.player = player;
        this.killer = killer;
    }

    public Battle getBattle() {
        return battle;
    }

    public Player getPlayer() {
        return player;
    }

    public Player getKiller() {
        return killer;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}