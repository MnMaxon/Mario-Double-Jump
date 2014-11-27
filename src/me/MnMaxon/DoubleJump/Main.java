package me.MnMaxon.DoubleJump;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin implements Listener {
	public static String dataFolder;
	public static Main plugin;
	public static ArrayList<Player> trackList = new ArrayList<Player>();
	public static ArrayList<Player> groundList = new ArrayList<Player>();
	public static YamlConfiguration config = null;
	public static double reactionTime = 0;
	public static double upwardVelocity = 0;
	public static double forewardVelocity = 0;
	public static boolean needsToRun = false;

	@Override
	public void onEnable() {
		plugin = this;
		dataFolder = this.getDataFolder().getAbsolutePath();
		setupConfig();
		getServer().getPluginManager().registerEvents(this, this);
	}

	public static void setupConfig() {
		config = Config.Load(dataFolder + "/Config.yml");
		boolean save = false;
		if (config.get("Running") == null) {
			config.set("Running", true);
			save = true;
		}
		if (config.get("Reaction_Time") == null) {
			config.set("Reaction_Time", .5);
			save = true;
		}
		if (config.get("Upward_Velocity") == null) {
			config.set("Upward_Velocity", .5);
			save = true;
		}
		if (config.get("Foreward_Velocity") == null) {
			config.set("Foreward_Velocity", .3);
			save = true;
		}
		reactionTime = config.getDouble("Reaction_Time");
		upwardVelocity = config.getDouble("Upward_Velocity");
		forewardVelocity = config.getDouble("Foreward_Velocity");
		needsToRun = config.getBoolean("Running");
		if (save)
			Config.Save(config, dataFolder + "/Config.yml");
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player && !((Player) sender).hasPermission("Mario.Reload")) {
			sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to do this");
			return true;
		}
		setupConfig();
		sender.sendMessage(ChatColor.GREEN + "Config reloaded!");
		return false;
	}

	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		final Player p = e.getPlayer();
		if (!p.hasPermission("Mario.Jump"))
			return;
		boolean running = true;
		if (needsToRun && !p.isSprinting())
			running = false;
		if (running && e.getFrom().getY() < e.getTo().getY()
				&& !p.getLocation().subtract(0, 1, 0).getBlock().getType().isSolid() && groundList.contains(p)) {
			groundList.remove(p);
			p.setVelocity(p.getLocation().getDirection().multiply(forewardVelocity).setY(upwardVelocity));
		} else if (running && !p.getLocation().subtract(0, 1, 0).getBlock().getType().isSolid()
				&& !trackList.contains(p))
			trackList.add(p);
		else if (trackList.contains(p) && p.getLocation().subtract(0, 1, 0).getBlock().getType().isSolid()) {
			trackList.remove(p);
			if (!groundList.contains(p)) {
				groundList.add(p);
				this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
					public void run() {
						groundList.remove(p);
					}
				}, (long) (reactionTime * 20));
			}
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		trackList.remove(e.getPlayer());
		groundList.remove(e.getPlayer());
	}
}