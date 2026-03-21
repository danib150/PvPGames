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

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;

import wild.api.WildCommons;
import wild.api.WildConstants;
import wild.api.sound.EasySound;
import wild.api.world.Particle;

import com.gmail.filoghost.pvpgames.PvPGames;
import com.gmail.filoghost.pvpgames.hud.menu.TeleporterMenu;
import com.gmail.filoghost.pvpgames.player.PvPGamer;
import com.gmail.filoghost.pvpgames.player.Status;

public class InventoryToolsListener implements Listener {
	
	@EventHandler (priority = EventPriority.HIGH, ignoreCancelled = false)
	public void onInteract(PlayerInteractEvent event) {

		if (event.hasItem() && isRightClick(event.getAction())) {
			
			Material mat = event.getItem().getType();
			
			if (mat == Material.MUSHROOM_SOUP) {
				final Player player = event.getPlayer();
				
				if (((Damageable) player).getHealth() >= ((Damageable) player).getMaxHealth()) {
					return;
				}
				
				if (event.getItem().getAmount() <= 1) {
					new BukkitRunnable() {
						
						@Override
						public void run() {
							if (event.getHand() == EquipmentSlot.HAND) {
								player.getInventory().setItemInMainHand(null);
							} else {
								player.getInventory().setItemInOffHand(null);
							}
						}
					}.runTask(PvPGames.getInstance());
				} else {
					event.getItem().setAmount(event.getItem().getAmount() - 1);
				}
				
				WildCommons.heal(player, 8);
				EasySound.quickPlay(player, Sound.BLOCK_GRASS_BREAK, 1f, 2f);
				Location loc = player.getLocation();
				Particle.iconCrack(Material.MUSHROOM_SOUP, player.getWorld(), (float) loc.getX(), (float) loc.getY() + 1.4f, (float) loc.getZ(), 0.6f, 0.6f, 0.6f, 0.1f, 60);
				
			} if (mat == Material.COMPASS) {
				
				PvPGamer pvpGamer = PvPGames.getPvPGamer(event.getPlayer());
				
				if (pvpGamer.getStatus() != Status.GAMER) {
					TeleporterMenu.open(event.getPlayer());
				}

			} else if (mat == WildConstants.Spectator.QUIT_SPECTATING.getType()) {
				
				PvPGamer pvpGamer = PvPGames.getPvPGamer(event.getPlayer());
				
				if (pvpGamer.getStatus() != Status.GAMER) {
					
					pvpGamer.setMode(null);
					pvpGamer.getPlayer().teleport(PvPGames.getSpawn());
					pvpGamer.setStatus(Status.GAMER, false, true, true);
					pvpGamer.sendMessage(ChatColor.GREEN + "Ora sei un giocatore, puoi combattere!");
					
				}
			}
		}
	}
	
	@EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
	public void itemConsume(PlayerItemConsumeEvent event) {
		if (event.getItem().getType() == Material.POTION) {
			
			final Player player = event.getPlayer();
			
			new BukkitRunnable() {
				
				@Override
				public void run() {
					player.getInventory().remove(Material.GLASS_BOTTLE);
					player.updateInventory();
				}
			}.runTask(PvPGames.getInstance());
			
		} else if (event.getItem().getType() == Material.MUSHROOM_SOUP) {
			event.setCancelled(true);
		}
	}
	
	private boolean isRightClick(Action action) {
		return action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK;
	}

}
