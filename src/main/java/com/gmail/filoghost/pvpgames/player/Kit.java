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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

import com.gmail.filoghost.holographicmobs.api.ClickHandler;
import com.gmail.filoghost.holographicmobs.object.HologramEquippable;
import com.gmail.filoghost.holographicmobs.object.types.HologramZombie;
import com.gmail.filoghost.pvpgames.Perms;
import com.gmail.filoghost.pvpgames.PvPGames;
import com.gmail.filoghost.pvpgames.bridge.MobStatue;
import com.gmail.filoghost.pvpgames.files.Serializer;
import com.gmail.filoghost.pvpgames.hud.menu.ItemStackIcon;
import com.gmail.filoghost.pvpgames.utils.Format;
import com.gmail.filoghost.pvpgames.utils.ItemAndPosition;
import com.gmail.filoghost.pvpgames.utils.ItemStackWrapper;
import com.gmail.filoghost.pvpgames.utils.PlayerUtils;
import com.google.common.collect.Lists;

import lombok.Getter;
import wild.api.WildCommons;
import wild.api.bridges.CosmeticsBridge;
import wild.api.config.PluginConfig;
import wild.api.item.ItemBuilder;
import wild.api.item.parsing.ItemParser;
import wild.api.item.parsing.ItemParser.JsonReturner;
import wild.api.item.parsing.ParserException;
import wild.api.item.parsing.PotionEffectParser;
import wild.api.menu.Icon;
import wild.api.menu.IconMenu;
import wild.api.menu.StaticIcon;
import wild.api.sound.EasySound;

public class Kit {
	
	@Getter private		PluginConfig yaml;

	@Getter private 	String id;
	@Getter private 	String name;
	@Getter private 	String mode;
	@Getter private 	int requiredLevel;
	@Getter private 	int coins;
	@Getter private 	boolean vipOnly;
	
	private 			ItemStack helmet, chestplate, leggings, boots, shield;
	@Getter private 	List<ItemAndPosition> items;
	private 			List<PotionEffect> potionEffects;
	
	private 			MobStatue statue;
	@Getter private 	IconMenu showcaseMenu;
	

