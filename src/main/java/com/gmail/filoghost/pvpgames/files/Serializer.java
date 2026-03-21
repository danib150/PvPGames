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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import com.gmail.filoghost.pvpgames.player.Mode.SpawnPoint;

public class Serializer {
	
	private static DecimalFormat decimalFormat;
	static {
		// More precision is not needed at all.
		decimalFormat = new DecimalFormat("0.000");
		DecimalFormatSymbols formatSymbols = decimalFormat.getDecimalFormatSymbols();
		formatSymbols.setDecimalSeparator('.');
		decimalFormat.setDecimalFormatSymbols(formatSymbols);
	}

	public static Location locationFromString(String input) throws IllegalArgumentException {
		if (input == null) {
			throw new IllegalArgumentException("input was null");
		}
		
		String[] parts = input.split(",");
		
		if (parts.length != 6) {
			throw new IllegalArgumentException("location parts are not 6");
		}
		
		try {
			double x = Double.parseDouble(parts[1].replace(" ", ""));
			double y = Double.parseDouble(parts[2].replace(" ", ""));
			double z = Double.parseDouble(parts[3].replace(" ", ""));
			float yaw = (float) Double.parseDouble(parts[4].replace(" ", ""));
			float pitch = (float) Double.parseDouble(parts[5].replace(" ", ""));
		
			World world = Bukkit.getWorld(parts[0].trim());
			if (world == null) {
				throw new IllegalArgumentException("world not found");
			}
			
			return new Location(world, x, y, z, yaw, pitch);
			
		} catch (NumberFormatException ex) {
			throw new IllegalArgumentException("invalid numbers format");
		}
	}
	
	public static String locationToString(Location loc) {
		return (loc.getWorld().getName() + ", " + decimalFormat.format(loc.getX()) + ", " + decimalFormat.format(loc.getY()) + ", " + decimalFormat.format(loc.getZ()) + ", " + decimalFormat.format(loc.getYaw()) + ", " + decimalFormat.format(loc.getPitch()));
	}
	
	public static SpawnPoint spawnPointFromString(String input) throws IllegalArgumentException {
		if (input == null) {
			throw new IllegalArgumentException("input was null");
		}
		
		String[] parts = input.split(",");
		
		if (parts.length != 5) {
			throw new IllegalArgumentException("spawn point parts are not 5");
		}
		
		try {
			int radius = Integer.parseInt(parts[0].replace(" ", ""));
			int x = Integer.parseInt(parts[2].replace(" ", ""));
			int y = Integer.parseInt(parts[3].replace(" ", ""));
			int z = Integer.parseInt(parts[4].replace(" ", ""));
		
			World world = Bukkit.getWorld(parts[1].trim());
			if (world == null) {
				throw new IllegalArgumentException("world not found");
			}
			
			return new SpawnPoint(world, x, y, z, radius);
			
		} catch (NumberFormatException ex) {
			throw new IllegalArgumentException("invalid numbers format");
		}
	}
	
	public static String spawnPointToString(SpawnPoint point) {
		return (point.getRadius() + ", " + point.getWorld().getName() + ", " + point.getX() + ", " + point.getY() + ", " + point.getZ());
	}
}
