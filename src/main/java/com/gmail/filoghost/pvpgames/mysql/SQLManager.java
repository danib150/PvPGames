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
package com.gmail.filoghost.pvpgames.mysql;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.gmail.filoghost.pvpgames.player.Mode;
import com.gmail.filoghost.pvpgames.player.SavedKit;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import lombok.Cleanup;
import lombok.Getter;
import wild.api.mysql.MySQL;
import wild.api.mysql.SQLResult;

public class SQLManager {
	
	@Getter private static MySQL mysql;
	
	public static void connect(String host, int port, String database, String user, String pass) throws SQLException, ClassNotFoundException {
		mysql = new MySQL(host, port, database, user, pass);
		mysql.connect();
	}

	public static void checkConnection() throws SQLException {
		mysql.isConnectionValid();
	}
	
	public void close() {
		if (mysql != null) {
			mysql.close();
		}
	}
	
	public static void deletePlayerData(String playerName, Mode mode) throws SQLException {
		if (mode == null) {
			return;
		}
		
		mysql.preparedUpdate("DELETE FROM kitpvp_mode_" + mode.getId() + " WHERE " + SQLColumns.NAME + " = ?;", playerName);
	}
	
	public static boolean hasPlayerData(String playerName, Mode mode) throws SQLException {
		@Cleanup SQLResult result = mysql.preparedQuery("SELECT null FROM kitpvp_mode_" + mode.getId() + " WHERE " + SQLColumns.NAME + " = ?;", playerName);
		
		return result.next();
	}

	public static SQLPlayerData getPlayerData(String playerName, Mode mode) throws SQLException {
		@Cleanup SQLResult result = mysql.preparedQuery("SELECT * FROM kitpvp_mode_" + mode.getId() + " WHERE " + SQLColumns.NAME + " = ?;", playerName);
		
		if (result.next()) {
			return new SQLPlayerData(result.getInt(SQLColumns.EXP), result.getInt(SQLColumns.COINS), result.getInt(SQLColumns.KILLS), result.getInt(SQLColumns.DEATHS), result.getInt(SQLColumns.BEST_KILLSTREAK));
		}
		
		return new SQLPlayerData(0, 0, 0, 0, 0);
	}
	
	public static int getStat(String playerName, Mode mode, String column) throws SQLException {
		if (mode == null) {
			return 0;
		}
		
		@Cleanup SQLResult result = mysql.preparedQuery("SELECT * FROM kitpvp_mode_" + mode.getId() + " WHERE " + SQLColumns.NAME + " = ?;", playerName);
		
		if (result.next()) {
			return result.getInt(column);
		} else {
			return 0;
		}
	}
	
	public static void setStats(String playerName, Mode mode, int exp, int coins, int kills, int deaths, int bestKillStreak) throws SQLException {
		if (mode == null) {
			return;
		}

		mysql.preparedUpdate(
				"INSERT INTO kitpvp_mode_" + mode.getId() + " (" +
					SQLColumns.NAME + ", " +
					SQLColumns.EXP + ", " +
					SQLColumns.COINS + ", " +
					SQLColumns.KILLS + ", " +
					SQLColumns.DEATHS + ", " +
					SQLColumns.BEST_KILLSTREAK +
				") VALUES(?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
					SQLColumns.EXP + " = ?, " +
					SQLColumns.COINS + " = ?, " +
					SQLColumns.KILLS + " = ?, " +
					SQLColumns.DEATHS + " = ?, " +
					SQLColumns.BEST_KILLSTREAK + " = ?;",
				playerName, exp, coins, kills, deaths, bestKillStreak,
							exp, coins, kills, deaths, bestKillStreak);
	}

	public static List<SQLStat> getTop(Mode mode, String stat, int limit) throws SQLException {
		if (mode == null) {
			return Lists.newArrayList();
		}
		
		@Cleanup SQLResult result = mysql.preparedQuery("SELECT * FROM kitpvp_mode_" + mode.getId() + " ORDER BY " + stat + " DESC LIMIT " + limit + ";");
			
		List<SQLStat> stats = Lists.newArrayList();
		while (result.next()) {
			stats.add(new SQLStat(result.getString(SQLColumns.NAME), result.getInt(stat)));
		}
			
		return stats;
	}
	
	public static void setSavedKit(String playerName, String kitID, SavedKit savedKit) throws SQLException {
		String serializedItems = savedKit.serialize();
		
		mysql.preparedUpdate(
				"INSERT INTO kitpvp_savedkits (" +
					SQLColumns.NAME + ", " +
					SQLColumns.KIT_ID + ", " +
					SQLColumns.KIT_ITEMS +
				") VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE " +
					SQLColumns.KIT_ITEMS + " = ?;",
				playerName, kitID, serializedItems, serializedItems);
	}

	public static Map<String, SavedKit> getSavedKits(String playerName) throws SQLException {
		@Cleanup SQLResult result = mysql.preparedQuery("SELECT * FROM kitpvp_savedkits WHERE " + SQLColumns.NAME + " = ?;", playerName);
		Map<String, SavedKit> savedKits = Maps.newHashMap();
		
		while (result.next()) {
			String id = result.getString(SQLColumns.KIT_ID);
			String itemsString = result.getString(SQLColumns.KIT_ITEMS);
			
			List<Integer> items = Lists.newArrayList();
			for (String itemString : itemsString.split(",")) {
				try {
					items.add(Integer.parseInt(itemString.trim()));
				} catch (NumberFormatException e) {
					throw new SQLException("Could not parse kit items: " + itemsString + " (error on " + itemString + ")");
				}
			}
			
			savedKits.put(id, new SavedKit(items));
		}
		
		return savedKits;
	}
}
