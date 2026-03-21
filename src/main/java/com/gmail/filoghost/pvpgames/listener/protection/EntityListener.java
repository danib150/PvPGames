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
package com.gmail.filoghost.pvpgames.listener.protection;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

import com.gmail.filoghost.pvpgames.Perms;
import com.gmail.filoghost.pvpgames.PvPGames;
import com.gmail.filoghost.pvpgames.player.Status;
import com.gmail.filoghost.pvpgames.utils.PlayerUtils;


public class EntityListener implements Listener {
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST) // Low perché previene il danno
	public void onEntityDamage(EntityDamageEvent event) {
		
		if (event.getEntityType() != EntityType.PLAYER) {
			// Ignora i mob
			return;
		}
			
		if (event.getEntity().getLocation().getY() > PvPGames.getSettings().pvpMaxHeight) {
			event.setCancelled(true);
			return;
		}
		
		if (event instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent damageByEntityEvent = (EntityDamageByEntityEvent) event;
			Player damager = PlayerUtils.getRealDamager(damageByEntityEvent);
			
			if (damager != null && (damager.getLocation().getY() > PvPGames.getSettings().pvpMaxHeight || PvPGames.getPvPGamer(damager).getStatus() != Status.GAMER)) {
				event.setCancelled(true);
				return;
			}

		}
			
		if (event.getCause() == DamageCause.FALL) {
			event.setCancelled(true);
		} else if (event.getCause() == DamageCause.VOID) {
			event.setDamage(10000.0);
		}
	}
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onHangingInteract(PlayerInteractEntityEvent event) {
		if (event.getRightClicked().getType() == EntityType.ITEM_FRAME) {
			
			if (!canModifyWorld(event.getPlayer())) {
				event.setCancelled(true);
				return;
			}
		}
	}
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onHangingDamage(EntityDamageByEntityEvent event) {
		if (event.getEntityType() == EntityType.ITEM_FRAME) {
			
			if (event.getDamager().getType() != EntityType.PLAYER) {
				event.setCancelled(true);
				return;
			}
			
			if (!canModifyWorld((Player) event.getDamager())) {
				event.setCancelled(true);
				return;
			}
		}
	}
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST) // Lowest perché previene il danno
	public void onHangingBreak(HangingBreakEvent event) {

		if (event instanceof HangingBreakByEntityEvent) {
			HangingBreakByEntityEvent eventByEntity = (HangingBreakByEntityEvent) event;
			
			if (eventByEntity.getRemover() != null && eventByEntity.getRemover().getType() == EntityType.PLAYER) {
				
				if (!canModifyWorld((Player) eventByEntity.getRemover())) {
					event.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST) // Lowest perché previene il danno
	public void onHangingPlace(HangingPlaceEvent event) {
		if (!canModifyWorld(event.getPlayer())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST) // Lowest perché previene il danno
	public void onProjectileLaunch(ProjectileLaunchEvent event) {
		if (event.getEntity().getShooter() instanceof Player) {
			Player shooter = (Player) event.getEntity().getShooter();
			
			if (!canInteractWithWorld(shooter)) {
				event.setCancelled(true);
				return;
			}
			
			if (shooter.getLocation().getY() > PvPGames.getSettings().pvpMaxHeight) {
				event.setCancelled(true);
				return;
			}
		}
	}
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST) // Lowest perché previene il danno
	public void onInventoryOpen(InventoryOpenEvent event) {
		switch (event.getInventory().getType()) {
			case ANVIL:
				break;
			case BEACON:
				break;
			case BREWING:
				break;
			case DISPENSER:
				break;
			case DROPPER:
				break;
			case ENCHANTING:
				break;
			case ENDER_CHEST:
				break;
			case FURNACE:
				break;
			case HOPPER:
				break;
			default:
				return;
		}
		
		if (!canModifyWorld((Player) event.getPlayer())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST) // Lowest perché previene il danno
	public void onItemPickup(PlayerPickupItemEvent event) {
		if (!canModifyWorld(event.getPlayer())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST) // Lowest perché previene il danno
	public void onCreativeItem(InventoryCreativeEvent event) {
		if (!canModifyWorld(((Player) event.getWhoClicked()))) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler (priority = EventPriority.LOWEST)
	public void onInteract(PlayerInteractEvent event) {
		if (!canInteractWithWorld(event.getPlayer())) {
			event.setCancelled(true);
			return;
		}
		
		if (event.hasItem() && isPotion(event.getItem().getType()) && event.getPlayer().getLocation().getY() > PvPGames.getSettings().pvpMaxHeight) {
			event.setCancelled(true);
			event.getPlayer().updateInventory();
			return;
		}
	}
	
	private boolean isPotion(Material type) {
		return type == Material.POTION || type == Material.SPLASH_POTION || type == Material.LINGERING_POTION;
	}

	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onDrop(PlayerDropItemEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onItemSpawn(ItemSpawnEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onBedEnter(PlayerBedEnterEvent event) {
		event.setCancelled(true);
	}
	
	public static boolean canModifyWorld(Player player) {
		if (player == null) {
			return false;
		}
		
		return PvPGames.getPvPGamer(player).getStatus() == Status.SPECTATOR && player.hasPermission(Perms.BUILD);
	}
	
	public static boolean canInteractWithWorld(Player player) {
		if (player == null) {
			return false;
		}
		
		return PvPGames.getPvPGamer(player).getStatus() == Status.GAMER || canModifyWorld(player);
	}
}
