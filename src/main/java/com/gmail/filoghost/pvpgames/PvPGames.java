/*
 * Copyright (c) 2020, Wild Adventure
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 * 4. Redistribution of this software in source or binary forms shall be free
 *    of all charges or fees to the recipient of this software.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gmail.filoghost.pvpgames;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.placeholder.PlaceholderReplacer;
import com.gmail.filoghost.pvpgames.commands.ClassificaCommand;
import com.gmail.filoghost.pvpgames.commands.FixCommand;
import com.gmail.filoghost.pvpgames.commands.GoCommand;
import com.gmail.filoghost.pvpgames.commands.ModeCommand;
import com.gmail.filoghost.pvpgames.commands.ModesCommand;
import com.gmail.filoghost.pvpgames.commands.PvpgamesCommand;
import com.gmail.filoghost.pvpgames.commands.QuitCommand;
import com.gmail.filoghost.pvpgames.commands.SaveCommand;
import com.gmail.filoghost.pvpgames.commands.SpawnCommand;
import com.gmail.filoghost.pvpgames.commands.SpectatorCommand;
import com.gmail.filoghost.pvpgames.commands.StatsCommand;
import com.gmail.filoghost.pvpgames.files.GameModesFile;
import com.gmail.filoghost.pvpgames.files.Serializer;
import com.gmail.filoghost.pvpgames.files.Settings;
import com.gmail.filoghost.pvpgames.files.WarpsFile;
import com.gmail.filoghost.pvpgames.hud.menu.TeleporterMenu;
import com.gmail.filoghost.pvpgames.hud.sidebar.SidebarManager;
import com.gmail.filoghost.pvpgames.hud.tags.TagsManager;
import com.gmail.filoghost.pvpgames.listener.ChatListener;
import com.gmail.filoghost.pvpgames.listener.DamageListener;
import com.gmail.filoghost.pvpgames.listener.DeathListener;
import com.gmail.filoghost.pvpgames.listener.InventoryToolsListener;
import com.gmail.filoghost.pvpgames.listener.JoinQuitListener;
import com.gmail.filoghost.pvpgames.listener.LastDamageCauseListener;
import com.gmail.filoghost.pvpgames.listener.SignListener;
import com.gmail.filoghost.pvpgames.listener.StrengthFixListener;
import com.gmail.filoghost.pvpgames.listener.protection.BlockListener;
import com.gmail.filoghost.pvpgames.listener.protection.EntityListener;
import com.gmail.filoghost.pvpgames.listener.protection.WeatherListener;
import com.gmail.filoghost.pvpgames.mysql.SQLColumns;
import com.gmail.filoghost.pvpgames.mysql.SQLManager;
import com.gmail.filoghost.pvpgames.player.ExpManager;
import com.gmail.filoghost.pvpgames.player.Kit;
import com.gmail.filoghost.pvpgames.player.Mode;
import com.gmail.filoghost.pvpgames.player.NoRepeatKillManager;
import com.gmail.filoghost.pvpgames.player.PvPGamer;
import com.gmail.filoghost.pvpgames.player.Status;
import com.gmail.filoghost.pvpgames.timers.MySQLKeepAliveTimer;
import com.gmail.filoghost.pvpgames.timers.NoKitCheckerTimer;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import net.cubespace.yamler.YamlerConfigurationException;
import wild.api.WildCommons;
import wild.api.bridges.BoostersBridge;
import wild.api.config.PluginConfig;
import wild.api.item.BookTutorial;
import wild.api.util.CaseInsensitiveMap;

public class PvPGames extends JavaPlugin {
	
	public static final String PLUGIN_ID = "pvp_games";

	@Getter private static PvPGames instance;
	@Getter private static Settings settings;
	@Getter private static BookTutorial bookTutorial;
	@Getter private static GameModesFile modesFile;
	@Getter private static WarpsFile warpsFile;
	
	@Getter private static boolean wildChat;
	
	@Getter @Setter private static Location spawn;
	private static Map<Player, PvPGamer> players = Maps.newHashMap();
	
	@Getter
	private static Map<String, Kit> kits = new CaseInsensitiveMap<>();
	
	@Getter	private static SpawnCommand spawnCommand;
	@Getter	private static QuitCommand quitCommand;
	@Getter	private static ModeCommand modeCommand;
	
	@Override
	public void onEnable() {
		if (!Bukkit.getPluginManager().isPluginEnabled("WildCommons")) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[" + this.getName() + "] Richiesto WildCommons!");
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) { }
			Bukkit.shutdown();
			return;
		}
		
		instance = this;
		
		if (!Bukkit.getPluginManager().isPluginEnabled("HolographicMobs")) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[" + this.getName() + "] Richiesto HolographicMobs!");
			WildCommons.pauseThread(10000);
			Bukkit.shutdown();
			return;
		}
		
		if (!Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[" + this.getName() + "] Richiesto HolographicDisplays!");
			WildCommons.pauseThread(10000);
			Bukkit.shutdown();
			return;
		}
		
		if (Bukkit.getPluginManager().isPluginEnabled("WildChat")) {
			wildChat = true;
		}
		
		// Configurazione
		try {
			settings = new Settings();
			settings.init();
		} catch (YamlerConfigurationException e) {
			e.printStackTrace();
			logPurple("config.yml non caricato! Spegnimento server fra 10 secondi...");
			WildCommons.pauseThread(10000);
			Bukkit.shutdown();
			return;
		}
		
		settings.chat_modeFormat = WildCommons.color(settings.chat_modeFormat);
		settings.chat_globalFormat = WildCommons.color(settings.chat_globalFormat);
		
		try {
			modesFile = new GameModesFile(this, "modes");
		} catch (Exception e) {
			modesFile = null;
			e.printStackTrace();
			logPurple("Una delle modalità è stata non caricata! Spegnimento server fra 10 secondi...");
			WildCommons.pauseThread(10000);
			Bukkit.shutdown();
			return;
		}
		try {
			warpsFile = new WarpsFile(this, "warps.yml");
		} catch (Exception e) {
			warpsFile = null;
			e.printStackTrace();
			logPurple("warps.yml non caricato! Spegnimento server fra 10 secondi...");
			WildCommons.pauseThread(10000);
			Bukkit.shutdown();
			return;
		}
		
		// Database MySQL
		try {
			SQLManager.connect(settings.mysql_host, settings.mysql_port, settings.mysql_database, settings.mysql_user, settings.mysql_pass);
			SQLManager.checkConnection();
			
			for (Mode mode : modesFile.getAllModes()) {
				SQLManager.getMysql().update("CREATE TABLE IF NOT EXISTS kitpvp_mode_" + mode.getId() + " ("
						+ SQLColumns.NAME + " varchar(20) NOT NULL UNIQUE, "
						+ SQLColumns.EXP + " INT unsigned NOT NULL, "
						+ SQLColumns.COINS + " INT unsigned NOT NULL, "
						+ SQLColumns.KILLS + " INT unsigned NOT NULL, "
						+ SQLColumns.DEATHS + " INT unsigned NOT NULL, "
						+ SQLColumns.BEST_KILLSTREAK + " INT unsigned NOT NULL"
						+ ") ENGINE = InnoDB DEFAULT CHARSET = UTF8;");
			}
			
			SQLManager.getMysql().update("CREATE TABLE IF NOT EXISTS kitpvp_savedkits ("
					+ SQLColumns.NAME + " varchar(20) NOT NULL, "
					+ SQLColumns.KIT_ID + " varchar(30) NOT NULL, "
					+ SQLColumns.KIT_ITEMS + " varchar(150) NOT NULL, "
					+ "PRIMARY KEY(" + SQLColumns.NAME + ", " + SQLColumns.KIT_ID + ")"
					+ ") ENGINE = InnoDB DEFAULT CHARSET = UTF8;");
			
		} catch (Exception ex) {
			ex.printStackTrace();
			logPurple("Impossibile connettersi al database! Il server verrà spento in 10 secondi...");
			WildCommons.pauseThread(10000);
			Bukkit.shutdown();
			return;
		}
		
		BoostersBridge.registerPluginID(PLUGIN_ID);
		
		// Livelli
		ExpManager.loadLevels(settings.exp_levels);
		logAqua("Livello massimo: " + ExpManager.getMaxLevel());
		
		World world = Bukkit.getWorld("world");
		
		if (settings.spawn != null && !settings.spawn.isEmpty()) {
			try {
				spawn = Serializer.locationFromString(settings.spawn);
			} catch (IllegalArgumentException e) {
				logPurple("Impossibile leggere lo spawn: " + e.getMessage());
			}
		}
		
		if (spawn == null) {
			spawn = world.getSpawnLocation();
		}
		
		world.setSpawnLocation(spawn.getBlockX(), spawn.getBlockY(), spawn.getBlockZ());
		
		bookTutorial = new BookTutorial(this, "PvP Games");
		
		// Lettura kit
		File kitFolder = new File(getDataFolder(), "kits");
		if (!kitFolder.isDirectory()) {
			kitFolder.mkdirs();
		}
		
		readKits(kitFolder, kits);
		logAqua("Caricati " + kits.size() + " kit.");
		
		// Teleporter
		TeleporterMenu.load();
		
		// Sidebar & teams
		SidebarManager.initialize();
		TagsManager.initialize(SidebarManager.getScoreboard(), SidebarManager.getEmptyScoreboard());

		
		// Impostazioni del mondo
		world.setPVP(true);
		world.setSpawnFlags(false, false);
		world.setStorm(false);
		world.setThundering(false);
		world.setKeepSpawnInMemory(true);
		world.setGameRuleValue("doFireTick", "false");
		world.setGameRuleValue("doMobLoot", "false");
		world.setGameRuleValue("doMobSpawning", "false");
		world.setGameRuleValue("doTileDrops", "true");
		world.setGameRuleValue("keepInventory", "false");
		world.setGameRuleValue("mobGriefing", "false");
		world.setGameRuleValue("naturalRegeneration", "true");
		
		// Comandi
		new PvpgamesCommand();
		new ClassificaCommand();
		new StatsCommand();
		quitCommand = new QuitCommand();
		modeCommand = new ModeCommand();
		spawnCommand = new SpawnCommand();
		new ModesCommand();
		new SpectatorCommand();
		new FixCommand();
		new GoCommand();
		new SaveCommand();
		
		// Listeners
		Bukkit.getPluginManager().registerEvents(new JoinQuitListener(), this);
		Bukkit.getPluginManager().registerEvents(new LastDamageCauseListener(), this);
		Bukkit.getPluginManager().registerEvents(new DeathListener(), this);
		Bukkit.getPluginManager().registerEvents(new EntityListener(), this);
		Bukkit.getPluginManager().registerEvents(new BlockListener(), this);
		Bukkit.getPluginManager().registerEvents(new WeatherListener(), this);
		Bukkit.getPluginManager().registerEvents(new InventoryToolsListener(), this);
		Bukkit.getPluginManager().registerEvents(new ChatListener(), this);
		Bukkit.getPluginManager().registerEvents(new SignListener(), this);
		Bukkit.getPluginManager().registerEvents(new StrengthFixListener(), this);
		Bukkit.getPluginManager().registerEvents(new DamageListener(), this);
		
		// Clean up entry vecchie
		new BukkitRunnable() {
			
			@Override
			public void run() {
				NoRepeatKillManager.cleanupOld(TimeUnit.HOURS.toMillis(2));
			}
		}.runTaskTimer(this, 5 * 60 * 20L, 5 * 60 * 20L);
		
		// Timers
		new MySQLKeepAliveTimer().startNewTask();
		new NoKitCheckerTimer().startNewTask();
				
		for (final Mode mode : modesFile.getAllModes()) {
			HologramsAPI.registerPlaceholder(this, "{modeplayers_" + mode.getId() + "}", 1.0, new PlaceholderReplacer() {
				
				@Override
				public String update() {
					return Integer.toString(mode.getCurrentPlayers().size());
				}
			});
			
			mode.updateJoinMob();
		}
	}
	
	@Override
	public void onDisable() {
		for (PvPGamer pvpGamer : players.values()) {
			pvpGamer.flushStats(false);
		}
		
		for (Block b : BlockListener.getFireToExtinguish().keySet()) {
			if (b.getType() == Material.FIRE) {
				b.setType(Material.AIR);
			}
		}
		
		BlockListener.getFireToExtinguish().clear();
		BoostersBridge.unregisterPluginID(PLUGIN_ID);
	}
	
	// Ricorsiva
	private void readKits(File file, Map<String, Kit> map) {
		if (file.isFile()) {
			
			if (file.getName().endsWith(".yml")) {
				
				try {
					
					PluginConfig kitYaml = new PluginConfig(instance, file);
					Kit kit = new Kit(kitYaml);

					if (kit.getId() == null || kit.getName() == null) {
						logPurple("Kit senza 'name' o 'id': " + file.getName());
						return;
					}
					
					if (map.put(kit.getId(), kit) != null) {
						logPurple("Kit con id duplicato: " + kit.getId());
					}
					
				} catch (InvalidConfigurationException e) {
					e.printStackTrace();
					logPurple("Impossibile leggere il kit '" + file.getName() + "': configurazione non valida");
						
				} catch (IOException e) {
					e.printStackTrace();
					logPurple("Impossibile leggere il kit '" + file.getName() + "': errore I/O");
				}
			}
			
		} else if (file.isDirectory()) {
			for (File kitFile : file.listFiles()) {
				readKits(kitFile, map);
			}
		}
	}
	
	public static PvPGamer registerPvPGamer(Player bukkitPlayer, Status status) {
		PvPGamer pvpGamer = new PvPGamer(bukkitPlayer, status);
		players.put(bukkitPlayer, pvpGamer);
		return pvpGamer;
	}
	
	public static PvPGamer unregisterPvPGamer(Player bukkitPlayer) {
		return players.remove(bukkitPlayer);
	}
	
	public static PvPGamer getPvPGamer(String name) {
		name = name.toLowerCase();
		for (PvPGamer hGamer : players.values()) {
			if (hGamer.getName().toLowerCase().equals(name)) {
				return hGamer;
			}
		}
		return null;
	}
	
	public static PvPGamer getPvPGamer(Player bukkitPlayer) {
		if (bukkitPlayer == null) {
			return null;
		}
		return players.get(bukkitPlayer);
	}
	
	public static Collection<PvPGamer> getAllGamersUnsafe() {
		return players.values();
	}
	
	public static void refreshPlayersCount(Mode mode) {
		for (PvPGamer hGamer : mode.getCurrentPlayers()) {
			SidebarManager.setPlayersAmount(hGamer.getPlayer(), mode.getCurrentPlayers().size());
		}
	}
	
	// Scritte di errore
	public static void logPurple(String log) {
		Bukkit.getConsoleSender().sendMessage(ChatColor.LIGHT_PURPLE + log);
	}
	
	// Scritte normali
	public static void logAqua(String log) {
		Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + log);
	}
	
}
