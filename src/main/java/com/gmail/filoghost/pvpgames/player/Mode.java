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

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import com.gmail.filoghost.holographicmobs.api.ClickHandler;
import com.gmail.filoghost.holographicmobs.object.types.HologramSkeleton;
import com.gmail.filoghost.holographicmobs.object.types.HologramSlime;
import com.gmail.filoghost.pvpgames.PvPGames;
import com.gmail.filoghost.pvpgames.bridge.MobStatue;
import com.gmail.filoghost.pvpgames.files.Serializer;
import com.gmail.filoghost.pvpgames.utils.ConfigUtils;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import wild.api.WildCommons;
import wild.api.item.parsing.ItemParser;
import wild.api.item.parsing.ParserException;
import wild.api.item.parsing.PotionEffectParser;

@Getter
public class Mode {
	
	private String name;
	private String id;
	private String chatTag;
	
	@Setter
	private Location warpLocation;
	
	private String defaultKit;
	
	private boolean enableCoins;
	private boolean autoRespawn;
	
	private int killExp;
	private int killCoins;

	private List<SpawnPoint> spawnPoints;
	
	private boolean oneShotProjectile;
	private boolean oneShotMelee;

	private MobStatue joinMob;
	private MobStatue quitMob;

	private Set<PvPGamer> currentPlayers;
	
	private KillstreakAction[] killstreakActions;
	
