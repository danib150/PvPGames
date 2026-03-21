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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

import com.gmail.filoghost.pvpgames.PvPGames;

import lombok.Getter;
import wild.api.util.CaseInsensitiveMap;

public class NoRepeatKillManager {

	private static Map<String, RecentKills> playersMap = new CaseInsensitiveMap<>();
	
	public static RecentKills getRecentKills(Player player) {
		RecentKills recent = playersMap.get(player.getName());
		if (recent == null) {
			recent = new RecentKills(PvPGames.getSettings().repeatKill_maxRecent, PvPGames.getSettings().repeatKill_memorySize);
			playersMap.put(player.getName(), recent);
		}
		
		return recent;
	}
	
	public static void cleanupOld(long timespan) {
		long now = System.currentTimeMillis();
		
		for (Iterator<RecentKills> iterator = playersMap.values().iterator(); iterator.hasNext();) {
			RecentKills next = iterator.next();
			
			if (now - next.getLastUpdate() > timespan) {
				iterator.remove();
			}
		}
	}
	
	public static class RecentKills {
		
		private int maxRecent;
		private int memorySize;
		private List<String> recentlyKilledPlayers;
		@Getter
		private long lastUpdate;

		public RecentKills(int maxRecent, int memorySize) {
			this.maxRecent = maxRecent;
			this.memorySize = memorySize;
			recentlyKilledPlayers = new ArrayList<String>();
		}
		
		public void addKill(String victim) {
			recentlyKilledPlayers.add(0, victim.toLowerCase());
			while (recentlyKilledPlayers.size() > memorySize) {
				recentlyKilledPlayers.remove(recentlyKilledPlayers.size() - 1);
			}
			
			lastUpdate = System.currentTimeMillis();
		}
		
		public boolean shouldCount(String victim) {
			victim = victim.toLowerCase();
			int count = 0;
			
			for (int i = 0; i < recentlyKilledPlayers.size(); i++) {
				if (recentlyKilledPlayers.get(i).equals(victim)) {
					count++;
				}
			}
			
			lastUpdate = System.currentTimeMillis();
			return count < maxRecent;
		}
		
		
	}

}
