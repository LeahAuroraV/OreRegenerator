package me.steffansk1997.OreRegenerator;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.StateFlag;

public class OreRegenerator extends JavaPlugin {
	public final SQLManager sql = new SQLManager(this);
	public final EventListener el = new EventListener(this);
	private WorldGuardPlugin pluginWorldGuard;
	public static final StateFlag FLAG_REGENORES = new StateFlag("regen-ores",
			false);
	public static OreRegenerator plugin;


	@Override
	public void onEnable() {
		plugin = this;
		this.saveDefaultConfig();
		this.getServer().getPluginManager().registerEvents(el, this);
		sql.initDatabase();
		startCheck();
	}

	@Override
	public void onDisable() {
		sql.closeConnection();
	}

	private void startCheck() {
		new BukkitRunnable() {
			@Override
			public void run() {
				sql.check();
			}
		}.runTaskTimerAsynchronously(this, 0L,
				getConfig().getInt("interval") * 20L);
	}

	public WorldGuardPlugin setWG() {
		Plugin wg = Bukkit.getPluginManager().getPlugin("WorldGuard");
		if ((wg == null) || (!(wg instanceof WorldGuardPlugin))) {
			return null;
		}
		return (WorldGuardPlugin) wg;
	}

	public WorldGuardPlugin getWG() {
		return this.pluginWorldGuard;
	}
}