	public Kit(PluginConfig config) {
		statue = new MobStatue();
		statue.setType(HologramZombie.class);
		statue.setHologramLines(new ArrayList<String>());
		statue.setClickHandler(new ClickHandler() {

			@Override
			public void onClick(Player player) {
				PvPGamer gamer = PvPGames.getPvPGamer(player);
					
				if (gamer.getStatus() != Status.GAMER) {
					gamer.sendMessage(ChatColor.RED + "Gli spettatori non possono scegliere kit.");
					return;
				}
					
				if (gamer.getMode() == null) {
					gamer.sendMessage(ChatColor.RED + "Devi scegliere una modalità prima di scegliere un kit.");
					return;
				}
					
				if (!gamer.getMode().getId().equalsIgnoreCase(mode)) {
					gamer.sendMessage(ChatColor.RED + "Non puoi scegliere questo kit nella modalità " + gamer.getMode().getName() + ".");
					return;
				}
				
				if (requiredLevel > 0 && gamer.getLevel() < requiredLevel) {
					EasySound.quickPlay(player, Sound.BLOCK_NOTE_BASS);
					gamer.sendMessage(ChatColor.RED + "Devi essere almeno livello " + requiredLevel + " per questo kit.");
					return;
				}

				if (vipOnly && !player.hasPermission(Perms.VIP)) {
					EasySound.quickPlay(player, Sound.BLOCK_NOTE_BASS);
					gamer.sendMessage(ChatColor.RED + "Solo i VIP possono usare questo kit.");
					return;
				}
					
				if (gamer.getKit() != null && gamer.getKit() == Kit.this) {
					gamer.sendMessage(ChatColor.RED + "Hai già questo kit.");
					return;
				}
				
				if (gamer.getKit() != null && gamer.isPaidForKit()) {
					gamer.sendMessage(ChatColor.RED + "Hai già comprato un kit a pagamento.");
					return;
				}
				
				gamer.setPaidForKit(false);
				
				if (gamer.getMode().isEnableCoins() && coins > 0) {
					if (player.hasPermission(Perms.BYPASS_COINS)) {
						player.sendMessage(ChatColor.GRAY + "Hai bypassato il costo in Coins.");
					} else {
						if (gamer.getCoins().get() < coins) {
							EasySound.quickPlay(player, Sound.BLOCK_NOTE_BASS);
							gamer.sendMessage(ChatColor.RED + "Non hai abbastanza Coins per questo kit.");
							return;
						}
						
						gamer.detractCoins(coins);
						player.sendMessage(ChatColor.GOLD + "Hai speso " + coins + " Coins.");
						gamer.setPaidForKit(true);
					}
				}
					
				gamer.setKit(Kit.this);
				player.sendMessage(ChatColor.GRAY + "Hai scelto il kit " + ChatColor.YELLOW + ChatColor.BOLD + Kit.this.name + ChatColor.GRAY + ".");
				EasySound.quickPlay(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.4f);
				apply(gamer);
			}
		});
		
		this.yaml = config;

		id = config.getString("id");
		name = config.getString("name");
		
		items = Lists.newArrayList();
		potionEffects = Lists.newArrayList();
		
		mode = config.getString("mode", "{UNKNOWN}");
		
		requiredLevel = config.getInt("level");
		if (requiredLevel < 0) {
			requiredLevel = 0;
		}
		
		coins = config.getInt("coins");
		
		vipOnly = config.getBoolean("vip");
		
		String locString = yaml.getString("display-location");
		if (locString != null) {
			try {
				Location loc = Serializer.locationFromString(locString);
				statue.setLocation(loc);
			} catch (IllegalArgumentException e) {
				PvPGames.logPurple("Location non valida nel kit " + yaml.getFileName() + "!");
			}
		}
		
		// Oggetti
		List<String> itemsBundles = config.getStringList("items");
		List<ItemStackWrapper> itemWrappersLocal = Lists.newArrayList();
		
		if (itemsBundles != null) {
			
			for (String itemBundle : itemsBundles) {
				int position = -1;
				
				if (itemBundle.matches("^[0-9]+\\|.+")) {
					String[] pieces = itemBundle.split("\\|", 2);
					
					try {
						position = Integer.parseInt(pieces[0]);
					} catch (NumberFormatException e) {
						PvPGames.logPurple("Posizione non valida nel kit '" + yaml.getFileName() + "' (" + e.getMessage() + "): " + itemBundle);
					}
					itemBundle = pieces[1];
				}
				
				try {
					JsonReturner jsonReturner = new JsonReturner();
					ItemStack item = ItemParser.parse(itemBundle, jsonReturner);
					ItemStackWrapper itemWrapper = new ItemStackWrapper(item, position);
					
					if (jsonReturner.getJson() != null) {
						if (jsonReturner.getJson().has("helmet")) {
							itemWrapper.setForceHelmet(jsonReturner.getJson().get("helmet").getAsBoolean());
						}
						
						if (jsonReturner.getJson().has("stack")) {
							itemWrapper.setForceStacking(jsonReturner.getJson().get("stack").getAsBoolean());
						}
					}
					
					itemWrappersLocal.add(itemWrapper);
				
				} catch (ParserException e) {
					PvPGames.logPurple("Oggetto non valido nel kit '" + yaml.getFileName() + "' (" + e.getMessage() + "): " + itemBundle);
				}
			}
			
			// Ora iteriamo e distribuiamo le cose negli slot
			for (ItemStackWrapper itemWrapper : itemWrappersLocal) {
				
				ItemStack item = itemWrapper.getItemStack();
				Integer position = itemWrapper.getPosition();
				
				if (item.getDurability() == 0 && isWeaponOrArmor(item.getType())) { // 0 = massima durabilità
					ItemMeta meta = item.getItemMeta();
					meta.spigot().setUnbreakable(true);
					item.setItemMeta(meta);
				}
				
				if ((PlayerUtils.isHelmet(item) || itemWrapper.isForceHelmet()) && helmet == null) {
					helmet = item;
				} else if (PlayerUtils.isChestplate(item) && chestplate == null) {
					chestplate = item;
				} else if (PlayerUtils.isLeggings(item) && leggings == null) {
					leggings = item;
				} else if (PlayerUtils.isBoots(item) && boots == null) {
					boots = item;
				} else if (PlayerUtils.isShield(item) && shield == null) {
					shield = item;
				} else {
					
					if (item.getAmount() > item.getMaxStackSize() && !itemWrapper.isForceStacking()) {
						int remaining = item.getAmount();
						
						while (remaining > item.getMaxStackSize()) {
							remaining -= item.getMaxStackSize();
							
							ItemStack clone = item.clone();
							clone.setAmount(item.getMaxStackSize());
							items.add(new ItemAndPosition(clone, position));
						}
						
						ItemStack clone = item.clone();
						clone.setAmount(remaining);
						items.add(new ItemAndPosition(clone, position));
						
					} else {
						items.add(new ItemAndPosition(item, position));
					}
				}
			}
		}
		
		// Pozioni
		List<String> potionsBundles = config.getStringList("effects");
		List<PotionEffect> iconMenuPotionEffects = Lists.newArrayList();
		
		if (potionsBundles != null) {
			for (String potionBundle : potionsBundles) {
				boolean show = true;
				if (potionBundle.contains("hidden")) {
					potionBundle = potionBundle.replace("hidden", "");
					show = false;
				}
				try {
					PotionEffect potionEffect = PotionEffectParser.parse(potionBundle, true, show);
					potionEffects.add(potionEffect);
					if (show) {
						iconMenuPotionEffects.add(potionEffect);
					}
				} catch (ParserException ex) {
					PvPGames.logPurple("Effetto non valido nel kit '" + yaml.getFileName() + "' (" + ex.getMessage() + "): " + potionBundle);
				}
			}
		}
		
		boolean hasSomeArmor = countNonNull(helmet, chestplate, leggings, boots, shield) > 0;
		
		int rows = 0;
		int totalItems = items.size();
		if (hasSomeArmor) {
			rows++; // Una riga solo per l'armatura
		}
		rows += totalItems % 9 == 0 ? totalItems / 9 : (totalItems / 9) + 1;
		rows += potionsBundles.size() % 9 == 0 ? potionsBundles.size() / 9 : (potionsBundles.size() / 9) + 1;
		showcaseMenu = new IconMenu("Kit " + name, rows);
		
		int index = 0;
		
		for (PotionEffect potionEffect : iconMenuPotionEffects) {
			ItemStack potionItem = ItemBuilder.of(Material.POTION)
					.name(ChatColor.LIGHT_PURPLE + Format.formatEffectName(potionEffect))
					.lore(ChatColor.GRAY + Format.formatEffectDuration(potionEffect))
					.amount(potionEffect.getAmplifier() + 1)
					.build();
			
			PotionMeta meta = (PotionMeta) potionItem.getItemMeta();
			
			PotionType potionType = null;
			for (PotionType test : PotionType.values()) {
				if (potionEffect.getType().equals(test.getEffectType())) {
					potionType = test;
				}
			}
			if (potionType == null) {
				potionType = PotionType.INVISIBILITY; // Alcuni effetti non hanno pozioni, e quindi non sono visualizzabili, usiamo un colore grigio
			}
			meta.setBasePotionData(new PotionData(potionType));
			potionItem.setItemMeta(meta);
			
			Icon potionIcon = new StaticIcon(potionItem, true);
			showcaseMenu.setIconRaw(index, potionIcon);
			index++;
		}
		index = nextMenuLine(index); // Nuova riga se sono state messe delle pozioni
		
		if (helmet != null) {
			showcaseMenu.setIconRaw(index, new ItemStackIcon(helmet));
			index++;
		}
		if (chestplate != null) {
			showcaseMenu.setIconRaw(index, new ItemStackIcon(chestplate));
			index++;
		}
		if (leggings != null) {
			showcaseMenu.setIconRaw(index, new ItemStackIcon(leggings));
			index++;
		}
		if (boots != null) {
			showcaseMenu.setIconRaw(index, new ItemStackIcon(boots));
			index++;
		}
		if (shield != null) {
			showcaseMenu.setIconRaw(index, new ItemStackIcon(shield));
			index++;
		}
		index = nextMenuLine(index); // Nuova riga se è stata messa dell'armatura

		// Oggetti rimanenti
		for (ItemAndPosition itemAndPos : items) {
			showcaseMenu.setIconRaw(index, new ItemStackIcon(itemAndPos.getItem()));
			index++;
		}
		
		showcaseMenu.refresh();
		updateDisplayMob();
	}
	
