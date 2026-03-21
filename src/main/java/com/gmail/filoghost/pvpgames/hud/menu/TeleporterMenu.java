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
package com.gmail.filoghost.pvpgames.hud.menu;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;

import com.gmail.filoghost.pvpgames.PvPGames;
import com.gmail.filoghost.pvpgames.player.PvPGamer;
import com.gmail.filoghost.pvpgames.player.Status;
import com.google.common.collect.Lists;

import wild.api.WildCommons;
import wild.api.bridges.PexBridge;
import wild.api.bridges.PexBridge.PrefixSuffix;
import wild.api.menu.ClickHandler;
import wild.api.menu.Icon;
import wild.api.menu.IconMenu;
import wild.api.sound.EasySound;

public class TeleporterMenu {
	
	private static final int SPECTATOR_ROWS = 5;
	private static final int SPECTATOR_SLOTS = SPECTATOR_ROWS * 9;
	private static final int TOTAL_ROWS = SPECTATOR_ROWS + 1;
	private static List<IconMenu> teleporterMenus;
	
	public static void load() {
		teleporterMenus = Lists.newArrayList();
		update();
	}
	
	public static void update() {
		Collection<PvPGamer> gamers = new HashSet<PvPGamer>();
		
		for (PvPGamer pvpGamer : PvPGames.getAllGamersUnsafe()) {
			if (pvpGamer.getStatus() == Status.GAMER) {
				gamers.add(pvpGamer);
			}
		}
		
		int pages = gamers.size() % SPECTATOR_SLOTS == 0 ? (gamers.size() / SPECTATOR_SLOTS) : (gamers.size() / SPECTATOR_SLOTS) + 1;
		if (pages < 1) {
			pages = 1;
		}
		
		if (teleporterMenus.size() < pages) {

			while (teleporterMenus.size() < pages) {
				teleporterMenus.add(new IconMenu("Teletrasporto rapido", TOTAL_ROWS));
			}
			
		} else if (teleporterMenus.size() > pages) {
			
			while (teleporterMenus.size() > pages) {
				IconMenu toDelete = teleporterMenus.remove(teleporterMenus.size() - 1);
				toDelete.clearIcons();
				toDelete.refresh();
			}
		}
		
		for (IconMenu menu : teleporterMenus) {
			menu.clearIcons();
		}
		
		int index = 0;
		for (PvPGamer gamer : gamers) {
			
			int page0 = index / SPECTATOR_SLOTS;
			int slot0 = index % SPECTATOR_SLOTS;
			
			ItemMeta headMeta = gamer.getSkullItem().getItemMeta();
			if (PvPGames.isWildChat()) {
				PrefixSuffix prefixSuffix = PexBridge.getCachedPrefixSuffix(gamer.getPlayer());
				headMeta.setDisplayName(ChatColor.WHITE + WildCommons.color(prefixSuffix.getPrefix() + gamer.getName() + prefixSuffix.getSuffix()));
			} else {
				headMeta.setDisplayName(ChatColor.WHITE + gamer.getName());
			}
			
			gamer.getSkullItem().setItemMeta(headMeta);

			final Icon icon = new ItemStackIcon(gamer.getSkullItem());
			
			icon.setClickHandler(new TeleportClickHandler(gamer.getName()));
			teleporterMenus.get(page0).setIconRaw(slot0, icon);
			
			if (slot0 == 0 && pages > 1) {
				// Se ha appena cambiato pagina
				
				if (page0 == 0) {
					final int followingPage = page0 + 1;
					
					teleporterMenus.get(page0).setIcon(6, TOTAL_ROWS, new IconBuilder(Material.ARROW).name(ChatColor.WHITE + "Pagina successiva").clickHandler(new ClickHandler() {
						
						@Override
						public void onClick(Player player) {
							if (followingPage < teleporterMenus.size()) {
								EasySound.quickPlay(player, Sound.UI_BUTTON_CLICK, 1.6f, 0.5f);
								teleporterMenus.get(followingPage).open(player);
							}
						}
					}).build());
				} else if (page0 == pages - 1) {
					final int previousPage = page0 - 1;
					
					teleporterMenus.get(page0).setIcon(4, TOTAL_ROWS, new IconBuilder(Material.ARROW).name(ChatColor.WHITE + "Pagina precedente").clickHandler(new ClickHandler() {
						
						@Override
						public void onClick(Player player) {
							if (previousPage < teleporterMenus.size()) {
								EasySound.quickPlay(player, Sound.UI_BUTTON_CLICK, 1.6f, 0.5f);
								teleporterMenus.get(previousPage).open(player);
							}
						}
					}).build());
				} else {
					final int previousPage = page0 - 1;
					final int followingPage = page0 + 1;
					
					teleporterMenus.get(page0).setIcon(4, TOTAL_ROWS, new IconBuilder(Material.ARROW).name(ChatColor.WHITE + "Pagina precedente").clickHandler(new ClickHandler() {
						
						@Override
						public void onClick(Player player) {
							if (previousPage < teleporterMenus.size()) {
								EasySound.quickPlay(player, Sound.UI_BUTTON_CLICK, 1.6f, 0.5f);
								teleporterMenus.get(previousPage).open(player);
							}
						}
					}).build());
					
					teleporterMenus.get(page0).setIcon(6, TOTAL_ROWS, new IconBuilder(Material.ARROW).name(ChatColor.WHITE + "Pagina successiva").clickHandler(new ClickHandler() {
						
						@Override
						public void onClick(Player player) {
							if (followingPage < teleporterMenus.size()) {
								EasySound.quickPlay(player, Sound.UI_BUTTON_CLICK, 1.6f, 0.5f);
								teleporterMenus.get(followingPage).open(player);
							}
						}
					}).build());
				}
				
				teleporterMenus.get(page0).setIcon(5, TOTAL_ROWS, new IconBuilder(Material.PAPER).name(ChatColor.WHITE + "Pagina " + (page0 + 1)).build());
				
			}
			index++;
		}
		
		for (IconMenu menu : teleporterMenus) {
			menu.refresh();
		}
	}

	public static void open(Player player) {
		if (!teleporterMenus.isEmpty()) {
			teleporterMenus.get(0).open(player);
		}
	}
}
