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
package com.gmail.filoghost.pvpgames.player;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import wild.api.WildCommons;
import wild.api.WildConstants;
import wild.api.bridges.BoostersBridge;
import wild.api.bridges.BoostersBridge.Booster;
import wild.api.bridges.CosmeticsBridge;
import wild.api.sound.EasySound;
import wild.api.world.SpectatorAPI;

import com.gmail.filoghost.pvpgames.PvPGames;
import com.gmail.filoghost.pvpgames.hud.menu.TeleporterMenu;
import com.gmail.filoghost.pvpgames.hud.sidebar.SidebarManager;
import com.gmail.filoghost.pvpgames.hud.tags.TagsManager;
import com.gmail.filoghost.pvpgames.mysql.SQLManager;
import com.gmail.filoghost.pvpgames.mysql.SQLPlayerData;
import com.gmail.filoghost.pvpgames.mysql.SQLTask;
import com.gmail.filoghost.pvpgames.player.ExpManager.LevelInfo;
import com.gmail.filoghost.pvpgames.player.Mode.SpawnPoint;
import com.gmail.filoghost.pvpgames.player.NoRepeatKillManager.RecentKills;
import com.gmail.filoghost.pvpgames.tasks.GivePotionEffectTask;
import com.gmail.filoghost.pvpgames.utils.PlayerUtils;
import com.google.common.collect.Maps;

public class PvPGamer {
	
	private static final 		int SAVE_THREESHOLD = 10;
	private static final 		Random random = new Random();
	private static final 		ItemStack compass;
	
	static {
		compass = WildConstants.Spectator.TELEPORTER.clone();
		ItemMeta meta = compass.getItemMeta();
		meta.setDisplayName(ChatColor.GREEN + "Teletrasporto rapido " + ChatColor.GRAY + "(click destro o " + ChatColor.WHITE + "/go <giocatore>" + ChatColor.GRAY + ")");
		compass.setItemMeta(meta);
	}

	@Getter private 			Player player;
	@Setter private				Map<String, SavedKit> savedKits;
	@Getter private 			Status status;
	@Getter private volatile	Mode mode;
	@Getter @Setter private 	Kit kit;
	@Getter @Setter private 	boolean paidForKit;
	
	private final				ReentrantLock lock = new ReentrantLock();
	
	@Getter private 			AtomicInteger kills;
	@Getter private 			AtomicInteger deaths;
	@Getter private 			double kdr;
	
	@Getter @Setter private 	int currentKillstreak;
	@Getter private 			AtomicInteger bestKillstreak;
	
	@Getter private 			AtomicInteger exp;
	@Getter private 			int level;
	
	@Getter private 			AtomicInteger coins;
	
	@Getter private 			AtomicInteger operationsToSave;
	
	@Getter private 			ItemStack skullItem;
	
	public PvPGamer(@NonNull Player bukkitPlayer, @NonNull Status status) {
		this.player = bukkitPlayer;
		exp = new AtomicInteger(0);
		coins = new AtomicInteger(0);
		kills = new AtomicInteger(0);
		deaths = new AtomicInteger(0);
		bestKillstreak = new AtomicInteger(0);
		operationsToSave = new AtomicInteger(0);
		skullItem = new ItemStack(Material.SKULL_ITEM);
		skullItem.setDurability((short) 3);
		SkullMeta meta = (SkullMeta) skullItem.getItemMeta();
		meta.setOwner(bukkitPlayer.getName());
		skullItem.setItemMeta(meta);

		setStatus(status, false, false, false);
	}
	
	public String getName() {
		return player.getName();
	}
	
	public void sendMessage(String message) {
		player.sendMessage(message);
	}
	
