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

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import com.gmail.filoghost.pvpgames.PvPGames;
import com.gmail.filoghost.pvpgames.player.PvPGamer;
import com.gmail.filoghost.pvpgames.utils.PlayerUtils;

public class DamageListener implements Listener {
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.HIGH)
	public void onEntityDamage(EntityDamageByEntityEvent event) {
		
		if (event.getEntityType() != EntityType.PLAYER) {
			// Ignora i mob
			return;
		}
		
		if (event.getDamager().getType() == EntityType.PLAYER) {
			// Damager è un giocatore
			PvPGamer damagerGamer = PvPGames.getPvPGamer((Player) event.getDamager());
			
			if (damagerGamer.getMode() != null && damagerGamer.getMode().isOneShotMelee()) {
				event.setDamage(10000.0);
			}
			
		} else if (event.getDamager() instanceof Projectile) {
			
			Player shooterPlayer = PlayerUtils.getOnlineShooter((Projectile) event.getDamager());
			if (shooterPlayer == null) {
				return;
			}
			
			PvPGamer damagerGamer = PvPGames.getPvPGamer(shooterPlayer);
			
			if (damagerGamer.getMode() != null && damagerGamer.getMode().isOneShotProjectile()) {
				event.setDamage(10000.0);
			}
		}
		
	}

}
