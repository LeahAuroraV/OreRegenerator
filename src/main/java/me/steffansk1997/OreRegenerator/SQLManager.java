package me.steffansk1997.OreRegenerator;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

public class SQLManager {
	public Connection connection;
	private OreRegenerator plugin;

	public SQLManager(final OreRegenerator plugin) {
		this.plugin = plugin;
	}

	private void openConnection() {
		try {
			if (plugin.getConfig().getString("databasetype")
					.equalsIgnoreCase("mysql")) {
				this.connection = DriverManager.getConnection(
						"jdbc:mysql://"
								+ this.plugin.getConfig().getString(
										"MySQL.host")
								+ ":"
								+ this.plugin.getConfig().getString(
										"MySQL.port")
								+ "/"
								+ this.plugin.getConfig().getString(
										"MySQL.database"),
						new StringBuilder()
								.append(this.plugin.getConfig().getString(
										"MySQL.user")).toString(),
						new StringBuilder().append(
								this.plugin.getConfig().getString(
										"MySQL.password")).toString());
			} else {
				Class.forName("org.sqlite.JDBC");
				this.connection = DriverManager.getConnection("jdbc:sqlite:"
						+ this.plugin.getDataFolder() + "/data.db");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void closeConnection() {
		try {
			this.connection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void initDatabase() {
		if (!plugin.getConfig().getString("databasetype")
				.equalsIgnoreCase("mysql")) {
			try {
				DriverManager.registerDriver(new org.sqlite.JDBC());
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			File dbFile = new File(this.plugin.getDataFolder() + "/data.db");
			if (!dbFile.exists()) {
				try {
					dbFile.createNewFile();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		this.openConnection();
		try {
			final PreparedStatement sql = connection
					.prepareStatement("CREATE TABLE IF NOT EXISTS `OreRegen-Blocks` (`ID` ROWID, `material` varchar(100), `respawntime` INT, `x` INT, `y` INT, `z` INT, `world` VARCHAR(255)) ;");
			sql.execute();
			sql.close();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	public void removeItem(int id) {
		try {
			PreparedStatement sql = connection
					.prepareStatement("DELETE FROM `OreRegen-Blocks` WHERE `ID`=?;");
			sql.setInt(1, id);
			sql.execute();
			sql.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void insertBlock(final String material, final int x,
			final int y, final int z, final String world, final int respawntime) {

		new BukkitRunnable() {
			@Override
			public void run() {
				try {
					PreparedStatement sql = connection
							.prepareStatement("INSERT INTO `OreRegen-Blocks` (`id`, `material`, `respawntime`, `x`, `y`, `z`, `world`) VALUES (?,?,?,?,?,?,?);");
					sql.setInt(1, nextID());
					sql.setString(2, material);
					sql.setInt(3, respawntime);
					sql.setInt(4, x);
					sql.setInt(5, y);
					sql.setInt(6, z);
					sql.setString(7, world);
					sql.execute();
					sql.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.runTaskAsynchronously(plugin);

	}

	public String getBlockData(String field, String world, int x, int y, int z) {
		try {
			PreparedStatement sql = connection
					.prepareStatement("SELECT * FROM `OreRegen-Blocks` WHERE `world`=? AND `x`=? AND `y`=? AND `z`=?;");
			sql.setString(1, world);
			sql.setInt(2, x);
			sql.setInt(3, y);
			sql.setInt(4, z);
			ResultSet rs = sql.executeQuery();
			if (rs.next()) {
				String data = rs.getString(field);
				rs.close();
				sql.close();
				return data;
			} else {
				rs.close();
				sql.close();
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@SuppressWarnings("deprecation")
	public void check() {
		try {
			PreparedStatement sql1 = connection
					.prepareStatement("SELECT * FROM `OreRegen-Blocks`;");
			ResultSet rs1 = sql1.executeQuery();
			while (rs1.next()) {
				PreparedStatement sql3 = connection
						.prepareStatement("UPDATE `OreRegen-Blocks` SET `respawntime`=? WHERE `id`=?;");
				sql3.setInt(1, rs1.getInt("respawntime")
						- plugin.getConfig().getInt("interval"));
				sql3.setInt(2, rs1.getInt("id"));
				sql3.executeUpdate();
				if (rs1.getInt("respawntime")
						- plugin.getConfig().getInt("interval") <= 0) {
					String world = rs1.getString("world");
					int x = rs1.getInt("x");
					int y = rs1.getInt("y");
					int z = rs1.getInt("z");
					String material = rs1.getString("material");
					new BukkitRunnable() {
						@Override
						public void run() {
							Location loc = null;
							loc = new Location(Bukkit.getWorld(world), x, y, z);
							if (loc != null) {
								Block bl = loc.getBlock();
								if (bl.getType() == Material.valueOf(plugin
										.getConfig().getString("empty")
										.toUpperCase())
										|| bl.getType() == Material
												.valueOf(plugin
														.getConfig()
														.getString(
																"delays."
																		+ material
																				.toUpperCase()
																		+ ".empty")
														.toUpperCase())) {
									bl.setType(Material.valueOf(material
											.toUpperCase()));
								}
							}
						}
					}.runTask(plugin);
					this.removeItem(rs1.getInt("id"));
				}
				sql3.close();
			}
			rs1.close();
			sql1.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int nextID() {
		int data = 0;
		try {
			PreparedStatement sql = connection
					.prepareStatement("SELECT `ID` FROM `OreRegen-Blocks` ORDER BY `ID` DESC LIMIT 1;");
			ResultSet rs = sql.executeQuery();
			if (rs.next()) {
				data = rs.getInt("id") + 1;
			}
			rs.close();
			sql.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}

}
