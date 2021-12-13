package me.steffansk1997.OreRegenerator;

import java.util.Set;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class EventListener implements Listener{
	private OreRegenerator plugin;
	public EventListener(OreRegenerator plugin){
		this.plugin = plugin;
	}
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e){
		if(!plugin.getConfig().getBoolean("creative") && e.getPlayer().getGameMode() == GameMode.CREATIVE){
			return;
		}
		Material mat = e.getBlock().getType();
		Block bl = e.getBlock();
		Set<String> delays = plugin.getConfig().getConfigurationSection("delays").getKeys(false);
		for(String i:delays){
			if(Material.valueOf(i.toUpperCase()) == mat){
				if(plugin.getConfig().getString("mode").equalsIgnoreCase("flag")){
					WorldGuardPlugin wgp = this.plugin.getWG();
					RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
					BlockVector3 blockVector3 = BlockVector3.at(bl.getX(),bl.getY(), bl.getZ());
					ApplicableRegionSet regions = container.get((World) bl.getWorld()).getApplicableRegions(blockVector3);
					LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(e.getPlayer());

					// StateFlag.State state = (StateFlag.State)wgp.getRegionManager(bl.getWorld()).getApplicableRegions(bl.getLocation()).getFlag(OreRegenerator.FLAG_REGENORES);
					if(regions.testState(localPlayer, OreRegenerator.FLAG_REGENORES)){
						int delay = plugin.getConfig().getInt("delays."+i+".delay");

						plugin.sql.insertBlock(i, bl.getX(), bl.getY(), bl.getZ(), bl.getWorld().getName(), delay);
						if(plugin.getConfig().contains("delays."+bl.getType().name()+".empty")){
							Material type = bl.getType();
							setBlock(bl, Material.valueOf(plugin.getConfig().getString("delays."+type.name()+".empty").toUpperCase()));
						}else{
							setBlock(bl, Material.valueOf(plugin.getConfig().getString("empty").toUpperCase()));
						}
					}
				}else{
					int delay = plugin.getConfig().getInt("delays."+i+".delay");

					plugin.sql.insertBlock(i, bl.getX(), bl.getY(), bl.getZ(), bl.getWorld().getName(), delay);
					if(plugin.getConfig().contains("delays."+bl.getType().name()+".empty")){
						Material type = bl.getType();
						setBlock(bl, Material.valueOf(plugin.getConfig().getString("delays."+type.name()+".empty").toUpperCase()));
					}else{
						setBlock(bl, Material.valueOf(plugin.getConfig().getString("empty").toUpperCase()));
					}
				}
			}
		}
	}
	@EventHandler
	public void onRightClick(final PlayerInteractEvent e){
		if(plugin.getConfig().getBoolean("right-click-message")){
			if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
				new BukkitRunnable() {
					@Override 
					public void run() {
						if(plugin.sql.getBlockData("id", e.getClickedBlock().getWorld().getName(), e.getClickedBlock().getX(), e.getClickedBlock().getY(), e.getClickedBlock().getZ()) != null){
							String blocktype = plugin.getConfig().getString("delays."+ plugin.sql.getBlockData("material", e.getClickedBlock().getWorld().getName(), e.getClickedBlock().getX(), e.getClickedBlock().getY(), e.getClickedBlock().getZ()) + ".name");
							int secs = Integer.parseInt(plugin.sql.getBlockData("respawntime", e.getClickedBlock().getWorld().getName(), e.getClickedBlock().getX(), e.getClickedBlock().getY(), e.getClickedBlock().getZ()));
							e.getPlayer().sendMessage(ChatColor.RED + "[" + ChatColor.AQUA + "OreRegen" + ChatColor.RED + "] " + ChatColor.GREEN + "This " + ChatColor.RED + blocktype + ChatColor.GREEN + " will respawn in "+ secToHMS(secs));
						}
					}
				}.runTaskAsynchronously(plugin);
			}
		}
	}
	public String secToHMS (int secs){
		int hr = (int) Math.floor(secs/3600);
	    int rem = (int)(secs%3600);
	    int mn = (int) Math.floor(rem/60);
	    int sec = rem%60;
	    String hrs = (hr == 0 ? "" : ChatColor.RED +""+ hr + ChatColor.GREEN +" hour" +(hr == 1 ? "" : "s") + (mn == 0 ? (sec == 0 ? "" : " and ") : (sec == 0 ? " and " : ", ")));
	    String mns = (mn == 0 ? "" : ChatColor.RED +""+ mn + ChatColor.GREEN + " minute"+ (mn == 1 ?  "" : "s") + (sec == 0 ? "" : " and "));
	    String seco = (sec == 0 ? "" : ChatColor.RED+""+sec+ChatColor.GREEN + " second" + (sec == 1 ? "" : "s"));
		return hrs + mns + seco;
	}
	public void setBlock(final Block bl, final Material m){
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        scheduler.scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                bl.setType(m);
            }
        }, 1L);
    }
}
