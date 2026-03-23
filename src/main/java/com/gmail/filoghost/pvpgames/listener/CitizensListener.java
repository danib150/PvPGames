package com.gmail.filoghost.pvpgames.listener;

import com.gmail.filoghost.pvpgames.bridge.MobStatue;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class CitizensListener implements Listener {

    @EventHandler
    public void onNPCRightClick(NPCRightClickEvent event) {
        MobStatue statue = MobStatue.getByNPCId(event.getNPC().getId());

        if (statue != null) {
            statue.handleClick(event.getClicker());
        }
    }

}
