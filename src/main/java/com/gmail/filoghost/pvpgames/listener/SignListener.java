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
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.gmail.filoghost.pvpgames.Perms;
import com.gmail.filoghost.pvpgames.PvPGames;
import com.gmail.filoghost.pvpgames.player.Kit;

public class SignListener implements Listener {
	
	public static String TRIGGER_RANDOMTP = ChatColor.DARK_GRAY + "[RandomTP]";
	public static String TRIGGER_SHOWCASE = ChatColor.DARK_BLUE + "[Anteprima]";
	public static String TRIGGER_WARP = ChatColor.DARK_BLUE + "[Warp]";
	
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void onSignChange(SignChangeEvent event) {
		if (ChatColor.stripColor(event.getLine(0)).equalsIgnoreCase("[randomtp]")) {
			if (event.getPlayer().hasPermission(Perms.BUILD)) {
				event.setLine(0, TRIGGER_RANDOMTP);
			}
		}
		
		if (ChatColor.stripColor(event.getLine(0)).equalsIgnoreCase("[anteprima]")) {
			if (event.getPlayer().hasPermission(Perms.BUILD)) {
				
				if (PvPGames.getKits().get(event.getLine(1)) == null) {
					event.getPlayer().sendMessage(ChatColor.RED + "Kit nella seconda riga non trovato!");
					return;
				}
				
				event.setLine(3, ChatColor.GRAY + event.getLine(1));
				event.setLine(0, TRIGGER_SHOWCASE);
				event.setLine(1, ChatColor.DARK_GRAY + "Click destro");
			}
		}
		
		if (ChatColor.stripColor(event.getLine(0)).equalsIgnoreCase("[warp]")) {
			if (event.getPlayer().hasPermission(Perms.BUILD)) {
				
				if (PvPGames.getWarpsFile().getWarpByName(event.getLine(1)) == null) {
					event.getPlayer().sendMessage(ChatColor.RED + "Warp nella seconda riga non trovato!");
					return;
				}
				
				event.setLine(0, TRIGGER_WARP);
				event.setLine(3, ChatColor.DARK_GRAY + "Click destro");
			}
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void onRightClick(PlayerInteractEvent event) {
		if (isBlockClick(event.getAction())) {
			if (event.hasBlock()) {
				Block b = event.getClickedBlock();
				if (b.getType() == Material.WALL_SIGN || b.getType() == Material.SIGN_POST) {
					
					Sign sign = (Sign) b.getState();
					
					if (sign.getLine(0).equals(TRIGGER_RANDOMTP)) {
						
						// Random spawn
						PvPGames.getPvPGamer(event.getPlayer()).executeRandomTeleport();
						
	
					} else if (sign.getLine(0).equals(TRIGGER_SHOWCASE)) {
						
						String name = sign.getLine(3);
						if (name.length() >= 2) {
							name = name.substring(2); // Rimuove il colore
						}
						Kit kit = PvPGames.getKits().get(name);
						
						if (kit == null) {
							event.getPlayer().sendMessage(ChatColor.RED + "Kit non trovato!");
							return;
						}
						
						kit.getShowcaseMenu().open(event.getPlayer());
						
					} else if (sign.getLine(0).equals(TRIGGER_WARP)) {
						
						if (sign.getLine(1).toLowerCase().equals("spawn")) {
							// Special warp
							PvPGames.getSpawnCommand().execute(event.getPlayer(), new String[0]);
							return;
						}
						
						Location warp = PvPGames.getWarpsFile().getWarpByName(sign.getLine(1));
						
						if (warp == null) {
							event.getPlayer().sendMessage(ChatColor.RED + "Warp non trovato!");
							return;
						}
						
						event.getPlayer().teleport(warp);
						event.getPlayer().sendMessage(ChatColor.GREEN + "Sei andato al warp " + sign.getLine(1) + ".");
					}
				}
			}
		} else if (event.getAction() == Action.PHYSICAL) {
			if (event.hasBlock()) {
				if (isPlate(event.getClickedBlock().getType()) && isPortal(event.getClickedBlock().getRelative(BlockFace.UP).getType())) {
					// Random spawn
					event.setCancelled(true);
					PvPGames.getPvPGamer(event.getPlayer()).executeRandomTeleport();
				}
			}
		}
	}
	
	private boolean isBlockClick(Action action) {
		return action == Action.RIGHT_CLICK_BLOCK || action == Action.LEFT_CLICK_BLOCK;
	}
	
	public static boolean isPlate(Material mat) {
		switch(mat) {
			case WOOD_PLATE:
			case STONE_PLATE:
			case GOLD_PLATE:
			case IRON_PLATE:
				return true;
			default:
				return false;
		}
	}
	
	public static boolean isPortal(Material mat) {
		switch(mat) {
			case ENDER_PORTAL:
			case END_GATEWAY:
				return true;
			default:
				return false;
		}
	}
}
