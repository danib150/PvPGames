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
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import com.gmail.filoghost.pvpgames.PvPGames;
import com.gmail.filoghost.pvpgames.mysql.SQLManager;
import com.gmail.filoghost.pvpgames.mysql.SQLTask;
import com.gmail.filoghost.pvpgames.player.Kit;
import com.gmail.filoghost.pvpgames.player.PvPGamer;
import com.gmail.filoghost.pvpgames.player.SavedKit;
import com.gmail.filoghost.pvpgames.utils.ItemAndPosition;
import com.google.common.collect.Lists;

import wild.api.command.CommandFramework;

public class SaveCommand extends CommandFramework {

	public SaveCommand() {
		super(PvPGames.getInstance(), "save");
	}

	@Override
	public void execute(CommandSender sender, String label, String[] args) {
		
		PvPGamer pvpGamer = PvPGames.getPvPGamer(CommandValidate.getPlayerSender(sender));
		Kit kit = pvpGamer.getKit();
		
		CommandValidate.notNull(kit, "Non hai un kit equipaggiato al momento.");
		
		List<Integer> kitItemsPosition = Lists.newArrayList();
		List<ItemAndPosition> kitItems = kit.getItems();
		ItemStack[] inventoryContent = pvpGamer.getPlayer().getInventory().getContents();
		boolean[] excludedIndexesBitmap = new boolean[inventoryContent.length];
		
		for (int i = 0; i < kitItems.size(); i++) {
			ItemStack item = kitItems.get(i).getItem();
			int position = indexOf(inventoryContent, item, excludedIndexesBitmap);

			CommandValidate.isTrue(position >= 0, "Puoi usare questo comando solo prima di utilizzare gli oggetti.");
			excludedIndexesBitmap[position] = true;
			kitItemsPosition.add(i, position);
		}
		
		SavedKit savedKit = new SavedKit(kitItemsPosition);
		
		new SQLTask() {
			
			@Override
			public void execute() throws SQLException {
				SQLManager.setSavedKit(pvpGamer.getName(), kit.getId(), savedKit);
				pvpGamer.updateSavedKit(kit.getId(), savedKit);
				pvpGamer.sendMessage(ChatColor.GREEN + "Posizione dell'inventario salvata.");
			}
			
		}.submitAsync(sender);
	}

	private int indexOf(ItemStack[] inventory, ItemStack item, boolean[] excludedIndexesBitmap) {
		for (int i = 0; i < inventory.length; i++) {
			if (excludedIndexesBitmap[i]) {
				continue;
			}
			
			if (inventory[i] != null && inventory[i].equals(item)) {
				return i;
			}
		}
		
		return -1;
	}
	
	
}
