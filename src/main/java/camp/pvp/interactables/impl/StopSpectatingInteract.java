package camp.pvp.interactables.impl;

import camp.pvp.events.games.SpectateEndEvent;
import camp.pvp.interactables.ItemInteract;
import camp.pvp.profiles.GameProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class StopSpectatingInteract implements ItemInteract {
    @Override
    public void onInteract(Player player, GameProfile gameProfile) {
        Bukkit.getServer().getPluginManager().callEvent(new SpectateEndEvent(gameProfile.getGame(), player));
    }
}