	private int nextMenuLine(int i) {
		if (i % 9 == 0) {
			return i; // Siamo già su una nuova riga
		} else {
			// Prende la riga corrente e la aumenta
			int currentLine = i / 9;
			currentLine++;
			return currentLine * 9;
		}
	}
	
	private int countNonNull(Object... objects) {
		int count = 0;
		for (Object o : objects) {
			if (o != null) {
				count++;
			}
		}
		return count;
	}
	
	// Di default durabilità infinita per armi e armature
	private boolean isWeaponOrArmor(Material type) {
		switch (type) {
			case DIAMOND_SWORD:
			case DIAMOND_AXE:
			case DIAMOND_HELMET:
			case DIAMOND_CHESTPLATE:
			case DIAMOND_LEGGINGS:
			case DIAMOND_BOOTS:
			case GOLD_SWORD:
			case GOLD_AXE:
			case GOLD_HELMET:
			case GOLD_CHESTPLATE:
			case GOLD_LEGGINGS:
			case GOLD_BOOTS:
			case IRON_SWORD:
			case IRON_AXE:
			case IRON_HELMET:
			case IRON_CHESTPLATE:
			case IRON_LEGGINGS:
			case IRON_BOOTS:
			case STONE_SWORD:
			case STONE_AXE:
			case LEATHER_HELMET:
			case LEATHER_CHESTPLATE:
			case LEATHER_LEGGINGS:
			case LEATHER_BOOTS:
			case WOOD_SWORD:
			case WOOD_AXE:
			case CHAINMAIL_HELMET:
			case CHAINMAIL_CHESTPLATE:
			case CHAINMAIL_LEGGINGS:
			case CHAINMAIL_BOOTS:
			case BOW:
			case FISHING_ROD:
			case SHIELD:
				return true;
			default:
				return false;
		}
	}

