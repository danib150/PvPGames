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
package com.gmail.filoghost.pvpgames.player;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

@Getter @Setter
public class KillstreakAction {
	
	private final int killstreak;
	private boolean repeat;
	private boolean executeAlways;
	private String broadcastMessage;
	private String privateMessage;
	private int coins;
	private boolean heal;
	private boolean giveKitAgain;
	
	private List<ItemStack> rewardItems;
	private List<String> stringRewardItems;
	
	private List<PotionEffect> rewardPotions;
	private List<String> stringRewardPotions;
	
	private List<RemovableItem> itemsToRemove;
	
	public KillstreakAction(int killstreak) {
		this.killstreak = killstreak;
	}
	
	public boolean shouldActivate(int currentKillstreak) {
		if (repeat) {
			return currentKillstreak % this.killstreak == 0;
		} else {
			return currentKillstreak == this.killstreak;
		}
	}
	
	public boolean isSingle() {
		return !repeat;
	}
	
	public void execute(PvPGamer pvpGamer, Player player, Mode mode, int currentKillstreak) {
		if (broadcastMessage != null) {
			String msg = broadcastMessage.replace("{mode}", mode.getChatTag()).replace("{killstreak}", String.valueOf(currentKillstreak)).replace("{player}", player.getName());
			
			for (PvPGamer gamer : mode.getCurrentPlayers()) {
				gamer.sendMessage(msg);
			}
		}
		if (privateMessage != null) {
			player.sendMessage(privateMessage.replace("{mode}", mode.getChatTag()).replace("{killstreak}", String.valueOf(currentKillstreak)).replace("{player}", player.getName()));
		}
		
		if (coins > 0 && mode.isEnableCoins()) {
			pvpGamer.getCoins().addAndGet(coins);
		}
		
		if (heal) {
			player.setHealth(((Damageable) player).getMaxHealth());
		}
		
		PlayerInventory inv = player.getInventory();
		
		if (giveKitAgain) {
			pvpGamer.getKit().apply(pvpGamer);
		}
		
		if (itemsToRemove != null) {
			for (int i = 0; i < inv.getSize(); i++) {
				ItemStack current = inv.getItem(i);
				if (current != null) {
					for (RemovableItem removable : itemsToRemove) {
						if (removable.matches(current)) {
							inv.setItem(i, null);
							break;
						}
					}
				}
			}
		}
		
		if (rewardItems != null) {
			for (ItemStack reward : rewardItems) {
				inv.addItem(reward);
			}
		}
		
		if (rewardPotions != null) {
			for (PotionEffect effect : rewardPotions) {
				player.addPotionEffect(effect, true);
			}
		}
	}
	
}