	public void resetCoinsKillsDeathsBestKillstreak() {
		coins.set(0);
		kills.set(0);
		deaths.set(0);
		bestKillstreak.set(0);
		
		kdr = PlayerUtils.calculateKDR(kills.get(), deaths.get());
		SidebarManager.setKdr(player, kdr);
		
		SidebarManager.setCoins(player, 0);
		SidebarManager.setKills(player, 0);
		SidebarManager.setDeaths(player, 0);
		SidebarManager.setBestKillStreak(player, 0);
	}
	
	public boolean setMode(Mode newMode) {
		
		if (lock.isLocked()) {
			return false;
		}
		
		
		lock.lock();
		
		if (mode == newMode) {
			lock.unlock();
			return false;
		}
		
		flushStats(true);

		kit = null;
		paidForKit = false;
		exp.set(0);
		coins.set(0);
		kills.set(0);
		deaths.set(0);
		bestKillstreak.set(0);
		kdr = 0;
		currentKillstreak = 0;
		
		if (this.mode != null) {
			this.mode.removePlayer(this);
		}
		
		if (newMode == null) {
			
			Mode oldMode = this.mode;
			this.mode = null;
			lock.unlock();
			
			PvPGames.refreshPlayersCount(oldMode);
			
			if (!Bukkit.isPrimaryThread()) {
				Bukkit.getScheduler().scheduleSyncDelayedTask(PvPGames.getInstance(), new Runnable() {
					@Override
					public void run() {
						SidebarManager.hideSidebar(player);
						player.setExp(0);
						player.setLevel(0);
					}
				});
			} else {
				SidebarManager.hideSidebar(player);
				player.setExp(0);
				player.setLevel(0);
			}
		} else {
			
			try {
				SQLPlayerData data = SQLManager.getPlayerData(getName(), newMode);
				
				exp.set(data.getExp());
				if (newMode.isEnableCoins()) {
					coins.set(data.getCoins());
				} else {
					coins.set(-1);
				}
				
				
				kills.set(data.getKills());
				deaths.set(data.getDeaths());
				bestKillstreak.set(data.getBestKillstreak());
				kdr = PlayerUtils.calculateKDR(data.getKills(), data.getDeaths());
				
				final LevelInfo levelInfo = ExpManager.getCurrentLevelInfo(data.getExp());
				
				level = levelInfo.getLevel();

				mode = newMode;
				mode.addPlayer(this);
				lock.unlock();

				if (!Bukkit.isPrimaryThread()) {
					Bukkit.getScheduler().scheduleSyncDelayedTask(PvPGames.getInstance(), new Runnable() {
						@Override
						public void run() {
							SidebarManager.showSidebar(player);
							SidebarManager.setMode(player, mode.getName());
							SidebarManager.setLevel(player, level);
							SidebarManager.setCoins(player, coins.get());
							SidebarManager.setKills(player, kills.get());
							SidebarManager.setDeaths(player, deaths.get());
							SidebarManager.setKdr(player, kdr);
							SidebarManager.setKillStreak(player, currentKillstreak);
							SidebarManager.setBestKillStreak(player, bestKillstreak.get());
							player.setExp(levelInfo.getCompletePercentage());
							player.setLevel(level);
							PvPGames.refreshPlayersCount(newMode);
						}
					});
				} else {
					SidebarManager.showSidebar(player);
					SidebarManager.setMode(player, mode.getName());
					SidebarManager.setLevel(player, level);
					SidebarManager.setCoins(player, coins.get());
					SidebarManager.setKills(player, kills.get());
					SidebarManager.setDeaths(player, deaths.get());
					SidebarManager.setKdr(player, kdr);
					SidebarManager.setKillStreak(player, currentKillstreak);
					SidebarManager.setBestKillStreak(player, bestKillstreak.get());
					player.setExp(levelInfo.getCompletePercentage());
					player.setLevel(level);
					PvPGames.refreshPlayersCount(newMode);
				}
				
			} catch (SQLException e) {
				
				lock.unlock();
				e.printStackTrace();
				sendMessage(ChatColor.RED + "Errore interno, non è stato possibile caricare i tuoi dati della modalità " + newMode.getName() + ".");
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Aggiunge già l'esperienza, con suono e messaggio in chat.
	 */
	public void registerKill(PvPGamer victim) {
		
		if (mode == null) {
			return;
		}
		
		RecentKills recentKills = NoRepeatKillManager.getRecentKills(player);
		if (!recentKills.shouldCount(victim.getName())) {
			sendMessage(ChatColor.RED + "Hai ucciso " + victim.getName() + " troppe volte di recente, non ricevi nessuna ricompensa.");
			return;
		}
		recentKills.addKill(victim.getName());
		
		kills.incrementAndGet();
		kdr = PlayerUtils.calculateKDR(kills.get(), deaths.get());
		
		int oldLevel = level;
		
		exp.addAndGet(mode.getKillExp());

		EasySound.quickPlay(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
		currentKillstreak++;
		
		if (currentKillstreak > bestKillstreak.get()) {
			bestKillstreak.set(currentKillstreak);
			SidebarManager.setBestKillStreak(player, currentKillstreak);
		}
		
		SidebarManager.setKills(player, kills.get());
		SidebarManager.setKdr(player, kdr);
		SidebarManager.setKillStreak(player, currentKillstreak);
		
		if (mode.getKillstreakActions() != null) {
			
			// Primarie: singole e repeating, che non siano execute always.
			boolean executedPrimary = false;
			
			// Esegue tutte le singole
			for (KillstreakAction action : mode.getKillstreakActions()) {
				
				// Quelle singole vengono sempre eseguite, ignorano execute always.
				if (action.isSingle() && action.shouldActivate(currentKillstreak)) {
					// Se è singola viene sempre eseguita e conta come primaria
					action.execute(this, player, mode, currentKillstreak);
					executedPrimary = true;
				}
			}
			
			// Esegue quelle che si ripetono
			for (KillstreakAction action : mode.getKillstreakActions()) {
			
				if (action.isSingle()) {
					continue; // Processate prima tutte le singole, ora ci sono quelle che si ripetono.
				}

				if (action.isExecuteAlways()) {
					// Queste vanno sempre eseguite e non contano come primarie
					if (action.shouldActivate(currentKillstreak)) {
						action.execute(this, player, mode, currentKillstreak);
					}
				} else {
					
					if (!executedPrimary) {
						// Se non sono execute always, contano come primarie anche se si ripetono.
						if (action.shouldActivate(currentKillstreak)) {
							action.execute(this, player, mode, currentKillstreak);
							executedPrimary = true;
						}
					}
				}
				
			}

		} // Fine esecuzione killstreak
		
		// Dopo perché i coins possono cambiare
		Booster booster = BoostersBridge.getActiveBooster(PvPGames.PLUGIN_ID);
		
		int earnedCoins = 0;
		if (mode.isEnableCoins()) {
			earnedCoins = BoostersBridge.applyMultiplier(mode.getKillCoins(), booster);
		}
		
		if (earnedCoins > 0) {
			coins.addAndGet(earnedCoins);
			SidebarManager.setCoins(player, coins.get());
		}
		
		String msg = ChatColor.GREEN + "+" + mode.getKillExp() + " punti esperienza!";
		if (earnedCoins > 0) {
			msg = msg + " " + ChatColor.GOLD + "+" + earnedCoins + " coins" + BoostersBridge.messageSuffix(booster) + "!";
		}
		player.sendMessage(msg);
		
		LevelInfo levelInfo = ExpManager.getCurrentLevelInfo(exp.get());
		
		level = levelInfo.getLevel();
		
		player.setExp(levelInfo.getCompletePercentage());
		
		if (player.getLevel() != level) {
			player.setLevel(level);
		}
		if (oldLevel != level) {
			// Level up!
			SidebarManager.setLevel(player, level);
			EasySound.quickPlay(player, Sound.ENTITY_PLAYER_LEVELUP, 1.5F);
			sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "LIVELLO SUCCESSIVO!" + ChatColor.GRAY + " Sei ora al livello " + ChatColor.WHITE + ChatColor.BOLD + levelInfo.getLevel() + ChatColor.GRAY + ".");
		}

		if (operationsToSave.incrementAndGet() > SAVE_THREESHOLD) {
			flushStats(true);
		}
	}
	
	public void registerDeath() {
		
		if (mode == null) {
			return;
		}
		
		deaths.incrementAndGet();
		kdr = PlayerUtils.calculateKDR(kills.get(), deaths.get());
		
		currentKillstreak = 0;
		
		SidebarManager.setDeaths(player, deaths.get());
		SidebarManager.setKdr(player, kdr);
		SidebarManager.setKillStreak(player, currentKillstreak);
		
		if (operationsToSave.incrementAndGet() > SAVE_THREESHOLD) {
			flushStats(true);
		}
	}
	
	public void detractCoins(int toDetract) {
		
		if (mode == null) {
			throw new IllegalStateException("No mode while taking coins");
		}
		
		if (coins.get() < toDetract) {
			throw new IllegalArgumentException("Not enough coins");
		}
		
		coins.getAndAdd(-toDetract);
		
		SidebarManager.setCoins(player, coins.get());
		
		if (operationsToSave.incrementAndGet() > SAVE_THREESHOLD) {
			flushStats(true);
		}
	}
	
	public void flushStats(boolean async) {
		final Mode localMode = mode;
		int old = operationsToSave.getAndSet(0);
		
		if (localMode == null || old == 0) {
			return; // No stats to flush or no mode
		}
		
		final int localExp = exp.get();
		final int localCoins = localMode.isEnableCoins() ? coins.get() : 0;
		final int localKills = kills.get();
		final int localDeaths = deaths.get();
		final int localBestKillstreak = bestKillstreak.get();
		
		if (async) {
			new SQLTask() {
				
				@Override
				public void execute() throws SQLException {
					SQLManager.setStats(getName(), localMode, localExp, localCoins, localKills, localDeaths, localBestKillstreak);
				}
			}.submitAsync(player);
		} else {
			try {
				SQLManager.setStats(getName(), localMode, localExp, localCoins, localKills, localDeaths, localBestKillstreak);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	private long lastRandomTeleport;
	
	public void executeRandomTeleport() {
		long now = System.currentTimeMillis();
		if (now - lastRandomTeleport < 1000) {
			return; // Ignore
		} else {
			lastRandomTeleport = now;
		}
		
		if (status == Status.SPECTATOR) {
			sendMessage(ChatColor.RED + "Non puoi farlo da spettatore.");
			return;
		}
		
		if (mode == null) {
			sendMessage(ChatColor.RED + "Devi scegliere prima un modalità.");
			return;
		}
		
		if (getKit() == null) {
			if (mode.getDefaultKit() != null) {
				Kit kit = PvPGames.getKits().get(mode.getDefaultKit());
				if (kit != null) {
					setKit(kit);
					kit.apply(this);
					player.updateInventory();
				} else {
					sendMessage(ChatColor.RED + "Non hai scelto nessun kit (kit default non valido).");
					return;
				}
			} else {
				sendMessage(ChatColor.RED + "Non hai scelto nessun kit.");
				return;
			}
		}
		
		List<SpawnPoint> spawnpoints = mode.getSpawnPoints();
		
		if (spawnpoints == null || spawnpoints.isEmpty()) {
			sendMessage(ChatColor.RED + "Non sono ancora stati settati punti di teletrasporto.");
			return;
		}
		
		SpawnPoint point = spawnpoints.get(random.nextInt(spawnpoints.size()));
		player.teleport(point.getRandomLocation());
		sendMessage(ChatColor.GRAY + "Sei stato teletrasportato in un punto casuale.");
	}
	
	public void showPlayer(PvPGamer other) {
		player.showPlayer(other.getPlayer());
	}
	
	public void hidePlayer(PvPGamer other) {
		player.hidePlayer(other.getPlayer());
	}
	
	public void teleportDismount(Location loc) {
		PlayerUtils.teleportDismount(player, loc);
	}
	
	public void teleportDismount(Entity entity) {
		PlayerUtils.teleportDismount(player, entity);
	}
	
	public void cleanCompletely(GameMode mode, boolean clearExp) {
		PlayerUtils.cleanCompletely(player, mode, clearExp);
	}
	
	public void cleanInventoryAndPotions(GameMode mode) {
		WildCommons.clearInventoryFully(player);
		WildCommons.removePotionEffects(player);
		player.resetMaxHealth();
		player.setHealth(((Damageable) player).getMaxHealth());
	}
	
	public void onRespawn() {
		switch (status) {
				
			case SPECTATOR:
				cleanCompletely(GameMode.CREATIVE, true);
				giveSpectatorStuff();
				Bukkit.getScheduler().scheduleSyncDelayedTask(PvPGames.getInstance(), new GivePotionEffectTask(PotionEffectType.INVISIBILITY, player));
				Bukkit.getScheduler().scheduleSyncDelayedTask(PvPGames.getInstance(), new GivePotionEffectTask(PotionEffectType.NIGHT_VISION, player));
				break;
				
			case GAMER:
				cleanCompletely(GameMode.SURVIVAL, false);
				giveSpawnStuff();
				break;
		}
	}
	
	public void setStatus(Status newStatus, boolean sendMessage, boolean cleanPlayer, boolean updateTeleporter) {
		
		if (newStatus == this.status) {
			if (sendMessage) sendMessage(ChatColor.RED + "Sei già " + status.getNameAndArticle() + "!");
			return;
		}
		
		this.status = newStatus;
		
		switch (newStatus) {
				
			case SPECTATOR:
				if (cleanPlayer) {
					cleanCompletely(GameMode.CREATIVE, true);
					giveSpectatorStuff();
				}
				TagsManager.setGhost(player);
				player.setCollidable(false);
				player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0), true);
				player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0), true);
				SpectatorAPI.setSpectator(player);
				break;
				
			case GAMER:
				if (cleanPlayer) {
					cleanCompletely(GameMode.SURVIVAL, true);
					giveSpawnStuff();
				}
				
				TagsManager.setGamer(player);
				player.setCollidable(true);
				SpectatorAPI.removeSpectator(player);
				break;
		}
		
		VanishManager.updatePlayer(this);
		
		if (sendMessage) sendMessage(ChatColor.YELLOW + "Ora sei " + newStatus.getNameAndArticle() + "!");
		if (updateTeleporter) TeleporterMenu.update();
	}
	
	public void giveSpawnStuff() {
		player.getInventory().setItem(4, PvPGames.getBookTutorial().getItemStack());
		CosmeticsBridge.giveCosmeticsItems(player.getInventory());
		CosmeticsBridge.updateCosmetics(player, CosmeticsBridge.Status.LOBBY); // Per forza qui siamo nello status lobby se gli vengono dati gli oggetti della lobby

	}
	
	public void giveSpectatorStuff() {
		player.getInventory().setItem(0, compass);
		player.getInventory().setItem(8, WildConstants.Spectator.QUIT_SPECTATING);
		CosmeticsBridge.updateCosmetics(player, CosmeticsBridge.Status.SPECTATOR); // Per forza qui siamo nello status spectator se gli vengono dati gli oggetti dello spectator
	}
	
	public void updateSavedKit(String id, SavedKit value) {
		if (savedKits == null) {
			savedKits = Maps.newHashMap();
		}
		savedKits.put(id, value);
	}
	
	public SavedKit getSavedKit(String id) {
		if (savedKits != null) {
			return savedKits.get(id);
		}
		
		return null;
	}
	
	@Override
	public String toString() {
		return getName();
	}

}
