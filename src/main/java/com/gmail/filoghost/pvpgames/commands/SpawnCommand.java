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
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import wild.api.WildCommons;
import wild.api.command.CommandFramework;
import wild.api.command.SilentCommand;
import wild.api.command.SilentCommandHandler;

import com.gmail.filoghost.pvpgames.PvPGames;
import com.gmail.filoghost.pvpgames.hud.sidebar.SidebarManager;
import com.gmail.filoghost.pvpgames.player.PvPGamer;

public class SpawnCommand extends CommandFramework implements SilentCommandHandler {

	public SpawnCommand() {
		super(PvPGames.getInstance(), "spawn");
		SilentCommand.register(PvPGames.getInstance(), "spawn", this);
	}

	@Override
	public void execute(CommandSender sender, String label, String[] args) {
		
		PvPGamer pvpGamer = PvPGames.getPvPGamer(CommandValidate.getPlayerSender(sender));
		
		if (pvpGamer.getMode() != null) {
			
			boolean needsConfirm;
			if (args.length > 0 && args[0].equalsIgnoreCase("confirm")) {
				needsConfirm = false;
			} else if (pvpGamer.getCurrentKillstreak() > 0 || (pvpGamer.getKit() != null && pvpGamer.getKit().getCoins() > 0)) { // Se ha una killstreak o un kit a pagamento
				needsConfirm = true;
			} else {
				needsConfirm = false;
			}
			
			if (!needsConfirm) {
				pvpGamer.setKit(null);
				pvpGamer.setCurrentKillstreak(0);
				SidebarManager.setKillStreak(pvpGamer.getPlayer(), 0);
				
				pvpGamer.getPlayer().teleport(pvpGamer.getMode().getWarpLocation());
				pvpGamer.cleanInventoryAndPotions(GameMode.SURVIVAL);
				pvpGamer.giveSpawnStuff();
				pvpGamer.sendMessage(ChatColor.GREEN + "Sei tornato allo spawn della modalità " + pvpGamer.getMode().getName() + ".");
			} else {
				pvpGamer.sendMessage("");
				pvpGamer.sendMessage(ChatColor.YELLOW + "Sei sicuro di voler tornare allo spawn?");
				pvpGamer.sendMessage(ChatColor.YELLOW + "Perderai la serie di uccisioni e/o gli oggetti del kit.");
				WildCommons.fancyMessage("Clicca qui per confermare.").color(ChatColor.RED).style(ChatColor.UNDERLINE).command("/spawn confirm").tooltip("Clicca per confermare.").send(pvpGamer.getPlayer());
				pvpGamer.sendMessage("");
			}
			
		} else {
			pvpGamer.getPlayer().teleport(PvPGames.getSpawn());
			pvpGamer.sendMessage(ChatColor.GREEN + "Sei tornato allo spawn globale.");
		}
	}
	
	@Override
	public void execute(Player player, String[] args) {
		try {
			execute(player, "spawn", args);
		} catch (ExecuteException e) {
			player.sendMessage(ChatColor.RED + e.getMessage());
		}
	}

}
