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
package com.gmail.filoghost.pvpgames.commands;

import java.io.IOException;

import net.cubespace.yamler.YamlerConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import wild.api.command.CommandFramework;
import wild.api.command.CommandFramework.Permission;

import com.gmail.filoghost.pvpgames.PvPGames;
import com.gmail.filoghost.pvpgames.files.Serializer;
import com.gmail.filoghost.pvpgames.player.Kit;
import com.gmail.filoghost.pvpgames.player.Mode;
import com.gmail.filoghost.pvpgames.player.Mode.SpawnPoint;
import com.google.common.base.Joiner;

@Permission("pvpgames.command.admin")
public class PvpgamesCommand extends CommandFramework {

	public PvpgamesCommand() {
		super(PvPGames.getInstance(), "pvpgames");
	}

	@Override
	public void execute(CommandSender sender, String label, String[] args) {
		
		if (args.length == 0) {
			sender.sendMessage(ChatColor.GOLD + "Comandi PvPGames:");
			sender.sendMessage(ChatColor.YELLOW + "/" + label + " setSpawn");
			sender.sendMessage(ChatColor.YELLOW + "/" + label + " setKitLoc <kit>");
			sender.sendMessage(ChatColor.YELLOW + "/" + label + " setJoinMob <mode>");
			sender.sendMessage(ChatColor.YELLOW + "/" + label + " setQuitMob <mode>");
			sender.sendMessage(ChatColor.YELLOW + "/" + label + " setModeSpawn <mode>");
			sender.sendMessage(ChatColor.YELLOW + "/" + label + " spawns list <mode>");
			sender.sendMessage(ChatColor.YELLOW + "/" + label + " spawns add <mode> <radius>");
			sender.sendMessage(ChatColor.YELLOW + "/" + label + " spawns remove <mode> <number>");
			sender.sendMessage(ChatColor.YELLOW + "/" + label + " setWarp <nome>");
			sender.sendMessage(ChatColor.YELLOW + "/" + label + " delWarp <nome>");
			sender.sendMessage(ChatColor.YELLOW + "/" + label + " warpList");
			return;
		}
		
		if (args[0].equalsIgnoreCase("spawns")) {
			CommandValidate.minLength(args, 2, "Troppi pochi argomenti.");
			
			if (args[1].equalsIgnoreCase("list")) {
				
				CommandValidate.minLength(args, 3, "Troppi pochi argomenti.");
				
				Mode mode = PvPGames.getModesFile().matchMode(args[2]);
				CommandValidate.notNull(mode, "Modalità non trovata.");
				CommandValidate.isTrue(!mode.getSpawnPoints().isEmpty(), "Non ci sono ancora spawn points in quella modalità.");
				
				int index = 1;
				for (SpawnPoint sp : mode.getSpawnPoints()) {
					sender.sendMessage(ChatColor.GREEN + "#" + index + ChatColor.GRAY + " raggio: " + sp.getRadius() + " §8|§7 x: " + sp.getX() + " §8|§7 y: " + sp.getY() + " §8|§7 z: " + sp.getZ() + " §8(§7" + sp.getWorld().getName() + "§8)");
					index++;
				}
				
				
			} else if (args[1].equalsIgnoreCase("add")) {
				
				Player player = CommandValidate.getPlayerSender(sender);
				CommandValidate.minLength(args, 4, "Troppi pochi argomenti.");
				
				Mode mode = PvPGames.getModesFile().matchMode(args[2]);
				CommandValidate.notNull(mode, "Modalità non trovata.");
				
				int radius = CommandValidate.getPositiveInteger(args[3]);
				
				Location loc = player.getLocation();
				mode.getSpawnPoints().add(new SpawnPoint(player.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), radius));
				player.sendMessage(ChatColor.GREEN + "Aggiunto spawnpoint #" + mode.getSpawnPoints().size());
				
				if (!PvPGames.getModesFile().saveGamemodes()) {
					sender.sendMessage(ChatColor.RED + "Impossibile salvare il file delle modalità, controlla la console.");
				}
				
				
			} else if (args[1].equalsIgnoreCase("remove")) {
				
				CommandValidate.minLength(args, 4, "Troppi pochi argomenti.");
				
				Mode mode = PvPGames.getModesFile().matchMode(args[2]);
				CommandValidate.notNull(mode, "Modalità non trovata.");
				
				int realIndex = CommandValidate.getPositiveInteger(args[3]) - 1;
				CommandValidate.isTrue(realIndex >= 0 && realIndex < mode.getSpawnPoints().size(), "Devi inserire un numero tra 1 e " + mode.getSpawnPoints().size());
					
				SpawnPoint removed = mode.getSpawnPoints().remove(realIndex);
				sender.sendMessage(ChatColor.GREEN + "Rimosso spawnpoint #" + (realIndex + 1) + ", raggio " + removed.getRadius() + " §8|§7 x: " + removed.getX() + " §8|§7 y: " + removed.getY() + " §8|§7 z: " + removed.getZ());
				
				if (!PvPGames.getModesFile().saveGamemodes()) {
					sender.sendMessage(ChatColor.RED + "Impossibile salvare il file delle modalità, controlla la console.");
				}
			}
			return;
		}
		
		
		if (args[0].equalsIgnoreCase("setSpawn")) {
			
			Location newSpawn = CommandValidate.getPlayerSender(sender).getLocation();
			
			PvPGames.setSpawn(newSpawn);
			PvPGames.getSettings().spawn = Serializer.locationToString(newSpawn);
			
			try {
				PvPGames.getSettings().save();
			} catch (YamlerConfigurationException e) {
				e.printStackTrace();
				sender.sendMessage(ChatColor.RED + "Impossibile salvare il file config.yml, controlla la console.");
			}

			sender.sendMessage(ChatColor.GREEN + "Posizione spawn cambiata!");
			return;
		}
		
		
		
		
		if (args[0].equalsIgnoreCase("setKitLoc")) {
			CommandValidate.minLength(args, 2, "Troppi pochi argomenti.");
			
			Kit kit = PvPGames.getKits().get(StringUtils.join(args, ' ', 1, args.length));
			CommandValidate.notNull(kit, "Kit non trovato.");
			
			try {
				kit.setMobLocation(CommandValidate.getPlayerSender(sender).getLocation());
			} catch (IOException e) {
				e.printStackTrace();
				sender.sendMessage(ChatColor.RED + "Impossibile salvare il file del kit, controlla la console.");
			}
			kit.updateDisplayMob();
			sender.sendMessage(ChatColor.GREEN + "Posizione del kit aggiornata!");
			return;
		}
		
		
		if (args[0].equalsIgnoreCase("setJoinMob")) {
			CommandValidate.minLength(args, 2, "Troppi pochi argomenti.");
			
			Mode mode = PvPGames.getModesFile().matchMode(args[1]);
			CommandValidate.notNull(mode, "Modalità non trovata.");
			
			mode.setJoinMobLocation(CommandValidate.getPlayerSender(sender).getLocation());
			
			if (!PvPGames.getModesFile().saveGamemodes()) {
				sender.sendMessage(ChatColor.RED + "Impossibile salvare il file delle modalità, controlla la console.");
			}
			
			mode.updateJoinMob();
			sender.sendMessage(ChatColor.GREEN + "Posizione di join aggiornata!");
			return;
		}
		
		
		if (args[0].equalsIgnoreCase("setQuitMob")) {
			CommandValidate.minLength(args, 2, "Troppi pochi argomenti.");
			
			Mode mode = PvPGames.getModesFile().matchMode(args[1]);
			CommandValidate.notNull(mode, "Modalità non trovata.");
			
			mode.setQuitMobLocation(CommandValidate.getPlayerSender(sender).getLocation());
			
			if (!PvPGames.getModesFile().saveGamemodes()) {
				sender.sendMessage(ChatColor.RED + "Impossibile salvare il file delle modalità, controlla la console.");
			}
			
			mode.updateQuitMob();
			sender.sendMessage(ChatColor.GREEN + "Posizione di quit aggiornata!");
			return;
		}

		
		if (args[0].equalsIgnoreCase("setModeSpawn")) {
			CommandValidate.minLength(args, 2, "Troppi pochi argomenti.");
			
			Mode mode = PvPGames.getModesFile().matchMode(args[1]);
			CommandValidate.notNull(mode, "Modalità non trovata.");
			
			Location newSpawn = CommandValidate.getPlayerSender(sender).getLocation();
			mode.setWarpLocation(newSpawn);
			
			if (!PvPGames.getModesFile().saveGamemodes()) {
				sender.sendMessage(ChatColor.RED + "Impossibile salvare il file delle modalità, controlla la console.");
			}

			sender.sendMessage(ChatColor.GREEN + "Spawn della modalità scelta aggiornato!");
			return;
		}
		
