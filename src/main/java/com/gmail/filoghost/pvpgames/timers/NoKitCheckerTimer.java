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
package com.gmail.filoghost.pvpgames.timers;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;

import wild.api.sound.EasySound;

import com.gmail.filoghost.pvpgames.PvPGames;
import com.gmail.filoghost.pvpgames.player.PvPGamer;
import com.gmail.filoghost.pvpgames.player.Status;

public class NoKitCheckerTimer extends TimerMaster {
	
	public NoKitCheckerTimer() {
		super(10L, 10L);
	}

	@Override
	public void run() {
		for (PvPGamer pvpGamer : PvPGames.getAllGamersUnsafe()) {
			
			if (pvpGamer.getStatus() == Status.GAMER && !pvpGamer.getPlayer().isDead() && pvpGamer.getMode() != null) {
				
				if (pvpGamer.getKit() == null && pvpGamer.getPlayer().getLocation().getY() < PvPGames.getSettings().pvpMaxHeight) {
					// Non può stare lì!
					Location warpLoc = pvpGamer.getMode().getWarpLocation();
					
					if (warpLoc != null && !equalsCoords(warpLoc, pvpGamer.getPlayer().getLocation())) {
						pvpGamer.getPlayer().teleport(warpLoc);
						pvpGamer.cleanInventoryAndPotions(GameMode.SURVIVAL);
						pvpGamer.giveSpawnStuff();
						EasySound.quickPlay(pvpGamer.getPlayer(), Sound.BLOCK_NOTE_BASS);
						pvpGamer.sendMessage(ChatColor.RED + "Non puoi stare in questa zona senza un kit.");
					}
				}
			}
			
		}
	}
	
	public boolean equalsCoords(Location loc1, Location loc2) {
		return loc1.getX() == loc2.getX() && loc1.getY() == loc2.getY() && loc1.getZ() == loc2.getZ();
	}
}
