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

import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import wild.api.command.CommandFramework;

import com.gmail.filoghost.pvpgames.Perms;
import com.gmail.filoghost.pvpgames.PvPGames;
import com.gmail.filoghost.pvpgames.mysql.SQLColumns;
import com.gmail.filoghost.pvpgames.mysql.SQLManager;
import com.gmail.filoghost.pvpgames.mysql.SQLTask;
import com.gmail.filoghost.pvpgames.player.Mode;
import com.gmail.filoghost.pvpgames.player.PvPGamer;
import com.gmail.filoghost.pvpgames.tasks.SendRankingTask;
import com.gmail.filoghost.pvpgames.tasks.SendTopExpTask;

public class ClassificaCommand extends CommandFramework {
	
	public ClassificaCommand() {
		super(PvPGames.getInstance(), "classifica");
	}

	@SuppressWarnings("deprecation")
	@Override
	public void execute(final CommandSender sender, String label, String[] args) {
		
		if (args.length == 0) {
			sender.sendMessage(ChatColor.GOLD + "----- Comandi classifica -----");
			sender.sendMessage(ChatColor.YELLOW + "/classifica exp [modalità]");
			sender.sendMessage(ChatColor.YELLOW + "/classifica uccisioni [modalità]");
			sender.sendMessage(ChatColor.YELLOW + "/classifica killstreak [modalità]");
			sender.sendMessage(ChatColor.YELLOW + "/stats " + ChatColor.GRAY + "- Le tue statistiche");
			if (sender.hasPermission(Perms.DELETE_USER)) {
				sender.sendMessage(ChatColor.RED + "(Nascosto) " + ChatColor.YELLOW + "/classifica delete <utente>");
			}
			sender.sendMessage("");
			return;
		}
		
		if (args[0].equalsIgnoreCase("uccisioni")) {
			
			Mode mode = null;
			
			if (args.length >= 2) {
				mode = PvPGames.getModesFile().matchOrGetByChatTag(args[1]);
			} else if (sender instanceof Player) {
				mode = PvPGames.getPvPGamer((Player) sender).getMode();
			}
			
			if (mode == null) {
				if (args.length >= 2) {
					// Se ha specificato una modalità
					sender.sendMessage(ChatColor.RED + "Modalità \"" + args[1] + "\" non trovata. Scrivi /modes per un elenco delle modalità.");
				} else {
					
					sender.sendMessage(ChatColor.RED + "Specifica una modalità con: /classifica uccisioni <modalità>,");
					sender.sendMessage(ChatColor.RED + "oppure entra in una modalità. Scrivi /modes per un elenco delle modalità.");
				}
				return;
			}
			
			Bukkit.getScheduler().scheduleAsyncDelayedTask(PvPGames.getInstance(), new SendRankingTask(sender, mode, SQLColumns.KILLS, "uccisioni"));
			return;
		}
		
		if (args[0].equalsIgnoreCase("killstreak")) {
			
			Mode mode = null;
			
			if (args.length >= 2) {
				mode = PvPGames.getModesFile().matchOrGetByChatTag(args[1]);
			} else if (sender instanceof Player) {
				mode = PvPGames.getPvPGamer((Player) sender).getMode();
			}
			
			if (mode == null) {
				if (args.length >= 2) {
					// Se ha specificato una modalità
					sender.sendMessage(ChatColor.RED + "Modalità \"" + args[1] + "\" non trovata. Scrivi /modes per un elenco delle modalità.");
				} else {
					
					sender.sendMessage(ChatColor.RED + "Specifica una modalità con: /classifica uccisioni <modalità>,");
					sender.sendMessage(ChatColor.RED + "oppure entra in una modalità. Scrivi /modes per un elenco delle modalità.");
				}
				return;
			}
			
			Bukkit.getScheduler().scheduleAsyncDelayedTask(PvPGames.getInstance(), new SendRankingTask(sender, mode, SQLColumns.BEST_KILLSTREAK, "migliori killstreak"));
			return;
		}
		
		if (args[0].equalsIgnoreCase("exp")) {
			
			Mode mode;
			
			if (args.length >= 2) {
				mode = PvPGames.getModesFile().matchOrGetByChatTag(args[1]);
			} else if (sender instanceof Player) {
				mode = PvPGames.getPvPGamer((Player) sender).getMode();
			} else {
				sender.sendMessage(ChatColor.RED + "Devi specificare una modalità.");
				return;
			}
			
			if (mode == null) {
				if (args.length >= 2) {
					// Se ha specificato una modalità
					sender.sendMessage(ChatColor.RED + "Modalità \"" + args[1] + "\" non trovata. Scrivi /modes per un elenco delle modalità.");
				} else {
					
					sender.sendMessage(ChatColor.RED + "Specifica una modalità con: /classifica uccisioni <modalità>,");
					sender.sendMessage(ChatColor.RED + "oppure entra in una modalità. Scrivi /modes per un elenco delle modalità.");
				}
				return;
			}
			
			Bukkit.getScheduler().scheduleAsyncDelayedTask(PvPGames.getInstance(), new SendTopExpTask(sender, mode));
			return;
		}
		
		
		if (args[0].equalsIgnoreCase("delete")) {
			CommandValidate.isTrue(sender.hasPermission(Perms.DELETE_USER), "Non puoi usare questo comando.");
			CommandValidate.minLength(args, 2, "Utilizzo: /classifica delete <utente>");
			final String userToDelete = args[1];
			
			sender.sendMessage(ChatColor.GRAY + "Per favore attendi...");
			new SQLTask() {
				@Override
				public void execute() throws SQLException {
					
					int modesReset = 0;
					
					for (Mode mode : PvPGames.getModesFile().getAllModes()) {
						if (SQLManager.hasPlayerData(userToDelete, mode)) {
							SQLManager.deletePlayerData(userToDelete, mode);
							modesReset++;
						}
					}
					
					if (modesReset > 0) {
						sender.sendMessage(ChatColor.GREEN + "Resettate le statistiche di " + userToDelete + " in " + modesReset + " modalità.");
					} else {
						sender.sendMessage(ChatColor.RED + "Non sono state trovate statistiche di " + userToDelete + " in nessuna modalità.");
					}
				}
			}.submitAsync(sender);
			return;
		}
		
		
		if (args[0].equalsIgnoreCase("reset") && sender instanceof ConsoleCommandSender) {
			sender.sendMessage(ChatColor.GREEN + "Pulizia uccisioni morti e coins...");
			
			for (PvPGamer gamer : PvPGames.getAllGamersUnsafe()) {
				gamer.resetCoinsKillsDeathsBestKillstreak();
			}
			
			new SQLTask() {
				@Override
				public void execute() throws SQLException {
					
					for (Mode mode : PvPGames.getModesFile().getAllModes()) {
						SQLManager.getMysql().update("UPDATE kitpvp_mode_" + mode.getId() + " SET " +
								SQLColumns.COINS + " = 0, " +
								SQLColumns.KILLS + " = 0, " +
								SQLColumns.DEATHS + " = 0, " +
								SQLColumns.BEST_KILLSTREAK + " = 0" +
								";"
						);
					}
					sender.sendMessage(ChatColor.GREEN + "Finita pulizia!");
				}
			}.submitAsync(sender);
			return;
		}
		
		sender.sendMessage(ChatColor.RED + "Comando sconosciuto. Scrivi \"/classifica\" per i comandi.");
	}

}
