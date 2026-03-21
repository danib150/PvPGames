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

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.gmail.filoghost.pvpgames.utils.Matcher;
import com.gmail.filoghost.pvpgames.utils.MathUtils;

import lombok.Setter;
import wild.api.item.parsing.ParserException;


public class RemovableItem {
	
	private Material material;
	@Setter private Short dataValue;
	
	public RemovableItem(Material material) {
		this.material = material;
	}
	
	public boolean matches(ItemStack item) {
		if (item.getType() == material) {
			if (dataValue != null) {
				return item.getDurability() == dataValue.shortValue();
			}
			
			return true;
		}
		
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public static RemovableItem deserialize(String input) throws ParserException {
		Material mat;
		String dataValueString = null;
		
		if (input.contains(":")) {
			String[] pieces = input.split(":");
			input = pieces[0];
			dataValueString = pieces[1];
		}
		
		if (MathUtils.isValidInteger(input)) {
			mat = Material.getMaterial(Integer.parseInt(input));
		} else {
			mat = Matcher.matchMaterial(input);
		}
		
		if (mat == null || mat == Material.AIR) {
			throw new ParserException("materiale non valido");
		}
		
		RemovableItem item = new RemovableItem(mat);
		if (dataValueString != null) {
			try {
				item.setDataValue(Short.parseShort(dataValueString));
			} catch (NumberFormatException e) {
				throw new ParserException("data value non valido");
			}
		}
		
		return item;
	}
	
	public String serialize() {
		if (dataValue != null) {
			return material.toString().toLowerCase().replace(" ", "") + ":" + dataValue.shortValue();
		} else {
			return material.toString().toLowerCase().replace(" ", "");
		}
	}

}
