package io.github.retrooper.customkb;

import io.github.retrooper.customkb.data.PlayerData;
import io.github.retrooper.customkb.packet.PacketProcessor;
import io.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.event.PacketListenerDynamic;
import io.github.retrooper.packetevents.settings.PacketEventsSettings;
import io.github.retrooper.packetevents.utils.server.ServerVersion;
import io.github.retrooper.packetevents.utils.vector.Vector3d;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Main extends JavaPlugin implements CommandExecutor {
    private final Map<UUID, PlayerData> playerDataMap = new ConcurrentHashMap<>();
    public static Main INSTANCE;
    private boolean asyncKB = false;
    private Vector3d knockbackFactor = new Vector3d(1, 1, 1);
    public final Vector3d defaultKnockbackFactor = new Vector3d(1, 1, 1);
    public ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

    @Override
    public void onLoad() {
        INSTANCE = this;
        PacketEventsSettings settings = PacketEvents.create(this).getSettings();
        settings.compatInjector(false).backupServerVersion(ServerVersion.v_1_7_10).checkForUpdates(false);
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        boolean prevAsyncKBVal = asyncKB;
        Vector3d prevKnockbackFactorVal = knockbackFactor;
        try {
            asyncKB = getConfig().getBoolean("async_knockback");
            double knockbackFactorX = getConfig().getDouble("knockback_factor_x");
            double knockbackFactorY = getConfig().getDouble("knockback_factor_y");
            double knockbackFactorZ = getConfig().getDouble("knockback_factor_z");
            this.knockbackFactor = new Vector3d(knockbackFactorX, knockbackFactorY, knockbackFactorZ);
        }
        catch (Exception ex) {
            asyncKB = prevAsyncKBVal;
            knockbackFactor = prevKnockbackFactorVal;
            getLogger().severe("[CustomKB] Failed to access CustomKB settings in the config!");
        }
        PacketEvents.get().init();
        PacketEvents.get().registerListener(new PacketProcessor());
    }

    @Override
    public void onDisable() {
        PacketEvents.get().terminate();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equals("customkb")) {
            if (args.length == 2) {
                if (args[0].equalsIgnoreCase("async")) {
                    boolean asyncKBValue = Boolean.parseBoolean(args[1]);
                    setAsyncKB(asyncKBValue);
                    sender.sendMessage(ChatColor.GREEN + "[CustomKB] Set async knockback value to " + ChatColor.GOLD + asyncKBValue);
                }
                else {
                    sender.sendMessage(ChatColor.RED + "[CustomKB] Did you mean: " + ChatColor.DARK_RED + "/customkb async <true/false>");
                }
            }
            else if (args.length == 3) {
                try {
                    double factorX = Double.parseDouble(args[0]);
                    double factorY = Double.parseDouble(args[1]);
                    double factorZ = Double.parseDouble(args[2]);
                    setKnockbackFactor(new Vector3d(factorX, factorY, factorZ));
                    sender.sendMessage(ChatColor.GREEN + "[CustomKB] Set knockback factor values to: X: " + factorX + ", Y: " + factorY + ", Z: " + factorZ);
                    return true;
                }
                catch (Exception ex) {
                    sender.sendMessage(ChatColor.RED + "[CustomKB] Invalid knockback factor values. Did you mean: " + ChatColor.DARK_RED + "/customkb <factor-x> <factor-y> <factor-z>");
                }
            }
            else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("reload")) {
                    reloadConfig();
                    sender.sendMessage(ChatColor.GREEN + "[CustomKB] Config reloaded!");
                }
                else {
                    sender.sendMessage(ChatColor.RED + "[CustomKB] Did you mean: " + ChatColor.DARK_RED + "/customkb reload");
                }
            }
            else {
                sender.sendMessage(ChatColor.RED + "[CustomKB] Unknown arguments!");
            }
        }
        return true;
    }

    public PlayerData getPlayerData(UUID uuid) {
        PlayerData data = playerDataMap.get(uuid);
        if (data == null) {
            data = new PlayerData();
            playerDataMap.put(uuid, data);
        }
        return data;
    }

    public boolean isAsyncKB() {
        return asyncKB;
    }

    public void setAsyncKB(boolean asyncKB) {
        this.asyncKB = asyncKB;
        getConfig().set("async_knockback", asyncKB);
        saveConfig();
    }

    public Vector3d getKnockbackFactor() {
        return knockbackFactor;
    }

    public void setKnockbackFactor(Vector3d knockbackFactor) {
        this.knockbackFactor = knockbackFactor;
        getConfig().set("knockback_factor_x", knockbackFactor.x);
        getConfig().set("knockback_factor_y", knockbackFactor.y);
        getConfig().set("knockback_factor_z", knockbackFactor.z);
        saveConfig();
    }
}
