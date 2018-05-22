package me.therealdan.galacticwarfront.mechanics.lobby;

import me.therealdan.galacticwarfront.GalacticWarFront;
import me.therealdan.galacticwarfront.mechanics.battle.battle.Battle;
import me.therealdan.galacticwarfront.util.Icon;
import me.therealdan.galacticwarfront.util.PlayerHandler;
import me.therealdan.galacticwarfront.util.WXYZ;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class Lobby implements Listener {

    private static Lobby lobby;

    private static File file;
    private static FileConfiguration data;
    private static String path = "data/lobby.yml";

    private WXYZ spawnpoint;
    private ItemStack createBattleIcon;
    private boolean teleportOnJoin = true;

    private HashSet<UUID> uiOpen = new HashSet<>();

    private Lobby() {
        if (getData().contains("Lobby.Spawnpoint"))
            spawnpoint = new WXYZ(getData().getString("Lobby.Spawnpoint"));
    }

    public void join(Player player) {
        if (spawnpoint == null) return;
        player.teleport(getSpawnpoint());
        PlayerHandler.refresh(player);
    }

    public void open(Player player) {
        int size = 9;
        while (size < Battle.values().size() + 1) size += 9;
        if (size > 54) size = 54;
        Inventory inventory = Bukkit.createInventory(null, size, "");

        inventory.setItem(size - 1, getCreateBattleIcon());

        for (Battle battle : Battle.values())
            if (battle.isOpen())
                inventory.addItem(getJoinBattleIcon(battle));

        player.openInventory(inventory);
        uiOpen.add(player.getUniqueId());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (contains(player))
            player.setFoodLevel(20);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!hasUIOpen(player)) return;
        event.setCancelled(true);

        if (event.getCurrentItem() == null) return;
        if (!event.getCurrentItem().hasItemMeta()) return;
        if (!event.getCurrentItem().getItemMeta().hasDisplayName()) return;

        if (getCreateBattleIcon().isSimilar(event.getCurrentItem())) {
            BattleCreator.getInstance().openBattleCreator(player);
        } else {
            for (Battle battle : Battle.values()) {
                if (battle.isOpen()) {
                    if (event.getCurrentItem().isSimilar(getJoinBattleIcon(battle))) {
                        battle.add(player);
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (!hasUIOpen(player)) return;
        uiOpen.remove(player.getUniqueId());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (teleportOnJoin)
            join(player);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (contains(player))
            if (player.getGameMode().equals(GameMode.SURVIVAL))
                event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (contains(player))
            if (player.getGameMode().equals(GameMode.SURVIVAL))
                event.setCancelled(true);
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (contains(player))
            if (player.getGameMode().equals(GameMode.SURVIVAL))
                event.setCancelled(true);
    }

    @EventHandler
    public void onCombat(EntityDamageByEntityEvent event) {
        Player player = null;
        if (event.getDamager() instanceof Player) player = (Player) event.getDamager();
        if (event.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile) event.getDamager();
            if (projectile.getShooter() instanceof Player) player = (Player) projectile.getShooter();
        }

        if (player == null) return;

        if (contains(player)) {
            if (player.getGameMode().equals(GameMode.SURVIVAL)) {
                event.setCancelled(true);
                if (event.getDamager() instanceof Projectile) event.getDamager().remove();
            }
        }
    }

    public void setSpawnpoint(Location location) {
        this.spawnpoint = new WXYZ(location);
    }

    public void unload() {
        if (spawnpoint != null)
            getData().set("Lobby.Spawnpoint", spawnpoint.getWxyz());

        saveData();
    }

    public boolean contains(Player player) {
        if (spawnpoint == null) return false;
        if (Battle.get(player) != null) return false;

        return spawnpoint.getLocation().getWorld() == player.getLocation().getWorld();
    }

    public boolean hasUIOpen(Player player) {
        return uiOpen.contains(player.getUniqueId());
    }

    public Location getSpawnpoint() {
        if (spawnpoint == null) return null;
        return spawnpoint.getLocation();
    }

    private ItemStack getJoinBattleIcon(Battle battle) {
        List<String> lore = new ArrayList<>();
        ItemStack icon = Icon.build(GalacticWarFront.getInstance().getConfig(), "Join_UI.Join_Battle.Icon");
        ItemMeta itemMeta = icon.getItemMeta();

        String players = Integer.toString(battle.getPlayers().size());

        for (String line : itemMeta.getLore()) {
            lore.add(line
                    .replace("%players%", players)
                    .replace("%battle%", battle.getType().toString())
                    .replace("%arena%", battle.getArena().getName())
            );
        }
        itemMeta.setLore(lore);

        icon.setItemMeta(itemMeta);
        return icon;
    }

    private ItemStack getCreateBattleIcon() {
        if (createBattleIcon == null)
            createBattleIcon = Icon.build(GalacticWarFront.getInstance().getConfig(), "Join_UI.Create_Battle.Icon", true);
        return createBattleIcon;
    }

    private static void saveData() {
        try {
            getData().save(getFile());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static FileConfiguration getData() {
        if (data == null) data = YamlConfiguration.loadConfiguration(getFile());
        return data;
    }

    private static File getFile() {
        if (file == null) {
            file = new File(GalacticWarFront.getInstance().getDataFolder(), path);
        }
        return file;
    }

    public static Lobby getInstance() {
        if (lobby == null) lobby = new Lobby();
        return lobby;
    }
}