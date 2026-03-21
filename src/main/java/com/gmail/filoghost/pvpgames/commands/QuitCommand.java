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

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import wild.api.command.CommandFramework;
import wild.api.command.SilentCommand;
import wild.api.command.SilentCommandHandler;
import wild.api.sound.EasySound;
import com.gmail.filoghost.pvpgames.PvPGames;
import com.gmail.filoghost.pvpgames.player.Mode;
import com.gmail.filoghost.pvpgames.player.PvPGamer;

public class QuitCommand extends CommandFramework implements SilentCommandHandler {

	public QuitCommand() {
		super(PvPGames.getInstance(), "quit");
		SilentCommand.register(PvPGames.getInstance(), "quit", this);
	}

	@Override
	public void execute(CommandSender sender, String label, String[] args) {
		
		PvPGamer pvpGamer = PvPGames.getPvPGamer(CommandValidate.getPlayerSender(sender));
		
		// Li porta semplicemente allo spawn globale
		if (pvpGamer.getMode() == null) {
			pvpGamer.getPlayer().teleport(PvPGames.getSpawn());
			pvpGamer.sendMessage(ChatColor.GREEN + "Sei tornato allo spawn globale.");
			return;
		}
		
		Mode oldMode = pvpGamer.getMode();
		
		pvpGamer.setMode(null);
		pvpGamer.cleanInventoryAndPotions(GameMode.SURVIVAL);
		pvpGamer.getPlayer().setLevel(0);
		pvpGamer.getPlayer().setExp(0);
		pvpGamer.getPlayer().teleport(PvPGames.getSpawn());
		pvpGamer.giveSpawnStuff();
		pvpGamer.getPlayer().updateInventory();
		
		pvpGamer.sendMessage(ChatColor.GREEN + "Sei uscito dalla modalità " + oldMode.getName() + ".");
		EasySound.quickPlay(pvpGamer.getPlayer(), Sound.BLOCK_NOTE_BASS);
	}
	
	@Override
	public void execute(Player player, String[] args) {
		try {
			execute(player, "quit", args);
		} catch (ExecuteException e) {
			player.sendMessage(ChatColor.RED + e.getMessage());
		}
	}
}