		if (args[0].equalsIgnoreCase("setWarp")) {
			CommandValidate.minLength(args, 2, "Troppi pochi argomenti.");
			
			String name = args[1];
			CommandValidate.isTrue(!name.equalsIgnoreCase("spawn"), "Questo nome è riservato, e serve per eseguire /spawn senza usare comandi.");
						
			Location newWarp = CommandValidate.getPlayerSender(sender).getLocation();
			
			Location oldWarp = PvPGames.getWarpsFile().getWarps().put(name, newWarp);
			
			try {
				PvPGames.getWarpsFile().saveWarps();
			} catch (IOException e) {
				e.printStackTrace();
				sender.sendMessage(ChatColor.RED + "Impossibile salvare il file warps.yml, controlla la console.");
			}

			if (oldWarp != null) {
				sender.sendMessage(ChatColor.GREEN + "Warp aggiornato!");
			} else {
				sender.sendMessage(ChatColor.GREEN + "Warp settato!");
			}
			return;
		}
		
		if (args[0].equalsIgnoreCase("delWarp")) {
			CommandValidate.minLength(args, 2, "Troppi pochi argomenti.");
			
			String name = args[1];
			
			CommandValidate.notNull(PvPGames.getWarpsFile().getWarpByName(name), "Non esiste nessun warp con quel nome.");
			PvPGames.getWarpsFile().getWarps().remove(name);
			
			try {
				PvPGames.getWarpsFile().saveWarps();
			} catch (IOException e) {
				e.printStackTrace();
				sender.sendMessage(ChatColor.RED + "Impossibile salvare il file warps.yml, controlla la console.");
			}

			sender.sendMessage(ChatColor.GREEN + "Warp rimosso.");
			return;
		}
		
		if (args[0].equalsIgnoreCase("warpList")) {
			
			sender.sendMessage(ChatColor.DARK_GREEN + "Warps esistenti:");
			sender.sendMessage("§a" + Joiner.on("§8, §a").join(PvPGames.getWarpsFile().getWarps().keySet()));
			return;
		}
		
		sender.sendMessage(ChatColor.RED + "Comando sconosciuto. Scrivi \"/pvpgames\" per i comandi.");
	}

}
