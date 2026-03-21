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

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.gmail.filoghost.pvpgames.PvPGames;
import com.gmail.filoghost.pvpgames.listener.SignListener;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import lombok.Getter;

public class BlockListener implements Listener {
	
	private Set<Material> allowedPlace;
	private Set<Material> allowedBreak;
	
	@Getter
	private static Map<Block, Long> fireToExtinguish;
	
	
	public BlockListener() {
		fireToExtinguish = Maps.newHashMap();
		allowedPlace = Sets.newHashSet();
		allowedBreak = Sets.newHashSet();
		
		allowedPlace.add(Material.FIRE);
		allowedBreak.add(Material.FIRE);
		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				Iterator<Entry<Block, Long>> iter = fireToExtinguish.entrySet().iterator();
				long now = System.currentTimeMillis();
				
				while (iter.hasNext()) {
					Entry<Block, Long> entry = iter.next();
					if (now >= entry.getValue()) {
						iter.remove();
						
						if (entry.getKey().getType() == Material.FIRE) {
							entry.getKey().setType(Material.AIR);
						}
					}
				}
			}
		}.runTaskTimer(PvPGames.getInstance(), 10L, 10L);
	}

	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onBreak(BlockBreakEvent event) {
		if (!EntityListener.canModifyWorld(event.getPlayer())) {
			
			if (EntityListener.canInteractWithWorld(event.getPlayer()) && allowedBreak.contains(event.getBlock().getType()) && event.getBlock().getY() <= PvPGames.getSettings().pvpMaxHeight) {
				return;
			}
			
			event.setCancelled(true);
			return;
		}
	}
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onPlace(BlockPlaceEvent event) {
		if (!EntityListener.canModifyWorld(event.getPlayer())) {
			
			if (EntityListener.canInteractWithWorld(event.getPlayer()) && allowedPlace.contains(event.getBlock().getType()) && event.getBlock().getY() <= PvPGames.getSettings().pvpMaxHeight) {
				return;
			}
			
			event.setCancelled(true);
			return;
		}
	}
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onBreakMonitor(BlockBreakEvent event) {
		if (event.getBlock().getType() == Material.FIRE) {
			fireToExtinguish.remove(event.getBlock());
		}
	}
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onPlaceMonitor(BlockPlaceEvent event) {
		if (event.getBlock().getType() == Material.FIRE) {
			fireToExtinguish.put(event.getBlock(), System.currentTimeMillis() + 5000);
		}
	}
	
	
	/**
	 * 
	 *  Cose secondarie
	 * 
	 */
	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onBucketFill(PlayerBucketFillEvent event) {
		if (!EntityListener.canModifyWorld(event.getPlayer())) {
			event.setCancelled(true);
			return;
		}
	}
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onBucketEmpty(PlayerBucketEmptyEvent event) {
		if (!EntityListener.canModifyWorld(event.getPlayer())) {
			event.setCancelled(true);
			return;
		}
	}
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onMelt(BlockFadeEvent event) {
		if (event.getBlock().getType() != Material.GRASS) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onIgnite(BlockIgniteEvent event) {
		if (event.getCause() != BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onBurn(BlockBurnEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onPhysics(BlockPhysicsEvent event) {
		if (SignListener.isPlate(event.getBlock().getType())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onExplode(EntityExplodeEvent event) {
		event.blockList().clear();
	}
	
}
