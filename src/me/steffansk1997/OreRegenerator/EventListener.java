package me.steffansk1997.OreRegenerator;

import java.util.Collection;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.StateFlag;

public class EventListener implements Listener{
	private OreRegenerator plugin;
	public EventListener(OreRegenerator plugin){
		this.plugin = plugin;
	}
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e){
		Material mat = e.getBlock().getType();
		Block bl = e.getBlock();
		Set<String> delays = plugin.getConfig().getConfigurationSection("delays").getKeys(false);
		for(String i:delays){
			if(Material.valueOf(i.toUpperCase()) == mat){
				if(plugin.getConfig().getString("mode").equalsIgnoreCase("flag")){
					WorldGuardPlugin wgp = this.plugin.getWG();
					StateFlag.State state = (StateFlag.State)wgp.getRegionManager(bl.getWorld()).getApplicableRegions(bl.getLocation()).getFlag(OreRegenerator.FLAG_REGENORES);
					if(state == StateFlag.State.ALLOW && state != null){
						int delay = plugin.getConfig().getInt("delays."+i+".delay");
						plugin.sql.insertBlock(i, (int) bl.getData(), bl.getX(), bl.getY(), bl.getZ(), bl.getWorld().getName(), delay);
						Collection<ItemStack> drops = bl.getDrops();
						for(ItemStack item:drops){
							bl.getWorld().dropItem(bl.getLocation(), item);
						}
						bl.setType(Material.valueOf(plugin.getConfig().getString("empty").toUpperCase()));
						e.setCancelled(true);
					}
				}else{
					int delay = plugin.getConfig().getInt("delays."+i+".delay");
					plugin.sql.insertBlock(i, (int) bl.getData(), bl.getX(), bl.getY(), bl.getZ(), bl.getWorld().getName(), delay);
					Collection<ItemStack> drops = bl.getDrops();
					for(ItemStack item:drops){
						bl.getWorld().dropItem(bl.getLocation(), item);
					}
					bl.setType(Material.valueOf(plugin.getConfig().getString("empty").toUpperCase()));
					e.setCancelled(true);
				}
			}
		}
	}
	@EventHandler
	public void onRightClick(PlayerInteractEvent e){
		if(plugin.getConfig().getBoolean("right-click-message")){
			if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
				if(Material.valueOf(plugin.getConfig().getString("empty").toUpperCase()) == e.getClickedBlock().getType()){
					if(plugin.sql.getBlockData("id", e.getClickedBlock().getWorld().getName(), e.getClickedBlock().getX(), e.getClickedBlock().getY(), e.getClickedBlock().getZ()) != null){
						String blocktype = plugin.getConfig().getString("delays."+ plugin.sql.getBlockData("material", e.getClickedBlock().getWorld().getName(), e.getClickedBlock().getX(), e.getClickedBlock().getY(), e.getClickedBlock().getZ()) + ".name");
						int secs = Integer.parseInt(plugin.sql.getBlockData("respawntime", e.getClickedBlock().getWorld().getName(), e.getClickedBlock().getX(), e.getClickedBlock().getY(), e.getClickedBlock().getZ()));
						e.getPlayer().sendMessage(ChatColor.RED + "[" + ChatColor.AQUA + "OreRegen" + ChatColor.RED + "] " + ChatColor.GREEN + "This " + ChatColor.RED + blocktype + ChatColor.GREEN + " will respawn in "+ secToHMS(secs));
					}
				}
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

}
