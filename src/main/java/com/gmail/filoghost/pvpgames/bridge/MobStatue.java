package com.gmail.filoghost.pvpgames.bridge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPC.Metadata;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.gmail.filoghost.pvpgames.PvPGames;

public class MobStatue {

	private static final Map<Integer, MobStatue> NPCS = new HashMap<Integer, MobStatue>();

	@Getter @Setter
	private EntityType type;

	@Getter
	private NPC mob;

	@Setter
	private ClickHandler clickHandler;

	@Getter @Setter
	private List<String> hologramLines;

	@Getter @Setter
	private Location location;

	@Getter
	private Hologram hologram;

	public void update() {
		delete();

		if (type == null || location == null) {
			return;
		}

		mob = CitizensAPI.getNPCRegistry().createNPC(type, "");
		mob.setName("");
		mob.setProtected(true);
		mob.data().setPersistent(Metadata.NAMEPLATE_VISIBLE, false);
		mob.data().setPersistent(Metadata.DEFAULT_PROTECTED, true);
		mob.data().setPersistent(Metadata.USE_MINECRAFT_AI, false);

		if (!mob.spawn(location)) {
			mob = null;
			return;
		}

		Entity entity = mob.getEntity();
		if (entity != null) {
			entity.setCustomNameVisible(false);
		}

		NPCS.put(mob.getId(), this);

		if (hologramLines != null && !hologramLines.isEmpty()) {
			Location hologramLocation = location.clone().add(0.0, getHologramOffset(type), 0.0);
			hologram = PvPGames.getInstance().getHolographicDisplaysAPI().createHologram(hologramLocation);

			for (String line : hologramLines) {
				hologram.getLines().appendText(line);
			}
		}
	}

	public void delete() {
		if (mob != null) {
			NPCS.remove(mob.getId());
			mob.destroy();
			mob = null;
		}

		if (hologram != null) {
			hologram.delete();
			hologram = null;
		}
	}

	public void handleClick(Player player) {
		if (clickHandler != null) {
			clickHandler.onClick(player);
		}
	}

	public static MobStatue getByNPCId(int npcId) {
		return NPCS.get(npcId);
	}

	private double getHologramOffset(EntityType type) {
        return switch (type) {
            case PLAYER, ZOMBIE, SKELETON, CREEPER -> 2.2;
            case ENDERMAN -> 2.9;
            case SLIME, MAGMA_CUBE -> 1.8;
            case GIANT -> 6.5;
            default -> 2.0;
        };
	}

	public static interface ClickHandler {
		void onClick(Player player);
	}
}