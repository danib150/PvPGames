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

import java.sql.SQLException;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.gmail.filoghost.pvpgames.Perms;
import com.gmail.filoghost.pvpgames.PvPGames;
import com.gmail.filoghost.pvpgames.hud.menu.TeleporterMenu;
import com.gmail.filoghost.pvpgames.hud.sidebar.SidebarManager;
import com.gmail.filoghost.pvpgames.mysql.SQLManager;
import com.gmail.filoghost.pvpgames.mysql.SQLTask;
import com.gmail.filoghost.pvpgames.player.PvPGamer;
import com.gmail.filoghost.pvpgames.player.Status;
import com.google.common.collect.Sets;

public class JoinQuitListener implements Listener {
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onPreLogin(AsyncPlayerPreLoginEvent event) {
		
		if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
			return;
		}
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		
		final Player player = event.getPlayer();
		
		Status status = player.hasPermission(Perms.SPECTATOR_ONJOIN) ? Status.SPECTATOR : Status.GAMER;
		final PvPGamer pvpGamer = PvPGames.registerPvPGamer(player, status); // IMPORTANTE!
		player.setScoreboard(SidebarManager.getEmptyScoreboard());
		pvpGamer.teleportDismount(PvPGames.getSpawn());
		pvpGamer.onRespawn();
		player.setLevel(0);
		player.setExp(0);
		
		new SQLTask() {
			public void execute() throws SQLException {
				pvpGamer.setSavedKits(SQLManager.getSavedKits(player.getName()));
			}
		}.submitAsync(player);

		// Dopo aver registrato il giocatore
		TeleporterMenu.update();
	}
	
	public static Set<PvPGamer> kickedOnDeath = Sets.newHashSet();
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		PvPGamer pvpGamer = PvPGames.unregisterPvPGamer(event.getPlayer()); // IMPORTANTE!
		if (pvpGamer != null) {
			pvpGamer.flushStats(true);
		}
		if (pvpGamer.getMode() != null) {
			pvpGamer.getMode().getCurrentPlayers().remove(pvpGamer);
			PvPGames.refreshPlayersCount(pvpGamer.getMode());
		}
		pvpGamer.getPlayer().teleport(PvPGames.getSpawn());
		TeleporterMenu.update();
	}
	
}
