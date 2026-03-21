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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import wild.api.command.CommandFramework;
import wild.api.command.SilentCommand;
import wild.api.command.SilentCommandHandler;
import wild.api.command.CommandFramework.Permission;
import wild.api.sound.EasySound;
import com.gmail.filoghost.pvpgames.PvPGames;
import com.gmail.filoghost.pvpgames.player.Mode;
import com.gmail.filoghost.pvpgames.player.PvPGamer;
import com.gmail.filoghost.pvpgames.player.Status;

@Permission("pvpgames.command.mode")
public class ModeCommand extends CommandFramework implements SilentCommandHandler {
	
	public ModeCommand() {
		super(PvPGames.getInstance(), "mode");
		SilentCommand.register(PvPGames.getInstance(), "mode", this);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void execute(CommandSender sender, String label, String[] args) {
		
		CommandValidate.minLength(args, 1, "Utilizzo comando: /" + label + " <modalità>");
		
		final PvPGamer pvpGamer = PvPGames.getPvPGamer(CommandValidate.getPlayerSender(sender));
		final Mode mode = PvPGames.getModesFile().matchOrGetByChatTag(args[0]);
		
		CommandValidate.notNull(mode, "Modalità \"" + args[0] + "\" non trovata.");
		CommandValidate.notNull(mode.getWarpLocation(), "Non è ancora stato impostato un punto di teletrasporto per quella modalità.");
		CommandValidate.isTrue(pvpGamer.getMode() == null, "Sei già in una modalità! Scrivi /quit per tornare allo spawn globale.");
		
		if (pvpGamer.getStatus() == Status.GAMER) {
		
			Bukkit.getScheduler().scheduleAsyncDelayedTask(PvPGames.getInstance(), new Runnable() {
				@Override
				public void run() {
					if (pvpGamer.setMode(mode)) {
						
						// Metodi bukkit sempre sync!
						Bukkit.getScheduler().scheduleSyncDelayedTask(PvPGames.getInstance(), new Runnable() {
							@Override
							public void run() {
								pvpGamer.cleanInventoryAndPotions(GameMode.SURVIVAL);
								pvpGamer.sendMessage(ChatColor.GRAY + "Hai scelto la modalità " + ChatColor.AQUA + ChatColor.BOLD + mode.getName() + ChatColor.GRAY + ".");
								pvpGamer.teleportDismount(mode.getWarpLocation());
								pvpGamer.giveSpawnStuff();
								EasySound.quickPlay(pvpGamer.getPlayer(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.4f);
							}
						});
					}
				}
			});
			
		} else if (pvpGamer.getStatus() == Status.SPECTATOR) {
			
			pvpGamer.getPlayer().teleport(mode.getWarpLocation());
			sender.sendMessage(ChatColor.GREEN + "Sei andato allo spawn della modalità " + mode.getName() + " come spettatore!");
			
		} else {
			sender.sendMessage(ChatColor.RED + "Errore interno!");
		}
		
	}

	@Override
	public void execute(Player player, String[] args) {
		try {
			execute(player, "mode", args);
		} catch (ExecuteException e) {
			player.sendMessage(ChatColor.RED + e.getMessage());
		}
	}
	
}
