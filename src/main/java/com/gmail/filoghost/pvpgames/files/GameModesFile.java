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
package com.gmail.filoghost.pvpgames.files;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import lombok.val;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.java.JavaPlugin;

import wild.api.config.PluginConfig;
import wild.api.util.CaseInsensitiveMap;

import com.gmail.filoghost.pvpgames.PvPGames;
import com.gmail.filoghost.pvpgames.player.Mode;
import com.google.common.collect.Maps;

public class GameModesFile {
	
	private Map<Mode, PluginConfig> configs;

	private Map<String, Mode> gamemodesNamesNoSpacesMap;
	private Map<String, Mode> gamemodesIdsMap;
	
	public GameModesFile(JavaPlugin plugin, String folder) throws IOException, InvalidConfigurationException {
		gamemodesNamesNoSpacesMap = new CaseInsensitiveMap<>();
		gamemodesIdsMap = new CaseInsensitiveMap<>();
		configs = Maps.newHashMap();
		
		File modesFolder = new File(plugin.getDataFolder(), folder);
		
		if (!modesFolder.isDirectory()) {
			modesFolder.mkdirs();
		}
		
		PluginConfig config;
		for (File modeFile : modesFolder.listFiles()) {
			
			if (modeFile.isFile() && modeFile.getName().toLowerCase().endsWith(".yml")) {
				try {
					config = new PluginConfig(plugin, modeFile);
				} catch(Exception e) {
					plugin.getLogger().severe("Impossibile caricare " + modeFile.getName());
					throw e;
				}
				Mode mode = new Mode(config);
				configs.put(mode, config);
				gamemodesNamesNoSpacesMap.put(mode.getName().replace(" ", ""), mode);
				gamemodesIdsMap.put(mode.getId(), mode);
				
			}
		}
	}
	
	public Collection<Mode> getAllModes() {
		return gamemodesIdsMap.values();
	}
	
	public boolean saveGamemodes() {
		
		boolean savedCorrectly = true;
		
		PluginConfig config;
		for (val entry : configs.entrySet()) {
			
			config = entry.getValue();
			
			for (String key : config.getKeys(false)) {
				config.set(key, null); // To clear
			}
			
			entry.getKey().save(config);
			
			try {
				config.save();
			} catch (IOException e) {
				savedCorrectly = false;
				e.printStackTrace();
				PvPGames.logPurple("Impossibile salvare su disco il file della modalità " + entry.getKey().getName() + "!");
			}
		}
		
		return savedCorrectly;
	}
	
	public Mode getModeByName(String name) {
		return gamemodesIdsMap.get(name);
	}
	
	public Mode getModeByIds(String id) {
		return gamemodesNamesNoSpacesMap.get(id);
	}
	
	public Mode matchMode(String id) {
		Mode mode = gamemodesIdsMap.get(id);
		
		if (mode != null) {
			return mode;
		} else {
			return gamemodesNamesNoSpacesMap.get(id.replace(" ", ""));
		}
	}
	
	public Mode getByChatTag(String chatTag) {
		chatTag = chatTag.toLowerCase();
		
		for (Mode mode : getAllModes()) {
			if (mode.getChatTag().toLowerCase().equals(chatTag)) {
				return mode;
			}
		}
		
		return null;
	}
	
	public Mode matchOrGetByChatTag(String chatTag) {
		Mode m = matchMode(chatTag);
		if (m != null) {
			return m;
		}
		
		return getByChatTag(chatTag);
	}

}
