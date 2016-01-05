package me.steffansk1997.OreRegenerator;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.mewin.WGCustomFlags.WGCustomFlagsPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.StateFlag;

public class OreRegenerator extends JavaPlugin{
	public final SQLManager sql = new SQLManager(this);
	public final EventListener el = new EventListener(this);
	private WGCustomFlagsPlugin pluginWGCustomFlags;
	private WorldGuardPlugin pluginWorldGuard;
	public static final StateFlag FLAG_REGENORES = new StateFlag("regen-ores", false);
	
	@Override
	public void onEnable(){
		this.saveDefaultConfig();
		this.getServer().getPluginManager().registerEvents(el, this);
		sql.initDatabase();
		startCheck();
		if(getConfig().getString("mode").equalsIgnoreCase("flag")){
			this.pluginWGCustomFlags = this.setWGCustomFlags();
			this.pluginWorldGuard = this.setWG();
			this.pluginWGCustomFlags.addCustomFlag(FLAG_REGENORES);
		}
	}
	@Override
	public void onDisable(){
		sql.closeConnection();
	}
	
	private void startCheck(){
		new BukkitRunnable() {
			@Override
			public void run() {
				sql.check();
			}			
		}.runTaskTimerAsynchronously(this, 0L, getConfig().getInt("interval")*20L);
	}
	public WGCustomFlagsPlugin setWGCustomFlags(){
		Plugin wgcf = Bukkit.getPluginManager().getPlugin("WGCustomFlags");
		if((wgcf == null) || (!(wgcf instanceof WGCustomFlagsPlugin))){
			return null;
		}
		return (WGCustomFlagsPlugin) wgcf;
	}
	public WorldGuardPlugin setWG(){
		Plugin wg = Bukkit.getPluginManager().getPlugin("WorldGuard");
		if((wg == null) || (!(wg instanceof WorldGuardPlugin))){
			return null;
		}
		return (WorldGuardPlugin) wg;
	}
	public WorldGuardPlugin getWG(){
		return this.pluginWorldGuard;
	}
	public WGCustomFlagsPlugin getWGCF(){
		return this.pluginWGCustomFlags;
	}
}