	public void apply(PvPGamer pvpGamer) {
		Player player = pvpGamer.getPlayer();
		SavedKit savedKitPosition = pvpGamer.getSavedKit(this.getId());
		
		WildCommons.removePotionEffects(player);
		WildCommons.clearInventoryFully(player);
		CosmeticsBridge.updateCosmetics(player, CosmeticsBridge.Status.GAME); // Per essere in game, serve per forza un kit
		PlayerInventory inv = player.getInventory();
		
		if (helmet != null) {
			inv.setHelmet(helmet);
		}
		
		if (chestplate != null) {
			inv.setChestplate(chestplate);
		}
		
		if (leggings != null) {
			inv.setLeggings(leggings);
		}
		
		if (boots != null) {
			inv.setBoots(boots);
		}
		
		if (shield != null) {
			inv.setItemInOffHand(shield);
		}
		
		if (savedKitPosition != null && savedKitPosition.getInventoryPositions().size() == items.size()) {
			// Ok, c'è lo stesso numero di oggetti
			for (int i = 0; i < items.size(); i++) {
				int position = savedKitPosition.getInventoryPositions().get(i);
				inv.setItem(position, items.get(i).getItem());
			}
			
		} else {
			// Distribuzione normale
			for (ItemAndPosition itemAndPos : items) {
				int position = itemAndPos.getPosition();
				
				if (position < 0) {
					inv.addItem(itemAndPos.getItem());
				} else {
					while (position < inv.getSize() && inv.getItem(position) != null && inv.getItem(position).getType() != Material.AIR) {
						position++;
					}
					
					if (inv.getItem(position) != null && inv.getItem(position).getType() != Material.AIR) {
						// Non c'è proprio spazio
						inv.addItem(itemAndPos.getItem());
					} else {
						inv.setItem(position, itemAndPos.getItem());
					}
				}
			}
		}
		
		if (potionEffects.size() > 0) {
			for (PotionEffect potionEffect : potionEffects) {
				player.addPotionEffect(potionEffect, true);
			}
		}
	}
	
	public void setMobLocation(Location loc) throws IOException {
		loc.setPitch(0);
		if (Math.abs(loc.getYaw()) % 90.0f != 0.0f) {
			
			float yawNormalized = Math.round(loc.getYaw() / 90.0f) * 90.0f;
			loc.setYaw(yawNormalized);
		}
		
		loc.setX(Math.round(loc.getX() * 2.0) / 2.0);
		loc.setZ(Math.round(loc.getZ() * 2.0) / 2.0);
		
		statue.setLocation(loc);
		yaml.set("display-location", Serializer.locationToString(loc));
		yaml.save();
	}
	
	public void updateDisplayMob() {
		
		statue.getHologramLines().clear();
		statue.getHologramLines().add("" + ChatColor.YELLOW + ChatColor.BOLD + name + (vipOnly ? " §8§l[§6§lVIP§8§l]" : ""));
		if (requiredLevel > 0) {
			statue.getHologramLines().add(ChatColor.WHITE + "Livello minimo: " + requiredLevel);
		}
		if (coins > 0) {
			statue.getHologramLines().add(ChatColor.WHITE + "Prezzo: " + coins + " Coins");
		}
		
		statue.update();
		
		if (statue.getMob() instanceof HologramEquippable) {
			
			HologramEquippable hologramMob = (HologramEquippable) statue.getMob();
			
			if (!items.isEmpty()) {
				hologramMob.setItemInHand(items.get(0).getItem());
			}
			if (helmet != null) {
				hologramMob.setHelmet(helmet);
			}
			if (chestplate != null) {
				hologramMob.setChestplate(chestplate);
			}
			if (leggings != null) {
				hologramMob.setLeggings(leggings);
			}
			if (boots != null) {
				hologramMob.setBoots(boots);
			}
			if (shield != null) {
				hologramMob.setItemInOffHand(shield);
			}
			
			hologramMob.update();
		}
	}

	@Override
	public boolean equals(Object obj) {
		
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		
		Kit other = (Kit) obj;
		return other.name.equals(this.name);
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
}
