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
package com.gmail.filoghost.pvpgames.listener;

import java.util.Map;
import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import com.gmail.filoghost.pvpgames.PvPGames;
import com.gmail.filoghost.pvpgames.player.PvPGamer;
import com.gmail.filoghost.pvpgames.player.Status;
import com.gmail.filoghost.pvpgames.utils.DamageEventData;
import com.gmail.filoghost.pvpgames.utils.PlayerUtils;

import wild.api.world.Particle;

public class DeathListener implements Listener {

	private static Map<Player, DamageEventData> lastPlayerDamageEvent = new WeakHashMap<>();
	
	public static void setLastDamager(Player victim, Player damager) {
		lastPlayerDamageEvent.put(victim, new DamageEventData(damager.getName(), System.currentTimeMillis()));
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onDeath(PlayerDeathEvent event) {
		// Nasconde il messaggio della morte
		event.setDeathMessage(null);
		event.setDroppedExp(0);
		event.setKeepLevel(true);
		event.getDrops().clear();
		
		for (Entity entity : event.getEntity().getWorld().getEntities()) {
			if (entity.getType() == EntityType.WOLF) {
				Wolf wolf = (Wolf) entity;
				if (wolf.getOwner() != null && wolf.getOwner() == event.getEntity()) {
					Particle.CLOUD.display(wolf.getLocation(), 0.2F, 0.2F, 0.2F, 0, 20);
					wolf.remove();
				}
			}
		}
		
		Player killer = PlayerUtils.getRealDamager(event.getEntity().getLastDamageCause());
		
		if (killer == null && lastPlayerDamageEvent.containsKey(event.getEntity())) {
			
			DamageEventData lastDamage = lastPlayerDamageEvent.get(event.getEntity());
			PvPGamer hKiller = PvPGames.getPvPGamer(lastDamage.getDamager());
			
			if (hKiller != null && System.currentTimeMillis() - lastDamage.getTimestamp() < 10000) {
				killer = hKiller.getPlayer();
			}
		}
		
		PvPGamer victim = PvPGames.getPvPGamer(event.getEntity());
		victim.setKit(null);
		
		if (victim.getMode().isAutoRespawn()) {
			Bukkit.getScheduler().runTaskLater(PvPGames.getInstance(), () -> {
				victim.getPlayer().spigot().respawn();
			}, 1L);
		}
		
		parseDeath(victim, killer != null ? PvPGames.getPvPGamer(killer) : null);
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onRespawn(PlayerRespawnEvent event) {
		PvPGamer pvpGamer = PvPGames.getPvPGamer(event.getPlayer());
		pvpGamer.onRespawn();
		
		if (pvpGamer.getMode() != null) {
			event.setRespawnLocation(pvpGamer.getMode().getWarpLocation());
		} else {
			event.setRespawnLocation(PvPGames.getSpawn());
		}
	}
	
	/**
	 *  Messaggio sulla chat, kick, coins... (static perché utilizzato da altre classi, come per gli headshot)
	 */
	public static void parseDeath(final PvPGamer pvpVictim, final PvPGamer pvpKiller) {
		
		if (pvpKiller != null && pvpKiller.getStatus() == Status.GAMER && pvpKiller != pvpVictim) {
			pvpKiller.sendMessage(ChatColor.GREEN + "» " + ChatColor.DARK_GRAY + "Hai ucciso " + ChatColor.GRAY + pvpVictim.getPlayer().getName());
			int hearts = calculateHearts(pvpKiller.getPlayer());
			String kitString = pvpKiller.getKit() != null ? pvpKiller.getKit().getName() + ", " : "";
			String heartsString = hearts > 0 ? hearts + " \u2764" : "morto";
			pvpVictim.sendMessage(ChatColor.RED + "» " + ChatColor.DARK_GRAY + "Sei stato ucciso da §7" + pvpKiller.getPlayer().getName() + " §8(§7" + kitString + heartsString + "§8)");
			pvpKiller.registerKill(pvpVictim);
		}
		
		pvpVictim.registerDeath();
	}

	private static int calculateHearts(Player player) {
		return (int) Math.ceil(10.0 * player.getHealth() / player.getMaxHealth());
	}
}