	public Mode(ConfigurationSection section) {
		currentPlayers = Sets.newHashSet();
		
		id = section.getString("id", "unknown");
		name = section.getString("name", id);
		chatTag = WildCommons.color(section.getString("chat-tag", id));
		
		defaultKit = section.getString("default-kit");
		
		oneShotProjectile = section.getBoolean("one-shot.projectile");
		oneShotMelee = section.getBoolean("one-shot.melee");
		
		joinMob = new MobStatue();
		joinMob.setType(HologramSkeleton.class);
		joinMob.setHologramLines(Arrays.asList(
				WildCommons.color(PvPGames.getSettings().statues_modeCustomName).replace("[NAME]", name).replace("[ID]", id).split(Pattern.quote("\\n"))
				));
		
		joinMob.setClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(Player player) {
				PvPGames.getModeCommand().execute(player, new String[] {Mode.this.name});
			}
		});
		
		quitMob = new MobStatue();
		quitMob.setType(HologramSlime.class);
		quitMob.setHologramLines(Arrays.asList("" + ChatColor.RED + ChatColor.BOLD + "Cambia modalità"));
		
		quitMob.setClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(Player player) {
				PvPGames.getQuitCommand().execute(player, new String[0]);
			}
		});
		
		enableCoins = section.getBoolean("enable-coins", false);
		autoRespawn = section.getBoolean("auto-respawn", false);
		
		killExp = section.getInt("kill.exp", 10);
		killCoins = section.getInt("kill.coins", 10);
		
		if (section.isSet("warp-location")) {
			try {
				warpLocation = Serializer.locationFromString(section.getString("warp-location"));
			} catch (IllegalArgumentException e) {
				PvPGames.logPurple("Location non valida nella modalità " + name + "!");
			}
		}
		if (section.isSet("join-statue")) {
			try {
				Location loc = Serializer.locationFromString(section.getString("join-statue"));
				joinMob.setLocation(loc);
				
			} catch (IllegalArgumentException e) {
				PvPGames.logPurple("Location della join statue non valida nella modalità " + name + "!");
			}
		}
		
		if (section.isSet("quit-statue")) {
			try {
				Location loc = Serializer.locationFromString(section.getString("quit-statue"));
				quitMob.setLocation(loc);
				
			} catch (IllegalArgumentException e) {
				PvPGames.logPurple("Location della quit statue non valida nella modalità " + name + "!");
			}
		}
		
		spawnPoints = Lists.newArrayList();
		if (section.isList("spawn-points")) {
			for (String spawnpoint : section.getStringList("spawn-points")) {
				try {
					spawnPoints.add(Serializer.spawnPointFromString(spawnpoint));
				} catch (Exception e) {
					PvPGames.logPurple("Impossibile leggere uno spawnpoint (" + spawnpoint + ") non valido nella modalità " + name + ": " + e.getMessage());
				}
			}
		}
		
		if (section.isList("killstreak-actions")) {
			
			List<KillstreakAction> tempKillstreakActions = Lists.newArrayList();
			
			for (Object o : section.getList("killstreak-actions")) {
				if (o instanceof Map) {
					Map<?, ?> ksActionMap = (Map<?, ?>) o;
					
					int killstreak = ConfigUtils.getInt(ksActionMap, "killstreak");
					
					if (killstreak <= 0) {
						PvPGames.logPurple("Killstreak action minore o uguale a zero, o non settata, nella modalità " + name);
						continue;
					}
					
					KillstreakAction action = new KillstreakAction(killstreak);
					
					action.setRepeat(ConfigUtils.getBoolean(ksActionMap, "repeat"));
					action.setGiveKitAgain(ConfigUtils.getBoolean(ksActionMap, "restore-kit"));
					action.setExecuteAlways(ConfigUtils.getBoolean(ksActionMap, "execute-always"));
					action.setBroadcastMessage(WildCommons.color(ConfigUtils.nullifyIfEmpty(ConfigUtils.getString(ksActionMap, "broadcast-message"))));
					action.setPrivateMessage(WildCommons.color(ConfigUtils.nullifyIfEmpty(ConfigUtils.getString(ksActionMap, "private-message"))));
					action.setHeal(ConfigUtils.getBoolean(ksActionMap, "heal"));
					action.setCoins(ConfigUtils.getInt(ksActionMap, "coins"));
					
					if (ksActionMap.get("clear") instanceof List) {
						List<RemovableItem> itemsToRemove = Lists.newArrayList();
						List<String> removableItemStrings = ConfigUtils.filter((List<?>) ksActionMap.get("clear"), String.class);
						
						for (String removableItemString : removableItemStrings) {
							try {
								RemovableItem item = RemovableItem.deserialize(removableItemString);
								itemsToRemove.add(item);
							} catch (ParserException e) {
								PvPGames.logPurple("Oggetto da rimuovere (clear) (" + removableItemString + ") non valido nella modalità " + name + ": " + e.getMessage());
							}
						}
						
						if (!itemsToRemove.isEmpty()) {
							action.setItemsToRemove(itemsToRemove);
						}
					}
					
					if (ksActionMap.get("potions") instanceof List) {
						
						List<PotionEffect> rewardEffects = Lists.newArrayList();
						List<String> stringRewardEffects = ConfigUtils.filter((List<?>) ksActionMap.get("potions"), String.class);
						
						for (String effect : stringRewardEffects) {
							try {
								PotionEffect potionEffect = PotionEffectParser.parse(effect, true);
								rewardEffects.add(potionEffect);
							} catch (ParserException e) {
								PvPGames.logPurple("Effetto di una killstreak (" + effect + ") non valido nella modalità " + name + ": " + e.getMessage());
							}
						}
						
						if (!rewardEffects.isEmpty()) {
							action.setRewardPotions(rewardEffects);
							action.setStringRewardPotions(stringRewardEffects);
						}
					}
					
					if (ksActionMap.get("items") instanceof List) {
						
						List<ItemStack> rewardItems = Lists.newArrayList();
						List<String> stringRewardItems = ConfigUtils.filter((List<?>) ksActionMap.get("items"), String.class);
						
						for (String itemString : stringRewardItems) {
							try {
								ItemStack itemStack = ItemParser.parse(itemString);
								rewardItems.add(itemStack);
							} catch (ParserException e) {
								PvPGames.logPurple("Item di una killstreak (" + itemString + ") non valido nella modalità " + name + ": " + e.getMessage());
							}
						}
						
						if (!rewardItems.isEmpty()) {
							action.setRewardItems(rewardItems);
							action.setStringRewardItems(stringRewardItems);
						}
					}
					
					tempKillstreakActions.add(action);
				}
			}
			
			killstreakActions = tempKillstreakActions.toArray(new KillstreakAction[0]);
			
			
			// Così si itera prima sulle più grandi
			Arrays.sort(killstreakActions, new Comparator<KillstreakAction>() {

				@Override
				public int compare(KillstreakAction o1, KillstreakAction o2) {
					return o2.getKillstreak() - o1.getKillstreak();
				}
			});
		}
		
		updateJoinMob();
		updateQuitMob();
	}
	
	public void save(ConfigurationSection section) {
		
		section.set("id", id);
		section.set("name", name);
		section.set("chat-tag", chatTag.replace("§", "&"));
		
		if (defaultKit != null) {
			section.set("default-kit", defaultKit);
		}
		
		section.set("enable-coins", enableCoins);
		
		if (oneShotMelee) {
			section.set("one-shot.melee", true);
		}
		if (oneShotProjectile) {
			section.set("one-shot.projectile", true);
		}
		
		section.set("kill.exp", killExp);
		section.set("kill.coins", killCoins);
		
		if (spawnPoints != null && !spawnPoints.isEmpty()) {
			List<String> serializedSpawnPoints = Lists.newArrayList();
			for (SpawnPoint sp : spawnPoints) {
				serializedSpawnPoints.add(Serializer.spawnPointToString(sp));
			}
			section.set("spawn-points", serializedSpawnPoints);
		}
		
		if (killstreakActions != null && killstreakActions.length > 0) {
			List<Object> list = Lists.newArrayList();
			
			for (KillstreakAction action : killstreakActions) {
				Map<String, Object> actionSection = Maps.newLinkedHashMap();
				actionSection.put("killstreak", action.getKillstreak());
				actionSection.put("repeat", action.isRepeat());
				actionSection.put("restore-kit", action.isGiveKitAgain());
				actionSection.put("execute-always", action.isExecuteAlways());
				if (action.getBroadcastMessage() != null) {
					actionSection.put("broadcast-message", ConfigUtils.toSaveableFormat(action.getBroadcastMessage()));
				}
				if (action.getPrivateMessage() != null) {
					actionSection.put("private-message", ConfigUtils.toSaveableFormat(action.getPrivateMessage()));
				}
				
				if (action.isHeal()) {
					actionSection.put("heal", action.isHeal());
				}
				
				if (action.getCoins() > 0) {
					actionSection.put("coins", action.getCoins());
				}
				
				if (action.getStringRewardItems() != null && !action.getStringRewardItems().isEmpty()) {
					actionSection.put("items", action.getStringRewardItems());
				}
				
				if (action.getStringRewardPotions() != null && !action.getStringRewardPotions().isEmpty()) {
					actionSection.put("potions", action.getStringRewardPotions());
				}
				
				if (action.getItemsToRemove() != null && !action.getItemsToRemove().isEmpty()) {
					actionSection.put("clear", Lists.transform(action.getItemsToRemove(), new Function<RemovableItem, String>() {

						@Override
						public String apply(RemovableItem arg) {
							return arg.serialize();
						}
						
					}));
				}
				
				list.add(actionSection);
			}
			
			section.set("killstreak-actions", list);
		}
		
		if (warpLocation != null) {
			section.set("warp-location", Serializer.locationToString(warpLocation));
		}
		if (joinMob.getLocation() != null) {
			section.set("join-statue", Serializer.locationToString(joinMob.getLocation()));
		}
		if (quitMob.getLocation() != null) {
			section.set("quit-statue", Serializer.locationToString(quitMob.getLocation()));
		}
	}

	
	public void setJoinMobLocation(Location loc) {
		loc.setPitch(0);
		if (Math.abs(loc.getYaw()) % 90.0f != 0.0f) {
			
			float yawNormalized = Math.round(loc.getYaw() / 90.0f) * 90.0f;
			loc.setYaw(yawNormalized);
		}
		
		loc.setX(Math.round(loc.getX() * 2.0) / 2.0);
		loc.setZ(Math.round(loc.getZ() * 2.0) / 2.0);
		
		this.joinMob.setLocation(loc);
	}
	
	
	public void setQuitMobLocation(Location loc) {
		loc.setPitch(0);
		if (Math.abs(loc.getYaw()) % 90.0f != 0.0f) {
			
			float yawNormalized = Math.round(loc.getYaw() / 90.0f) * 90.0f;
			loc.setYaw(yawNormalized);
		}
		
		loc.setX(Math.round(loc.getX() * 2.0) / 2.0);
		loc.setZ(Math.round(loc.getZ() * 2.0) / 2.0);
		
		this.quitMob.setLocation(loc);
	}
	
	
	public void updateJoinMob() {
		joinMob.update();
	}

	
	public void updateQuitMob() {
		quitMob.update();
		
		if (quitMob.getMob() instanceof HologramSlime) {
			((HologramSlime) quitMob.getMob()).setSize(1);
			quitMob.getMob().update();
		}
	}
	
	@Getter
	public static class SpawnPoint {
		
		private World world;
		private int x;
		private int y;
		private int z;
		private int radius;
		
		public SpawnPoint(World world, int x, int y, int z, int radius) {
			this.world = world;
			this.x = x;
			this.y = y;
			this.z = z;
			this.radius = radius;
		}
		
		public Location getRandomLocation() {
			double angle = Math.random() * Math.PI * 2;
			double r = Math.random() * radius;
			return new Location(world,
								x + 0.5 + Math.cos(angle)*r,
								y,
								z + 0.5 + Math.sin(angle)*r,
								(float) ((Math.random() * 360.0) - 180.0),
								1F);
		}
	}
	
	public void addPlayer(PvPGamer pvpGamer) {
		currentPlayers.add(pvpGamer);
	}

	public void removePlayer(PvPGamer pvpGamer) {
		currentPlayers.remove(pvpGamer);
	}

}
