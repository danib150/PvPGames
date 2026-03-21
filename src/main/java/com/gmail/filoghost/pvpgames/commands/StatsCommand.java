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
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import wild.api.command.CommandFramework;

import com.gmail.filoghost.pvpgames.Perms;
import com.gmail.filoghost.pvpgames.PvPGames;
import com.gmail.filoghost.pvpgames.mysql.SQLManager;
import com.gmail.filoghost.pvpgames.mysql.SQLPlayerData;
import com.gmail.filoghost.pvpgames.player.ExpManager;
import com.gmail.filoghost.pvpgames.player.Mode;
import com.gmail.filoghost.pvpgames.player.PvPGamer;
import com.gmail.filoghost.pvpgames.player.ExpManager.LevelInfo;
import com.gmail.filoghost.pvpgames.utils.PlayerUtils;

public class StatsCommand extends CommandFramework {
	
	private static final DecimalFormat format = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.ENGLISH));
	
	public StatsCommand() {
		super(PvPGames.getInstance(), "stats");
		// stats <modalità> [user]
	}

	@Override
	public void execute(final CommandSender sender, String label, String[] args) {
		
		Mode mode = null;
		
		if (args.length >= 1) {
			mode = PvPGames.getModesFile().matchOrGetByChatTag(args[0]);
		} else if (sender instanceof Player) {
			mode = PvPGames.getPvPGamer((Player) sender).getMode();
		}
		
		if (mode == null) {
			if (args.length >= 1) {
				// Se ha specificato una modalità
				sender.sendMessage(ChatColor.RED + "Modalità \"" + args[0] + "\" non trovata. Scrivi /modes per un elenco delle modalità.");
			} else {
				
				sender.sendMessage(ChatColor.RED + "Specifica una modalità con: /stats <modalità>,");
				sender.sendMessage(ChatColor.RED + "oppure entra in una modalità. Scrivi /modes per un elenco delle modalità.");
			}
			return;
		}

		final String name;
		final boolean isSelf;

		if (args.length > 1) {
			CommandValidate.isTrue(sender.hasPermission(Perms.VIEW_OTHERS_STATS), "Non puoi vedere le statistiche degli altri.");
			
			name = args[1];
			isSelf = false;
			
			
		} else {
			name = CommandValidate.getPlayerSender(sender).getName();
			isSelf = true;
		}
		
		final Mode finalMode = mode;
		final PvPGamer pvpGamer = PvPGames.getPvPGamer(name);
		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				
				try {
					if (pvpGamer != null) {
						pvpGamer.flushStats(false);
					}
					
					SQLPlayerData data;
					
					if (SQLManager.hasPlayerData(name, finalMode)) {
						data = SQLManager.getPlayerData(name, finalMode);

					} else {
						if (isSelf) {
							data = new SQLPlayerData(0, 0, 0, 0, 0);
						} else {
							sender.sendMessage(ChatColor.RED + "Quel giocatore non ha mai giocato qui!");
							return;
						}
					}
					
					sender.sendMessage("");
					if (isSelf) {
						sender.sendMessage(ChatColor.GOLD + "----- Le tue statistiche (" + finalMode.getName() + ") -----");
					} else {
						sender.sendMessage(ChatColor.GOLD + "----- Statistiche di " + name + " (" + finalMode.getName() + ") -----");
					}
					LevelInfo levelInfo = ExpManager.getCurrentLevelInfo(data.getExp());
					sender.sendMessage(ChatColor.GRAY + "Livello: " + ChatColor.YELLOW + levelInfo.getLevel() + ChatColor.GRAY + (levelInfo.isMax() ? " §8[§7Livello masssimo§8]" : " §8[§f" + levelInfo.getCurrentLevelExp() + "§7/§f" + levelInfo.getTotalExpForNextLevel() + "§7 esperienza§8]"));
					if (finalMode.isEnableCoins()) {
						sender.sendMessage(ChatColor.GRAY + "Coins: " + ChatColor.YELLOW + data.getCoins());
					}
					sender.sendMessage("");
					sender.sendMessage(ChatColor.GRAY + "Uccisioni: " + ChatColor.YELLOW + data.getKills());
					sender.sendMessage(ChatColor.GRAY + "Morti: " + ChatColor.YELLOW + data.getDeaths());
					
					double kdr = PlayerUtils.calculateKDR(data.getKills(), data.getDeaths());
					String result = Double.isInfinite(kdr) ? "-" : format.format(kdr);
					
					sender.sendMessage(ChatColor.GRAY + "Rapporto uccisioni/morti: " + ChatColor.YELLOW + result);
					sender.sendMessage(ChatColor.GRAY + "Miglior killstreak: " + ChatColor.YELLOW + data.getBestKillstreak());
					
				} catch (SQLException e) {
					e.printStackTrace();
					sender.sendMessage(ChatColor.RED + "Errore nel database, informa lo staff se persiste.");
				}
				
			}
		}.runTaskAsynchronously(PvPGames.getInstance());

	}

}
