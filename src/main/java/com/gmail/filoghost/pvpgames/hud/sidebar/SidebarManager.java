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
package com.gmail.filoghost.pvpgames.hud.sidebar;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import lombok.Getter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import wild.api.WildCommons;
import wild.api.WildConstants;

public class SidebarManager {

	@Getter
	private static Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
	
	@Getter
	private static Scoreboard emptyScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
	
	private static Objective side;
	
	private static Team killsTeam, levelTeam, coinsTeam, deathsTeam, kdrTeam, killstreakTeam, bestKillstreakTeam, modeTeam, playersAmountTeam;
	
	private static final String	SMALL_TITLE_PREFIX = "" + ChatColor.WHITE;
	private static final DecimalFormat format = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.ENGLISH));
	
	public static void initialize() {
		
		String sidebarTitle = "       " + ChatColor.WHITE + ChatColor.BOLD + ChatColor.UNDERLINE + "PvP Games" + ChatColor.RESET + "       ";
		
		safeRemoveObjective(emptyScoreboard.getObjective(DisplaySlot.SIDEBAR));
		safeRemoveObjective(emptyScoreboard.getObjective("info"));
		
		Objective staticSide = emptyScoreboard.registerNewObjective("info", "dummy");
		staticSide.setDisplayName(sidebarTitle);
		staticSide.setDisplaySlot(DisplaySlot.SIDEBAR);
		
		String spacer = setScore("                ", 9, staticSide);
		Team team = emptyScoreboard.registerNewTeam("spacer");
		team.addEntry(spacer);
		team.setSuffix("        ");
		String line1 = setScore(ChatColor.WHITE + "Scegli una ", 8, staticSide);
		team = emptyScoreboard.registerNewTeam("line1");
		team.addEntry(line1);
		team.setSuffix("modalità");
		String line2 = setScore(ChatColor.WHITE + "per vedere le ", 7, staticSide);
		team = emptyScoreboard.registerNewTeam("line2");
		team.addEntry(line2);
		team.setSuffix("tue");
		setScore(ChatColor.WHITE + "statistiche!", 6, staticSide);
		setScore(emptyLine(5), 5, staticSide);
		String line3 = setScore(ChatColor.WHITE + "Leggi il libro", 4, staticSide);
		team = emptyScoreboard.registerNewTeam("line3");
		team.addEntry(line3);
		team.setSuffix(" per ");
		String line4 = setScore(ChatColor.WHITE + "capire come ", 3, staticSide);
		team = emptyScoreboard.registerNewTeam("line4");
		team.addEntry(line4);
		team.setSuffix("giocare.");
		setScore(emptyLine(1), 1, staticSide);
		WildConstants.Messages.displayIP(emptyScoreboard, staticSide, 0);
		
		
		//------------ Vera Scoreboard -----------------
		
		// Rimuove gli obiettivi precedenti
		safeRemoveObjective(scoreboard.getObjective(DisplaySlot.SIDEBAR));
		safeRemoveObjective(scoreboard.getObjective("info"));
		
		side = scoreboard.registerNewObjective("info", "dummy");
		side.setDisplayName(sidebarTitle);
		side.setDisplaySlot(DisplaySlot.SIDEBAR);
		
		setScore(emptyLine(14), 14);
		//setScore(ChatColor.WHITE.toString() + "Modalità:", 13);
		String modeEntry = setScore(emptyLine(13) + ChatColor.AQUA, 13);
		String playersAmountEntry = setScore(SMALL_TITLE_PREFIX + "Giocatori: " + ChatColor.GRAY, 12);
		setScore(emptyLine(11), 11);
		
		String levelEntry = setScore(SMALL_TITLE_PREFIX + "Livello: " + ChatColor.GREEN, 10);
		String coinsEntry = setScore(emptyLine(9), 9);
		
		setScore(emptyLine(8), 8);
		
		String killsEntry = setScore(SMALL_TITLE_PREFIX + "Uccisioni: ", 7);
		String deathsEntry = setScore(SMALL_TITLE_PREFIX + "Morti: ", 6);
		String kdrEntry = setScore(SMALL_TITLE_PREFIX + "Rapporto: ", 5);
		setScore(emptyLine(4), 4);
		String killstreakEntry = setScore(SMALL_TITLE_PREFIX + "Killstreak: ", 3);
		String bestKillstreakEntry = setScore("Record: ", 2);
		setScore(emptyLine(1), 1);
		WildConstants.Messages.displayIP(scoreboard, side, 0);
		
		// Crea i team per i prefissi
		levelTeam = createSafeTeam("level", levelEntry);
		coinsTeam = createSafeTeam("coins", coinsEntry);
		killsTeam = createSafeTeam("kills", killsEntry);
		deathsTeam = createSafeTeam("deaths", deathsEntry);
		kdrTeam = createSafeTeam("kdr", kdrEntry);
		killstreakTeam = createSafeTeam("killstreak", killstreakEntry);
		bestKillstreakTeam = createSafeTeam("bestKs", bestKillstreakEntry);
		modeTeam = createSafeTeam("mode", modeEntry);
		playersAmountTeam = createSafeTeam("playersAmount", playersAmountEntry);
		
		killsTeam.setSuffix(ChatColor.GRAY + "?");
		deathsTeam.setSuffix(ChatColor.GRAY + "?");
		kdrTeam.setSuffix(ChatColor.GRAY + "?");
		killstreakTeam.setSuffix(ChatColor.GRAY + "?");
		bestKillstreakTeam.setSuffix(ChatColor.GRAY + "?");
		modeTeam.setSuffix("?");
		playersAmountTeam.setSuffix("?");
		levelTeam.setSuffix("?");
		coinsTeam.setSuffix("?");
		
		//bestKillstreakTeam.setPrefix(SMALL_TITLE_PREFIX + "Record ");
	}
	
	// -1 = disabilitati
	public static void setCoins(Player player, int coins) {
		if (player.getScoreboard() != scoreboard) {
			return;
		}
		
		try {
			
			if (coins == -1) {
				WildCommons.Unsafe.sendTeamPrefixSuffixChangePacket(player, coinsTeam, "", "");
			} else {
				WildCommons.Unsafe.sendTeamPrefixSuffixChangePacket(player, coinsTeam, ChatColor.WHITE + "Coins: ", ChatColor.GOLD + Integer.toString(coins));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void setLevel(Player player, int level) {
		if (player.getScoreboard() != scoreboard) {
			return;
		}
		
		try {
			WildCommons.Unsafe.sendTeamPrefixSuffixChangePacket(player, levelTeam, levelTeam.getPrefix(), String.valueOf(level));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void setKillStreak(Player player, int killstreak) {
		if (player.getScoreboard() != scoreboard) {
			return;
		}
		
		try {
			WildCommons.Unsafe.sendTeamPrefixSuffixChangePacket(player, killstreakTeam, "", ChatColor.GRAY.toString() + killstreak);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void setBestKillStreak(Player player, int bestKillstreak) {
		if (player.getScoreboard() != scoreboard) {
			return;
		}
		
		try {
			WildCommons.Unsafe.sendTeamPrefixSuffixChangePacket(player, bestKillstreakTeam, bestKillstreakTeam.getPrefix(), ChatColor.GRAY.toString() + bestKillstreak);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void setKills(Player player, int kills) {
		if (player.getScoreboard() != scoreboard) {
			return;
		}
		
		try {
			WildCommons.Unsafe.sendTeamPrefixSuffixChangePacket(player, killsTeam, "", ChatColor.GRAY.toString() + kills);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void setDeaths(Player player, int deaths) {
		if (player.getScoreboard() != scoreboard) {
			return;
		}
		
		try {
			WildCommons.Unsafe.sendTeamPrefixSuffixChangePacket(player, deathsTeam, "", ChatColor.GRAY.toString() + deaths);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void setKdr(Player player, double kdr) {
		if (player.getScoreboard() != scoreboard) {
			return;
		}
		
		try {
			
			String result;
			
			if (Double.isInfinite(kdr)) {
				result = "§8-"; // Infinito
			} else {
				result = format.format(kdr);
			}

			WildCommons.Unsafe.sendTeamPrefixSuffixChangePacket(player, kdrTeam, "", ChatColor.GRAY + result);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void setMode(Player player, String mode) {
		if (player.getScoreboard() != scoreboard) {
			return;
		}
		
		String prefix = ChatColor.AQUA + mode;
		String suffix = "";
		if (prefix.length() > 16) {
			suffix = ChatColor.AQUA + prefix.substring(16);
			prefix = prefix.substring(0, 16);
			
			if (suffix.length() > 16) {
				suffix = suffix.substring(0, 16);
			}
		}
		
		try {
			WildCommons.Unsafe.sendTeamPrefixSuffixChangePacket(player, modeTeam, prefix, suffix);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void setPlayersAmount(Player player, int playersAmount) {
		if (player.getScoreboard() != scoreboard) {
			return;
		}
		
		try {
			WildCommons.Unsafe.sendTeamPrefixSuffixChangePacket(player, playersAmountTeam, playersAmountTeam.getPrefix(), String.valueOf(playersAmount));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private static String emptyLine(int sideNumber) {
		if (sideNumber > 15 || sideNumber < 0) return "";
		return ChatColor.values()[sideNumber].toString();
	}
	
	private static Team createSafeTeam(String name, String member) {
		if (scoreboard.getTeam(name) != null) {
			scoreboard.getTeam(name).unregister();
		}
		
		Team t = scoreboard.registerNewTeam(name);
		t.addEntry(member);
		return t;
	}
	
	private static void safeRemoveObjective(Objective o) {
		if (o != null) o.unregister();
	}
	
	private static String setScore(String entry, int score) {
		side.getScore(entry).setScore(score);
		return entry;
	}
	
	private static String setScore(String entry, int score, Objective o) {
		o.getScore(entry).setScore(score);
		return entry;
	}
	
	public static void showSidebar(Player player) {
		player.setScoreboard(scoreboard);
	}
	
	public static void hideSidebar(Player player) {
		player.setScoreboard(emptyScoreboard);
	}
}
