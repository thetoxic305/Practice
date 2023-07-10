package camp.pvp.listeners.bukkit.player;

import camp.pvp.Practice;
import camp.pvp.profiles.GameProfile;
import camp.pvp.profiles.GameProfileManager;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class PlayerInteractEntityListener implements Listener {

    private Practice plugin;
    public PlayerInteractEntityListener(Practice plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        if (entity instanceof Player) {
            Player target = (Player) entity;
            GameProfileManager gpm = plugin.getGameProfileManager();
            GameProfile profile = gpm.getLoadedProfiles().get(player.getUniqueId());
            GameProfile targetProfile = gpm.getLoadedProfiles().get(target.getUniqueId());
            if(player.getItemInHand() == null || (player.getItemInHand() != null && player.getItemInHand().getType().equals(Material.AIR))) {
                if(!profile.getState().equals(GameProfile.State.LOBBY) && !targetProfile.getState().equals(GameProfile.State.LOBBY)) {
                    player.performCommand("duel " + target.getName());
                }
            }
        }
    }
}
