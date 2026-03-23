package com.gmail.filoghost.pvpgames.placeholder;

import com.gmail.filoghost.pvpgames.PvPGames;
import com.gmail.filoghost.pvpgames.player.Mode;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public class ModePlayerExpansion extends PlaceholderExpansion {

    @Override
    public @Nonnull String getIdentifier() {
        return "modeplayers";
    }

    @Override
    public @Nonnull String getAuthor() {
        return "filoghost";
    }

    @Override
    public @Nonnull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @Nonnull String identifier) {
        if (identifier.isEmpty()) {
            return "0";
        }

        Mode mode = PvPGames.getInstance().getModeById(identifier.toLowerCase().trim());

        return mode != null ? String.valueOf(mode.getCurrentPlayers().size()) : "0";
    }
}